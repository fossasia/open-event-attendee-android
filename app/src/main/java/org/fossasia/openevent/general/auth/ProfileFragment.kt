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
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.view.profileCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_profile.view.avatar
import kotlinx.android.synthetic.main.fragment_profile.view.email
import kotlinx.android.synthetic.main.fragment_profile.view.name
import kotlinx.android.synthetic.main.fragment_profile.view.editProfileRL
import kotlinx.android.synthetic.main.fragment_profile.view.logoutLL
import kotlinx.android.synthetic.main.fragment_profile.view.manageEventsLL
import kotlinx.android.synthetic.main.fragment_profile.view.settingsLL
import kotlinx.android.synthetic.main.fragment_profile.view.ticketIssuesLL
import kotlinx.android.synthetic.main.fragment_profile.view.loginButton
import kotlinx.android.synthetic.main.fragment_profile.view.verificationLayout
import kotlinx.android.synthetic.main.fragment_profile.view.verifiedTextView
import kotlinx.android.synthetic.main.fragment_profile.view.verifiedTick
import kotlinx.android.synthetic.main.fragment_profile.view.resendEmailTextView
import org.fossasia.openevent.general.CircleTransform
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.requireDrawable
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.Utils.progressDialog
import org.fossasia.openevent.general.utils.Utils.show
import org.jetbrains.anko.design.snackbar

const val PROFILE_FRAGMENT = "profileFragment"

class ProfileFragment : Fragment() {
    private val profileViewModel by viewModel<ProfileViewModel>()

    private lateinit var rootView: View
    private var emailSettings: String? = null
    private var user: User? = null

    private fun redirectToLogin() {
        findNavController(rootView).navigate(ProfileFragmentDirections.actionProfileToAuth(null, PROFILE_FRAGMENT))
    }

    private fun redirectToEventsFragment() {
        findNavController(rootView).popBackStack(R.id.eventsFragment, false)
    }

    override fun onStart() {
        super.onStart()
        handleLayoutVisibility(profileViewModel.isLoggedIn())
    }

    private fun handleLayoutVisibility(isLoggedIn: Boolean) {
        rootView.editProfileRL.isVisible = isLoggedIn
        rootView.logoutLL.isVisible = isLoggedIn
        rootView.loginButton.isVisible = !isLoggedIn
        rootView.verificationLayout.isVisible = isLoggedIn
        rootView.resendEmailTextView.isVisible = isLoggedIn
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false)
        if (profileViewModel.isLoggedIn())
            profileViewModel.fetchProfile()

        val progressDialog = progressDialog(context, getString(R.string.loading_message))
        profileViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })

        profileViewModel.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.profileCoordinatorLayout.snackbar(it)
            })

        profileViewModel.user
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                user = it
                rootView.name.text = "${it.firstName.nullToEmpty()} ${it.lastName.nullToEmpty()}"
                rootView.email.text = it.email
                emailSettings = it.email
                rootView.verifiedTick.isVisible = it.isVerified
                rootView.resendEmailTextView.isVisible = !it.isVerified
                rootView.verifiedTextView.text =
                    if (it.isVerified) getString(R.string.verified) else getString(R.string.not_verified)
                if (it.isVerified)
                    rootView.verifiedTextView.setTextColor(
                        resources.getColorStateList(android.R.color.holo_green_light)
                    )

                Picasso.get()
                        .load(it.avatarUrl)
                        .placeholder(requireDrawable(requireContext(), R.drawable.ic_account_circle_grey))
                        .transform(CircleTransform())
                        .into(rootView.avatar)

                rootView.editProfileRL.setOnClickListener {
                        findNavController(rootView).navigate(ProfileFragmentDirections.actionProfileToEditProfile())
                }
            })
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootView.manageEventsLL.setOnClickListener { startOrgaApp("com.eventyay.organizer") }

        rootView.settingsLL.setOnClickListener {
            findNavController(rootView).navigate(ProfileFragmentDirections.actionProfileToSettings(emailSettings))
        }

        rootView.ticketIssuesLL.setOnClickListener {
            Utils.openUrl(requireContext(), resources.getString(R.string.ticket_issues_url))
        }

        rootView.logoutLL.setOnClickListener { showLogoutDialog() }
        rootView.loginButton.setOnClickListener { redirectToLogin() }

        rootView.resendEmailTextView.setOnClickListener {
            val userEmail = user?.email
            if (userEmail != null) {
                profileViewModel.resendVerificationEmail(userEmail)
            } else {
                rootView.profileCoordinatorLayout.snackbar(getString(R.string.error))
            }
        }
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
