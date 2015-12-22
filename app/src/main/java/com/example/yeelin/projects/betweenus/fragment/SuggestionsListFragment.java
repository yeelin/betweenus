package com.example.yeelin.projects.betweenus.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.adapter.SuggestionsAdapter;
import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalResult;
import com.example.yeelin.projects.betweenus.fragment.callback.OnSelectionChangedCallback;
import com.example.yeelin.projects.betweenus.fragment.callback.OnSuggestionActionListener;
import com.example.yeelin.projects.betweenus.fragment.callback.OnSuggestionsLoadedCallback;
import com.example.yeelin.projects.betweenus.utils.AnimationUtils;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 7/13/15.
 */
public class SuggestionsListFragment
        extends Fragment
        implements OnSuggestionsLoadedCallback, //tells fragment when data is loaded
        OnSelectionChangedCallback, //tells fragment when selections have changed
        AdapterView.OnItemClickListener, //tells fragment when an item in the list is clicked
        SuggestionsAdapter.OnItemToggleListener { //tells fragment when an item in the list is toggled
    //logcat
    private static final String TAG = SuggestionsListFragment.class.getCanonicalName();

    //saved instance state
    private static final String STATE_USER_LATLNG = SuggestionsListFragment.class.getSimpleName() + ".userLatLng";
    private static final String STATE_FRIEND_LATLNG = SuggestionsListFragment.class.getSimpleName() + ".friendLatLng";
    private static final String STATE_MID_LATLNG = SuggestionsListFragment.class.getSimpleName() + ".midLatLng";

    //member variables
    private LatLng userLatLng;
    private LatLng friendLatLng;
    private LatLng midLatLng;

    private boolean hasMoreData;
    private boolean isLoading = false;
    private OnSuggestionActionListener suggestionActionListener;

    /**
     * Creates a new instance of the list fragment
     *
     * @return
     */
    public static SuggestionsListFragment newInstance() {
        return new SuggestionsListFragment();
    }

    /**
     * Required empty constructor
     */
    public SuggestionsListFragment() {
    }

    /**
     * Make sure the parent fragment or activity implements the suggestion click listener
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Object objectToCast = getParentFragment() != null ? getParentFragment() : context;
        try {
            suggestionActionListener = (OnSuggestionActionListener) objectToCast;
        } catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName() + " must implement OnSuggestionActionListener");
        }
    }

    /**
     * Configure the fragment. Request that onCreateOptionsMenu be called later.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            userLatLng = savedInstanceState.getParcelable(STATE_USER_LATLNG);
            friendLatLng = savedInstanceState.getParcelable(STATE_FRIEND_LATLNG);
            midLatLng = savedInstanceState.getParcelable(STATE_MID_LATLNG);
        }
    }

    /**
     * Inflate the fragment's view
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggestions_list, container, false);
    }

    /**
     * Inflate the fragment's menus items
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_suggestions_list_fragment, menu);
    }

    /**
     * Configure the fragment's view
     *
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //set view holder
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        //set up the listview
        viewHolder.suggestionsListView.setOnItemClickListener(this);
        viewHolder.suggestionsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        viewHolder.suggestionsListView.setOnScrollListener(new InfiniteScrollListener());

        //initially make the listcontainer invisible and show the progress bar
        viewHolder.suggestionsListContainer.setVisibility(View.GONE);
        viewHolder.suggestionsProgressBar.setVisibility(View.VISIBLE);

        //setup the adapter
        SuggestionsAdapter suggestionsAdapter = new SuggestionsAdapter(
                    viewHolder.suggestionsListView.getContext(),
                    new ArrayList<LocalBusiness>(),
                    null,
                    userLatLng, friendLatLng, midLatLng,
                    this);
        viewHolder.suggestionsListView.setAdapter(suggestionsAdapter);
    }

    /**
     * Saves out the latlngs to save instance state so that they can be restored later.
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (userLatLng != null) outState.putParcelable(STATE_USER_LATLNG, userLatLng);
        if (friendLatLng != null) outState.putParcelable(STATE_FRIEND_LATLNG, friendLatLng);
        if (midLatLng != null) outState.putParcelable(STATE_MID_LATLNG, midLatLng);
    }

    /**
     * Nullify the click listener
     */
    @Override
    public void onDetach() {
        suggestionActionListener = null;
        super.onDetach();
    }

    /**
     * Handles user selection of menu options that were added by this fragment.
     * 1. Show as map
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_map:
                Log.d(TAG, "onOptionsItemSelected: User wants to see results in a map");
                suggestionActionListener.showMap();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * AdapterView.OnItemClickListener
     * Item click callback when an item in the listview is clicked.  Notify listener
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick: Position clicked:" + position);

        LocalBusiness business = (LocalBusiness) parent.getAdapter().getItem(position);
        suggestionActionListener.onSuggestionClick(
                business.getId(),
                business.getName(),
                business.getLocalBusinessLocation().getLatLng(),
                position);
    }

    /**
     * SuggestionsAdapter.OnItemToggleListener
     * Handles toggling of item in listview. Notify listener i.e. SuggestionsActivity.
     *
     * @param id          business id
     * @param position    position of item in list
     * @param toggleState resulting toggle state (true means selected, false means not selected)
     */
    @Override
    public void onItemToggle(String id, int position, boolean toggleState) {
        Log.d(TAG, String.format("onItemToggle:Id:%s, Position:%d, ToggleState:%s", id, position, toggleState));
        suggestionActionListener.onSuggestionToggle(id, position, toggleState);
    }

    /**
     * OnSuggestionsLoadedCallback implementation
     * The service has finished fetching the latlngs.  Called by SuggestionsActivity to update the latlngs in
     * this fragment
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng
     */
    public void onLatLngLoad(LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng) {
        setLatLng(userLatLng, friendLatLng, midLatLng);

        //pass latlngs to adapter
        ViewHolder viewHolder = getViewHolder();
        if (viewHolder !=  null) {
            final SuggestionsAdapter suggestionsAdapter = (SuggestionsAdapter) viewHolder.suggestionsListView.getAdapter();
            suggestionsAdapter.setLatLng(userLatLng, friendLatLng, midLatLng);
        }
    }

    /**
     * Setter for latlngs
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng
     */
    private void setLatLng(LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng) {
        this.userLatLng = userLatLng;
        this.friendLatLng = friendLatLng;
        this.midLatLng = midLatLng;
    }

    /**
     * OnSuggestionsLoadedCallback implementation
     * The data fragment has finished fetching a page of data.  Called by SuggestionsActivity to update the view.
     * @param localResult
     * @param selectedIdsMap
     * @param hasMoreData
     */
    public void onSinglePageLoad(@Nullable LocalResult localResult,
                                 @NonNull ArrayMap<String, Integer> selectedIdsMap,
                                 boolean hasMoreData) {
        //reset isLoading
        isLoading = false;
        if (localResult == null || localResult.getLocalBusinesses().size() == 0) {
            Log.d(TAG, "onSinglePageLoad: Local result is null or empty, so nothing to do");
            return;
        }

        //check if views are null
        ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) {
            //nothing to do since views are not ready yet
            Log.d(TAG, "onSinglePageLoad: View holder is null, so nothing to do");
            return;
        }

        //save the value of hasMoreData TODO: do we really need to?
        this.hasMoreData = hasMoreData;

        //first: update the adapter
        final SuggestionsAdapter suggestionsAdapter = (SuggestionsAdapter) viewHolder.suggestionsListView.getAdapter();
        suggestionsAdapter.updateItems(localResult.getLocalBusinesses(), selectedIdsMap);

        //second: animate in the list, and animate out the progress bar
        if (viewHolder.suggestionsListContainer.getVisibility() != View.VISIBLE) {
            AnimationUtils.crossFadeViews(getActivity(), viewHolder.suggestionsListContainer, viewHolder.suggestionsProgressBar);
        }
    }

    /**
     * SuggestionsLoadedCallback implementation
     * The data fragment has finished fetching multiple pages data.  Called by SuggestionsActivity to update the view.
     * @param localResultArrayList
     * @param selectedIdsMap
     * @param hasMoreData
     */
    @Override
    public void onMultiPageLoad(ArrayList<LocalResult> localResultArrayList,
                                @NonNull ArrayMap<String, Integer> selectedIdsMap,
                                boolean hasMoreData) {

        //reset isLoading
        isLoading = false;
        if (localResultArrayList == null || localResultArrayList.size() == 0) {
            Log.d(TAG, "onMultiPageLoad: Local result arraylist is null or empty, so nothing to do");
            return;
        }

        //check if views are null
        ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) {
            //nothing to do since views are not ready yet
            Log.d(TAG, "onMultiPageLoad: View holder is null, so nothing to do");
            return;
        }

        //save the value of hasMoreData TODO: should we really?
        this.hasMoreData = hasMoreData;

        //update the adapter
        final SuggestionsAdapter suggestionsAdapter = (SuggestionsAdapter) viewHolder.suggestionsListView.getAdapter();
        suggestionsAdapter.updateAllItems(localResultArrayList, selectedIdsMap);

        //animation
        if (viewHolder.suggestionsListContainer.getVisibility() != View.VISIBLE) {
            AnimationUtils.crossFadeViews(getActivity(), viewHolder.suggestionsListContainer, viewHolder.suggestionsProgressBar);
        }
    }

    /**
     * OnSelectionChangedCallback implementation
     * The contents of the selections array map has changed (even if the reference itself hasn't).
     * Check if the changed item is currently visible in the listview.  If it is, only update that item.
     * Otherwise, do nothing.
     *
     * @param id          id of the item whose selection has changed.
     * @param toggleState resulting toggle state (true means selected, false means not selected)
     */
    @Override
    public void onSelectionChanged(String id, boolean toggleState) {
        Log.d(TAG, String.format("onSelectionChanged: Id:%s, ToggleState:%s", id, toggleState));

        //check if views are null
        ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) {
            //nothing to do since views are not ready yet
            Log.d(TAG, "onSuggestionsLoaded: View holder is null, so nothing to do");
            return;
        }

        //views are not null, so update the changed row if necessary
        SuggestionsAdapter suggestionsAdapter = (SuggestionsAdapter) viewHolder.suggestionsListView.getAdapter();
        if (suggestionsAdapter != null) {
            //first: check if the changed row is between the first and last visible row
            //if it is, we will update the row, otherwise, there is no reason to do it now since view recycling will take care of it later
            int firstVisiblePosition = viewHolder.suggestionsListView.getFirstVisiblePosition();
            int lastVisiblePosition = viewHolder.suggestionsListView.getLastVisiblePosition();
            Log.d(TAG, String.format("onSelectionChanged: First visible:%d, Last visible:%d", firstVisiblePosition, lastVisiblePosition));

            for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++) {
                //second: check if the business id is the same as the one we are looking for
                LocalBusiness business = (LocalBusiness) viewHolder.suggestionsListView.getItemAtPosition(i);
                if (id.equalsIgnoreCase(business.getId())) { //found it
                    Log.d(TAG, "onSelectionChanged: Found matching business id. Position:" + i);
                    //get the view corresponding to that row
                    View view = viewHolder.suggestionsListView.getChildAt(i - firstVisiblePosition);
                    //third: ask the adapter to update the selection state in the view to match the given toggleState
                    suggestionsAdapter.onSelectionChanged(view, toggleState);
                    break;
                } else {
                    Log.d(TAG, "onSelectionChanged: Not a match. Position:" + i);
                }
            }
        }
    }

    /**
     * Return the view holder for the fragment's view if one exists.
     * @return
     */
    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
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

    /**
     * Listener for scroll events
     */
    private class InfiniteScrollListener implements AbsListView.OnScrollListener {
        /**
         * @param view
         * @param scrollState
         */
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        /**
         * @param view
         * @param firstVisibleItem
         * @param visibleItemCount
         * @param totalItemCount
         */
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (hasMoreData && !isLoading) {
                if ((totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleItemCount)) {
                    //we have at less than 1 screenful of data left to show, so go fetch more
                    isLoading = true;
                    suggestionActionListener.onMoreDataFetch();
                }
            }
        }
    }
}
