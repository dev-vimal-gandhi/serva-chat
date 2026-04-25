package com.servalabs.chat.dependencies

import com.servalabs.chat.libsignal.keytrans.KeyTransparencyException
import com.servalabs.chat.libsignal.net.KeyTransparency.CheckMode
import com.servalabs.chat.libsignal.net.RequestResult
import com.servalabs.chat.libsignal.net.getOrError
import com.servalabs.chat.libsignal.protocol.IdentityKey
import com.servalabs.chat.libsignal.protocol.ServiceId
import com.servalabs.chat.database.model.KeyTransparencyStore
import com.servalabs.chat.libsignal.api.websocket.SignalWebSocket

/**
 * Operations used when interacting with [com.servalabs.chat.libsignal.net.KeyTransparencyClient]
 */
class KeyTransparencyApi(private val unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket) {

  suspend fun check(checkMode: CheckMode, aci: ServiceId.Aci, aciIdentityKey: IdentityKey, e164: String?, unidentifiedAccessKey: ByteArray?, usernameHash: ByteArray?, keyTransparencyStore: KeyTransparencyStore): RequestResult<Unit, KeyTransparencyException> {
    return unauthWebSocket.runCatchingWithUnauthChatConnection { chatConnection ->
      chatConnection.keyTransparencyClient().check(checkMode, aci, aciIdentityKey, e164, unidentifiedAccessKey, usernameHash, keyTransparencyStore)
    }.getOrError()
  }
}
