package com.servalabs.chat.crypto.storage;

import android.content.Context;

import androidx.annotation.NonNull;

import com.servalabs.chat.keyvalue.SignalStore;
import com.servalabs.chat.libsignal.api.SignalServiceDataStore;
import com.servalabs.chat.core.models.ServiceId;

public final class SignalServiceDataStoreImpl implements SignalServiceDataStore {

  private final Context                           context;
  private final SignalServiceAccountDataStoreImpl aciStore;
  private final SignalServiceAccountDataStoreImpl pniStore;

  public SignalServiceDataStoreImpl(@NonNull Context context,
                                    @NonNull SignalServiceAccountDataStoreImpl aciStore,
                                    @NonNull SignalServiceAccountDataStoreImpl pniStore)
  {
    this.context  = context;
    this.aciStore = aciStore;
    this.pniStore = pniStore;
  }

  @Override
  public SignalServiceAccountDataStoreImpl get(@NonNull ServiceId accountIdentifier) {
    if (accountIdentifier.equals(SignalStore.account().getAci())) {
      return aciStore;
    } else if (accountIdentifier.equals(SignalStore.account().getPni())) {
      return pniStore;
    } else {
      throw new IllegalArgumentException("No matching store found for " + accountIdentifier);
    }
  }

  @Override
  public SignalServiceAccountDataStoreImpl aci() {
    return aciStore;
  }

  @Override
  public SignalServiceAccountDataStoreImpl pni() {
    return pniStore;
  }

  @Override
  public boolean isMultiDevice() {
    return SignalStore.account().isMultiDevice();
  }
}
