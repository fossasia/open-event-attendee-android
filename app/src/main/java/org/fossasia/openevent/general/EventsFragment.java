package org.fossasia.openevent.general;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.fossasia.openevent.general.rest.ApiClient;
import org.fossasia.openevent.general.utils.ConstantStrings;
import org.fossasia.openevent.general.utils.SharedPreferencesUtil;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import timber.log.Timber;

public class EventsFragment extends Fragment {

    private static final String app = "application/vnd.api+json";
    private RecyclerView recyclerView;
    private EventsRecyclerAdapter eventsRecyclerAdapter;
    private ProgressBar progressBar;
    private LinearLayoutManager linearLayoutManager;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String token = SharedPreferencesUtil.getString(ConstantStrings.TOKEN, null);
        token = "JWT "+ token;

        ApiClient.setToken(token);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        progressBar = view.findViewById(R.id.progressHeader);
        progressBar.setIndeterminate(true);

        recyclerView = view.findViewById(R.id.events_recycler);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        eventsRecyclerAdapter = new EventsRecyclerAdapter();
        recyclerView.setAdapter(eventsRecyclerAdapter);
        recyclerView.setNestedScrollingEnabled(false);

        compositeDisposable.add(ApiClient.getEventApi().getEvents(app)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(events -> {
                    Timber.d("Response Success");
                    eventsRecyclerAdapter.addAll(events.getEventList());

                    progressBarHandle();
                    addAnim();
                    notifyItems();

                    Timber.d("Fetched events of size %s", eventsRecyclerAdapter.getItemCount());
                }, throwable -> Timber.e(throwable, "Fetching Failed")));

        return view;
    }

    private void notifyItems() {
        int firstVisible = linearLayoutManager.findFirstVisibleItemPosition();
        int lastVisible = linearLayoutManager.findLastVisibleItemPosition();

        int itemsChanged = lastVisible - firstVisible + 1; // + 1 because we start count items from 0
        int start = firstVisible - itemsChanged > 0 ? firstVisible - itemsChanged : 0;

        eventsRecyclerAdapter.notifyItemRangeChanged(start, itemsChanged + itemsChanged);
    }

    private void addAnim() {
        //item animator
        SlideInUpAnimator slideup = new SlideInUpAnimator();
        slideup.setAddDuration(500);
        recyclerView.setItemAnimator(slideup);
    }

    private void progressBarHandle() {
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}