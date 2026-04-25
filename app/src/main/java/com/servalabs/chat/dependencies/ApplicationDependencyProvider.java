package com.servalabs.chat.dependencies;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import org.jetbrains.annotations.NotNull;
import com.servalabs.chat.billing.BillingFactory;
import com.servalabs.chat.core.util.ThreadUtil;
import com.servalabs.chat.core.util.billing.BillingApi;
import com.servalabs.chat.core.util.concurrent.DeadlockDetector;
import com.servalabs.chat.core.util.concurrent.SignalExecutors;
import org.signal.libsignal.net.Network;
import org.signal.libsignal.zkgroup.profiles.ClientZkProfileOperations;
import org.signal.libsignal.zkgroup.receipts.ClientZkReceiptOperations;
import com.servalabs.chat.BuildConfig;
import com.servalabs.chat.components.TypingStatusRepository;
import com.servalabs.chat.components.TypingStatusSender;
import com.servalabs.chat.crypto.ReentrantSessionLock;
import com.servalabs.chat.crypto.storage.SignalBaseIdentityKeyStore;
import com.servalabs.chat.crypto.storage.SignalIdentityKeyStore;
import com.servalabs.chat.crypto.storage.SignalKyberPreKeyStore;
import com.servalabs.chat.crypto.storage.SignalSenderKeyStore;
import com.servalabs.chat.crypto.storage.SignalServiceAccountDataStoreImpl;
import com.servalabs.chat.crypto.storage.SignalServiceDataStoreImpl;
import com.servalabs.chat.crypto.storage.TextSecurePreKeyStore;
import com.servalabs.chat.crypto.storage.TextSecureSessionStore;
import com.servalabs.chat.database.DatabaseObserver;
import com.servalabs.chat.database.JobDatabase;
import com.servalabs.chat.database.PendingRetryReceiptCache;
import com.servalabs.chat.jobmanager.JobManager;
import com.servalabs.chat.jobmanager.JobMigrator;
import com.servalabs.chat.jobmanager.impl.FactoryJobPredicate;
import com.servalabs.chat.jobs.AttachmentCompressionJob;
import com.servalabs.chat.jobs.AttachmentUploadJob;
import com.servalabs.chat.jobs.FastJobStorage;
import com.servalabs.chat.jobs.GroupCallUpdateSendJob;
import com.servalabs.chat.jobs.IndividualSendJob;
import com.servalabs.chat.jobs.JobManagerFactories;
import com.servalabs.chat.jobs.MarkerJob;
import com.servalabs.chat.jobs.PreKeysSyncJob;
import com.servalabs.chat.jobs.PushGroupSendJob;
import com.servalabs.chat.jobs.PushProcessMessageJob;
import com.servalabs.chat.jobs.ReactionSendJob;
import com.servalabs.chat.jobs.SendDeliveryReceiptJob;
import com.servalabs.chat.jobs.TypingSendJob;
import com.servalabs.chat.keyvalue.SignalStore;
import com.servalabs.chat.megaphone.MegaphoneRepository;
import com.servalabs.chat.messages.IncomingMessageObserver;
import com.servalabs.chat.net.DeviceTransferBlockingInterceptor;
import com.servalabs.chat.net.NetworkManager;
import com.servalabs.chat.net.SignalWebSocketHealthMonitor;
import com.servalabs.chat.net.StandardUserAgentInterceptor;
import com.servalabs.chat.notifications.MessageNotifier;
import com.servalabs.chat.notifications.OptimizedMessageNotifier;
import com.servalabs.chat.push.SecurityEventListener;
import com.servalabs.chat.push.SignalServiceNetworkAccess;
import com.servalabs.chat.recipients.LiveRecipientCache;
import com.servalabs.chat.revealable.ViewOnceMessageManager;
import com.servalabs.chat.service.DeletedCallEventManager;
import com.servalabs.chat.service.ExpiringArchivedStoriesManager;
import com.servalabs.chat.service.ExpiringMessageManager;
import com.servalabs.chat.service.ExpiringStoriesManager;
import com.servalabs.chat.service.PendingRetryReceiptManager;
import com.servalabs.chat.service.PinnedMessageManager;
import com.servalabs.chat.service.ScheduledMessageManager;
import com.servalabs.chat.service.TrimThreadsByDateManager;
import com.servalabs.chat.service.webrtc.SignalCallManager;
import com.servalabs.chat.stories.Stories;
import com.servalabs.chat.util.AlarmSleepTimer;
import com.servalabs.chat.util.AppForegroundObserver;
import com.servalabs.chat.util.ByteUnit;
import com.servalabs.chat.util.EarlyMessageCache;
import com.servalabs.chat.util.Environment;
import com.servalabs.chat.util.FrameRateTracker;
import com.servalabs.chat.util.RemoteConfig;
import com.servalabs.chat.util.TextSecurePreferences;
import com.servalabs.chat.video.exo.GiphyMp4Cache;
import com.servalabs.chat.video.exo.SimpleExoPlayerPool;
import com.servalabs.chat.webrtc.audio.AudioManagerCompat;
import org.signal.libsignal.api.SignalServiceAccountManager;
import org.signal.libsignal.api.SignalServiceDataStore;
import org.signal.libsignal.api.SignalServiceMessageReceiver;
import org.signal.libsignal.api.SignalServiceMessageSender;
import org.signal.libsignal.api.account.AccountApi;
import org.signal.libsignal.api.archive.ArchiveApi;
import org.signal.libsignal.api.attachment.AttachmentApi;
import org.signal.libsignal.api.calling.CallingApi;
import org.signal.libsignal.api.cds.CdsApi;
import org.signal.libsignal.api.certificate.CertificateApi;
import org.signal.libsignal.api.donations.DonationsApi;
import org.signal.libsignal.api.groupsv2.ClientZkOperations;
import org.signal.libsignal.api.groupsv2.GroupsV2Operations;
import org.signal.libsignal.api.keys.KeysApi;
import org.signal.libsignal.api.link.LinkDeviceApi;
import org.signal.libsignal.api.message.MessageApi;
import org.signal.libsignal.api.payments.PaymentsApi;
import org.signal.libsignal.api.profiles.ProfileApi;
import org.signal.libsignal.api.provisioning.ProvisioningApi;
import com.servalabs.chat.core.models.ServiceId.ACI;
import com.servalabs.chat.core.models.ServiceId.PNI;
import org.signal.libsignal.api.ratelimit.RateLimitChallengeApi;
import org.signal.libsignal.api.registration.RegistrationApi;
import org.signal.libsignal.api.remoteconfig.RemoteConfigApi;
import org.signal.libsignal.api.services.DonationsService;
import org.signal.libsignal.api.services.ProfileService;
import org.signal.libsignal.api.storage.StorageServiceApi;
import org.signal.libsignal.api.svr.SvrBApi;
import org.signal.libsignal.api.username.UsernameApi;
import org.signal.libsignal.api.util.CredentialsProvider;
import org.signal.libsignal.api.util.SleepTimer;
import org.signal.libsignal.api.util.UptimeSleepTimer;
import org.signal.libsignal.api.websocket.SignalWebSocket;
import org.signal.libsignal.api.websocket.WebSocketFactory;
import org.signal.libsignal.api.websocket.WebSocketUnavailableException;
import org.signal.libsignal.internal.configuration.SignalServiceConfiguration;
import com.servalabs.chat.libsignal.internal.push.PushServiceSocket;
import com.servalabs.chat.libsignal.internal.websocket.LibSignalChatConnection;
import com.servalabs.chat.libsignal.internal.websocket.LibSignalNetworkExtensions;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Implementation of {@link AppDependencies.Provider} that provides real app dependencies.
 */
