package org.fossasia.openevent.general;

/**
 * Created by harsimar on 20/05/18.
 */

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.fossasia.openevent.general.model.Attributes;
import org.fossasia.openevent.general.model.Event;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventsRecyclerAdapter extends RecyclerView.Adapter<EventsRecyclerAdapter.EventViewHolder> {
    private Context context;

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.all_events_card)
        CardView cv;
        @BindView(R.id.all_events_card_image)
        ImageView eventImage;
        @BindView(R.id.all_events_card_event_name)
        TextView eventNameTv;
        @BindView(R.id.description)
        TextView description;
        @BindView(R.id.date)
        TextView startsAtDay;
        @BindView(R.id.year)
        TextView startsAtYear;
        @BindView(R.id.month)
        TextView startsAtMonth;

        EventViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

    List<Event> events;

    EventsRecyclerAdapter(Context context) {
        this.context = context;
        events = new ArrayList<>();
    }

    public void addAll(List<Event> eventList) {
        this.events.addAll(eventList);
    }

    @NonNull
    @Override
    public EventsRecyclerAdapter.EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_events, parent, false);
        EventViewHolder eventViewHolder = new EventViewHolder(v);
        return eventViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventsRecyclerAdapter.EventViewHolder holder, int position) {

        Attributes attributes = events.get(position).getAttributes();
        holder.eventNameTv.setText(attributes.getName());
        holder.description.setText(attributes.getDescription());

        String[] splitDay = dateFormat(attributes).split(" ");
        holder.startsAtDay.setText(splitDay[0]);
        holder.startsAtMonth.setText(splitDay[1].toUpperCase());
        holder.startsAtYear.setText(splitDay[2]);

        //Picasso
        if (attributes.getOriginalImageUrl() != null) {
            Picasso.with(holder.eventImage.getContext())
                    .load(Uri.parse(attributes.getOriginalImageUrl()))
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.eventImage);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public String dateFormat(Attributes attributes) {
        String returnString = "";
        String string = attributes.getStartsAt().substring(0, 9);
        DateFormat format = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(string);
            SimpleDateFormat dt1 = new SimpleDateFormat("dd MMM yyyy");
            returnString = dt1.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return returnString;
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}