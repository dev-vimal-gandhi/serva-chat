package com.servalabs.chat.libsignal.internal.push.http;


import com.servalabs.chat.libsignal.api.crypto.DigestingOutputStream;

import java.io.IOException;
import java.io.OutputStream;

public interface OutputStreamFactory {

  public DigestingOutputStream createFor(OutputStream wrap) throws IOException;

}
