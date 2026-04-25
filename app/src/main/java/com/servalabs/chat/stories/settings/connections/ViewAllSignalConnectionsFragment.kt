package com.servalabs.chat.stories.settings.connections

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.servalabs.chat.R
import com.servalabs.chat.components.ViewBinderDelegate
import com.servalabs.chat.components.WrapperDialogFragment
import com.servalabs.chat.contacts.LetterHeaderDecoration
import com.servalabs.chat.contacts.paged.ContactSearchAdapter
import com.servalabs.chat.contacts.paged.ContactSearchConfiguration
import com.servalabs.chat.contacts.paged.ContactSearchMediator
import com.servalabs.chat.database.RecipientTable
import com.servalabs.chat.databinding.ViewAllSignalConnectionsFragmentBinding
import com.servalabs.chat.groups.SelectionLimits

class ViewAllSignalConnectionsFragment : Fragment(R.layout.view_all_signal_connections_fragment) {

  private val binding by ViewBinderDelegate(ViewAllSignalConnectionsFragmentBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.recycler.addItemDecoration(LetterHeaderDecoration(requireContext()) { false })
    binding.toolbar.setNavigationOnClickListener {
      requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    val mediator = ContactSearchMediator(
      fragment = this,
      selectionLimits = SelectionLimits(0, 0),
      isMultiSelect = false,
      displayOptions = ContactSearchAdapter.DisplayOptions(
        displayCheckBox = false,
        displaySecondaryInformation = ContactSearchAdapter.DisplaySecondaryInformation.NEVER
      ),
      mapStateToConfiguration = { getConfiguration() },
      performSafetyNumberChecks = false
    )

    binding.recycler.adapter = mediator.adapter
  }

  private fun getConfiguration(): ContactSearchConfiguration {
    return ContactSearchConfiguration.build {
      addSection(
        ContactSearchConfiguration.Section.Individuals(
          includeHeader = false,
          includeSelfMode = RecipientTable.IncludeSelfMode.Exclude,
          includeLetterHeaders = true,
          transportType = ContactSearchConfiguration.TransportType.PUSH
        )
      )
    }
  }

  class Dialog : WrapperDialogFragment() {
    override fun getWrappedFragment(): Fragment {
      return ViewAllSignalConnectionsFragment()
    }

    companion object {
      fun show(fragmentManager: FragmentManager) {
        Dialog().show(fragmentManager, null)
      }
    }
  }
}
