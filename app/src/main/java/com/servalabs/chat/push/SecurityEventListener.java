package com.servalabs.chat.push;

import android.content.Context;

import com.servalabs.chat.core.util.logging.Log;
import com.servalabs.chat.crypto.SecurityEvent;
import com.servalabs.chat.libsignal.api.SignalServiceMessageSender;
import com.servalabs.chat.libsignal.api.push.SignalServiceAddress;

public class SecurityEventListener implements SignalServiceMessageSender.EventListener {

  private static final String TAG = Log.tag(SecurityEventListener.class);

  private final Context context;

  public SecurityEventListener(Context context) {
    this.context = context.getApplicationContext();
  }

  @Override
  public void onSecurityEvent(SignalServiceAddress textSecureAddress) {
    SecurityEvent.broadcastSecurityUpdateEvent(context);
  }
}
