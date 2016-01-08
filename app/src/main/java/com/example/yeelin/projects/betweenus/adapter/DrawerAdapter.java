package com.example.yeelin.projects.betweenus.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;

/**
 * Created by ninjakiki on 1/7/16.
 */
public class DrawerAdapter extends BaseAdapter {
    private static final String TAG = DrawerAdapter.class.getCanonicalName();

    private final String[] listItems;
    private final int[] drawableItems;

    public DrawerAdapter(Context context) {
        super();

        //read the list of strings for the drawer list
        listItems = context.getResources().getStringArray(R.array.drawer_items);

        //read the list of drawable resources for the drawer list
        TypedArray imageRes = context.getResources().obtainTypedArray(R.array.drawer_images);
        drawableItems = new int[imageRes.length()];
        for (int i=0; i<imageRes.length(); i++) {
            drawableItems[i] = imageRes.getResourceId(i, 0);
        }
        imageRes.recycle();

        //log warning if there's a mismatch is the number of strings vs drawable resources
        if (listItems.length != drawableItems.length) {
            Log.w(TAG, "Number of text strings and images for navigation drawer do not match");
        }
    }

    @Override
    public int getCount() {
        return listItems.length;
    }

    @Override
    public Object getItem(int position) {
        return listItems[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = (TextView) convertView;
        if (convertView == null) {
            textView = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_list_item, parent, false);
        }

        textView.setText(listItems[position]);
        textView.setCompoundDrawablesWithIntrinsicBounds(drawableItems[position], 0, 0, 0);
        return textView;
    }
}
