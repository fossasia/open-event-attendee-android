package org.republica.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.republica.R;
import org.republica.db.DatabaseManager;
import org.republica.loaders.BookmarkStatusLoader;
import org.republica.loaders.LocalCacheLoader;
import org.republica.model.FossasiaEvent;
import org.republica.model.Link;
import org.republica.model.Speaker;
import org.republica.model.Venue;
import org.republica.utils.DateUtils;
import org.republica.utils.StringUtils;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;


public class FossasiaEventDetailsFragment extends Fragment {

    private static final int BOOKMARK_STATUS_LOADER_ID = 1;
    private static final int EVENT_DETAILS_LOADER_ID = 2;
    private static final String ARG_EVENT = "event";
    private static final DateFormat TIME_DATE_FORMAT = DateUtils.getTimeDateFormat();
    private FossasiaEvent event;
    private int personsCount = 1;
    private final LoaderCallbacks<EventDetails> eventDetailsLoaderCallbacks = new LoaderCallbacks<EventDetails>() {

        @Override
        public Loader<EventDetails> onCreateLoader(int id, Bundle args) {
            return new EventDetailsLoader(getActivity(), event);
        }

        @Override
        public void onLoadFinished(Loader<EventDetails> loader, EventDetails data) {
            // 1. Persons
            if (data.persons != null) {
                personsCount = data.persons.size();
                if (personsCount > 0) {
                    // Build a list of clickable persons
                    SpannableStringBuilder sb = new SpannableStringBuilder();
                    int length = 0;
                    for (String person : data.persons) {
                        if (length != 0) {
                            sb.append(", ");
                        }
                        String name = person;
                        sb.append(name);
                        length = sb.length();
                        // TODO: Fix this,
//						sb.setSpan(new PersonClickableSpan(person), length - name.length(), length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        sb.setSpan(name, length - name.length(), length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    holder.personsTextView.setText(sb);
                    holder.personsTextView.setVisibility(View.VISIBLE);
                }
            }
//TODO: Understand and fix this.
            // 2. Links
            // Keep the first view in links container (title) only
//			int linkViewCount = holder.linksContainer.getChildCount();
//			if (linkViewCount > 1) {
//				holder.linksContainer.removeViews(1, linkViewCount - 1);
//			}
//			if ((data.links != null) && (data.links.size() > 0)) {
//				holder.linksContainer.setVisibility(View.VISIBLE);
//				for (Link link : data.links) {
//					View view = holder.inflater.inflate(R.layout.item_link, holder.linksContainer, false);
//					TextView tv = (TextView) view.findViewById(R.id.description);
//					tv.setText(link.getDescription());
//					view.setOnClickListener(new LinkClickListener(link));
//					holder.linksContainer.addView(view);
//					// Add a list divider
//					holder.inflater.inflate(R.layout.list_divider, holder.linksContainer, true);
//				}
//			} else {
//				holder.linksContainer.setVisibility(View.GONE);
//			}
        }

        @Override
        public void onLoaderReset(Loader<EventDetails> loader) {
        }
    };
    private Boolean isBookmarked;
    private final View.OnClickListener actionButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (isBookmarked != null) {
                new UpdateBookmarkAsyncTask(event).execute(isBookmarked);
            }
        }
    };
    private final LoaderCallbacks<Boolean> bookmarkStatusLoaderCallbacks = new LoaderCallbacks<Boolean>() {

        @Override
        public Loader<Boolean> onCreateLoader(int id, Bundle args) {
            return new BookmarkStatusLoader(getActivity(), event);
        }

        @Override
        public void onLoadFinished(Loader<Boolean> loader, Boolean data) {
            isBookmarked = data;
            updateOptionsMenu();
        }

        @Override
        public void onLoaderReset(Loader<Boolean> loader) {
        }
    };
    private ViewHolder holder;

    private MenuItem bookmarkMenuItem;
    private ImageView actionButton;

    public static FossasiaEventDetailsFragment newInstance(FossasiaEvent event, String map) {
        FossasiaEventDetailsFragment f = new FossasiaEventDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_EVENT, event);
        args.putString("MAP", map);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        event = getArguments().getParcelable(ARG_EVENT);
    }

    public FossasiaEvent getEvent() {
        return event;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        holder = new ViewHolder();
        holder.inflater = inflater;

        ((TextView) view.findViewById(R.id.title)).setText(event.getTitle());
        TextView textView = (TextView) view.findViewById(R.id.subtitle);
        String text = event.getSubTitle();
        if (TextUtils.isEmpty(text)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(text);
        }

        final MovementMethod linkMovementMethod = LinkMovementMethod.getInstance();

        // Set the persons summary text first; replace it with the clickable text when the loader completes
        holder.personsTextView = (TextView) view.findViewById(R.id.persons);
        String personsSummary = event.getPersonSummary();
        if (TextUtils.isEmpty(personsSummary)) {
            holder.personsTextView.setVisibility(View.GONE);
        } else {
//            holder.personsTextView.setText(personsSummary);
//            holder.personsTextView.setMovementMethod(linkMovementMethod);
            holder.personsTextView.setVisibility(View.VISIBLE);
        }
        // TODO: Fix keynote in spreadsheet and here
        ((TextView) view.findViewById(R.id.track)).setText(event.getTrack());
        // TODO: Use date from Date object, not from string
//		Date startTime = event.getStartTime();
//		Date endTime = event.getEndTime();
        String startTime = event.getStartTime();
        text = String.format("%1$s, %2$s", event.getDay(), (startTime != null) ? startTime : "?");
        ((TextView) view.findViewById(R.id.time)).setText(text);
        TextView moderator = (TextView)view.findViewById(R.id.moderators);
        if(event.getModerator() == null || event.getModerator().equals("")) {
            moderator.setVisibility(View.GONE);
        }
        else {
            moderator.setText("Moderator: " + event.getModerator());
        }
        final String venue = event.getVenue();
        TextView roomTextView = (TextView) view.findViewById(R.id.room);
        Spannable roomText = new SpannableString(String.format("%1$s", venue));
        final int roomImageResId = getResources().getIdentifier(StringUtils.roomNameToResourceName(venue), "drawable", getActivity().getPackageName());
        // If the room image exists, make the room text clickable to display it
        roomText.setSpan(new UnderlineSpan(), 0, roomText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        final String map = getArguments().getString("MAP");

        roomTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                DatabaseManager db = DatabaseManager.getInstance();
                final Venue ven = db.getVenueFromTrack(event.getTrack());
                LayoutInflater inflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialogView = inflater.inflate(R.layout.venue_dialog, null);
                alertDialogBuilder.setView(dialogView);
                final AlertDialog dialog = alertDialogBuilder.create();
                dialog.getWindow().setWindowAnimations(R.style.VenueDialogAnimation);
                TextView ok = (TextView) dialogView.findViewById(R.id.venue_okay);
                TextView mapLink = (TextView) dialogView.findViewById(R.id.venue_map);
                TextView venueLink = (TextView) dialogView.findViewById(R.id.venue_link);
                TextView venueName = (TextView) dialogView.findViewById(R.id.venue_name);
                TextView venueAddress = (TextView) dialogView.findViewById(R.id.venue_address);
                venueAddress.setText(ven.getAddress());
                TextView venueRoom = (TextView) dialogView.findViewById(R.id.venue_room);
                venueRoom.setText(ven.getRoom());
                TextView howToReach = (TextView) dialogView.findViewById(R.id.venue_how_to_reach);
                howToReach.setText(ven.getHowToReach());

                venueName.setText(ven.getVenue());
                venueLink.setText(ven.getLink());
                venueLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ven.getLink()));
                        startActivity(intent);
                    }
                });
                mapLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
                        startActivity(intent);
                    }
                });
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
        roomTextView.setFocusable(true);
        roomTextView.setText(roomText);

        textView = (TextView) view.findViewById(R.id.abstract_text);
        text = event.getAbstractText();
        if (TextUtils.isEmpty(text)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(StringUtils.trimEnd(Html.fromHtml(text)));
            textView.setMovementMethod(linkMovementMethod);
        }
        textView = (TextView) view.findViewById(R.id.description);
        text = event.getDescription();
        if (TextUtils.isEmpty(text)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(StringUtils.trimEnd(Html.fromHtml(text)));
            textView.setMovementMethod(linkMovementMethod);
        }

        holder.linksContainer = (ViewGroup) view.findViewById(R.id.links_container);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        if (activity instanceof FloatingActionButtonProvider) {
            actionButton = ((FloatingActionButtonProvider) activity).getActionButton();
            if (actionButton != null) {
                actionButton.setOnClickListener(actionButtonClickListener);
            }
        }

        // Ensure the actionButton is initialized before creating the options menu
        setHasOptionsMenu(true);

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(BOOKMARK_STATUS_LOADER_ID, null, bookmarkStatusLoaderCallbacks);
        loaderManager.initLoader(EVENT_DETAILS_LOADER_ID, null, eventDetailsLoaderCallbacks);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        holder = null;
        if (actionButton != null) {
            // Clear the reference to this fragment
            actionButton.setOnClickListener(null);
            actionButton = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.event, menu);
        menu.findItem(R.id.share).setIntent(getShareChooserIntent());
        bookmarkMenuItem = menu.findItem(R.id.bookmark);
        if (actionButton != null) {
            bookmarkMenuItem.setEnabled(false).setVisible(false);
        }
        updateOptionsMenu();
    }

    private Intent getShareChooserIntent() {
        return ShareCompat.IntentBuilder.from(getActivity())
                .setSubject(String.format("%1$s (FOSDEM)", event.getTitle()))
                .setType("text/plain")
//				.setText(String.format("%1$s %2$s #FOSDEM", event.getTitle(), event.getUrl()))
                .setChooserTitle(R.string.share)
                .createChooserIntent();
    }

    private void updateOptionsMenu() {
        if (actionButton != null) {
            // Action Button is used as bookmark button

            if (isBookmarked == null) {
                actionButton.setEnabled(false);
            } else {
                actionButton.setEnabled(true);

                if (isBookmarked) {
                    actionButton.setContentDescription(getString(R.string.remove_bookmark));
                    actionButton.setImageResource(R.drawable.ic_bookmark_white_24dp);
                } else {
                    actionButton.setContentDescription(getString(R.string.add_bookmark));
                    actionButton.setImageResource(R.drawable.ic_bookmark_outline_white_24dp);
                }
            }
        } else {
            // Standard menu item is used as bookmark button

            if (bookmarkMenuItem != null) {
                if (isBookmarked == null) {
                    bookmarkMenuItem.setEnabled(false);
                } else {
                    bookmarkMenuItem.setEnabled(true);

                    if (isBookmarked) {
                        bookmarkMenuItem.setTitle(R.string.remove_bookmark);
                        bookmarkMenuItem.setIcon(R.drawable.ic_bookmark_white_24dp);
                    } else {
                        bookmarkMenuItem.setTitle(R.string.add_bookmark);
                        bookmarkMenuItem.setIcon(R.drawable.ic_bookmark_outline_white_24dp);
                    }
                }
            }
        }
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        bookmarkMenuItem = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bookmark:
                if (isBookmarked != null) {
                    new UpdateBookmarkAsyncTask(event).execute(isBookmarked);
                }
                return true;
            case R.id.add_to_agenda:
                addToAgenda();
                return true;
        }
        return false;
    }

    @SuppressLint("InlinedApi")
    private void addToAgenda() {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra(CalendarContract.Events.TITLE, event.getTitle());
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, event.getVenue());
        String description = event.getAbstractText();
        if (TextUtils.isEmpty(description)) {
            description = event.getDescription();
        }
        // Strip HTML
        description = StringUtils.trimEnd(Html.fromHtml(description)).toString();
        // Add speaker info if available
        if (personsCount > 0) {
            description = String.format("%1$s: %2$s\n\n%3$s", getResources().getQuantityString(R.plurals.speakers, personsCount), event.getPersonSummary(),
                    description);
        }
        intent.putExtra(CalendarContract.Events.DESCRIPTION, description);

        Date time = StringUtils.StringToDate(event.getDate(), event.getStartTime());
        if (time != null) {
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, time.getTime());
        }
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), R.string.calendar_not_found, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Interface implemented by container activities
     */
    public interface FloatingActionButtonProvider {
        // May return null
        ImageView getActionButton();
    }

    private static class EventDetails {
        List<String> persons;
        List<String> links;
    }

    private static class ViewHolder {
        LayoutInflater inflater;
        TextView personsTextView;
        ViewGroup linksContainer;
    }

    private static class UpdateBookmarkAsyncTask extends AsyncTask<Boolean, Void, Void> {

        private final FossasiaEvent event;

        public UpdateBookmarkAsyncTask(FossasiaEvent event) {
            this.event = event;
        }

        @Override
        protected Void doInBackground(Boolean... remove) {
            if (remove[0]) {
                DatabaseManager.getInstance().removeBookmark(event);
            } else {
                DatabaseManager.getInstance().addBookmark(event);
            }
            return null;
        }
    }

    private static class EventDetailsLoader extends LocalCacheLoader<EventDetails> {

        private final FossasiaEvent event;

        public EventDetailsLoader(Context context, FossasiaEvent event) {
            super(context);
            this.event = event;
        }

        @Override
        public EventDetails loadInBackground() {
            EventDetails result = new EventDetails();
            DatabaseManager dbm = DatabaseManager.getInstance();
            result.persons = event.getKeyNoteList();
            // TODO: Fix the link's attached with each speaker
            result.links = event.getKeyNoteList();
            return result;
        }
    }

    private static class PersonClickableSpan extends ClickableSpan {

        private final Speaker person;

        public PersonClickableSpan(Speaker person) {
            this.person = person;
        }

        @Override
        public void onClick(View v) {
            // TODO: uncomment this code and fix it, to display speaker info.
//			Context context = v.getContext();
//			Intent intent = new Intent(context, PersonInfoActivity.class).putExtra(PersonInfoActivity.EXTRA_PERSON, person);
//			context.startActivity(intent);
        }
    }

    private static class LinkClickListener implements View.OnClickListener {

        private final Link link;

        public LinkClickListener(Link link) {
            this.link = link;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link.getUrl()));
            v.getContext().startActivity(intent);
        }
    }
}