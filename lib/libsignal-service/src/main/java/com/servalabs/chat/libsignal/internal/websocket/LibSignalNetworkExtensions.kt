/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
@file:JvmName("LibSignalNetworkExtensions")

package com.servalabs.chat.libsignal.internal.websocket

import org.signal.libsignal.net.Network
import com.servalabs.chat.libsignal.internal.configuration.SignalServiceConfiguration

private const val TAG = "LibSignalNetworkExtensions"

/**
 * Helper method to apply settings from the SignalServiceConfiguration.
 */
fun Network.applyConfiguration(config: SignalServiceConfiguration) {
  this.setCensorshipCircumventionEnabled(config.censored)
}
