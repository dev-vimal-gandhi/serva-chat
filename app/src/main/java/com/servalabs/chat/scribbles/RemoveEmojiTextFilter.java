package com.servalabs.chat.scribbles;

import androidx.annotation.NonNull;

import com.servalabs.chat.imageeditor.core.HiddenEditText;
import com.servalabs.chat.components.emoji.EmojiUtil;

class RemoveEmojiTextFilter implements HiddenEditText.TextFilter {
  @Override
  public String filter(@NonNull String text) {
    return EmojiUtil.stripEmoji(text);
  }
}
