package com.servalabs.chat.components.settings.app.appearance

import com.servalabs.chat.keyvalue.SettingsValues

data class AppearanceSettingsState(
  val theme: SettingsValues.Theme,
  val dynamicColors: Boolean,
  val messageFontSize: Int,
  val language: String,
  val isCompactNavigationBar: Boolean
)
