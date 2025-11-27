/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.session

import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.SessionConfiguration
import org.jetbrains.kotlin.fir.analysis.checkers.FirIdentityLessPlatformDeterminer
import org.jetbrains.kotlin.fir.analysis.checkers.FirPlatformDiagnosticSuppressor
import org.jetbrains.kotlin.fir.analysis.js.checkers.FirJsIdentityLessPlatformDeterminer
import org.jetbrains.kotlin.fir.analysis.js.checkers.FirJsPlatformDiagnosticSuppressor
import org.jetbrains.kotlin.fir.declarations.FirTypeSpecificityComparatorProvider
import org.jetbrains.kotlin.fir.deserialization.ModuleDataProvider
import org.jetbrains.kotlin.fir.deserialization.SingleModuleDataProvider
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.java.FirProjectSessionProvider
import org.jetbrains.kotlin.fir.resolve.calls.overloads.ConeCallConflictResolverFactory
import org.jetbrains.kotlin.fir.resolve.providers.impl.FirBuiltinSyntheticFunctionInterfaceProvider
import org.jetbrains.kotlin.fir.scopes.FirDefaultImportProviderHolder
import org.jetbrains.kotlin.fir.scopes.FirKotlinScopeProvider
import org.jetbrains.kotlin.fir.types.typeContext
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.js.resolve.JsPlatformAnalyzerServices
import org.jetbrains.kotlin.js.resolve.JsTypeSpecificityComparatorWithoutDelegate
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.name.Name

/**
 * Factory for creating FIR sessions for BrightScript compilation.
 *
 * This follows the pattern established by FirJsSessionFactory, creating
 * library sessions for dependencies and module-based sessions for source compilation.
 *
 * For MVP, we reuse many JS components since both BrightScript and JavaScript
 * are dynamically-typed scripting languages with similar characteristics.
 */
@OptIn(SessionConfiguration::class)
object FirBrsSessionFactory : FirAbstractSessionFactory<FirBrsSessionFactory.Context, FirBrsSessionFactory.Context>() {

    // ==================================== Library session ====================================

    /**
     * Create a library session for BrightScript compilation.
     *
     * This handles dependencies and pre-compiled libraries.
     */
    fun createLibrarySession(
        mainModuleName: Name,
        resolvedLibraries: List<KotlinLibrary>,
        sessionProvider: FirProjectSessionProvider,
        moduleDataProvider: ModuleDataProvider,
        extensionRegistrars: List<FirExtensionRegistrar>,
        compilerConfiguration: CompilerConfiguration,
    ): FirSession {
        val context = Context(compilerConfiguration)
        return createLibrarySession(
            mainModuleName,
            context,
            sessionProvider,
            moduleDataProvider,
            compilerConfiguration.languageVersionSettings,
            extensionRegistrars,
            createProviders = { session, builtinsModuleData, kotlinScopeProvider, syntheticFunctionInterfaceProvider ->
                listOfNotNull(
                    // For MVP with no pre-compiled libraries, just use the klib provider if libraries exist
                    if (resolvedLibraries.isNotEmpty()) {
                        KlibBasedSymbolProvider(
                            session, moduleDataProvider, kotlinScopeProvider, resolvedLibraries,
                            flexibleTypeFactory = JsFlexibleTypeFactory(session), // Reuse JS flexible type factory
                        )
                    } else null,
                    FirBuiltinSyntheticFunctionInterfaceProvider(session, builtinsModuleData, kotlinScopeProvider),
                    syntheticFunctionInterfaceProvider
                )
            }
        )
    }

    override fun createKotlinScopeProviderForLibrarySession(): FirKotlinScopeProvider {
        return FirKotlinScopeProvider()
    }

    override fun FirSession.registerLibrarySessionComponents(c: Context) {
        registerComponents(c.configuration)
    }

    // ==================================== Platform session ====================================

    /**
     * Create a module-based session for BrightScript source compilation.
     *
     * This is used to compile Kotlin source files to BrightScript.
     */
    fun createModuleBasedSession(
        moduleData: FirModuleData,
        sessionProvider: FirProjectSessionProvider,
        extensionRegistrars: List<FirExtensionRegistrar>,
        compilerConfiguration: CompilerConfiguration,
        lookupTracker: LookupTracker?,
        icData: KlibIcData? = null,
        init: FirSessionConfigurator.() -> Unit
    ): FirSession {
        val context = Context(compilerConfiguration)
        return createModuleBasedSession(
            moduleData,
            context,
            sessionProvider,
            extensionRegistrars,
            compilerConfiguration.languageVersionSettings,
            lookupTracker,
            enumWhenTracker = null,
            importTracker = null,
            init,
            createProviders = { session, kotlinScopeProvider, symbolProvider, generatedSymbolsProvider, dependencies ->
                listOfNotNull(
                    symbolProvider,
                    generatedSymbolsProvider,
                    icData?.let {
                        KlibIcCacheBasedSymbolProvider(
                            session,
                            SingleModuleDataProvider(moduleData),
                            kotlinScopeProvider,
                            it,
                            flexibleTypeFactory = JsFlexibleTypeFactory(session), // Reuse JS flexible type factory
                        )
                    },
                    *dependencies.toTypedArray(),
                )
            }
        )
    }

    override fun createKotlinScopeProviderForSourceSession(
        moduleData: FirModuleData,
        languageVersionSettings: LanguageVersionSettings,
    ): FirKotlinScopeProvider {
        return FirKotlinScopeProvider()
    }

    override fun FirSessionConfigurator.registerPlatformCheckers(c: Context) {
        // For MVP, we don't register BrightScript-specific checkers
        // TODO: Create and register BrightScript-specific FIR checkers
    }

    override fun FirSession.registerSourceSessionComponents(c: Context) {
        registerComponents(c.configuration)
    }

    // ==================================== Common parts ====================================

    private fun FirSession.registerComponents(compilerConfiguration: CompilerConfiguration) {
        registerDefaultComponents()
        registerBrsComponents()
    }

    /**
     * Register BrightScript-specific session components.
     *
     * For MVP, we reuse many JS components since BrightScript and JavaScript
     * have similar characteristics (dynamic typing, scripting language).
     */
    fun FirSession.registerBrsComponents() {
        // Use the JS call conflict resolver (similar dynamic typing behavior)
        register(ConeCallConflictResolverFactory::class, JsCallConflictResolverFactory)

        // Use JS type specificity comparator (similar type system behavior)
        register(
            FirTypeSpecificityComparatorProvider::class,
            FirTypeSpecificityComparatorProvider(JsTypeSpecificityComparatorWithoutDelegate(typeContext))
        )

        // Use JS platform diagnostic suppressor for now
        // TODO: Create BrightScript-specific diagnostic suppressor
        register(FirPlatformDiagnosticSuppressor::class, FirJsPlatformDiagnosticSuppressor())

        // Use JS identity-less platform determiner for now
        // TODO: Create BrightScript-specific platform determiner
        register(FirIdentityLessPlatformDeterminer::class, FirJsIdentityLessPlatformDeterminer)

        // Use JS platform analyzer services for default imports
        // TODO: Create BrsPlatformAnalyzerServices in a proper brs.frontend module
        register(FirDefaultImportProviderHolder::class, FirDefaultImportProviderHolder(JsPlatformAnalyzerServices))
    }

    // ==================================== Utilities ====================================

    /**
     * Context for session creation containing compiler configuration.
     */
    class Context(val configuration: CompilerConfiguration)
}
