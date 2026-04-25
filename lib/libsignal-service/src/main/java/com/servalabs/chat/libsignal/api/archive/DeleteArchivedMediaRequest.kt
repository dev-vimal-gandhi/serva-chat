/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.libsignal.api.archive

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Delete media from the backup cdn.
 */
class DeleteArchivedMediaRequest(
  @JsonProperty val mediaToDelete: List<ArchivedMediaObject>
) {
  data class ArchivedMediaObject(
    @JsonProperty val cdn: Int,
    @JsonProperty val mediaId: String
  )
}
