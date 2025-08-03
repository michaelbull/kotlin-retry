import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform

plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Empty(),
            sourcesJar = true,
        )
    )

    pom {
        name.set(project.name)
        description.set(project.description)
        url.set("https://github.com/michaelbull/kotlin-retry")
        inceptionYear.set("2019")

        licenses {
            license {
                name.set("ISC License")
                url.set("https://opensource.org/licenses/isc-license.txt")
            }
        }

        developers {
            developer {
                name.set("Michael Bull")
                url.set("https://www.michael-bull.com")
            }
        }

        contributors {
            contributor {
                name.set("Nicolas Dermine")
                url.set("https://github.com/nicoder")
            }

            contributor {
                name.set("Thorsten Hake")
                url.set("http://www.thorsten-hake.com/")
            }

            contributor {
                name.set("gnefedev")
                url.set("https://github.com/gnefedev")
            }

            contributor {
                name.set("cherrydev")
                url.set("https://github.com/cherrydev")
            }

            contributor {
                name.set("Joose Fjällström")
                url.set("https://github.com/jfjallstrom")
            }
        }

        scm {
            connection.set("scm:git:https://github.com/michaelbull/kotlin-retry")
            developerConnection.set("scm:git:git@github.com:michaelbull/kotlin-retry.git")
            url.set("https://github.com/michaelbull/kotlin-retry")
        }

        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/michaelbull/kotlin-retry/issues")
        }

        ciManagement {
            system.set("GitHub Actions")
            url.set("https://github.com/michaelbull/kotlin-retry/actions")
        }
    }
}
