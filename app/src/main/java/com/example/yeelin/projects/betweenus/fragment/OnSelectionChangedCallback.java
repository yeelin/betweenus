package com.example.yeelin.projects.betweenus.fragment;

/**
 * OnSelectionChangedCallback interface
 * To be implemented by list or map fragments interested in updating their view when
 * a selection changes.
 * Created by ninjakiki on 8/13/15.
 */
public interface OnSelectionChangedCallback {
    /**
     * The selections array map has changed.  Called by SuggestionsActivity.
     * To be implemented by fragments interested in updating their view.
     * @param id
     * @param toggleState resulting toggle state (true means selected, false means not selected)
     */
    public void onSelectionChanged(String id, boolean toggleState);
}

