package com.servalabs.chat.libsignal.api.crypto;

import com.servalabs.chat.libsignal.metadata.InvalidMetadataMessageException;
import com.servalabs.chat.libsignal.metadata.InvalidMetadataVersionException;
import com.servalabs.chat.libsignal.metadata.ProtocolDuplicateMessageException;
import com.servalabs.chat.libsignal.metadata.ProtocolInvalidKeyException;
import com.servalabs.chat.libsignal.metadata.ProtocolInvalidKeyIdException;
import com.servalabs.chat.libsignal.metadata.ProtocolInvalidMessageException;
import com.servalabs.chat.libsignal.metadata.ProtocolInvalidVersionException;
import com.servalabs.chat.libsignal.metadata.ProtocolLegacyMessageException;
import com.servalabs.chat.libsignal.metadata.ProtocolNoSessionException;
import com.servalabs.chat.libsignal.metadata.ProtocolUntrustedIdentityException;
import com.servalabs.chat.libsignal.metadata.SealedSessionCipher;
import com.servalabs.chat.libsignal.metadata.SelfSendException;
import com.servalabs.chat.libsignal.metadata.certificate.CertificateValidator;
import com.servalabs.chat.libsignal.metadata.protocol.UnidentifiedSenderMessageContent;
import com.servalabs.chat.libsignal.protocol.InvalidKeyException;
import com.servalabs.chat.libsignal.protocol.InvalidRegistrationIdException;
import com.servalabs.chat.libsignal.protocol.NoSessionException;
import com.servalabs.chat.libsignal.protocol.SignalProtocolAddress;
import com.servalabs.chat.libsignal.protocol.UntrustedIdentityException;
import com.servalabs.chat.libsignal.protocol.state.SessionRecord;
import com.servalabs.chat.libsignal.protocol.state.SignalProtocolStore;
import com.servalabs.chat.libsignal.api.SignalSessionLock;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A thread-safe wrapper around {@link SealedSessionCipher}.
 */
public class SignalSealedSessionCipher {

  private final SignalSessionLock   lock;
  private final SealedSessionCipher cipher;

  public SignalSealedSessionCipher(SignalSessionLock lock, SealedSessionCipher cipher) {
    this.lock   = lock;
    this.cipher = cipher;
  }

  public byte[] encrypt(SignalProtocolAddress destinationAddress, UnidentifiedSenderMessageContent content)
      throws InvalidKeyException, UntrustedIdentityException
  {
    try (SignalSessionLock.Lock unused = lock.acquire()) {
      return cipher.encrypt(destinationAddress, content);
    }
  }

  public byte[] multiRecipientEncrypt(List<SignalProtocolAddress> recipients, Map<SignalProtocolAddress, SessionRecord> sessionMap, UnidentifiedSenderMessageContent content)
      throws InvalidKeyException, UntrustedIdentityException, NoSessionException, InvalidRegistrationIdException
  {
    try (SignalSessionLock.Lock unused = lock.acquire()) {
      List<SessionRecord> recipientSessions = recipients.stream().map(sessionMap::get).collect(Collectors.toList());

      if (recipientSessions.contains(null)) {
        throw new NoSessionException("No session for some recipients");
      }

      return cipher.multiRecipientEncrypt(recipients, recipientSessions, content);
    }
  }

  public SealedSessionCipher.DecryptionResult decrypt(CertificateValidator validator, byte[] ciphertext, long timestamp) throws InvalidMetadataMessageException, InvalidMetadataVersionException, ProtocolInvalidMessageException, ProtocolInvalidKeyException, ProtocolNoSessionException, ProtocolLegacyMessageException, ProtocolInvalidVersionException, ProtocolDuplicateMessageException, ProtocolInvalidKeyIdException, ProtocolUntrustedIdentityException, SelfSendException {
    try (SignalSessionLock.Lock unused = lock.acquire()) {
      return cipher.decrypt(validator, ciphertext, timestamp);
    }
  }

  public int getSessionVersion(SignalProtocolAddress remoteAddress) {
    try (SignalSessionLock.Lock unused = lock.acquire()) {
      return cipher.getSessionVersion(remoteAddress);
    }
  }

  public int getRemoteRegistrationId(SignalProtocolAddress remoteAddress) {
    try (SignalSessionLock.Lock unused = lock.acquire()) {
      return cipher.getRemoteRegistrationId(remoteAddress);
    }
  }
}
