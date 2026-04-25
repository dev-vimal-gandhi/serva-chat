/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.net

import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.dependencies.KeyTransparencyApi
import org.signal.libsignal.api.account.AccountApi
import org.signal.libsignal.api.archive.ArchiveApi
import org.signal.libsignal.api.attachment.AttachmentApi
import org.signal.libsignal.api.calling.CallingApi
import org.signal.libsignal.api.cds.CdsApi
import org.signal.libsignal.api.certificate.CertificateApi
import org.signal.libsignal.api.keys.KeysApi
import org.signal.libsignal.api.link.LinkDeviceApi
import org.signal.libsignal.api.message.MessageApi
import org.signal.libsignal.api.payments.PaymentsApi
import org.signal.libsignal.api.profiles.ProfileApi
import org.signal.libsignal.api.provisioning.ProvisioningApi
import org.signal.libsignal.api.ratelimit.RateLimitChallengeApi
import org.signal.libsignal.api.remoteconfig.RemoteConfigApi
import org.signal.libsignal.api.storage.StorageServiceApi
import org.signal.libsignal.api.svr.SvrBApi
import org.signal.libsignal.api.username.UsernameApi

/**
 * A convenient way to access network operations, similar to [com.servalabs.chat.database.SignalDatabase] and [com.servalabs.chat.keyvalue.SignalStore].
 */
object SignalNetwork {
  @JvmStatic
  @get:JvmName("account")
  val account: AccountApi
    get() = AppDependencies.accountApi

  val archive: ArchiveApi
    get() = AppDependencies.archiveApi

  val attachments: AttachmentApi
    get() = AppDependencies.attachmentApi

  @JvmStatic
  @get:JvmName("calling")
  val calling: CallingApi
    get() = AppDependencies.callingApi

  val cdsApi: CdsApi
    get() = AppDependencies.cdsApi

  @JvmStatic
  @get:JvmName("certificate")
  val certificate: CertificateApi
    get() = AppDependencies.certificateApi

  @JvmStatic
  @get:JvmName("keys")
  val keys: KeysApi
    get() = AppDependencies.keysApi

  val linkDevice: LinkDeviceApi
    get() = AppDependencies.linkDeviceApi

  @JvmStatic
  @get:JvmName("message")
  val message: MessageApi
    get() = AppDependencies.messageApi

  @JvmStatic
  @get:JvmName("payments")
  val payments: PaymentsApi
    get() = AppDependencies.paymentsApi

  @JvmStatic
  @get:JvmName("profile")
  val profile: ProfileApi
    get() = AppDependencies.profileApi

  val provisioning: ProvisioningApi
    get() = AppDependencies.provisioningApi

  @JvmStatic
  @get:JvmName("rateLimitChallenge")
  val rateLimitChallenge: RateLimitChallengeApi
    get() = AppDependencies.rateLimitChallengeApi

  @JvmStatic
  @get:JvmName("remoteConfig")
  val remoteConfig: RemoteConfigApi
    get() = AppDependencies.remoteConfigApi

  val storageService: StorageServiceApi
    get() = AppDependencies.storageServiceApi

  @JvmStatic
  @get:JvmName("username")
  val username: UsernameApi
    get() = AppDependencies.usernameApi

  val svrB: SvrBApi
    get() = AppDependencies.svrBApi

  val keyTransparency: KeyTransparencyApi
    get() = AppDependencies.keyTransparencyApi
}