public class ApplicationDependencyProvider implements AppDependencies.Provider {

  private final Application context;

  public ApplicationDependencyProvider(@NonNull Application context) {
    this.context = context;
  }

  private @NonNull ClientZkOperations provideClientZkOperations(@NonNull SignalServiceConfiguration signalServiceConfiguration) {
    return ClientZkOperations.create(signalServiceConfiguration);
  }

  @Override
  public @NonNull PushServiceSocket providePushServiceSocket(@NonNull SignalServiceConfiguration signalServiceConfiguration, @NonNull GroupsV2Operations groupsV2Operations) {
    return new PushServiceSocket(signalServiceConfiguration,
                                 new DynamicCredentialsProvider(),
                                 BuildConfig.SIGNAL_AGENT,
                                 RemoteConfig.okHttpAutomaticRetry());
  }

  @Override
  public @NonNull GroupsV2Operations provideGroupsV2Operations(@NonNull SignalServiceConfiguration signalServiceConfiguration) {
    return new GroupsV2Operations(provideClientZkOperations(signalServiceConfiguration), RemoteConfig.groupLimits().getHardLimit());
  }

  @Override
  public @NonNull SignalServiceAccountManager provideSignalServiceAccountManager(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull AccountApi accountApi, @NonNull PushServiceSocket pushServiceSocket, @NonNull GroupsV2Operations groupsV2Operations) {
    return new SignalServiceAccountManager(authWebSocket, accountApi, pushServiceSocket, groupsV2Operations);
  }

