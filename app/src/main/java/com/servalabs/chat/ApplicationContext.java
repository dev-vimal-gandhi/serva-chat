/*
 * Copyright (C) 2013 Open Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.servalabs.chat;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import net.zetetic.database.Logger;

import org.conscrypt.Conscrypt;
import org.greenrobot.eventbus.EventBus;
import org.signal.aesgcmprovider.AesGcmProvider;
import com.servalabs.chat.core.util.DiskUtil;
import com.servalabs.chat.core.util.MemoryTracker;
import com.servalabs.chat.core.util.ThreadUtil;
import com.servalabs.chat.core.util.concurrent.SignalExecutors;
import com.servalabs.chat.core.util.logging.AndroidLogger;
import com.servalabs.chat.core.util.logging.Log;
import com.servalabs.chat.core.util.logging.Scrubber;
import com.servalabs.chat.glide.SignalGlideCodecs;
import org.signal.libsignal.net.ChatServiceException;
import org.signal.libsignal.protocol.logging.SignalProtocolLoggerProvider;
import org.signal.ringrtc.CallManager;
import com.servalabs.chat.apkupdate.ApkUpdateRefreshListener;
import com.servalabs.chat.avatar.AvatarPickerStorage;
import com.servalabs.chat.backup.v2.BackupRepository;
import com.servalabs.chat.crypto.AttachmentSecretProvider;
import com.servalabs.chat.crypto.DatabaseSecretProvider;
import com.servalabs.chat.crypto.InvalidPassphraseException;
import com.servalabs.chat.crypto.MasterSecretUtil;
import com.servalabs.chat.crypto.UnrecoverableKeyException;
import com.servalabs.chat.database.LogDatabase;
import com.servalabs.chat.database.SignalDatabase;
import com.servalabs.chat.database.SqlCipherLibraryLoader;
import com.servalabs.chat.dependencies.AppDependencies;
import com.servalabs.chat.dependencies.ApplicationDependencyProvider;
import com.servalabs.chat.emoji.EmojiSource;
import com.servalabs.chat.emoji.JumboEmoji;
import com.servalabs.chat.gcm.FcmFetchManager;
import com.servalabs.chat.glide.SignalGlideComponents;
import com.servalabs.chat.jobs.AccountConsistencyWorkerJob;
import com.servalabs.chat.jobs.BackupRefreshJob;
import com.servalabs.chat.jobs.BackupSubscriptionCheckJob;
import com.servalabs.chat.jobs.BuildExpirationConfirmationJob;
import com.servalabs.chat.jobs.CallingAssetsDownloadJob;
import com.servalabs.chat.jobs.CheckKeyTransparencyJob;
import com.servalabs.chat.jobs.CheckServiceReachabilityJob;
import com.servalabs.chat.jobs.DownloadLatestEmojiDataJob;
import com.servalabs.chat.jobs.EmojiSearchIndexDownloadJob;
import com.servalabs.chat.jobs.FcmRefreshJob;
import com.servalabs.chat.jobs.FontDownloaderJob;
import com.servalabs.chat.jobs.GroupRingCleanupJob;
import com.servalabs.chat.jobs.GroupV2UpdateSelfProfileKeyJob;
import com.servalabs.chat.jobs.LinkedDeviceInactiveCheckJob;
import com.servalabs.chat.jobs.MultiDeviceContactUpdateJob;
import com.servalabs.chat.jobs.PreKeysSyncJob;
import com.servalabs.chat.jobs.ProfileUploadJob;
import com.servalabs.chat.jobs.RefreshAttributesJob;
import com.servalabs.chat.jobs.RefreshSvrCredentialsJob;
import com.servalabs.chat.jobs.RestoreOptimizedMediaJob;
import com.servalabs.chat.jobs.RetrieveProfileJob;
import com.servalabs.chat.jobs.RetrieveRemoteAnnouncementsJob;
import com.servalabs.chat.jobmanager.impl.SealedSenderConstraint;
import com.servalabs.chat.jobs.StoryOnboardingDownloadJob;
import com.servalabs.chat.jobs.UnifiedPushRefreshJob;
import com.servalabs.chat.keyvalue.KeepMessagesDuration;
import com.servalabs.chat.keyvalue.SettingsValues.NotificationDeliveryMethod;
import com.servalabs.chat.keyvalue.SignalStore;
import com.servalabs.chat.logging.CustomSignalProtocolLogger;
import com.servalabs.chat.logging.PersistentLogger;
import com.servalabs.chat.messageprocessingalarm.RoutineMessageFetchReceiver;
import com.servalabs.chat.migrations.ApplicationMigrations;
import com.servalabs.chat.mms.SignalGlideModule;
import com.servalabs.chat.net.NetworkManager;
import com.servalabs.chat.notifications.MessageNotifier;
import com.servalabs.chat.osm.SingleSessionDiskTileWriter;
import com.servalabs.chat.providers.BlobProvider;
import com.servalabs.chat.ratelimit.RateLimitUtil;
import com.servalabs.chat.recipients.Recipient;
import com.servalabs.chat.registration.util.RegistrationUtil;
import org.signal.ringrtc.RingRtcLogger;
import com.servalabs.chat.service.AnalyzeDatabaseAlarmListener;
import com.servalabs.chat.service.DirectoryRefreshListener;
import com.servalabs.chat.service.KeyCachingService;
import com.servalabs.chat.service.LocalBackupListener;
import com.servalabs.chat.service.MessageBackupListener;
import com.servalabs.chat.service.RotateSenderCertificateListener;
import com.servalabs.chat.service.RotateSignedPreKeyListener;
import com.servalabs.chat.service.WipeMemoryService;
import com.servalabs.chat.service.webrtc.ActiveCallManager;
import com.servalabs.chat.service.webrtc.CallingAssets;
import com.servalabs.chat.service.webrtc.AndroidTelecomUtil;
import com.servalabs.chat.storage.StorageSyncHelper;
import com.servalabs.chat.util.AppForegroundObserver;
import com.servalabs.chat.util.AppStartup;
import com.servalabs.chat.util.DeviceProperties;
import com.servalabs.chat.util.DynamicTheme;
import com.servalabs.chat.util.Environment;
import com.servalabs.chat.util.RemoteConfig;
import com.servalabs.chat.util.FileUtils;
import com.servalabs.chat.util.SignalLocalMetrics;
import com.servalabs.chat.util.SignalUncaughtExceptionHandler;
import com.servalabs.chat.util.SqlCipherLogTarget;
import com.servalabs.chat.util.TextSecurePreferences;
import com.servalabs.chat.core.util.Util;
import com.servalabs.chat.util.dynamiclanguage.DynamicLanguageContextWrapper;
import com.servalabs.chat.libsignal.api.websocket.SignalWebSocket;

import java.io.InterruptedIOException;
import java.net.SocketException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.servalabs.chat.base.ApkInfo;
import com.servalabs.chat.base.ApplicationInstance;
import com.servalabs.chat.unifiedpush.UnifiedPushDistributor;
import io.reactivex.rxjava3.exceptions.OnErrorNotImplementedException;
import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import rxdogtag2.RxDogTag;

/**
 * Will be called once when the TextSecure process is created.
 * <p>
 * We're using this as an insertion point to patch up the Android PRNG disaster,
 * to initialize the job manager, and to check for GCM registration freshness.
 *
 * @author Moxie Marlinspike
 */
