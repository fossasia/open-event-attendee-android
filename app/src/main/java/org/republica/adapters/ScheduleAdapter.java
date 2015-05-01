package org.republica.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.republica.R;
import org.republica.model.FossasiaEvent;

import java.util.ArrayList;

/**
 * Created by Abhishek on 20/02/15.
 */
public class ScheduleAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<FossasiaEvent> mFossasiaEventList;
    private LayoutInflater mInflater;

    public ScheduleAdapter(Context context, ArrayList<FossasiaEvent> fossasiaEventList) {
        this.mContext = context;
        this.mFossasiaEventList = fossasiaEventList;
    }

    @Override
    public int getCount() {
        return mFossasiaEventList.size();
    }

    @Override
    public FossasiaEvent getItem(int position) {
        return mFossasiaEventList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mFossasiaEventList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        if (mInflater == null) {
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        View row;
        if (convertView == null) {
            row = mInflater.inflate(R.layout.item_schedule_event, parent, false);
        } else {
            row = convertView;
        }

        ScheduleHolder holder = new ScheduleHolder();
        holder.dateTime = (TextView) row.findViewById(R.id.time);
        holder.title = (TextView) row.findViewById(R.id.text);
        FossasiaEvent fossasiaEvent = getItem(position);
        holder.dateTime.setText(fossasiaEvent.getStartTime());

        row.setTag(fossasiaEvent.getId());

        //
        String title = fossasiaEvent.getTitle();
        String subTitle = "";
        if (fossasiaEvent.getPersonSummary() != null && !fossasiaEvent.getPersonSummary().equals("")) {
            subTitle = "\n" + fossasiaEvent.getPersonSummary();
        }
        SpannableString styledString = new SpannableString(title + subTitle);
        styledString.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), 0);
        holder.title.setText(styledString);
        return row;
    }

    public static class ScheduleHolder {
        TextView title;
        TextView dateTime;

    }
}