  @Override
  public @NonNull SignalServiceMessageSender provideSignalServiceMessageSender(@NonNull SignalServiceDataStore protocolStore,
                                                                               @NonNull PushServiceSocket pushServiceSocket,
                                                                               @NonNull AttachmentApi attachmentApi,
                                                                               @NonNull MessageApi messageApi,
                                                                               @NonNull KeysApi keysApi) {
      return new SignalServiceMessageSender(pushServiceSocket,
                                            protocolStore,
                                            ReentrantSessionLock.INSTANCE,
                                            attachmentApi,
                                            messageApi,
                                            keysApi,
                                            Optional.of(new SecurityEventListener(context)),
                                            SignalExecutors.newCachedBoundedExecutor("signal-messages", ThreadUtil.PRIORITY_IMPORTANT_BACKGROUND_THREAD, 1, 16, 30),
                                            RemoteConfig.maxEnvelopeSizeBytes(),
                                            RemoteConfig.maxIncrementalMacsPerEnvelope(),
                                            RemoteConfig::useMessageSendRestFallback,
                                            RemoteConfig.useBinaryId(),
                                            BuildConfig.USE_STRING_ID);
  }

  @Override
  public @NonNull SignalServiceMessageReceiver provideSignalServiceMessageReceiver(@NonNull PushServiceSocket pushServiceSocket) {
    return new SignalServiceMessageReceiver(pushServiceSocket);
  }

  @Override
  public @NonNull SignalServiceNetworkAccess provideSignalServiceNetworkAccess() {
    return new SignalServiceNetworkAccess(context);
  }

  @Override
  public @NonNull NetworkManager provideNetworkManager() {
    return NetworkManager.create(context);
  }

  @Override
  public @NonNull LiveRecipientCache provideRecipientCache() {
    return new LiveRecipientCache(context);
  }

