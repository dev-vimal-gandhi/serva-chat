package com.servalabs.chat.util;

import androidx.annotation.StyleRes;

import com.servalabs.chat.R;

public class DynamicNoActionBarTheme extends DynamicTheme {

  protected @StyleRes int getRegularTheme() {
    return R.style.Theme_Signal_DayNight_NoActionBar;
  }

  protected @StyleRes int getDynamicTheme() {
    return R.style.Theme_Molly_Dynamic_NoActionBar;
  }
}
