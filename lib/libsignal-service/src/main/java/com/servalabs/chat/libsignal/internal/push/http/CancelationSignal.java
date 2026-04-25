package com.servalabs.chat.libsignal.internal.push.http;

/**
 * Used to communicate to observers whether or not something is canceled.
 */
public interface CancelationSignal {
  boolean isCanceled();
}