public class ApplicationContext extends Application implements AppForegroundObserver.Listener {

  private static final String TAG = Log.tag(ApplicationContext.class);

  public static ApplicationContext getInstance(Context context) {
    return (ApplicationContext) context.getApplicationContext();
  }

  public ApplicationContext() {
    super();
    ApplicationInstance.set(this);
  }

  private volatile boolean isAppInitialized;

  @Override
  public void onCreate() {
    initializeLogging(true);
    Log.i(TAG, "onCreate()");

    super.onCreate();

    SqlCipherLibraryLoader.load();
    EventBus.builder().logNoSubscriberMessages(false).installDefaultEventBus();
    DynamicTheme.setDefaultDayNightMode(this);
    ScreenLockController.enableAutoLock(TextSecurePreferences.isBiometricScreenLockEnabled(this));
    AppDependencies.installDependencyProviders();

    initializePassphraseLock();
    cleanCacheDir();
  }

  private void onCreateUnlock() {
    AppStartup.getInstance().onApplicationCreate();
    SignalLocalMetrics.ColdStart.start();

    long startTime = System.currentTimeMillis();

    AppStartup.getInstance().addBlocking("sqlcipher-init", () -> {
                SignalDatabase.init(this,
                                    DatabaseSecretProvider.getOrCreateDatabaseSecret(this),
                                    AttachmentSecretProvider.getInstance(this).getOrCreateAttachmentSecret());
                Logger.setTarget(SqlCipherLogTarget.INSTANCE);
              })
              .addBlocking("signal-store", () -> SignalStore.init(this))
              .addBlocking("logging", () -> {
                initializeLogging(false);
                Log.i(TAG, "onCreateUnlock()");
              })
              .addBlocking("app-dependencies", this::initializeAppDependencies)
              .addBlocking("security-provider", this::initializeSecurityProvider)
              .addBlocking("crash-handling", this::initializeCrashHandling)
              .addBlocking("rx-init", this::initializeRx)
              .addBlocking("scrubber", () -> Scrubber.setIdentifierHmacKeyProvider(() -> SignalStore.svr().getMasterKey().deriveLoggingKey()))
              .addBlocking("network-settings", this::initializeNetworkSettings)
              .addBlocking("first-launch", this::initializeFirstEverAppLaunch)
              .addBlocking("app-migrations", this::initializeApplicationMigrations)
              .addBlocking("lifecycle-observer", () -> AppForegroundObserver.addListener(this))
              .addBlocking("push", this::updatePushNotificationServices)
              .addBlocking("message-retriever", this::initializeMessageRetrieval)
              .addBlocking("blob-provider", this::initializeBlobProvider)
              .addBlocking("remote-config", RemoteConfig::init)
              .addBlocking("ring-rtc", this::initializeRingRtc)
              .addBlocking("glide", () -> SignalGlideModule.setRegisterGlideComponents(new SignalGlideComponents()))
              .addNonBlocking(() -> RegistrationUtil.maybeMarkRegistrationComplete())
              .addNonBlocking(() -> Glide.get(this))
              .addNonBlocking(this::cleanAvatarStorage)
              .addNonBlocking(this::initializeRevealableMessageManager)
              .addNonBlocking(this::initializePendingRetryReceiptManager)
              .addNonBlocking(this::initializeScheduledMessageManager)
              .addNonBlocking(PreKeysSyncJob::enqueueIfNeeded)
              .addNonBlocking(this::initializePeriodicTasks)
              .addNonBlocking(this::initializeCleanup)
              .addNonBlocking(this::initializeGlideCodecs)
              .addNonBlocking(SealedSenderConstraint::checkAndSetValidity)
              .addNonBlocking(StorageSyncHelper::scheduleRoutineSync)
              .addNonBlocking(this::beginJobLoop)
              .addNonBlocking(EmojiSource::refresh)
              .addNonBlocking(() -> AppDependencies.getGiphyMp4Cache().onAppStart(this))
              .addNonBlocking(AppDependencies::getBillingApi)
              .addNonBlocking(this::ensureProfileUploaded)
              .addNonBlocking(() -> AppDependencies.getExpireStoriesManager().scheduleIfNecessary())
              .addNonBlocking(BackupRepository::maybeFixAnyDanglingUploadProgress)
              .addPostRender(() -> AppDependencies.getDeletedCallEventManager().scheduleIfNecessary())
              .addPostRender(() -> RateLimitUtil.retryAllRateLimitedMessages(this))
              .addPostRender(this::initializeExpiringMessageManager)
              .addPostRender(this::initializeTrimThreadsByDateManager)
              .addPostRender(RefreshSvrCredentialsJob::enqueueIfNecessary)
              .addPostRender(() -> DownloadLatestEmojiDataJob.scheduleIfNecessary(this))
              .addPostRender(EmojiSearchIndexDownloadJob::scheduleIfNecessary)
              .addPostRender(() -> SignalDatabase.messageLog().trimOldMessages(System.currentTimeMillis(), RemoteConfig.retryRespondMaxAge()))
              .addPostRender(() -> JumboEmoji.updateCurrentVersion(this))
              .addPostRender(RetrieveRemoteAnnouncementsJob::enqueue)
              .addPostRender(AndroidTelecomUtil::registerPhoneAccount)
              .addPostRender(() -> AppDependencies.getJobManager().add(new FontDownloaderJob()))
              .addPostRender(() -> AppDependencies.getJobManager().add(new CallingAssetsDownloadJob()))
              .addPostRender(CheckServiceReachabilityJob::enqueueIfNecessary)
              .addPostRender(GroupV2UpdateSelfProfileKeyJob::enqueueForGroupsIfNecessary)
              .addPostRender(StoryOnboardingDownloadJob.Companion::enqueueIfNeeded)
              .addPostRender(() -> AppDependencies.getExoPlayerPool().getPoolStats().getMaxUnreserved())
              .addPostRender(() -> AppDependencies.getRecipientCache().warmUp())
              .addPostRender(AccountConsistencyWorkerJob::enqueueIfNecessary)
              .addPostRender(GroupRingCleanupJob::enqueue)
              .addPostRender(LinkedDeviceInactiveCheckJob::enqueueIfNecessary)
              .addPostRender(() -> ActiveCallManager.clearNotifications(this))
              .addPostRender(RestoreOptimizedMediaJob::enqueueIfNecessary)
              .addPostRender(() -> AppDependencies.getPinnedMessageManager().scheduleIfNecessary())
              .execute();

    Log.d(TAG, "onCreateUnlock() took " + (System.currentTimeMillis() - startTime) + " ms");
    SignalLocalMetrics.ColdStart.onApplicationCreateFinished();
  }

