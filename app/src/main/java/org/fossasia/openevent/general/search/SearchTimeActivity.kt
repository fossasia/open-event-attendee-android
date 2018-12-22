package org.fossasia.openevent.general.search

import android.app.DatePickerDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_search_time.*
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.koin.android.architecture.ext.viewModel
import java.text.SimpleDateFormat
import java.util.*

const val ANYTIME = "Anytime"
const val TODAY  = "Today"
const val TOMORROW = "Tomorrow"
const val WEEKEND = "This Weekend"
const val MONTH = "In the next month"


class SearchTimeActivity : AppCompatActivity() {
    private val searchTimeViewModel by viewModel<SearchTimeViewModel>()
    private val TO_SEARCH: String = "ToSearchFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_time)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.supportActionBar?.title = ""

        val calendar = Calendar.getInstance()
        val dateFormat = "yyyy-MM-dd"
        val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.US)


        val date = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            searchTimeViewModel.saveDate(simpleDateFormat.format(calendar.time))
            calendar.add(Calendar.DATE, 1)
            searchTimeViewModel.saveNextDate(simpleDateFormat.format(calendar.time))
            redirectToSearch()
        }

        anytimeTextView.setOnClickListener {
            searchTimeViewModel.saveDate(ANYTIME)
            redirectToSearch()
        }

        todayTextView.setOnClickListener {
            searchTimeViewModel.saveDate(simpleDateFormat.format(calendar.time))
            calendar.add(Calendar.DATE, 1)
            searchTimeViewModel.saveNextDate(simpleDateFormat.format(calendar.time))
            searchTimeViewModel.saveDate(TODAY)
            redirectToSearch()
        }

        tomorrowTextView.setOnClickListener {
            calendar.add(Calendar.DATE,1)
            searchTimeViewModel.saveNextDate(simpleDateFormat.format(calendar.time))
            calendar.add(Calendar.DATE,1)
            searchTimeViewModel.saveNextToNextDate(simpleDateFormat.format(calendar.time))
            searchTimeViewModel.saveDate(TOMORROW)
            redirectToSearch()
        }

        thisWeekendTextView.setOnClickListener {
            val today = calendar.get(Calendar.DAY_OF_WEEK)
            if (today != Calendar.SATURDAY) {
                val offset = Calendar.SATURDAY - today
                calendar.add(Calendar.DATE, offset)
            }
            searchTimeViewModel.saveWeekendDate(simpleDateFormat.format(calendar.time))
            calendar.add(Calendar.DATE, 1)
            searchTimeViewModel.saveNextToWeekendDate(simpleDateFormat.format(calendar.time))
            searchTimeViewModel.saveDate(WEEKEND)
            redirectToSearch()
        }

        nextMonthTextView.setOnClickListener {
            val today = calendar.get(Calendar.DAY_OF_MONTH)
            val offset = 30-today
            calendar.add(Calendar.DATE, offset)
            searchTimeViewModel.saveNextMonth(simpleDateFormat.format(calendar.time))
            calendar.add(Calendar.MONTH, 1)
            searchTimeViewModel.saveNextToNextMonth(simpleDateFormat.format(calendar.time))
            searchTimeViewModel.saveDate(MONTH)
            redirectToSearch()
        }

        anytimeTextView.setOnClickListener {
            searchTimeViewModel.saveDate(ANYTIME)
            redirectToSearch()
        }

        timeTextView.setOnClickListener {
            DatePickerDialog(this, date, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun redirectToSearch() {
        val intent = Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val bundle = Bundle()
        bundle.putBoolean(TO_SEARCH, true)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
