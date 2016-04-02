package com.sixbynine.waterwheels.offerdisplay;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.sixbynine.waterwheels.BaseFragment;
import com.sixbynine.waterwheels.R;
import com.sixbynine.waterwheels.manager.FacebookIntents;
import com.sixbynine.waterwheels.manager.FacebookManager;
import com.sixbynine.waterwheels.model.Offer;
import com.sixbynine.waterwheels.util.Keys;
import com.sixbynine.waterwheels.util.OfferUtils;
import com.sixbynine.waterwheels.util.Prefs;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

public final class DisplayFragment extends BaseFragment {

    private Offer mOffer;
    private Toolbar mToolbar;

    public static DisplayFragment newInstance(Offer offer) {
        DisplayFragment fragment = new DisplayFragment();
        Bundle args = new Bundle();
        args.putParcelable(Keys.OFFER, offer);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mOffer = getArguments().getParcelable(Keys.OFFER);
        } else {
            mOffer = savedInstanceState.getParcelable(Keys.OFFER);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Keys.OFFER, mOffer);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display, container, false);

        if (mOffer == null) {
            return view;
        }

        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);

        TextView timestamp = (TextView) view.findViewById(R.id.timestamp);
        timestamp.setText(OfferUtils.makePrettyTimestamp(getContext(), mOffer.getTime()));

        TextView price = (TextView) view.findViewById(R.id.price);
        if (mOffer.getPrice().isPresent()) {
            price.setText("$" + mOffer.getPrice().get());
        } else {
            price.setVisibility(View.INVISIBLE);
        }

        TextView origin = (TextView) view.findViewById(R.id.origin);
        origin.setText(getResources().getString(R.string.from_x, mOffer.getOrigin().getName()));

        TextView destination = (TextView) view.findViewById(R.id.destination);
        destination.setText(getResources().getString(R.string.to_x, mOffer.getDestination().getName()));

        ImageView profileImageView = (ImageView) view.findViewById(R.id.profile_image);
        Picasso.with(getContext())
                .load(FacebookManager.getProfilePicUrlFromId(mOffer.getPost().getFrom().getId()))
                .noFade()
                .into(profileImageView);

        TextView profileName = (TextView) view.findViewById(R.id.profile_name);
        profileName.setText(mOffer.getPost().getFrom().getName());

        TextView postTime = (TextView) view.findViewById(R.id.post_created_time);
        long createdTime = TimeUnit.MILLISECONDS.convert(mOffer.getPost().getCreatedTime(), TimeUnit.SECONDS);
        postTime.setText(OfferUtils.makePrettyPostTime(getContext(), createdTime));

        final ImageView map = (ImageView) view.findViewById(R.id.map);
        map.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Picasso.with(getContext())
                        .load(OfferUtils.getStaticMapUrl(mOffer, map.getWidth(), map.getHeight()))
                        .into(map);
                if (Build.VERSION.SDK_INT >= 16) {
                    map.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    map.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        TextView message = (TextView) view.findViewById(R.id.message);
        message.setText(mOffer.getPost().getMessage());

        final View clickPostTooltip = view.findViewById(R.id.click_post);
        if (Prefs.getBoolean(Keys.POST_CLICKED)) {
            clickPostTooltip.setVisibility(View.GONE);
        } else {
            clickPostTooltip.setVisibility(View.VISIBLE);
        }

        view.findViewById(R.id.post_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() != null) {
                    Prefs.putBoolean(Keys.POST_CLICKED, true);
                    clickPostTooltip.setVisibility(View.GONE);
                    startActivity(FacebookIntents.viewPost(getContext(), mOffer.getPost()));
                }
            }
        });

        View phoneLayout = view.findViewById(R.id.phone_layout);
        if (mOffer.getPhoneNumber().isPresent()) {
            phoneLayout.setVisibility(View.VISIBLE);
            view.findViewById(R.id.call_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mOffer.getPhoneNumber().get()));
                    startActivity(intent);
                }
            });
            view.findViewById(R.id.text_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                    smsIntent.setType("vnd.android-dir/mms-sms");
                    smsIntent.putExtra("address", mOffer.getPhoneNumber().get());
                    smsIntent.putExtra("sms_body", "Hi, do you still have a spot left?");
                    startActivity(smsIntent);
                }
            });
        } else {
            phoneLayout.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getAppCompatActivity().setSupportActionBar(mToolbar);
        getAppCompatActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
