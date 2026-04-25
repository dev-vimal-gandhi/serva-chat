package com.servalabs.chat.crypto.storage;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.groups.state.SenderKeyRecord;
import com.servalabs.chat.crypto.ReentrantSessionLock;
import com.servalabs.chat.database.SenderKeyTable;
import com.servalabs.chat.database.SignalDatabase;
import org.signal.libsignal.api.SignalServiceSenderKeyStore;
import org.signal.libsignal.api.SignalSessionLock;
import org.signal.libsignal.api.push.DistributionId;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * An implementation of the storage interface used by the protocol layer to store sender keys. For
 * more details around sender keys, see {@link SenderKeyTable}.
 */
public final class SignalSenderKeyStore implements SignalServiceSenderKeyStore {

  private final Context context;

  public SignalSenderKeyStore(@NonNull Context context) {
    this.context = context;
  }

  @Override
  public void storeSenderKey(@NonNull SignalProtocolAddress sender, @NonNull UUID distributionId, @NonNull SenderKeyRecord record) {
    try (SignalSessionLock.Lock unused = ReentrantSessionLock.INSTANCE.acquire()) {
      SignalDatabase.senderKeys().store(sender, DistributionId.from(distributionId), record);
    }
  }

  @Override
  public @Nullable SenderKeyRecord loadSenderKey(@NonNull SignalProtocolAddress sender, @NonNull UUID distributionId) {
    try (SignalSessionLock.Lock unused = ReentrantSessionLock.INSTANCE.acquire()) {
      return SignalDatabase.senderKeys().load(sender, DistributionId.from(distributionId));
    }
  }

  @Override
  public Set<SignalProtocolAddress> getSenderKeySharedWith(DistributionId distributionId) {
    try (SignalSessionLock.Lock unused = ReentrantSessionLock.INSTANCE.acquire()) {
      return SignalDatabase.senderKeyShared().getSharedWith(distributionId);
    }
  }

  @Override
  public void markSenderKeySharedWith(DistributionId distributionId, Collection<SignalProtocolAddress> addresses) {
    try (SignalSessionLock.Lock unused = ReentrantSessionLock.INSTANCE.acquire()) {
      SignalDatabase.senderKeyShared().markAsShared(distributionId, addresses);
    }
  }

  @Override
  public void clearSenderKeySharedWith(Collection<SignalProtocolAddress> addresses) {
    try (SignalSessionLock.Lock unused = ReentrantSessionLock.INSTANCE.acquire()) {
      SignalDatabase.senderKeyShared().deleteAllFor(addresses);
    }
  }

  /**
   * Removes all sender key session state for all devices for the provided recipient-distributionId pair.
   */
  public void deleteAllFor(@NonNull String addressName, @NonNull DistributionId distributionId) {
    try (SignalSessionLock.Lock unused = ReentrantSessionLock.INSTANCE.acquire()) {
      SignalDatabase.senderKeys().deleteAllFor(addressName, distributionId);
    }
  }

  /**
   * Deletes all sender key session state.
   */
  public void deleteAll() {
    try (SignalSessionLock.Lock unused = ReentrantSessionLock.INSTANCE.acquire()) {
      SignalDatabase.senderKeys().deleteAll();
    }
  }
}