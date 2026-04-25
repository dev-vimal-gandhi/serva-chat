package com.servalabs.chat.dependencies

import android.app.Application
import com.servalabs.chat.base.ApplicationInstance
import io.reactivex.rxjava3.subjects.BehaviorSubject
import okhttp3.OkHttpClient
import com.servalabs.chat.core.ui.CoreUiDependencies
import com.servalabs.chat.core.util.CoreUtilDependencies
import com.servalabs.chat.core.util.billing.BillingApi
import com.servalabs.chat.core.util.concurrent.DeadlockDetector
import com.servalabs.chat.core.util.concurrent.LatestValueObservable
import com.servalabs.chat.core.util.resettableLazy
import com.servalabs.chat.glide.SignalGlideDependencies
import com.servalabs.chat.libsignal.net.Network
import com.servalabs.chat.libsignal.zkgroup.profiles.ClientZkProfileOperations
import com.servalabs.chat.libsignal.zkgroup.receipts.ClientZkReceiptOperations
import com.servalabs.chat.mediasend.MediaSendDependencies
import com.servalabs.chat.BuildConfig
import com.servalabs.chat.components.TypingStatusRepository
import com.servalabs.chat.components.TypingStatusSender
import com.servalabs.chat.crypto.storage.SignalServiceDataStoreImpl
import com.servalabs.chat.database.DatabaseObserver
import com.servalabs.chat.database.PendingRetryReceiptCache
import com.servalabs.chat.dependencies.AppDependencies.authWebSocket
import com.servalabs.chat.groups.GroupsV2Authorization
import com.servalabs.chat.jobmanager.JobManager
import com.servalabs.chat.megaphone.MegaphoneRepository
import com.servalabs.chat.messages.IncomingMessageObserver
import com.servalabs.chat.net.NetworkManager
import com.servalabs.chat.notifications.MessageNotifier
import com.servalabs.chat.push.SignalServiceNetworkAccess
import com.servalabs.chat.recipients.LiveRecipientCache
import com.servalabs.chat.revealable.ViewOnceMessageManager
import com.servalabs.chat.service.DeletedCallEventManager
import com.servalabs.chat.service.ExpiringArchivedStoriesManager
import com.servalabs.chat.service.ExpiringMessageManager
import com.servalabs.chat.service.ExpiringStoriesManager
import com.servalabs.chat.service.PendingRetryReceiptManager
import com.servalabs.chat.service.PinnedMessageManager
import com.servalabs.chat.service.ScheduledMessageManager
import com.servalabs.chat.service.TrimThreadsByDateManager
import com.servalabs.chat.service.webrtc.SignalCallManager
import com.servalabs.chat.util.EarlyMessageCache
import com.servalabs.chat.util.FrameRateTracker
import com.servalabs.chat.video.exo.GiphyMp4Cache
import com.servalabs.chat.video.exo.SimpleExoPlayerPool
import com.servalabs.chat.webrtc.audio.AudioManagerCompat
import com.servalabs.chat.libsignal.api.SignalServiceAccountManager
import com.servalabs.chat.libsignal.api.SignalServiceDataStore
import com.servalabs.chat.libsignal.api.SignalServiceMessageReceiver
import com.servalabs.chat.libsignal.api.SignalServiceMessageSender
import com.servalabs.chat.libsignal.api.account.AccountApi
import com.servalabs.chat.libsignal.api.archive.ArchiveApi
import com.servalabs.chat.libsignal.api.attachment.AttachmentApi
import com.servalabs.chat.libsignal.api.calling.CallingApi
import com.servalabs.chat.libsignal.api.cds.CdsApi
import com.servalabs.chat.libsignal.api.certificate.CertificateApi
import com.servalabs.chat.libsignal.api.donations.DonationsApi
import com.servalabs.chat.libsignal.api.groupsv2.GroupsV2Operations
import com.servalabs.chat.libsignal.api.keys.KeysApi
import com.servalabs.chat.libsignal.api.link.LinkDeviceApi
import com.servalabs.chat.libsignal.api.message.MessageApi
import com.servalabs.chat.libsignal.api.payments.PaymentsApi
import com.servalabs.chat.libsignal.api.profiles.ProfileApi
import com.servalabs.chat.libsignal.api.provisioning.ProvisioningApi
import com.servalabs.chat.libsignal.api.ratelimit.RateLimitChallengeApi
import com.servalabs.chat.libsignal.api.registration.RegistrationApi
import com.servalabs.chat.libsignal.api.remoteconfig.RemoteConfigApi
import com.servalabs.chat.libsignal.api.services.DonationsService
import com.servalabs.chat.libsignal.api.services.ProfileService
import com.servalabs.chat.libsignal.api.storage.StorageServiceApi
import com.servalabs.chat.libsignal.api.svr.SvrBApi
import com.servalabs.chat.libsignal.api.username.UsernameApi
import com.servalabs.chat.libsignal.api.websocket.SignalWebSocket
import com.servalabs.chat.libsignal.api.websocket.WebSocketConnectionState
import com.servalabs.chat.libsignal.internal.configuration.SignalServiceConfiguration
import com.servalabs.chat.libsignal.internal.push.PushServiceSocket
import java.util.function.Supplier

