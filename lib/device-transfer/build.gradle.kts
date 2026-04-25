plugins {
  id("signal-library")
}

android {
  namespace = "com.servalabs.chat.devicetransfer"
}

dependencies {
  implementation(project(":core:util"))
  implementation(libs.libsignal.android)
  api(libs.greenrobot.eventbus)

  testImplementation(testLibs.robolectric.robolectric) {
    exclude(group = "com.google.protobuf", module = "protobuf-java")
  }
  testImplementation(testFixtures(project(":lib:libsignal-service")))
}
