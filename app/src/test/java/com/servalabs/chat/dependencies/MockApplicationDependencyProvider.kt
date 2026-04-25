package com.servalabs.chat.dependencies

import io.mockk.mockk
import com.servalabs.chat.core.util.billing.BillingApi
import com.servalabs.chat.core.util.concurrent.DeadlockDetector
import com.servalabs.chat.libsignal.net.Network
import com.servalabs.chat.libsignal.zkgroup.profiles.ClientZkProfileOperations
import com.servalabs.chat.libsignal.zkgroup.receipts.ClientZkReceiptOperations
import com.servalabs.chat.components.TypingStatusRepository
import com.servalabs.chat.components.TypingStatusSender
import com.servalabs.chat.crypto.storage.SignalServiceDataStoreImpl
import com.servalabs.chat.database.DatabaseObserver
import com.servalabs.chat.database.PendingRetryReceiptCache
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
import com.servalabs.chat.libsignal.internal.configuration.SignalServiceConfiguration
import com.servalabs.chat.libsignal.internal.push.PushServiceSocket
import java.util.function.Supplier

class MockApplicationDependencyProvider : AppDependencies.Provider {
  override fun providePushServiceSocket(signalServiceConfiguration: SignalServiceConfiguration, groupsV2Operations: GroupsV2Operations): PushServiceSocket {
    return mockk(relaxed = true)
  }

  override fun provideGroupsV2Operations(signalServiceConfiguration: SignalServiceConfiguration): GroupsV2Operations {
    return mockk(relaxed = true)
  }

  override fun provideSignalServiceAccountManager(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, accountApi: AccountApi, pushServiceSocket: PushServiceSocket, groupsV2Operations: GroupsV2Operations): SignalServiceAccountManager {
    return mockk(relaxed = true)
  }

  override fun provideSignalServiceMessageSender(
    protocolStore: SignalServiceDataStore,
    pushServiceSocket: PushServiceSocket,
    attachmentApi: AttachmentApi,
    messageApi: MessageApi,
    keysApi: KeysApi
  ): SignalServiceMessageSender {
    return mockk(relaxed = true)
  }

  override fun provideSignalServiceMessageReceiver(pushServiceSocket: PushServiceSocket): SignalServiceMessageReceiver {
    return mockk(relaxed = true)
  }

  override fun provideSignalServiceNetworkAccess(): SignalServiceNetworkAccess {
    return mockk(relaxed = true)
  }

  override fun provideRecipientCache(): LiveRecipientCache {
    return mockk(relaxed = true)
  }

  override fun provideJobManager(): JobManager {
    return mockk(relaxed = true)
  }

  override fun provideFrameRateTracker(): FrameRateTracker {
    return mockk(relaxed = true)
  }

  override fun provideMegaphoneRepository(): MegaphoneRepository {
    return mockk(relaxed = true)
  }

  override fun provideEarlyMessageCache(): EarlyMessageCache {
    return mockk(relaxed = true)
  }

  override fun provideMessageNotifier(): MessageNotifier {
    return mockk(relaxed = true)
  }

  override fun provideIncomingMessageObserver(webSocket: SignalWebSocket.AuthenticatedWebSocket, unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket): IncomingMessageObserver {
    return mockk(relaxed = true)
  }

  override fun provideTrimThreadsByDateManager(): TrimThreadsByDateManager {
    return mockk(relaxed = true)
  }

  override fun provideViewOnceMessageManager(): ViewOnceMessageManager {
    return mockk(relaxed = true)
  }

  override fun provideExpiringStoriesManager(): ExpiringStoriesManager {
    return mockk(relaxed = true)
  }

  override fun provideExpiringArchivedStoriesManager(): ExpiringArchivedStoriesManager {
    return mockk(relaxed = true)
  }

  override fun provideExpiringMessageManager(): ExpiringMessageManager {
    return mockk(relaxed = true)
  }

  override fun provideDeletedCallEventManager(): DeletedCallEventManager {
    return mockk(relaxed = true)
  }

  override fun provideTypingStatusRepository(): TypingStatusRepository {
    return mockk(relaxed = true)
  }

  override fun provideTypingStatusSender(): TypingStatusSender {
    return mockk(relaxed = true)
  }

  override fun provideDatabaseObserver(): DatabaseObserver {
    return mockk(relaxed = true)
  }

  override fun provideSignalCallManager(): SignalCallManager {
    return mockk(relaxed = true)
  }

  override fun providePendingRetryReceiptManager(): PendingRetryReceiptManager {
    return mockk(relaxed = true)
  }

  override fun providePendingRetryReceiptCache(): PendingRetryReceiptCache {
    return mockk(relaxed = true)
  }

  override fun provideProtocolStore(): SignalServiceDataStoreImpl {
    return mockk(relaxed = true)
  }

