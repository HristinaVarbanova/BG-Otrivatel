plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.loginscreen"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.loginscreen"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

dependencies {
    // Основни зависимости
    implementation(libs.appcompat) // За съвместимост с по-стари версии на Android
    implementation(libs.material) // За Material Design компоненти
    implementation(libs.activity) // За Activity компоненти
    implementation(libs.constraintlayout) // За ConstraintLayout
    implementation(libs.firebase.database) // За Firebase Realtime Database
    implementation("androidx.cardview:cardview:1.0.0") // За CardView компоненти
    implementation("com.google.firebase:firebase-auth:23.2.0") // За Firebase Authentication
    implementation("com.google.firebase:firebase-database:21.0.0") // За Firebase Database
    implementation("com.google.firebase:firebase-firestore:25.1.3")
    implementation("com.google.mlkit:image-labeling:17.0.9")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("androidx.work:work-runtime:2.10.0")
    implementation("com.google.guava:guava:32.1.3-android")




    // Тестови зависимости
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

