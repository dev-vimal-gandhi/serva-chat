/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.servalabs.chat.registration.ui.welcome

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.servalabs.chat.core.ui.logging.LoggingFragment
import com.servalabs.chat.core.util.getSerializableCompat
import com.servalabs.chat.core.util.logging.Log
import com.servalabs.chat.R
import com.servalabs.chat.components.ViewBinderDelegate
import com.servalabs.chat.databinding.FragmentRegistrationWelcomeV3Binding
import com.servalabs.chat.dependencies.AppDependencies
import com.servalabs.chat.registration.fragments.RegistrationViewDelegate.setDebugLogSubmitMultiTapView
import com.servalabs.chat.registration.fragments.WelcomePermissions
import com.servalabs.chat.registration.ui.RegistrationCheckpoint
import com.servalabs.chat.registration.ui.RegistrationViewModel
import com.servalabs.chat.registration.ui.permissions.GrantPermissionsFragment
import com.servalabs.chat.registration.ui.phonenumber.EnterPhoneNumberMode
import com.servalabs.chat.util.BackupUtil
import com.servalabs.chat.util.CommunicationActions
import com.servalabs.chat.util.TextSecurePreferences
import com.servalabs.chat.util.navigation.safeNavigate
import com.servalabs.chat.util.visible

/**
 * First screen that is displayed on the very first app launch.
 */
class WelcomeFragment : LoggingFragment(R.layout.fragment_registration_welcome_v3) {
  companion object {
    private val TAG = Log.tag(WelcomeFragment::class.java)
    private const val TERMS_AND_CONDITIONS_URL = "https://signal.org/legal"
  }

  private val sharedViewModel by activityViewModels<RegistrationViewModel>()
  private val binding: FragmentRegistrationWelcomeV3Binding by ViewBinderDelegate(FragmentRegistrationWelcomeV3Binding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setDebugLogSubmitMultiTapView(binding.image)
    setDebugLogSubmitMultiTapView(binding.title)

    binding.welcomeContinueButton.setOnClickListener { onContinueClicked() }
    // binding.welcomeTermsButton.setOnClickListener { onTermsClicked() }
    binding.welcomeTransferOrRestore.setOnClickListener { onRestoreOrTransferClicked() }
    binding.welcomeTransferOrRestore.visible = !sharedViewModel.isReregister

    binding.link.setOnClickListener { onLinkDeviceClicked() }

    childFragmentManager.setFragmentResultListener(RestoreWelcomeBottomSheet.REQUEST_KEY, viewLifecycleOwner) { requestKey, bundle ->
      if (requestKey == RestoreWelcomeBottomSheet.REQUEST_KEY) {
        when (val userSelection = bundle.getSerializableCompat(RestoreWelcomeBottomSheet.REQUEST_KEY, WelcomeUserSelection::class.java)) {
          WelcomeUserSelection.RESTORE_WITH_OLD_PHONE,
          WelcomeUserSelection.RESTORE_WITH_NO_PHONE -> afterRestoreOrTransferClicked(userSelection)
          else -> Unit
        }
      }
    }

    parentFragmentManager.setFragmentResultListener(GrantPermissionsFragment.REQUEST_KEY, viewLifecycleOwner) { requestKey, bundle ->
      if (requestKey == GrantPermissionsFragment.REQUEST_KEY) {
        when (val userSelection = bundle.getSerializableCompat(GrantPermissionsFragment.REQUEST_KEY, WelcomeUserSelection::class.java)) {
          WelcomeUserSelection.RESTORE_WITH_OLD_PHONE,
          WelcomeUserSelection.RESTORE_WITH_NO_PHONE -> navigateToNextScreenViaRestore(userSelection)
          WelcomeUserSelection.CONTINUE -> navigateToNextScreenViaContinue()
          WelcomeUserSelection.LINK -> navigateToLinkDevice()
          null -> Unit
        }
      }
    }

    with(requireActivity() as AppCompatActivity) {
      setSupportActionBar(binding.toolbar)
      supportActionBar?.apply {
        setDisplayShowTitleEnabled(false)
      }
      addMenuProvider(UseProxyMenuProvider(), viewLifecycleOwner)
    }
  }