  @Override
  public @NonNull JobManager provideJobManager() {
    JobManager.Configuration config = new JobManager.Configuration.Builder()
                                                                  .setJobFactories(JobManagerFactories.getJobFactories(context))
                                                                  .setConstraintFactories(JobManagerFactories.getConstraintFactories(context))
                                                                  .setConstraintObservers(JobManagerFactories.getConstraintObservers(context))
                                                                  .setJobStorage(new FastJobStorage(JobDatabase.getInstance(context)))
                                                                  .setJobMigrator(new JobMigrator(TextSecurePreferences.getJobManagerVersion(context), JobManager.CURRENT_VERSION, JobManagerFactories.getJobMigrations(context)))
                                                                  .addReservedJobRunner(new FactoryJobPredicate(PushProcessMessageJob.KEY, MarkerJob.KEY))
                                                                  .addReservedJobRunner(new FactoryJobPredicate(AttachmentUploadJob.KEY, AttachmentCompressionJob.KEY))
                                                                  .addReservedJobRunner(new FactoryJobPredicate(
                                                                      IndividualSendJob.KEY,
                                                                      PushGroupSendJob.KEY,
                                                                      ReactionSendJob.KEY,
                                                                      TypingSendJob.KEY,
                                                                      GroupCallUpdateSendJob.KEY,
                                                                      SendDeliveryReceiptJob.KEY
                                                                  ))
                                                                  .build();
    return new JobManager(context, config);
  }

  @Override
  public @NonNull FrameRateTracker provideFrameRateTracker() {
    return new FrameRateTracker(context);
  }

  @SuppressLint("DiscouragedApi")
  public @NonNull MegaphoneRepository provideMegaphoneRepository() {
    return new MegaphoneRepository(context);
  }

  @Override
  public @NonNull EarlyMessageCache provideEarlyMessageCache() {
    return new EarlyMessageCache();
  }

  @Override
  public @NonNull MessageNotifier provideMessageNotifier() {
    return new OptimizedMessageNotifier(context);
  }

  @Override
  public @NonNull IncomingMessageObserver provideIncomingMessageObserver(@NonNull SignalWebSocket.AuthenticatedWebSocket webSocket, @NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket) {
    return new IncomingMessageObserver(context, webSocket, unauthWebSocket);
  }

  @Override
  public @NonNull TrimThreadsByDateManager provideTrimThreadsByDateManager() {
    return new TrimThreadsByDateManager(context);
  }

  @Override
  public @NonNull ViewOnceMessageManager provideViewOnceMessageManager() {
    return new ViewOnceMessageManager(context);
  }

  @Override
  public @NonNull ExpiringStoriesManager provideExpiringStoriesManager() {
    return new ExpiringStoriesManager(context);
  }

  @Override
  public @NonNull ExpiringArchivedStoriesManager provideExpiringArchivedStoriesManager() {
    return new ExpiringArchivedStoriesManager(context);
  }

  @Override
  public @NonNull ExpiringMessageManager provideExpiringMessageManager() {
    return new ExpiringMessageManager(context);
  }

  @Override
  public @NonNull DeletedCallEventManager provideDeletedCallEventManager() {
    return new DeletedCallEventManager(context);
  }

  @Override
  public @NonNull ScheduledMessageManager provideScheduledMessageManager() {
    return new ScheduledMessageManager(context);
  }

  @Override
  public @NonNull PinnedMessageManager providePinnedMessageManager() {
    return new PinnedMessageManager(context);
  }

  @Override
  public @NonNull Network provideLibsignalNetwork(@NonNull SignalServiceConfiguration config) {
    Network network = new Network(BuildConfig.LIBSIGNAL_NET_ENV, StandardUserAgentInterceptor.USER_AGENT);
    LibSignalNetworkExtensions.applyConfiguration(network, config);
    network.setRemoteConfig(RemoteConfig.getLibsignalConfigs());

    return network;
  }

  @Override
  public @NonNull TypingStatusRepository provideTypingStatusRepository() {
    return new TypingStatusRepository();
  }

  @Override
  public @NonNull TypingStatusSender provideTypingStatusSender() {
    return new TypingStatusSender();
  }

  @Override
  public @NonNull DatabaseObserver provideDatabaseObserver() {
    return new DatabaseObserver();
  }

  @Override
  public @NonNull SignalCallManager provideSignalCallManager() {
    return new SignalCallManager(context);
  }

  @Override
  public @NonNull PendingRetryReceiptManager providePendingRetryReceiptManager() {
    return new PendingRetryReceiptManager(context);
  }

