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
import com.example.yeelin.projects.betweenus.model.YelpBusiness;
import com.example.yeelin.projects.betweenus.utils.ImageUtils;
import com.facebook.rebound.BaseSpringSystem;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.squareup.picasso.Target;

import java.util.List;

/**
 * Created by ninjakiki on 7/21/15.
 */
public class SuggestionsAdapter
        extends ArrayAdapter<YelpBusiness>
        implements View.OnClickListener,
        View.OnTouchListener {
    //logcat
    private static final String TAG = SuggestionsAdapter.class.getCanonicalName();
    //member variables
    private List<YelpBusiness> businessList;
    private ArrayMap<String,String> selectedIdsMap;
    private OnItemToggleListener listener;

    //for rebound
    private final BaseSpringSystem springSystem = SpringSystem.create();
    private final ToggleSpringListener buttonSpringListener = new ToggleSpringListener();
    private Spring scaleSpring;
    private CheckedTextView toggledView;

    /**
     * Interface for listening to toggling of the checked text view
     */
    public interface OnItemToggleListener {
        /**
         * Handle the toggling of an item in the listview
         * @param id business id of item toggled
         * @param toggleState resulting toggle state
         */
        public void onItemToggle(String id, boolean toggleState);
    }

    /**
     * Constructor
     * @param context
     * @param businessList
     */
    public SuggestionsAdapter(Context context, @Nullable List<YelpBusiness> businessList,
                              ArrayMap<String,String> selectedIdsMap, OnItemToggleListener listener) {

        super(context, 0, businessList);
        this.businessList = businessList;
        this.selectedIdsMap = selectedIdsMap;
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

        YelpBusiness business = getItem(position);
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        //set the views
        //image
        if (business.getImage_url() != null) {
            ImageUtils.loadImage(parent.getContext(), business.getImage_url(), viewHolder.image, R.drawable.ic_business_image_placeholder, R.drawable.ic_business_image_placeholder);
        }

        //name
        viewHolder.name.setText(business.getName());

        //short address
        if (business.getLocation() == null) {
            viewHolder.address.setVisibility(View.INVISIBLE);
        }
        else {
            final String shortAddress = business.getLocation().getShortDisplayAddress();
            if (shortAddress == null) {
                viewHolder.address.setVisibility(View.INVISIBLE);
            }
            else {
                viewHolder.address.setVisibility(View.VISIBLE);
                viewHolder.address.setText(shortAddress);
            }
        }

        //categories
        final String categories = business.getDisplayCategories();
        if (categories != null) {
            viewHolder.categories.setVisibility(View.VISIBLE);
            viewHolder.categories.setText(categories);
        }
        else {
            viewHolder.categories.setVisibility(View.INVISIBLE);
        }

        //ratings and reviews
        viewHolder.reviews.setText(getContext().getString(R.string.review_count, String.valueOf(business.getReview_count())));
        //note: picasso only keeps a weak ref to the target so it may be gc-ed
        //use setTag so that target will be alive as long as the view is alive
        if (business.getRating_img_url_large() != null) {
            final Target target = ImageUtils.newTarget(parent.getContext(), viewHolder.reviews);
            viewHolder.reviews.setTag(target);
            ImageUtils.loadImage(parent.getContext(), business.getRating_img_url_large(), target);
        }

        //set the checked state
        viewHolder.itemToggle.setChecked(selectedIdsMap.containsKey(business.getId()));
        viewHolder.itemToggle.setTag(R.id.business_id, business.getId());
        viewHolder.itemToggle.setTag(R.id.position, position);
        viewHolder.itemToggle.setOnClickListener(this);
        viewHolder.itemToggle.setOnTouchListener(this);

        return view;
    }

    /**
     * Updates the adapter with a new list of businessList
     * @param businessList
     */
    public void updateAllItems(@Nullable List<YelpBusiness> businessList, @NonNull ArrayMap<String,String> newSelectedIdsMap) {
        //if it's the same businessList, do nothing. Otherwise, you end up clearing out businessList
        if (this.businessList == businessList) {
            Log.d(TAG, "updateAllItems: this.businessList == businessList. Nothing to do");
            return;
        }

        //remove all from the current list
        clear();
        this.selectedIdsMap = newSelectedIdsMap;

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
        CheckedTextView itemToggle = (CheckedTextView) v;
        itemToggle.toggle();

        //notify the list fragment, providing both business id and resulting toggle state
        listener.onItemToggle((String)itemToggle.getTag(R.id.business_id), itemToggle.isChecked());
    }

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
                listener.onItemToggle((String) toggledView.getTag(R.id.business_id), toggledView.isChecked());
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
