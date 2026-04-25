package com.servalabs.chat.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.servalabs.chat.R;

public final class PlayStoreUtil {

  private PlayStoreUtil() {}

  public static void openPlayStoreOrOurApkDownloadPage(@NonNull Context context) {
    CommunicationActions.openBrowserLink(context, context.getString(R.string.install_url));
  }

  public static void openPlayStoreHome(@NonNull Context context) {
    try {
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.android.vending"));
      intent.setPackage("com.android.vending");
      context.startActivity(intent);
    } catch (android.content.ActivityNotFoundException e) {
      CommunicationActions.openBrowserLink(context, "https://play.google.com/store/apps/");
    }
  }
}
