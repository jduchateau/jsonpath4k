import com.strumenta.antlrkotlin.gradle.AntlrKotlinTask
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFrameworkConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.kotlinx.serialization)
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.antlr.kotlin.plugin)
    alias(libs.plugins.jetbrains.dokka)
    id("maven-publish")
    id("signing")
}

/* required for maven publication */
val artifactVersion: String by extra
group = "at.asitplus"
version = artifactVersion

repositories {
    google()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots") //KOTEST snapshot
    mavenCentral()
}


val SRCDIR_ANTRL = "generated/antlr"
val generateKotlinGrammarSource = tasks.register<AntlrKotlinTask>("generateKotlinGrammarSource") {
    dependsOn("cleanGenerateKotlinGrammarSource")

    // compiling any *.g4 files within the project
    source = fileTree(layout.projectDirectory.dir("src")) {
        include("**/*.g4")
    }

    // We want the generated source files to have this package name
    packageName = "at.asitplus.jsonpath.generated"

    // We want visitors alongside listeners.
    // The Kotlin target language is implicit, as is the file encoding (UTF-8)
    arguments = listOf("-visitor")

    // Generated files are output inside src/gen/kotlin/{package-name}
    val outDir = "$SRCDIR_ANTRL/${packageName!!.replace(".", "/")}"
    outputDirectory = layout.buildDirectory.dir(outDir).get().asFile
}


kotlin {
    jvmToolchain(17)

    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
                freeCompilerArgs = listOf(
                    "-Xjsr305=strict"
                )
            }
        }
    }

    androidTarget {
        compilerOptions {
            publishLibraryVariants("release")
            jvmTarget = JvmTarget.JVM_1_8
        }
    }


    iosArm64()
    iosSimulatorArm64()
    iosX64()


    sourceSets {
        commonMain {
            kotlin.srcDir(layout.buildDirectory.dir(SRCDIR_ANTRL))
            dependencies {
                implementation(libs.antlr.kotlin)
                implementation(libs.jetbrains.kotlinx.serialization)
                implementation(libs.napier)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotest.common)
                implementation(libs.kotest.property)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.framework.datatest)
                implementation(libs.jetbrains.kotlinx.serialization)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest.runner.junit5)
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>> {
    dependsOn(generateKotlinGrammarSource)
}
tasks.withType<KotlinCompile> {
    dependsOn(generateKotlinGrammarSource)
}

exportIosFramework("JsonPath4K")

val javadocJar = setupDokka(
    baseUrl = "https://github.com/a-sit-plus/jsonpath4k/tree/main/",
    multiModuleDoc = false
)


android {
    namespace = "at.asitplus.jsonpath4k"
    compileSdk = 34
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    defaultConfig {
        minSdk = 30
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            if (this.name != "relocation") artifact(javadocJar)
            pom {
                name.set("JsonPath4K")
                description.set("Kotlin Multiplatform library for using Json Paths as specified in [RFC9535](https://datatracker.ietf.org/doc/rfc9535/)")
                url.set("https://github.com/a-sit-plus/jsonpath4k")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("acrusage")
                        name.set("Stefan Kreiner")
                        email.set("stefan.kreiner@iaik.tugraz.at")
                    }
                    developer {
                        id.set("nodh")
                        name.set("Christian Kollmann")
                        email.set("christian.kollmann@a-sit.at")
                    }
                    developer {
                        id.set("JesusMcCloud")
                        name.set("Bernd Prünster")
                        email.set("bernd.pruenster@a-sit.at")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:a-sit-plus/jsonpath4k.git")
                    developerConnection.set("scm:git:git@github.com:a-sit-plus/jsonpath4k.git")
                    url.set("https://github.com/a-sit-plus/jsonpath4k")
                }
            }
        }
    }

    repositories {
        mavenLocal {
            signing.isRequired = false
        }
        maven {
            url = uri(layout.projectDirectory.dir("..").dir("repo"))
            name = "local"
            signing.isRequired = false
        }
    }
}

signing {
    val signingKeyId: String? by project
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    sign(publishing.publications)
}


/**
 * taken from vclib conventions plugin at https://github.com/a-sit-plus/gradle-conventions-plugin
 */
fun Project.exportIosFramework(
    name: String,
    vararg additionalExports: Any
) = exportIosFramework(name, bitcodeEmbeddingMode = BitcodeEmbeddingMode.BITCODE, additionalExports = additionalExports)

