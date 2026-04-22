plugins {
    kotlin("jvm")
    id("jps-compatible")
    id("generated-sources")
}

dependencies {
    api(project(":core:compiler.common.brightscript"))
    api(project(":compiler:fir:checkers"))
    api(project(":compiler:fir:providers"))  // For FirDeclarationGenerationExtension
    api(project(":compiler:fir:plugin-utils"))  // For createMemberProperty, createNestedClass, etc.
    // Note: FirExtensionRegistrar is in :compiler:fir:entrypoint but we can't add it here
    // due to circular dependency. The extension registrar is registered via cli-brs instead.

    /*
     * We can't remove this dependency until we use
     *   diagnostics framework from FE 1.0
     */
    implementation(project(":compiler:frontend"))
    implementation(project(":compiler:psi:psi-api"))

    compileOnly(intellijCore())
}

sourceSets {
    "main" {
        projectDefault()
    }
    "test" { none() }
}

generatedDiagnosticContainersAndCheckerComponents()
