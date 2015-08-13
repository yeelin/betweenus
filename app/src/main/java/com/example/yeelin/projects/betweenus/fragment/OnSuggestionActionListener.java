package com.example.yeelin.projects.betweenus.fragment;

/**
 * Created by ninjakiki on 7/31/15.
 */
public interface OnSuggestionActionListener {
    /**
     * A suggestion was clicked in either the list or map fragment.
     * To be implemented by activities or parent fragments interested in handling
     * this event
     * @param id
     */
    public void onSuggestionClick(String id, String name);

    /**
     * A suggestion was toggled in either the list or map fragment.
     * To be implemented by activities or parent fragments interested in handling
     * this event
     * @param id
     * @param isSelected
     */
    public void onSuggestionToggle(String id, boolean isSelected);
}
