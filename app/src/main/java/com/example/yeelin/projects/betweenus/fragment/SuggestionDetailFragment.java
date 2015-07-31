package com.example.yeelin.projects.betweenus.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;

/**
 * Created by ninjakiki on 7/15/15.
 */
public class SuggestionDetailFragment extends Fragment {
    //logcat
    private static final String TAG = SuggestionDetailFragment.class.getCanonicalName();

    //bundle args
    private static final String ARG_ID = SuggestionDetailFragment.class.getSimpleName() + ".id";

    /**
     * Creates a new instance of this fragment
     * @param id
     * @return
     */
    public static SuggestionDetailFragment newInstance(String id) {
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);

        SuggestionDetailFragment fragment = new SuggestionDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Required empty constructor
     */
    public SuggestionDetailFragment() {}

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
        return inflater.inflate(R.layout.fragment_suggestion_detail, container, false);
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

        //TODO: set click listeners on buttons
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
        final TextView name;
        final TextView categories;
        final TextView price;
        final TextView distanceFromUser;
        final TextView address;
        final TextView crossStreets;
        final TextView phone;
        final TextView webAddress;
        final TextView rating;
        final TextView reviewCount;
        final TextView hoursRange;
        final TextView acceptsCredit;
        final TextView parking;
        final TextView accessible;
        final TextView outdoorSeating;
        final TextView wifi;

        final Button websiteButton;
        final Button menuButton;
        final Button phoneButton;
        final Button mapButton;

        ViewHolder(View view) {
            //text views
            name = (TextView) view.findViewById(R.id.detail_name);
            categories = (TextView) view.findViewById(R.id.detail_categories);
            price = (TextView) view.findViewById(R.id.detail_price);
            distanceFromUser = (TextView) view.findViewById(R.id.detail_distance_from_user);
            address = (TextView) view.findViewById(R.id.detail_address);
            crossStreets = (TextView) view.findViewById(R.id.detail_crossStreets);
            phone = (TextView) view.findViewById(R.id.detail_phone);
            webAddress = (TextView) view.findViewById(R.id.detail_webAddress);
            rating = (TextView) view.findViewById(R.id.detail_rating);
            reviewCount = (TextView) view.findViewById(R.id.detail_review_count);
            hoursRange = (TextView) view.findViewById(R.id.detail_hours_range);
            acceptsCredit = (TextView) view.findViewById(R.id.detail_accepts_credit);
            parking = (TextView) view.findViewById(R.id.detail_parking);
            accessible = (TextView) view.findViewById(R.id.detail_accessible);
            outdoorSeating = (TextView) view.findViewById(R.id.detail_outdoor_seating);
            wifi = (TextView) view.findViewById(R.id.detail_wifi);

            //buttons
            websiteButton = (Button) view.findViewById(R.id.detail_website_button);
            menuButton = (Button) view.findViewById(R.id.detail_menu_button);
            phoneButton = (Button) view.findViewById(R.id.detail_phone_button);
            mapButton = (Button) view.findViewById(R.id.detail_map_button);
        }
    }
}
