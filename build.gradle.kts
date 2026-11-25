import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.publish.PublishingExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.npm.NpmExtension

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.dokka)
}

// A working tree is never a release: the version in gradle.properties is the one that will be
// published next, so a local build keeps a `-SNAPSHOT` suffix and can never shadow it. The release
// workflow passes `-Pkmp.reflect.release=true`, which drops the suffix.
val versionSuffix =
    if (providers.gradleProperty("kmp.reflect.release").map(String::toBoolean).getOrElse(false)) "" else "-SNAPSHOT"
val projectVersion = providers.gradleProperty("kmp-reflect.version").get().trim()
extra["versionSuffix"] = versionSuffix
extra["kmpReflectVersion"] = projectVersion

// Every publishable module shares this prefix, which is also how publishing and Dokka find them.
// Keep it in sync with the module directories and settings.gradle.kts.
val publishedPrefix = "kmp-reflect-"

// Dokka aggregates every published module into one API reference: the core metadata model, the proxy
// API, and the KSP processor that produces the code.
dependencies {
    subprojects
        .filter { it.name.startsWith(publishedPrefix) }
        .forEach { dokka(project(it.path)) }
}

dokka {
    dokkaPublications.html {
        moduleName.set("kmp-reflect")
        outputDirectory.set(layout.buildDirectory.dir("dokka/html"))
    }
}

// GitHub-backed namespace; verify it once in the Central Portal. All modules share it.
// Setting the root `group` alone does not propagate to subprojects.
val publishedGroup = "io.github.shijianjs"
val repositoryUrl = "https://github.com/shijianjs/kmp-reflect"

// Keep a single webpack instance across the npm workspace: npm otherwise installs a second copy
// for source-map-loader's peer dependency, and the test bundle fails with
// "The 'compilation' argument must be an instance of Compilation".
val webpackVersion = libs.versions.webpack.get()

plugins.withType<NodeJsRootPlugin> {
    extensions.getByType(NpmExtension::class.java).override("webpack", webpackVersion)
}

// The demo page builds DOM with kotlin-browser's `web.*` types, and detekt.yml forbids the two
// overlapping-but-unrelated DOM APIs (`org.w3c.dom.*`, `kotlinx.browser.*`) so that a public API
// cannot mix them. Applying detekt to every subproject keeps that rule in one place.
subprojects {
    apply(plugin = "dev.detekt")

    extensions.configure<DetektExtension> {
        config.setFrom(rootProject.files("detekt.yml"))
        source.setFrom(files("src"))
    }

    if (name.startsWith(publishedPrefix)) {
        apply(plugin = "org.jetbrains.dokka")
    }
}

// Publishing is configured once for every module so that coordinates, POM metadata, signing and
// the Maven Central target cannot drift apart.
//
// klibs.io discovers libraries automatically: it reads the POM (url / scm.url must point at the
// GitHub repository) and requires the kotlin-tooling-metadata.json produced by the Kotlin
// Multiplatform plugin. Keep both intact when changing this block.
subprojects {
    if (!name.startsWith(publishedPrefix)) return@subprojects

    val module = this
    group = publishedGroup

    apply(plugin = "com.vanniktech.maven.publish")

    // The subproject's Kotlin plugin is applied after this root script runs. Choosing the javadoc
    // jar earlier would hit the "no compatible plugin" branch and the plugin's afterEvaluate default
    // (Dokka HTML) would win. Both plugins are handled: `kmp-reflect-ksp` is a plain JVM module.
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        extensions.configure<MavenPublishBaseExtension> {
            configureBasedOnAppliedPlugins(javadocJar = JavadocJar.Empty())
        }
    }
    plugins.withId("org.jetbrains.kotlin.jvm") {
        extensions.configure<MavenPublishBaseExtension> {
            configureBasedOnAppliedPlugins(javadocJar = JavadocJar.Empty())
        }
    }

    extensions.configure<MavenPublishBaseExtension> {
        publishToMavenCentral(automaticRelease = true)
        signAllPublications()

        // Gradle writes .md5/.sha1 next to every .asc signature. Maven Central does not need
        // them and they count toward the monthly file quota.
        excludeSignatureChecksums()

        pom {
            name.set(module.name)
            description.set(
                module.provider { module.description ?: "Compile-time reflection for Kotlin Multiplatform." },
            )
            url.set(repositoryUrl)
            inceptionYear.set("2026")

            licenses {
                license {
                    name.set("MIT License")
                    url.set("$repositoryUrl/blob/main/LICENSE")
                }
            }

            developers {
                developer {
                    id.set("shijianjs")
                    name.set("shijianjs")
                    url.set("https://github.com/shijianjs")
                }
            }

            scm {
                url.set(repositoryUrl)
                connection.set("scm:git:git://github.com/shijianjs/kmp-reflect.git")
                developerConnection.set("scm:git:ssh://git@github.com/shijianjs/kmp-reflect.git")
            }
        }
    }

    // A plain file repository produces the Maven repository layout — POMs, artifacts, sources,
    // javadoc and the `.asc` signatures — without contacting Central. The release workflow packages
    // one archive per coordinate from it and registers them with the hub.
    // Task name: publishAllPublicationsToHubStagingRepository
    extensions.configure<PublishingExtension> {
        repositories {
            maven {
                name = "hubStaging"
                url = module.layout.buildDirectory.dir("hub-staging").get().asFile.toURI()
            }
        }
    }
}

// Prints "<module>=<version>" for every publishable module, so the release workflow never duplicates
// the version rules in shell.
tasks.register("printPublishedVersions") {
    val publishedModules = subprojects.filter { it.name.startsWith(publishedPrefix) }
    doLast {
        publishedModules
            .associate { it.name to it.version.toString() }
            .toSortedMap()
            .forEach { (name, version) -> println("$name=$version") }
    }
}

// Assembles the GitHub Pages site: the documentation at the root, the demo under /demo/ and the API
// reference under /api/. The docs site's `baseUrl` and the two links it builds from it (in Config.kt)
// have to mirror this layout.
tasks.register<Sync>("pagesSite") {
    dependsOn(":demo:jsBrowserDistribution", ":docs:docusaurusBuild", "dokkaGeneratePublicationHtml")
    into(layout.buildDirectory.dir("pages"))
    from(layout.buildDirectory.dir("js/docusaurus/docs/build"))
    from(project(":demo").layout.buildDirectory.dir("dist/js/productionExecutable")) {
        into("demo")
    }
    from(layout.buildDirectory.dir("dokka/html")) {
        into("api")
    }
}
