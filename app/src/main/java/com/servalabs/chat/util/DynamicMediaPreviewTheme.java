package com.servalabs.chat.util;

import androidx.annotation.StyleRes;

import com.servalabs.chat.R;

public class DynamicMediaPreviewTheme extends DynamicTheme {

  protected @StyleRes int getRegularTheme() {
    return R.style.TextSecure_MediaPreview;
  }

  protected @StyleRes int getDynamicTheme() {
    return R.style.Theme_Molly_Dynamic_MediaPreview;
  }
}
