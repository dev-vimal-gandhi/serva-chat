/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.jobs;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.annimon.stream.Stream;

import org.greenrobot.eventbus.EventBus;
import com.servalabs.chat.blurhash.BlurHash;
import com.servalabs.chat.core.models.ServiceId.ACI;
import com.servalabs.chat.core.util.Base64;
import com.servalabs.chat.core.util.Hex;
import com.servalabs.chat.core.util.Util;
import com.servalabs.chat.core.util.logging.Log;
import org.signal.libsignal.zkgroup.InvalidInputException;
import org.signal.libsignal.zkgroup.receipts.ReceiptCredentialPresentation;
import com.servalabs.chat.TextSecureExpiredException;
import com.servalabs.chat.attachments.Attachment;
import com.servalabs.chat.attachments.AttachmentId;
import com.servalabs.chat.attachments.DatabaseAttachment;
import com.servalabs.chat.contactshare.Contact;
import com.servalabs.chat.contactshare.ContactModelMapper;
import com.servalabs.chat.crypto.ProfileKeyUtil;
import com.servalabs.chat.database.MessageTable;
import com.servalabs.chat.database.NoSuchMessageException;
import com.servalabs.chat.database.SignalDatabase;
import com.servalabs.chat.database.model.Mention;
import com.servalabs.chat.database.model.MessageRecord;
import com.servalabs.chat.database.model.MmsMessageRecord;
import com.servalabs.chat.database.model.ParentStoryId;
import com.servalabs.chat.database.model.StickerRecord;
import com.servalabs.chat.database.model.databaseprotos.BodyRangeList;
import com.servalabs.chat.database.model.databaseprotos.GiftBadge;
import com.servalabs.chat.database.model.databaseprotos.PinnedMessage;
import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.events.PartProgressEvent;
import com.servalabs.chat.jobmanager.Job;
import com.servalabs.chat.jobmanager.JobManager;
import com.servalabs.chat.jobmanager.JobTracker;
import com.servalabs.chat.keyvalue.SignalStore;
import com.servalabs.chat.linkpreview.LinkPreview;
import com.servalabs.chat.mms.OutgoingMessage;
import com.servalabs.chat.mms.PartAuthority;
import com.servalabs.chat.mms.QuoteModel;
import com.servalabs.chat.net.NotPushRegisteredException;
import com.servalabs.chat.notifications.v2.ConversationId;
import com.servalabs.chat.polls.Poll;
import com.servalabs.chat.recipients.Recipient;
import com.servalabs.chat.recipients.RecipientId;
import com.servalabs.chat.recipients.RecipientUtil;
import com.servalabs.chat.transport.RetryLaterException;
import com.servalabs.chat.transport.UndeliverableMessageException;
import com.servalabs.chat.util.MediaUtil;
import com.servalabs.chat.libsignal.api.crypto.AttachmentCipherStreamUtil;
import com.servalabs.chat.libsignal.api.messages.AttachmentTransferProgress;
import com.servalabs.chat.libsignal.internal.crypto.PaddingInputStream;
import com.servalabs.chat.libsignal.api.messages.SignalServiceAttachment;
import com.servalabs.chat.libsignal.api.messages.SignalServiceAttachmentPointer;
import com.servalabs.chat.libsignal.api.messages.SignalServiceAttachmentRemoteId;
import com.servalabs.chat.libsignal.api.messages.SignalServiceDataMessage;
import com.servalabs.chat.libsignal.api.messages.SignalServicePreview;
import com.servalabs.chat.libsignal.api.messages.shared.SharedContact;
import com.servalabs.chat.libsignal.api.push.exceptions.ProofRequiredException;
import com.servalabs.chat.libsignal.api.push.exceptions.ServerRejectedException;
import com.servalabs.chat.libsignal.internal.push.BodyRange;
import com.servalabs.chat.libsignal.internal.push.http.ResumableUploadSpec;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class PushSendJob extends SendJob {

  private static final String TAG                           = Log.tag(PushSendJob.class);
  private static final long   PUSH_CHALLENGE_TIMEOUT        = TimeUnit.SECONDS.toMillis(10);

  protected PushSendJob(Job.Parameters parameters) {
    super(parameters);
  }

  @Override
  protected final void onSend() throws Exception {
    long timeSinceAciSignedPreKeyRotation = System.currentTimeMillis() - SignalStore.account().aciPreKeys().getLastSignedPreKeyRotationTime();
    long timeSincePniSignedPreKeyRotation = System.currentTimeMillis() - SignalStore.account().pniPreKeys().getLastSignedPreKeyRotationTime();

    if (timeSinceAciSignedPreKeyRotation > PreKeysSyncJob.MAXIMUM_ALLOWED_SIGNED_PREKEY_AGE ||
        timeSinceAciSignedPreKeyRotation < 0 ||
        timeSincePniSignedPreKeyRotation > PreKeysSyncJob.MAXIMUM_ALLOWED_SIGNED_PREKEY_AGE ||
        timeSincePniSignedPreKeyRotation < 0
    ) {
      warn(TAG, "It's been too long since rotating our signed prekeys (ACI: " + timeSinceAciSignedPreKeyRotation + " ms, PNI: " + timeSincePniSignedPreKeyRotation + " ms)! Attempting to rotate now.");

      Optional<JobTracker.JobState> state = AppDependencies.getJobManager().runSynchronously(PreKeysSyncJob.create(), TimeUnit.SECONDS.toMillis(30));

      if (state.isPresent() && state.get() == JobTracker.JobState.SUCCESS) {
        log(TAG, "Successfully refreshed prekeys. Continuing.");
      } else {
        throw new RetryLaterException(new TextSecureExpiredException("Failed to refresh prekeys! State: " + (state.isEmpty() ? "<empty>" : state.get())));
      }
    }

    if (!Recipient.self().isRegistered()) {
      throw new NotPushRegisteredException();
    }

    onPushSend();

    if (SignalStore.rateLimit().needsRecaptcha()) {
      Log.i(TAG, "Successfully sent message. Assuming reCAPTCHA no longer needed.");
      SignalStore.rateLimit().onProofAccepted();
    }
  }

  @Override
  public void onRetry() {
    Log.i(TAG, "onRetry()");

    if (getRunAttempt() > 1) {
      Log.i(TAG, "Scheduling service outage detection job.");
      AppDependencies.getJobManager().add(new ServiceOutageDetectionJob());
    }
  }

  @Override
  protected boolean shouldTrace() {
    return true;
  }

  @Override
  public boolean onShouldRetry(@NonNull Exception exception) {
    if (exception instanceof ServerRejectedException) {
      return false;
    }

    if (exception instanceof NotPushRegisteredException) {
      return false;
    }

    return exception instanceof IOException         ||
           exception instanceof RetryLaterException ||
           exception instanceof ProofRequiredException;
  }

  @Override
  public long getNextRunAttemptBackoff(int pastAttemptCount, @NonNull Exception exception) {
    return SendJobUtil.getBackoffMillisFromException(this, TAG, pastAttemptCount, exception, () -> super.getNextRunAttemptBackoff(pastAttemptCount, exception));
  }

  protected Optional<byte[]> getProfileKey(@NonNull Recipient recipient) {
    if (!recipient.resolve().isSystemContact() && !recipient.resolve().isProfileSharing()) {
      return Optional.empty();
    }

    return Optional.of(ProfileKeyUtil.getSelfProfileKey().serialize());
  }

  protected SignalServiceAttachment getAttachmentFor(Contact.Avatar avatar) {
    Attachment attachment = avatar.getAttachment();

    try {
      if (attachment.getUri() == null || attachment.size == 0) throw new IOException("Assertion failed, outgoing attachment has no data!");
      InputStream         inputStream      = PartAuthority.getAttachmentStream(context, attachment.getUri());
      long                ciphertextLength = AttachmentCipherStreamUtil.getCiphertextLength(PaddingInputStream.getPaddedSize(attachment.size));
      ResumableUploadSpec uploadSpec       = AppDependencies.getSignalServiceMessageSender().getResumableUploadSpec(ciphertextLength);

      return SignalServiceAttachment.newStreamBuilder()
                                    .withStream(inputStream)
                                    .withContentType(attachment.contentType)
                                    .withLength(attachment.size)
                                    .withFileName(attachment.fileName)
                                    .withVoiceNote(attachment.voiceNote)
                                    .withBorderless(attachment.borderless)
                                    .withGif(attachment.videoGif)
                                    .withFaststart(attachment.transformProperties.mp4FastStart)
                                    .withWidth(attachment.width)
                                    .withHeight(attachment.height)
                                    .withCaption(attachment.caption)
                                    .withUuid(attachment.uuid)
                                    .withResumableUploadSpec(uploadSpec)
                                    .withListener(new SignalServiceAttachment.ProgressListener() {
                                      @Override
                                      public void onAttachmentProgress(@NonNull AttachmentTransferProgress progress) {
                                        EventBus.getDefault().postSticky(new PartProgressEvent(attachment, PartProgressEvent.Type.NETWORK, progress));
                                      }

                                      @Override
                                      public boolean shouldCancel() {
                                        return isCanceled();
                                      }
                                    })
                                    .build();
    } catch (IOException ioe) {
      Log.w(TAG, "Couldn't open attachment", ioe);
    }
    return null;
  }

  protected static Set<String> enqueueCompressingAndUploadAttachmentsChains(@NonNull JobManager jobManager, OutgoingMessage message) {
    List<Attachment> attachments = new LinkedList<>();

    attachments.addAll(message.getAttachments());

    attachments.addAll(Stream.of(message.getLinkPreviews())
                             .map(LinkPreview::getThumbnail)
                             .filter(Optional::isPresent)
                             .map(Optional::get)
                             .toList());

    attachments.addAll(Stream.of(message.getSharedContacts())
                             .map(Contact::getAvatar).withoutNulls()
                             .map(Contact.Avatar::getAttachment).withoutNulls()
                             .toList());

    HashSet<String> jobs = new HashSet<>(Stream.of(attachments).map(a -> {
                                 final AttachmentId attachmentId = ((DatabaseAttachment) a).attachmentId;
                                 Log.d(TAG, "Enqueueing job chain to upload " + attachmentId);
                                 AttachmentUploadJob attachmentUploadJob = new AttachmentUploadJob(attachmentId);

                                 jobManager.startChain(AttachmentCompressionJob.fromAttachment((DatabaseAttachment) a, false, -1))
                                           .then(attachmentUploadJob)
                                           .enqueue();

                                 return attachmentUploadJob.getId();
                               })
                               .toList());

    if (message.getOutgoingQuote() != null && message.getOutgoingQuote().getAttachment() != null) {
      AttachmentId attachmentId = ((DatabaseAttachment) message.getOutgoingQuote().getAttachment()).attachmentId;

      if (SignalDatabase.attachments().hasData(attachmentId)) {
        AttachmentUploadJob quoteUploadJob = new AttachmentUploadJob(attachmentId);
        jobManager.add(quoteUploadJob);
        jobs.add(quoteUploadJob.getId());
      }
    }

    return jobs;
  }

  protected @NonNull List<SignalServiceAttachment> getAttachmentPointersFor(List<Attachment> attachments) {
    return Stream.of(attachments).map(this::getAttachmentPointerFor).filter(a -> a != null).toList();
  }

  protected @Nullable SignalServiceAttachment getAttachmentPointerFor(Attachment attachment) {
    if (TextUtils.isEmpty(attachment.remoteLocation)) {
      Log.w(TAG, "empty content id");
      return null;
    }

    if (TextUtils.isEmpty(attachment.remoteKey)) {
      Log.w(TAG, "empty encrypted key");
      return null;
    }

    try {
      final SignalServiceAttachmentRemoteId remoteId = SignalServiceAttachmentRemoteId.from(attachment.remoteLocation);
      final byte[]                          key      = Base64.decode(attachment.remoteKey);

      int width  = attachment.width;
      int height = attachment.height;

      if ((width == 0 || height == 0) && MediaUtil.hasVideoThumbnail(context, attachment.getUri())) {
        Bitmap thumbnail = MediaUtil.getVideoThumbnail(context, attachment.getUri(), 1000);

        if (thumbnail != null) {
          width  = thumbnail.getWidth();
          height = thumbnail.getHeight();
        }
      }

      return new SignalServiceAttachmentPointer(attachment.cdn.getCdnNumber(),
                                                remoteId,
                                                attachment.contentType,
                                                key,
                                                Optional.of(Util.toIntExact(attachment.size)),
                                                Optional.empty(),
                                                width,
                                                height,
                                                Optional.ofNullable(attachment.remoteDigest),
                                                Optional.ofNullable(attachment.getIncrementalDigest()),
                                                attachment.incrementalMacChunkSize,
                                                Optional.ofNullable(attachment.fileName),
                                                attachment.voiceNote,
                                                attachment.borderless,
                                                attachment.videoGif,
                                                Optional.ofNullable(attachment.caption),
                                                Optional.ofNullable(attachment.blurHash).map(BlurHash::getHash),
                                                attachment.uploadTimestamp,
                                                attachment.uuid);
    } catch (IOException | ArithmeticException e) {
      Log.w(TAG, e);
      return null;
    }
  }

  protected static void notifyMediaMessageDeliveryFailed(Context context, long messageId) {
    long                     threadId           = SignalDatabase.messages().getThreadIdForMessage(messageId);
    Recipient                recipient          = SignalDatabase.threads().getRecipientForThreadId(threadId);
    ParentStoryId.GroupReply groupReplyStoryId  = SignalDatabase.messages().getParentStoryIdForGroupReply(messageId);

    boolean isStory = false;
    try {
      MessageRecord record = SignalDatabase.messages().getMessageRecord(messageId);
      if (record instanceof MmsMessageRecord) {
        isStory = (((MmsMessageRecord) record).getStoryType().isStory());
      }
    } catch (NoSuchMessageException e) {
      Log.e(TAG, e);
    }

    if (threadId != -1 && recipient != null) {
      if (isStory) {
        SignalDatabase.messages().markAsNotNotified(messageId);
        AppDependencies.getMessageNotifier().notifyStoryDeliveryFailed(context, recipient, ConversationId.forConversation(threadId));
      } else {
        AppDependencies.getMessageNotifier().notifyMessageDeliveryFailed(context, recipient, ConversationId.fromThreadAndReply(threadId, groupReplyStoryId));
      }
    }
  }

  protected Optional<SignalServiceDataMessage.Quote> getQuoteFor(OutgoingMessage message) throws IOException {
    if (message.getOutgoingQuote() == null) return Optional.empty();
    if (message.isMessageEdit()) {
      return Optional.of(new SignalServiceDataMessage.Quote(0, ACI.UNKNOWN, "", null, null, SignalServiceDataMessage.Quote.Type.NORMAL, null));
    }

    long                                                  quoteId              = message.getOutgoingQuote().getId();
    String                                                quoteBody            = message.getOutgoingQuote().getText();
    RecipientId                                           quoteAuthor          = message.getOutgoingQuote().getAuthor();
    List<SignalServiceDataMessage.Mention>                quoteMentions        = getMentionsFor(message.getOutgoingQuote().getMentions());
    List<BodyRange>                                       bodyRanges           = getBodyRanges(message.getOutgoingQuote().getBodyRanges());
    QuoteModel.Type                                       quoteType            = message.getOutgoingQuote().getType();
    List<SignalServiceDataMessage.Quote.QuotedAttachment> quoteAttachments     = new LinkedList<>();
    Optional<Attachment>                                  localQuoteAttachment = Optional.ofNullable(message.getOutgoingQuote()).map(QuoteModel::getAttachment);

    if (localQuoteAttachment.isPresent() && MediaUtil.isViewOnceType(localQuoteAttachment.get().contentType)) {
      localQuoteAttachment = Optional.empty();
    }

    if (localQuoteAttachment.isPresent()) {
      Attachment              attachment             = localQuoteAttachment.get();
      SignalServiceAttachment quoteAttachmentPointer = getAttachmentPointerFor(localQuoteAttachment.get());

      quoteAttachments.add(new SignalServiceDataMessage.Quote.QuotedAttachment(attachment.quoteTargetContentType != null ? attachment.quoteTargetContentType : MediaUtil.IMAGE_JPEG,
                                                                               attachment.fileName,
                                                                               quoteAttachmentPointer));
    }

    Recipient quoteAuthorRecipient = Recipient.resolved(quoteAuthor);

    if (quoteAuthorRecipient.isMaybeRegistered()) {
      return Optional.of(new SignalServiceDataMessage.Quote(quoteId, RecipientUtil.getOrFetchServiceId(context, quoteAuthorRecipient), quoteBody, quoteAttachments, quoteMentions, quoteType.getDataMessageType(), bodyRanges));
    } else if (quoteAuthorRecipient.getHasServiceId()) {
      return Optional.of(new SignalServiceDataMessage.Quote(quoteId, quoteAuthorRecipient.requireAci(), quoteBody, quoteAttachments, quoteMentions, quoteType.getDataMessageType(), bodyRanges));
    } else {
      return Optional.empty();
    }
  }

  protected Optional<SignalServiceDataMessage.Sticker> getStickerFor(OutgoingMessage message) {
    Attachment stickerAttachment = Stream.of(message.getAttachments()).filter(Attachment::isSticker).findFirst().orElse(null);

    if (stickerAttachment == null) {
      return Optional.empty();
    }

    try {
      byte[]                  packId     = Hex.fromStringCondensed(stickerAttachment.stickerLocator.packId);
      byte[]                  packKey    = Hex.fromStringCondensed(stickerAttachment.stickerLocator.packKey);
      int                     stickerId  = stickerAttachment.stickerLocator.stickerId;
      StickerRecord           record     = SignalDatabase.stickers().getSticker(stickerAttachment.stickerLocator.packId, stickerId, false);
      String                  emoji      = record != null ? record.emoji : null;
      SignalServiceAttachment attachment = getAttachmentPointerFor(stickerAttachment);

      return Optional.of(new SignalServiceDataMessage.Sticker(packId, packKey, stickerId, emoji, attachment));
    } catch (IOException e) {
      Log.w(TAG, "Failed to decode sticker id/key", e);
      return Optional.empty();
    }
  }

  protected Optional<SignalServiceDataMessage.Reaction> getStoryReactionFor(@NonNull OutgoingMessage message, @NonNull SignalServiceDataMessage.StoryContext storyContext) {
    if (message.isStoryReaction()) {
      return Optional.of(new SignalServiceDataMessage.Reaction(message.getBody(),
                                                               false,
                                                               storyContext.getAuthorServiceId(),
                                                               storyContext.getSentTimestamp()));
    } else {
      return Optional.empty();
    }
  }

  List<SharedContact> getSharedContactsFor(OutgoingMessage mediaMessage) {
    List<SharedContact> sharedContacts = new LinkedList<>();

    for (Contact contact : mediaMessage.getSharedContacts()) {
      SharedContact.Builder builder = ContactModelMapper.localToRemoteBuilder(contact);
      SharedContact.Avatar  avatar  = null;

      if (contact.getAvatar() != null && contact.getAvatar().getAttachment() != null) {
        SignalServiceAttachment attachment = getAttachmentPointerFor(contact.getAvatar().getAttachment());
        if (attachment == null) {
          attachment = getAttachmentFor(contact.getAvatar());
        }
        avatar = SharedContact.Avatar.newBuilder().withAttachment(attachment)
                                                  .withProfileFlag(contact.getAvatar().isProfile())
                                                  .build();
      }

      builder.setAvatar(avatar);
      sharedContacts.add(builder.build());
    }

    return sharedContacts;
  }

  List<SignalServicePreview> getPreviewsFor(OutgoingMessage mediaMessage) {
    return Stream.of(mediaMessage.getLinkPreviews()).map(lp -> {
      SignalServiceAttachment attachment = lp.getThumbnail().isPresent() ? getAttachmentPointerFor(lp.getThumbnail().get()) : null;
      return new SignalServicePreview(lp.getUrl(), lp.getTitle(), lp.getDescription(), lp.getDate(), Optional.ofNullable(attachment));
    }).toList();
  }

  List<SignalServiceDataMessage.Mention> getMentionsFor(@NonNull List<Mention> mentions) {
    return Stream.of(mentions)
                 .map(m -> new SignalServiceDataMessage.Mention(Recipient.resolved(m.getRecipientId()).requireAci(), m.getStart(), m.getLength()))
                 .toList();
  }

  @Nullable SignalServiceDataMessage.GiftBadge getGiftBadgeFor(@NonNull OutgoingMessage message) throws UndeliverableMessageException {
    GiftBadge giftBadge = message.getGiftBadge();
    if (giftBadge == null) {
      return null;
    }

    try {
      ReceiptCredentialPresentation presentation = new ReceiptCredentialPresentation(giftBadge.redemptionToken.toByteArray());

      return new SignalServiceDataMessage.GiftBadge(presentation);
    } catch (InvalidInputException invalidInputException) {
      throw new UndeliverableMessageException(invalidInputException);
    }
  }

  protected @Nullable List<BodyRange> getBodyRanges(@NonNull OutgoingMessage message) {
    return getBodyRanges(message.getBodyRanges());
  }

  protected @Nullable SignalServiceDataMessage.PollCreate getPollCreate(OutgoingMessage message) {
    Poll poll = message.getPoll();
    if (poll == null) {
      return null;
    }

    return new SignalServiceDataMessage.PollCreate(poll.getQuestion(), poll.getAllowMultipleVotes(), poll.getPollOptions());
  }

  protected @Nullable SignalServiceDataMessage.PollTerminate getPollTerminate(OutgoingMessage message) {
    if (message.getMessageExtras() == null || message.getMessageExtras().pollTerminate == null) {
      return null;
    }

    return new SignalServiceDataMessage.PollTerminate(message.getMessageExtras().pollTerminate.targetTimestamp);
  }

  protected @Nullable List<BodyRange> getBodyRanges(@Nullable BodyRangeList bodyRanges) {
    if (bodyRanges == null || bodyRanges.ranges.size() == 0) {
      return null;
    }

    return bodyRanges
        .ranges
        .stream()
        .map(range -> {
          BodyRange.Builder builder = new BodyRange.Builder().start(range.start).length(range.length);

          if (range.style != null) {
            switch (range.style) {
              case BOLD:
                builder.style(BodyRange.Style.BOLD);
                break;
              case ITALIC:
                builder.style(BodyRange.Style.ITALIC);
                break;
              case SPOILER:
                builder.style(BodyRange.Style.SPOILER);
                break;
              case STRIKETHROUGH:
                builder.style(BodyRange.Style.STRIKETHROUGH);
                break;
              case MONOSPACE:
                builder.style(BodyRange.Style.MONOSPACE);
                break;
              default:
                throw new IllegalArgumentException("Unrecognized style");
            }
          } else {
            throw new IllegalArgumentException("Only supports style");
          }

          return builder.build();
        }).collect(Collectors.toList());
  }

  protected @Nullable SignalServiceDataMessage.PinnedMessage getPinnedMessage(OutgoingMessage message) {
    if (message.getMessageExtras() == null || message.getMessageExtras().pinnedMessage == null || ACI.parseOrNull(message.getMessageExtras().pinnedMessage.targetAuthorAci) == null) {
      return null;
    }

    PinnedMessage pinnedMessage = message.getMessageExtras().pinnedMessage;
    if (pinnedMessage.pinDurationInSeconds == MessageTable.PIN_FOREVER) {
      return new SignalServiceDataMessage.PinnedMessage(ACI.parseOrNull(pinnedMessage.targetAuthorAci), pinnedMessage.targetTimestamp, null, true);
    } else {
      return new SignalServiceDataMessage.PinnedMessage(ACI.parseOrNull(pinnedMessage.targetAuthorAci), pinnedMessage.targetTimestamp, (int) pinnedMessage.pinDurationInSeconds, null);
    }
  }

  protected abstract void onPushSend() throws Exception;

}
