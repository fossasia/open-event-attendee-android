package org.fossasia.openevent.general.auth

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
import org.fossasia.openevent.general.CircleTransform
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.getAnimFade
import org.fossasia.openevent.general.utils.Utils.getAnimSlide
import org.fossasia.openevent.general.utils.Utils.requireDrawable
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : Fragment() {
    private val profileViewModel by viewModel<ProfileViewModel>()

    private lateinit var rootView: View
    private var emailSettings: String? = null
    private val EMAIL: String = "EMAIL"

    private fun redirectToLogin() {
        findNavController(rootView).navigate(R.id.loginFragment, null, getAnimSlide())
    }

    private fun redirectToMain() {
        startActivity(Intent(activity, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false)

        setHasOptionsMenu(true)

        if (!profileViewModel.isLoggedIn()) {
            Snackbar.make(rootView.profileCoordinatorLayout, "You need to log in first!", Snackbar.LENGTH_SHORT).show()
            Handler().postDelayed({
                redirectToLogin()
            }, 1000)
        }

        profileViewModel.progress
            .nonNull()
            .observe(this, Observer {
                rootView.progressBar.isVisible = it
            })

        profileViewModel.error
            .nonNull()
            .observe(this, Observer {
                Snackbar.make(rootView.profileCoordinatorLayout, it, Snackbar.LENGTH_SHORT).show()
            })

        profileViewModel.user
            .nonNull()
            .observe(this, Observer {
                rootView.name.text = "${it.firstName.nullToEmpty()} ${it.lastName.nullToEmpty()}"
                rootView.email.text = it.email
                emailSettings = it.email

                Picasso.get()
                        .load(it.avatarUrl)
                        .placeholder(requireDrawable(requireContext(), R.drawable.ic_person_black_24dp))
                        .transform(CircleTransform())
                        .into(rootView.avatar)
            })

        fetchProfile()

        return rootView
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_profile -> {
                findNavController(rootView).navigate(R.id.editProfileFragment, null, getAnimFade())
                return true
            }
            R.id.orga_app -> {
                startOrgaApp("com.eventyay.organizer")
                return true
            }
            R.id.ticket_issues -> {
                context?.let {
                    Utils.openUrl(it, resources.getString(R.string.ticket_issues_url))
                }
                return true
            }
            R.id.logout -> {
                profileViewModel.logout()
                activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
                redirectToMain()
                return true
            }
            R.id.settings -> {
                val bundle = Bundle()
                bundle.putString(EMAIL, emailSettings)
                findNavController(rootView).navigate(R.id.settingsFragment, bundle, getAnimFade())
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile, menu)
        super.onCreateOptionsMenu(menu, inflater)
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
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun fetchProfile() {
        if (!profileViewModel.isLoggedIn())
            return

        rootView.progressBar.isIndeterminate = true
        profileViewModel.fetchProfile()
    }

    override fun onResume() {
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        activity?.supportActionBar?.title = "Profile"
        setHasOptionsMenu(true)
        super.onResume()
    }
}
