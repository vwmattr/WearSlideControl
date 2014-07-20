package com.vwmattr.wearslidecontrol;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * This Fragment is effectively the "Slide" that is shown in the
 * {@link com.vwmattr.wearslidecontrol.SlidePresentationActivity}'s
 * {@link android.support.v4.view.ViewPager} instance in the mobile app.
 */
public class SlideFragment extends Fragment {

    //Bundle argument keys:
    private static final String ARG_PAGE_NO = "pageNo";

    private int mPageNo;

    public SlideFragment() {}

    /**
     * Factory method for this fragment. Constructs a new fragment with the provided page number in
     * its argument {@link android.os.Bundle}.
     */
    public static SlideFragment newInstance(int pageNo) {
        SlideFragment fragment = new SlideFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_PAGE_NO, pageNo);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNo = getArguments().getInt(ARG_PAGE_NO);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView =
                (ViewGroup) inflater.inflate(R.layout.fragment_slide, container, false);

        ((TextView) rootView.findViewById(R.id.pageNumber)).setText(Integer.toString(mPageNo));
        return rootView;
    }

}
