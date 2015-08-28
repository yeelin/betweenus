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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.loader.LoaderId;
import com.example.yeelin.projects.betweenus.loader.SingleSuggestionLoaderCallbacks;
import com.example.yeelin.projects.betweenus.model.YelpBusiness;
import com.example.yeelin.projects.betweenus.utils.AnimationUtils;
import com.example.yeelin.projects.betweenus.utils.FormattingUtils;
import com.example.yeelin.projects.betweenus.utils.ImageUtils;
import com.example.yeelin.projects.betweenus.utils.LocationUtils;
import com.example.yeelin.projects.betweenus.utils.MapColorUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
        implements SingleSuggestionLoaderCallbacks.SingleSuggestionLoaderListener,
        View.OnClickListener,
        OnMapReadyCallback {
    //logcat
    private static final String TAG = SuggestionDetailFragment.class.getCanonicalName();
    //bundle args
    private static final String ARG_ID = SuggestionDetailFragment.class.getSimpleName() + ".id";
    private static final String ARG_NAME = SuggestionDetailFragment.class.getSimpleName() + ".name";
    private static final String ARG_LATLNG = SuggestionDetailFragment.class.getSimpleName() + ".latLng";
    private static final String ARG_USER_LATLNG = SuggestionDetailFragment.class.getSimpleName() + ".userLatLng";
    private static final String ARG_FRIEND_LATLNG = SuggestionDetailFragment.class.getSimpleName() + ".friendLatLng";
    private static final String ARG_MID_LATLNG = SuggestionDetailFragment.class.getSimpleName() + ".midLatLng";
    private static final String ARG_TOGGLE_STATE = SuggestionDetailFragment.class.getSimpleName() + ".toggleState";

    //constants
    private static final int IMPERIAL = 0;
    private static final int METRIC = 1;

    private static final int EQUIDISTANT = 0;
    private static final int CLOSER_TO_USER = 1;
    private static final int CLOSER_TO_FRIEND = 2;

    //member variables
    private String id;
    private String name;
    private LatLng latLng;
    private boolean toggleState;

    private LatLng userLatLng;
    private LatLng friendLatLng;
    private LatLng midLatLng;

    private Marker marker;
    private YelpBusiness business;
    private SuggestionDetailFragmentListener listener;

    /**
     * Creates a new instance of this fragment
     * @param id
     * @param name
     * @param latLng
     * @param toggleState
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng midpoint between userLatLng and friendLatLng
     * @return
     */
    public static SuggestionDetailFragment newInstance(String id, String name, LatLng latLng, boolean toggleState,
                                                       LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng) {
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        args.putString(ARG_NAME, name);
        args.putParcelable(ARG_LATLNG, latLng);

        args.putParcelable(ARG_USER_LATLNG, userLatLng);
        args.putParcelable(ARG_FRIEND_LATLNG, friendLatLng);
        args.putParcelable(ARG_MID_LATLNG, midLatLng);

        args.putBoolean(ARG_TOGGLE_STATE, toggleState);

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
        public void onSelectionToggle();
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
            id = args.getString(ARG_ID);
            name = args.getString(ARG_NAME);
            latLng = args.getParcelable(ARG_LATLNG);
            toggleState = args.getBoolean(ARG_TOGGLE_STATE, false);

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
        viewHolder.selectButton.setCompoundDrawablesWithIntrinsicBounds(toggleState ? R.drawable.ic_action_detail_favorite : R.drawable.ic_action_detail_unfavorite, 0, 0, 0);
        viewHolder.selectButton.setText(toggleState ? R.string.selected_button : R.string.select_button);

        //set up click listeners
        viewHolder.websiteButton.setOnClickListener(this);
        viewHolder.phoneButton.setOnClickListener(this);
        viewHolder.selectButton.setOnClickListener(this);

        //initially make the detail container gone and show the progress bar
        viewHolder.detailContainer.setVisibility(View.GONE);
        viewHolder.detailProgressBar.setVisibility(View.VISIBLE);

        //set up the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.detail_mapContainer);
        if (mapFragment == null) {
            GoogleMapOptions googleMapOptions = new GoogleMapOptions()
                    .camera(new CameraPosition(latLng, SuggestionsMapFragment.DEFAULT_ZOOM, 0, 0))
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
                    .add(R.id.detail_mapContainer, mapFragment)
                    .commit();
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

        //initialize the loader to fetch details for this particular id from the network
        SingleSuggestionLoaderCallbacks.initLoader(
                getActivity(),
                getLoaderManager(),
                this,
                id);
    }

    /**
     * SingleSuggestionLoaderCallbacks.SingleSuggestionLoaderListener callback
     * When the loader delivers the results, this method would be called.  This method then calls updateView.
     * @param loaderId
     * @param business
     */
    @Override
    public void onLoadComplete(LoaderId loaderId, @Nullable YelpBusiness business) {
        if (loaderId != LoaderId.SINGLE_PLACE) {
            Log.d(TAG, "onLoadComplete: Unknown loaderId:" + loaderId);
            return;
        }

        this.business = business;
        //debugging purposes
        if (business == null) {
            Log.d(TAG, "onLoadComplete: Yelp business is null. Loader must be resetting");
            //animate in the detail empty textview, and animate out the progress bar
            ViewHolder viewHolder = getViewHolder();
            if (viewHolder != null && viewHolder.detailEmtpy.getVisibility() != View.VISIBLE) {
                viewHolder.detailContainer.setVisibility(View.GONE); //make sure container doesn't show
                AnimationUtils.crossFadeViews(getActivity(), viewHolder.detailEmtpy, viewHolder.detailProgressBar);
            }
            return;
        }
        else {
            Log.d(TAG, "onLoadComplete: Yelp business is not null. Updating views");
            //animate in the detail container, and animate out the progress bar
            ViewHolder viewHolder = getViewHolder();
            if (viewHolder != null && viewHolder.detailContainer.getVisibility() != View.VISIBLE) {
                viewHolder.detailEmtpy.setVisibility(View.GONE); //make sure empty view doesn't show
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
        if (business.getImage_url() != null) {
            ImageUtils.loadImage(getActivity(), business.getImage_url(), viewHolder.image, R.drawable.ic_business_image_placeholder, R.drawable.ic_business_image_placeholder);
        }

        //name
        viewHolder.name.setText(name);

        //categories
        final String categories = business.getDisplayCategories();
        viewHolder.categories.setText(categories != null ? categories : getString(R.string.not_available));

        //compute who is closer
        final int fairness = computeFairnessScore();
        final double distanceFromMidPoint = computeDistanceFromMidPoint(getUnitPreference());
        final String displayString = formatDistanceFromMidPointString(distanceFromMidPoint, fairness);
        viewHolder.distanceFromMidPoint.setText(displayString);

        //address
        final String fullAddress = business.getLocation() != null ? business.getLocation().getFullDisplayAddress() : null;
        viewHolder.address.setText(fullAddress != null ? fullAddress : getString(R.string.not_available));

        //cross streets
        final String crossStreets = business.getLocation() != null ? business.getLocation().getCross_streets() : null;
        viewHolder.crossStreets.setText(getString(R.string.detail_crossStreets, crossStreets != null ? crossStreets : getString(R.string.not_available)));

        //phone
        viewHolder.phone.setText(business.getDisplay_phone() != null ? business.getDisplay_phone() : getString(R.string.not_available));

        //web address
        viewHolder.webAddress.setText(business.getMobile_url() != null ? business.getMobile_url() : getString(R.string.not_available));

        //ratings and reviews
        viewHolder.reviews.setText(getString(R.string.review_count, String.valueOf(business.getReview_count())));
        //note: picasso only keeps a weak ref to the target so it may be gc-ed
        //use setTag so that target will be alive as long as the view is alive
        if (business.getRating_img_url_large() != null) {
            final Target target = ImageUtils.newTarget(getActivity(), viewHolder.reviews);
            viewHolder.reviews.setTag(target);
            ImageUtils.loadImage(getActivity(), business.getRating_img_url_large(), target);
        }

        //hours
        viewHolder.hoursRange.setText(R.string.not_available);
    }

    /**
     * Handles the different button clicks
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_website_button:
                listener.onOpenWebsite(business.getMobile_url());
                break;
            case R.id.detail_phone_button:
                listener.onDialPhone(business.getPhone());
                break;
            case R.id.detail_select_button:
                toggleState = !toggleState;
                ViewHolder viewHolder = getViewHolder();
                if (viewHolder != null) {
                    //update the select button icon and text
                    viewHolder.selectButton.setCompoundDrawablesWithIntrinsicBounds(toggleState ? R.drawable.ic_action_detail_favorite : R.drawable.ic_action_detail_unfavorite, 0, 0, 0);
                    viewHolder.selectButton.setText(toggleState ? R.string.selected_button : R.string.select_button);
                    //update the marker color
                    marker.setIcon(determineMarkerIcon());
                }
                listener.onSelectionToggle();
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
        //add marker
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(determineMarkerIcon());
        marker = googleMap.addMarker(markerOptions);

        //disable click listener
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.detail_mapContainer);
        if (mapFragment != null && mapFragment.getView() != null) {
            mapFragment.getView().setClickable(false);
        }
    }

    /**
     * Helper method that returns the correct hue based on the toggle state
     * @return
     */
    private BitmapDescriptor determineMarkerIcon() {
        return toggleState ?
                BitmapDescriptorFactory.defaultMarker(MapColorUtils.getInstance(getActivity()).getAccentDarkHue()) :
                BitmapDescriptorFactory.defaultMarker(MapColorUtils.getInstance(getActivity()).getPrimaryDarkHue());
    }

    /**
     * Compares the distance between the user and place with the distance between the friend and place.
     * Returns constants indicating if the place is closer to user, closer to friend, or equidistant.
     * @return
     */
    private int computeFairnessScore() {
        //compute distance between 1) user and place, 2) friend and place
        double userDistance = LocationUtils.computeDistanceBetween(userLatLng, latLng);
        double friendDistance = LocationUtils.computeDistanceBetween(friendLatLng, latLng);

        //compare the distance and return fairness score
        if (userDistance < friendDistance) {
            Log.d(TAG, String.format("computeFairnessScore: CLOSER_TO_USER. UserDistance:%.2f, FriendDistance:%.2f", userDistance, friendDistance));
            return CLOSER_TO_USER;
        }
        if (userDistance > friendDistance) {
            Log.d(TAG, String.format("computeFairnessScore: CLOSER_TO_FRIEND. UserDistance:%.2f, FriendDistance:%.2f", userDistance, friendDistance));
            return CLOSER_TO_FRIEND;
        }
        Log.d(TAG, String.format("computeFairnessScore: EQUIDISTANT. UserDistance:%.2f, FriendDistance:%.2f", userDistance, friendDistance));
        return EQUIDISTANT;
    }

    /**
     * Computes the distance between the place and midpoint. Returns distance in the preferred unit.
     * @param unitPreference
     * @return
     */
    private double computeDistanceFromMidPoint(int unitPreference) {
        //compute distance between place and mid point
        double distanceInMeters = LocationUtils.computeDistanceBetween(latLng, midLatLng);

        //return distance in meters or miles
        if (unitPreference == IMPERIAL) {
            double distanceInMiles = LocationUtils.convertMetersToMiles(distanceInMeters);
            Log.d(TAG, String.format("computeDistanceFromMidPoint: Distance(m):%.2f, Distance(mi):%.2f", distanceInMeters, distanceInMiles));
            return distanceInMiles;
        }
        Log.d(TAG, "computeDistanceFromMidPoint: Distance(m):" + distanceInMeters);
        return distanceInMeters;
    }

    /**
     * Formats a string which comprises of 1) distance between the place and midpoint, and 2) fairness score.
     * @param distanceFromMidPoint
     * @param fairness
     * @return
     */
    private String formatDistanceFromMidPointString(double distanceFromMidPoint, int fairness) {
        //get reference to a decimal formatter
        final DecimalFormat decimalFormatter = FormattingUtils.getDecimalFormatter();

        //build string
        StringBuilder stringBuilder = new StringBuilder();

        //build the distance between place and midpoint
        if (getUnitPreference() == IMPERIAL) {
            stringBuilder.append(getString(R.string.detail_distance_from_midPoint_miles, decimalFormatter.format(distanceFromMidPoint)));
        }
        else if (getUnitPreference() == METRIC) {
            if (distanceFromMidPoint > 1000) { //if using metric and distance is greater than 1000m, then show in km
                stringBuilder.append(getString(R.string.detail_distance_from_midPoint_km, decimalFormatter.format(distanceFromMidPoint / 1000.0)));
            }
            else {
                stringBuilder.append(getString(R.string.detail_distance_from_midPoint_meters, decimalFormatter.format(distanceFromMidPoint)));
            }
        }

        //append fairness score i.e. text that says whether the place is closer to user or friend
        stringBuilder.append(" ");
        switch (fairness) {
            case CLOSER_TO_USER:
                stringBuilder.append(getString(R.string.detail_closer_to_user));
                break;
            case CLOSER_TO_FRIEND:
                stringBuilder.append(getString(R.string.detail_closer_to_friend));
                break;
            default:
                stringBuilder.append(getString(R.string.detail_equidistant));
                break;
        }

        //done
        return stringBuilder.toString();
    }


    /**
     * Get user's preference for units.
     * TODO: Read this from preferences
     * @return
     */
    private int getUnitPreference() {
        return IMPERIAL;
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
        final TextView reviews;
        final TextView hoursRange;

        final Button websiteButton;
        final Button phoneButton;
        final Button selectButton;

        final View detailContainer;
        final View detailProgressBar;
        final TextView detailEmtpy;

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
            reviews = (TextView) view.findViewById(R.id.detail_reviews);
            hoursRange = (TextView) view.findViewById(R.id.detail_hours_range);

            //buttons
            websiteButton = (Button) view.findViewById(R.id.detail_website_button);
            phoneButton = (Button) view.findViewById(R.id.detail_phone_button);
            selectButton = (Button) view.findViewById(R.id.detail_select_button);

            //for animation
            detailContainer = view.findViewById(R.id.detail_container);
            detailProgressBar = view.findViewById(R.id.detail_progressBar);
            detailEmtpy = (TextView) view.findViewById(R.id.detail_empty);
        }
    }
}
