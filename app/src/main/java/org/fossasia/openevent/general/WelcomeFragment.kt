package org.fossasia.openevent.general

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_welcome.view.pickCityButton
import org.fossasia.openevent.general.utils.Utils

class WelcomeFragment : Fragment() {
    private lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_welcome, container, false)
        val thisActivity = activity
        if (thisActivity is AppCompatActivity)
            thisActivity.supportActionBar?.hide()
        rootView.pickCityButton.setOnClickListener {
            Navigation.findNavController(rootView).navigate(R.id.searchLocationFragment, null, Utils.getAnimSlide())
        }
        return rootView
    }
}