/**
 * Location for storing and retrieving application-scoped singletons. Users must call
 * [.init] before using any of the methods, preferably early on in
 * [Application.onCreate].
 *
 * All future application-scoped singletons should be written as normal objects, then placed here
 * to manage their singleton-ness.
 */
object AppDependencies {

  private lateinit var provider: Provider

  @JvmStatic
  @Synchronized
  fun init(provider: Provider) {
    if (this::provider.isInitialized) {
      return
    }

    AppDependencies.provider = provider
    installDependencyProviders()
  }

  @JvmStatic
  @Synchronized
  fun installDependencyProviders() {
    CoreUtilDependencies.init(
      application,
      CoreUtilDependenciesProvider,
      CoreUtilDependencies.BuildInfo(
        buildTimestamp = BuildConfig.BUILD_TIMESTAMP_OR_ZERO.takeUnless { it == 0L }
      )
    )
    CoreUiDependencies.init(application, CoreUiDependenciesProvider)
    SignalGlideDependencies.init(application, SignalGlideDependenciesProvider)
    MediaSendDependencies.init(application, MediaSendDependenciesProvider)
  }

  @JvmStatic
  val isInitialized: Boolean
    get() = this::provider.isInitialized

  @JvmStatic
  val application: Application
    // MOLLY: Ensure the app instance is always available for non-test runs, even before init() is called
    get() = ApplicationInstance.get()

  @JvmStatic
  val recipientCache: LiveRecipientCache by lazy {
    provider.provideRecipientCache()
  }

  @JvmStatic
  val jobManager: JobManager by lazy {
    provider.provideJobManager()
  }

  @JvmStatic
  val frameRateTracker: FrameRateTracker by lazy {
    provider.provideFrameRateTracker()
  }

  @JvmStatic
  val megaphoneRepository: MegaphoneRepository by lazy {
    provider.provideMegaphoneRepository()
  }

  @JvmStatic
  val earlyMessageCache: EarlyMessageCache by lazy {
    provider.provideEarlyMessageCache()
  }

  @JvmStatic
  val typingStatusRepository: TypingStatusRepository by lazy {
    provider.provideTypingStatusRepository()
  }

  @JvmStatic
  val typingStatusSender: TypingStatusSender by lazy {
    provider.provideTypingStatusSender()
  }

  @JvmStatic
  val databaseObserver: DatabaseObserver by lazy {
    provider.provideDatabaseObserver()
  }

  @JvmStatic
  val trimThreadsByDateManager: TrimThreadsByDateManager by lazy {
    provider.provideTrimThreadsByDateManager()
  }

  @JvmStatic
  val viewOnceMessageManager: ViewOnceMessageManager by lazy {
    provider.provideViewOnceMessageManager()
  }

  @JvmStatic
  val expiringMessageManager: ExpiringMessageManager by lazy {
    provider.provideExpiringMessageManager()
  }

