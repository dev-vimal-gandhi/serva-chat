/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.dependencies

import org.signal.core.ui.CoreUiDependencies
import com.servalabs.chat.ScreenLockController
import com.servalabs.chat.keyvalue.SignalStore
import com.servalabs.chat.service.KeyCachingService
import com.servalabs.chat.util.BackupUtil
import com.servalabs.chat.util.TextSecurePreferences

object CoreUiDependenciesProvider : CoreUiDependencies.Provider {
  override fun provideBackupBaseDirName(): String {
    return BackupUtil.getBackupBaseDirName();
  }

  override fun provideIsIncognitoKeyboardEnabled(): Boolean {
    return TextSecurePreferences.isIncognitoKeyboardEnabled(AppDependencies.application)
  }

  override fun provideIsScreenSecurityEnabled(): Boolean {
    if (ScreenLockController.alwaysSetSecureFlagOnResume || KeyCachingService.isLocked()) {
      return true
    }
    return TextSecurePreferences.isScreenSecurityEnabled(AppDependencies.application)
  }

  override fun provideForceSplitPane(): Boolean {
    return SignalStore.internal.forceSplitPane
  }
}
