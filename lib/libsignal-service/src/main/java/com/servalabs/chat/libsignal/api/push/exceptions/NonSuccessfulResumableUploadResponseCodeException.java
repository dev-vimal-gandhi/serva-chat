package com.servalabs.chat.libsignal.api.push.exceptions;

public class NonSuccessfulResumableUploadResponseCodeException extends NonSuccessfulResponseCodeException {
  public NonSuccessfulResumableUploadResponseCodeException(int code, String s) {
    super(code, s);
  }
}
