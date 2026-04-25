package com.servalabs.chat.libsignal.api.crypto;

import com.servalabs.chat.libsignal.protocol.SessionBuilder;
import com.servalabs.chat.libsignal.protocol.SignalProtocolAddress;
import com.servalabs.chat.libsignal.protocol.groups.GroupSessionBuilder;
import com.servalabs.chat.libsignal.protocol.message.SenderKeyDistributionMessage;
import com.servalabs.chat.libsignal.api.SignalSessionLock;

import java.util.UUID;

/**
 * A thread-safe wrapper around {@link SessionBuilder}.
 */
public class SignalGroupSessionBuilder {

  private final SignalSessionLock   lock;
  private final GroupSessionBuilder builder;

  public SignalGroupSessionBuilder(SignalSessionLock lock, GroupSessionBuilder builder) {
    this.lock    = lock;
    this.builder = builder;
  }

  public void process(SignalProtocolAddress sender, SenderKeyDistributionMessage senderKeyDistributionMessage) {
    try (SignalSessionLock.Lock unused = lock.acquire()) {
      builder.process(sender, senderKeyDistributionMessage);
    }
  }

  public SenderKeyDistributionMessage create(SignalProtocolAddress sender, UUID distributionId) {
    try (SignalSessionLock.Lock unused = lock.acquire()) {
      return builder.create(sender, distributionId);
    }
  }
}
