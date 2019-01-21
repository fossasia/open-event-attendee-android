package org.fossasia.openevent.general.search

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_search_time.view.anytimeTextView
import kotlinx.android.synthetic.main.fragment_search_time.view.todayTextView
import kotlinx.android.synthetic.main.fragment_search_time.view.tomorrowTextView
import kotlinx.android.synthetic.main.fragment_search_time.view.thisWeekendTextView
import kotlinx.android.synthetic.main.fragment_search_time.view.nextMonthTextView
import kotlinx.android.synthetic.main.fragment_search_time.view.timeTextView
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EventUtils.getSimpleFormattedDate
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar

const val ANYTIME = "Anytime"
const val TODAY = "Today"
const val TOMORROW = "Tomorrow"
const val THIS_WEEKEND = "This Weekend"
const val NEXT_MONTH = "In the next month"
const val TO_SEARCH = "ToSearchFragment"

class SearchTimeFragment : Fragment() {
    private val searchTimeViewModel by viewModel<SearchTimeViewModel>()
    private lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_search_time, container, false)

        val thisActivity = activity
        if (thisActivity is AppCompatActivity) {
            thisActivity.supportActionBar?.title = ""
            thisActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
        setCurrentChoice(arguments?.getString(SEARCH_TIME))

        val calendar = Calendar.getInstance()

        val date = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            searchTimeViewModel.saveDate(getSimpleFormattedDate(calendar.time))
            calendar.add(Calendar.DATE, 1)
            searchTimeViewModel.saveNextDate(getSimpleFormattedDate(calendar.time))
            redirectToSearch()
        }

        rootView.anytimeTextView.setOnClickListener {
            searchTimeViewModel.saveDate(ANYTIME)
            redirectToSearch()
        }

        rootView.todayTextView.setOnClickListener {
            searchTimeViewModel.saveDate(getSimpleFormattedDate(calendar.time))
            calendar.add(Calendar.DATE, 1)
            searchTimeViewModel.saveNextDate(getSimpleFormattedDate(calendar.time))
            searchTimeViewModel.saveDate(TODAY)
            redirectToSearch()
        }

        rootView.tomorrowTextView.setOnClickListener {
            calendar.add(Calendar.DATE, 1)
            searchTimeViewModel.saveNextDate(getSimpleFormattedDate(calendar.time))
            calendar.add(Calendar.DATE, 1)
            searchTimeViewModel.saveNextToNextDate(getSimpleFormattedDate(calendar.time))
            searchTimeViewModel.saveDate(TOMORROW)
            redirectToSearch()
        }

        rootView.thisWeekendTextView.setOnClickListener {
            val today = calendar.get(Calendar.DAY_OF_WEEK)
            if (today != Calendar.SATURDAY) {
                val offset = Calendar.SATURDAY - today
                calendar.add(Calendar.DATE, offset)
            }
            searchTimeViewModel.saveWeekendDate(getSimpleFormattedDate(calendar.time))
            calendar.add(Calendar.DATE, 1)
            searchTimeViewModel.saveNextToWeekendDate(getSimpleFormattedDate(calendar.time))
            searchTimeViewModel.saveDate(THIS_WEEKEND)
            redirectToSearch()
        }

        rootView.nextMonthTextView.setOnClickListener {
            val today = calendar.get(Calendar.DAY_OF_MONTH)
            val offset = 30 - today
            calendar.add(Calendar.DATE, offset)
            searchTimeViewModel.saveNextMonth(getSimpleFormattedDate(calendar.time))
            calendar.add(Calendar.MONTH, 1)
            searchTimeViewModel.saveNextToNextMonth(getSimpleFormattedDate(calendar.time))
            searchTimeViewModel.saveDate(NEXT_MONTH)
            redirectToSearch()
        }

        rootView.timeTextView.setOnClickListener {
            DatePickerDialog(context, date, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        return rootView
    }

    private fun redirectToSearch() {
        val intent = Intent(context, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val bundle = Bundle()
        bundle.putBoolean(TO_SEARCH, true)
        intent.putExtras(bundle)
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        activity?.finish()
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
