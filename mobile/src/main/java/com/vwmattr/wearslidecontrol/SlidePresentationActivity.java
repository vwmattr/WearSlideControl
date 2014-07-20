package com.vwmattr.wearslidecontrol;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;


/**
 * The Main presentation view for the Mobile side of this example.  Displays a ViewPager with fodder
 * content that the user will be able to control either on the mobile or the wearable.
 */
public class SlidePresentationActivity extends Activity {

    private ViewPager mPager;

    private PagerAdapter mPagerAdapter;

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_presentation);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new SlidePageAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    /**
     * A simple pager adapter that represents 5 {@link com.vwmattr.wearslidecontrol.SlideFragment}
     * objects, in sequence.
     */
    private class SlidePageAdapter extends FragmentStatePagerAdapter {

        public SlidePageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return SlideFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
