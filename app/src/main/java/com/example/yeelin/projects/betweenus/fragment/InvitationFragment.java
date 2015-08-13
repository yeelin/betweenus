package com.example.yeelin.projects.betweenus.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 7/24/15.
 */
public class InvitationFragment
        extends Fragment
        implements View.OnClickListener, TextView.OnEditorActionListener {
    //logcat
    public static final String TAG = InvitationFragment.class.getCanonicalName();

    //bundle args
    public static final String ARG_SELECTED_IDS = InvitationFragment.class.getSimpleName() + ".selectedIds";

    //member variables
    private ArrayList<String> selectedItemIds;
    private InvitationFragmentListener listener;

    /**
     * Listener interface for fragments or activities interested in events from this fragment
     */
    public interface InvitationFragmentListener {
        public void onInviteByTextMessage(String friendPhone, ArrayList<String> selectedItemIds);
        public void onInviteByEmail(String friendEmail, ArrayList<String> selectedItemIds);
    }

    /**
     * Creates a new instance of this fragment
     * @param selectedItemIds
     * @return
     */
    public static InvitationFragment newInstance(ArrayList<String> selectedItemIds) {
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_SELECTED_IDS, selectedItemIds);

        InvitationFragment fragment = new InvitationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Required public empty constructor
     */
    public InvitationFragment() {}

    /**
     * Make sure either the activity or the parent fragment implements the listener interface
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Object objectToCast = getParentFragment() != null ? getParentFragment() : activity;
        try {
            listener = (InvitationFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName() + " must implement InvitationFragmentListener");
        }
    }

    /**
     * Configure the fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //read bungle args
        Bundle args = getArguments();
        if (args != null) {
            selectedItemIds = args.getStringArrayList(ARG_SELECTED_IDS);
        }
    }

    /**
     * Inflate the fragment's view
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invitation, container, false);
    }

    /**
     * Configure the view
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //set up the view holder
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        //set up the field to show selected items
        viewHolder.selectedItems.setText(selectedItemIds.toString());

        //setup listener for phone and email fields
        viewHolder.friendPhone.setOnEditorActionListener(this);
        viewHolder.friendEmail.setOnEditorActionListener(this);

        //set up listener for buttons
        viewHolder.inviteButton.setOnClickListener(this);
    }

    /**
     * Nullify the listener
     */
    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    /**
     * Handle keyboard actions
     * @param v
     * @param actionId
     * @param event
     * @return
     */
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            sendInvite();
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Handle the Send and Cancel button clicks
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.invite_send_button:
                sendInvite();
                break;

            default:
                break;
        }
    }

    /**
     * Ask the listener to send the invite either by text or email depending on which field was filled out
     */
    private void sendInvite() {
        ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) return;

        if (viewHolder.friendPhone.getText().length() > 0) {
            //text friend
            listener.onInviteByTextMessage(viewHolder.friendPhone.getText().toString(), selectedItemIds);
        }
        else if (viewHolder.friendEmail.getText().length() > 0) {
            //email friend
            listener.onInviteByEmail(viewHolder.friendEmail.getText().toString(), selectedItemIds);
        }
    }

    /**
     * Returns the fragment view's view holder if it exists, or null.
     * @return
     */
    @Nullable
    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /**
     * ViewHolder class
     */
    private class ViewHolder {
        final EditText friendPhone;
        final EditText friendEmail;
        final TextView selectedItems;
        final Button inviteButton;

        ViewHolder(View view) {
            friendPhone = (EditText) view.findViewById(R.id.friend_phone);
            friendEmail = (EditText) view.findViewById(R.id.friend_email);
            selectedItems = (TextView) view.findViewById(R.id.selected_items);
            inviteButton = (Button) view.findViewById(R.id.invite_send_button);
        }
    }
}