  @JvmStatic
  val deletedCallEventManager: DeletedCallEventManager by lazy {
    provider.provideDeletedCallEventManager()
  }

  @JvmStatic
  val signalCallManager: SignalCallManager by lazy {
    provider.provideSignalCallManager()
  }

  @JvmStatic
  val pendingRetryReceiptManager: PendingRetryReceiptManager by lazy {
    provider.providePendingRetryReceiptManager()
  }

  @JvmStatic
  val pendingRetryReceiptCache: PendingRetryReceiptCache by lazy {
    provider.providePendingRetryReceiptCache()
  }

  @JvmStatic
  val messageNotifier: MessageNotifier by lazy {
    provider.provideMessageNotifier()
  }

  @JvmStatic
  val giphyMp4Cache: GiphyMp4Cache by lazy {
    provider.provideGiphyMp4Cache()
  }

  @JvmStatic
  val exoPlayerPool: SimpleExoPlayerPool by lazy {
    provider.provideExoPlayerPool()
  }

  @JvmStatic
  val deadlockDetector: DeadlockDetector by lazy {
    provider.provideDeadlockDetector()
  }

  @JvmStatic
  val expireStoriesManager: ExpiringStoriesManager by lazy {
    provider.provideExpiringStoriesManager()
  }

  @JvmStatic
  val expireArchivedStoriesManager: ExpiringArchivedStoriesManager by lazy {
    provider.provideExpiringArchivedStoriesManager()
  }

  @JvmStatic
  val scheduledMessageManager: ScheduledMessageManager by lazy {
    provider.provideScheduledMessageManager()
  }

  @JvmStatic
  val pinnedMessageManager: PinnedMessageManager by lazy {
    provider.providePinnedMessageManager()
  }

  @JvmStatic
  val androidCallAudioManager: AudioManagerCompat by lazy {
    provider.provideAndroidCallAudioManager()
  }

  @JvmStatic
  val billingApi: BillingApi by lazy {
    provider.provideBillingApi()
  }

  private val _webSocketObserver: BehaviorSubject<WebSocketConnectionState> = BehaviorSubject.create()

  /**
   * An observable that emits the current state of the WebSocket connection across the various lifecycles
   * of the [authWebSocket].
   */
  @JvmStatic
  val webSocketObserver: LatestValueObservable<WebSocketConnectionState> = LatestValueObservable(_webSocketObserver)

  @JvmStatic
  val networkManager: NetworkManager by lazy {
    provider.provideNetworkManager()
  }

  private val _networkModule = resettableLazy {
    NetworkDependenciesModule(application, provider, _webSocketObserver)
  }
  private val networkModule by _networkModule

  @JvmStatic
  val signalServiceNetworkAccess: SignalServiceNetworkAccess
    get() = networkModule.signalServiceNetworkAccess

  @JvmStatic
  val protocolStore: SignalServiceDataStoreImpl
    get() = networkModule.protocolStore

  @JvmStatic
  val signalServiceMessageSender: SignalServiceMessageSender
    get() = networkModule.signalServiceMessageSender

  @JvmStatic
  val signalServiceAccountManager: SignalServiceAccountManager
    get() = networkModule.signalServiceAccountManager

  @JvmStatic
  val signalServiceMessageReceiver: SignalServiceMessageReceiver
    get() = networkModule.signalServiceMessageReceiver

  @JvmStatic
  val incomingMessageObserver: IncomingMessageObserver
    get() = networkModule.incomingMessageObserver

  @JvmStatic
  val libsignalNetwork: Network
    get() = networkModule.libsignalNetwork()

  @JvmStatic
  val authWebSocket: SignalWebSocket.AuthenticatedWebSocket
    get() = networkModule.authWebSocket

  @JvmStatic
  val unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket
    get() = networkModule.unauthWebSocket

  @JvmStatic
  val groupsV2Authorization: GroupsV2Authorization
    get() = networkModule.groupsV2Authorization

  @JvmStatic
  val groupsV2Operations: GroupsV2Operations
    get() = networkModule.groupsV2Operations

