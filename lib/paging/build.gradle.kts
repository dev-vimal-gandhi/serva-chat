plugins {
  id("signal-library")
}

android {
  namespace = "com.servalabs.chat.paging"
}

dependencies {
  implementation(project(":core:util"))
  implementation(libs.kotlinx.coroutines.core)
}
