package com.sixbynine.waterwheels.main;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.sixbynine.waterwheels.BaseFragment;
import com.sixbynine.waterwheels.BuildConfig;
import com.sixbynine.waterwheels.R;
import com.sixbynine.waterwheels.abstracts.AbstractOnItemSelectedListener;
import com.sixbynine.waterwheels.data.OfferDbManager;
import com.sixbynine.waterwheels.events.FeedRequestFinishedEvent;
import com.sixbynine.waterwheels.manager.FacebookManager;
import com.sixbynine.waterwheels.model.Offer;
import com.sixbynine.waterwheels.model.Place;
import com.sixbynine.waterwheels.model.PlaceChoice;
import com.sixbynine.waterwheels.util.Logger;
import com.sixbynine.waterwheels.util.OfferUtils;
import com.sixbynine.waterwheels.util.Utils;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class MainFragment extends BaseFragment {

    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private View mMainLayout;
    private View mLoadingLayout;
    private View mNotWaterlooLayout;
    private Callback mCallback;
    private View mDisplayHeader;
    private View mChoiceHeader;
    private TextView mDisplayText;
    private TextView mTimeDisplayText;
    private Spinner mOriginSpinner;
    private Spinner mDestinationSpinner;
    private TextView mTimeStartChoice;
    private TextView mTimeEndChoice;
    private View mClearButton;

    private boolean mShouldChangeFilter;
    private MainFragmentState mState;

    public interface Callback {
        void onOfferClicked(Offer offer);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = Callback.class.cast(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mState = MainFragmentState.getState();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mMainLayout = view.findViewById(R.id.main_layout);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mListView = (ListView) view.findViewById(R.id.list_view);
        mLoadingLayout = view.findViewById(R.id.loading_layout);
        mNotWaterlooLayout = view.findViewById(R.id.not_waterloo_layout);

        view.findViewById(R.id.how_do_you_do).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://uwaterloo.ca/find-out-more/admissions")));
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (getActivity() != null) {
                    Offer offer = (Offer) parent.getItemAtPosition(position);
                    mCallback.onOfferClicked(offer);
                }
            }
        });

        if (BuildConfig.DEBUG) {
            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Offer offer = (Offer) parent.getItemAtPosition(position);
                    throw new BadParseException(offer.toString());
                }
            });
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utils.isNetworkAvailable(getContext())) {
                    mSwipeRefreshLayout.setRefreshing(true);
                    mListView.setEnabled(false);
                    FacebookManager.getInstance().refreshGroupPosts();
                } else {
                    Toast.makeText(getContext(), R.string.no_network_connection, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mDisplayHeader = view.findViewById(R.id.display_header);
        mChoiceHeader = view.findViewById(R.id.choice_header);
        mDisplayText = (TextView) view.findViewById(R.id.display_text);
        mTimeDisplayText = (TextView) view.findViewById(R.id.time_display_text);
        mOriginSpinner = (Spinner) mChoiceHeader.findViewById(R.id.origin_spinner);
        mDestinationSpinner = (Spinner) mChoiceHeader.findViewById(R.id.destination_spinner);

        final List<PlaceChoice> placeChoices = Place.getAllPlaceChoices();
        List<String> names = new ArrayList<>(placeChoices.size());
        for (PlaceChoice placeChoice : placeChoices) {
            names.add(placeChoice.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_2, names);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mOriginSpinner.setAdapter(adapter);
        if (mState.getOrigin() != null) {
            mOriginSpinner.setSelection(placeChoices.indexOf(mState.getOrigin()));
        }
        mOriginSpinner.setOnItemSelectedListener(new AbstractOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mState.isFilterOpen()) {
                    mState.setOrigin(placeChoices.get(position));
                    mShouldChangeFilter = true;
                    syncViews();
                }
            }
        });

        mClearButton = view.findViewById(R.id.btn_clear);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mState.setFilterOpen(false);
                mOriginSpinner.setSelection(0);
                mDestinationSpinner.setSelection(0);
                mState.setOrigin(placeChoices.get(0));
                mState.setDestination(placeChoices.get(0));
                mState.setTimeEnd(0);
                mState.setTimeStart(0);
                mShouldChangeFilter = true;
                syncViews();
            }
        });

        ArrayAdapter<String> destAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_2, names);
        destAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mDestinationSpinner.setAdapter(destAdapter);
        if (mState.getDestination() != null) {
            mDestinationSpinner.setSelection(placeChoices.indexOf(mState.getDestination()));
        }
        mDestinationSpinner.setOnItemSelectedListener(new AbstractOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mState.isFilterOpen()) {
                    mState.setDestination(placeChoices.get(position));
                    mShouldChangeFilter = true;
                    syncViews();
                }
            }
        });
        mSwipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary);

        mTimeStartChoice = (TextView) view.findViewById(R.id.date_time_start_choice);
        mTimeStartChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePickers(true);
            }
        });
        mTimeEndChoice = (TextView) view.findViewById(R.id.date_time_end_choice);
        mTimeEndChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePickers(false);
            }
        });

        syncViews();

        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (getActivity() != null) {
            menu.clear();
            getActivity().getMenuInflater().inflate(R.menu.menu_main_2, menu);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getAppCompatActivity().setSupportActionBar(mToolbar);
        getAppCompatActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                if (mState.isFilterOpen()) {
                    mDisplayHeader.setVisibility(View.VISIBLE);
                    mChoiceHeader.setVisibility(View.GONE);
                    mState.setFilterOpen(false);
                } else {
                    mChoiceHeader.setVisibility(View.VISIBLE);
                    mDisplayHeader.setVisibility(View.GONE);
                    mState.setFilterOpen(true);
                }
                syncViews();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onOffersLoaded(FeedRequestFinishedEvent event) {
        syncViews();
    }

    private void syncViews() {
        if (getContext() != null) {
            if (FacebookManager.getInstance().getStatus() == FacebookManager.Status.LOADED) {
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), R.string.content_updated, Toast.LENGTH_SHORT).show();
                }
                mLoadingLayout.setVisibility(View.GONE);
                mMainLayout.setVisibility(View.VISIBLE);

                if (mState.getTimeStart() < System.currentTimeMillis()) {
                    mState.setTimeStart(System.currentTimeMillis());
                }
                if (mState.getTimeEnd() < mState.getTimeStart()) {
                    mState.setTimeEnd(mState.getTimeStart() + TimeUnit.MILLISECONDS.convert(6, TimeUnit.DAYS));
                }

                MainFragmentState.saveState(mState);

                mTimeStartChoice.setText(OfferUtils.makePrettyTimestamp(getContext(), mState.getTimeStart()) + " - ");
                mTimeEndChoice.setText(OfferUtils.makePrettyTimestamp(getContext(), mState.getTimeEnd()));

                final PlaceChoice origin = mState.getOrigin();
                final PlaceChoice destination = mState.getDestination();
                Predicate<Offer> filter = new Predicate<Offer>() {
                    @Override
                    public boolean apply(@Nullable Offer offer) {
                        boolean match = true;
                        if (origin != null && destination != null && !origin.equals(destination)) {
                            match = match && origin.getPlaces().contains(offer.getOrigin())
                                    && destination.getPlaces().contains(offer.getDestination());
                        }
                        return match && offer.getTime() >= mState.getTimeStart() - 1000 * 60
                                && offer.getTime() <= mState.getTimeEnd() + 1000 * 60;
                    }
                };
                if (origin != null && destination != null && !origin.equals(destination)) {
                    mDisplayText.setText(origin.getName() + " to " + destination.getName());
                } else {
                    mDisplayText.setText(R.string.showing_all_rides);
                }

                if (mState.getTimeEnd() - mState.getTimeStart() >= TimeUnit.MILLISECONDS.convert(6, TimeUnit.DAYS) - TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES)) {
                    mTimeDisplayText.setVisibility(View.GONE);
                } else {
                    mTimeDisplayText.setVisibility(View.VISIBLE);
                    mTimeDisplayText.setText(mTimeStartChoice.getText().toString() + mTimeEndChoice.getText().toString());
                }


                if (mState.isFilterOpen()) {
                    mChoiceHeader.setVisibility(View.VISIBLE);
                    mDisplayHeader.setVisibility(View.GONE);
                } else {
                    mDisplayHeader.setVisibility(View.VISIBLE);
                    mChoiceHeader.setVisibility(View.GONE);
                }

                mListView.setEnabled(true);
                if (mListView.getAdapter() == null || mShouldChangeFilter) {
                    Iterable<Offer> offers = Iterables.filter(OfferDbManager.getInstance().getOffers(), filter);
                    mListView.setAdapter(new FeedAdapter(getContext(), ImmutableList.copyOf(offers)));
                    mShouldChangeFilter = false;
                }
                mNotWaterlooLayout.setVisibility(View.GONE);
            } else if (FacebookManager.getInstance().getStatus() == FacebookManager.Status.NOT_WATERLOO) {
                mLoadingLayout.setVisibility(View.GONE);
                mMainLayout.setVisibility(View.GONE);
                mNotWaterlooLayout.setVisibility(View.VISIBLE);
            } else {
                mLoadingLayout.setVisibility(View.VISIBLE);
                mMainLayout.setVisibility(View.GONE);
                mNotWaterlooLayout.setVisibility(View.GONE);
            }
        }
    }

    private void showDateTimePickers(final boolean start) {
        if (getActivity() == null) return;
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        final int month = c.get(Calendar.MONTH);
        final int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
                if (getActivity() == null) return;
                Logger.d("onDateSet: year=%d, month=%d, day=%d", year, monthOfYear, dayOfMonth);
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);

                // Create a new instance of TimePickerDialog and return it
                Dialog timeDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (getActivity() == null) return;
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);
                        long time = calendar.getTimeInMillis();
                        if (start) {
                            mState.setTimeStart(time);
                            if (mState.getTimeEnd() < time) {
                                mState.setTimeEnd(time + 1000 * 60 * 60);
                            }
                        } else {
                            mState.setTimeEnd(time);
                        }
                        mShouldChangeFilter = true;
                        syncViews();
                    }
                }, hour, 0, DateFormat.is24HourFormat(getActivity()));
                timeDialog.show();
            }
        }, year, month, day);
        if (start) {
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        } else {
            dialog.getDatePicker().setMinDate(mState.getTimeStart() - 1000);
        }
        dialog.show();
    }

    private static final class BadParseException extends RuntimeException {
        BadParseException(String detailMessage) {
            super(detailMessage);
        }
    }
}
