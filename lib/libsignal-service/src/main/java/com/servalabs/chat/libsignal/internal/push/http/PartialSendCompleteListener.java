package com.servalabs.chat.libsignal.internal.push.http;

import com.servalabs.chat.libsignal.api.messages.SendMessageResult;

/**
 * Used to let a listener know when each individual send in a collection of sends has been completed.
 */
public interface PartialSendCompleteListener {
  void onPartialSendComplete(SendMessageResult result);
}
