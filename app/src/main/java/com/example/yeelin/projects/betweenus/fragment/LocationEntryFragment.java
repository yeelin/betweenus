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

/**
 * Created by ninjakiki on 7/13/15.
 */
public class LocationEntryFragment
        extends Fragment
        implements View.OnClickListener {
    //logcat
    private static final String TAG = LocationEntryFragment.class.getCanonicalName();

    //member variables
    private Location userLocation;
    private Location friendLocation;
    private LocationEntryFragmentListener listener;

    /**
     * Listener interface. To be implemented by activity/fragment that is interested in events from this fragment
     */
    public interface LocationEntryFragmentListener {
        public void inputUserLocation();
        public void inputFriendLocation();
        public void onSearch(String userLocation, String friendLocation);
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

    public void setUserLocation(String name, double latitude, double longitude) {
        userLocation = new Location(name, latitude, longitude);

        ViewHolder viewHolder = getViewHolder();
        if (viewHolder != null) {
            viewHolder.userLocation.setText(name);
        }
    }

    public void setFriendLocation(String name, double latitude, double longitude) {
        friendLocation = new Location(name, latitude, longitude);

        ViewHolder viewHolder = getViewHolder();
        if (viewHolder != null) {
            viewHolder.friendLocation.setText(name);
        }
    }

    /**
     * Make sure either the activity or the parent fragment implements the listener interface.
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Object objectToCast = getParentFragment() != null ? getParentFragment() : activity;
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

        viewHolder.userLocation.setOnClickListener(this);
        viewHolder.friendLocation.setOnClickListener(this);
        viewHolder.searchButton.setOnClickListener(this);
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
                listener.inputUserLocation();
                break;
            case R.id.friend_location:
                listener.inputFriendLocation();
                break;
            case R.id.search_button:
                if (userLocation != null && friendLocation != null) {
                    listener.onSearch(userLocation.name, friendLocation.name);
                }
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

    private class Location {
        final String name;
        final double latitude;
        final double longitude;

        Location(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
