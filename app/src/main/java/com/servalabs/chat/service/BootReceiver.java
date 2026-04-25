package com.servalabs.chat.service;

import android.content.Context;
import android.content.Intent;

import com.servalabs.chat.core.util.logging.Log;
import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.jobs.MessageFetchJob;

public class BootReceiver extends ExportedBroadcastReceiver {

  private static final String TAG = Log.tag(BootReceiver.class);

  @Override
  public void onReceiveUnlock(Context context, Intent intent) {
    Log.i(TAG, "Restarting after: " + intent.getAction());
    AppDependencies.getJobManager().add(new MessageFetchJob());
  }
}