  @JvmStatic
  val clientZkReceiptOperations
    get() = networkModule.clientZkReceiptOperations

  @JvmStatic
  val profileService: ProfileService
    get() = networkModule.profileService

  @JvmStatic
  val donationsService: DonationsService
    get() = networkModule.donationsService

  @JvmStatic
  val archiveApi: ArchiveApi
    get() = networkModule.archiveApi

  @JvmStatic
  val keysApi: KeysApi
    get() = networkModule.keysApi

  @JvmStatic
  val attachmentApi: AttachmentApi
    get() = networkModule.attachmentApi

  @JvmStatic
  val linkDeviceApi: LinkDeviceApi
    get() = networkModule.linkDeviceApi

  @JvmStatic
  val pushServiceSocket: PushServiceSocket
    get() = networkModule.pushServiceSocket

  @JvmStatic
  val registrationApi: RegistrationApi
    get() = networkModule.registrationApi

  val storageServiceApi: StorageServiceApi
    get() = networkModule.storageServiceApi

  val accountApi: AccountApi
    get() = networkModule.accountApi

  val usernameApi: UsernameApi
    get() = networkModule.usernameApi

  val svrBApi: SvrBApi
    get() = networkModule.svrBApi

  val callingApi: CallingApi
    get() = networkModule.callingApi

  val paymentsApi: PaymentsApi
    get() = networkModule.paymentsApi

  val cdsApi: CdsApi
    get() = networkModule.cdsApi

  val rateLimitChallengeApi: RateLimitChallengeApi
    get() = networkModule.rateLimitChallengeApi

  val messageApi: MessageApi
    get() = networkModule.messageApi

  val provisioningApi: ProvisioningApi
    get() = networkModule.provisioningApi

  val certificateApi: CertificateApi
    get() = networkModule.certificateApi

  val profileApi: ProfileApi
    get() = networkModule.profileApi

  val remoteConfigApi: RemoteConfigApi
    get() = networkModule.remoteConfigApi

  val donationsApi: DonationsApi
    get() = networkModule.donationsApi

  val keyTransparencyApi: KeyTransparencyApi
    get() = networkModule.keyTransparencyApi

  @JvmStatic
  val okHttpClient: OkHttpClient
    get() = networkModule.okHttpClient

  @JvmStatic
  val signalOkHttpClient: OkHttpClient
    get() = networkModule.signalOkHttpClient

  @JvmStatic
  fun resetProtocolStores() {
    networkModule.resetProtocolStores()
  }

  @JvmStatic
  fun resetNetwork(restartMessageObserver: Boolean) {
    networkModule.closeConnections()
    _networkModule.reset()
    if (restartMessageObserver) {
      startNetwork()
    }
  }

  @JvmStatic
  fun startNetwork() {
    networkModule.openConnections()
  }

