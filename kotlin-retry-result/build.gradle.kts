description = "Extensions for integrating with kotlin-result."

plugins {
    id("kotlin-conventions")
    id("publish-conventions")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":kotlin-retry"))
                api(libs.kotlin.result)
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