  @Override
  public void onForeground() {
    Log.i(TAG, "App is now visible. Battery: " + DeviceProperties.getBatteryLevel(this) + "% (charging: " + DeviceProperties.isCharging(this) + ")");

    if (!KeyCachingService.isLocked()) {
      onStartUnlock();
    }
  }

  private void onStartUnlock() {
    long startTime = System.currentTimeMillis();

    AppDependencies.getFrameRateTracker().start();
    AppDependencies.getMegaphoneRepository().onAppForegrounded();
    AppDependencies.getDeadlockDetector().start();
    FcmFetchManager.onForeground(this);

    SignalExecutors.BOUNDED.execute(() -> {
      BackupRefreshJob.enqueueIfNecessary();
      RemoteConfig.refreshIfNecessary();
      RetrieveProfileJob.enqueueRoutineFetchIfNecessary();
      executePendingContactSync();
      checkBuildExpiration();
      checkFreeDiskSpace();
      MemoryTracker.start();
      BackupSubscriptionCheckJob.enqueueIfAble();
      CheckKeyTransparencyJob.enqueueIfNecessary(true);

      long lastForegroundTime = SignalStore.misc().getLastForegroundTime();
      long currentTime        = System.currentTimeMillis();
      long timeDiff           = currentTime - lastForegroundTime;

      if (timeDiff < 0) {
        Log.w(TAG, "Time travel! The system clock has moved backwards. (currentTime: " + currentTime + " ms, lastForegroundTime: " + lastForegroundTime + " ms, diff: " + timeDiff + " ms)", true);
      }

      SignalStore.misc().setLastForegroundTime(currentTime);
    });

    Log.d(TAG, "onStartUnlock() took " + (System.currentTimeMillis() - startTime) + " ms");
  }

