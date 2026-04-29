plugins {
    kotlin("jvm")
    id("jps-compatible")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

base.archivesName.set("kotlin-tooling-core-brs")
publish()
sourcesJar()
javadocJar()
configureKotlinCompileTasksGradleCompatibility()

dependencies {
    val coreDepsVersion = libs.versions.kotlin.`for`.gradle.plugins.compilation.get()
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:$coreDepsVersion")
    testImplementation(kotlinTest("junit"))
}

tasks {
    apiBuild {
        inputJar.value(jar.flatMap { it.archiveFile })
    }
}