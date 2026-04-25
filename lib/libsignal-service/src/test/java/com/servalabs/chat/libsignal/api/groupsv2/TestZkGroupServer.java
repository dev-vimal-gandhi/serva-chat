package com.servalabs.chat.libsignal.api.groupsv2;

import com.servalabs.chat.libsignal.zkgroup.ServerPublicParams;
import com.servalabs.chat.libsignal.zkgroup.ServerSecretParams;
import com.servalabs.chat.libsignal.zkgroup.VerificationFailedException;
import com.servalabs.chat.libsignal.zkgroup.groups.GroupPublicParams;
import com.servalabs.chat.libsignal.zkgroup.profiles.ExpiringProfileKeyCredentialResponse;
import com.servalabs.chat.libsignal.zkgroup.profiles.ProfileKeyCommitment;
import com.servalabs.chat.libsignal.zkgroup.profiles.ProfileKeyCredentialPresentation;
import com.servalabs.chat.libsignal.zkgroup.profiles.ProfileKeyCredentialRequest;
import com.servalabs.chat.libsignal.zkgroup.profiles.ServerZkProfileOperations;
import com.servalabs.chat.core.models.ServiceId.ACI;
import com.servalabs.chat.libsignal.testutil.LibSignalLibraryUtil;

import java.time.Instant;

/**
 * Provides Zk group operations that the server would provide.
 */
final class TestZkGroupServer {

  private final ServerPublicParams        serverPublicParams;
  private final ServerZkProfileOperations serverZkProfileOperations;

  TestZkGroupServer() {
    LibSignalLibraryUtil.assumeLibSignalSupportedOnOS();

    ServerSecretParams serverSecretParams = ServerSecretParams.generate();

    serverPublicParams        = serverSecretParams.getPublicParams();
    serverZkProfileOperations = new ServerZkProfileOperations(serverSecretParams);
  }

  public ServerPublicParams getServerPublicParams() {
    return serverPublicParams;
  }

  public ExpiringProfileKeyCredentialResponse getExpiringProfileKeyCredentialResponse(ProfileKeyCredentialRequest request, ACI aci, ProfileKeyCommitment commitment, Instant expiration) throws VerificationFailedException {
    return serverZkProfileOperations.issueExpiringProfileKeyCredential(request, aci.getLibSignalAci(), commitment, expiration);
  }

  public void assertProfileKeyCredentialPresentation(GroupPublicParams publicParams, ProfileKeyCredentialPresentation profileKeyCredentialPresentation, Instant now) {
    try {
      serverZkProfileOperations.verifyProfileKeyCredentialPresentation(publicParams, profileKeyCredentialPresentation, now);
    } catch (VerificationFailedException e) {
      throw new AssertionError(e);
    }
  }
}
