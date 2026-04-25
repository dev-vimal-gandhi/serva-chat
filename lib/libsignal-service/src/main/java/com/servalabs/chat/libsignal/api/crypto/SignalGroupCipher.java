package com.servalabs.chat.libsignal.api.crypto;

import com.servalabs.chat.libsignal.protocol.DuplicateMessageException;
import com.servalabs.chat.libsignal.protocol.InvalidMessageException;
import com.servalabs.chat.libsignal.protocol.LegacyMessageException;
import com.servalabs.chat.libsignal.protocol.NoSessionException;
import com.servalabs.chat.libsignal.protocol.groups.GroupCipher;
import com.servalabs.chat.libsignal.protocol.message.CiphertextMessage;
import com.servalabs.chat.libsignal.api.SignalSessionLock;

import java.util.UUID;

/**
 * A thread-safe wrapper around {@link GroupCipher}.
 */
public class SignalGroupCipher {

  private final SignalSessionLock lock;
  private final GroupCipher       cipher;

  public SignalGroupCipher(SignalSessionLock lock, GroupCipher cipher) {
    this.lock   = lock;
    this.cipher = cipher;
  }

  public CiphertextMessage encrypt(UUID distributionId, byte[] paddedPlaintext) throws NoSessionException {
    try (SignalSessionLock.Lock unused = lock.acquire()) {
      return cipher.encrypt(distributionId, paddedPlaintext);
    }
  }

  public byte[] decrypt(byte[] senderKeyMessageBytes)
      throws LegacyMessageException, DuplicateMessageException, InvalidMessageException, NoSessionException
  {
    try (SignalSessionLock.Lock unused = lock.acquire()) {
      return cipher.decrypt(senderKeyMessageBytes);
    }
  }
}
