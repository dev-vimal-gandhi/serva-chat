package com.servalabs.chat;

import com.servalabs.chat.libsignal.zkgroup.ServerPublicParams;
import com.servalabs.chat.libsignal.zkgroup.ServerSecretParams;
import com.servalabs.chat.libsignal.zkgroup.VerificationFailedException;
import com.servalabs.chat.libsignal.zkgroup.groups.GroupPublicParams;
import com.servalabs.chat.libsignal.zkgroup.profiles.ProfileKeyCommitment;
import com.servalabs.chat.libsignal.zkgroup.profiles.ProfileKeyCredentialPresentation;
import com.servalabs.chat.libsignal.zkgroup.profiles.ProfileKeyCredentialRequest;
import com.servalabs.chat.libsignal.zkgroup.profiles.ServerZkProfileOperations;
import com.servalabs.chat.libsignal.test.LibSignalLibraryUtil;

import java.util.UUID;

/**
 * Provides Zk group operations that the server would provide.
 * Copied in app from libsignal
 */
public final class TestZkGroupServer {

  private final ServerPublicParams        serverPublicParams;
  private final ServerZkProfileOperations serverZkProfileOperations;

  public TestZkGroupServer() {
    LibSignalLibraryUtil.assumeLibSignalSupportedOnOS();

    ServerSecretParams serverSecretParams = ServerSecretParams.generate();

    serverPublicParams        = serverSecretParams.getPublicParams();
    serverZkProfileOperations = new ServerZkProfileOperations(serverSecretParams);
  }

  public ServerPublicParams getServerPublicParams() {
    return serverPublicParams;
  }
}
