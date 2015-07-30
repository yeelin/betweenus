package com.example.yeelin.projects.betweenus.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.adapter.SuggestionsAdapter;
import com.example.yeelin.projects.betweenus.model.YelpBusiness;
import com.example.yeelin.projects.betweenus.utils.AnimationUtils;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 7/13/15.
 */
public class SuggestionsListFragment
        extends Fragment
        implements SuggestionsCallbacks,
        AdapterView.OnItemClickListener {
    //logcat
    private static final String TAG = SuggestionsListFragment.class.getCanonicalName();

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
     * The loader has finished fetching the data.  Called by SuggestionsActivity to update the view.
     * @param suggestedItems
     */
    public void onLoadComplete(@Nullable ArrayList<YelpBusiness> suggestedItems) {
        //debugging purposes
        if (suggestedItems == null) {
            Log.d(TAG, "onLoadComplete: SuggestedItems is null. Loader must be resetting");
        }
        else {
            Log.d(TAG, "onLoadComplete: Item count:" + suggestedItems.size());
        }

        ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) {
            //nothing to do since views are not ready yet
            Log.d(TAG, "onLoadComplete: View holder is null, so nothing to do");
            return;
        }

        //update the adapter
        SuggestionsAdapter suggestionsAdapter = (SuggestionsAdapter) viewHolder.suggestionsListView.getAdapter();
        if (suggestionsAdapter == null) {
            Log.d(TAG, "onLoadComplete: Suggestions adapter is null, so creating a new one. Item count:" + suggestedItems.size());
            suggestionsAdapter = new SuggestionsAdapter(viewHolder.suggestionsListView.getContext(), suggestedItems);
            viewHolder.suggestionsListView.setAdapter(suggestionsAdapter);
        }
        else {
            Log.d(TAG, "onLoadComplete: Suggestions adapter is not null, so updating. Item count:" + suggestedItems.size());
            suggestionsAdapter.updateAllItems(suggestedItems);
        }

        //animate in the list, and animate out the progress bar
        if (viewHolder.suggestionsListContainer.getVisibility() != View.VISIBLE) {
            AnimationUtils.crossFadeViews(getActivity(), viewHolder.suggestionsListContainer, viewHolder.suggestionsProgressBar);
        }
    }

    /**
     * The user has made his selections and wants to send the selected items off in an invite.
     * Return the business ids that were selected by the user.  Called by SuggestionActivity which manages
     * the menu items.
     * @return
     */
    @NonNull
    public ArrayList<String> onSelectAndSend() {
        //get all selected items
        ViewHolder viewHolder = getViewHolder();
        SparseBooleanArray sparseBooleanArray = viewHolder.suggestionsListView.getCheckedItemPositions();
        if (sparseBooleanArray.size() == 0) {
            Log.d(TAG, "onOptionsItemSelected: Sparse boolean array is empty so nothing to do");
            return new ArrayList<>();
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
            return new ArrayList<>();
        }

        Log.d(TAG, "onOptionsItemSelected: BusinessIds selected:" + businessIds);
        //return businessIds to the caller
        return businessIds;
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
