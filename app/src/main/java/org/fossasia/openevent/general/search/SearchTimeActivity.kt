package org.fossasia.openevent.general.search

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_search_time.*
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

class SearchTimeActivity : AppCompatActivity() {
    private val searchTimeViewModel by viewModel<SearchTimeViewModel>()
    private val TO_SEARCH: String = "ToSearchFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_time)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.supportActionBar?.title = ""
        setCurrentChoice(intent.getStringExtra("value"))

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

        anytimeTextView.setOnClickListener {
            searchTimeViewModel.saveDate(ANYTIME)
            redirectToSearch()
        }

        todayTextView.setOnClickListener {
            searchTimeViewModel.saveDate(getSimpleFormattedDate(calendar.time))
            calendar.add(Calendar.DATE, 1)
            searchTimeViewModel.saveNextDate(getSimpleFormattedDate(calendar.time))
            searchTimeViewModel.saveDate(TODAY)
            redirectToSearch()
        }

        tomorrowTextView.setOnClickListener {
            calendar.add(Calendar.DATE, 1)
            searchTimeViewModel.saveNextDate(getSimpleFormattedDate(calendar.time))
            calendar.add(Calendar.DATE, 1)
            searchTimeViewModel.saveNextToNextDate(getSimpleFormattedDate(calendar.time))
            searchTimeViewModel.saveDate(TOMORROW)
            redirectToSearch()
        }

        thisWeekendTextView.setOnClickListener {
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

        nextMonthTextView.setOnClickListener {
            val today = calendar.get(Calendar.DAY_OF_MONTH)
            val offset = 30 - today
            calendar.add(Calendar.DATE, offset)
            searchTimeViewModel.saveNextMonth(getSimpleFormattedDate(calendar.time))
            calendar.add(Calendar.MONTH, 1)
            searchTimeViewModel.saveNextToNextMonth(getSimpleFormattedDate(calendar.time))
            searchTimeViewModel.saveDate(NEXT_MONTH)
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

    private fun setCurrentChoice(value : String){
        when(value){
            ANYTIME -> anytimeTextView.setCheckMarkDrawable(R.drawable.ic_checked)
            TODAY -> todayTextView.setCheckMarkDrawable(R.drawable.ic_checked)
            TOMORROW -> tomorrowTextView.setCheckMarkDrawable(R.drawable.ic_checked)
            THIS_WEEKEND -> thisWeekendTextView.setCheckMarkDrawable(R.drawable.ic_checked)
            NEXT_MONTH-> nextMonthTextView.setCheckMarkDrawable(R.drawable.ic_checked)
            else -> timeTextView.setCheckMarkDrawable(R.drawable.ic_checked)
        }
    }
}
