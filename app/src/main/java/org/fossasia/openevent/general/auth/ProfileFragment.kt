package org.fossasia.openevent.general.auth

import androidx.appcompat.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.view.profileCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_profile.view.avatar
import kotlinx.android.synthetic.main.fragment_profile.view.email
import kotlinx.android.synthetic.main.fragment_profile.view.name
import kotlinx.android.synthetic.main.fragment_profile.view.progressBar
import kotlinx.android.synthetic.main.fragment_profile.view.editProfileRL
import kotlinx.android.synthetic.main.fragment_profile.view.logoutLL
import kotlinx.android.synthetic.main.fragment_profile.view.manageEventsLL
import kotlinx.android.synthetic.main.fragment_profile.view.settingsLL
import kotlinx.android.synthetic.main.fragment_profile.view.ticketIssuesLL
import org.fossasia.openevent.general.CircleTransform
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.settings.SettingsFragmentArgs
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.getAnimFade
import org.fossasia.openevent.general.utils.Utils.requireDrawable
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.fossasia.openevent.general.utils.Utils.setToolbar

class ProfileFragment : Fragment() {
    private val profileViewModel by viewModel<ProfileViewModel>()

    private lateinit var rootView: View
    private var emailSettings: String? = null

    private fun redirectToLogin() {
        LoginFragmentArgs.Builder()
            .setSnackbarMessage(getString(R.string.log_in_first))
            .build()
            .toBundle()
            .also { bundle ->
                findNavController(rootView).navigate(R.id.loginFragment, bundle, getAnimFade())
            }
    }

    private fun redirectToEventsFragment() {
        findNavController(rootView).popBackStack(R.id.eventsFragment, false)
    }

    override fun onStart() {
        super.onStart()
        if (!profileViewModel.isLoggedIn()) {
            redirectToLogin()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false)

        profileViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.progressBar.isVisible = it
            })

        profileViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                Snackbar.make(rootView.profileCoordinatorLayout, it, Snackbar.LENGTH_SHORT).show()
            })

        profileViewModel.user
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.name.text = "${it.firstName.nullToEmpty()} ${it.lastName.nullToEmpty()}"
                rootView.email.text = it.email
                emailSettings = it.email

                Picasso.get()
                        .load(it.avatarUrl)
                        .placeholder(requireDrawable(requireContext(), R.drawable.ic_account_circle_grey))
                        .transform(CircleTransform())
                        .into(rootView.avatar)

                rootView.editProfileRL.setOnClickListener {
                    findNavController(rootView).navigate(R.id.editProfileFragment, null, getAnimFade())
                }
            })

        fetchProfile()

        rootView.manageEventsLL.setOnClickListener { startOrgaApp("com.eventyay.organizer") }

        rootView.settingsLL.setOnClickListener {

            SettingsFragmentArgs.Builder(emailSettings)
                .build()
                .toBundle()
                .also { bundle ->
                    findNavController(rootView).navigate(R.id.settingsFragment, bundle, getAnimFade())
                }
        }

        rootView.ticketIssuesLL.setOnClickListener {
            Utils.openUrl(requireContext(), resources.getString(R.string.ticket_issues_url))
        }

        rootView.logoutLL.setOnClickListener { showLogoutDialog() }

        return rootView
    }

    private fun startOrgaApp(packageName: String) {
        val manager = activity?.packageManager
        try {
            val intent = manager?.getLaunchIntentForPackage(packageName)
                    ?: throw ActivityNotFoundException()
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            showInMarket(packageName)
        }
    }

    private fun showInMarket(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun fetchProfile() {
        if (!profileViewModel.isLoggedIn())
            return

        rootView.progressBar.isIndeterminate = true
        profileViewModel.fetchProfile()
    }

    override fun onResume() {
        setToolbar(activity, getString(R.string.profile), false)
        super.onResume()
    }

    private fun showLogoutDialog() {
            AlertDialog.Builder(requireContext()).setMessage(resources.getString(R.string.message))
            .setPositiveButton(resources.getString(R.string.logout)) { _, _ ->
                if (profileViewModel.isLoggedIn()) {
                    profileViewModel.logout()
                    redirectToEventsFragment()
                }
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
            .show()
    }
}
