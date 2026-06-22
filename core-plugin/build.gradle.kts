plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1" // Shadow plugin
}

repositories {
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.essentialsx.net/releases/") }
}


val pluginName: String by project
val repoUrl: String by project
val developerId: String by project
val developerName: String by project
val apiVersion: String by project
val authors: String by project
val pluginVersion: String = project.version.toString()

tasks.processResources {
    val props = mapOf(
        "pluginName" to pluginName,
        "version" to pluginVersion,
        "apiVersion" to apiVersion,
        "authors" to authors
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}


dependencies {
    implementation(project(":core-api"))
    compileOnly("io.papermc.paper:paper-api:1.21.6-R0.1-SNAPSHOT")
    compileOnly("org.yuemi:YueMiLibs-api:1.1.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    compileOnly("net.essentialsx:EssentialsX:2.21.0") {
        exclude(group = "org.bukkit", module = "bukkit")
        exclude(group = "org.spigotmc", module = "spigot-api")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.jar {
    archiveBaseName.set(pluginName)
    archiveVersion.set(pluginVersion)
    archiveClassifier.set("unshaded")

    manifest {
        attributes(
            "Implementation-Title" to pluginName,
            "Implementation-Version" to pluginVersion,
            "Implementation-Vendor" to developerName,
            "License" to "MIT"
        )
    }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set(pluginName)
    archiveVersion.set(pluginVersion)
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.named("shadowJar"))
}
