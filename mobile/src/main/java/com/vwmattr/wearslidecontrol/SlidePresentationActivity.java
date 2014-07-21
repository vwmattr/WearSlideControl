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
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import static com.vwmattr.wearslidecontrol.common.Constants.EXTRA_INDEX;


/**
 * The Main presentation view for the Mobile side of this example.  Displays a ViewPager with fodder
 * content that the user will be able to control either on the mobile or the wearable.
 */
public class SlidePresentationActivity extends Activity implements MessageApi.MessageListener {

    private static final String TAG = SlidePresentationActivity.class.getSimpleName();

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 5;
    private static final byte CONTROL_FWD_MSG = 0;
    private static final byte CONTROL_PREV_MSG = 1;

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

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        updateDataItem();
    }

    @Override
    protected void onPause() {

        Log.i(TAG, "on Paused()");

        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                NodeApi.GetConnectedNodesResult nodes =
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

                byte[] message = new byte[1];
                for (Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(mGoogleApiClient,
                            node.getId(), "/stopIt", message);
                    Log.i(TAG, "Sent stopIt message to node: " + node.getId());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                Wearable.MessageApi.removeListener(mGoogleApiClient, SlidePresentationActivity.this);
                mGoogleApiClient.disconnect();
            }

        }.execute();

        super.onPause();
    }

    private void setupGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
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

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i(TAG, "Received messageEvent : " + messageEvent.getPath());

        if ("/control".equals(messageEvent.getPath())) {
            switch (messageEvent.getData()[0]) {
                case CONTROL_FWD_MSG:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            advanceSlide();
                        }
                    });
                    break;
                case CONTROL_PREV_MSG:
                    //Not yet supported...
                default:
                    break;
            }
        }
    }

    /**
     * Helper method to advance the displayed slide
     */
    private void advanceSlide() {
        int currIndex = getCurrentIndex();
        int totalSlides = mPager.getChildCount();
        if (currIndex <= totalSlides) {
            mPager.setCurrentItem(currIndex+1);
        } else {
            mPager.setCurrentItem(0);
        }
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
