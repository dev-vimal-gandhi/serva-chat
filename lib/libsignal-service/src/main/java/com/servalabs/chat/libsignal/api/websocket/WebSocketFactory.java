package com.servalabs.chat.libsignal.api.websocket;

import com.servalabs.chat.libsignal.internal.websocket.WebSocketConnection;

public interface WebSocketFactory {
  WebSocketConnection createConnection() throws WebSocketUnavailableException;
}
