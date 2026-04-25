package com.servalabs.chat.qr.kitkat;

import androidx.annotation.NonNull;

public interface ScanListener {
  void onQrDataFound(@NonNull String data);
  default void onNoScan() {}
}
