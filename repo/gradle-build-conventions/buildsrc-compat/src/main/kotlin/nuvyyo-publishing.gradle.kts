/*
 * Registers the Nuvyyo GitHub Packages Maven repository on every subproject that
 * applies maven-publish, and fixes published dependency coordinates.
 *
 * Credentials are read from ~/.gradle/gradle.properties:
 *
 *   nuvyyoGitHubUser=<github-username>
 *   nuvyyoGitHubToken=<classic-pat-with-read:packages-or-write:packages>
 *
 * Publishing to the remote repo requires write:packages + repo scope on the PAT.
 * Consuming artifacts only requires read:packages.
 *
 * COORDINATE FIX: the fork sets group=com.nuvyyo globally, so generated POM and
 * Gradle-module-metadata files reference sibling modules the fork does NOT publish
 * (kotlin-native-utils, kotlin-gradle-plugin-model, kotlin-test, ...) as
 * com.nuvyyo:<module>:<brs-version>. Only *-brs / *-brs-runtime artifacts exist under
 * com.nuvyyo; everything else lives at org.jetbrains.kotlin:<module>:<upstream base
 * version>. Rewriting here, at publish time, means consumers (kotlin-roku, Roku apps)
 * need no dependency-substitution blocks.
 */
import org.gradle.api.publish.tasks.GenerateModuleMetadata

plugins {
    `maven-publish`
}

val upstreamKotlinVersion: String = project.version.toString().substringBefore("-brs")

fun publishedUnderNuvyyo(module: String): Boolean =
    module.endsWith("-brs") || module.endsWith("-brs-runtime")

publishing {
    repositories {
        maven {
            name = "nuvyyo"
            url = uri("https://maven.pkg.github.com/nuvyyo/maven-brs")
            credentials {
                username = providers.gradleProperty("nuvyyoGitHubUser").orNull
                password = providers.gradleProperty("nuvyyoGitHubToken").orNull
            }
        }
    }

    publications.withType<MavenPublication>().configureEach {
        pom.withXml {
            val root = asNode()
            fun childNodes(parent: groovy.util.Node, name: String): List<groovy.util.Node> =
                (parent.get(name) as groovy.util.NodeList).filterIsInstance<groovy.util.Node>()
            fun rewrite(dependency: groovy.util.Node) {
                val group = childNodes(dependency, "groupId").firstOrNull() ?: return
                val artifact = childNodes(dependency, "artifactId").firstOrNull() ?: return
                if (group.text() != "com.nuvyyo" || publishedUnderNuvyyo(artifact.text())) return
                group.setValue("org.jetbrains.kotlin")
                childNodes(dependency, "version").firstOrNull()?.setValue(upstreamKotlinVersion)
            }
            childNodes(root, "dependencies")
                .flatMap { childNodes(it, "dependency") }
                .forEach(::rewrite)
            childNodes(root, "dependencyManagement")
                .flatMap { childNodes(it, "dependencies") }
                .flatMap { childNodes(it, "dependency") }
                .forEach(::rewrite)
        }
    }
}

tasks.withType<GenerateModuleMetadata>().configureEach {
    doLast {
        val file = outputFile.get().asFile
        @Suppress("UNCHECKED_CAST")
        val json = groovy.json.JsonSlurper().parse(file) as MutableMap<String, Any?>
        @Suppress("UNCHECKED_CAST")
        val variants = json["variants"] as? List<MutableMap<String, Any?>> ?: emptyList()
        var rewritten = 0
        for (variant in variants) {
            for (key in listOf("dependencies", "dependencyConstraints")) {
                @Suppress("UNCHECKED_CAST")
                val deps = variant[key] as? List<MutableMap<String, Any?>> ?: continue
                for (dep in deps) {
                    val module = dep["module"] as? String ?: continue
                    if (dep["group"] != "com.nuvyyo" || publishedUnderNuvyyo(module)) continue
                    dep["group"] = "org.jetbrains.kotlin"
                    @Suppress("UNCHECKED_CAST")
                    val version = dep["version"] as? MutableMap<String, Any?>
                    version?.keys?.toList()?.forEach { version[it] = upstreamKotlinVersion }
                    rewritten++
                }
            }
        }
        if (rewritten > 0) {
            file.writeText(groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(json)))
            logger.lifecycle("nuvyyo-publishing: rewrote $rewritten dependency coordinate(s) in ${file.name}")
        }
    }
}
