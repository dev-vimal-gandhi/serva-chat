package com.servalabs.chat.libsignal.api.messages.multidevice;

import com.servalabs.chat.core.models.ServiceId;

public class ViewedMessage {

  private final ServiceId sender;
  private final long      timestamp;

  public ViewedMessage(ServiceId sender, long timestamp) {
    this.sender    = sender;
    this.timestamp = timestamp;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public ServiceId getSender() {
    return sender;
  }
}
