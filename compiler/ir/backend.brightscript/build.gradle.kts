plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    api(project(":compiler:util"))
    api(project(":compiler:frontend"))
    api(project(":compiler:backend-common"))
    api(project(":compiler:ir.tree"))
    api(project(":compiler:ir.backend.common"))
    api(project(":compiler:ir.serialization.common"))
    api(project(":compiler:ir.inline"))
    api(project(":brightscript:brs.ast"))
    api(project(":core:compiler.common.brightscript"))

    compileOnly(intellijCore())

    testImplementation(kotlinTest("junit"))
    testImplementation(projectTests(":compiler:tests-common-new"))
    testImplementation(project(":compiler:cli-brs"))
    testRuntimeOnly(project(":compiler:cli-common"))
    testRuntimeOnly(intellijCore())
    // Required for IntelliJ plugin loading in Kotlin 2.2.x
    testRuntimeOnly(commonDependency("org.codehaus.woodstox:stax2-api"))
    testRuntimeOnly(commonDependency("com.fasterxml:aalto-xml"))
}

optInToUnsafeDuringIrConstructionAPI()
optInTo("org.jetbrains.kotlin.DeprecatedForRemovalCompilerApi")

sourceSets {
    "main" { projectDefault() }
    "test" { projectDefault() }
}

tasks.test {
    // Run tests from the repo root so the compiler can find its resources
    workingDir = rootDir

    // Pass the golden file update flag to tests
    systemProperty(
        "kotlin.test.update.golden.files",
        project.findProperty("updateGoldenFiles")?.toString() ?: "false"
    )
}

// Convenience task: Run all BRS compiler tests (golden files)
tasks.register("brsTest") {
    group = "verification"
    description = "Run all BrightScript compiler tests (golden file tests)"
    dependsOn("test")
}

// Convenience task: Run only golden file tests
tasks.register("goldenFileTest") {
    group = "verification"
    description = "Run golden file tests for BrightScript code generation"
    doFirst {
        tasks.test.get().filter {
            includeTestsMatching("*GoldenFile*")
        }
    }
    finalizedBy("test")
}

// Convenience task: Update golden files with current compiler output
tasks.register("updateGoldenFiles") {
    group = "verification"
    description = "Update golden files with current BrightScript compiler output"
    doFirst {
        // Set the property before test runs
        project.extra.set("updateGoldenFiles", "true")
    }
    finalizedBy("test")
}