  private fun onLinkDeviceClicked() {
    if (!hasAllPermissions()) {
      findNavController().safeNavigate(WelcomeFragmentDirections.actionWelcomeFragmentToGrantPermissionsFragment(WelcomeUserSelection.LINK))
    } else {
      navigateToLinkDevice()
    }
  }

  private fun navigateToLinkDevice() {
    enableNetwork()
    findNavController().safeNavigate(WelcomeFragmentDirections.goToLinkViaQr())
  }

  private fun enableNetwork() {
    TextSecurePreferences.setHasSeenNetworkConfig(requireContext(), true)
    AppDependencies.networkManager.setNetworkEnabled(true)
  }

  override fun onResume() {
    super.onResume()
    sharedViewModel.resetRestoreDecision()
  }

  private fun onContinueClicked() {
    if (!hasAllPermissions()) {
      findNavController().safeNavigate(WelcomeFragmentDirections.actionWelcomeFragmentToGrantPermissionsFragment(WelcomeUserSelection.CONTINUE))
    } else {
      navigateToNextScreenViaContinue()
    }
  }

  private fun navigateToNextScreenViaContinue() {
    sharedViewModel.maybePrefillE164(requireContext())
    findNavController().safeNavigate(WelcomeFragmentDirections.goToEnterPhoneNumber(EnterPhoneNumberMode.NORMAL))
  }

  private fun onTermsClicked() {
    CommunicationActions.openBrowserLink(requireContext(), TERMS_AND_CONDITIONS_URL)
  }

  private fun onRestoreOrTransferClicked() {
    RestoreWelcomeBottomSheet().show(childFragmentManager, null)
  }

  private fun afterRestoreOrTransferClicked(userSelection: WelcomeUserSelection) {
    if (!hasAllPermissions()) {
      findNavController().safeNavigate(WelcomeFragmentDirections.actionWelcomeFragmentToGrantPermissionsFragment(userSelection))
    } else {
      navigateToNextScreenViaRestore(userSelection)
    }
  }

  private fun navigateToNextScreenViaRestore(userSelection: WelcomeUserSelection) {
    sharedViewModel.maybePrefillE164(requireContext())
    sharedViewModel.setRegistrationCheckpoint(RegistrationCheckpoint.PERMISSIONS_GRANTED)

    when (userSelection) {
      WelcomeUserSelection.LINK,
      WelcomeUserSelection.CONTINUE -> throw IllegalArgumentException()
      WelcomeUserSelection.RESTORE_WITH_OLD_PHONE -> {
        enableNetwork()
        sharedViewModel.intendToRestore(hasOldDevice = true, fromRemote = true)
        findNavController().safeNavigate(WelcomeFragmentDirections.goToRestoreViaQr())
      }
      WelcomeUserSelection.RESTORE_WITH_NO_PHONE -> {
        sharedViewModel.intendToRestore(hasOldDevice = false, fromRemote = true)
        findNavController().safeNavigate(WelcomeFragmentDirections.goToSelectRestoreMethod(userSelection))
      }
    }
  }

  private fun hasAllPermissions(): Boolean {
    val isUserSelectionRequired = BackupUtil.isUserSelectionRequired(requireContext())
    return WelcomePermissions.getWelcomePermissions(isUserSelectionRequired).all { ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED }
  }

  private inner class UseProxyMenuProvider : MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
      menuInflater.inflate(R.menu.enter_phone_number, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean = if (menuItem.itemId == R.id.phone_menu_use_proxy) {
      NavHostFragment.findNavController(this@WelcomeFragment).safeNavigate(WelcomeFragmentDirections.actionEditProxy())
      true
    } else {
      false
    }
  }
}
