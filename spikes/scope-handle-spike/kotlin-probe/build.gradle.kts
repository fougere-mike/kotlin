buildscript {
    // Same coordinate redirect as roku-test-app: the KGP -brs POMs declare
    // transitive utility deps under com.nuvyyo:* (fork-global group), but those
    // utilities are only published as org.jetbrains.kotlin:*:2.2.20.
    configurations.all {
        resolutionStrategy.dependencySubstitution {
            all {
                val req = requested
                if (req is ModuleComponentSelector &&
                    req.group == "com.nuvyyo" &&
                    !req.module.endsWith("-brs") &&
                    !req.module.endsWith("-brs-runtime")
                ) {
                    useTarget("org.jetbrains.kotlin:${req.module}:2.2.20")
                }
            }
        }
    }
}

plugins {
    id("com.nuvyyo.brightscript.kotlin-roku") version "2.2.20-brs.1"
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        all {
            val req = requested
            if (req is ModuleComponentSelector &&
                req.group == "com.nuvyyo" &&
                !req.module.endsWith("-brs") &&
                !req.module.endsWith("-brs-runtime")
            ) {
                useTarget("org.jetbrains.kotlin:${req.module}:2.2.20")
            }
        }
    }
}

roku {
    appName.set("ScopeSpikeKotlinProbe")
    appVersion.set("1.0.0")
    minRokuOS.set("10.0")
}
