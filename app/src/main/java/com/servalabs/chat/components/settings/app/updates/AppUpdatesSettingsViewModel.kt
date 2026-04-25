/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.components.settings.app.updates

import android.app.Application
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.servalabs.chat.apkupdate.ApkUpdateNotifications
import com.servalabs.chat.apkupdate.ApkUpdateRefreshListener
import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.jobs.ApkUpdateJob
import com.servalabs.chat.keyvalue.SignalStore
import com.servalabs.chat.util.TextSecurePreferences

class AppUpdatesSettingsViewModel : ViewModel() {

  private val application: Application = AppDependencies.application

  private val internalState = MutableStateFlow(getState())

  val state: StateFlow<AppUpdatesSettingsState> = internalState

  fun setUpdateApkEnabled(enabled: Boolean) {
    TextSecurePreferences.setUpdateApkEnabled(application, enabled)
    if (enabled) {
      checkForUpdates()
    }
    refresh()
  }

  fun setIncludeBetaEnabled(enabled: Boolean) {
    TextSecurePreferences.setUpdateApkIncludeBetaEnabled(application, enabled)
    ApkUpdateNotifications.dismissInstallPrompt(application)
    checkForUpdates()
    refresh()
  }

  fun checkForUpdates() {
    ApkUpdateRefreshListener.scheduleIfAllowed(application)
    AppDependencies.jobManager.add(ApkUpdateJob())
  }

  fun refresh() {
    internalState.update { getState() }
  }

  private fun getState(): AppUpdatesSettingsState {
    return AppUpdatesSettingsState(
      lastCheckedTime = SignalStore.apkUpdate.lastSuccessfulCheck,
      includeBetaEnabled = TextSecurePreferences.isUpdateApkIncludeBetaEnabled(application),
      autoUpdateEnabled = TextSecurePreferences.isUpdateApkEnabled(application),
    )
  }
}
