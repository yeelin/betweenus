package com.example.yeelin.projects.betweenus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;

import java.util.List;

/**
 * Created by ninjakiki on 7/16/15.
 * Adapter for the search list view
 */
public class SearchAdapter extends ArrayAdapter<SearchResultItem> {
    //logcat
    private static final String TAG = SearchAdapter.class.getCanonicalName();

    /**
     * Constructor
     * @param context
     * @param objects
     */
    public SearchAdapter(Context context, List<SearchResultItem> objects) {
        super(context, 0, objects);
    }

    /**
     * Recycles a listview item.  If the view is null, then this method inflates a new view and then
     * sets the text on view.
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_search, parent, false);
        }

        TextView searchTextView = (TextView) view.findViewById(R.id.search_result_item);
        searchTextView.setText(getItem(position).getDescription());
        return view;
    }

    /**
     *
     * @param newItems
     */
    public void updateAllItems(List<SearchResultItem> newItems) {
        //remove all items from the list
        clear();
        //add all items to the end of the array
        addAll(newItems);
    }
}
