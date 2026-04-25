package com.servalabs.chat.libsignal.internal.configuration;


import com.servalabs.chat.libsignal.api.push.TrustStore;

import okhttp3.ConnectionSpec;

public class SignalCdnUrl extends SignalUrl {
  public SignalCdnUrl(String url, TrustStore trustStore) {
    super(url, trustStore);
  }

  public SignalCdnUrl(String url, String hostHeader, TrustStore trustStore, ConnectionSpec connectionSpec) {
    super(url, hostHeader, trustStore, connectionSpec);
  }
}