  @Override
  public void onBackground() {
    Log.i(TAG, "App is no longer visible.");

    ScreenLockController.onAppBackgrounded(this);
    if (!KeyCachingService.isLocked()) {
      onStopUnlock();
    }
  }

  private void onStopUnlock() {
    AppDependencies.getMessageNotifier().clearVisibleThread();
    AppDependencies.getFrameRateTracker().stop();
    AppDependencies.getDeadlockDetector().stop();
    AppDependencies.getAuthWebSocket().removeKeepAliveToken(SignalWebSocket.FOREGROUND_KEEPALIVE);
    AppDependencies.getUnauthWebSocket().removeKeepAliveToken(SignalWebSocket.FOREGROUND_KEEPALIVE);
    MemoryTracker.stop();
  }

  @MainThread
  public void onUnlock() {
    Log.i(TAG, "onUnlock()");

    if (!isAppInitialized) {
      onCreateUnlock();
      registerKeyEventReceiver();
      onStartUnlock();
      isAppInitialized = true;
    }
  }

  @MainThread
  public void onLock(boolean keyExpired) {
    Log.i(TAG, "onLock()");

    ActiveCallManager.stop();

    finalizeExpiringMessageManager();
    finalizeMessageRetrieval();
    unregisterKeyEventReceiver();

    MessageNotifier messageNotifier = AppDependencies.getMessageNotifier();
    messageNotifier.cancelDelayedNotifications();
    boolean hadActiveNotifications = messageNotifier.clearNotifications(this);

    if (hadActiveNotifications && keyExpired && SignalStore.account().isPushAvailable() &&
        TextSecurePreferences.isPassphraseLockNotificationsEnabled(this) ) {
      Log.d(TAG, "Replacing active notifications with may-have-messages notification");
      FcmFetchManager.postMayHaveMessagesNotification(this);
    }

    ThreadUtil.runOnMainDelayed(() -> {
      AppDependencies.getJobManager().shutdown(TimeUnit.SECONDS.toMillis(10));
      KeyCachingService.clearMasterSecret();
      WipeMemoryService.run(this, true);
    }, TimeUnit.SECONDS.toMillis(1));
  }

