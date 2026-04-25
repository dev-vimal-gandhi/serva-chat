package com.servalabs.chat.dependencies

import android.app.Application
import io.mockk.mockk
import io.mockk.spyk
import com.servalabs.chat.core.util.billing.BillingApi
import com.servalabs.chat.push.SignalServiceNetworkAccess
import com.servalabs.chat.recipients.LiveRecipientCache
import org.signal.libsignal.api.SignalServiceDataStore
import org.signal.libsignal.api.SignalServiceMessageSender
import org.signal.libsignal.api.account.AccountApi
import org.signal.libsignal.api.archive.ArchiveApi
import org.signal.libsignal.api.attachment.AttachmentApi
import org.signal.libsignal.api.donations.DonationsApi
import org.signal.libsignal.api.keys.KeysApi
import org.signal.libsignal.api.message.MessageApi
import org.signal.libsignal.api.websocket.SignalWebSocket
import com.servalabs.chat.libsignal.internal.push.PushServiceSocket

/**
 * Dependency provider used for instrumentation tests (aka androidTests).
 *
 * Handles setting up a mock web server for API calls, and provides mockable versions of [SignalServiceNetworkAccess].
 */
class InstrumentationApplicationDependencyProvider(val application: Application, private val default: ApplicationDependencyProvider) : AppDependencies.Provider by default {

  private val recipientCache: LiveRecipientCache
  private var signalServiceMessageSender: SignalServiceMessageSender? = null
  private var billingApi: BillingApi = mockk()
  private var accountApi: AccountApi = mockk()

  init {
    recipientCache = LiveRecipientCache(application) { r -> r.run() }
  }

  override fun provideBillingApi(): BillingApi = billingApi

  override fun provideAccountApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket): AccountApi = accountApi

  override fun provideRecipientCache(): LiveRecipientCache {
    return recipientCache
  }

  override fun provideArchiveApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket, pushServiceSocket: PushServiceSocket): ArchiveApi {
    return mockk()
  }

  override fun provideDonationsApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket): DonationsApi {
    return mockk()
  }

  override fun provideSignalServiceMessageSender(
    protocolStore: SignalServiceDataStore,
    pushServiceSocket: PushServiceSocket,
    attachmentApi: AttachmentApi,
    messageApi: MessageApi,
    keysApi: KeysApi
  ): SignalServiceMessageSender {
    if (signalServiceMessageSender == null) {
      signalServiceMessageSender = spyk(objToCopy = default.provideSignalServiceMessageSender(protocolStore, pushServiceSocket, attachmentApi, messageApi, keysApi))
    }
    return signalServiceMessageSender!!
  }
}
