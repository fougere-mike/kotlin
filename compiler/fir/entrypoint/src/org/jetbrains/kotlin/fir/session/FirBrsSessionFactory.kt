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
import org.jetbrains.kotlin.fir.analysis.brs.checkers.FirBrsIdentityLessPlatformDeterminer
import org.jetbrains.kotlin.fir.analysis.brs.checkers.FirBrsPlatformDiagnosticSuppressor
import org.jetbrains.kotlin.fir.analysis.checkers.FirIdentityLessPlatformDeterminer
import org.jetbrains.kotlin.fir.analysis.checkers.FirPlatformDiagnosticSuppressor
import org.jetbrains.kotlin.fir.checkers.registerBrsCheckers
import org.jetbrains.kotlin.fir.declarations.FirTypeSpecificityComparatorProvider
import org.jetbrains.kotlin.fir.deserialization.ModuleDataProvider
import org.jetbrains.kotlin.fir.deserialization.SingleModuleDataProvider
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.java.FirProjectSessionProvider
import org.jetbrains.kotlin.fir.resolve.calls.overloads.ConeCallConflictResolverFactory
import org.jetbrains.kotlin.fir.resolve.providers.impl.FirBuiltinSyntheticFunctionInterfaceProvider
import org.jetbrains.kotlin.fir.resolve.providers.impl.FirFallbackBuiltinSymbolProvider
import org.jetbrains.kotlin.fir.scopes.FirDefaultImportProviderHolder
import org.jetbrains.kotlin.fir.scopes.FirKotlinScopeProvider
import org.jetbrains.kotlin.fir.types.typeContext
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.brs.resolve.BrsPlatformAnalyzerServices
import org.jetbrains.kotlin.brs.resolve.BrsTypeSpecificityComparatorWithoutDelegate
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.name.Name

/**
 * Factory for creating FIR sessions for BrightScript compilation.
 *
 * This follows the pattern established by FirJsSessionFactory, creating
 * library sessions for dependencies and module-based sessions for source compilation.
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
                    // Fallback builtins provider - provides Int, String, etc. from compiler resources
                    FirFallbackBuiltinSymbolProvider(session, builtinsModuleData, kotlinScopeProvider),
                    // For MVP with no pre-compiled libraries, just use the klib provider if libraries exist
                    if (resolvedLibraries.isNotEmpty()) {
                        KlibBasedSymbolProvider(
                            session, moduleDataProvider, kotlinScopeProvider, resolvedLibraries,
                            flexibleTypeFactory = BrsFlexibleTypeFactory(session),
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
                            flexibleTypeFactory = BrsFlexibleTypeFactory(session),
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
        registerBrsCheckers()
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
     */
    fun FirSession.registerBrsComponents() {
        // Use BrightScript-specific call conflict resolver
        register(ConeCallConflictResolverFactory::class, BrsCallConflictResolverFactory)

        // Use BrightScript-specific type specificity comparator
        register(
            FirTypeSpecificityComparatorProvider::class,
            FirTypeSpecificityComparatorProvider(BrsTypeSpecificityComparatorWithoutDelegate(typeContext))
        )

        // Use BrightScript-specific platform diagnostic suppressor
        register(FirPlatformDiagnosticSuppressor::class, FirBrsPlatformDiagnosticSuppressor())

        // Use BrightScript-specific identity-less platform determiner
        register(FirIdentityLessPlatformDeterminer::class, FirBrsIdentityLessPlatformDeterminer)

        // Use BrightScript-specific platform analyzer services for default imports
        register(FirDefaultImportProviderHolder::class, FirDefaultImportProviderHolder(BrsPlatformAnalyzerServices))
    }

    // ==================================== Utilities ====================================

    /**
     * Context for session creation containing compiler configuration.
     */
    class Context(val configuration: CompilerConfiguration)
}
