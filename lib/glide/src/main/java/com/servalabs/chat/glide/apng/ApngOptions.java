package com.servalabs.chat.glide.apng;

import com.bumptech.glide.load.Option;

import com.servalabs.chat.core.util.Conversions;

/**
 * Holds options that can be used to alter how APNGs are decoded in Glide.
 */
public final class ApngOptions {

  private static final String KEY = "com.servalabs.chat.skip_apng";

  public static Option<Boolean> ANIMATE = Option.disk(KEY, true, (keyBytes, value, messageDigest) -> {
    messageDigest.update(keyBytes);
    messageDigest.update(Conversions.intToByteArray(value ? 1 : 0));
  });

  private ApngOptions() {}
}
