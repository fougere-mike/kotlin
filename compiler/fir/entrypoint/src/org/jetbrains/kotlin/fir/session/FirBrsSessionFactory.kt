/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.session

import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.SessionConfiguration
import org.jetbrains.kotlin.fir.analysis.brs.checkers.FirBrsIdentityLessPlatformDeterminer
import org.jetbrains.kotlin.fir.analysis.brs.checkers.FirBrsPlatformDiagnosticSuppressor
import org.jetbrains.kotlin.fir.analysis.checkers.FirIdentityLessPlatformDeterminer
import org.jetbrains.kotlin.fir.analysis.checkers.FirPlatformDiagnosticSuppressor
import org.jetbrains.kotlin.fir.checkers.registerBrsCheckers
import org.jetbrains.kotlin.fir.declarations.FirTypeSpecificityComparatorProvider
import org.jetbrains.kotlin.fir.deserialization.FirTypeDeserializer
import org.jetbrains.kotlin.fir.resolve.calls.overloads.ConeCallConflictResolverFactory
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.impl.FirFallbackBuiltinSymbolProvider
import org.jetbrains.kotlin.fir.scopes.FirDefaultImportProviderHolder
import org.jetbrains.kotlin.fir.scopes.FirKotlinScopeProvider
import org.jetbrains.kotlin.fir.types.typeContext
import org.jetbrains.kotlin.brs.resolve.BrsPlatformAnalyzerServices
import org.jetbrains.kotlin.brs.resolve.BrsTypeSpecificityComparatorWithoutDelegate

/**
 * Factory for creating FIR sessions for BrightScript compilation.
 *
 * This follows the pattern established by FirJsSessionFactory, creating
 * library sessions for dependencies and module-based sessions for source compilation.
 */
@OptIn(SessionConfiguration::class)
object FirBrsSessionFactory : AbstractFirKlibSessionFactory<FirBrsSessionFactory.Context, FirBrsSessionFactory.Context>() {

    // ==================================== Library session ====================================

    override fun createLibraryContext(configuration: CompilerConfiguration): Context {
        return Context(configuration)
    }

    override fun createFlexibleTypeFactory(session: FirSession): FirTypeDeserializer.FlexibleTypeFactory {
        return BrsFlexibleTypeFactory(session)
    }

    override fun FirSession.registerLibrarySessionComponents(c: Context) {
        registerComponents(c.configuration)
    }

    /**
     * Creates platform-specific shared providers for BrightScript.
     * This includes the FirFallbackBuiltinSymbolProvider which is needed to resolve
     * built-in types (Any, Nothing, Unit, Int, Boolean, etc.) during compilation.
     *
     * Unlike JVM which loads builtins from classfiles, BRS (like JS/Native/Wasm) uses
     * the fallback provider which loads builtins from serialized .kotlin_builtins files
     * embedded in the compiler resources.
     */
    override fun createPlatformSpecificSharedProviders(
        session: FirSession,
        moduleData: FirModuleData,
        scopeProvider: FirKotlinScopeProvider,
        context: Context,
    ): List<FirSymbolProvider> {
        return listOf(
            FirFallbackBuiltinSymbolProvider(session, moduleData, scopeProvider)
        )
    }

    // ==================================== Platform session ====================================

    override fun createSourceContext(configuration: CompilerConfiguration): Context {
        return Context(configuration)
    }

    override fun FirSessionConfigurator.registerPlatformCheckers(c: Context) {
        registerBrsCheckers()
    }

    override fun FirSessionConfigurator.registerExtraPlatformCheckers(c: Context) {}

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
