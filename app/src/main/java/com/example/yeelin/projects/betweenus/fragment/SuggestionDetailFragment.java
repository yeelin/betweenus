package com.example.yeelin.projects.betweenus.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.loader.LoaderId;
import com.example.yeelin.projects.betweenus.loader.SingleSuggestionLoaderCallbacks;
import com.example.yeelin.projects.betweenus.model.YelpBusiness;
import com.example.yeelin.projects.betweenus.utils.ImageUtils;
import com.squareup.picasso.Target;

/**
 * Created by ninjakiki on 7/15/15.
 */
public class SuggestionDetailFragment
        extends Fragment
        implements SingleSuggestionLoaderCallbacks.SingleSuggestionLoaderListener,
        View.OnClickListener {
    //logcat
    private static final String TAG = SuggestionDetailFragment.class.getCanonicalName();
    //bundle args
    private static final String ARG_SEARCH_ID = SuggestionDetailFragment.class.getSimpleName() + ".searchId";
    //member variables
    private String searchId;
    private YelpBusiness yelpBusiness;
    private SuggestionDetailFragmentListener listener;

    /**
     * Creates a new instance of this fragment
     * @param id
     * @return
     */
    public static SuggestionDetailFragment newInstance(String id) {
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_ID, id);

        SuggestionDetailFragment fragment = new SuggestionDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Interface for activities or parent fragments interested in events from this fragment
     */
    public interface SuggestionDetailFragmentListener {
        public void onOpenWebsite(String url);
        public void onDialPhone(String phone);
    }

    /**
     * Required empty constructor
     */
    public SuggestionDetailFragment() {}

    /**
     * Make sure activity or parent fragment is a listener
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Object objectToCast = getParentFragment() != null ? getParentFragment() : activity;
        try {
            listener = (SuggestionDetailFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName() + " must implement SuggestionDetailFragmentListener");
        }
    }

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
            searchId = args.getString(ARG_SEARCH_ID);
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
        viewHolder.websiteButton.setOnClickListener(this);
        viewHolder.phoneButton.setOnClickListener(this);
    }

    /**
     * Init the loader
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //initialize the loader to fetch details for this particular id from the network
        SingleSuggestionLoaderCallbacks.initLoader(
                getActivity(),
                getLoaderManager(),
                this,
                searchId);
    }

    /**
     * SingleSuggestionLoaderCallbacks.SingleSuggestionLoaderListener callback
     * When the loader delivers the results, this method would be called.  This method then calls updateView.
     * @param loaderId
     * @param yelpBusiness
     */
    @Override
    public void onLoadComplete(LoaderId loaderId, @Nullable YelpBusiness yelpBusiness) {
        if (loaderId != LoaderId.SINGLE_PLACE) {
            Log.d(TAG, "onLoadComplete: Unknown loaderId:" + loaderId);
            return;
        }

        this.yelpBusiness = yelpBusiness;
        //debugging purposes
        if (yelpBusiness == null) {
            Log.d(TAG, "onLoadComplete: Yelp business is null. Loader must be resetting");
            return;
        }
        else {
            Log.d(TAG, "onLoadComplete: Yelp business is not null. Updating views");
            updateView();
        }
    }

    /**
     * Helper method to update all the text views that display business details
     */
    private void updateView() {
        Log.d(TAG, "updateView");

        //check if the view is ready
        ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) return;

        //name
        viewHolder.name.setText(yelpBusiness.getName());

        //categories
        viewHolder.categories.setText(yelpBusiness.getDisplayCategories());

        //distance from user
        //TODO: // FIXME: 7/31/15
        viewHolder.distanceFromCenter.setText(getString(R.string.detail_distance_from_center, String.valueOf(yelpBusiness.getDistance())));

        //address
        viewHolder.address.setText(yelpBusiness.getLocation().getAddress()[0]);

        //cross streets
        viewHolder.crossStreets.setText(getString(R.string.detail_crossStreets, yelpBusiness.getLocation().getCross_streets()));

        //phone
        viewHolder.phone.setText(yelpBusiness.getDisplay_phone());

        //web address
        viewHolder.webAddress.setText(yelpBusiness.getMobile_url());

        //ratings and reviews
        viewHolder.reviews.setText(getString(R.string.review_count, String.valueOf(yelpBusiness.getReview_count())));
        //note: picasso only keeps a weak ref to the target so it may be gc-ed
        //use setTag so that target will be alive as long as the view is alive
        final Target target = ImageUtils.newTarget(getActivity(), viewHolder.reviews);
        viewHolder.reviews.setTag(target);
        ImageUtils.loadImage(getActivity(), yelpBusiness.getRating_img_url_large(), target);

        viewHolder.hoursRange.setText("None");
    }

    /**
     * Handles the different button clicks
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_website_button:
                listener.onOpenWebsite(yelpBusiness.getMobile_url());
                break;
            case R.id.detail_phone_button:
                listener.onDialPhone(yelpBusiness.getPhone());
                break;
        }
    }

    /**
     * Returns the fragment view's view holder if it exists, or null.
     * @return
     */
    @Nullable
    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /**
     * View holder class
     */
    private class ViewHolder {
        final TextView name;
        final TextView categories;;
        final TextView distanceFromCenter;
        final TextView address;
        final TextView crossStreets;
        final TextView phone;
        final TextView webAddress;
        final TextView reviews;
        final TextView hoursRange;

        final Button websiteButton;
        final Button phoneButton;

        ViewHolder(View view) {
            //text views
            name = (TextView) view.findViewById(R.id.detail_name);
            categories = (TextView) view.findViewById(R.id.detail_categories);
            distanceFromCenter = (TextView) view.findViewById(R.id.detail_distance_from_center);
            address = (TextView) view.findViewById(R.id.detail_address);
            crossStreets = (TextView) view.findViewById(R.id.detail_crossStreets);
            phone = (TextView) view.findViewById(R.id.detail_phone);
            webAddress = (TextView) view.findViewById(R.id.detail_webAddress);
            reviews = (TextView) view.findViewById(R.id.detail_reviews);
            hoursRange = (TextView) view.findViewById(R.id.detail_hours_range);

            //buttons
            websiteButton = (Button) view.findViewById(R.id.detail_website_button);
            phoneButton = (Button) view.findViewById(R.id.detail_phone_button);
        }
    }
}
