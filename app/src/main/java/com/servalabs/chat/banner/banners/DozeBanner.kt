/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.banner.banners

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import com.servalabs.chat.core.ui.compose.DayNightPreviews
import com.servalabs.chat.core.ui.compose.Previews
import com.servalabs.chat.R
import com.servalabs.chat.banner.Banner
import com.servalabs.chat.banner.ui.compose.Action
import com.servalabs.chat.banner.ui.compose.DefaultBanner
import com.servalabs.chat.banner.ui.compose.Importance
import com.servalabs.chat.keyvalue.SignalStore
import com.servalabs.chat.util.PowerManagerCompat
import com.servalabs.chat.util.ServiceUtil
import com.servalabs.chat.util.TextSecurePreferences

class DozeBanner(private val context: Context, private val onDismissListener: () -> Unit) : Banner<Unit>() {

  override val enabled: Boolean
    get() = !SignalStore.account.fcmEnabled && !TextSecurePreferences.hasPromptedOptimizeDoze(context) && !ServiceUtil.getPowerManager(context).isIgnoringBatteryOptimizations(context.packageName)

  override val dataFlow: Flow<Unit>
    get() = flowOf(Unit)

  @Composable
  override fun DisplayBanner(model: Unit, contentPadding: PaddingValues) {
    Banner(
      contentPadding = contentPadding,
      onDismissListener = {
        TextSecurePreferences.setPromptedOptimizeDoze(context, true)
        onDismissListener.invoke()
      },
      onOkListener = {
        TextSecurePreferences.setPromptedOptimizeDoze(context, true)
        PowerManagerCompat.requestIgnoreBatteryOptimizations(context)
      }
    )
  }
}

@Composable
private fun Banner(contentPadding: PaddingValues, onDismissListener: () -> Unit = {}, onOkListener: () -> Unit = {}) {
  DefaultBanner(
    title = null,
    body = stringResource(id = R.string.DozeReminder_allow_molly_to_deliver_timely_notifications),
    importance = Importance.ERROR,
    onDismissListener = onDismissListener,
    actions = listOf(
      Action(R.string.DozeReminder_disable_battery_restrictions) {
        onOkListener()
      }
    ),
    paddingValues = contentPadding
  )
}

@DayNightPreviews
@Composable
private fun BannerPreview() {
  Previews.Preview {
    Banner(contentPadding = PaddingValues(0.dp))
  }
}
