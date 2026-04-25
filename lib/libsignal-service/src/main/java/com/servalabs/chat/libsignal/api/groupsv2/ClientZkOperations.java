package com.servalabs.chat.libsignal.api.groupsv2;

import com.servalabs.chat.libsignal.zkgroup.InvalidInputException;
import com.servalabs.chat.libsignal.zkgroup.ServerPublicParams;
import com.servalabs.chat.libsignal.zkgroup.auth.ClientZkAuthOperations;
import com.servalabs.chat.libsignal.zkgroup.profiles.ClientZkProfileOperations;
import com.servalabs.chat.libsignal.zkgroup.receipts.ClientZkReceiptOperations;
import com.servalabs.chat.libsignal.internal.configuration.SignalServiceConfiguration;

/**
 * Contains access to all ZK group operations for the client.
 * <p>
 * Authorization and profile operations.
 */
public final class ClientZkOperations {

  private final ClientZkAuthOperations    clientZkAuthOperations;
  private final ClientZkProfileOperations clientZkProfileOperations;
  private final ClientZkReceiptOperations clientZkReceiptOperations;
  private final ServerPublicParams        serverPublicParams;

  public ClientZkOperations(ServerPublicParams serverPublicParams) {
    this.serverPublicParams        = serverPublicParams;
    this.clientZkAuthOperations    = new ClientZkAuthOperations   (serverPublicParams);
    this.clientZkProfileOperations = new ClientZkProfileOperations(serverPublicParams);
    this.clientZkReceiptOperations = new ClientZkReceiptOperations(serverPublicParams);
  }

  public static ClientZkOperations create(SignalServiceConfiguration configuration) {
    try {
      return new ClientZkOperations(new ServerPublicParams(configuration.getZkGroupServerPublicParams()));
    } catch (InvalidInputException e) {
      throw new AssertionError(e);
    }
  }

  public ClientZkAuthOperations getAuthOperations() {
    return clientZkAuthOperations;
  }

  public ClientZkProfileOperations getProfileOperations() {
    return clientZkProfileOperations;
  }

  public ClientZkReceiptOperations getReceiptOperations() {
    return clientZkReceiptOperations;
  }

  public ServerPublicParams getServerPublicParams() {
    return serverPublicParams;
  }
}
