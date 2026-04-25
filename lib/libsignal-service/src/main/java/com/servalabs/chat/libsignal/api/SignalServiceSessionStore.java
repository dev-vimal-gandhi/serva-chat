package com.servalabs.chat.libsignal.api;

import com.servalabs.chat.libsignal.protocol.SignalProtocolAddress;
import com.servalabs.chat.libsignal.protocol.state.SessionRecord;
import com.servalabs.chat.libsignal.protocol.state.SessionStore;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * And extension of the normal protocol session store interface that has additional methods that are
 * needed in the service layer, but not the protocol layer.
 */
public interface SignalServiceSessionStore extends SessionStore {
  void archiveSession(SignalProtocolAddress address);
  Map<SignalProtocolAddress, SessionRecord> getAllAddressesWithActiveSessions(List<String> addressNames);
}
