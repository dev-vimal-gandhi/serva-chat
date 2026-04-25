package com.servalabs.chat.libsignal.internal.configuration;


import com.servalabs.chat.libsignal.api.push.TrustStore;

import okhttp3.ConnectionSpec;

public class SignalCdsiUrl extends SignalUrl {

  public SignalCdsiUrl(String url, TrustStore trustStore) {
    super(url, trustStore);
  }

  public SignalCdsiUrl(String url, String hostHeader, TrustStore trustStore, ConnectionSpec connectionSpec) {
    super(url, hostHeader, trustStore, connectionSpec);
  }
}
