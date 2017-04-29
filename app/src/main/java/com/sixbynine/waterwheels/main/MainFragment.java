package com.sixbynine.waterwheels.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.collect.ImmutableList;
import com.sixbynine.waterwheels.BaseFragment;
import com.sixbynine.waterwheels.BuildConfig;
import com.sixbynine.waterwheels.R;
import com.sixbynine.waterwheels.data.OfferDbManager;
import com.sixbynine.waterwheels.events.FeedRequestFinishedEvent;
import com.sixbynine.waterwheels.manager.FacebookManager;
import com.sixbynine.waterwheels.model.Offer;
import com.sixbynine.waterwheels.offerdisplay.FeedAdapter;
import com.sixbynine.waterwheels.offerdisplay.OnOfferClickListener;
import com.sixbynine.waterwheels.util.Utils;
import com.squareup.otto.Subscribe;

public final class MainFragment extends BaseFragment {

  private SwipeRefreshLayout mSwipeRefreshLayout;
  private ListView mListView;
  private View mMainLayout;
  private View mLoadingLayout;
  private View mNotWaterlooLayout;
  private OnOfferClickListener mOnOfferClickListener;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    mOnOfferClickListener = OnOfferClickListener.class.cast(context);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_main, container, false);

    mMainLayout = view.findViewById(R.id.main_layout);
    mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
    mListView = (ListView) view.findViewById(R.id.list_view);
    mLoadingLayout = view.findViewById(R.id.loading_layout);
    mNotWaterlooLayout = view.findViewById(R.id.not_waterloo_layout);

    view.findViewById(R.id.how_do_you_do).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://uwaterloo.ca/find-out-more/admissions")));
      }
    });

    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (getActivity() != null) {
          Offer offer = (Offer) parent.getItemAtPosition(position);
          mOnOfferClickListener.onOfferClick(offer);
        }
      }
    });

    if (BuildConfig.DEBUG) {
      mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
          Offer offer = (Offer) parent.getItemAtPosition(position);
          //FacebookManager.getInstance().buildDebugNotification(offer);
          //return false;
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

    mSwipeRefreshLayout.setColorSchemeColors(
        ContextCompat.getColor(getContext(), R.color.colorPrimary));

    syncViews();

    return view;
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

        mListView.setEnabled(true);
        if (mListView.getAdapter() == null) {
          Iterable<Offer> offers = OfferDbManager.getInstance().getOffers();
          mListView.setAdapter(new FeedAdapter(getContext(), ImmutableList.copyOf(offers)));
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

  private static final class BadParseException extends RuntimeException {
    BadParseException(String detailMessage) {
      super(detailMessage);
    }
  }
}
