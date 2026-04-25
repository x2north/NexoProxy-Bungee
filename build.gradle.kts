import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers

plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    id("com.gradleup.shadow") version "9.3.1"
    id("eclipse")
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.8"
}

val copyJarPath = project.findProperty("proxy_bungee_plugin_path")?.toString()
group = "com.nexomc"
version = "1.1"

repositories {
    mavenCentral()
    maven("https://repo.opencollab.dev/maven-snapshots")
    maven("https://repo.nexomc.com/releases/")
    maven("https://repo.nexomc.com/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://libraries.minecraft.net")
    mavenLocal()
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.21-R0.4")
    implementation("com.mojang:brigadier:1.2.9")

    implementation("com.charleskorn.kaml:kaml:0.67.0")
    implementation("org.bstats:bstats-bungeecord:3.1.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("team.unnamed:creative-api:1.13.0")
    implementation("team.unnamed:creative-serializer-minecraft:1.13.0")
    implementation("net.kyori:adventure-api:4.24.0")
}

tasks {
    shadowJar {
        relocate("org.bstats", "com.nexomc.nexoproxy.bstats")
        relocate("team.unnamed", "com.nexomc.nexoproxy.unnamed")
        relocate("net.kyori", "com.nexomc.nexoproxy.kyori")
        relocate("com.mojang.brigadier", "com.nexomc.nexoproxy.libs.brigadier")
        if (copyJarPath != null) {
            destinationDirectory.set(File(copyJarPath))
        }
    }

    build.get().dependsOn(shadowJar)
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

val templateSource = file("src/main/templates")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")
val generateTemplates = tasks.register<Copy>("generateTemplates") {
    val props = mapOf("version" to project.version)
    inputs.properties(props)

    from(templateSource)
    into(templateDest)
    expand(props)
}

sourceSets.main.configure { java.srcDir(generateTemplates.map { it.outputs }) }

project.idea.project.settings.taskTriggers.afterSync(generateTemplates)
project.eclipse.synchronizationTasks(generateTemplates)
