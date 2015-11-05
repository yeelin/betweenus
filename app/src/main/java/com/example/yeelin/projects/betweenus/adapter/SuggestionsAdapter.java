package com.example.yeelin.projects.betweenus.adapter;

import android.content.Context;
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
import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.utils.FairnessScoringUtils;
import com.example.yeelin.projects.betweenus.utils.ImageUtils;
import com.facebook.rebound.BaseSpringSystem;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Target;

import java.util.List;

/**
 * Created by ninjakiki on 7/21/15.
 */
public class SuggestionsAdapter
        extends ArrayAdapter<LocalBusiness>
        implements View.OnClickListener,
        View.OnTouchListener {
    //logcat
    private static final String TAG = SuggestionsAdapter.class.getCanonicalName();
    //member variables
    private List<LocalBusiness> businessList;
    private ArrayMap<String,Integer> selectedIdsMap;
    private LatLng userLatLng;
    private LatLng friendLatLng;
    private LatLng midLatLng;

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
     */
    public SuggestionsAdapter(Context context, @Nullable List<LocalBusiness> businessList,
                              ArrayMap<String,Integer> selectedIdsMap,
                              LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng,
                              OnItemToggleListener listener) {
        super(context, 0, businessList);

        this.businessList = businessList;
        this.selectedIdsMap = selectedIdsMap;
        this.userLatLng = userLatLng;
        this.friendLatLng = friendLatLng;
        this.midLatLng = midLatLng;

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
        //checking business.getImageUrl() != null causes an issue here because if the view is recycled and the imageUrl
        //for the current business is null, then the imageView will not be cleared.
        ImageUtils.loadImage(parent.getContext(), business.getImageUrl(), viewHolder.image, R.drawable.ic_business_image_placeholder, R.drawable.ic_business_image_placeholder);

        //name
        viewHolder.name.setText(business.getName());

        //short address
        if (business.getLocalBusinessLocation() == null) {
            viewHolder.address.setVisibility(View.INVISIBLE);
        }
        else {
            final String shortAddress = business.getLocalBusinessLocation().getShortDisplayAddress();
            if (shortAddress == null) {
                viewHolder.address.setVisibility(View.INVISIBLE);
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

        //ratings and reviews OR likes and checkins
        if (business.getReviewCount() != -1) {
            //we have yelp data
            viewHolder.reviews.setText(getContext().getString(R.string.review_count, business.getReviewCount()));
            //note: picasso only keeps a weak ref to the target so it may be gc-ed
            //use setTag so that target will be alive as long as the view is alive
            if (business.getRatingImageUrl() != null) {
                final Target target = ImageUtils.newTarget(parent.getContext(), viewHolder.reviews);
                viewHolder.reviews.setTag(target);
                ImageUtils.loadImage(parent.getContext(), business.getRatingImageUrl(), target);
            }
            //no likes or checkins
            viewHolder.checkins.setVisibility(View.GONE);
        }
        else {
            //we most likely have fb data
            viewHolder.checkins.setVisibility(View.VISIBLE);
            viewHolder.reviews.setText(getContext().getResources().getQuantityString(
                    R.plurals.like_count,
                    business.getLikes(),
                    business.getLikes()));
            viewHolder.checkins.setText(getContext().getResources().getQuantityString(
                    R.plurals.checkin_count,
                    business.getCheckins(),
                    business.getCheckins()));
        }
        //price range
        final String priceRange = business.getPriceRange();
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
        viewHolder.itemToggle.setOnClickListener(this);
        viewHolder.itemToggle.setOnTouchListener(this);

        //compute fairness and distance from center
        final LatLng businessLatLng = business.getLocalBusinessLocation().getLatLng();
        final int fairness = FairnessScoringUtils.computeFairnessScore(userLatLng, friendLatLng, businessLatLng);
        final double distanceDelta = FairnessScoringUtils.computeDistanceDelta(businessLatLng, midLatLng, FairnessScoringUtils.IMPERIAL);

        //set distance from center
        viewHolder.distanceFromMidPoint.setText(FairnessScoringUtils.formatDistanceDelta(getContext(), distanceDelta, FairnessScoringUtils.IMPERIAL, false));

        //set fairness score
        viewHolder.fairnessScore.setText(FairnessScoringUtils.formatFairnessScore(getContext(), fairness));

        return view;
    }

    /**
     * Updates the adapter with a new list of businessList
     * @param businessList
     * @param newSelectedIdsMap
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng
     */
    public void updateAllItems(@Nullable List<LocalBusiness> businessList, @NonNull ArrayMap<String,Integer> newSelectedIdsMap,
                               LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng) {
        //if it's the same businessList, do nothing. Otherwise, you end up clearing out businessList
        if (this.businessList == businessList) {
            Log.d(TAG, "updateAllItems: this.businessList == businessList. Nothing to do");
            return;
        }

        //remove all from the current list
        clear();
        this.selectedIdsMap = newSelectedIdsMap;
        this.userLatLng = userLatLng;
        this.friendLatLng = friendLatLng;
        this.midLatLng = midLatLng;

        //add all new businessList to the end of the array
        if (businessList != null) {
            this.businessList = businessList;
            addAll(businessList);
        }
    }

    /**
     * Handles the click on the checkedTextView. All we do here is toggle the view
     * and then call the listener to let it know.
     * @param v CheckedTextView that was clicked
     */
    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: Toggle was clicked");

        //toggle the view
        CheckedTextView toggledView = (CheckedTextView) v;
        toggledView.toggle();

        //notify the list fragment, providing both business id and resulting toggle state
        listener.onItemToggle((String) toggledView.getTag(R.id.business_id),
                (Integer) toggledView.getTag(R.id.position),
                toggledView.isChecked());
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
                //when pressed start solving the spring to to 1
                toggledView.toggle(); //toggle the state
                scaleSpring.setEndValue(1);
                break;

            case MotionEvent.ACTION_UP:
                //notify the list fragment, providing both business id and resulting toggle state
                listener.onItemToggle((String) toggledView.getTag(R.id.business_id),
                        (Integer) toggledView.getTag(R.id.position),
                        toggledView.isChecked());
                scaleSpring.setEndValue(0);
                break;

            case MotionEvent.ACTION_CANCEL:
                //when released start solving the spring to 0
                toggledView.toggle(); //undo the earlier toggle state because it was a cancel
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
        final TextView reviews;
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
            reviews = (TextView) view.findViewById(R.id.item_reviews);
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
