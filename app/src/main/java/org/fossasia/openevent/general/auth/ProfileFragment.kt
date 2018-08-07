package org.fossasia.openevent.general.auth

import android.arch.lifecycle.Observer
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.content.res.AppCompatResources
import android.view.*
import android.widget.Toast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.view.*
import org.fossasia.openevent.general.AuthActivity
import org.fossasia.openevent.general.CircleTransform
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.settings.SettingsFragment
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.android.architecture.ext.viewModel

class ProfileFragment : Fragment() {
    private val profileFragmentViewModel by viewModel<ProfileFragmentViewModel>()

    private lateinit var rootView: View
    private var emailSettings: String? = null
    private val EMAIL: String = "EMAIL"

    private fun redirectToLogin() {
        startActivity(Intent(activity, AuthActivity::class.java))
        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun redirectToMain() {
        startActivity(Intent(activity, MainActivity::class.java))
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (!profileFragmentViewModel.isLoggedIn()) {
            Toast.makeText(context, "You need to Login!", Toast.LENGTH_LONG).show()
            redirectToLogin()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false)

        setHasOptionsMenu(true)

        profileFragmentViewModel.progress.observe(this, Observer {
            it?.let { Utils.showProgressBar(rootView.progressBar, it) }
        })

        profileFragmentViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        profileFragmentViewModel.user.observe(this, Observer {
            it?.let {
                rootView.name.text = "${it.firstName.nullToEmpty()} ${it.lastName.nullToEmpty()}"
                rootView.email.text = it.email
                emailSettings = it.email


                Picasso.get()
                        .load(it.avatarUrl)
                        .placeholder(AppCompatResources.getDrawable(context!!, R.drawable.ic_person_black_24dp)!!)   //TODO: Make null safe
                        .transform(CircleTransform())
                        .into(rootView.avatar)
            }
        })

        fetchProfile()

        return rootView
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.getItemId()) {
            R.id.edit_profile -> {
                val fragment = EditProfileFragment()
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.frameContainer, fragment)?.addToBackStack(null)?.commit()
                return true
            }
            R.id.orga_app -> {
                startOrgaApp("org.fossasia.eventyay")
                return true
            }
            R.id.ticket_issues -> {
                context?.let {
                    Utils.openUrl(it, resources.getString(R.string.ticket_issues_url))
                }
                return true
            }
            R.id.logout -> {
                profileFragmentViewModel.logout()
                redirectToMain()
                return true
            }
            R.id.settings -> {
                val fragment = SettingsFragment()
                val bundle = Bundle()
                bundle.putString(EMAIL, emailSettings)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.frameContainer, fragment)?.addToBackStack(null)?.commit()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu?.setGroupVisible(R.id.profile_menu, true)
        super.onPrepareOptionsMenu(menu)
    }

    private fun startOrgaApp(packageName: String) {
        val manager = activity?.packageManager
        try {
            val intent = manager?.getLaunchIntentForPackage(packageName)
                    ?: throw  ActivityNotFoundException()
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
        if (!profileFragmentViewModel.isLoggedIn())
            return

        rootView.progressBar.isIndeterminate = true
        profileFragmentViewModel.fetchProfile()

    }

    override fun onResume() {
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        activity?.supportActionBar?.title = "Profile"
        setHasOptionsMenu(true)
        super.onResume()
    }
}