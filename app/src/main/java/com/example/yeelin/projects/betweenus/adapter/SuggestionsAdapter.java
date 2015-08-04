package com.example.yeelin.projects.betweenus.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.util.SparseArray;
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
import com.example.yeelin.projects.betweenus.utils.ImageUtils;
import com.squareup.picasso.Target;

import java.util.ArrayList;
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

        YelpBusiness item = getItem(position);
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        //set the views
        ImageUtils.loadImage(parent.getContext(), item.getImage_url(), viewHolder.image);
        viewHolder.name.setText(item.getName());
        viewHolder.address.setText(item.getLocation().getAddress()[0]);
        viewHolder.categories.setText(item.getDisplayCategories());

        //set the textview and the yelp stars
        viewHolder.reviews.setText(getContext().getString(R.string.review_count, String.valueOf(item.getReview_count())));
        //note: picasso only keeps a weak ref to the target so it may be gc-ed
        //use setTag so that target will be alive as long as the view is alive
        final Target target = ImageUtils.newTarget(parent.getContext(), viewHolder.reviews);
        viewHolder.reviews.setTag(target);
        ImageUtils.loadImage(parent.getContext(), item.getRating_img_url_large(), target);

        //set the checked state
        ListView listView = (ListView) parent;
        viewHolder.itemToggle.setChecked(listView.isItemChecked(position));

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
        final ImageView image;

        final TextView name;
        final TextView address;
        final TextView categories;
        final TextView reviews;

        public final CheckedTextView itemToggle;

        ViewHolder(View view) {
            image = (ImageView) view.findViewById(R.id.item_image);
            name = (TextView) view.findViewById(R.id.item_name);
            address = (TextView) view.findViewById(R.id.item_address);
            categories = (TextView) view.findViewById(R.id.item_categories);
            reviews = (TextView) view.findViewById(R.id.item_reviews);
            itemToggle = (CheckedTextView) view.findViewById(R.id.item_toggle);
        }
    }
}
