package com.servalabs.chat.libsignal.api.crypto;

import com.servalabs.chat.libsignal.metadata.certificate.SenderCertificate;
import com.servalabs.chat.libsignal.metadata.protocol.UnidentifiedSenderMessageContent;
import com.servalabs.chat.libsignal.protocol.InvalidKeyException;
import com.servalabs.chat.libsignal.protocol.NoSessionException;
import com.servalabs.chat.libsignal.protocol.SignalProtocolAddress;
import com.servalabs.chat.libsignal.protocol.UntrustedIdentityException;
import com.servalabs.chat.libsignal.protocol.message.CiphertextMessage;
import com.servalabs.chat.libsignal.protocol.message.DecryptionErrorMessage;
import com.servalabs.chat.libsignal.protocol.message.PlaintextContent;
import com.servalabs.chat.libsignal.internal.push.Content;
import com.servalabs.chat.libsignal.internal.push.Envelope.Type;
import com.servalabs.chat.libsignal.internal.push.OutgoingPushMessage;
import com.servalabs.chat.libsignal.internal.push.PushTransportDetails;
import com.servalabs.chat.core.util.Base64;

import java.util.Optional;

/**
 * An abstraction over the different types of message contents we can have.
 */
public interface EnvelopeContent {

  /**
   * Processes the content using sealed sender.
   */
  OutgoingPushMessage processSealedSender(SignalSessionCipher sessionCipher,
                                          SignalSealedSessionCipher sealedSessionCipher,
                                          SignalProtocolAddress destination,
                                          SenderCertificate senderCertificate)
      throws UntrustedIdentityException, InvalidKeyException, NoSessionException;

  /**
   * Processes the content using unsealed sender.
   */
  OutgoingPushMessage processUnsealedSender(SignalSessionCipher sessionCipher, SignalProtocolAddress destination) throws UntrustedIdentityException, NoSessionException;

  /**
   * An estimated size, in bytes.
   */
  int size();

  /**
   * A content proto, if applicable.
   */
  Optional<Content> getContent();

  /**
   * Wrap {@link Content} you plan on sending as an encrypted message.
   * This is the default. Consider anything else exceptional.
   */
  static EnvelopeContent encrypted(Content content, ContentHint contentHint, Optional<byte[]> groupId) {
    return new Encrypted(content, contentHint, groupId);
  }

  /**
   * Wraps a {@link PlaintextContent}. This is exceptional, currently limited only to {@link DecryptionErrorMessage}.
   */
  static EnvelopeContent plaintext(PlaintextContent content, Optional<byte[]> groupId) {
    return new Plaintext(content, groupId);
  }

  class Encrypted implements EnvelopeContent {

    private final Content          content;
    private final ContentHint      contentHint;
    private final Optional<byte[]> groupId;

    public Encrypted(Content content, ContentHint contentHint, Optional<byte[]> groupId) {
      this.content     = content;
      this.contentHint = contentHint;
      this.groupId     = groupId;
    }

    @Override
    public OutgoingPushMessage processSealedSender(SignalSessionCipher sessionCipher,
                                                   SignalSealedSessionCipher sealedSessionCipher,
                                                   SignalProtocolAddress destination,
                                                   SenderCertificate senderCertificate)
        throws UntrustedIdentityException, InvalidKeyException, NoSessionException
    {
      PushTransportDetails             transportDetails = new PushTransportDetails();
      CiphertextMessage                message          = sessionCipher.encrypt(transportDetails.getPaddedMessageBody(content.encode()));
      UnidentifiedSenderMessageContent messageContent   = new UnidentifiedSenderMessageContent(message,
                                                                                               senderCertificate,
                                                                                               contentHint.getType(),
                                                                                               groupId);

      byte[] ciphertext           = sealedSessionCipher.encrypt(destination, messageContent);
      String body                 = Base64.encodeWithPadding(ciphertext);
      int    remoteRegistrationId = sealedSessionCipher.getRemoteRegistrationId(destination);

      return new OutgoingPushMessage(Type.UNIDENTIFIED_SENDER.getValue(), destination.getDeviceId(), remoteRegistrationId, body);
    }

    @Override
    public OutgoingPushMessage processUnsealedSender(SignalSessionCipher sessionCipher, SignalProtocolAddress destination) throws UntrustedIdentityException, NoSessionException {
      PushTransportDetails transportDetails     = new PushTransportDetails();
      CiphertextMessage    message              = sessionCipher.encrypt(transportDetails.getPaddedMessageBody(content.encode()));
      int                  remoteRegistrationId = sessionCipher.getRemoteRegistrationId();
      String               body                 = Base64.encodeWithPadding(message.serialize());

      int type;

      switch (message.getType()) {
        case CiphertextMessage.PREKEY_TYPE:  type = Type.PREKEY_MESSAGE.getValue(); break;
        case CiphertextMessage.WHISPER_TYPE: type = Type.DOUBLE_RATCHET.getValue();    break;
        default: throw new AssertionError("Bad type: " + message.getType());
      }

      return new OutgoingPushMessage(type, destination.getDeviceId(), remoteRegistrationId, body);
    }

    @Override
    public int size() {
      return Content.ADAPTER.encodedSize(content);
    }

    @Override
    public Optional<Content> getContent() {
      return Optional.of(content);
    }
  }

  class Plaintext implements EnvelopeContent {

    private final PlaintextContent plaintextContent;
    private final Optional<byte[]> groupId;

    public Plaintext(PlaintextContent plaintextContent, Optional<byte[]> groupId) {
      this.plaintextContent = plaintextContent;
      this.groupId          = groupId;
    }

    @Override
    public OutgoingPushMessage processSealedSender(SignalSessionCipher sessionCipher,
                                                   SignalSealedSessionCipher sealedSessionCipher,
                                                   SignalProtocolAddress destination,
                                                   SenderCertificate senderCertificate)
        throws UntrustedIdentityException, InvalidKeyException
    {
      UnidentifiedSenderMessageContent messageContent = new UnidentifiedSenderMessageContent(plaintextContent,
                                                                                             senderCertificate,
                                                                                             ContentHint.IMPLICIT.getType(),
                                                                                             groupId);

      byte[] ciphertext           = sealedSessionCipher.encrypt(destination, messageContent);
      String body                 = Base64.encodeWithPadding(ciphertext);
      int    remoteRegistrationId = sealedSessionCipher.getRemoteRegistrationId(destination);

      return new OutgoingPushMessage(Type.UNIDENTIFIED_SENDER.getValue(), destination.getDeviceId(), remoteRegistrationId, body);
    }

    @Override
    public OutgoingPushMessage processUnsealedSender(SignalSessionCipher sessionCipher, SignalProtocolAddress destination) {
      String body                 = Base64.encodeWithPadding(plaintextContent.serialize());
      int    remoteRegistrationId = sessionCipher.getRemoteRegistrationId();

      return new OutgoingPushMessage(Type.PLAINTEXT_CONTENT.getValue(), destination.getDeviceId(), remoteRegistrationId, body);
    }

    @Override
    public int size() {
      return plaintextContent.getBody().length;
    }

    @Override
    public Optional<Content> getContent() {
      return Optional.empty();
    }
  }
}
