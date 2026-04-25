package com.servalabs.chat.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.servalabs.chat.jobs.EmojiSearchIndexDownloadJob;
import com.servalabs.chat.service.KeyCachingService;
import com.servalabs.chat.util.DateUtils;

public class LocaleChangedReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    DateUtils.updateFormat();
    if (!KeyCachingService.isLocked()) {
      NotificationChannels.getInstance().onLocaleChanged();
      EmojiSearchIndexDownloadJob.scheduleImmediately();
    }
  }
}
