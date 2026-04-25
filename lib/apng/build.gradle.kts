plugins {
  id("signal-library")
}

android {
  namespace = "com.servalabs.chat.apng"
}

dependencies {
  implementation(project(":core:util"))
  testImplementation(testLibs.junit.junit)
  testImplementation(testLibs.robolectric.robolectric)
}
