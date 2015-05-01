package org.republica.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.republica.R;
import org.republica.activities.PersonInfoActivity;
import org.republica.db.DatabaseManager;
import org.republica.loaders.SimpleCursorLoader;
import org.republica.model.Person;

public class PersonsListFragment extends SmoothListFragment implements LoaderCallbacks<Cursor> {

    private static final int PERSONS_LOADER_ID = 1;

    private PersonsAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new PersonsAdapter(getActivity());
        setListAdapter(adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setFastScrollEnabled(true);
        setEmptyText(getString(R.string.no_data));
        setListShown(false);

        getLoaderManager().initLoader(PERSONS_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new PersonsLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            adapter.swapCursor(data);
        }

        setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Person person = adapter.getItem(position);
        Intent intent = new Intent(getActivity(), PersonInfoActivity.class).putExtra(PersonInfoActivity.SPEAKER, person);
        startActivity(intent);
    }

    private static class PersonsLoader extends SimpleCursorLoader {

        public PersonsLoader(Context context) {
            super(context);
        }

        @Override
        protected Cursor getCursor() {
            return DatabaseManager.getInstance().getPersons();
        }
    }

    private static class PersonsAdapter extends CursorAdapter implements SectionIndexer {

        private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        private final LayoutInflater inflater;
        private final AlphabetIndexer indexer;

        public PersonsAdapter(Context context) {
            super(context, null, 0);
            inflater = LayoutInflater.from(context);
            indexer = new AlphabetIndexer(null, DatabaseManager.PERSON_NAME_COLUMN_INDEX, ALPHABET);
        }

        @Override
        public Person getItem(int position) {
            return DatabaseManager.toPerson((Cursor) super.getItem(position));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

            ViewHolder holder = new ViewHolder();
            holder.textView = (TextView) view.findViewById(android.R.id.text1);
            view.setTag(holder);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.person = DatabaseManager.toPerson(cursor, holder.person);
            holder.textView.setText(holder.person.getName());
        }

        @Override
        public Cursor swapCursor(Cursor newCursor) {
            indexer.setCursor(newCursor);
            return super.swapCursor(newCursor);
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            return indexer.getPositionForSection(sectionIndex);
        }

        @Override
        public int getSectionForPosition(int position) {
            return indexer.getSectionForPosition(position);
        }

        @Override
        public Object[] getSections() {
            return indexer.getSections();
        }

        private static class ViewHolder {
            public TextView textView;
            public Person person;
        }
    }
}
