package com.example.yeelin.projects.betweenus.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.model.YelpBusiness;

import java.util.List;

/**
 * Created by ninjakiki on 7/21/15.
 */
public class SuggestionsAdapter extends ArrayAdapter<YelpBusiness> {
    //logcat
    private static final String TAG = SuggestionsAdapter.class.getCanonicalName();
    //member variables
    private List<YelpBusiness> items;

    /**
     * Constructor
     * @param context
     * @param items
     */
    public SuggestionsAdapter(Context context, List<YelpBusiness> items) {
        super(context, 0, items);
        this.items = items;
    }

    /**
     * Creates a new view or recycles the view
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_suggestion, parent, false);
            view.setTag(new ViewHolder(view));
        }

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        //set the views
        viewHolder.name.setText(getItem(position).getName());
        //set the checked state
        ListView listView = (ListView) parent;
        viewHolder.suggestedItem.setChecked(listView.isItemChecked(position));

        return view;
    }

    /**
     * Updates the adapter with a new list of items
     * @param newItems
     */
    public void updateAllItems(List<YelpBusiness> newItems) {
        //if it's the same items, do nothing. Otherwise, you end up clearing out newItems
        if (items == newItems) {
            Log.d(TAG, "updateAllItems: items == newItems. Nothing to do");
            return;
        }

        Log.d(TAG, "updateAllItems: Before clear. Item count:" + newItems.size());
        //remove all items from the current list
        clear();

        Log.d(TAG, "updateAllItems: After clear. Item count:" + newItems.size());
        //add all new items to the end of the array
        if (newItems != null) {
            this.items = newItems;
            addAll(newItems);
        }
    }

    /**
     * ViewHolder class
     */
    public class ViewHolder {
        public final CheckedTextView suggestedItem;
        final TextView name;
        final ImageView image;

        ViewHolder(View view) {
            suggestedItem = (CheckedTextView) view.findViewById(R.id.suggestion_item);

            name = (TextView) view.findViewById(R.id.suggestion_name);
            image = (ImageView) view.findViewById(R.id.suggestion_image);
        }
    }
}
