package com.servalabs.chat.jobs;

import androidx.annotation.NonNull;

import com.annimon.stream.Stream;

import org.signal.core.util.logging.Log;
import com.servalabs.chat.BuildConfig;
import com.servalabs.chat.TextSecureExpiredException;
import com.servalabs.chat.attachments.Attachment;
import com.servalabs.chat.attachments.DatabaseAttachment;
import com.servalabs.chat.contactshare.Contact;
import com.servalabs.chat.database.AttachmentTable;
import com.servalabs.chat.database.SignalDatabase;
import com.servalabs.chat.jobmanager.Job;
import com.servalabs.chat.keyvalue.SignalStore;
import com.servalabs.chat.mms.OutgoingMessage;
import org.signal.core.util.Util;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class SendJob extends BaseJob {

  @SuppressWarnings("unused")
  private final static String TAG = Log.tag(SendJob.class);

  public SendJob(Job.Parameters parameters) {
    super(parameters);
  }

  @Override
  public final void onRun() throws Exception {
    if (SignalStore.misc().isClientDeprecated()) {
      throw new TextSecureExpiredException(String.format("TextSecure expired (build %d, now %d)",
                                                         BuildConfig.BUILD_TIMESTAMP_OR_ZERO,
                                                         System.currentTimeMillis()));
    }

    Log.i(TAG, "Starting message send attempt");
    onSend();
    Log.i(TAG, "Message send completed");
  }

  protected abstract void onSend() throws Exception;

  protected static void markAttachmentsUploaded(long messageId, @NonNull OutgoingMessage message) {
    List<Attachment> attachments = new LinkedList<>();

    attachments.addAll(message.getAttachments());
    attachments.addAll(Stream.of(message.getLinkPreviews()).map(lp -> lp.getThumbnail().orElse(null)).withoutNulls().toList());
    attachments.addAll(Stream.of(message.getSharedContacts()).map(Contact::getAvatarAttachment).withoutNulls().toList());

    if (message.getOutgoingQuote() != null && message.getOutgoingQuote().getAttachment() != null) {
      attachments.add(message.getOutgoingQuote().getAttachment());
    }

    AttachmentTable database = SignalDatabase.attachments();

    for (Attachment attachment : attachments) {
      database.markAttachmentUploaded(messageId, attachment);
    }
  }

  protected String buildAttachmentString(@NonNull List<Attachment> attachments) {
    List<String> strings = attachments.stream().map(attachment -> {
      if (attachment instanceof DatabaseAttachment) {
        return ((DatabaseAttachment) attachment).attachmentId.toString();
      } else if (attachment.getUri() != null) {
        return attachment.getUri().toString();
      } else {
        return attachment.toString();
      }
    }).collect(Collectors.toList());

    return Util.join(strings, ", ");
  }
}
