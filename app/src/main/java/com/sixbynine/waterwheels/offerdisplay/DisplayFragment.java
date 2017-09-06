package com.sixbynine.waterwheels.offerdisplay;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
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

  private static final boolean USE_FLAT_PHONE_LAYOUT = true;

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

    mToolbar = view.findViewById(R.id.toolbar);

    TextView timestamp = view.findViewById(R.id.timestamp);
    timestamp.setText(OfferUtils.makePrettyTimestamp(getContext(), mOffer.getTime()));

    TextView price = view.findViewById(R.id.price);
    if (mOffer.getPrice().isPresent()) {
      price.setText(getString(R.string.price, mOffer.getPrice().get()));
    } else {
      price.setVisibility(View.INVISIBLE);
    }

    TextView origin = view.findViewById(R.id.origin);
    origin.setText(getResources().getString(R.string.from_x, mOffer.getOrigin().getName()));

    TextView destination = view.findViewById(R.id.destination);
    destination.setText(getResources().getString(R.string.to_x, mOffer.getDestination().getName()));

    ImageView profileImageView = view.findViewById(R.id.profile_image);
    Picasso.with(getContext())
        .load(FacebookManager.getProfilePicUrlFromId(mOffer.getPost().getFrom().getId()))
        .noFade()
        .into(profileImageView);

    TextView profileName = view.findViewById(R.id.profile_name);
    profileName.setText(mOffer.getPost().getFrom().getName());

    TextView postTime = view.findViewById(R.id.post_created_time);
    long createdTime = TimeUnit.MILLISECONDS.convert(mOffer.getPost().getCreatedTime(), TimeUnit.SECONDS);
    postTime.setText(OfferUtils.makePrettyPostTime(getContext(), createdTime));

    final ImageView map = view.findViewById(R.id.map);
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

    TextView message = view.findViewById(R.id.message);
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
    View flatPhoneLayout = view.findViewById(R.id.phone_layout_flat);
    if (mOffer.getPhoneNumber().isPresent()) {
      final Intent phoneIntent = getPhoneIntent();
      final Intent smsIntent = getSmsIntent();

      Pair<Drawable, CharSequence> phoneIntentDetails = getDisplayDetailsForIntent(phoneIntent);
      Pair<Drawable, CharSequence> smsIntentDetails = getDisplayDetailsForIntent(smsIntent);

      View phoneButton;
      View smsButton;
      if (phoneIntentDetails == null || smsIntentDetails == null || !USE_FLAT_PHONE_LAYOUT) {
        phoneLayout.setVisibility(View.VISIBLE);
        flatPhoneLayout.setVisibility(View.GONE);
        phoneButton = view.findViewById(R.id.phone_btn);
        smsButton = view.findViewById(R.id.sms_btn);
      } else {
        phoneLayout.setVisibility(View.GONE);
        flatPhoneLayout.setVisibility(View.VISIBLE);
        phoneButton = view.findViewById(R.id.phone_btn_flat);
        smsButton = view.findViewById(R.id.sms_btn_flat);

        ImageView phoneIcon = view.findViewById(R.id.phone_icon);
        phoneIcon.setImageDrawable(phoneIntentDetails.first);

        ImageView smsIcon = view.findViewById(R.id.sms_icon);
        smsIcon.setImageDrawable(smsIntentDetails.first);
      }

      phoneButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          startActivity(phoneIntent);
        }
      });
      smsButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          startActivity(smsIntent);
        }
      });
    } else {
      phoneLayout.setVisibility(View.GONE);
      flatPhoneLayout.setVisibility(View.GONE);
    }

    return view;
  }

  private Intent getPhoneIntent() {
    return new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mOffer.getPhoneNumber().get()));
  }

  private Intent getSmsIntent() {
    Intent smsIntent;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      // Add the phone number in the data
      Uri uri = Uri.parse("smsto:" + mOffer.getPhoneNumber().get());
      // Create intent with the action and data
      smsIntent = new Intent(Intent.ACTION_SENDTO, uri);
      // Set the message
      smsIntent.putExtra("sms_body", "Hi, do you still have a spot left?");
    } else {
      smsIntent = new Intent(Intent.ACTION_VIEW);
      smsIntent.setType("vnd.android-dir/mms-sms");
      smsIntent.putExtra("address", mOffer.getPhoneNumber().get());
      smsIntent.putExtra("sms_body", "Hi, do you still have a spot left?");
    }
    return smsIntent;
  }

  /** Gets a label and icon for this intent if there is just one match, null otherwise */
  private Pair<Drawable, CharSequence> getDisplayDetailsForIntent(Intent intent) {
    PackageManager packageManager = getContext().getPackageManager();
    ResolveInfo resolveInfo = packageManager.resolveActivity(intent, 0);
    if (resolveInfo == null
        || resolveInfo.activityInfo == null
        || resolveInfo.activityInfo.processName == null
        || resolveInfo.activityInfo.processName.equals("system:ui")) {
      return null;
    }

    CharSequence label = resolveInfo.activityInfo.loadLabel(packageManager);
    if (label == null) {
      label = resolveInfo.loadLabel(packageManager);
    }

    Drawable icon = resolveInfo.activityInfo.loadIcon(packageManager);
    if (icon == null) {
      icon = resolveInfo.loadIcon(packageManager);
    }

    if (label == null || icon == null) {
      return null;
    }

    return new Pair<>(icon, label);
  }

  @Override
  public void onResume() {
    super.onResume();
    getAppCompatActivity().setSupportActionBar(mToolbar);
    getAppCompatActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }
}