fun Project.exportIosFramework(
    name: String,
    bitcodeEmbeddingMode: BitcodeEmbeddingMode,
    vararg additionalExports: Any
) {
    val iosTargets = kotlinExtension.let {
        if (it is KotlinMultiplatformExtension) {
            it.targets.filterIsInstance<KotlinNativeTarget>().filter { it.name.startsWith("ios") }
        } else throw StopExecutionException("No iOS Targets found! Declare them explicitly before calling exportIosFramework!")
    }

    extensions.getByType<KotlinMultiplatformExtension>().apply {
        XCFrameworkConfig(project, name).also { xcf ->
            logger.lifecycle("  \u001B[1mXCFrameworks will be exported for the following iOS targets: ${iosTargets.joinToString { it.name }}\u001B[0m")
            iosTargets.forEach {
                it.binaries.framework {
                    baseName = name
                    embedBitcode(bitcodeEmbeddingMode)
                    additionalExports.forEach { export(it) }
                    xcf.add(this)
                }
            }
        }
    }
}

fun Project.setupDokka(
    outputDir: String = rootProject.layout.buildDirectory.dir("dokka").get().asFile.canonicalPath,
    baseUrl: String,
    multiModuleDoc: Boolean = false,
    remoteLineSuffix: String = "#L"
): TaskProvider<Jar> {
    val dokkaHtml = (tasks["dokkaHtml"] as DokkaTask).apply { outputDirectory.set(file(outputDir)) }

    val deleteDokkaOutput = tasks.register<Delete>("deleteDokkaOutputDirectory") {
        delete(outputDir)
    }
    val sourceLinktToConfigure = if (multiModuleDoc) (tasks["dokkaHtmlPartial"] as DokkaTaskPartial) else dokkaHtml
    sourceLinktToConfigure.dokkaSourceSets.configureEach {
        sourceLink {
            localDirectory.set(file("src/$name/kotlin"))
            remoteUrl.set(uri("$baseUrl/${project.name}/src/$name/kotlin").toURL())
            this@sourceLink.remoteLineSuffix.set(remoteLineSuffix)
        }
    }

    return tasks.register<Jar>("javadocJar") {
        dependsOn(deleteDokkaOutput, dokkaHtml)
        archiveClassifier.set("javadoc")
        from(outputDir)
    }
}


/**
 * Makes all publishing tasks depend on all signing tasks. Hampers parallelization but works around dodgy task dependencies
 * that (more often than anticipated) make the build process stumble over its own feet.
 */
internal fun setupSignDependency() {
    logger.lifecycle("")
    logger.lifecycle("  Making signing tasks run after publish tasks")
    logger.info("")
    tasks.whenTaskAdded {
        if (this is Sign) {
            val publishTasks = tasks.withType<AbstractPublishToMaven>()
            publishTasks.forEach {
                it.dependsOn(this)
                it.mustRunAfter(this)
            }
        }

        if (this is AbstractPublishToMaven) {
            val signTasks = tasks.withType<Sign>()
            signTasks.forEach {
                this.dependsOn(it)
                this.mustRunAfter(it)
            }
        }

        if (name=="androidReleaseSourcesJar") {
            tasks.findByName("generateKotlinGrammarSource")?.let {
                dependsOn(it)
                mustRunAfter(it)
            }
        }
        if (name == "generateKotlinGrammarSource") {
            tasks.filter { it.name== "androidReleaseSourcesJar" }.forEach {
                it.dependsOn(this)
                it.mustRunAfter(this)
            }
        }
    }

    tasks.withType<Sign>().configureEach {
        val sign = this
        tasks.withType<AbstractPublishToMaven>().forEach {
            it.dependsOn(sign)
            it.mustRunAfter(sign)
        }
    }

    tasks.findByName("androidReleaseSourcesJar")?.apply {
        tasks.findByName("generateKotlinGrammarSource")?.let {
            dependsOn(it)
            mustRunAfter(it)
        }
    }

    gradle.taskGraph.whenReady {
        logger.info("")
        logger.info("  Task Graph for project $name is ready. The following publish tasks are present:")
        tasks.withType<AbstractPublishToMaven>().forEach {
            logger.info("    * ${it.name}")
        }
        logger.info("\n  The following signing tasks are present:")
        tasks.withType<Sign>().forEach {
            logger.info("    * ${it.name}")
        }
        tasks.withType<AbstractPublishToMaven>().forEach { publishTask ->
            val signingTasks = publishTask.dependsOn.filterIsInstance<Sign>()
            logger.info("   * ${publishTask.name} must now run after")
            signingTasks.forEach {
                logger.info("      * ${it.name}")
            }
        }
        logger.info("")
    }
}


afterEvaluate {
    tasks.withType<Test> {
        useJUnitPlatform()
    }
    setupSignDependency()
}