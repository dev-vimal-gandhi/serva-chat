/**
 * Copyright (C) 2014-2016 Open Whisper Systems
 *
 * Licensed according to the LICENSE file in this repository.
 */

package com.servalabs.chat.libsignal.api.messages.multidevice;


import com.servalabs.chat.libsignal.api.messages.SignalServiceDataMessage;
import com.servalabs.chat.libsignal.api.messages.SignalServiceEditMessage;
import com.servalabs.chat.libsignal.api.messages.SignalServiceStoryMessage;
import com.servalabs.chat.libsignal.api.messages.SignalServiceStoryMessageRecipient;
import com.servalabs.chat.core.models.ServiceId;
import com.servalabs.chat.libsignal.api.push.SignalServiceAddress;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SentTranscriptMessage {

  private final Optional<SignalServiceAddress>          destination;
  private final long                                    timestamp;
  private final long                                    expirationStartTimestamp;
  private final Optional<SignalServiceDataMessage>      message;
  private final Map<ServiceId, Boolean>                 unidentifiedStatusBySid;
  private final Set<ServiceId>                          recipients;
  private final boolean                                 isRecipientUpdate;
  private final Optional<SignalServiceStoryMessage>     storyMessage;
  private final Set<SignalServiceStoryMessageRecipient> storyMessageRecipients;
  private final Optional<SignalServiceEditMessage>      editMessage;

  public SentTranscriptMessage(Optional<SignalServiceAddress> destination,
                               long timestamp,
                               Optional<SignalServiceDataMessage> message,
                               long expirationStartTimestamp,
                               Map<ServiceId, Boolean> unidentifiedStatus,
                               boolean isRecipientUpdate,
                               Optional<SignalServiceStoryMessage> storyMessage,
                               Set<SignalServiceStoryMessageRecipient> storyMessageRecipients,
                               Optional<SignalServiceEditMessage> editMessage)
  {
    this.destination              = destination;
    this.timestamp                = timestamp;
    this.message                  = message;
    this.expirationStartTimestamp = expirationStartTimestamp;
    this.unidentifiedStatusBySid  = new HashMap<>(unidentifiedStatus);
    this.recipients               = unidentifiedStatus.keySet();
    this.isRecipientUpdate        = isRecipientUpdate;
    this.storyMessage             = storyMessage;
    this.storyMessageRecipients   = storyMessageRecipients;
    this.editMessage              = editMessage;
  }

  public Optional<SignalServiceAddress> getDestination() {
    return destination;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public long getExpirationStartTimestamp() {
    return expirationStartTimestamp;
  }

  public Optional<SignalServiceDataMessage> getDataMessage() {
    return message;
  }

  public Optional<SignalServiceEditMessage> getEditMessage() {
    return editMessage;
  }

  public Optional<SignalServiceStoryMessage> getStoryMessage() {
    return storyMessage;
  }

  public Set<SignalServiceStoryMessageRecipient> getStoryMessageRecipients() {
    return storyMessageRecipients;
  }

  public boolean isUnidentified(ServiceId serviceId) {
    return unidentifiedStatusBySid.getOrDefault(serviceId, false);
  }

  public Set<ServiceId> getRecipients() {
    return recipients;
  }

  public boolean isRecipientUpdate() {
    return isRecipientUpdate;
  }
}
