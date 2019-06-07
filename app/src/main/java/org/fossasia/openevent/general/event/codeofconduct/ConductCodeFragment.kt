package org.fossasia.openevent.general.event.codeofconduct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_conduct_code.view.*
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EventDetailsViewModel
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.stripHtml
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConductCodeFragment : Fragment() {
    private lateinit var rootView: View
    private val eventViewModel by viewModel<EventDetailsViewModel>()
    private val safeArgs: ConductCodeFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_conduct_code, container, false)

        setToolbar(activity, getString(R.string.code_of_conduct))
        setHasOptionsMenu(true)

        eventViewModel.loadEvent(safeArgs.eventId)

        eventViewModel.event
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.conductCodeText.text = it.codeOfConduct.stripHtml()
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
