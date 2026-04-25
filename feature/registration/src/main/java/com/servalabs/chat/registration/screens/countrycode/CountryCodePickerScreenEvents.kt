/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.screens.countrycode

import com.servalabs.chat.registration.util.DebugLoggableModel

sealed class CountryCodePickerScreenEvents : DebugLoggableModel() {
  data class Search(val query: String) : CountryCodePickerScreenEvents()
  data class CountrySelected(val country: Country) : CountryCodePickerScreenEvents()
  data object Dismissed : CountryCodePickerScreenEvents()
}
