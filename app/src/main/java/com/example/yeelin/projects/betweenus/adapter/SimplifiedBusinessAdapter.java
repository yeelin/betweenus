package com.example.yeelin.projects.betweenus.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.data.generic.model.SimplifiedBusiness;
import com.example.yeelin.projects.betweenus.utils.ImageUtils;
import com.squareup.picasso.Target;

import java.util.List;

/**
 * Created by ninjakiki on 8/19/15.
 */
public class SimplifiedBusinessAdapter
        extends ArrayAdapter<SimplifiedBusiness> {
    //logcat
    private static final String TAG = SimplifiedBusinessAdapter.class.getCanonicalName();
    //member variables
    private List<SimplifiedBusiness> businessList;

    /**
     * Constructor
     * @param context
     * @param businessList
     */
    public SimplifiedBusinessAdapter(Context context, List<SimplifiedBusiness> businessList) {
        super(context, 0, businessList);
        this.businessList = businessList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getView: Position:" + position);
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_simplified, parent, false);
            view.setTag(new ViewHolder(view));
        }

        SimplifiedBusiness business = getItem(position);
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        //set the views
        viewHolder.name.setText(business.getName());
        viewHolder.address.setText(business.getAddress());
        viewHolder.categories.setText(business.getCategories());
        viewHolder.reviews.setText(parent.getContext().getString(R.string.review_count, business.getReviews()));

        //load the images
        ImageUtils.loadImage(parent.getContext(), business.getImageUrl(), viewHolder.image);
        final Target target = ImageUtils.newTarget(parent.getContext(), viewHolder.reviews);
        viewHolder.reviews.setTag(target);
        ImageUtils.loadImage(parent.getContext(), business.getRatingUrl(), target);

        return view;
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

        ViewHolder(View view) {
            image = (ImageView) view.findViewById(R.id.item_image);
            name = (TextView) view.findViewById(R.id.item_name);
            address = (TextView) view.findViewById(R.id.item_address);
            categories = (TextView) view.findViewById(R.id.item_categories);
            reviews = (TextView) view.findViewById(R.id.item_reviews);
        }
    }
}
