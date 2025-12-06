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
    api(project(":brightscript:brs.ast"))
    api(project(":core:compiler.common.brightscript"))

    compileOnly(intellijCore())

    testImplementation(kotlinTest("junit"))
    testImplementation(projectTests(":compiler:tests-common-new"))
    testImplementation(project(":compiler:cli-brs"))
    testRuntimeOnly(project(":compiler:cli-common"))
    testRuntimeOnly(intellijCore())
}

optInToUnsafeDuringIrConstructionAPI()

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
