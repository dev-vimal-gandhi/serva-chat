/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.net

import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.dependencies.KeyTransparencyApi
import com.servalabs.chat.libsignal.api.account.AccountApi
import com.servalabs.chat.libsignal.api.archive.ArchiveApi
import com.servalabs.chat.libsignal.api.attachment.AttachmentApi
import com.servalabs.chat.libsignal.api.calling.CallingApi
import com.servalabs.chat.libsignal.api.cds.CdsApi
import com.servalabs.chat.libsignal.api.certificate.CertificateApi
import com.servalabs.chat.libsignal.api.keys.KeysApi
import com.servalabs.chat.libsignal.api.link.LinkDeviceApi
import com.servalabs.chat.libsignal.api.message.MessageApi
import com.servalabs.chat.libsignal.api.payments.PaymentsApi
import com.servalabs.chat.libsignal.api.profiles.ProfileApi
import com.servalabs.chat.libsignal.api.provisioning.ProvisioningApi
import com.servalabs.chat.libsignal.api.ratelimit.RateLimitChallengeApi
import com.servalabs.chat.libsignal.api.remoteconfig.RemoteConfigApi
import com.servalabs.chat.libsignal.api.storage.StorageServiceApi
import com.servalabs.chat.libsignal.api.svr.SvrBApi
import com.servalabs.chat.libsignal.api.username.UsernameApi

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
