package com.example.yeelin.projects.betweenus.fragment;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.adapter.SuggestionsAdapter;
import com.example.yeelin.projects.betweenus.loader.LoaderId;
import com.example.yeelin.projects.betweenus.loader.SuggestionsLoaderCallbacks;
import com.example.yeelin.projects.betweenus.model.YelpBusiness;
import com.example.yeelin.projects.betweenus.utils.AnimationUtils;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 7/13/15.
 */
public class SuggestionsFragment
        extends Fragment
        implements SuggestionsLoaderCallbacks.SuggestionsLoaderListener,
        AdapterView.OnItemClickListener {
    //logcat
    private static final String TAG = SuggestionsFragment.class.getCanonicalName();

    //bundle args
    private static final String ARG_SEARCH_TERM = SuggestionsFragment.class.getSimpleName() + ".searchTerm";
    private static final String ARG_USER_LOCATION = SuggestionsFragment.class.getSimpleName() + ".userLocation";
    private static final String ARG_FRIEND_LOCATION = SuggestionsFragment.class.getSimpleName() + ".friendLocation";

    //member variables
    private String searchTerm;
    private Location userLocation;
    private Location friendLocation;
    //listener
    private SuggestionsFragmentListener listener;

    /**
     * Listener interface
     */
    public interface SuggestionsFragmentListener {
        public void onSelectionComplete(ArrayList<String> selectedItemIds);
    }

    /**
     * Creates a new instance of the suggested places fragment
     * @param searchTerm
     * @param userLocation
     * @param friendLocation
     * @return
     */
    public static SuggestionsFragment newInstance(String searchTerm, Location userLocation, Location friendLocation) {
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_TERM, searchTerm);
        args.putParcelable(ARG_USER_LOCATION, userLocation);
        args.putParcelable(ARG_FRIEND_LOCATION, friendLocation);

        SuggestionsFragment fragment = new SuggestionsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Required empty constructor
     */
    public SuggestionsFragment() {}

    /**
     * Make sure either the activity or the parent fragment implements the listener interface
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Object objectToCast = getParentFragment() != null ? getParentFragment() : activity;
        try {
            listener = (SuggestionsFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName() + " must implement SuggestedPlacesFragmentListener");
        }
    }

    /**
     * Configure the fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            searchTerm = args.getString(ARG_SEARCH_TERM, "");
            userLocation = args.getParcelable(ARG_USER_LOCATION);
            friendLocation = args.getParcelable(ARG_FRIEND_LOCATION);
        }

        //notify that we have an options menu so that we will get a callback to create one later
        setHasOptionsMenu(true);
    }

    /**
     * Inflate the fragment's view
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggestions, container, false);
    }

    /**
     * Configure the fragment's view
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //set view holder
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        //set up the listview adapter
        viewHolder.suggestionsListView.setAdapter(new SuggestionsAdapter(view.getContext(), new ArrayList<YelpBusiness>()));
        viewHolder.suggestionsListView.setOnItemClickListener(this);
        viewHolder.suggestionsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        //initially make the listcontainer invisible and show the progress bar
        viewHolder.suggestionsListContainer.setVisibility(View.GONE);
        viewHolder.suggestionsProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Init the suggestions loader
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "onActivityCreated: Init Suggestions loader");
        SuggestionsLoaderCallbacks.initLoader(
                getActivity(),
                getLoaderManager(),
                this,
                searchTerm,
                userLocation,
                friendLocation);
    }

    /**
     * Configure the select button in the options menu
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_suggestions, menu);
    }

    /**
     * Handle the select and invite action
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select:
                //get all selected items
                ViewHolder viewHolder = getViewHolder();
                SparseBooleanArray sparseBooleanArray = viewHolder.suggestionsListView.getCheckedItemPositions();
                if (sparseBooleanArray.size() == 0) {
                    Log.d(TAG, "onOptionsItemSelected: Sparse boolean array is empty so nothing to do");
                    return true;
                }
                Log.d(TAG, "onOptionsItemSelected: Sparse boolean array size:" + sparseBooleanArray.size()); //note: //size != number of currently selected rows

                SuggestionsAdapter suggestionsAdapter = (SuggestionsAdapter) viewHolder.suggestionsListView.getAdapter();
                ArrayList<String> businessIds = new ArrayList<>(sparseBooleanArray.size());
                //sparse boolean array is map of key-value pairs
                //key = row in the listview, value = true/false to indicate whether row is selected
                //value would be false if the row was checked then unchecked
                for (int i=0; i<sparseBooleanArray.size(); i++) {
                    int key = sparseBooleanArray.keyAt(i);
                    boolean value = sparseBooleanArray.valueAt(i);

                    Log.d(TAG, String.format("onOptionsItemSelected: Index:%d, Key:%d, Value:%s", i, key, String.valueOf(value)));
                    //check if value is true (true means row is currently selected)
                    if (value) {
                        String id = suggestionsAdapter.getItem(key).getId();
                        String name = suggestionsAdapter.getItem(key).getName();
                        businessIds.add(id);
                        Log.d(TAG, String.format("onOptionsItemSelected: Index:%d, Key:%d, Value:%s, Id:%s, Name:%s",
                                i, key, String.valueOf(value), id, name));
                    }
                }

                if (businessIds.size() == 0) {
                    Log.d(TAG, "onOptionsItemSelected: BusinessIds size is 0 so nothing is currently selected");
                    return true;
                }

                Log.d(TAG, "onOptionsItemSelected: BusinessIds selected:" + businessIds);

                //pass it to the listener
                listener.onSelectionComplete(businessIds);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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
     * Return the view holder for the fragment's view if one exists.
     * @return
     */
    public ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /**
     * SuggestionsLoaderCallbacks.SuggestionsLoaderListener,
     * Loader callback with an updated arraylist. Update the view
     * @param loaderId
     * @param suggestedItems
     */
    @Override
    public void onLoadComplete(LoaderId loaderId, @Nullable ArrayList<YelpBusiness> suggestedItems) {
        ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) {
            //nothing to do since views are not ready yet
            Log.d(TAG, "onLoadComplete: View holder is null, so nothing to do");
            return;
        }

        if (loaderId == LoaderId.MULTI_PLACES) {
            SuggestionsAdapter suggestionsAdapter = (SuggestionsAdapter) viewHolder.suggestionsListView.getAdapter();
            if (suggestionsAdapter == null) {
                Log.d(TAG, "onLoadComplete: Suggestions adapter is null, so creating a new one");
                suggestionsAdapter = new SuggestionsAdapter(viewHolder.suggestionsListView.getContext(), suggestedItems);
                viewHolder.suggestionsListView.setAdapter(suggestionsAdapter);
            }
            else {
                Log.d(TAG, "onLoadComplete: Suggestions adapter is not null, so updating");
                suggestionsAdapter.updateAllItems(suggestedItems);
            }

            if (viewHolder.suggestionsListContainer.getVisibility() != View.VISIBLE) {
                AnimationUtils.crossFadeViews(getActivity(), viewHolder.suggestionsListContainer, viewHolder.suggestionsProgressBar);
            }
        }
        else {
            Log.d(TAG, "onLoadComplete: Unknown loaderId:" + loaderId);
        }
    }

    /**
     * AdapterView.OnItemClickListener
     * Item click callback when an item in the listview is clicked.  All we do here is toggle the checked textview
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick: Position clicked:" + position);

        SuggestionsAdapter.ViewHolder viewHolder = (SuggestionsAdapter.ViewHolder) view.getTag();
        viewHolder.suggestedItem.toggle();
    }

    /**
     * View Holder
     */
    private class ViewHolder {
        final View suggestionsListContainer;
        final ListView suggestionsListView;
        final View suggestionsProgressBar;

        ViewHolder(View view) {
            suggestionsListContainer = view.findViewById(R.id.suggestions_listContainer);
            suggestionsListView = (ListView) view.findViewById(R.id.suggestions_listView);
            suggestionsListView.setEmptyView(view.findViewById(R.id.suggestions_empty));
            suggestionsProgressBar = view.findViewById(R.id.suggestions_progressBar);
        }
    }
}
