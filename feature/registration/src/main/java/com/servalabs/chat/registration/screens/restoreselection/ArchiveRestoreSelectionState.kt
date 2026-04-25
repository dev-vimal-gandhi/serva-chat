/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.screens.restoreselection

import com.servalabs.chat.registration.util.DebugLoggableModel

data class ArchiveRestoreSelectionState(
  val restoreOptions: List<ArchiveRestoreOption> = emptyList(),
  val showSkipButton: Boolean = false,
  val showSkipRestoreWarning: Boolean = false
) : DebugLoggableModel()
