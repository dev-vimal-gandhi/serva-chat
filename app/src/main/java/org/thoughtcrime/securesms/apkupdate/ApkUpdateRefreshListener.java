/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.apkupdate;


import android.content.Context;

import org.signal.core.util.logging.Log;
import com.servalabs.chat.BuildConfig;
import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.jobs.ApkUpdateJob;
import com.servalabs.chat.service.PersistentAlarmManagerListener;
import com.servalabs.chat.util.TextSecurePreferences;

import java.util.concurrent.TimeUnit;

public class ApkUpdateRefreshListener extends PersistentAlarmManagerListener {

  private static final String TAG = Log.tag(ApkUpdateRefreshListener.class);

  private static final long INTERVAL = TimeUnit.HOURS.toMillis(22);

  @Override
  protected long getNextScheduledExecutionTime(Context context) {
    return TextSecurePreferences.getUpdateApkRefreshTime(context);
  }

  @Override
  protected long onAlarm(Context context, long scheduledTime) {
    Log.i(TAG, "onAlarm...");

    if (scheduledTime != 0 && BuildConfig.MANAGE_MOLLY_UPDATES) {
      Log.i(TAG, "Queueing APK update job...");
      AppDependencies.getJobManager().add(new ApkUpdateJob());
    }

    long newTime = System.currentTimeMillis() + INTERVAL;
    TextSecurePreferences.setUpdateApkRefreshTime(context, newTime);

    return newTime;
  }

  public static void scheduleIfAllowed(Context context) {
    if (BuildConfig.MANAGE_MOLLY_UPDATES) {
      new ApkUpdateRefreshListener().onReceive(context, getScheduleIntent());
    }
  }

}
