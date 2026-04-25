package com.servalabs.chat.components.settings.app.subscription.receipts.list

import android.view.LayoutInflater
import android.view.ViewGroup
import org.signal.core.ui.util.ThemeUtil
import com.servalabs.chat.R
import com.servalabs.chat.components.settings.DSLSettingsText
import com.servalabs.chat.components.settings.SectionHeaderPreference
import com.servalabs.chat.components.settings.SectionHeaderPreferenceViewHolder
import com.servalabs.chat.components.settings.TextPreference
import com.servalabs.chat.components.settings.TextPreferenceViewHolder
import com.servalabs.chat.util.StickyHeaderDecoration
import com.servalabs.chat.util.adapter.mapping.LayoutFactory
import com.servalabs.chat.util.adapter.mapping.MappingAdapter
import com.servalabs.chat.util.toLocalDateTime

class DonationReceiptListAdapter(onModelClick: (DonationReceiptListItem.Model) -> Unit) : MappingAdapter(), StickyHeaderDecoration.StickyHeaderAdapter<SectionHeaderPreferenceViewHolder> {

  init {
    registerFactory(TextPreference::class.java, LayoutFactory({ TextPreferenceViewHolder(it) }, R.layout.dsl_preference_item))
    DonationReceiptListItem.register(this, onModelClick)
  }

  override fun getHeaderId(position: Int): Long {
    return when (val item = getItem(position)) {
      is DonationReceiptListItem.Model -> item.record.timestamp.toLocalDateTime().year.toLong()
      else -> StickyHeaderDecoration.StickyHeaderAdapter.NO_HEADER_ID
    }
  }

  override fun onCreateHeaderViewHolder(parent: ViewGroup?, position: Int, type: Int): SectionHeaderPreferenceViewHolder {
    return SectionHeaderPreferenceViewHolder(LayoutInflater.from(parent!!.context).inflate(R.layout.dsl_section_header, parent, false))
  }

  override fun onBindHeaderViewHolder(viewHolder: SectionHeaderPreferenceViewHolder?, position: Int, type: Int) {
    viewHolder?.itemView?.run {
      val color = ThemeUtil.getThemedColor(context, com.google.android.material.R.attr.colorSurface)
      setBackgroundColor(color)
    }

    viewHolder?.bind(SectionHeaderPreference(DSLSettingsText.from(getHeaderId(position).toString())))
  }
}
