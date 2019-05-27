package org.fossasia.openevent.general.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_welcome.view.currentLocation
import kotlinx.android.synthetic.main.fragment_welcome.view.pickCityButton
import kotlinx.android.synthetic.main.fragment_welcome.view.skip
import org.fossasia.openevent.general.R

class WelcomeFragment : Fragment() {
    private lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_welcome, container, false)

        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        rootView.currentLocation.visibility = View.GONE

        rootView.pickCityButton.setOnClickListener {
            findNavController().navigate(WelcomeFragmentDirections.actionWelcomeToSearch())
        }

        rootView.skip.setOnClickListener {
            findNavController().navigate(WelcomeFragmentDirections.actionWelcomeToEvents())
        }

        return rootView
    }
}
