package com.example.yeelin.projects.betweenus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
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

    /**
     * Constructor
     * @param context
     * @param items
     */
    public SuggestionsAdapter(Context context, List<YelpBusiness> items) {
        super(context, 0, items);
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
        //set the text
        viewHolder.suggestedItem.setText(getItem(position).getName());
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
        //remove all items from the list
        clear();

        //add all items to the end of the array
        if (newItems != null) {
            addAll(newItems);
        }
    }

    /**
     * ViewHolder class
     */
    public class ViewHolder {
        public final CheckedTextView suggestedItem;

        ViewHolder(View view) {
            suggestedItem = (CheckedTextView) view.findViewById(R.id.suggestion_item);
        }
    }
}
