/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

plugins {
    kotlin("multiplatform")
}

description = "Minimal BrightScript stdlib bootstrap - provides only intrinsics needed by the backend"

kotlin {
    js(IR) {
        nodejs()
        compilations["main"].compileTaskProvider.configure {
            compilerOptions {
                freeCompilerArgs.addAll(
                    "-Xallow-kotlin-package",
                    "-opt-in=kotlin.ExperimentalMultiplatform",
                    "-Xir-module-name=kotlin-brs-bootstrap"
                )
            }
        }
    }

    sourceSets {
        val jsMain by getting {
            kotlin.srcDir("src")
            dependencies {
                implementation(project(":kotlin-stdlib"))
            }
        }
    }
}
