package com.servalabs.chat.libsignal.internal.push.http;

import com.servalabs.chat.libsignal.api.crypto.DigestingOutputStream;
import com.servalabs.chat.libsignal.api.crypto.NoCipherOutputStream;

import java.io.OutputStream;

/**
 * See {@link NoCipherOutputStream}.
 */
public final class NoCipherOutputStreamFactory implements OutputStreamFactory {

  @Override
  public DigestingOutputStream createFor(OutputStream wrap) {
    return new NoCipherOutputStream(wrap);
  }
}
