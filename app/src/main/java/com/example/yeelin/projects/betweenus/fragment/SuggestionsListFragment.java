package com.example.yeelin.projects.betweenus.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.adapter.SuggestionsAdapter;
import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalResult;
import com.example.yeelin.projects.betweenus.utils.AnimationUtils;
import com.google.android.gms.maps.model.LatLng;

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

    //member variables
    private OnSuggestionActionListener suggestionActionListener;

    /**
     * Creates a new instance of the list fragment
     * @return
     */
    public static SuggestionsListFragment newInstance() {
        return new SuggestionsListFragment();
    }

    /**
     * Required empty constructor
     */
    public SuggestionsListFragment() {}

    /**
     * Make sure the parent fragment or activity implements the suggestion click listener
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Object objectToCast = getParentFragment() != null ? getParentFragment() : activity;
        try {
            suggestionActionListener = (OnSuggestionActionListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName() + " must implement OnSuggestionActionListener");
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
     * Inflate the fragment's view
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

        //set up the listview
        viewHolder.suggestionsListView.setOnItemClickListener(this);
        viewHolder.suggestionsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        //initially make the listcontainer invisible and show the progress bar
        viewHolder.suggestionsListContainer.setVisibility(View.GONE);
        viewHolder.suggestionsProgressBar.setVisibility(View.VISIBLE);
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
     * AdapterView.OnItemClickListener
     * Item click callback when an item in the listview is clicked.  Notify listener
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
     * @param id business id
     * @param position position of item in list
     * @param toggleState resulting toggle state (true means selected, false means not selected)
     */
    @Override
    public void onItemToggle(String id, int position, boolean toggleState) {
        Log.d(TAG, String.format("onItemToggle:Id:%s, Position:%d, ToggleState:%s", id, position, toggleState));
        suggestionActionListener.onSuggestionToggle(id, position, toggleState);
    }

    /**
     * OnSuggestionsLoadedCallback implementation
     * The loader has finished fetching the data.  Called by SuggestionsActivity to update the view.
     * @param result
     * @param selectedIdsMap
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng 
     */
    public void onSuggestionsLoaded(@Nullable LocalResult result, @NonNull ArrayMap<String,Integer> selectedIdsMap,
                                    LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng) {
        //check if views are null
        ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) {
            //nothing to do since views are not ready yet
            Log.d(TAG, "onSuggestionsLoaded: View holder is null, so nothing to do");
            return;
        }

        //views are not null, so update it
        //first: update the adapter
        SuggestionsAdapter suggestionsAdapter = (SuggestionsAdapter) viewHolder.suggestionsListView.getAdapter();
        if (suggestionsAdapter == null) {
            Log.d(TAG, "onSuggestionsLoaded: Suggestions adapter is null, so creating a new one.");
            suggestionsAdapter = new SuggestionsAdapter(
                    viewHolder.suggestionsListView.getContext(),
                    result != null ? result.getLocalBusinesses() : null,
                    selectedIdsMap,
                    userLatLng, friendLatLng, midLatLng,
                    this);
            viewHolder.suggestionsListView.setAdapter(suggestionsAdapter);
        }
        else {
            Log.d(TAG, "onSuggestionsLoaded: Suggestions adapter is not null, so updating.");
            suggestionsAdapter.updateAllItems(
                    result != null ? result.getLocalBusinesses() : null,
                    selectedIdsMap,
                    userLatLng, friendLatLng, midLatLng);
        }

        //second: animate in the list, and animate out the progress bar
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
     * @param id id of the item whose selection has changed.
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

            for (int i=firstVisiblePosition; i<=lastVisiblePosition; i++) {
                //second: check if the business id is the same as the one we are looking for
                LocalBusiness business = (LocalBusiness) viewHolder.suggestionsListView.getItemAtPosition(i);
                if (id.equalsIgnoreCase(business.getId())) { //found it
                    Log.d(TAG, "onSelectionChanged: Found matching business id. Position:" + i);
                    //get the view corresponding to that row
                    View view = viewHolder.suggestionsListView.getChildAt(i-firstVisiblePosition);
                    //third: ask the adapter to update the selection state in the view to match the given toggleState
                    suggestionsAdapter.onSelectionChanged(view, toggleState);
                    break;
                }
                else {
                    Log.d(TAG, "onSelectionChanged: Not a match. Position:" + i);
                }
            }
        }
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
