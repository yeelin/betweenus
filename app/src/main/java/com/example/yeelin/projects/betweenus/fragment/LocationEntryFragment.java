package com.example.yeelin.projects.betweenus.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.utils.LocationUtils;
import com.example.yeelin.projects.betweenus.utils.PreferenceUtils;
import com.facebook.rebound.BaseSpringSystem;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;

/**
 * Created by ninjakiki on 7/13/15.
 */
public class LocationEntryFragment
        extends Fragment
        implements View.OnClickListener,
        View.OnTouchListener {
    //logcat
    private static final String TAG = LocationEntryFragment.class.getCanonicalName();

    //saved instance state
    private static final String STATE_USER_PLACE_ID = LocationEntryFragment.class.getSimpleName() + ".userPlaceId";
    private static final String STATE_FRIEND_PLACE_ID = LocationEntryFragment.class.getSimpleName() + ".friendPlaceId";
    private static final String STATE_USER_PLACE_DESC = LocationEntryFragment.class.getSimpleName() + ".userPlaceDesc";
    private static final String STATE_FRIEND_PLACE_DESC = LocationEntryFragment.class.getSimpleName() + ".friendPlaceDesc";

    //member variables
    private String userPlaceId;
    private String friendPlaceId;
    private String userPlaceDescription;
    private String friendPlaceDescription;

    private LocationEntryFragmentListener listener;

    //for rebound
    private final BaseSpringSystem springSystem = SpringSystem.create();
    private final ButtonSpringListener buttonSpringListener = new ButtonSpringListener();
    private Spring scaleSpring;

    /**
     * Listener interface. To be implemented by activity/fragment that is interested in events from this fragment
     */
    public interface LocationEntryFragmentListener {
        void onInputLocation(int locationType);
        void onSearch(String searchTerm, String userPlaceId, String friendPlaceId);
    }

    /**
     * Creates a new instance of location entry fragment
     * @return
     */
    public static LocationEntryFragment newInstance() {
        Bundle args = new Bundle();

        LocationEntryFragment fragment = new LocationEntryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Required empty constructor
     */
    public LocationEntryFragment() {}

    /**
     * Stores the placeId in the corresponding member variable and sets the description text in the field
     * for display to the user
     * @param locationType
     * @param placeId
     * @param description
     */
    public void setUserLocation(int locationType, @Nullable String placeId, @Nullable String description) {
        Log.d(TAG, String.format("setUserLocation: PlaceId:%s, Description:%s", placeId, description));

        if (placeId == null || description == null) {
            Log.d(TAG, "setUserLocation: Params are null, so nothing to do");
            return;
        }

        ViewHolder viewHolder = getViewHolder();
        switch (locationType) {
            case LocationUtils.USER_LOCATION:
                userPlaceId = placeId;
                userPlaceDescription = description;
                if (viewHolder != null) viewHolder.userLocation.setText(userPlaceDescription);
                break;

            case LocationUtils.FRIEND_LOCATION:
                friendPlaceId = placeId;
                friendPlaceDescription = description;
                if (viewHolder != null) viewHolder.friendLocation.setText(friendPlaceDescription);
                break;
        }
    }

    /**
     * Make sure either the activity or the parent fragment implements the listener interface.
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Object objectToCast = getParentFragment() != null ? getParentFragment() : context;
        try {
            listener = (LocationEntryFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName() + " must implement LocationEntryFragmentListener");
        }
    }

    /**
     * Configure the fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //create the animation spring
        scaleSpring = springSystem.createSpring();
        SpringConfig springConfig = scaleSpring.getSpringConfig();
        Log.d(TAG, String.format("onCreate: Before: Tension:%f, Friction:%f", springConfig.tension, springConfig.friction));
        scaleSpring.setSpringConfig(new SpringConfig(70, 15));

        //read saved instance state
        if (savedInstanceState != null) {
            userPlaceId = savedInstanceState.getString(STATE_USER_PLACE_ID);
            friendPlaceId = savedInstanceState.getString(STATE_FRIEND_PLACE_ID);
            userPlaceDescription = savedInstanceState.getString(STATE_USER_PLACE_DESC);
            friendPlaceDescription = savedInstanceState.getString(STATE_FRIEND_PLACE_DESC);
        }
    }

    /**
     * Inflate layout
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location_entry, container, false);
    }

    /**
     * Configure fragment's view
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //set the view holder
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        //add click listeners
        viewHolder.userLocation.setOnClickListener(this);
        viewHolder.friendLocation.setOnClickListener(this);
        viewHolder.searchButton.setOnClickListener(this);

        //add on touch listener to button
        viewHolder.searchButton.setOnTouchListener(this);

        //set user and friend locations
        setUserLocation(LocationUtils.USER_LOCATION, userPlaceId, userPlaceDescription);
        setUserLocation(LocationUtils.FRIEND_LOCATION, friendPlaceId, friendPlaceDescription);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Add a listener to the spring when the Activity resumes.
        scaleSpring.addListener(buttonSpringListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Remove the listener to the spring when the Activity pauses.
        scaleSpring.removeListener(buttonSpringListener);
    }

    /**
     * Save out the user or friend place id and description if they have been set
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (userPlaceId != null) outState.putString(STATE_USER_PLACE_ID, userPlaceId);
        if (friendPlaceId != null) outState.putString(STATE_FRIEND_PLACE_ID, friendPlaceId);
        if (userPlaceDescription != null) outState.putString(STATE_USER_PLACE_DESC, userPlaceDescription);
        if (friendPlaceDescription != null) outState.putString(STATE_FRIEND_PLACE_DESC, friendPlaceDescription);
    }

    /**
     * Nullify the listener
     */
    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    /**
     * View.OnClickListener implementation
     * This method is called when the search button is clicked
     * @param v
     */
    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: Search button was clicked");

        switch (v.getId()) {
            case R.id.user_location:
                listener.onInputLocation(LocationUtils.USER_LOCATION);
                break;
            case R.id.friend_location:
                listener.onInputLocation(LocationUtils.FRIEND_LOCATION);
                break;
//            case R.id.search_button:
//                if (userPlaceId != null && friendPlaceId != null) {
//                    listener.onSearch(searchTerm, userPlaceId, friendPlaceId);
//                }
//                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //when pressed start solving the spring to to 1
                scaleSpring.setEndValue(1);
                break;
            case MotionEvent.ACTION_UP:
                scaleSpring.setEndValue(0);
                break;
            case MotionEvent.ACTION_CANCEL:
                //when released start solving the spring to 0
                scaleSpring.setEndValue(0);
                break;
        }
        return true;
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
     * View holder
     */
    private class ViewHolder {
        final TextView userLocation;
        final TextView friendLocation;
        final Button searchButton;

        ViewHolder(View view) {
            userLocation = (TextView) view.findViewById(R.id.user_location);
            friendLocation = (TextView) view.findViewById(R.id.friend_location);
            searchButton = (Button) view.findViewById(R.id.search_button);
        }
    }

    /**
     * Handles spring callbacks
     */
    private class ButtonSpringListener extends SimpleSpringListener {
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
            viewHolder.searchButton.setScaleX(mappedValue);
            viewHolder.searchButton.setScaleY(mappedValue);
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
            if (spring.getEndValue() == 0) {
                if (userPlaceId != null && friendPlaceId != null) {
                    listener.onSearch(PreferenceUtils.getPreferredSearchTerm(getContext()), userPlaceId, friendPlaceId);
                }
            }
        }
    }
}
