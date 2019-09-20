import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val SourceSet.kotlin: SourceDirectorySet
    get() = withConvention(KotlinSourceSet::class) { kotlin }

fun BintrayExtension.pkg(configure: BintrayExtension.PackageConfig.() -> Unit) {
    pkg(delegateClosureOf(configure))
}

plugins {
    `maven-publish`
    kotlin("jvm") version ("1.3.50")
    id("com.github.ben-manes.versions") version ("0.25.0")
    id("com.jfrog.bintray") version ("1.8.4")
    id("org.jetbrains.dokka") version ("0.9.18")
    id("net.researchgate.release") version ("2.8.1")
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/michaelbull/maven")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.1")
    testImplementation(enforcedPlatform("org.junit:junit-bom:5.5.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.1")
    testImplementation("io.mockk:mockk:1.9.3")
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        listOf("alpha", "beta", "rc", "cr", "m", "eap", "pr").any {
            candidate.version.contains(it, ignoreCase = true)
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xuse-experimental=kotlin.contracts.ExperimentalContracts")
    }
}

tasks.withType<Test> {
    failFast = true
    useJUnitPlatform()
}

val dokka by tasks.existing(DokkaTask::class) {
    sourceDirs = project.the<SourceSetContainer>().getByName("main").kotlin.srcDirs
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/docs/javadoc"
    kotlinTasks(::defaultKotlinTasks)
}

val javadocJar by tasks.registering(Jar::class) {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Assembles a jar archive containing the Javadoc API documentation."
    archiveClassifier.set("javadoc")
    dependsOn(dokka)
    from(dokka.get().outputDirectory)
}

val sourcesJar by tasks.registering(Jar::class) {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Assembles a jar archive containing the main classes with sources."
    archiveClassifier.set("sources")
    from(project.the<SourceSetContainer>().getByName("main").allSource)
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(javadocJar.get())
            artifact(sourcesJar.get())
        }
    }
}

val bintrayUser: String? by project
val bintrayKey: String? by project

bintray {
    user = bintrayUser
    key = bintrayKey
    setPublications("mavenJava")

    pkg {
        repo = "maven"
        name = project.name
        desc = project.description
        websiteUrl = "https://github.com/michaelbull/kotlin-retry"
        issueTrackerUrl = "https://github.com/michaelbull/kotlin-retry/issues"
        vcsUrl = "git@github.com:michaelbull/kotlin-retry.git"
        githubRepo = "michaelbull/kotlin-retry"
        setLicenses("ISC")
    }
}

val bintrayUpload by tasks.existing(BintrayUploadTask::class) {
    dependsOn("build")
    dependsOn("generatePomFileForMavenJavaPublication")
    dependsOn(sourcesJar)
    dependsOn(javadocJar)
}

tasks.named("afterReleaseBuild") {
    dependsOn(bintrayUpload)
}
