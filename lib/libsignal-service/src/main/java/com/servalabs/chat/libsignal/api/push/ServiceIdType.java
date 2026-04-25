package com.servalabs.chat.libsignal.api.push;

public enum ServiceIdType {
  ACI("aci"), PNI("pni");

  private final String queryParamName;

  ServiceIdType(String queryParamName) {
    this.queryParamName = queryParamName;
  }

  public String queryParam() {
    return queryParamName;
  }
}