  public void checkBuildExpiration() {
    if (Util.getTimeUntilBuildExpiry(SignalStore.misc().getEstimatedServerTime()) <= 0 && !SignalStore.misc().isClientDeprecated()) {
      Log.w(TAG, "Build potentially expired! Enqueing job to check.", true);
      AppDependencies.getJobManager().add(new BuildExpirationConfirmationJob());
    }
  }

  public void checkFreeDiskSpace() {
    long availableBytes = DiskUtil.getAvailableSpace(getApplicationContext()).getBytes();
    SignalStore.backup().setSpaceAvailableOnDiskBytes(availableBytes);
  }

  private void initializeSecurityProvider() {
    int aesPosition = Security.insertProviderAt(new AesGcmProvider(), 1);
    Log.i(TAG, "Installed AesGcmProvider: " + aesPosition);

    if (aesPosition < 0) {
      Log.e(TAG, "Failed to install AesGcmProvider()");
      throw new ProviderInitializationException();
    }

    int conscryptPosition = Security.insertProviderAt(Conscrypt.newProvider(), 2);
    Log.i(TAG, "Installed Conscrypt provider: " + conscryptPosition);

    if (conscryptPosition < 0) {
      Log.w(TAG, "Did not install Conscrypt provider. May already be present.");
    }
  }

  @VisibleForTesting
  protected void initializeLogging(boolean locked) {
    if (locked) {
      Log.initialize(AndroidLogger.INSTANCE);
    } else {
      boolean enableLogging = TextSecurePreferences.isLogEnabled(this);
      boolean alwaysRedact  = !BuildConfig.DEBUG;
      Log.configure(RemoteConfig::internalUser, enableLogging, alwaysRedact, AndroidLogger.INSTANCE, PersistentLogger.getInstance(this));

      SignalProtocolLoggerProvider.setProvider(new CustomSignalProtocolLogger());
      SignalProtocolLoggerProvider.initializeLogging(BuildConfig.LIBSIGNAL_LOG_LEVEL);

      SignalExecutors.UNBOUNDED.execute(() -> {
        Log.blockUntilAllWritesFinished();
        LogDatabase.getInstance(this).logs().trimToSize();
        LogDatabase.getInstance(this).crashes().trimToSize();
      });
    }
  }

  private void initializeCrashHandling() {
    final Thread.UncaughtExceptionHandler originalHandler = Thread.getDefaultUncaughtExceptionHandler();
    Thread.setDefaultUncaughtExceptionHandler(new SignalUncaughtExceptionHandler(originalHandler));
  }

