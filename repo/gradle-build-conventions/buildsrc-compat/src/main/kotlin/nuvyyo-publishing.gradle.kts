/*
 * Registers the Nuvyyo GitHub Packages Maven repository on every subproject that
 * applies maven-publish. Credentials are read from ~/.gradle/gradle.properties:
 *
 *   nuvyyoGitHubUser=<github-username>
 *   nuvyyoGitHubToken=<classic-pat-with-read:packages-or-write:packages>
 *
 * Publishing to the remote repo requires write:packages + repo scope on the PAT.
 * Consuming artifacts only requires read:packages.
 */
plugins {
    `maven-publish`
}

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
}
