package com.servalabs.chat.libsignal.api.groupsv2;

import com.servalabs.chat.core.util.Hex;
import com.servalabs.chat.libsignal.zkgroup.auth.AuthCredentialPresentation;
import com.servalabs.chat.libsignal.zkgroup.groups.GroupSecretParams;

import okhttp3.Credentials;

public final class GroupsV2AuthorizationString {

  private final String authString;

  GroupsV2AuthorizationString(GroupSecretParams groupSecretParams, AuthCredentialPresentation authCredentialPresentation) {
    String username = Hex.toStringCondensed(groupSecretParams.getPublicParams().serialize());
    String password = Hex.toStringCondensed(authCredentialPresentation.serialize());

    authString = Credentials.basic(username, password);
  }

  @Override
  public String toString() {
    return authString;
  }
}
