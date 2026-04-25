package com.servalabs.chat.libsignal.internal.push.exceptions;

import com.servalabs.chat.libsignal.api.push.exceptions.NonSuccessfulResponseCodeException;

import java.util.function.Function;

import okhttp3.ResponseBody;

public final class PaymentsRegionException extends NonSuccessfulResponseCodeException {
  public PaymentsRegionException(int code) {
    super(code);
  }
}
