/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api.storage

import com.servalabs.chat.core.models.ServiceId
import com.servalabs.chat.libsignal.internal.storage.protos.ContactRecord

val ContactRecord.signalAci: ServiceId.ACI?
  get() = ServiceId.ACI.parseOrNull(this.aci, this.aciBinary)

val ContactRecord.signalPni: ServiceId.PNI?
  get() = ServiceId.PNI.parseOrNull(this.pni, this.pniBinary)
