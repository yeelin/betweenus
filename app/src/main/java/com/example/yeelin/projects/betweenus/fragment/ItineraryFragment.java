package com.example.yeelin.projects.betweenus.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.adapter.ItineraryRecyclerAdapter;
import com.example.yeelin.projects.betweenus.cursorloader.ItineraryLoaderCallbacks;
import com.example.yeelin.projects.betweenus.cursorloader.callback.ItineraryLoaderListener;
import com.example.yeelin.projects.betweenus.provider.ItineraryContract;

/**
 * Created by ninjakiki on 3/30/16.
 */
public class ItineraryFragment
        extends Fragment
        implements ItineraryLoaderListener {

    private static final String TAG = ItineraryFragment.class.getCanonicalName();

    //specify the columns we need
    private static final String[] ITINERARY_COLUMNS = {
            ItineraryContract.Columns.ITINERARY_ID,
            ItineraryContract.Columns.CLOSEST_CITY,
            ItineraryContract.Columns.CLOSEST_CITY_LATITUDE,
            ItineraryContract.Columns.CLOSEST_CITY_LONGITUDE,
            ItineraryContract.Columns.NAME,
            ItineraryContract.Columns.EMAIL,
            ItineraryContract.Columns.PHONE,
            ItineraryContract.Columns.DATA_SOURCE,
            ItineraryContract.Columns.CREATED_DATETIME
    };

    //member variables
    private ItineraryRecyclerAdapter itineraryRecyclerAdapter;
    private ItineraryFragmentListener listener;

    public interface ItineraryFragmentListener {
        void onItineraryView(int itineraryId);
        void onItineraryReuse(int itineraryId);
    }

    public ItineraryFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Object objectToCast = getParentFragment() != null ? getParentFragment() : context;
        try {
            listener = (ItineraryFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName() + " must implement ItineraryFragmentListener");
        }
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_itinerary, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //get a reference to recycler view and attach this adapter to it
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.itinerary_recyclerview);
        View emptyView = view.findViewById(R.id.itinerary_empty);

        //setup the recycler view
        setupRecyclerView(recyclerView, emptyView);
    }

    /**
     * Helper method to setup the recycler view
     * @param recyclerView
     * @param emptyView
     */
    private void setupRecyclerView(RecyclerView recyclerView, View emptyView) {
        //set the layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));

        //the itinerary adapter will take data from a source and use it to populate the recycler view it's attached
        itineraryRecyclerAdapter = new ItineraryRecyclerAdapter(getContext(), new ItineraryRecyclerAdapter.ItineraryRecyclerAdapterOnClickHandler() {
            @Override
            public void onItemClick(int itineraryId, ItineraryRecyclerAdapter.ItineraryViewHolder itineraryViewHolder) {
                Log.d(TAG, "onItemClick: ItineraryId: " + itineraryId);
                listener.onItineraryView(itineraryId);
            }

            @Override
            public void onViewDetails(int itineraryId, ItineraryRecyclerAdapter.ItineraryViewHolder itineraryViewHolder) {
                Log.d(TAG, "onViewDetails: ItineraryId: " + itineraryId);
                listener.onItineraryView(itineraryId);
            }

            @Override
            public void onReuseClick(int itineraryId, ItineraryRecyclerAdapter.ItineraryViewHolder itineraryViewHolder) {
                Log.d(TAG, "onReuseClick: ItineraryId: " + itineraryId);
                listener.onItineraryReuse(itineraryId);
            }
        }, emptyView);

        //set the adapter
        recyclerView.setAdapter(itineraryRecyclerAdapter);
    }

    /**
     * Init the loader to load the itinerary history from db
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //init the loader to load itineraries/history
        ItineraryLoaderCallbacks.initLoader(ItineraryLoaderCallbacks.ITINERARY,
                getContext(),
                getLoaderManager(),
                this,
                ITINERARY_COLUMNS,
                null,
                null);
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    /**
     * ItineraryLoaderListener implementation
     * @param loaderId
     * @param cursor
     */
    @Override
    public void onLoadComplete(int loaderId, @Nullable Cursor cursor) {
        if (loaderId != ItineraryLoaderCallbacks.ITINERARY) {
            Log.d(TAG, "onLoadComplete: Unknown loaderId: " + loaderId);
            return;
        }

        //TODO: cross fade recycler view and the progress bar
        itineraryRecyclerAdapter.swapCursor(cursor);
    }
}
