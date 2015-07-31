package com.example.yeelin.projects.betweenus.fragment;

/**
 * Created by ninjakiki on 7/31/15.
 */
public interface OnSuggestionItemClickListener {
    /**
     * A suggestion was clicked in either the list or map fragment.
     * To be implemented by activities or parent fragments interested in handling
     * this event
     * @param id
     */
    public void onSuggestionClick(String id);
}
