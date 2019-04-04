package org.fossasia.openevent.general.search

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_search_time.view.anytimeTextView
import kotlinx.android.synthetic.main.fragment_search_time.view.todayTextView
import kotlinx.android.synthetic.main.fragment_search_time.view.tomorrowTextView
import kotlinx.android.synthetic.main.fragment_search_time.view.thisWeekendTextView
import kotlinx.android.synthetic.main.fragment_search_time.view.nextMonthTextView
import kotlinx.android.synthetic.main.fragment_search_time.view.timeTextView
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EventUtils.getSimpleFormattedDate
import java.util.Calendar
import org.fossasia.openevent.general.utils.Utils.setToolbar

const val ANYTIME = "Anytime"
const val TODAY = "Today"
const val TOMORROW = "Tomorrow"
const val THIS_WEEKEND = "This weekend"
const val NEXT_MONTH = "In the next month"

class SearchTimeFragment : Fragment() {
    private val safeArgs: SearchTimeFragmentArgs by navArgs()
    private lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_search_time, container, false)

        setToolbar(activity)
        setHasOptionsMenu(true)
        setCurrentChoice(safeArgs.time)

        val calendar = Calendar.getInstance()

        val date = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            calendar.add(Calendar.DATE, 1)
            redirectToSearch(getSimpleFormattedDate(calendar.time))
        }

        rootView.anytimeTextView.setOnClickListener {
            redirectToSearch(ANYTIME)
        }

        rootView.todayTextView.setOnClickListener {
            redirectToSearch(TODAY)
        }

        rootView.tomorrowTextView.setOnClickListener {
            redirectToSearch(TOMORROW)
        }

        rootView.thisWeekendTextView.setOnClickListener {
            redirectToSearch(THIS_WEEKEND)
        }

        rootView.nextMonthTextView.setOnClickListener {
            redirectToSearch(NEXT_MONTH)
        }

        rootView.timeTextView.setOnClickListener {
            DatePickerDialog(requireContext(), date, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        return rootView
    }

    private fun redirectToSearch(time: String) {
        val args = SearchFragmentArgs.Builder().setStringSavedDate(time).build().toBundle()
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.eventsFragment, false).build()
        Navigation.findNavController(rootView).navigate(R.id.searchFragment, args, navOptions)
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

    private fun setCurrentChoice(value: String?) {
        when (value) {
            ANYTIME -> rootView.anytimeTextView.setCheckMarkDrawable(R.drawable.ic_checked)
            TODAY -> rootView.todayTextView.setCheckMarkDrawable(R.drawable.ic_checked)
            TOMORROW -> rootView.tomorrowTextView.setCheckMarkDrawable(R.drawable.ic_checked)
            THIS_WEEKEND -> rootView.thisWeekendTextView.setCheckMarkDrawable(R.drawable.ic_checked)
            NEXT_MONTH -> rootView.nextMonthTextView.setCheckMarkDrawable(R.drawable.ic_checked)
            else -> rootView.timeTextView.setCheckMarkDrawable(R.drawable.ic_checked)
        }
    }
}
