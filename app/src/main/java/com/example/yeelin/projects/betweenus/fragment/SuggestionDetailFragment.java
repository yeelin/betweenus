package com.example.yeelin.projects.betweenus.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.analytics.EventConstants;
import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.LocalTravelElement;
import com.example.yeelin.projects.betweenus.loader.SingleSuggestionLoaderCallbacks;
import com.example.yeelin.projects.betweenus.loader.callback.SingleSuggestionLoaderListener;
import com.example.yeelin.projects.betweenus.utils.AnimationUtils;
import com.example.yeelin.projects.betweenus.utils.FairnessScoringUtils;
import com.example.yeelin.projects.betweenus.utils.FormattingUtils;
import com.example.yeelin.projects.betweenus.utils.ImageUtils;
import com.example.yeelin.projects.betweenus.utils.LocationUtils;
import com.example.yeelin.projects.betweenus.utils.MapColorUtils;
import com.facebook.AccessToken;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.rebound.BaseSpringSystem;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
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

import java.text.DecimalFormat;

/**
 * Created by ninjakiki on 7/15/15.
 */
public class SuggestionDetailFragment
        extends Fragment
        implements
        SingleSuggestionLoaderListener,
        View.OnClickListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener, View.OnTouchListener {
    //logcat
    private static final String TAG = SuggestionDetailFragment.class.getCanonicalName();
    //bundle args
    private static final String ARG_ID = SuggestionDetailFragment.class.getSimpleName() + ".id";
    private static final String ARG_NAME = SuggestionDetailFragment.class.getSimpleName() + ".name";
    private static final String ARG_LATLNG = SuggestionDetailFragment.class.getSimpleName() + ".latLng";
    private static final String ARG_POSITION = SuggestionDetailFragment.class.getSimpleName() + ".position";
    private static final String ARG_TOGGLE_STATE = SuggestionDetailFragment.class.getSimpleName() + ".toggleState";

    private static final String ARG_RATING = SuggestionDetailFragment.class.getSimpleName() + ".rating";
    private static final String ARG_LIKES = SuggestionDetailFragment.class.getSimpleName() + ".likes";
    private static final String ARG_NORMALIZED_LIKES = SuggestionDetailFragment.class.getSimpleName() + ".normalizedLikes";
    private static final String ARG_TRAVEL_INFO = SuggestionDetailFragment.class.getSimpleName() + ".travelInfo";

    private static final String ARG_USER_LATLNG = SuggestionDetailFragment.class.getSimpleName() + ".userLatLng";
    private static final String ARG_FRIEND_LATLNG = SuggestionDetailFragment.class.getSimpleName() + ".friendLatLng";
    private static final String ARG_MID_LATLNG = SuggestionDetailFragment.class.getSimpleName() + ".midLatLng";
    private static final String ARG_DATA_SOURCE = SuggestionDetailFragment.class.getSimpleName() + ".dataSource";
    private static final String ARG_USE_METRIC = SuggestionDetailFragment.class.getSimpleName() + ".useMetric";

    //child fragment tags
    private static String FRAGMENT_TAG_LITEMAP = SupportMapFragment.class.getSimpleName();

    //member variables
    private String id;
    private String name;
    private LatLng latLng;
    private int position = 0;
    private boolean toggleState;
    private double rating;
    private int likes;
    private double normalizedLikes;
    private LocalTravelElement travelElement;

    private LatLng userLatLng;
    private LatLng friendLatLng;
    private LatLng midLatLng;
    private int preferredDataSource;
    private boolean useMetric;

    private Marker marker;
    private LocalBusiness business;
    private SuggestionDetailFragmentListener listener;

    //for rebound
    private final BaseSpringSystem springSystem = SpringSystem.create();
    private final ToggleSpringListener buttonSpringListener = new ToggleSpringListener();
    private Spring scaleSpring;

    /**
     * Creates a new instance of this fragment
     * @param id
     * @param name
     * @param latLng
     * @param position
     * @param toggleState
     * @param rating
     * @param likes
     * @param normalizedLikes
     * @param travelElement
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng midpoint between userLatLng and friendLatLng
     * @param dataSource user pref
     * @param useMetric user pref
     * @return
     */
    public static SuggestionDetailFragment newInstance(String id, String name, LatLng latLng,
                                                       int position, boolean toggleState, double rating, int likes, double normalizedLikes,
                                                       LocalTravelElement travelElement,
                                                       LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng,
                                                       @LocalConstants.DataSourceId int dataSource, boolean useMetric) {
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        args.putString(ARG_NAME, name);
        args.putParcelable(ARG_LATLNG, latLng);

        args.putInt(ARG_POSITION, position);
        args.putBoolean(ARG_TOGGLE_STATE, toggleState);
        args.putDouble(ARG_RATING, rating);
        args.putInt(ARG_LIKES, likes);
        args.putDouble(ARG_NORMALIZED_LIKES, normalizedLikes);
        args.putParcelable(ARG_TRAVEL_INFO, travelElement);

        args.putParcelable(ARG_USER_LATLNG, userLatLng);
        args.putParcelable(ARG_FRIEND_LATLNG, friendLatLng);
        args.putParcelable(ARG_MID_LATLNG, midLatLng);
        args.putInt(ARG_DATA_SOURCE, dataSource);
        args.putBoolean(ARG_USE_METRIC, useMetric);

        SuggestionDetailFragment fragment = new SuggestionDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Interface for activities or parent fragments interested in events from this fragment
     */
    public interface SuggestionDetailFragmentListener {
        void onOpenPhotos(int position);
        void onOpenMap(int position, boolean toggleState);
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
            rating = args.getDouble(ARG_RATING, LocalConstants.NO_DATA_DOUBLE);
            likes = args.getInt(ARG_LIKES, LocalConstants.NO_DATA_INTEGER);
            normalizedLikes = args.getDouble(ARG_NORMALIZED_LIKES, LocalConstants.NO_DATA_DOUBLE);
            travelElement = args.getParcelable(ARG_TRAVEL_INFO);

            //user and friend related info
            userLatLng = args.getParcelable(ARG_USER_LATLNG);
            friendLatLng = args.getParcelable(ARG_FRIEND_LATLNG);
            midLatLng = args.getParcelable(ARG_MID_LATLNG);

            //user preferences
            preferredDataSource = args.getInt(ARG_DATA_SOURCE);
            useMetric = args.getBoolean(ARG_USE_METRIC);
        }

        //init spring from Rebound Api
        scaleSpring = springSystem.createSpring();
        scaleSpring.addListener(buttonSpringListener);
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
        viewHolder.image.setOnClickListener(this);
        viewHolder.websiteButton.setOnClickListener(this);
        viewHolder.phoneButton.setOnClickListener(this);
//        viewHolder.selectButton.setOnClickListener(this); //use touch listener instead because of integration with Rebound Api
        viewHolder.selectButton.setOnTouchListener(this);

        //initially make the detail container gone and show the progress bar
        viewHolder.detailContainer.setVisibility(View.GONE);
        viewHolder.detailProgressBar.setVisibility(View.VISIBLE);

        //set up the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentByTag(FRAGMENT_TAG_LITEMAP);
        if (mapFragment == null) {
            Log.d(TAG, "onViewCreated: LiteMap fragment is null");
            GoogleMapOptions googleMapOptions = new GoogleMapOptions()
                    .camera(new CameraPosition(latLng, getResources().getInteger(R.integer.default_detail_map_zoom), 0, 0))
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

        //load the map asynchronously
        mapFragment.getMapAsync(this);
    }

    /**
     * Init the loader
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchPlaceDetails();
    }

    /**
     * Helper method that initializes the loader to fetch details for a particular
     * id from either Yelp or Facebook
     */
    private void fetchPlaceDetails() {
        int imageSizePx = getResources().getDimensionPixelSize(R.dimen.profile_image_size);

        switch (preferredDataSource) {
            case LocalConstants.FACEBOOK:
                //check if user is currently logged into fb
                if (AccessToken.getCurrentAccessToken() != null) {
                    Log.d(TAG, "fetchPlaceDetails: User is logged in");
                    //initialize the loader to fetch details for this particular id from fb
                    SingleSuggestionLoaderCallbacks.initLoader(SingleSuggestionLoaderCallbacks.SINGLE_PLACE, getActivity(), getLoaderManager(), this,
                            id, imageSizePx, imageSizePx, preferredDataSource);
                }
                else {
                    Log.d(TAG, "fetchPlaceDetails: User is not logged in");
                }
                break;

            case LocalConstants.YELP:
                //initialize the loader to fetch details for this particular id from Yelp
                SingleSuggestionLoaderCallbacks.initLoader(SingleSuggestionLoaderCallbacks.SINGLE_PLACE, getActivity(), getLoaderManager(), this,
                        id, imageSizePx, imageSizePx, preferredDataSource);
                break;

            case LocalConstants.GOOGLE:
                Log.d(TAG, "fetchPlaceDetails: Google has not been implemented as a data source");
                break;

            default:
                break;
        }
    }

    /**
     * Do some shimmering in case we load slowly.
     */
    @Override
    public void onResume() {
        super.onResume();

        //shimmer while we are loading
//        ViewHolder viewHolder = getViewHolder();
//        if (viewHolder != null) {
//            viewHolder.shimmerContainer.setDuration(400);
//            viewHolder.shimmerContainer.startShimmerAnimation();
//        }
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
    public void onLoadComplete(@SingleSuggestionLoaderCallbacks.SinglePlaceLoaderId int loaderId, @Nullable LocalBusiness business) {
        this.business = business;

        if (business == null) {
            Log.d(TAG, "onLoadComplete: Local business is null. Loader must be resetting");
            //animate in the detail empty textview, and animate out the progress bar
            ViewHolder viewHolder = getViewHolder();
            if (viewHolder != null && viewHolder.detailEmpty.getVisibility() != View.VISIBLE) {
                //stop shimmering
                //viewHolder.shimmerContainer.stopShimmerAnimation();

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
                //viewHolder.shimmerContainer.stopShimmerAnimation();

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
        if (business.getProfilePictureUrl() != null) {
            ImageUtils.loadImage(getActivity(), business.getProfilePictureUrl(), viewHolder.image, R.drawable.ic_business_image_placeholder, R.drawable.ic_business_image_placeholder);
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
        final double distanceDelta = FairnessScoringUtils.computeDistanceDelta(latLng, midLatLng, useMetric);
        final String displayString = FairnessScoringUtils.formatDistanceDeltaAndFairness(getActivity(), distanceDelta, fairness, useMetric, true);
        viewHolder.distanceFromMidPoint.setText(displayString);

        //travel info
        String travelInfo;
        final int distanceInMeters = travelElement.getTravelDistance();
        final int durationInSecs = travelElement.getTravelDuration();
        final double durationInMins = durationInSecs / FormattingUtils.ONE_MIN_IN_SECS;
        final DecimalFormat decimalFormatter = FormattingUtils.getDecimalFormatter(1);
        if (useMetric) { //metric
            if (distanceInMeters > FormattingUtils.ONE_KM_IN_METERS) { //check if more than 1000m then display in km
                travelInfo = getContext().getString(R.string.detail_travel_info_metric_km,
                        decimalFormatter.format(distanceInMeters / FormattingUtils.ONE_KM_IN_METERS),
                        decimalFormatter.format(durationInMins));
            }
            else {
                travelInfo = getContext().getString(R.string.detail_travel_info_metric_m,
                        decimalFormatter.format(distanceInMeters),
                        decimalFormatter.format(durationInMins));
            }
        }
        else { //imperial
            travelInfo = getContext().getString(R.string.detail_travel_info_imperial,
                    decimalFormatter.format(LocationUtils.convertMetersToMiles(distanceInMeters)),
                    decimalFormatter.format(durationInMins));
        }

        viewHolder.travelInfoFromUser.setText(travelInfo);

        //address
        final String fullAddress = business.getLocalBusinessLocation() != null ? business.getLocalBusinessLocation().getLongDisplayAddress() : null;
        viewHolder.address.setText(fullAddress != null ? fullAddress : getString(R.string.not_available));

        //cross streets
        final String crossStreets = business.getLocalBusinessLocation() != null ? business.getLocalBusinessLocation().getCrossStreets() : null;
        viewHolder.crossStreets.setText(getString(R.string.detail_crossStreets, crossStreets != null ? crossStreets : getString(R.string.not_available)));

        //phone
        viewHolder.phone.setText(business.getPhoneNumber() != null ? business.getPhoneNumber() : getString(R.string.not_available));

        //web address and fbAddress
        viewHolder.webAddress.setText(business.getMobileUrl() != null ? business.getMobileUrl() : getString(R.string.not_available));
        if (business.getFbLink() == null) viewHolder.fbAddress.setVisibility(View.GONE);
        else viewHolder.fbAddress.setText(business.getFbLink());

        //price range
        viewHolder.priceRange.setText(business.getPriceRangeString() != null ? business.getPriceRangeString() : getString(R.string.not_available));

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
                    R.plurals.detail_like_count,
                    business.getLikes(),
                    business.getLikes()));
            viewHolder.checkins.setText(getContext().getResources().getQuantityString(
                    R.plurals.detail_checkin_count,
                    business.getCheckins(),
                    business.getCheckins()));
        }

        //hours
        if (business.isAlwaysOpen()) {
            viewHolder.hoursRange.setText(R.string.always_open);
        }
        else {
            final String[] hoursArray = business.getHours();
            if (hoursArray == null) {
                viewHolder.hoursRange.setText(R.string.not_available);
            } else {
                StringBuilder builder = new StringBuilder(hoursArray.length);
                for (int i=0; i<hoursArray.length; i++) {
                    builder.append(hoursArray[i]);
                    if (i < hoursArray.length-1) builder.append("\n");
                }
                viewHolder.hoursRange.setText(builder.toString());
            }
        }

        //specialities
        final String[] specialitiesArray = business.getRestaurantSpecialities();
        if (specialitiesArray == null) {
            viewHolder.specialities.setText(R.string.not_available);
        } else {
            StringBuilder builder = new StringBuilder(specialitiesArray.length);
            for (int i=0; i<specialitiesArray.length; i++) {
                builder.append(specialitiesArray[i]);
                if (i < specialitiesArray.length-1) builder.append(", ");
            }
            viewHolder.specialities.setText(builder.toString());
        }

        //services
        final String[] servicesArray = business.getRestaurantServices();
        if (servicesArray == null) {
            viewHolder.services.setText(R.string.not_available);
        } else {
            StringBuilder builder = new StringBuilder(servicesArray.length);
            for (int i=0; i<servicesArray.length; i++) {
                builder.append(servicesArray[i]);
                if (i < servicesArray.length-1) builder.append(", ");
            }
            viewHolder.services.setText(builder.toString());
        }

        //parking
        final String[] parkingArray = business.getParking();
        if (parkingArray == null) {
            viewHolder.parking.setText(R.string.not_available);
        } else {
            StringBuilder builder = new StringBuilder(parkingArray.length);
            for (int i=0; i<parkingArray.length; i++) {
                builder.append(parkingArray[i]);
                if (i < parkingArray.length-1) builder.append(", ");
            }
            viewHolder.parking.setText(builder.toString());
        }

        //payment options
        final String[] paymentOptionsArray = business.getPaymentOptions();
        if (paymentOptionsArray == null) {
            viewHolder.paymentOptions.setText(R.string.not_available);
        } else {
            StringBuilder builder = new StringBuilder(paymentOptionsArray.length);
            for (int i=0; i<paymentOptionsArray.length; i++) {
                builder.append(paymentOptionsArray[i]);
                if (i < paymentOptionsArray.length-1) builder.append(", ");
            }
            viewHolder.paymentOptions.setText(builder.toString());
        }

        //culinary team
        viewHolder.culinaryTeam.setText(business.getCulinaryTeam() != null ? business.getCulinaryTeam() : getString(R.string.not_available));

        //description (if description is null, use about. if both are null, then show "not available")
        final String description = business.getDescription() != null ? business.getDescription() : business.getAbout();
        viewHolder.description.setText(description != null ? description : getString(R.string.not_available));
    }

    /**
     * Handles the different button clicks
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_image:
                Log.d(TAG, "onClick: Detail image clicked");
                listener.onOpenPhotos(position);
                break;
            case R.id.detail_website_button:
                listener.onOpenWebsite(business.getMobileUrl());
                break;
            case R.id.detail_phone_button:
                listener.onDialPhone(business.getPhoneNumber());
                break;
//            case R.id.detail_select_button:
//                toggleState = !toggleState;
//
//                final ViewHolder viewHolder = getViewHolder();
//                if (viewHolder != null) {
//                    //update the select button icon
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
//                        viewHolder.selectButton.setCompoundDrawablesRelativeWithIntrinsicBounds(toggleState ? R.drawable.ic_action_detail_favorite_red300 : R.drawable.ic_action_detail_favorite, 0, 0, 0);
//                    else
//                        viewHolder.selectButton.setCompoundDrawablesWithIntrinsicBounds(toggleState ? R.drawable.ic_action_detail_favorite_red300 : R.drawable.ic_action_detail_favorite, 0, 0, 0);
//                    //update the select button text
//                    viewHolder.selectButton.setText(toggleState ? R.string.selected_button : R.string.select_button);
//                    //update the marker color
//                    marker.setIcon(MapColorUtils.determineMarkerIcon(toggleState, rating != LocalConstants.NO_DATA_DOUBLE ? rating : normalizedLikes));
//                }
//
//                //log user selection from the detail/pager view
//                Log.d(TAG, "onClick: Start logging selection");
//                AppEventsLogger logger = AppEventsLogger.newLogger(getContext());
//                Bundle parameters = new Bundle();
//                parameters.putString(EventConstants.EVENT_PARAM_SELECTION_VIEW, EventConstants.EVENT_PARAM_VIEW_PAGER);
//                parameters.putInt(EventConstants.EVENT_PARAM_SELECTION_DATA_SOURCE, business.getDataSource());
//                switch (business.getDataSource()) {
//                    case LocalConstants.FACEBOOK:
//                        parameters.putString(EventConstants.EVENT_PARAM_SELECTION_PRICE, business.getPriceRangeString());
//                        parameters.putInt(EventConstants.EVENT_PARAM_SELECTION_LIKES, business.getLikes());
//                        parameters.putInt(EventConstants.EVENT_PARAM_SELECTION_CHECKINS, business.getCheckins());
//                        break;
//                    case LocalConstants.YELP:
//                        parameters.putDouble(EventConstants.EVENT_PARAM_SELECTION_RATING, business.getRating());
//                        parameters.putInt(EventConstants.EVENT_PARAM_SELECTION_REVIEWS, business.getReviewCount());
//                        break;
//                    case LocalConstants.GOOGLE:
//                        break;
//                }
//                logger.logEvent(EventConstants.EVENT_NAME_ADDED_TO_SELECTION, parameters);
//                logger.flush();
//                Log.d(TAG, "onClick: End logging selection");
//
//                listener.onToggle(id, position, toggleState);
//                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final Button selectButton = (Button)v;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //toggle the state
                toggleState = !toggleState;

                //when pressed start solving the spring to to 1
                scaleSpring.setEndValue(1);

                //update the select button icon
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    selectButton.setCompoundDrawablesRelativeWithIntrinsicBounds(toggleState ? R.drawable.ic_action_detail_favorite_red300 : R.drawable.ic_action_detail_favorite, 0, 0, 0);
                else
                    selectButton.setCompoundDrawablesWithIntrinsicBounds(toggleState ? R.drawable.ic_action_detail_favorite_red300 : R.drawable.ic_action_detail_favorite, 0, 0, 0);
                //update the select button text
                selectButton.setText(toggleState ? R.string.selected_button : R.string.select_button);
                //update the marker color
                marker.setIcon(MapColorUtils.determineMarkerIcon(toggleState, rating != LocalConstants.NO_DATA_DOUBLE ? rating : normalizedLikes));
                break;

            case MotionEvent.ACTION_UP:
                //when released start solving the spring to 0
                scaleSpring.setEndValue(0);

                //log user selection from the detail/pager view
                AppEventsLogger logger = AppEventsLogger.newLogger(getContext());
                Bundle parameters = new Bundle();
                parameters.putString(EventConstants.EVENT_PARAM_SELECTION_VIEW, EventConstants.EVENT_PARAM_VIEW_PAGER);
                parameters.putInt(EventConstants.EVENT_PARAM_SELECTION_DATA_SOURCE, business.getDataSource());
                switch (business.getDataSource()) {
                    case LocalConstants.FACEBOOK:
                        parameters.putString(EventConstants.EVENT_PARAM_SELECTION_PRICE, business.getPriceRangeString());
                        parameters.putInt(EventConstants.EVENT_PARAM_SELECTION_LIKES, business.getLikes());
                        parameters.putInt(EventConstants.EVENT_PARAM_SELECTION_CHECKINS, business.getCheckins());
                        break;
                    case LocalConstants.YELP:
                        parameters.putDouble(EventConstants.EVENT_PARAM_SELECTION_RATING, business.getRating());
                        parameters.putInt(EventConstants.EVENT_PARAM_SELECTION_REVIEWS, business.getReviewCount());
                        break;
                    case LocalConstants.GOOGLE:
                        break;
                }
                logger.logEvent(EventConstants.EVENT_NAME_ADDED_TO_SELECTION, parameters);

                //notify the parent activity
                listener.onToggle(id, position, toggleState);
                break;

            case MotionEvent.ACTION_CANCEL:
                //undo the earlier toggle state because it was a cancel
                toggleState = !toggleState;

                //when released start solving the spring to 0
                scaleSpring.setEndValue(0);
                break;
        }
        return false;
    }

    /**
     * OnMapReadyCallback callback
     * Add a single marker to the map
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");

        //intercept clicks on the map
        googleMap.setOnMapClickListener(this);

        //add marker
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(MapColorUtils.determineMarkerIcon(toggleState, rating != LocalConstants.NO_DATA_DOUBLE ? rating : normalizedLikes));
        marker = googleMap.addMarker(markerOptions);
    }

    /**
     * GoogleMap.OnMapClickListener callback.
     * Override the default behavior of opening the google maps app.
     * TODO: Open an interactive map within the app.
     * @param latLng
     */
    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick: The map was clicked");
        //notify the activity listener that the map was clicked to start the interactive map activity
        listener.onOpenMap(position, toggleState);
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
        final TextView travelInfoFromUser;
        final TextView address;
        final TextView crossStreets;
        final TextView phone;
        final TextView webAddress;
        final TextView fbAddress;
        final TextView priceRange;
        final TextView reviews;
        final TextView checkins;
        final TextView hoursRange;
        final TextView specialities;
        final TextView services;
        final TextView parking;
        final TextView paymentOptions;
        final TextView culinaryTeam;
        final TextView description;

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
            travelInfoFromUser = (TextView) view.findViewById(R.id.detail_travel_info_from_user);
            address = (TextView) view.findViewById(R.id.detail_address);
            crossStreets = (TextView) view.findViewById(R.id.detail_crossStreets);
            phone = (TextView) view.findViewById(R.id.detail_phone);
            webAddress = (TextView) view.findViewById(R.id.detail_webAddress);
            fbAddress = (TextView) view.findViewById(R.id.detail_fbAddress);
            priceRange = (TextView) view.findViewById(R.id.detail_price_range);
            reviews = (TextView) view.findViewById(R.id.detail_reviews);
            checkins = (TextView) view.findViewById(R.id.detail_checkins);
            hoursRange = (TextView) view.findViewById(R.id.detail_hours_range);
            specialities = (TextView) view.findViewById(R.id.detail_specialities);
            services = (TextView) view.findViewById(R.id.detail_services);
            parking = (TextView) view.findViewById(R.id.detail_parking);
            paymentOptions = (TextView) view.findViewById(R.id.detail_payment_options);
            culinaryTeam = (TextView) view.findViewById(R.id.detail_culinary_team);
            description = (TextView) view.findViewById(R.id.detail_description);

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

    /**
     * Handles spring callbacks
     */
    private class ToggleSpringListener extends SimpleSpringListener {
        /**
         * called whenever the spring leaves its resting state
         * @param spring
         */
        @Override
        public void onSpringActivate(Spring spring) {
        }

        /**
         * Called whenever the spring is updated
         * @param spring
         */
        @Override
        public void onSpringUpdate(Spring spring) {
            // On each update of the spring value, we adjust the scale of the image view to match the
            // springs new value. We use the SpringUtil linear interpolation function mapValueFromRangeToRange
            // to translate the spring's 0 to 1 scale to a 100% to 50% scale range and apply that to the View
            // with setScaleX/Y. Note that rendering is an implementation detail of the application and not
            // Rebound itself. If you need Gingerbread compatibility consider using NineOldAndroids to update
            // your view properties in a backwards compatible manner.
            ViewHolder viewHolder = getViewHolder();
            if (viewHolder == null) return;

            float mappedValue = (float) SpringUtil.mapValueFromRangeToRange(spring.getCurrentValue(), 0, 1, 1, 0.5);
            viewHolder.selectButton.setScaleX(mappedValue);
            viewHolder.selectButton.setScaleY(mappedValue);
        }

        /**
         * called whenever the spring notifies of displacement state changes
         * @param spring
         */
        @Override
        public void onSpringEndStateChange(Spring spring) {
        }

        /**
         * called whenever the spring achieves a resting state
         * @param spring
         */
        @Override
        public void onSpringAtRest(Spring spring) {
        }
    }
}
