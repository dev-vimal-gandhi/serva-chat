/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.dependencies

import android.app.Application
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.subjects.Subject
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import com.servalabs.chat.core.util.logging.Log
import com.servalabs.chat.core.util.resettableLazy
import org.signal.libsignal.net.Network
import org.signal.libsignal.zkgroup.receipts.ClientZkReceiptOperations
import com.servalabs.chat.BuildConfig
import com.servalabs.chat.crypto.storage.SignalServiceDataStoreImpl
import com.servalabs.chat.groups.GroupsV2Authorization
import com.servalabs.chat.groups.GroupsV2AuthorizationMemoryValueCache
import com.servalabs.chat.keyvalue.SignalStore
import com.servalabs.chat.messages.IncomingMessageObserver
import com.servalabs.chat.net.Networking
import com.servalabs.chat.net.StandardUserAgentInterceptor
import com.servalabs.chat.push.SignalServiceNetworkAccess
import com.servalabs.chat.push.SignalServiceTrustStore
import org.signal.libsignal.api.SignalServiceAccountManager
import org.signal.libsignal.api.SignalServiceMessageReceiver
import org.signal.libsignal.api.SignalServiceMessageSender
import org.signal.libsignal.api.account.AccountApi
import org.signal.libsignal.api.archive.ArchiveApi
import org.signal.libsignal.api.attachment.AttachmentApi
import org.signal.libsignal.api.calling.CallingApi
import org.signal.libsignal.api.cds.CdsApi
import org.signal.libsignal.api.certificate.CertificateApi
import org.signal.libsignal.api.donations.DonationsApi
import org.signal.libsignal.api.groupsv2.GroupsV2Operations
import org.signal.libsignal.api.keys.KeysApi
import org.signal.libsignal.api.link.LinkDeviceApi
import org.signal.libsignal.api.message.MessageApi
import org.signal.libsignal.api.payments.PaymentsApi
import org.signal.libsignal.api.profiles.ProfileApi
import org.signal.libsignal.api.provisioning.ProvisioningApi
import org.signal.libsignal.api.push.TrustStore
import org.signal.libsignal.api.ratelimit.RateLimitChallengeApi
import org.signal.libsignal.api.registration.RegistrationApi
import org.signal.libsignal.api.remoteconfig.RemoteConfigApi
import org.signal.libsignal.api.services.DonationsService
import org.signal.libsignal.api.services.ProfileService
import org.signal.libsignal.api.storage.StorageServiceApi
import org.signal.libsignal.api.svr.SvrBApi
import org.signal.libsignal.api.username.UsernameApi
import org.signal.libsignal.api.util.Tls12SocketFactory
import org.signal.libsignal.api.websocket.SignalWebSocket
import org.signal.libsignal.api.websocket.WebSocketConnectionState
import org.signal.libsignal.api.websocket.WebSocketUnavailableException
import com.servalabs.chat.libsignal.internal.push.PushServiceSocket
import org.signal.libsignal.internal.util.BlacklistingTrustManager
import org.signal.libsignal.internal.util.Util
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * A subset of [AppDependencies] that relies on the network. We bundle them together because when the network
 * needs to get reset, we just throw out the whole thing and recreate it.
 */
