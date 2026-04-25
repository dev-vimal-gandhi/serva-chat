package com.servalabs.chat.conversation.v2

import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.servalabs.chat.core.util.logging.Log
import com.servalabs.chat.R
import com.servalabs.chat.components.TooltipPopup
import com.servalabs.chat.stickers.StickerPackInstallEvent
import com.servalabs.chat.util.TextSecurePreferences

/**
 * Any and all tooltips that the conversation can display, and a light amount of related presentation logic.
 */
class ConversationTooltips(fragment: Fragment) {
  companion object {
    private val TAG = Log.tag(ConversationTooltips::class.java)
  }

  private val viewModel: TooltipViewModel by fragment.viewModels()

  /**
   *  Displayed to teach the user about sticker packs
   */
  /**
   * Displayed after a sticker pack is installed
   */
  fun displayStickerPackInstalledTooltip(anchor: View, event: StickerPackInstallEvent) {
    TooltipPopup.forTarget(anchor)
      .setText(R.string.ConversationActivity_sticker_pack_installed)
      .setIconGlideModel(event.iconGlideModel)
      .show(TooltipPopup.POSITION_ABOVE)
  }

  /**
   * ViewModel which holds different bits of session-local persistent state for different tooltips.
   */
  class TooltipViewModel : ViewModel() {
    var hasDisplayedCallingTooltip: Boolean = false
  }
}
