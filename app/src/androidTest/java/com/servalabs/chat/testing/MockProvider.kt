package com.servalabs.chat.testing

import com.servalabs.chat.core.models.ServiceId
import org.signal.libsignal.protocol.IdentityKeyPair
import org.signal.libsignal.protocol.ecc.ECKeyPair
import org.signal.libsignal.protocol.state.PreKeyRecord
import org.signal.libsignal.protocol.util.KeyHelper
import org.signal.libsignal.protocol.util.Medium
import com.servalabs.chat.crypto.PreKeyUtil
import com.servalabs.chat.keyvalue.SignalStore
import org.signal.libsignal.api.messages.multidevice.DeviceInfo
import org.signal.libsignal.api.push.SignedPreKeyEntity
import com.servalabs.chat.libsignal.internal.push.AuthCredentials
import com.servalabs.chat.libsignal.internal.push.DeviceInfoList
import com.servalabs.chat.libsignal.internal.push.PreKeyEntity
import com.servalabs.chat.libsignal.internal.push.PreKeyResponse
import com.servalabs.chat.libsignal.internal.push.PreKeyResponseItem
import com.servalabs.chat.libsignal.internal.push.PushServiceSocket
import com.servalabs.chat.libsignal.internal.push.RegistrationSessionMetadataJson
import com.servalabs.chat.libsignal.internal.push.SenderCertificate
import com.servalabs.chat.libsignal.internal.push.VerifyAccountResponse
import com.servalabs.chat.libsignal.internal.push.WhoAmIResponse
import java.security.SecureRandom

/**
 * Warehouse of reusable test data and mock configurations.
 */
object MockProvider {

  val senderCertificate = SenderCertificate().apply { certificate = ByteArray(0) }

  val lockedFailure = PushServiceSocket.RegistrationLockFailure().apply {
    svr1Credentials = AuthCredentials.create("username", "password")
    svr2Credentials = AuthCredentials.create("username", "password")
  }

  val primaryOnlyDeviceList = DeviceInfoList().apply {
    devices = listOf(
      DeviceInfo().apply {
        id = 1
      }
    )
  }

  val sessionMetadataJson = RegistrationSessionMetadataJson(
    id = "asdfasdfasdfasdf",
    nextCall = null,
    nextSms = null,
    nextVerificationAttempt = null,
    allowedToRequestCode = true,
    requestedInformation = emptyList(),
    verified = true
  )

  fun createVerifyAccountResponse(aci: ServiceId, newPni: ServiceId): VerifyAccountResponse {
    return VerifyAccountResponse().apply {
      uuid = aci.toString()
      pni = newPni.toString()
      storageCapable = false
    }
  }

  fun createWhoAmIResponse(aci: ServiceId, pni: ServiceId, e164: String): WhoAmIResponse {
    return WhoAmIResponse(
      aci = aci.toString(),
      pni = pni.toString(),
      number = e164
    )
  }

  fun createPreKeyResponse(identity: IdentityKeyPair = SignalStore.account.aciIdentityKey, deviceId: Int): PreKeyResponse {
    val signedPreKeyRecord = PreKeyUtil.generateSignedPreKey(SecureRandom().nextInt(Medium.MAX_VALUE), identity.privateKey)
    val oneTimePreKey = PreKeyRecord(SecureRandom().nextInt(Medium.MAX_VALUE), ECKeyPair.generate())

    val device = PreKeyResponseItem().apply {
      this.deviceId = deviceId
      registrationId = KeyHelper.generateRegistrationId(false)
      signedPreKey = SignedPreKeyEntity(signedPreKeyRecord.id.toLong(), signedPreKeyRecord.keyPair.publicKey, signedPreKeyRecord.signature)
      preKey = PreKeyEntity(oneTimePreKey.id.toLong(), oneTimePreKey.keyPair.publicKey)
    }

    return PreKeyResponse().apply {
      identityKey = identity.publicKey
      devices = listOf(device)
    }
  }
}
