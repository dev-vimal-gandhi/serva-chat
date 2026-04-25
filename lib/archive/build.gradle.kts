plugins {
  id("signal-library")
  id("com.squareup.wire")
}

android {
  namespace = "com.servalabs.chat.archive"
}

wire {
  kotlin {
    javaInterop = true
  }

  sourcePath {
    srcDir("src/main/protowire")
  }
}

dependencies {
  implementation(project(":core:util"))
  implementation(project(":core:models-jvm"))
  implementation(project(":lib:libsignal-service"))

  implementation(libs.libsignal.android)
  implementation(libs.google.guava.android)
}
