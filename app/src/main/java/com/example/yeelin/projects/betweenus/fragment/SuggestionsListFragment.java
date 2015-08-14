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
import com.example.yeelin.projects.betweenus.model.YelpBusiness;
import com.example.yeelin.projects.betweenus.model.YelpResult;
import com.example.yeelin.projects.betweenus.utils.AnimationUtils;

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

        YelpBusiness business = (YelpBusiness) parent.getAdapter().getItem(position);
        suggestionActionListener.onSuggestionClick(business.getId(), business.getName());
    }

    /**
     * SuggestionsAdapter.OnItemToggleListener
     * Handles toggling of item in listview. Notify listener i.e. SuggestionsActivity.
     * @param id
     */
    @Override
    public void onItemToggle(String id) {
        suggestionActionListener.onSuggestionToggle(id);
    }

    /**
     * OnSuggestionsLoadedCallback implementation
     * The loader has finished fetching the data.  Called by SuggestionsActivity to update the view.
     * @param result
     * @param selectedIdsMap
     */
    public void onSuggestionsLoaded(@Nullable YelpResult result, @NonNull ArrayMap<String,String> selectedIdsMap) {
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
                    result != null ? result.getBusinesses() : null,
                    selectedIdsMap,
                    this);
            viewHolder.suggestionsListView.setAdapter(suggestionsAdapter);
        }
        else {
            Log.d(TAG, "onSuggestionsLoaded: Suggestions adapter is not null, so updating.");
            suggestionsAdapter.updateAllItems(
                    result != null ? result.getBusinesses() : null,
                    selectedIdsMap);
        }

        //second: animate in the list, and animate out the progress bar
        if (viewHolder.suggestionsListContainer.getVisibility() != View.VISIBLE) {
            AnimationUtils.crossFadeViews(getActivity(), viewHolder.suggestionsListContainer, viewHolder.suggestionsProgressBar);
        }
    }

    /**
     * OnSelectionChangedCallback implememtation
     * The contents of the selections array map has changed (even if the reference itself hasn't).
     * Ask the adapter to reload the list view.
     * TODO: Check if item is curerntly in view.  If it is, only reload that item.
     *
     * @param id id of the item whose selection has changed.
     */
    @Override
    public void onSelectionChanged(String id) {
        Log.d(TAG, "onSelectionChanged: Id:" + id);

        //check if views are null
        ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) {
            //nothing to do since views are not ready yet
            Log.d(TAG, "onSuggestionsLoaded: View holder is null, so nothing to do");
            return;
        }

        //views are not null, so update it
        //first: ask adapter to reload views
        SuggestionsAdapter suggestionsAdapter = (SuggestionsAdapter) viewHolder.suggestionsListView.getAdapter();
        if (suggestionsAdapter != null) {
            suggestionsAdapter.notifyDataSetChanged();
        }

        //second: animate in the list, and animate out the progress bar
        if (viewHolder.suggestionsListContainer.getVisibility() != View.VISIBLE) {
            AnimationUtils.crossFadeViews(getActivity(), viewHolder.suggestionsListContainer, viewHolder.suggestionsProgressBar);
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
