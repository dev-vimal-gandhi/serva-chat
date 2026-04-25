package com.servalabs.chat.libsignal.api.crypto;

import com.servalabs.chat.libsignal.protocol.DuplicateMessageException;
import com.servalabs.chat.libsignal.protocol.InvalidKeyException;
import com.servalabs.chat.libsignal.protocol.InvalidKeyIdException;
import com.servalabs.chat.libsignal.protocol.InvalidMessageException;
import com.servalabs.chat.libsignal.protocol.InvalidVersionException;
import com.servalabs.chat.libsignal.protocol.LegacyMessageException;
import com.servalabs.chat.libsignal.protocol.NoSessionException;
import com.servalabs.chat.libsignal.protocol.SessionCipher;
import com.servalabs.chat.libsignal.protocol.UntrustedIdentityException;
import com.servalabs.chat.libsignal.protocol.message.CiphertextMessage;
import com.servalabs.chat.libsignal.protocol.message.PreKeySignalMessage;
import com.servalabs.chat.libsignal.protocol.message.SignalMessage;
import com.servalabs.chat.libsignal.api.SignalSessionLock;

/**
 * A thread-safe wrapper around {@link SessionCipher}.
 */
public class SignalSessionCipher {

  private final SignalSessionLock lock;
  private final SessionCipher     cipher;

  public SignalSessionCipher(SignalSessionLock lock, SessionCipher cipher) {
    this.lock   = lock;
    this.cipher = cipher;
  }

  public CiphertextMessage encrypt(byte[] paddedMessage) throws com.servalabs.chat.libsignal.protocol.UntrustedIdentityException, NoSessionException {
    try (SignalSessionLock.Lock unused = lock.acquire()) {
      return cipher.encrypt(paddedMessage);
    }
  }

  public byte[] decrypt(PreKeySignalMessage ciphertext) throws DuplicateMessageException, LegacyMessageException, InvalidMessageException, InvalidKeyIdException, InvalidKeyException, com.servalabs.chat.libsignal.protocol.UntrustedIdentityException {
    try (SignalSessionLock.Lock unused = lock.acquire()) {
      return cipher.decrypt(ciphertext);
    }
  }

  public byte[] decrypt(SignalMessage ciphertext) throws InvalidMessageException, InvalidVersionException, DuplicateMessageException, LegacyMessageException, NoSessionException, UntrustedIdentityException {
    try (SignalSessionLock.Lock unused = lock.acquire()) {
      return cipher.decrypt(ciphertext);
    }
  }

  public int getRemoteRegistrationId() {
    try (SignalSessionLock.Lock unused = lock.acquire()) {
      return cipher.getRemoteRegistrationId();
    }
  }

  public int getSessionVersion() {
    try (SignalSessionLock.Lock unused = lock.acquire()) {
      return cipher.getSessionVersion();
    }
  }
}