  interface Provider {
    fun providePushServiceSocket(signalServiceConfiguration: SignalServiceConfiguration, groupsV2Operations: GroupsV2Operations): PushServiceSocket
    fun provideGroupsV2Operations(signalServiceConfiguration: SignalServiceConfiguration): GroupsV2Operations
    fun provideSignalServiceAccountManager(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, accountApi: AccountApi, pushServiceSocket: PushServiceSocket, groupsV2Operations: GroupsV2Operations): SignalServiceAccountManager
    fun provideSignalServiceMessageSender(protocolStore: SignalServiceDataStore, pushServiceSocket: PushServiceSocket, attachmentApi: AttachmentApi, messageApi: MessageApi, keysApi: KeysApi): SignalServiceMessageSender
    fun provideSignalServiceMessageReceiver(pushServiceSocket: PushServiceSocket): SignalServiceMessageReceiver
    fun provideSignalServiceNetworkAccess(): SignalServiceNetworkAccess
    fun provideRecipientCache(): LiveRecipientCache
    fun provideJobManager(): JobManager
    fun provideFrameRateTracker(): FrameRateTracker
    fun provideMegaphoneRepository(): MegaphoneRepository
    fun provideEarlyMessageCache(): EarlyMessageCache
    fun provideMessageNotifier(): MessageNotifier
    fun provideIncomingMessageObserver(webSocket: SignalWebSocket.AuthenticatedWebSocket, unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket): IncomingMessageObserver
    fun provideTrimThreadsByDateManager(): TrimThreadsByDateManager
    fun provideViewOnceMessageManager(): ViewOnceMessageManager
    fun provideExpiringStoriesManager(): ExpiringStoriesManager
    fun provideExpiringArchivedStoriesManager(): ExpiringArchivedStoriesManager
    fun provideExpiringMessageManager(): ExpiringMessageManager
    fun provideDeletedCallEventManager(): DeletedCallEventManager
    fun provideTypingStatusRepository(): TypingStatusRepository
    fun provideTypingStatusSender(): TypingStatusSender
    fun provideDatabaseObserver(): DatabaseObserver
    fun provideSignalCallManager(): SignalCallManager
    fun providePendingRetryReceiptManager(): PendingRetryReceiptManager
    fun providePendingRetryReceiptCache(): PendingRetryReceiptCache
    fun provideProtocolStore(): SignalServiceDataStoreImpl
    fun provideGiphyMp4Cache(): GiphyMp4Cache
    fun provideExoPlayerPool(): SimpleExoPlayerPool
    fun provideAndroidCallAudioManager(): AudioManagerCompat
    fun provideDonationsService(donationsApi: DonationsApi): DonationsService
    fun provideProfileService(profileOperations: ClientZkProfileOperations, authWebSocket: SignalWebSocket.AuthenticatedWebSocket, unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket): ProfileService
    fun provideDeadlockDetector(): DeadlockDetector
    fun provideClientZkReceiptOperations(signalServiceConfiguration: SignalServiceConfiguration): ClientZkReceiptOperations
    fun provideScheduledMessageManager(): ScheduledMessageManager
    fun provideNetworkManager(): NetworkManager
    fun providePinnedMessageManager(): PinnedMessageManager
    fun provideLibsignalNetwork(config: SignalServiceConfiguration): Network
    fun provideBillingApi(): BillingApi
    fun provideArchiveApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket, pushServiceSocket: PushServiceSocket): ArchiveApi
    fun provideKeysApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket): KeysApi
    fun provideAttachmentApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, pushServiceSocket: PushServiceSocket): AttachmentApi
    fun provideLinkDeviceApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket): LinkDeviceApi
    fun provideRegistrationApi(pushServiceSocket: PushServiceSocket): RegistrationApi
    fun provideStorageServiceApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, pushServiceSocket: PushServiceSocket): StorageServiceApi
    fun provideAuthWebSocket(signalServiceConfigurationSupplier: Supplier<SignalServiceConfiguration>, libSignalNetworkSupplier: Supplier<Network>): SignalWebSocket.AuthenticatedWebSocket
    fun provideUnauthWebSocket(signalServiceConfigurationSupplier: Supplier<SignalServiceConfiguration>, libSignalNetworkSupplier: Supplier<Network>): SignalWebSocket.UnauthenticatedWebSocket
    fun provideAccountApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket): AccountApi
    fun provideUsernameApi(unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket): UsernameApi
    fun provideCallingApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket, pushServiceSocket: PushServiceSocket): CallingApi
    fun providePaymentsApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket): PaymentsApi
    fun provideCdsApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket): CdsApi
    fun provideRateLimitChallengeApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket): RateLimitChallengeApi
    fun provideMessageApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket): MessageApi
    fun provideProvisioningApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket): ProvisioningApi
    fun provideCertificateApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket): CertificateApi
    fun provideProfileApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket, pushServiceSocket: PushServiceSocket, clientZkProfileOperations: ClientZkProfileOperations): ProfileApi
    fun provideRemoteConfigApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, pushServiceSocket: PushServiceSocket): RemoteConfigApi
    fun provideDonationsApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket): DonationsApi
    fun provideSvrBApi(libSignalNetwork: Network): SvrBApi
    fun provideKeyTransparencyApi(unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket): KeyTransparencyApi
  }
}
