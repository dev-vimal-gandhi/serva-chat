package com.servalabs.chat.jobs

import com.servalabs.chat.core.util.logging.Log
import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.jobmanager.Job
import com.servalabs.chat.jobmanager.impl.NetworkConstraint
import com.servalabs.chat.jobmanager.impl.SealedSenderConstraint
import com.servalabs.chat.keyvalue.SignalStore
import com.servalabs.chat.net.NotPushRegisteredException
import com.servalabs.chat.recipients.Recipient
import com.servalabs.chat.libsignal.api.crypto.UntrustedIdentityException
import com.servalabs.chat.libsignal.api.messages.multidevice.KeysMessage
import com.servalabs.chat.libsignal.api.messages.multidevice.SignalServiceSyncMessage
import com.servalabs.chat.libsignal.api.push.exceptions.PushNetworkException
import com.servalabs.chat.libsignal.api.push.exceptions.ServerRejectedException
import java.io.IOException

class MultiDeviceKeysUpdateJob private constructor(parameters: Parameters) : BaseJob(parameters) {

  companion object {
    const val KEY: String = "MultiDeviceKeysUpdateJob"

    private val TAG = Log.tag(MultiDeviceKeysUpdateJob::class.java)
  }

  constructor() : this(
    Parameters.Builder()
      .setQueue("MultiDeviceKeysUpdateJob")
      .setMaxInstancesForFactory(2)
      .addConstraint(NetworkConstraint.KEY)
      .addConstraint(SealedSenderConstraint.KEY)
      .setMaxAttempts(10)
      .build()
  )

  override fun serialize(): ByteArray? = null

  override fun getFactoryKey(): String = KEY

  @Throws(IOException::class, UntrustedIdentityException::class)
  public override fun onRun() {
    if (!Recipient.self().isRegistered) {
      throw NotPushRegisteredException()
    }

    if (!SignalStore.account.isMultiDevice) {
      Log.i(TAG, "Not multi device, aborting...")
      return
    }

    if (SignalStore.account.isLinkedDevice) {
      Log.i(TAG, "Not primary device, aborting...")
      return
    }

    val syncMessage = SignalServiceSyncMessage.forKeys(
      KeysMessage(
        storageService = SignalStore.storageService.storageKey,
        accountEntropyPool = SignalStore.account.accountEntropyPool,
        mediaRootBackupKey = SignalStore.backup.mediaRootBackupKey
      )
    )

    AppDependencies.signalServiceMessageSender.sendSyncMessage(syncMessage)
  }

  public override fun onShouldRetry(e: Exception): Boolean {
    if (e is ServerRejectedException) return false
    return e is PushNetworkException
  }

  override fun onFailure() {
  }

  class Factory : Job.Factory<MultiDeviceKeysUpdateJob?> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): MultiDeviceKeysUpdateJob {
      return MultiDeviceKeysUpdateJob(parameters)
    }
  }
}
