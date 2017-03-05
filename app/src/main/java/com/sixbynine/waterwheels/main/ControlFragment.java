package com.sixbynine.waterwheels.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.sixbynine.waterwheels.BaseFragment;
import com.sixbynine.waterwheels.R;
import com.sixbynine.waterwheels.filter.FilterFragment;
import com.sixbynine.waterwheels.settings.SettingsFragment;
import com.sixbynine.waterwheels.util.Keys;

public final class ControlFragment extends BaseFragment {

    private ViewPager mPager;
    private PagerSlidingTabStrip mTabs;

    public static ControlFragment newInstance(boolean showFilter) {
        ControlFragment fragment = new ControlFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(Keys.SHOW_FILTER, showFilter);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control, container, false);

        mPager = (ViewPager) view.findViewById(R.id.pager);
        mTabs = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);

        mPager.setAdapter(new MyAdapter(getChildFragmentManager()));

        mTabs.setViewPager(mPager);

        Bundle args = getArguments();

        if (args != null && args.getBoolean(Keys.SHOW_FILTER)) {
            mPager.setCurrentItem(0);
        } else if (savedInstanceState == null) {
            mPager.setCurrentItem(1);
        } else {
            mPager.setCurrentItem(savedInstanceState.getInt(Keys.SELECTED_INDEX));
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPager != null) {
            outState.putInt(Keys.SELECTED_INDEX, mPager.getCurrentItem());
        }
    }

    private static final class MyAdapter extends FragmentStatePagerAdapter
            implements PagerSlidingTabStrip.IconTabProvider {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        private Fragment[] fragments = new Fragment[3];

        @Override
        public Fragment getItem(int position) {
            if (position >= getCount()) {
                throw new IllegalArgumentException();
            }
            if (fragments[position] == null) {
                switch (position) {
                    case 0:
                        fragments[0] = new FilterFragment();
                    case 1:
                        fragments[1] = new MainFragment();
                    case 2:
                        fragments[2] = new SettingsFragment();
                }
            }
            return fragments[position];
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return super.getPageTitle(position);
        }

        @Override
        public int getPageIconResId(int i) {
            switch (i) {
                case 0:
                    return R.drawable.ic_tab_filter;
                case 1:
                    return R.drawable.ic_car;
                case 2:
                    return R.drawable.ic_settings;
            }
            throw new IllegalStateException();
        }
    }
}
