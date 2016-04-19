package com.sixbynine.waterwheels.filter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.sixbynine.waterwheels.BaseFragment;
import com.sixbynine.waterwheels.R;
import com.sixbynine.waterwheels.abstracts.AbstractOnItemSelectedListener;
import com.sixbynine.waterwheels.data.OfferDbManager;
import com.sixbynine.waterwheels.events.FeedRequestFinishedEvent;
import com.sixbynine.waterwheels.manager.FacebookManager;
import com.sixbynine.waterwheels.model.Offer;
import com.sixbynine.waterwheels.model.Place;
import com.sixbynine.waterwheels.model.PlaceChoice;
import com.sixbynine.waterwheels.offerdisplay.FeedAdapter;
import com.sixbynine.waterwheels.offerdisplay.OnOfferClickListener;
import com.sixbynine.waterwheels.util.Logger;
import com.sixbynine.waterwheels.util.OfferUtils;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.sixbynine.waterwheels.util.Utils.or;

public final class FilterFragment extends BaseFragment {

    private Spinner mOriginSpinner;
    private Spinner mDestinationSpinner;
    private TextView mTimeStartChoice;
    private TextView mTimeEndChoice;
    private ListView mListView;
    private View mProgressBar;

    private FilterFragmentState mState;
    private boolean mShouldChangeFilter;
    private OnOfferClickListener mOnOfferClickListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mOnOfferClickListener = OnOfferClickListener.class.cast(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mState = FilterFragmentState.getState();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter, container, false);

        mOriginSpinner = (Spinner) view.findViewById(R.id.origin_spinner);
        mDestinationSpinner = (Spinner) view.findViewById(R.id.destination_spinner);

        final List<PlaceChoice> placeChoices = Place.getAllPlaceChoices();
        List<String> names = new ArrayList<>(placeChoices.size());
        for (PlaceChoice placeChoice : placeChoices) {
            names.add(placeChoice.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mOriginSpinner.setAdapter(adapter);
        if (mState.getOrigin() != null) {
            mOriginSpinner.setSelection(placeChoices.indexOf(mState.getOrigin()));
        }
        mOriginSpinner.setOnItemSelectedListener(new AbstractOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mShouldChangeFilter = mState.setOrigin(placeChoices.get(position));
                syncViews();
            }
        });

        View clearButton = view.findViewById(R.id.btn_clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOriginSpinner.setSelection(0);
                mDestinationSpinner.setSelection(0);
                mShouldChangeFilter = or(
                        mState.setOrigin(placeChoices.get(0)),
                        mState.setDestination(placeChoices.get(0)),
                        mState.setTimeEnd(0),
                        mState.setTimeStart(0));
                syncViews();
            }
        });

        ArrayAdapter<String> destAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, names);
        destAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mDestinationSpinner.setAdapter(destAdapter);
        if (mState.getDestination() != null) {
            mDestinationSpinner.setSelection(placeChoices.indexOf(mState.getDestination()));
        }
        mDestinationSpinner.setOnItemSelectedListener(new AbstractOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mShouldChangeFilter = mState.setDestination(placeChoices.get(position));
                syncViews();
            }
        });

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

        mListView = (ListView) view.findViewById(R.id.list_view);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (getActivity() != null) {
                    Offer offer = (Offer) parent.getItemAtPosition(position);
                    mOnOfferClickListener.onOfferClick(offer);
                }
            }
        });

        mProgressBar = view.findViewById(R.id.progress_bar);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        syncViews();
    }

    @Subscribe
    public void onOffersLoaded(FeedRequestFinishedEvent event) {
        syncViews();
    }

    private void syncViews() {
        if (getContext() != null) {
            if (FacebookManager.getInstance().getStatus() == FacebookManager.Status.LOADED) {
                if (mState.getTimeStart() < System.currentTimeMillis()) {
                    mState.setTimeStart(System.currentTimeMillis());
                }
                if (mState.getTimeEnd() < mState.getTimeStart()) {
                    mState.setTimeEnd(mState.getTimeStart() + TimeUnit.MILLISECONDS.convert(6, TimeUnit.DAYS));
                }

                FilterFragmentState.saveState(mState);

                mTimeStartChoice.setText(OfferUtils.makePrettyTimestamp(getContext(), mState.getTimeStart()) + " - ");
                mTimeEndChoice.setText(OfferUtils.makePrettyTimestamp(getContext(), mState.getTimeEnd()));

                if (mListView.getAdapter() == null || mShouldChangeFilter) {
                    Iterable<Offer> offers = Iterables.filter(
                            OfferDbManager.getInstance().getOffers(),
                            mState.getGenerousPredicate());
                    mListView.setAdapter(new FeedAdapter(getContext(), ImmutableList.copyOf(offers)));
                    mShouldChangeFilter = false;
                }

                mListView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            } else {
                mListView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
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
                            mShouldChangeFilter = mState.setTimeStart(time);
                            if (mState.getTimeEnd() < time) {
                                mState.setTimeEnd(time + 1000 * 60 * 60);
                                mShouldChangeFilter = true;
                            }
                        } else {
                            mShouldChangeFilter = mState.setTimeEnd(time);
                        }
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
}
