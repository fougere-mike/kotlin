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

    testImplementation(kotlinTest("junit5"))
    testImplementation(project(":compiler:cli-brs"))
    testImplementation(project(":compiler:cli-common"))
    testImplementation(project(":compiler:cli"))
    testRuntimeOnly(intellijCore())
    // Required for IntelliJ plugin loading in Kotlin 2.2.x
    testRuntimeOnly(commonDependency("org.codehaus.woodstox:stax2-api"))
    testRuntimeOnly(commonDependency("com.fasterxml:aalto-xml"))
}

sourceSets {
    "main" {
        projectDefault()
    }
    "test" {
        projectDefault()
    }
}

tasks.test {
    // Run tests from the repo root so the compiler can find testData and the prebuilt klib
    workingDir = rootDir
    useJUnitPlatform()
    // Diagnostic fixtures are a tracked input (mirrors the golden-testData fix in
    // compiler/ir/backend.brightscript): without this, a fixture-only edit leaves the
    // task UP-TO-DATE and silently serves stale results.
    inputs.dir(rootDir.resolve("compiler/testData/diagnostics/testsWithBrsStdLib"))
}

generatedDiagnosticContainersAndCheckerComponents()
