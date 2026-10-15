import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.util.Properties

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        apiVersion = KotlinVersion.KOTLIN_2_2
    }
}

val localProperties = Properties().apply {
    providers.fileContents(layout.projectDirectory.file("local.properties")).asText.orNull?.let { load(it.reader()) }
}
val platformLocalPath: String? =
    providers.gradleProperty("platformLocalPath").orNull ?: localProperties.getProperty("platformLocalPath")

dependencies {
    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        if (platformLocalPath != null) {
            local(platformLocalPath)
        } else {
            intellijIdea(providers.gradleProperty("platformVersion").get())
        }
    }
}

intellijPlatform {
    instrumentCode = false

    pluginConfiguration {
        version = providers.gradleProperty("version")

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = provider { null }
        }
    }
}
