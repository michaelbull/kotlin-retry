description = "Extensions for integrating with arrow-kt Either type."

plugins {
    id("kotlin-conventions")
    id("publish-conventions")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":kotlin-retry"))
                api(libs.arrow.core)
                implementation(libs.kotlin.coroutines.core)
            }
        }

        commonTest {
            dependencies {
                implementation(project(":kotlin-retry"))
                implementation(libs.kotlin.coroutines.test)
            }
        }
    }
}
