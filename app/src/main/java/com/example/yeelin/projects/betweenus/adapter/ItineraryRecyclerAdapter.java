package com.example.yeelin.projects.betweenus.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.provider.ItineraryContract;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by ninjakiki on 3/31/16.
 */
public class ItineraryRecyclerAdapter
        extends RecyclerView.Adapter<ItineraryRecyclerAdapter.ItineraryViewHolder> {
    //logcat
    private static final String TAG = ItineraryRecyclerAdapter.class.getCanonicalName();

    //view types
    private static final int VIEW_TYPE_ITINERARY = 0;

    //column index constants
//    @IntDef({ID, ITINERARY_ID, CLOSEST_CITY, CLOSEST_CITY_LATITUDE, CLOSEST_CITY_LONGITUDE, NAME, EMAIL, PHONE, DATA_SOURCE, CREATED_DATETIME})
//    @Retention(RetentionPolicy.SOURCE)
//    public @interface ItineraryCursorColumnIndex {}
//    public static final int ID = 0;
//    public static final int ITINERARY_ID = 1;
//    public static final int CLOSEST_CITY = 2;
//    public static final int CLOSEST_CITY_LATITUDE = 3;
//    public static final int CLOSEST_CITY_LONGITUDE = 4;
//    public static final int NAME = 5;
//    public static final int EMAIL = 6;
//    public static final int PHONE = 7;
//    public static final int DATA_SOURCE = 8;
//    public static final int CREATED_DATETIME = 9;

    //member variables
    private Cursor cursor;
    private final Context context;
    private final ItineraryRecyclerAdapterOnClickHandler onClickHander;
    private final View emptyView;

    /**
     * Interface to listen for clicks from adapter
     */
    public interface ItineraryRecyclerAdapterOnClickHandler {
        void onItemClick(int itineraryId, ItineraryViewHolder itineraryViewHolder);
        void onViewDetails(int itineraryId, ItineraryViewHolder itineraryViewHolder);
        void onReuseClick(int itineraryId, ItineraryViewHolder itineraryViewHolder);
    }

    /**
     * Constructor
     * @param context
     * @param onClickHander
     * @param emptyView
     */
    public ItineraryRecyclerAdapter(Context context, ItineraryRecyclerAdapterOnClickHandler onClickHander, View emptyView) {
        this.context = context;
        this.onClickHander = onClickHander;
        this.emptyView = emptyView;
    }

    /**
     * Inflate a view for an adapter item and then have the view holder track the view.
     * @param parent
     * @param viewType only 1 viewtype so far, otherwise, we'll have to switch on this
     * @return
     */
    @Override
    public ItineraryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_itinerary, parent, false);
        view.setFocusable(true);
        return new ItineraryViewHolder(view);
    }

    /**
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ItineraryViewHolder holder, int position) {
        cursor.moveToPosition(position);

        int itineraryColumnIndex = cursor.getColumnIndex(ItineraryContract.Columns.ITINERARY_ID);
        int itineraryId = cursor.getInt(itineraryColumnIndex);
        //TODO: do something with itinerary id

        int nameColumnIndex = cursor.getColumnIndex(ItineraryContract.Columns.NAME);
        int closestCityColumnIndex = cursor.getColumnIndex(ItineraryContract.Columns.CLOSEST_CITY);
        holder.title.setText(cursor.getString(nameColumnIndex));
        holder.title.setText(cursor.getString(closestCityColumnIndex));
    }

//    @Override
//    public int getItemViewType(int position) {
//        return super.getItemViewType(position);
//    }


    /**
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void swapCursor(Cursor newCursor) {
        cursor = newCursor;
        notifyDataSetChanged();
        emptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    /**
     * ItineraryViewHolder class i.e. cache of the children views for an itinerary list item.
     * Logically this is very much like the viewholder in list views. Key difference
     * here is the holder tracks the view instead of the other way around.
     */
    public class ItineraryViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        final ImageView profilePhoto;
        final TextView title;
        final TextView subtitle;
        final Button viewDetails;
        final Button reuse;

        public ItineraryViewHolder(View itemView) {
            super(itemView);

            profilePhoto = (ImageView) itemView.findViewById(R.id.itinerary_profile_photo);
            title = (TextView) itemView.findViewById(R.id.itinerary_title);
            subtitle = (TextView) itemView.findViewById(R.id.itinerary_subtitle);
            viewDetails = (Button) itemView.findViewById(R.id.itinerary_action_view);
            reuse = (Button) itemView.findViewById(R.id.itinerary_action_reuse);

            //set click listeners for buttons
            viewDetails.setOnClickListener(this);
            reuse.setOnClickListener(this);

            //set click listener for this item
            itemView.setOnClickListener(this);
        }

        @Override
        public String toString() {
            return String.format("Title:%s, Subtitle:%s", title.getText(), subtitle.getText());
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick:");
            int adapterPosition = getAdapterPosition();
            cursor.moveToPosition(adapterPosition);

            int itineraryColumnIndex = cursor.getColumnIndex(ItineraryContract.Columns.ITINERARY_ID);
            int itineraryId = cursor.getInt(itineraryColumnIndex);

            switch (v.getId()) {
                case R.id.itinerary_action_view:
                    onClickHander.onViewDetails(itineraryId, this);
                    break;

                case R.id.itinerary_action_reuse:
                    onClickHander.onReuseClick(itineraryId, this);
                    break;

                case R.id.itinerary_cardview:
                    onClickHander.onItemClick(itineraryId, this);
                    break;
            }
        }
    }
}