  @Override
  public @NonNull PendingRetryReceiptCache providePendingRetryReceiptCache() {
    return new PendingRetryReceiptCache();
  }

  @Override
  public @NonNull SignalWebSocket.AuthenticatedWebSocket provideAuthWebSocket(@NonNull Supplier<SignalServiceConfiguration> signalServiceConfigurationSupplier, @NonNull Supplier<Network> libSignalNetworkSupplier) {
    SleepTimer                   sleepTimer    = !SignalStore.account().isPushAvailable() || SignalStore.internal().isWebsocketModeForced() ? new AlarmSleepTimer(context) : new UptimeSleepTimer();
    SignalWebSocketHealthMonitor healthMonitor = new SignalWebSocketHealthMonitor(sleepTimer);

    WebSocketFactory authFactory = () -> {
      DynamicCredentialsProvider credentialsProvider = new DynamicCredentialsProvider();

      if (credentialsProvider.isInvalid()) {
        throw new WebSocketUnavailableException("Invalid auth credentials");
      }

      Network network = libSignalNetworkSupplier.get();
      return new LibSignalChatConnection("libsignal-auth",
                                         network,
                                         credentialsProvider,
                                         Stories.isFeatureEnabled(),
                                         healthMonitor);
    };

    SignalWebSocket.AuthenticatedWebSocket webSocket = new SignalWebSocket.AuthenticatedWebSocket(authFactory,
                                                                                                  () -> !SignalStore.misc().isClientDeprecated() && !DeviceTransferBlockingInterceptor.getInstance().isBlockingNetwork() && !Environment.IS_INSTRUMENTATION,
                                                                                                  sleepTimer,
                                                                                                  TimeUnit.SECONDS.toMillis(30));
    if (AppForegroundObserver.isForegrounded()) {
      webSocket.registerKeepAliveToken(SignalWebSocket.FOREGROUND_KEEPALIVE);
    }

    healthMonitor.monitor(webSocket);

    return webSocket;
  }

  @Override
  public @NonNull SignalWebSocket.UnauthenticatedWebSocket provideUnauthWebSocket(@NonNull Supplier<SignalServiceConfiguration> signalServiceConfigurationSupplier, @NonNull Supplier<Network> libSignalNetworkSupplier) {
    SleepTimer                   sleepTimer    = !SignalStore.account().isPushAvailable() || SignalStore.internal().isWebsocketModeForced() ? new AlarmSleepTimer(context) : new UptimeSleepTimer();
    SignalWebSocketHealthMonitor healthMonitor = new SignalWebSocketHealthMonitor(sleepTimer);

    WebSocketFactory unauthFactory = () -> {
      Network network = libSignalNetworkSupplier.get();
      return new LibSignalChatConnection("libsignal-unauth",
                                         network,
                                         null,
                                         Stories.isFeatureEnabled(),
                                         healthMonitor);
    };

    SignalWebSocket.UnauthenticatedWebSocket webSocket = new SignalWebSocket.UnauthenticatedWebSocket(unauthFactory,
                                                                                                      () -> !SignalStore.misc().isClientDeprecated() && !DeviceTransferBlockingInterceptor.getInstance().isBlockingNetwork() && !Environment.IS_INSTRUMENTATION,
                                                                                                      sleepTimer,
                                                                                                      TimeUnit.SECONDS.toMillis(30));
    if (AppForegroundObserver.isForegrounded()) {
      webSocket.registerKeepAliveToken(SignalWebSocket.FOREGROUND_KEEPALIVE);
    }

    healthMonitor.monitor(webSocket);
    return webSocket;
  }

