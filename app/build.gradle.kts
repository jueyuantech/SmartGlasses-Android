import java.lang.Runtime
import java.lang.Process
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
}

android {
    namespace = "com.jueyuantech.glasses"
    compileSdk = 34

    val localProp = Properties().apply {
        load(FileInputStream(File(rootProject.rootDir, "local.properties")))
    }

    defaultConfig {
        applicationId = "com.jueyuantech.glasses"
        minSdk = 24
        targetSdk = 33
        versionCode = 9
        versionName = "1.2.2" + "_" + getGitCommitVersion()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                argument("rxhttp_package", "rxhttp")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            manifestPlaceholders["amap_key"] = localProp.getProperty("LOCAL_PROP_AMAP_KEY")
            buildConfigField("String", "PROP_SPARK40_APP_ID", "\"${localProp.getProperty("LOCAL_PROP_SPARK40_APP_ID")}\"")
            buildConfigField("String", "PROP_SPARK40_API_SECRET", "\"${localProp.getProperty("LOCAL_PROP_SPARK40_API_SECRET")}\"")
            buildConfigField("String", "PROP_SPARK40_API_KEY", "\"${localProp.getProperty("LOCAL_PROP_SPARK40_API_KEY")}\"")
            buildConfigField("String", "PROP_ICP_NUMBER", "\"${localProp.getProperty("LOCAL_PROP_ICP_NUMBER")}\"")
        }
        debug {

            manifestPlaceholders["amap_key"] = localProp.getProperty("LOCAL_PROP_AMAP_KEY")
            buildConfigField("String", "PROP_SPARK40_APP_ID", "\"${localProp.getProperty("LOCAL_PROP_SPARK40_APP_ID")}\"")
            buildConfigField("String", "PROP_SPARK40_API_SECRET", "\"${localProp.getProperty("LOCAL_PROP_SPARK40_API_SECRET")}\"")
            buildConfigField("String", "PROP_SPARK40_API_KEY", "\"${localProp.getProperty("LOCAL_PROP_SPARK40_API_KEY")}\"")
            buildConfigField("String", "PROP_ICP_NUMBER", "\"debug\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    android.applicationVariants.all {
        val buildType = this.buildType.name
        outputs.all {
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                this.outputFileName =
                    "SmartGlasses-${defaultConfig.versionName}-" + getGitBranchName() + "_" + getCurTime() + "_${buildType}" + ".apk"
            }
        }
    }

    sourceSets {
        named("main") {
            jniLibs.srcDirs("libs")
        }
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.navigation:navigation-fragment:2.7.0")
    implementation("androidx.navigation:navigation-ui:2.7.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.22"))

    implementation("com.youth.banner:banner:2.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.alibaba:fastjson:1.2.83")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.github.bumptech.glide:glide:4.11.0")
    implementation("com.tencent:mmkv:1.3.1")
    implementation("me.jessyan:autosize:1.2.1")

    implementation(files("libs/AMap3DMap_10.0.700_AMapNavi_10.0.700_AMapSearch_9.7.2_AMapLocation_6.4.5_20240508.jar"))

    implementation("com.hankcs:hanlp:portable-1.7.5")

    implementation("com.airbnb.android:lottie:4.2.2")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    implementation("com.microsoft.cognitiveservices.speech:client-sdk:1.40.0")

    implementation("com.hjq:xxpermissions:8.2")
    implementation(files("libs/SparkChain.aar"))
    implementation(files("libs/Msc.jar"))

    implementation ("com.googlecode.libphonenumber:libphonenumber:8.13.5")

    implementation(files("libs/VenusSDK_v1.8.1_1ae897f_release.aar"))
}

fun getGitCommitCount(): Int {
    val cmd = "git rev-list --count HEAD".split(" ").toTypedArray()
    val process: Process = Runtime.getRuntime().exec(cmd)
    process.waitFor()
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val count = reader.readLine()!!.toInt()
    return count
}

fun getGitCommitVersion(): String {
    val cmd = "git describe --always --dirty".split(" ").toTypedArray()
    val process: Process = Runtime.getRuntime().exec(cmd)
    process.waitFor()
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val version = reader.readLine()!!.toString()
    return version
}

fun getGitBranchName(): String {
    val cmd = "git rev-parse --abbrev-ref HEAD".split(" ").toTypedArray()
    val process: Process = Runtime.getRuntime().exec(cmd)
    process.waitFor()
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val branchName = reader.readLine()!!.toString()
    return branchName
}

fun getCurTime(): String {
    val timestamp = SimpleDateFormat("yyyyMMddhhmm").format(Date())
    return timestamp
}