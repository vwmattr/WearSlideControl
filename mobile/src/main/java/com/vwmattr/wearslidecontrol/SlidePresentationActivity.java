package com.vwmattr.wearslidecontrol;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;


/**
 * The Main presentation view for the Mobile side of this example.  Displays a ViewPager with fodder
 * content that the user will be able to control either on the mobile or the wearable.
 */
public class SlidePresentationActivity extends Activity {

    private static final String TAG = SlidePresentationActivity.class.getSimpleName();

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 5;

    private static final String EXTRA_INDEX = "pageIndex";

    private ViewPager mPager;

    private PagerAdapter mPagerAdapter;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_presentation);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new SlidePageAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        setupGoogleApiClient();
        setupPageChangeListener();
    }

    private void setupGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void setupPageChangeListener() {
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int i) {
                updateDataItem();
            }

            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    private void updateDataItem() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                PutDataMapRequest putRequest = PutDataMapRequest.create("/state");

                DataMap map = putRequest.getDataMap();
                map.putInt(EXTRA_INDEX, getCurrentIndex());

                DataApi.DataItemResult result = Wearable.DataApi.putDataItem(
                        mGoogleApiClient, putRequest.asPutDataRequest()).await();

                Log.i(TAG, "Sent updated data item: Current Page: " + getCurrentIndex());

                return null;
            }
        }.execute();

    }

    /**
     * Convenience method to get the current page index from the ViewPager.
     */
    private int getCurrentIndex() {
        return mPager.getCurrentItem();
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