  @Override
  public @NonNull SignalServiceDataStoreImpl provideProtocolStore() {
    ACI localAci = SignalStore.account().getAci();
    PNI localPni = SignalStore.account().getPni();

    if (localAci == null) {
      throw new IllegalStateException("No ACI set!");
    }

    if (localPni == null) {
      throw new IllegalStateException("No PNI set!");
    }

    boolean needsPreKeyJob = false;

    if (!SignalStore.account().hasAciIdentityKey()) {
      SignalStore.account().generateAciIdentityKeyIfNecessary();
      needsPreKeyJob = true;
    }

    if (!SignalStore.account().hasPniIdentityKey()) {
      SignalStore.account().generatePniIdentityKeyIfNecessary();
      needsPreKeyJob = true;
    }

    if (needsPreKeyJob) {
      PreKeysSyncJob.enqueueIfNeeded();
    }

    SignalBaseIdentityKeyStore baseIdentityStore = new SignalBaseIdentityKeyStore(context);

    SignalServiceAccountDataStoreImpl aciStore = new SignalServiceAccountDataStoreImpl(context,
                                                                                       new TextSecurePreKeyStore(localAci),
                                                                                       new SignalKyberPreKeyStore(localAci),
                                                                                       new SignalIdentityKeyStore(baseIdentityStore, () -> SignalStore.account().getAciIdentityKey()),
                                                                                       new TextSecureSessionStore(localAci),
                                                                                       new SignalSenderKeyStore(context));

    SignalServiceAccountDataStoreImpl pniStore = new SignalServiceAccountDataStoreImpl(context,
                                                                                       new TextSecurePreKeyStore(localPni),
                                                                                       new SignalKyberPreKeyStore(localPni),
                                                                                       new SignalIdentityKeyStore(baseIdentityStore, () -> SignalStore.account().getPniIdentityKey()),
                                                                                       new TextSecureSessionStore(localPni),
                                                                                       new SignalSenderKeyStore(context));
    return new SignalServiceDataStoreImpl(context, aciStore, pniStore);
  }

  @Override
  public @NonNull GiphyMp4Cache provideGiphyMp4Cache() {
    return new GiphyMp4Cache(ByteUnit.MEGABYTES.toBytes(16));
  }

  @Override
  public @NonNull SimpleExoPlayerPool provideExoPlayerPool() {
    return new SimpleExoPlayerPool(context);
  }

  @Override
  public @NonNull AudioManagerCompat provideAndroidCallAudioManager() {
    return AudioManagerCompat.create(context);
  }

  @Override
  public @NonNull DonationsService provideDonationsService(@NonNull DonationsApi donationsApi) {
    return new DonationsService(donationsApi);
  }

  @Override
  public @NonNull ProfileService provideProfileService(@NonNull ClientZkProfileOperations clientZkProfileOperations,
                                                       @NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket,
                                                       @NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket)
  {
    return new ProfileService(clientZkProfileOperations, authWebSocket, unauthWebSocket);
  }

  @Override
  public @NonNull DeadlockDetector provideDeadlockDetector() {
    HandlerThread handlerThread = new HandlerThread("signal-DeadlockDetector", ThreadUtil.PRIORITY_BACKGROUND_THREAD);
    handlerThread.start();
    return new DeadlockDetector(new Handler(handlerThread.getLooper()), TimeUnit.SECONDS.toMillis(5));
  }

  @Override
  public @NonNull ClientZkReceiptOperations provideClientZkReceiptOperations(@NonNull SignalServiceConfiguration signalServiceConfiguration) {
    return provideClientZkOperations(signalServiceConfiguration).getReceiptOperations();
  }

  @Override
  public @NonNull BillingApi provideBillingApi() {
    return BillingFactory.create(GooglePlayBillingDependencies.INSTANCE, Environment.Backups.supportsGooglePlayBilling());
  }

