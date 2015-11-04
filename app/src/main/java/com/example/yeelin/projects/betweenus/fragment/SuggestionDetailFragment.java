package com.example.yeelin.projects.betweenus.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.fb.query.FbApiHelper;
import com.example.yeelin.projects.betweenus.data.fb.query.FbConstants;
import com.example.yeelin.projects.betweenus.loader.LoaderId;
import com.example.yeelin.projects.betweenus.loader.SingleSuggestionLoaderCallbacks;
import com.example.yeelin.projects.betweenus.loader.callback.SingleSuggestionLoaderListener;
import com.example.yeelin.projects.betweenus.utils.AnimationUtils;
import com.example.yeelin.projects.betweenus.utils.FairnessScoringUtils;
import com.example.yeelin.projects.betweenus.utils.ImageUtils;
import com.example.yeelin.projects.betweenus.utils.MapColorUtils;
import com.facebook.AccessToken;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Target;

/**
 * Created by ninjakiki on 7/15/15.
 */
public class SuggestionDetailFragment
        extends Fragment
        implements
        SingleSuggestionLoaderListener,
        View.OnClickListener,
        OnMapReadyCallback {
    //logcat
    private static final String TAG = SuggestionDetailFragment.class.getCanonicalName();
    //bundle args
    private static final String ARG_ID = SuggestionDetailFragment.class.getSimpleName() + ".id";
    private static final String ARG_NAME = SuggestionDetailFragment.class.getSimpleName() + ".name";
    private static final String ARG_LATLNG = SuggestionDetailFragment.class.getSimpleName() + ".latLng";
    private static final String ARG_POSITION = SuggestionDetailFragment.class.getSimpleName() + ".position";
    private static final String ARG_TOGGLE_STATE = SuggestionDetailFragment.class.getSimpleName() + ".toggleState";
    private static final String ARG_RATING = SuggestionDetailFragment.class.getSimpleName() + ".rating";
    private static final String ARG_USER_LATLNG = SuggestionDetailFragment.class.getSimpleName() + ".userLatLng";
    private static final String ARG_FRIEND_LATLNG = SuggestionDetailFragment.class.getSimpleName() + ".friendLatLng";
    private static final String ARG_MID_LATLNG = SuggestionDetailFragment.class.getSimpleName() + ".midLatLng";

    //child fragment tags
    private static String FRAGMENT_TAG_LITEMAP = SupportMapFragment.class.getSimpleName();
    //constants
    private static final int DEFAULT_ZOOM = 13;

    //member variables
    private String id;
    private String name;
    private LatLng latLng;
    private int position = 0;
    private boolean toggleState;
    private double rating;

    private LatLng userLatLng;
    private LatLng friendLatLng;
    private LatLng midLatLng;

    private Marker marker;
    private LocalBusiness business;
    private SuggestionDetailFragmentListener listener;

    /**
     * Creates a new instance of this fragment
     * @param id
     * @param name
     * @param latLng
     * @param position
     * @param toggleState
     * @param rating
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng midpoint between userLatLng and friendLatLng
     * @return
     */
    public static SuggestionDetailFragment newInstance(String id, String name, LatLng latLng,
                                                       int position, boolean toggleState, double rating,
                                                       LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng) {
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        args.putString(ARG_NAME, name);
        args.putParcelable(ARG_LATLNG, latLng);

        args.putInt(ARG_POSITION, position);
        args.putBoolean(ARG_TOGGLE_STATE, toggleState);
        args.putDouble(ARG_RATING, rating);

        args.putParcelable(ARG_USER_LATLNG, userLatLng);
        args.putParcelable(ARG_FRIEND_LATLNG, friendLatLng);
        args.putParcelable(ARG_MID_LATLNG, midLatLng);

        SuggestionDetailFragment fragment = new SuggestionDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Interface for activities or parent fragments interested in events from this fragment
     */
    public interface SuggestionDetailFragmentListener {
        void onOpenWebsite(String url);
        void onDialPhone(String phone);
        void onToggle(String id, int position, boolean toggleState);
    }

    /**
     * Required empty constructor
     */
    public SuggestionDetailFragment() {}

    /**
     * Make sure activity or parent fragment is a listener
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Object objectToCast = getParentFragment() != null ? getParentFragment() : context;
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
            //about the business
            id = args.getString(ARG_ID);
            name = args.getString(ARG_NAME);
            latLng = args.getParcelable(ARG_LATLNG);

            //about the view
            position = args.getInt(ARG_POSITION, position);
            FRAGMENT_TAG_LITEMAP = String.format("%s.%d", FRAGMENT_TAG_LITEMAP, position);
            toggleState = args.getBoolean(ARG_TOGGLE_STATE, false);
            rating = args.getDouble(ARG_RATING);

            //user and friend related info
            userLatLng = args.getParcelable(ARG_USER_LATLNG);
            friendLatLng = args.getParcelable(ARG_FRIEND_LATLNG);
            midLatLng = args.getParcelable(ARG_MID_LATLNG);
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

        //set the name so that users with slow connections won't see a completely blank screen
        viewHolder.name.setText(name);

        //set up the state of the select button
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            viewHolder.selectButton.setCompoundDrawablesRelativeWithIntrinsicBounds(toggleState ? R.drawable.ic_action_detail_favorite_red300 : R.drawable.ic_action_detail_favorite, 0, 0, 0);
        else
            viewHolder.selectButton.setCompoundDrawablesWithIntrinsicBounds(toggleState ? R.drawable.ic_action_detail_favorite_red300 : R.drawable.ic_action_detail_favorite, 0, 0, 0);
        viewHolder.selectButton.setText(toggleState ? R.string.selected_button : R.string.select_button);

        //set up click listeners
        viewHolder.websiteButton.setOnClickListener(this);
        viewHolder.phoneButton.setOnClickListener(this);
        viewHolder.selectButton.setOnClickListener(this);

        //initially make the detail container gone and show the progress bar
        viewHolder.detailContainer.setVisibility(View.GONE);
        viewHolder.detailProgressBar.setVisibility(View.VISIBLE);

        //set up the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentByTag(FRAGMENT_TAG_LITEMAP);
        if (mapFragment == null) {
            Log.d(TAG, "onViewCreated: LiteMap fragment is null");
            GoogleMapOptions googleMapOptions = new GoogleMapOptions()
                    .camera(new CameraPosition(latLng, DEFAULT_ZOOM, 0, 0))
                    .compassEnabled(false)
                    .liteMode(true)
                    .mapToolbarEnabled(false)
                    .mapType(GoogleMap.MAP_TYPE_NORMAL)
                    .rotateGesturesEnabled(false)
                    .scrollGesturesEnabled(false)
                    .tiltGesturesEnabled(false)
                    .zoomControlsEnabled(false)
                    .zoomGesturesEnabled(false);
            mapFragment = SupportMapFragment.newInstance(googleMapOptions);
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.detail_mapContainer, mapFragment, FRAGMENT_TAG_LITEMAP)
                    .commit();
        }

        if (mapFragment != null && mapFragment.getView() != null) {
            Log.d(TAG, "onViewCreated: Disable map click");
            mapFragment.getView().setClickable(false);
        }

        mapFragment.getMapAsync(this);
    }

    /**
     * Init the loader
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (FbConstants.USE_FB) {
            //check if user is currently logged into fb
            if (AccessToken.getCurrentAccessToken() != null) {
                Log.d(TAG, "onActivityCreated: User is logged in");

                //create fb graph request for searching places
                //provide SingleSuggestionLoaderListener as a callback (onLoadComplete) -- TODO: remove hack
                FbApiHelper.getPlaceDetails(AccessToken.getCurrentAccessToken(), id, this);
            }
        }
        else {
            //initialize the loader to fetch details for this particular id from the network
            SingleSuggestionLoaderCallbacks.initLoader(
                    getActivity(),
                    getLoaderManager(),
                    this,
                    id);
        }
    }

    /**
     * Do some shimmering in case we load slowly.
     */
    @Override
    public void onResume() {
        super.onResume();

        //shimmer while we are loading
        ViewHolder viewHolder = getViewHolder();
        if (viewHolder != null) {
            viewHolder.shimmerContainer.setDuration(400);
            viewHolder.shimmerContainer.startShimmerAnimation();
        }
    }

    /**
     * Nullify the open browser and dial phone listener
     */
    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    /**
     * SingleSuggestionLoaderCallbacks.SingleSuggestionLoaderListener callback
     * When the loader delivers the results, this method would be called.  This method then calls updateView.
     * @param loaderId
     * @param business
     */
    @Override
    public void onLoadComplete(LoaderId loaderId, @Nullable LocalBusiness business) {
        if (loaderId != LoaderId.SINGLE_PLACE) {
            Log.d(TAG, "onLoadComplete: Unknown loaderId:" + loaderId);
            return;
        }

        this.business = business;

        if (business == null) {
            Log.d(TAG, "onLoadComplete: Local business is null. Loader must be resetting");
            //animate in the detail empty textview, and animate out the progress bar
            ViewHolder viewHolder = getViewHolder();
            if (viewHolder != null && viewHolder.detailEmpty.getVisibility() != View.VISIBLE) {
                //stop shimmering
                viewHolder.shimmerContainer.stopShimmerAnimation();

                viewHolder.detailContainer.setVisibility(View.GONE); //make sure container doesn't show
                AnimationUtils.crossFadeViews(getActivity(), viewHolder.detailEmpty, viewHolder.detailProgressBar);
            }
            return;
        }
        else {
            Log.d(TAG, "onLoadComplete: Local business is not null. Updating views");
            //animate in the detail container, and animate out the progress bar
            ViewHolder viewHolder = getViewHolder();
            if (viewHolder != null && viewHolder.detailContainer.getVisibility() != View.VISIBLE) {
                //stop shimmering
                viewHolder.shimmerContainer.stopShimmerAnimation();

                viewHolder.detailEmpty.setVisibility(View.GONE); //make sure empty view doesn't show
                AnimationUtils.crossFadeViews(getActivity(), viewHolder.detailContainer, viewHolder.detailProgressBar);
            }
            updateView();
        }
    }

    /**
     * Helper method to update all the text views that display business details
     */
    private void updateView() {
        Log.d(TAG, "updateView");

        //check if the view is ready
        final ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) return;

        //image
        if (business.getImageUrl() != null) {
            ImageUtils.loadImage(getActivity(), business.getImageUrl(), viewHolder.image, R.drawable.ic_business_image_placeholder, R.drawable.ic_business_image_placeholder);
        }

        //name
        viewHolder.name.setText(name);

        //category list
        final String[] categoryList = business.getCategoryList();
        if (categoryList == null) {
            viewHolder.categories.setVisibility(View.GONE);
        }
        else {
            viewHolder.categories.setVisibility(View.VISIBLE);
            StringBuilder builder = new StringBuilder(categoryList.length);
            for (int i=0; i<categoryList.length; i++) {
                builder.append(categoryList[i]);
                if (i < categoryList.length-1) builder.append(", ");
            }
            viewHolder.categories.setText(builder.toString());
        }

        //compute who is closer
        final int fairness = FairnessScoringUtils.computeFairnessScore(userLatLng, friendLatLng, latLng);
        final double distanceDelta = FairnessScoringUtils.computeDistanceDelta(latLng, midLatLng, FairnessScoringUtils.IMPERIAL);
        final String displayString = FairnessScoringUtils.formatDistanceDeltaAndFairness(getActivity(), distanceDelta, fairness, FairnessScoringUtils.IMPERIAL, true);
        viewHolder.distanceFromMidPoint.setText(displayString);

        //address
        final String fullAddress = business.getLocalBusinessLocation() != null ? business.getLocalBusinessLocation().getLongDisplayAddress() : null;
        viewHolder.address.setText(fullAddress != null ? fullAddress : getString(R.string.not_available));

        //cross streets
        final String crossStreets = business.getLocalBusinessLocation() != null ? business.getLocalBusinessLocation().getCrossStreets() : null;
        viewHolder.crossStreets.setText(getString(R.string.detail_crossStreets, crossStreets != null ? crossStreets : getString(R.string.not_available)));

        //phone
        viewHolder.phone.setText(business.getPhoneNumber() != null ? business.getPhoneNumber() : getString(R.string.not_available));

        //web address
        viewHolder.webAddress.setText(business.getMobileUrl() != null ? business.getMobileUrl() : getString(R.string.not_available));

        //price range
        final String priceRange = business.getPriceRange();
        if (priceRange != null)
            viewHolder.priceRange.setText(priceRange);
        else
            viewHolder.priceRange.setText(R.string.not_available);

        //ratings and reviews OR likes and checkins
        if (business.getReviewCount() != -1) {
            //we have yelp data
            viewHolder.reviews.setText(getString(R.string.review_count, business.getReviewCount()));
            //note: picasso only keeps a weak ref to the target so it may be gc-ed
            //use setTag so that target will be alive as long as the view is alive
            if (business.getRatingImageUrl() != null) {
                final Target target = ImageUtils.newTarget(getActivity(), viewHolder.reviews);
                viewHolder.reviews.setTag(target);
                ImageUtils.loadImage(getActivity(), business.getRatingImageUrl(), target);
            }
            viewHolder.checkins.setVisibility(View.GONE);
        }
        else {
            //we most likely have fb data
            viewHolder.checkins.setVisibility(View.VISIBLE);
            viewHolder.reviews.setText(getContext().getResources().getQuantityString(
                    R.plurals.like_count,
                    business.getLikes(),
                    business.getLikes()));
            viewHolder.checkins.setText(getContext().getResources().getQuantityString(
                    R.plurals.checkin_count,
                    business.getCheckins(),
                    business.getCheckins()));
        }

        //hours
        if (business.isAlwaysOpen()) {
            viewHolder.hoursRange.setText(R.string.always_open);
        }
        else {
            String[] hoursArray = business.getHours();
            if (hoursArray == null) {
                viewHolder.hoursRange.setText(R.string.not_available);
            } else {
                StringBuilder builder = new StringBuilder(hoursArray.length);
                for (int i = 0; i < hoursArray.length; i++) {
                    builder.append(hoursArray[i] + "\n");
                }
                viewHolder.hoursRange.setText(builder.toString());
            }
        }
    }

    /**
     * Handles the different button clicks
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_website_button:
                listener.onOpenWebsite(business.getMobileUrl());
                break;
            case R.id.detail_phone_button:
                listener.onDialPhone(business.getPhoneNumber());
                break;
            case R.id.detail_select_button:
                toggleState = !toggleState;
                final ViewHolder viewHolder = getViewHolder();
                if (viewHolder != null) {
                    //update the select button icon and text
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                        viewHolder.selectButton.setCompoundDrawablesRelativeWithIntrinsicBounds(toggleState ? R.drawable.ic_action_detail_favorite_red300 : R.drawable.ic_action_detail_favorite, 0, 0, 0);
                    else
                        viewHolder.selectButton.setCompoundDrawablesWithIntrinsicBounds(toggleState ? R.drawable.ic_action_detail_favorite_red300 : R.drawable.ic_action_detail_favorite, 0, 0, 0);
                    viewHolder.selectButton.setText(toggleState ? R.string.selected_button : R.string.select_button);
                    //update the marker color
                    marker.setIcon(MapColorUtils.determineMarkerIcon(getContext(), toggleState, rating));
                }
                listener.onToggle(id, position, toggleState);
                break;
        }
    }

    /**
     * OnMapReadyCallback callback
     * Add a single marker to the map
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");

        //disable click listener
        final SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentByTag(FRAGMENT_TAG_LITEMAP);
        if (mapFragment != null && mapFragment.getView() != null) {
            Log.d(TAG, "onMapReady: Disable map click");
            mapFragment.getView().setClickable(false);
        }

        //add marker
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(MapColorUtils.determineMarkerIcon(getContext(), toggleState, rating));
        marker = googleMap.addMarker(markerOptions);
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
        final ImageView image;
        final TextView name;
        final TextView categories;;
        final TextView distanceFromMidPoint;
        final TextView address;
        final TextView crossStreets;
        final TextView phone;
        final TextView webAddress;
        final TextView priceRange;
        final TextView reviews;
        final TextView checkins;
        final TextView hoursRange;

        final Button websiteButton;
        final Button phoneButton;
        final Button selectButton;

        final View detailContainer;
        final View detailProgressBar;
        final TextView detailEmpty;

        final ShimmerFrameLayout shimmerContainer;

        ViewHolder(View view) {
            //image view
            image = (ImageView) view.findViewById(R.id.detail_image);

            //text views
            name = (TextView) view.findViewById(R.id.detail_name);
            categories = (TextView) view.findViewById(R.id.detail_categories);
            distanceFromMidPoint = (TextView) view.findViewById(R.id.detail_distance_from_midpoint);
            address = (TextView) view.findViewById(R.id.detail_address);
            crossStreets = (TextView) view.findViewById(R.id.detail_crossStreets);
            phone = (TextView) view.findViewById(R.id.detail_phone);
            webAddress = (TextView) view.findViewById(R.id.detail_webAddress);
            priceRange = (TextView) view.findViewById(R.id.detail_price_range);
            reviews = (TextView) view.findViewById(R.id.detail_reviews);
            checkins = (TextView) view.findViewById(R.id.detail_checkins);
            hoursRange = (TextView) view.findViewById(R.id.detail_hours_range);

            //buttons
            websiteButton = (Button) view.findViewById(R.id.detail_website_button);
            phoneButton = (Button) view.findViewById(R.id.detail_phone_button);
            selectButton = (Button) view.findViewById(R.id.detail_select_button);

            //for animation
            detailContainer = view.findViewById(R.id.detail_container);
            detailProgressBar = view.findViewById(R.id.detail_progressBar);
            detailEmpty = (TextView) view.findViewById(R.id.detail_empty);

            //shimmer
            shimmerContainer = (ShimmerFrameLayout) view.findViewById(R.id.shimmerContainer);
        }
    }
}
