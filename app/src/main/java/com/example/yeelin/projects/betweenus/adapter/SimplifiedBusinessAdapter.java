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

        SimplifiedBusiness simplifiedBusiness = getItem(position);
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        //set the views
        //load the image
        //don't worry about imageUrl being null since Picasso will handle it and use the placeholder instead
        //checking simplifiedBusiness.getImageUrl() != null causes an issue here because if the view is recycled and the imageUrl
        //for the current simplifiedBusiness is null, then the imageView will not be cleared.
        ImageUtils.loadImage(parent.getContext(), simplifiedBusiness.getImageUrl(), viewHolder.image, R.drawable.ic_business_image_placeholder, R.drawable.ic_business_image_placeholder);

        //name
        viewHolder.name.setText(simplifiedBusiness.getName());

        //short address
        if (simplifiedBusiness.getAddress() == null) {
            viewHolder.address.setVisibility(View.GONE);
        }
        else {
            viewHolder.address.setVisibility(View.VISIBLE);
            viewHolder.address.setText(simplifiedBusiness.getAddress());
        }

        //category list
        final String[] categoryList = simplifiedBusiness.getCategoryList();
        if (categoryList == null) {
            viewHolder.categories.setVisibility(View.GONE);
        }
        else {
            viewHolder.categories.setVisibility(View.VISIBLE);
            StringBuilder builder = new StringBuilder(categoryList.length);
            for (int i=0; i<categoryList.length; i++) {
                builder.append(categoryList[i]);
                if (i < categoryList.length-1) builder.append(", ");
            }
            viewHolder.categories.setText(builder.toString());
        }

        //ratings and reviews
        if (simplifiedBusiness.getReviews() < 0 || simplifiedBusiness.getRating() < 0) {
            //we have fb data, so hide ratings and reviews
            viewHolder.ratingAndReviews.setVisibility(View.GONE);
        }
        else {
            //we have yelp data
            viewHolder.ratingAndReviews.setVisibility(View.VISIBLE);
            viewHolder.ratingAndReviews.setText(parent.getContext().getString(R.string.review_count, simplifiedBusiness.getReviews()));

            //load the rating stars
            final Target target = ImageUtils.newTarget(parent.getContext(), viewHolder.ratingAndReviews);
            viewHolder.ratingAndReviews.setTag(target);
            ImageUtils.loadImage(parent.getContext(), simplifiedBusiness.getRatingImageUrl(), target);
        }

        //likes
        if (simplifiedBusiness.getLikes() < 0) {
            //we have yelp data, so hide likes
            viewHolder.likes.setVisibility(View.GONE);
        }
        else {
            //we have fb data
            viewHolder.likes.setVisibility(View.VISIBLE);
            viewHolder.likes.setText(getContext().getResources().getQuantityString(
                    R.plurals.short_like_count,
                    simplifiedBusiness.getLikes(),
                    simplifiedBusiness.getLikes()));
        }

        //checkins
        if (simplifiedBusiness.getCheckins() < 0) {
            //we have yelp data, so hide checkins
            viewHolder.checkins.setVisibility(View.GONE);
        }
        else {
            //we have fb data
            viewHolder.checkins.setVisibility(View.VISIBLE);
            viewHolder.checkins.setText(getContext().getResources().getQuantityString(
                    R.plurals.short_checkin_count,
                    simplifiedBusiness.getCheckins(),
                    simplifiedBusiness.getCheckins()));
        }

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
        final TextView ratingAndReviews;
        final TextView likes;
        final TextView checkins;

        ViewHolder(View view) {
            image = (ImageView) view.findViewById(R.id.item_image);
            name = (TextView) view.findViewById(R.id.item_name);
            address = (TextView) view.findViewById(R.id.item_address);
            categories = (TextView) view.findViewById(R.id.item_categories);
            ratingAndReviews = (TextView) view.findViewById(R.id.item_rating_and_reviews);
            likes = (TextView) view.findViewById(R.id.item_likes);
            checkins = (TextView) view.findViewById(R.id.item_checkins);
        }
    }
}
