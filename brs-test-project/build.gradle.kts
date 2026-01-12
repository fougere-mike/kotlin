// BrightScript Test Project
// Standalone project to test Kotlin-to-BrightScript compilation

// Use dist compiler (includes all runtime dependencies in the distribution)
val distLibDir = file("../dist/kotlinc/lib")
val stdlibKlib = file("../libraries/stdlib/brs-prebuilt/kotlin-stdlib-brs.klib")

tasks.register<JavaExec>("compileBrs") {
    group = "brightscript"
    description = "Compile Kotlin sources to BrightScript with stdlib"

    mainClass.set("org.jetbrains.kotlin.cli.brs.K2BrsCompiler")
    classpath = fileTree(distLibDir) { include("*.jar") }

    val srcDir = file("src/main/kotlin")
    val outDir = file("build/output")

    // Verify compiler and stdlib klib exist before compilation
    doFirst {
        val compilerJar = distLibDir.resolve("kotlin-compiler.jar")
        if (!compilerJar.exists()) {
            throw GradleException(
                "Kotlin distribution not found at: ${distLibDir.absolutePath}\n" +
                "Run: ./gradlew dist (from parent project)"
            )
        }
        if (!stdlibKlib.exists()) {
            throw GradleException(
                "Stdlib klib not found: ${stdlibKlib.absolutePath}\n" +
                "Run: ./gradlew :kotlin-stdlib-brs-prebuilt:regenerateKlib"
            )
        }
    }

    args("-libraries", stdlibKlib.absolutePath)
    args("-output-dir", outDir.absolutePath)
    args("-min-roku-os", "9.4")

    // Add all .kt files from src directory
    doFirst {
        outDir.mkdirs()
        val ktFiles = fileTree(srcDir).matching { include("**/*.kt") }.files
        if (ktFiles.isEmpty()) {
            throw GradleException("No .kt files found in $srcDir")
        }
        args(ktFiles.map { it.absolutePath })
        println("Compiling ${ktFiles.size} Kotlin file(s) to BrightScript...")
        ktFiles.forEach { println("  - ${it.name}") }
    }
}

tasks.register("clean") {
    group = "build"
    description = "Clean build output"
    doLast {
        delete(file("build"))
    }
}