  private void initializeRx() {
    RxDogTag.install();
    RxJavaPlugins.setInitIoSchedulerHandler(schedulerSupplier -> Schedulers.from(SignalExecutors.UNBOUNDED, true, false));
    RxJavaPlugins.setInitComputationSchedulerHandler(schedulerSupplier -> Schedulers.from(SignalExecutors.BOUNDED, true, false));
    RxJavaPlugins.setErrorHandler(e -> {
      boolean wasWrapped = false;
      while ((e instanceof UndeliverableException || e instanceof AssertionError || e instanceof OnErrorNotImplementedException) && e.getCause() != null) {
        wasWrapped = true;
        e = e.getCause();
      }

      if (wasWrapped && (e instanceof SocketException || e instanceof InterruptedException || e instanceof InterruptedIOException || e instanceof ChatServiceException)) {
        return;
      }

      Log.e(TAG, "RxJava error handler invoked", e);

      Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread.currentThread().getUncaughtExceptionHandler();
      if (uncaughtExceptionHandler == null) {
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
      }

      uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), e);
    });
  }

  private void initializeApplicationMigrations() {
    ApplicationMigrations.onApplicationCreate(this, AppDependencies.getJobManager());
  }

  private void initializeMessageRetrieval() {
    SignalExecutors.UNBOUNDED.execute(AppDependencies::startNetwork);
  }

  private void finalizeMessageRetrieval() {
    AppDependencies.resetNetwork(false);
  }

  private void initializePassphraseLock() {
    if (MasterSecretUtil.isPassphraseInitialized(this)) {
      try {
        KeyCachingService.setMasterSecret(MasterSecretUtil.getMasterSecret(this,
                MasterSecretUtil.getUnencryptedPassphrase()));
        TextSecurePreferences.setPassphraseLockEnabled(this, false);
        onUnlock();
      } catch (InvalidPassphraseException | UnrecoverableKeyException e) {
        TextSecurePreferences.setPassphraseLockEnabled(this, true);
      }
    }
  }

  private void cleanCacheDir() {
    SignalExecutors.BOUNDED.execute(
        () -> FileUtils.deleteDirectoryContents(SingleSessionDiskTileWriter.Companion.getTileCacheDir(this))
    );
  }

  @VisibleForTesting
  void initializeAppDependencies() {
    if (!AppDependencies.isInitialized()) {
      Log.i(TAG, "Initializing AppDependencies.");
      AppDependencies.init(new ApplicationDependencyProvider(this));
    }
    AppForegroundObserver.begin();

    if (Environment.USE_NEW_REGISTRATION) {
      initializeRegistrationDependencies();
    }
  }

  private void initializeRegistrationDependencies() {
    com.servalabs.chat.registration.RegistrationDependencies.Companion.provide(
      new com.servalabs.chat.registration.RegistrationDependencies(
        new com.servalabs.chat.registration.v2.AppRegistrationNetworkController(this, AppDependencies.getPushServiceSocket()),
        new com.servalabs.chat.registration.v2.AppRegistrationStorageController(this),
        null
      )
    );
  }

  private void initializeFirstEverAppLaunch() {
    if (TextSecurePreferences.getFirstInstallVersion(this) == -1) {
      Log.i(TAG, "First ever app launch!");
      AppInitialization.onFirstEverAppLaunch(this);

      Log.i(TAG, "Generating new identity keys...");
      SignalStore.account().generateAciIdentityKeyIfNecessary();
      SignalStore.account().generatePniIdentityKeyIfNecessary();

      Log.i(TAG, "Setting first install version to " + ApkInfo.signalCanonicalVersionCode);
      TextSecurePreferences.setFirstInstallVersion(this, ApkInfo.signalCanonicalVersionCode);
    }
  }

  private void initializeNetworkSettings() {
    NetworkManager nm = AppDependencies.getNetworkManager();

    nm.setProxyChoice(TextSecurePreferences.getProxyType(this));
    nm.setProxySocksHost(TextSecurePreferences.getProxySocksHost(this));
    nm.setProxySocksPort(TextSecurePreferences.getProxySocksPort(this));
    nm.applyProxyConfig();

    if (TextSecurePreferences.getFirstInstallVersion(this) != -1) {
      nm.setNetworkEnabled(TextSecurePreferences.hasSeenNetworkConfig(this));
    } else {
      Log.i(TAG, "Network will be disabled until registration begins");
      TextSecurePreferences.setHasSeenNetworkConfig(this, false);
    }
  }

  @MainThread
  public void updatePushNotificationServices() {
    if (!SignalStore.account().isRegistered()) {
      return;
    }

    NotificationDeliveryMethod method = SignalStore.settings().getPreferredNotificationMethod();

    boolean fcmEnabled         = SignalStore.account().isFcmEnabled();
    boolean unifiedPushEnabled = SignalStore.unifiedpush().isEnabled();

    if (method != NotificationDeliveryMethod.FCM) {
      if (fcmEnabled) {
        Log.i(TAG, "Play Services not allowed. Disabling FCM.");
        updateFcmStatus(false);
      } else {
        Log.d(TAG, "FCM is already disabled.");
      }
      if (method == NotificationDeliveryMethod.UNIFIEDPUSH) {
        if (SignalStore.account().isLinkedDevice()) {
          Log.i(TAG, "UnifiedPush not supported in linked devices.");
          updateUnifiedPushStatus(false);
        } else if (!unifiedPushEnabled) {
          Log.i(TAG, "Switching to UnifiedPush.");
          updateUnifiedPushStatus(true);
        } else {
          AppDependencies.getJobManager().add(new UnifiedPushRefreshJob());
        }
      } else {
        if (unifiedPushEnabled) {
          Log.i(TAG, "Switching to WebSocket.");
          updateUnifiedPushStatus(false);
        }
      }
    } else if (!fcmEnabled) {
      Log.i(TAG, "FCM preferred. Updating to use FCM.");
      updateFcmStatus(true);
      updateUnifiedPushStatus(false);
    } else {
      long lastSetTime = SignalStore.account().getFcmTokenLastSetTime();
      long nextSetTime = lastSetTime + TimeUnit.HOURS.toMillis(6);
      long now         = System.currentTimeMillis();

      // MOLLY: Token may have been invalidated while the app was locked
      if (TextSecurePreferences.shouldRefreshFcmToken(this)) {
        TextSecurePreferences.setShouldRefreshFcmToken(this, false);
        nextSetTime = now;
      }

      if (SignalStore.account().getFcmToken() == null || nextSetTime <= now || lastSetTime > now) {
        AppDependencies.getJobManager().add(new FcmRefreshJob());
      }
    }
  }

  private void updateFcmStatus(boolean fcmEnabled) {
    SignalStore.account().setFcmEnabled(fcmEnabled);
    if (!fcmEnabled) {
      FcmRefreshJob.cancelFcmFailureNotification(this);
    }
    AppDependencies.getJobManager().startChain(new FcmRefreshJob())
                                   .then(new RefreshAttributesJob())
                                   .enqueue();
  }

  private void updateUnifiedPushStatus(boolean enabled) {
    SignalStore.unifiedpush().setEnabled(enabled);
    if (enabled) {
      UnifiedPushDistributor.registerApp(SignalStore.unifiedpush().getVapidPublicKey());
    } else if (!SignalStore.unifiedpush().getAirGapped()) {
      // Delete registration only if it isn't air gapped,
      // When air gapped, we want to avoid unnecessary endpoint rotation
      UnifiedPushDistributor.unregisterApp();
    }
    AppDependencies.getJobManager().add(new UnifiedPushRefreshJob());
  }

  private void initializeExpiringMessageManager() {
    AppDependencies.getExpiringMessageManager().checkSchedule();
  }

  private void finalizeExpiringMessageManager() {
    AppDependencies.getExpiringMessageManager().quit();
  }

  private void initializeRevealableMessageManager() {
    AppDependencies.getViewOnceMessageManager().scheduleIfNecessary();
  }

  private void initializePendingRetryReceiptManager() {
    AppDependencies.getPendingRetryReceiptManager().scheduleIfNecessary();
  }

  private void initializeScheduledMessageManager() {
    AppDependencies.getScheduledMessageManager().scheduleIfNecessary();
  }

  private void initializeTrimThreadsByDateManager() {
    KeepMessagesDuration keepMessagesDuration = SignalStore.settings().getKeepMessagesDuration();
    if (keepMessagesDuration != KeepMessagesDuration.FOREVER) {
      AppDependencies.getTrimThreadsByDateManager().scheduleIfNecessary();
    }
  }

  private void initializePeriodicTasks() {
    RotateSignedPreKeyListener.schedule(this);
    DirectoryRefreshListener.schedule(this);
    LocalBackupListener.schedule(this);
    MessageBackupListener.schedule(this);
    RotateSenderCertificateListener.schedule(this);
    RoutineMessageFetchReceiver.startOrUpdateAlarm(this);
    AnalyzeDatabaseAlarmListener.schedule(this);

    if (TextSecurePreferences.isUpdateApkEnabled(this)) {
      ApkUpdateRefreshListener.scheduleIfAllowed(this);
    }
  }

  private void initializeRingRtc() {
    try {
      Map<String, String> fieldTrials = new HashMap<>();
      if (RemoteConfig.callingFieldTrialAnyAddressPortsKillSwitch()) {
        fieldTrials.put("RingRTC-AnyAddressPortsKillSwitch", "Enabled");
      }
      CallManager.initialize(this, new RingRtcLogger(), fieldTrials);
    } catch (UnsatisfiedLinkError e) {
      throw new AssertionError("Unable to load ringrtc library", e);
    }
  }

  private void ensureProfileUploaded() {
    if (SignalStore.account().isRegistered() && !SignalStore.registration().hasUploadedProfile() && !Recipient.self().getProfileName().isEmpty() && SignalStore.account().isPrimaryDevice()) {
      Log.w(TAG, "User has a profile, but has not uploaded one. Uploading now.");
      AppDependencies.getJobManager().add(new ProfileUploadJob());
    }
  }

  private void executePendingContactSync() {
    if (TextSecurePreferences.needsFullContactSync(this)) {
      AppDependencies.getJobManager().add(new MultiDeviceContactUpdateJob(true));
    }
  }

  @VisibleForTesting
  protected void beginJobLoop() {
    AppDependencies.getJobManager().beginJobLoop();
  }

  @WorkerThread
  private void initializeBlobProvider() {
    BlobProvider.getInstance().initialize(this);
  }

  @WorkerThread
  private void cleanAvatarStorage() {
    AvatarPickerStorage.cleanOrphans(this);
  }

  @WorkerThread
  private void initializeCleanup() {
    int deleted = SignalDatabase.attachments().deleteAbandonedPreuploadedAttachments();
    Log.i(TAG, "Deleted " + deleted + " abandoned attachments.");
  }

  private void initializeGlideCodecs() {
    SignalGlideCodecs.setLogProvider(new com.servalabs.chat.glide.Log.Provider() {
      @Override
      public void v(@NonNull String tag, @NonNull String message) {
        Log.v(tag, message);
      }

      @Override
      public void d(@NonNull String tag, @NonNull String message) {
        Log.d(tag, message);
      }

      @Override
      public void i(@NonNull String tag, @NonNull String message) {
        Log.i(tag, message);
      }

      @Override
      public void w(@NonNull String tag, @NonNull String message) {
        Log.w(tag, message);
      }

      @Override
      public void e(@NonNull String tag, @NonNull String message, @Nullable Throwable throwable) {
        Log.e(tag, message, throwable);
      }
    });
  }

  private final BroadcastReceiver keyEventReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      boolean keyExpired = intent.getBooleanExtra(KeyCachingService.EXTRA_KEY_EXPIRED, false);
      onLock(keyExpired);
    }
  };

  private void registerKeyEventReceiver() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(KeyCachingService.CLEAR_KEY_EVENT);
    ContextCompat.registerReceiver(this, keyEventReceiver, filter, KeyCachingService.KEY_PERMISSION, null, ContextCompat.RECEIVER_NOT_EXPORTED);
  }

  private void unregisterKeyEventReceiver() {
    unregisterReceiver(keyEventReceiver);
  }

  @Override
  protected void attachBaseContext(Context base) {
    DynamicLanguageContextWrapper.updateContext(base);
    super.attachBaseContext(base);
  }

  private static class ProviderInitializationException extends RuntimeException {
  }
}
