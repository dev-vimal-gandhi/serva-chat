package com.servalabs.chat.libsignal.api.groupsv2

import com.servalabs.chat.libsignal.zkgroup.groupsend.GroupSendEndorsementsResponse
import com.servalabs.chat.libsignal.internal.push.PushServiceSocket.GroupHistory

/**
 * Wraps result of group history fetch with it's associated paging data.
 */
data class GroupHistoryPage(val changeLogs: List<DecryptedGroupChangeLog>, val groupSendEndorsementsResponse: GroupSendEndorsementsResponse?, val pagingData: PagingData) {

  data class PagingData(val hasMorePages: Boolean, val nextPageRevision: Int) {
    companion object {
      @JvmField
      val NONE = PagingData(false, -1)

      @JvmStatic
      fun forGroupHistory(groupHistory: GroupHistory): PagingData {
        return PagingData(groupHistory.hasMore(), if (groupHistory.hasMore()) groupHistory.nextPageStartGroupRevision else -1)
      }
    }
  }
}
