package com.example.yeelin.projects.betweenus.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.utils.ImageUtils;


/**
 * Created by ninjakiki on 11/12/15.
 */
public class PhotoFragment
        extends Fragment
        implements View.OnClickListener {
    //logcat
    private static final String TAG = PhotoFragment.class.getCanonicalName();

    //bundle args
    private static final String ARG_PHOTO_URL = PhotoFragment.class.getSimpleName() + ".url";
    private static final String ARG_PHOTO_CAPTION = PhotoFragment.class.getSimpleName() + ".caption";

    //member variables
    private String url;
    private String caption;

    /**
     * Creates a new instance of this fragment
     * @return
     */
    public static PhotoFragment newInstance(String url, String caption) {
        Bundle args = new Bundle();
        args.putString(ARG_PHOTO_URL, url);
        args.putString(ARG_PHOTO_CAPTION, caption);

        PhotoFragment fragment = new PhotoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public PhotoFragment() {}

    /**
     * Configure the fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //read bundle args
        Bundle args = getArguments();
        if (args != null) {
            url = args.getString(ARG_PHOTO_URL);
            caption = args.getString(ARG_PHOTO_CAPTION);
        }
    }

    /**
     * Inflate the view
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo, container, false);
    }

    /**
     * Configure the fragment's view
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ImageView imageView = (ImageView) view.findViewById(R.id.image);
        final TextView textView = (TextView) view.findViewById(R.id.caption);

        //set up image view
        ImageUtils.loadImage(getActivity(), url, imageView,
                R.drawable.ic_business_image_placeholder, R.drawable.ic_business_image_placeholder);
        imageView.setOnClickListener(this);

        //set up textview
        if (caption == null) {
            textView.setVisibility(View.GONE);
        }
        else {
            textView.setText(caption);
        }
    }

    /**
     * TODO: If the image is clicked, hide the text view and make the image full screen.
     * If the textview is already hidden, then show the textview again.
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.image) {
            Log.d(TAG, "Image was clicked");
        }
    }
}
