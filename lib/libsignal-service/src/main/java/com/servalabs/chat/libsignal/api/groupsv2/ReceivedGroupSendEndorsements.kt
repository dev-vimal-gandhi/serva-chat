/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api.groupsv2

import com.servalabs.chat.core.models.ServiceId
import com.servalabs.chat.libsignal.zkgroup.groupsend.GroupSendEndorsement
import com.servalabs.chat.libsignal.zkgroup.groupsend.GroupSendEndorsementsResponse
import java.time.Instant

/**
 * Group send endorsement data received from the server.
 */
data class ReceivedGroupSendEndorsements(
  val expirationMs: Long,
  val endorsements: Map<ServiceId.ACI, GroupSendEndorsement>
) {
  constructor(
    expiration: Instant,
    members: List<ServiceId.ACI>,
    receivedEndorsements: GroupSendEndorsementsResponse.ReceivedEndorsements
  ) : this(
    expirationMs = expiration.toEpochMilli(),
    endorsements = members.zip(receivedEndorsements.endorsements).toMap()
  )
}
