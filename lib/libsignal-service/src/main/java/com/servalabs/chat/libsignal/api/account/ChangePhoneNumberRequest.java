package com.servalabs.chat.libsignal.api.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.servalabs.chat.libsignal.protocol.IdentityKey;
import com.servalabs.chat.libsignal.api.push.SignedPreKeyEntity;
import com.servalabs.chat.libsignal.internal.push.KyberPreKeyEntity;
import com.servalabs.chat.libsignal.internal.push.OutgoingPushMessage;
import com.servalabs.chat.libsignal.internal.util.JsonUtil;

import java.util.List;
import java.util.Map;

public final class ChangePhoneNumberRequest {
  @JsonProperty
  private String sessionId;

  @JsonProperty
  private String recoveryPassword;

  @JsonProperty
  private String number;

  @JsonProperty("reglock")
  private String registrationLock;

  @JsonProperty
  @JsonSerialize(using = JsonUtil.IdentityKeySerializer.class)
  @JsonDeserialize(using = JsonUtil.IdentityKeyDeserializer.class)
  private IdentityKey pniIdentityKey;

  @JsonProperty
  private List<OutgoingPushMessage> deviceMessages;

  @JsonProperty
  private Map<String, SignedPreKeyEntity> devicePniSignedPrekeys;

  @JsonProperty("devicePniPqLastResortPrekeys")
  private Map<String, KyberPreKeyEntity> devicePniLastResortKyberPrekeys;

  @JsonProperty
  private Map<String, Integer> pniRegistrationIds;

  @SuppressWarnings("unused") 
  public ChangePhoneNumberRequest() {}

  public ChangePhoneNumberRequest(String sessionId,
                                  String recoveryPassword,
                                  String number,
                                  String registrationLock,
                                  IdentityKey pniIdentityKey,
                                  List<OutgoingPushMessage> deviceMessages,
                                  Map<String, SignedPreKeyEntity> devicePniSignedPrekeys,
                                  Map<String, KyberPreKeyEntity> devicePniLastResortKyberPrekeys,
                                  Map<String, Integer> pniRegistrationIds)
  {
    this.sessionId                       = sessionId;
    this.recoveryPassword                = recoveryPassword;
    this.number                          = number;
    this.registrationLock                = registrationLock;
    this.pniIdentityKey                  = pniIdentityKey;
    this.deviceMessages                  = deviceMessages;
    this.devicePniSignedPrekeys          = devicePniSignedPrekeys;
    this.devicePniLastResortKyberPrekeys = devicePniLastResortKyberPrekeys;
    this.pniRegistrationIds              = pniRegistrationIds;
  }

  public String getNumber() {
    return number;
  }

  public String getRegistrationLock() {
    return registrationLock;
  }

  public IdentityKey getPniIdentityKey() {
    return pniIdentityKey;
  }

  public List<OutgoingPushMessage> getDeviceMessages() {
    return deviceMessages;
  }

  public Map<String, SignedPreKeyEntity> getDevicePniSignedPrekeys() {
    return devicePniSignedPrekeys;
  }

  public Map<String, Integer> getPniRegistrationIds() {
    return pniRegistrationIds;
  }
}
