import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ossrhUsername: String? by project
val ossrhPassword: String? by project

val signingKeyId: String? by project // must be the last 8 digits of the key
val signingKey: String? by project
val signingPassword: String? by project

description = "A higher-order function for retrying operations that may fail."

plugins {
    `maven-publish`
    signing
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.dokka") version "1.7.10"
    id("com.github.ben-manes.versions") version "0.42.0"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.16")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation("io.mockk:mockk:1.12.5")
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        listOf("alpha", "beta", "rc", "cr", "m", "eap", "pr", "dev").any {
            candidate.version.contains(it, ignoreCase = true)
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-opt-in=kotlin.contracts.ExperimentalContracts")
    }
}

tasks.withType<Test> {
    failFast = true
    useJUnitPlatform()
}

val dokkaJavadoc by tasks.existing(DokkaTask::class)

val javadocJar by tasks.registering(Jar::class) {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Assembles a jar archive containing the Javadoc API documentation."
    archiveClassifier.set("javadoc")
    from(dokkaJavadoc)
}

val sourcesJar by tasks.registering(Jar::class) {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Assembles a jar archive containing the main classes with sources."
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven {
            if (project.version.toString().endsWith("SNAPSHOT")) {
                setUrl("https://oss.sonatype.org/content/repositories/snapshots")
            } else {
                setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            }

            credentials {
                username = ossrhUsername
                password = ossrhPassword
            }
        }
    }

    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(javadocJar.get())
            artifact(sourcesJar.get())

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
                }

                scm {
                    connection.set("scm:git:https://github.com/michaelbull/kotlin-retry")
                    developerConnection.set("scm:git:git@github.com:michaelbull/kotlin-retry.git")
                    url.set("https://github.com/michaelbull/kotlin-retry")
                }

                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/michaelbull/kotlin-retry/issues")
                }

                ciManagement {
                    system.set("GitHub")
                    url.set("https://github.com/michaelbull/kotlin-retry/actions?query=workflow%3Aci")
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    sign(publishing.publications)
}
