package com.example.yeelin.projects.betweenus.fragment;

/**
 * OnSuggestionActionListener interface
 * To be implemented by activities or parent fragments interested in handling
 * suggestion clicks and toggles from list or map fragments.
 * Created by ninjakiki on 7/31/15.
 */
public interface OnSuggestionActionListener {
    /**
     * A suggestion was clicked in either the list or map fragment.
     * To be implemented by activities or parent fragments interested in handling
     * this event
     * @param id
     * @param name Name is passed so that the detail view is able to show the name while the
     *             rest of the business info is loaded from the network
     */
    public void onSuggestionClick(String id, String name);

    /**
     * A suggestion was toggled in either the list or map fragment.  Since it is a toggle
     * there is no need to pass the selection state; it's just the opposite of the current
     * state.
     * To be implemented by activities or parent fragments interested in handling
     * this event.
     * @param id
     */
    public void onSuggestionToggle(String id);
}
