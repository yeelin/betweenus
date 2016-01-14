package com.example.yeelin.projects.betweenus.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.analytics.EventConstants;
import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.LocalResult;
import com.example.yeelin.projects.betweenus.utils.FairnessScoringUtils;
import com.example.yeelin.projects.betweenus.utils.ImageUtils;
import com.example.yeelin.projects.betweenus.utils.PreferenceUtils;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.rebound.BaseSpringSystem;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ninjakiki on 7/21/15.
 */
public class SuggestionsAdapter
        extends ArrayAdapter<LocalBusiness>
        implements
        View.OnTouchListener {
    //logcat
    private static final String TAG = SuggestionsAdapter.class.getCanonicalName();
    //member variables
    //private List<LocalBusiness> businessList;
    private ArrayMap<String,Integer> selectedIdsMap;
    private LatLng userLatLng;
    private LatLng friendLatLng;
    private LatLng midLatLng;
    private boolean useMetric;

    //listener
    private OnItemToggleListener listener;

    //for rebound
    private final BaseSpringSystem springSystem = SpringSystem.create();
    private final ToggleSpringListener buttonSpringListener = new ToggleSpringListener();
    private Spring scaleSpring;

    //views
    private CheckedTextView toggledView;

    /**
     * Interface for listening to toggling of the checked text view
     */
    public interface OnItemToggleListener {
        /**
         * Handle the toggling of an item in the listview
         * @param id business id of item toggled
         * @param position position of item toggled
         * @param toggleState resulting toggle state
         */
        void onItemToggle(String id, int position, boolean toggleState);
    }

    /**
     * Constructor
     * @param context
     * @param businessList
     * @param selectedIdsMap
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng
     * @param useMetric
     * @param listener
     */
    public SuggestionsAdapter(Context context, @Nullable List<LocalBusiness> businessList,
                              ArrayMap<String,Integer> selectedIdsMap,
                              LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng,
                              boolean useMetric,
                              OnItemToggleListener listener) {
        super(context, 0, businessList);

        this.selectedIdsMap = selectedIdsMap;
        this.userLatLng = userLatLng;
        this.friendLatLng = friendLatLng;
        this.midLatLng = midLatLng;
        this.useMetric = useMetric;

        this.listener = listener;

        scaleSpring = springSystem.createSpring();
        scaleSpring.addListener(buttonSpringListener);
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

        LocalBusiness business = getItem(position);
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        //set the views
        //image
        //don't worry about imageUrl being null since Picasso will handle it and use the placeholder instead
        //checking business.getProfilePictureUrl() != null causes an issue here because if the view is recycled and the imageUrl
        //for the current business is null, then the imageView will not be cleared.
        ImageUtils.loadImage(parent.getContext(), business.getProfilePictureUrl(), viewHolder.image, R.drawable.ic_business_image_placeholder, R.drawable.ic_business_image_placeholder);

        //name
        viewHolder.name.setText(business.getName());

        //short address
        if (business.getLocalBusinessLocation() == null) {
            viewHolder.address.setVisibility(View.GONE);
        }
        else {
            final String shortAddress = business.getLocalBusinessLocation().getShortDisplayAddress();
            if (shortAddress == null) {
                viewHolder.address.setVisibility(View.GONE);
            }
            else {
                viewHolder.address.setVisibility(View.VISIBLE);
                viewHolder.address.setText(shortAddress);
            }
        }

        //category list
        final String[] categoryList = business.getCategoryList();
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
        if (business.getReviewCount() < 0 || business.getRating() < 0) {
            //we have fb data, so hide ratings and reviews
            viewHolder.ratingAndReviews.setVisibility(View.GONE);
        }
        else {
            //we have yelp data
            viewHolder.ratingAndReviews.setVisibility(View.VISIBLE);
            viewHolder.ratingAndReviews.setText(getContext().getString(R.string.review_count, business.getReviewCount()));
            //note: picasso only keeps a weak ref to the target so it may be gc-ed
            //use setTag so that target will be alive as long as the view is alive
            if (business.getRatingImageUrl() != null) {
                final Target target = ImageUtils.newTarget(parent.getContext(), viewHolder.ratingAndReviews);
                viewHolder.ratingAndReviews.setTag(target);
                ImageUtils.loadImage(parent.getContext(), business.getRatingImageUrl(), target);
            }
        }

        //likes
        if (business.getLikes() < 0) {
            //we have yelp data, so hide likes
            viewHolder.likes.setVisibility(View.GONE);
        }
        else {
            //we have fb data
            viewHolder.likes.setVisibility(View.VISIBLE);
            viewHolder.likes.setText(getContext().getResources().getQuantityString(
                    R.plurals.short_like_count,
                    business.getLikes(),
                    business.getLikes()));
        }

        //checkins
        if (business.getCheckins() < 0) {
            //we have yelp data, so hide checkins
            viewHolder.checkins.setVisibility(View.GONE);
        }
        else {
            //we have fb data
            viewHolder.checkins.setVisibility(View.VISIBLE);
            viewHolder.checkins.setText(getContext().getResources().getQuantityString(
                    R.plurals.short_checkin_count,
                    business.getCheckins(),
                    business.getCheckins()));
        }

        //price range
        final String priceRange = business.getPriceRangeString();
        if (priceRange == null) {
            viewHolder.priceRange.setVisibility(View.GONE);
        }
        else {
            viewHolder.priceRange.setVisibility(View.VISIBLE);
            viewHolder.priceRange.setText(priceRange);
        }
        //set the checked state
        viewHolder.itemToggle.setChecked(selectedIdsMap.containsKey(business.getId()));
        viewHolder.itemToggle.setTag(R.id.business_id, business.getId());
        viewHolder.itemToggle.setTag(R.id.position, position);
        //viewHolder.itemToggle.setOnClickListener(this); //use touch listener instead because of integration with Rebound Api
        viewHolder.itemToggle.setOnTouchListener(this);

        //compute fairness and distance from center
        final LatLng businessLatLng = business.getLocalBusinessLocation().getLatLng();
        final int fairness = FairnessScoringUtils.computeFairnessScore(userLatLng, friendLatLng, businessLatLng);
        final double distanceDelta = FairnessScoringUtils.computeDistanceDelta(businessLatLng, midLatLng, useMetric);

        //set distance from center
        viewHolder.distanceFromMidPoint.setText(FairnessScoringUtils.formatDistanceDelta(getContext(), distanceDelta, useMetric, false));

        //set fairness score
        viewHolder.fairnessScore.setText(FairnessScoringUtils.formatFairnessScore(getContext(), fairness));

        return view;
    }

    /**
     * Provide latlngs to the adapter.
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng
     */
    public void setLatLng(LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng) {
        this.userLatLng = userLatLng;
        this.friendLatLng = friendLatLng;
        this.midLatLng = midLatLng;
    }

    /**
     * Adds the new array of businesses to the end of the adapter's collection.
     * Notifies the listeners that the data has changed which causes the current view to be refreshed.
     * @param newBusinesses
     * @param newSelectedIdsMap
     */
    public void updateItems(@Nullable ArrayList<LocalBusiness> newBusinesses,
                            @NonNull ArrayMap<String,Integer> newSelectedIdsMap) {
        if (newBusinesses == null || newBusinesses.size() == 0) {
            Log.d(TAG, "updateItems: businessList is null or empty, so nothing to do.");
            return;
        }

        //update other member variables
        this.selectedIdsMap = newSelectedIdsMap;

        //add all new businesses to the end of the array
        Log.d(TAG, String.format("updateItems: Current size:%d, New size:%d", getCount(), newBusinesses.size()));
        addAll(newBusinesses);
        Log.d(TAG, "updateItems: After update size:" + getCount());
    }

    /**
     * Given an array of local result objects which can be thought of as pages, this method adds the businesses
     * from each page to the adapter's collection.
     * After all has been added, it notifies the listeners that the data has changed which causes the current
     * view to be refreshed.
     * @param newResults
     * @param newSelectedIdsMap
     */
    public void updateAllItems(@Nullable ArrayList<LocalResult> newResults,
                               @NonNull ArrayMap<String,Integer> newSelectedIdsMap) {
        if (newResults == null || newResults.size() == 0) {
            Log.d(TAG, "updateAllItems: New data is null or empty, so do nothing.");
            return;
        }

        //update other member variables
        this.selectedIdsMap = newSelectedIdsMap;

        Log.d(TAG, "updateAllItems: Current size:" + getCount());
        //clear existing businesses if any
        clear();

        //add all the businesses
        for (int i=0; i<newResults.size(); i++) {
            addAll(newResults.get(i).getLocalBusinesses());
        }

        Log.d(TAG, "updateAllItems: After update size:" + getCount());
    }

    /**
     * Handles touch events for integrating with Rebound API
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        toggledView = (CheckedTextView) v;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                toggledView.toggle(); //toggle the state

                //when pressed start solving the spring to to 1
                scaleSpring.setEndValue(1);
                break;

            case MotionEvent.ACTION_UP:
                //when released start solving the spring to 0
                scaleSpring.setEndValue(0);

                //log user selection from the list view
                LocalBusiness business = getItem((Integer)toggledView.getTag(R.id.position));
                AppEventsLogger logger = AppEventsLogger.newLogger(getContext());
                Bundle parameters = new Bundle();

                parameters.putString(EventConstants.EVENT_PARAM_SELECTION_VIEW, EventConstants.EVENT_PARAM_VIEW_LIST);
                parameters.putInt(EventConstants.EVENT_PARAM_SELECTION_DATA_SOURCE, business.getDataSource());
                switch (business.getDataSource()) {
                    case LocalConstants.FACEBOOK:
                        parameters.putString(EventConstants.EVENT_PARAM_SELECTION_PRICE, business.getPriceRangeString());
                        parameters.putInt(EventConstants.EVENT_PARAM_SELECTION_LIKES, business.getLikes());
                        parameters.putInt(EventConstants.EVENT_PARAM_SELECTION_CHECKINS, business.getCheckins());
                        break;
                    case LocalConstants.YELP:
                        parameters.putDouble(EventConstants.EVENT_PARAM_SELECTION_RATING, business.getRating());
                        parameters.putInt(EventConstants.EVENT_PARAM_SELECTION_REVIEWS, business.getReviewCount());
                        break;
                    case LocalConstants.GOOGLE:
                        break;
                }
                logger.logEvent(EventConstants.EVENT_NAME_ADDED_TO_SELECTION, parameters);

                //notify the list fragment, providing both business id and resulting toggle state
                listener.onItemToggle((String) toggledView.getTag(R.id.business_id),
                        (Integer) toggledView.getTag(R.id.position),
                        toggledView.isChecked());
                break;

            case MotionEvent.ACTION_CANCEL:
                toggledView.toggle(); //undo the earlier toggle state because it was a cancel

                //when released start solving the spring to 0
                scaleSpring.setEndValue(0);
                break;
        }
        return true;
    }

    /**
     * Given a view (i.e. a row in the listview), this method toggles the CheckedTextView state to
     * match the given toggleState. If it already matches, there is nothing to do.
     * @param view row in the listview
     * @param toggleState resulting toggle state (true means selected, false means not selected)
     */
    public void onSelectionChanged(View view, boolean toggleState) {
        Log.d(TAG, "onSelectionChanged: ToggleState:" + toggleState);

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (viewHolder == null) return;

        if (toggleState != viewHolder.itemToggle.isChecked()) {
            Log.d(TAG, "onSelectionChanged: ToggleState is different, so toggling now");
            viewHolder.itemToggle.toggle();
        }
        else {
            Log.d(TAG, "onSelectionChanged: ToggleState is the same, so nothing to do");
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
        final TextView ratingAndReviews;
        final TextView likes;
        final TextView checkins;
        final TextView priceRange;

        public final CheckedTextView itemToggle;
        final TextView distanceFromMidPoint;
        final TextView fairnessScore;

        ViewHolder(View view) {
            //first column
            image = (ImageView) view.findViewById(R.id.item_image);
            //second column
            name = (TextView) view.findViewById(R.id.item_name);
            address = (TextView) view.findViewById(R.id.item_address);
            categories = (TextView) view.findViewById(R.id.item_categories);
            ratingAndReviews = (TextView) view.findViewById(R.id.item_rating_and_reviews);
            likes = (TextView) view.findViewById(R.id.item_likes);
            checkins = (TextView) view.findViewById(R.id.item_checkins);
            priceRange = (TextView) view.findViewById(R.id.item_price_range);
            //third column
            itemToggle = (CheckedTextView) view.findViewById(R.id.item_toggle);
            distanceFromMidPoint = (TextView) view.findViewById(R.id.item_distance_from_midpoint);
            fairnessScore = (TextView) view.findViewById(R.id.item_fairness_score);
        }
    }

    /**
     * Handles spring callbacks
     */
    private class ToggleSpringListener extends SimpleSpringListener {
        /**
         * called whenever the spring leaves its resting state
         * @param spring
         */
        @Override
        public void onSpringActivate(Spring spring) {
        }

        /**
         * Called whenever the spring is updated
         * @param spring
         */
        @Override
        public void onSpringUpdate(Spring spring) {
            // On each update of the spring value, we adjust the scale of the image view to match the
            // springs new value. We use the SpringUtil linear interpolation function mapValueFromRangeToRange
            // to translate the spring's 0 to 1 scale to a 100% to 50% scale range and apply that to the View
            // with setScaleX/Y. Note that rendering is an implementation detail of the application and not
            // Rebound itself. If you need Gingerbread compatibility consider using NineOldAndroids to update
            // your view properties in a backwards compatible manner.
            if (toggledView == null) return;

            float mappedValue = (float) SpringUtil.mapValueFromRangeToRange(spring.getCurrentValue(), 0, 1, 1, 0.5);
            toggledView.setScaleX(mappedValue);
            toggledView.setScaleY(mappedValue);
        }

        /**
         * called whenever the spring notifies of displacement state changes
         * @param spring
         */
        @Override
        public void onSpringEndStateChange(Spring spring) {
        }

        /**
         * called whenever the spring achieves a resting state
         * @param spring
         */
        @Override
        public void onSpringAtRest(Spring spring) {
        }
    }
}