  @Override
  public @NonNull ArchiveApi provideArchiveApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket, @NonNull PushServiceSocket pushServiceSocket) {
    return new ArchiveApi(authWebSocket, unauthWebSocket, pushServiceSocket);
  }

  @Override
  public @NonNull KeysApi provideKeysApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket) {
    return new KeysApi(authWebSocket, unauthWebSocket);
  }

  @Override
  public @NonNull AttachmentApi provideAttachmentApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull PushServiceSocket pushServiceSocket) {
    return new AttachmentApi(authWebSocket, pushServiceSocket);
  }

  @Override
  public @NonNull LinkDeviceApi provideLinkDeviceApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket) {
    return new LinkDeviceApi(authWebSocket);
  }

  @Override
  public @NonNull RegistrationApi provideRegistrationApi(@NonNull PushServiceSocket pushServiceSocket) {
    return new RegistrationApi(pushServiceSocket);
  }

  @Override
  public @NonNull StorageServiceApi provideStorageServiceApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull PushServiceSocket pushServiceSocket) {
    return new StorageServiceApi(authWebSocket, pushServiceSocket);
  }

  @Override
  public @NonNull AccountApi provideAccountApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket) {
    return new AccountApi(authWebSocket);
  }

  @Override
  public @NonNull UsernameApi provideUsernameApi(@NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket) {
    return new UsernameApi(unauthWebSocket);
  }

  @Override
  public @NonNull CallingApi provideCallingApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket, @NonNull PushServiceSocket pushServiceSocket) {
    return new CallingApi(authWebSocket, unauthWebSocket, pushServiceSocket);
  }

  @Override
  public @NonNull PaymentsApi providePaymentsApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket) {
    return new PaymentsApi(authWebSocket);
  }

  @Override
  public @NonNull CdsApi provideCdsApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket) {
    return new CdsApi(authWebSocket);
  }

  @Override
  public @NonNull RateLimitChallengeApi provideRateLimitChallengeApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket) {
    return new RateLimitChallengeApi(authWebSocket);
  }

  @Override
  public @NonNull MessageApi provideMessageApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket) {
    return new MessageApi(authWebSocket, unauthWebSocket);
  }

  @Override
  public @NonNull ProvisioningApi provideProvisioningApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket) {
    return new ProvisioningApi(authWebSocket, unauthWebSocket);
  }

  @Override
  public @NonNull CertificateApi provideCertificateApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket) {
    return new CertificateApi(authWebSocket);
  }

  @Override
  public @NonNull ProfileApi provideProfileApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket, @NonNull PushServiceSocket pushServiceSocket, @NonNull ClientZkProfileOperations clientZkProfileOperations) {
    return new ProfileApi(authWebSocket, unauthWebSocket, pushServiceSocket, clientZkProfileOperations);
  }

  @Override
  public @NonNull RemoteConfigApi provideRemoteConfigApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull PushServiceSocket pushServiceSocket) {
    return new RemoteConfigApi(authWebSocket, pushServiceSocket);
  }

  @Override
  public @NonNull DonationsApi provideDonationsApi(@NonNull SignalWebSocket.AuthenticatedWebSocket authWebSocket, @NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket) {
    return new DonationsApi(authWebSocket, unauthWebSocket);
  }

  @Override
  public @NonNull SvrBApi provideSvrBApi(@NotNull Network libSignalNetwork) {
    return new SvrBApi(libSignalNetwork);
  }

  @Override
  public @NonNull KeyTransparencyApi provideKeyTransparencyApi(@NonNull SignalWebSocket.UnauthenticatedWebSocket unauthWebSocket) {
    return new KeyTransparencyApi(unauthWebSocket);
  }

  @VisibleForTesting
  static class DynamicCredentialsProvider implements CredentialsProvider {

    @Override
    public ACI getAci() {
      return SignalStore.account().getAci();
    }

    @Override
    public PNI getPni() {
      return SignalStore.account().getPni();
    }

    @Override
    public String getE164() {
      return SignalStore.account().getE164();
    }

    @Override
    public String getPassword() {
      return SignalStore.account().getServicePassword();
    }

    @Override
    public int getDeviceId() {
      return SignalStore.account().getDeviceId();
    }
  }
}
