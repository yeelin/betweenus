package com.example.yeelin.projects.betweenus.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yeelin.projects.betweenus.R;

/**
 * Created by ninjakiki on 7/15/15.
 */
public class PlaceFragment extends Fragment {
    //logcat
    private static final String TAG = PlaceFragment.class.getCanonicalName();

    //bundle args
    private static final String ARG_ID = PlaceFragment.class.getSimpleName() + ".placeId";
    private static final String ARG_NAME = PlaceFragment.class.getSimpleName() + ".placeName";

    /**
     *
     * @param placeId
     * @param name
     * @return
     */
    public static PlaceFragment newInstance(long placeId, String name) {
        Bundle args = new Bundle();
        args.putLong(ARG_ID, placeId);
        args.putString(ARG_NAME, name);

        PlaceFragment fragment = new PlaceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Required empty constructor
     */
    public PlaceFragment() {}

    /**
     * Configure the fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        return inflater.inflate(R.layout.fragment_place, container, false);
    }

    /**
     * Configure the fragment's view
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //set view holder
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
    }

    /**
     * Returns the fragment view's view holder if it exists, or null.
     * @return
     */
    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /**
     * View holder class
     */
    private class ViewHolder {

        ViewHolder(View view) {

        }
    }
}
