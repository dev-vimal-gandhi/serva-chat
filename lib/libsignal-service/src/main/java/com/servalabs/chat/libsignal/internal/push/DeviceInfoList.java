package com.servalabs.chat.libsignal.internal.push;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.servalabs.chat.libsignal.api.messages.multidevice.DeviceInfo;

import java.util.List;

public class DeviceInfoList {

  @JsonProperty
  public List<DeviceInfo> devices;

  public DeviceInfoList() {}

  public List<DeviceInfo> getDevices() {
    return devices;
  }
}