  override fun provideGiphyMp4Cache(): GiphyMp4Cache {
    return mockk(relaxed = true)
  }

  override fun provideExoPlayerPool(): SimpleExoPlayerPool {
    return mockk(relaxed = true)
  }

  override fun provideAndroidCallAudioManager(): AudioManagerCompat {
    return mockk(relaxed = true)
  }

  override fun provideDonationsService(donationsApi: DonationsApi): DonationsService {
    return mockk(relaxed = true)
  }

  override fun provideProfileService(
    profileOperations: ClientZkProfileOperations,
    authWebSocket: SignalWebSocket.AuthenticatedWebSocket,
    unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket
  ): ProfileService {
    return mockk(relaxed = true)
  }

  override fun provideDeadlockDetector(): DeadlockDetector {
    return mockk(relaxed = true)
  }

  override fun provideClientZkReceiptOperations(signalServiceConfiguration: SignalServiceConfiguration): ClientZkReceiptOperations {
    return mockk(relaxed = true)
  }

  override fun provideScheduledMessageManager(): ScheduledMessageManager {
    return mockk(relaxed = true)
  }

  override fun provideNetworkManager(): NetworkManager {
    return mockk(relaxed = true)
  }

  override fun providePinnedMessageManager(): PinnedMessageManager {
    return mockk(relaxed = true)
  }

  override fun provideLibsignalNetwork(config: SignalServiceConfiguration): Network {
    return mockk(relaxed = true)
  }

  override fun provideBillingApi(): BillingApi {
    return mockk(relaxed = true)
  }

  override fun provideArchiveApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket, pushServiceSocket: PushServiceSocket): ArchiveApi {
    return mockk(relaxed = true)
  }

  override fun provideKeysApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket): KeysApi {
    return mockk(relaxed = true)
  }

  override fun provideAttachmentApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, pushServiceSocket: PushServiceSocket): AttachmentApi {
    return mockk(relaxed = true)
  }

  override fun provideLinkDeviceApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket): LinkDeviceApi {
    return mockk(relaxed = true)
  }

  override fun provideRegistrationApi(pushServiceSocket: PushServiceSocket): RegistrationApi {
    return mockk(relaxed = true)
  }

  override fun provideStorageServiceApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, pushServiceSocket: PushServiceSocket): StorageServiceApi {
    return mockk(relaxed = true)
  }

  override fun provideAuthWebSocket(signalServiceConfigurationSupplier: Supplier<SignalServiceConfiguration>, libSignalNetworkSupplier: Supplier<Network>): SignalWebSocket.AuthenticatedWebSocket {
    return mockk(relaxed = true)
  }

  override fun provideUnauthWebSocket(signalServiceConfigurationSupplier: Supplier<SignalServiceConfiguration>, libSignalNetworkSupplier: Supplier<Network>): SignalWebSocket.UnauthenticatedWebSocket {
    return mockk(relaxed = true)
  }

  override fun provideAccountApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket): AccountApi {
    return mockk(relaxed = true)
  }

  override fun provideUsernameApi(unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket): UsernameApi {
    return mockk(relaxed = true)
  }

  override fun provideCallingApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket, pushServiceSocket: PushServiceSocket): CallingApi {
    return mockk(relaxed = true)
  }

  override fun providePaymentsApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket): PaymentsApi {
    return mockk(relaxed = true)
  }

  override fun provideCdsApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket): CdsApi {
    return mockk(relaxed = true)
  }

  override fun provideRateLimitChallengeApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket): RateLimitChallengeApi {
    return mockk(relaxed = true)
  }

  override fun provideMessageApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket): MessageApi {
    return mockk(relaxed = true)
  }

  override fun provideProvisioningApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket): ProvisioningApi {
    return mockk(relaxed = true)
  }

  override fun provideCertificateApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket): CertificateApi {
    return mockk(relaxed = true)
  }

  override fun provideProfileApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket, pushServiceSocket: PushServiceSocket, clientZkProfileOperations: ClientZkProfileOperations): ProfileApi {
    return mockk(relaxed = true)
  }

  override fun provideRemoteConfigApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, pushServiceSocket: PushServiceSocket): RemoteConfigApi {
    return mockk(relaxed = true)
  }

  override fun provideDonationsApi(authWebSocket: SignalWebSocket.AuthenticatedWebSocket, unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket): DonationsApi {
    return mockk(relaxed = true)
  }

  override fun provideSvrBApi(libSignalNetwork: Network): SvrBApi {
    return mockk(relaxed = true)
  }

  override fun provideKeyTransparencyApi(unauthWebSocket: SignalWebSocket.UnauthenticatedWebSocket): KeyTransparencyApi {
    return mockk(relaxed = true)
  }
}