class NetworkDependenciesModule(
  private val application: Application,
  private val provider: AppDependencies.Provider,
  private val webSocketStateSubject: Subject<WebSocketConnectionState>
) {

  companion object {
    private val TAG = "NetworkDependencies"
  }

  private val disposables: CompositeDisposable = CompositeDisposable()

  val signalServiceNetworkAccess: SignalServiceNetworkAccess by lazy {
    provider.provideSignalServiceNetworkAccess()
  }

  private val _protocolStore = resettableLazy {
    provider.provideProtocolStore()
  }
  val protocolStore: SignalServiceDataStoreImpl by _protocolStore

  private val _signalServiceMessageSender = resettableLazy {
    provider.provideSignalServiceMessageSender(protocolStore, pushServiceSocket, attachmentApi, messageApi, keysApi)
  }
  val signalServiceMessageSender: SignalServiceMessageSender by _signalServiceMessageSender

  val incomingMessageObserver: IncomingMessageObserver by lazy {
    provider.provideIncomingMessageObserver(authWebSocket, unauthWebSocket)
  }

  val pushServiceSocket: PushServiceSocket by lazy {
    provider.providePushServiceSocket(signalServiceNetworkAccess.getConfiguration(), groupsV2Operations)
  }

  val signalServiceAccountManager: SignalServiceAccountManager by lazy {
    provider.provideSignalServiceAccountManager(authWebSocket, accountApi, pushServiceSocket, groupsV2Operations)
  }

  private val _libsignalNetwork: Network by lazy {
    provider.provideLibsignalNetwork(signalServiceNetworkAccess.getConfiguration())
  }

  fun libsignalNetwork(): Network {
    return _libsignalNetwork.also { Networking.configureLibsignalProxy(it, BuildConfig.SIGNAL_URL) }
  }

  val authWebSocket: SignalWebSocket.AuthenticatedWebSocket by lazy {
    provider.provideAuthWebSocket({ signalServiceNetworkAccess.getConfiguration() }, { libsignalNetwork() }).also {
      disposables += it.state.subscribe { s -> webSocketStateSubject.onNext(s) }
    }
  }

  val unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket by lazy {
    provider.provideUnauthWebSocket({ signalServiceNetworkAccess.getConfiguration() }, { libsignalNetwork() })
  }

  val groupsV2Authorization: GroupsV2Authorization by lazy {
    val authCache: GroupsV2Authorization.ValueCache = GroupsV2AuthorizationMemoryValueCache(SignalStore.groupsV2AciAuthorizationCache)
    GroupsV2Authorization(signalServiceAccountManager.groupsV2Api, authCache)
  }

  val groupsV2Operations: GroupsV2Operations by lazy {
    provider.provideGroupsV2Operations(signalServiceNetworkAccess.getConfiguration())
  }

  val clientZkReceiptOperations: ClientZkReceiptOperations by lazy {
    provider.provideClientZkReceiptOperations(signalServiceNetworkAccess.getConfiguration())
  }

  val signalServiceMessageReceiver: SignalServiceMessageReceiver by lazy {
    provider.provideSignalServiceMessageReceiver(pushServiceSocket)
  }

  val profileService: ProfileService by lazy {
    provider.provideProfileService(groupsV2Operations.profileOperations, authWebSocket, unauthWebSocket)
  }

  val donationsService: DonationsService by lazy {
    provider.provideDonationsService(donationsApi)
  }

  val archiveApi: ArchiveApi by lazy {
    provider.provideArchiveApi(authWebSocket, unauthWebSocket, pushServiceSocket)
  }

  val keysApi: KeysApi by lazy {
    provider.provideKeysApi(authWebSocket, unauthWebSocket)
  }

  val attachmentApi: AttachmentApi by lazy {
    provider.provideAttachmentApi(authWebSocket, pushServiceSocket)
  }

  val linkDeviceApi: LinkDeviceApi by lazy {
    provider.provideLinkDeviceApi(authWebSocket)
  }

  val registrationApi: RegistrationApi by lazy {
    provider.provideRegistrationApi(pushServiceSocket)
  }

  val storageServiceApi: StorageServiceApi by lazy {
    provider.provideStorageServiceApi(authWebSocket, pushServiceSocket)
  }

  val accountApi: AccountApi by lazy {
    provider.provideAccountApi(authWebSocket)
  }

  val usernameApi: UsernameApi by lazy {
    provider.provideUsernameApi(unauthWebSocket)
  }

  val callingApi: CallingApi by lazy {
    provider.provideCallingApi(authWebSocket, unauthWebSocket, pushServiceSocket)
  }

  val paymentsApi: PaymentsApi by lazy {
    provider.providePaymentsApi(authWebSocket)
  }

  val cdsApi: CdsApi by lazy {
    provider.provideCdsApi(authWebSocket)
  }

  val rateLimitChallengeApi: RateLimitChallengeApi by lazy {
    provider.provideRateLimitChallengeApi(authWebSocket)
  }

  val messageApi: MessageApi by lazy {
    provider.provideMessageApi(authWebSocket, unauthWebSocket)
  }

  val provisioningApi: ProvisioningApi by lazy {
    provider.provideProvisioningApi(authWebSocket, unauthWebSocket)
  }

  val certificateApi: CertificateApi by lazy {
    provider.provideCertificateApi(authWebSocket)
  }

  val profileApi: ProfileApi by lazy {
    provider.provideProfileApi(authWebSocket, unauthWebSocket, pushServiceSocket, groupsV2Operations.profileOperations)
  }

  val remoteConfigApi: RemoteConfigApi by lazy {
    provider.provideRemoteConfigApi(authWebSocket, pushServiceSocket)
  }

  val donationsApi: DonationsApi by lazy {
    provider.provideDonationsApi(authWebSocket, unauthWebSocket)
  }

  val svrBApi: SvrBApi
    get() = provider.provideSvrBApi(libsignalNetwork())

  val keyTransparencyApi: KeyTransparencyApi by lazy {
    provider.provideKeyTransparencyApi(unauthWebSocket)
  }

  val okHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .socketFactory(Networking.socketFactory)
      .proxySelector(Networking.proxySelectorForSocks)
      .dns(Networking.dns)
      .addInterceptor(StandardUserAgentInterceptor())
      .build()
  }

  val signalOkHttpClient: OkHttpClient by lazy {
    try {
      val baseClient = okHttpClient
      val sslContext = SSLContext.getInstance("TLS")
      val trustStore: TrustStore = SignalServiceTrustStore(application)
      val trustManagers = BlacklistingTrustManager.createFor(trustStore)

      sslContext.init(null, trustManagers, null)

      val builder = baseClient.newBuilder()
        .sslSocketFactory(Tls12SocketFactory(sslContext.socketFactory), trustManagers[0] as X509TrustManager)
        .connectionSpecs(Util.immutableList(ConnectionSpec.RESTRICTED_TLS))

      builder.build()
    } catch (e: NoSuchAlgorithmException) {
      throw AssertionError(e)
    } catch (e: KeyManagementException) {
      throw AssertionError(e)
    }
  }

  fun closeConnections() {
    Log.i(TAG, "Closing connections.")
    incomingMessageObserver.terminate()
    if (_signalServiceMessageSender.isInitialized()) {
      signalServiceMessageSender.cancelInFlightRequests()
    }
    unauthWebSocket.disconnect()
    disposables.clear()
  }

  fun openConnections() {
    try {
      authWebSocket.connect()
    } catch (e: WebSocketUnavailableException) {
      Log.w(TAG, "Not allowed to start auth websocket", e)
    }

    try {
      unauthWebSocket.connect()
    } catch (e: WebSocketUnavailableException) {
      Log.w(TAG, "Not allowed to start unauth websocket", e)
    }

    incomingMessageObserver
  }

  fun resetProtocolStores() {
    _protocolStore.reset()
    _signalServiceMessageSender.reset()
  }
}
