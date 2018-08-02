package org.fossasia.openevent.general.auth

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.koin.android.architecture.ext.viewModel

class EditProfileFragment : Fragment() {

    private val editProfileViewModel by viewModel<EditProfileViewModel>()
    private lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity?.supportActionBar?.title = "Edit Profile"
        setHasOptionsMenu(true)

        editProfileViewModel.progress.observe(this, Observer {
            it?.let {
                Utils.showProgressBar(rootView.progressBar, it)
            }
        })

        rootView.buttonUpdate.setOnClickListener {
            editProfileViewModel.updateUser(rootView.firstName.text.toString(), rootView.lastName.text.toString())
        }

        editProfileViewModel.message.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        return rootView
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
