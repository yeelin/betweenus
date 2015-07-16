package com.example.yeelin.projects.betweenus.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.yeelin.projects.betweenus.R;

/**
 * Created by ninjakiki on 7/13/15.
 */
public class SuggestedPlacesFragment extends Fragment {
    //logcat
    private static final String TAG = SuggestedPlacesFragment.class.getCanonicalName();

    //listener
    private SuggestedPlacesFragmentListener listener;

    /**
     * Listener interface
     */
    public interface SuggestedPlacesFragmentListener {
        public void onSelectionComplete();
    }

    /**
     * Creates a new instance of the suggested places fragment
     * @return
     */
    public static SuggestedPlacesFragment newInstance() {
        Bundle args = new Bundle();

        SuggestedPlacesFragment fragment = new SuggestedPlacesFragment();
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Required empty constructor
     */
    public SuggestedPlacesFragment() {}

    /**
     * Make sure either the activity or the parent fragment implements the listener interface
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Object objectToCast = getParentFragment() != null ? getParentFragment() : null;
        try {
            listener = (SuggestedPlacesFragmentListener) objectToCast;
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
    }

    /**
     * Inflate the layout
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggested_places, container, false);
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

        //initially make the listcontainer invisible and show the progress bar
        viewHolder.suggestedPlacesListContainer.setVisibility(View.GONE);
        viewHolder.suggestedPlacesProgressBar.setVisibility(View.VISIBLE);
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
     * View Holder
     */
    private class ViewHolder {
        final View suggestedPlacesListContainer;
        final ListView suggestedPlacesListView;
        final View suggestedPlacesProgressBar;

        ViewHolder(View view) {
            suggestedPlacesListContainer = view.findViewById(R.id.suggestedPlaces_listContainer);
            suggestedPlacesListView = (ListView) view.findViewById(R.id.suggestedPlaces_listView);
            suggestedPlacesListView.setEmptyView(view.findViewById(R.id.suggestedPlaces_empty));
            suggestedPlacesProgressBar = view.findViewById(R.id.suggestedPlaces_progressBar);
        }
    }
}
