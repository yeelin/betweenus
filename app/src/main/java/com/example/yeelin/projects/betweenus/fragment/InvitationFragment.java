package com.example.yeelin.projects.betweenus.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.data.generic.model.SimplifiedBusiness;
import com.example.yeelin.projects.betweenus.adapter.SimplifiedBusinessAdapter;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 7/24/15.
 */
public class InvitationFragment
        extends Fragment
        implements View.OnClickListener,
        TextView.OnEditorActionListener {
    //logcat
    private static final String TAG = InvitationFragment.class.getCanonicalName();

    //bundle args
    private static final String ARG_SELECTED_ITEMS = InvitationFragment.class.getSimpleName() + ".selectedItems";

    //member variables
    private ArrayList<SimplifiedBusiness> selectedItems;
    private InvitationFragmentListener listener;
    private boolean inviteByText = true;

    /**
     * Listener interface for fragments or activities interested in events from this fragment
     */
    public interface InvitationFragmentListener {
        void onInviteByTextMessage(String friendPhone, ArrayList<SimplifiedBusiness> selectedItems);
        void onInviteByEmail(String friendEmail, ArrayList<SimplifiedBusiness> selectedItems);
    }

    /**
     * Creates a new instance of this fragment
     * @param selectedItems
     * @return
     */
    public static InvitationFragment newInstance(ArrayList<SimplifiedBusiness> selectedItems) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_SELECTED_ITEMS, selectedItems);

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
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Object objectToCast = getParentFragment() != null ? getParentFragment() : context;
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
            selectedItems = args.getParcelableArrayList(ARG_SELECTED_ITEMS);
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

        //set up the listview
        Log.d(TAG, "onViewCreated: Adapter is null, so creating a new one");
        SimplifiedBusinessAdapter simplifiedBusinessAdapter = new SimplifiedBusinessAdapter(viewHolder.selectedItemsListView.getContext(), selectedItems);
        viewHolder.selectedItemsListView.setAdapter(simplifiedBusinessAdapter);

        //setup listener for contact field
        viewHolder.friendContact.setOnEditorActionListener(this);

        //set up listener for buttons
        viewHolder.inviteSendButton.setOnClickListener(this);
        viewHolder.inviteToggleButton.setOnClickListener(this);

        //do remaining setup based on current toggle state
        toggleViews();
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

            case R.id.invite_toggle_button:
                inviteByText = !inviteByText;
                toggleViews();
                break;

            default:
                break;
        }
    }

    /**
     * Updates the views based on whether we are inviting by text or email.
     */
    private void toggleViews() {
        final ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) return;

        if (inviteByText) {
            Log.d(TAG, "toggleViews: Invite by Text");

            //update toggle button to show alternative option
            viewHolder.inviteToggleButton.setText(R.string.invite_by_email);
            //update send button
            viewHolder.inviteSendButton.setImageResource(R.drawable.ic_action_chat);
            //update contact field
            viewHolder.friendContact.setHint(R.string.friend_phone);
            viewHolder.friendContact.setInputType(InputType.TYPE_CLASS_PHONE);
            viewHolder.friendContact.setText("");
        }
        else {
            Log.d(TAG, "toggleViews: Invite by Email");

            //update toggle button to show alternative option
            viewHolder.inviteToggleButton.setText(R.string.invite_by_text);
            //update send button
            viewHolder.inviteSendButton.setImageResource(R.drawable.ic_communication_email);
            //update contact field
            viewHolder.friendContact.setHint(R.string.friend_email);
            viewHolder.friendContact.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            viewHolder.friendContact.setText("");
        }
    }

    /**
     * Ask the listener to send the invite by the selected option
     */
    private void sendInvite() {
        ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) return;

        if (viewHolder.friendContact.getText().length() <= 0) {
            Log.d(TAG, "sendInvite: No contact provided, nothing to do");
            return;
        }

        if (inviteByText) {
            Log.d(TAG, "sendInvite: Text invite: " + selectedItems);
            listener.onInviteByTextMessage(viewHolder.friendContact.getText().toString(), selectedItems);
        }
        else {
            Log.d(TAG, "sendInvite: Email invite: " + selectedItems);
            listener.onInviteByEmail(viewHolder.friendContact.getText().toString(), selectedItems);
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
        final ListView selectedItemsListView;
        final EditText friendContact;
        final ImageButton inviteSendButton;
        final Button inviteToggleButton;

        ViewHolder(View view) {
            selectedItemsListView = (ListView) view.findViewById(R.id.selected_items_listView);
            selectedItemsListView.setEmptyView(view.findViewById(R.id.selected_items_empty));

            friendContact = (EditText) view.findViewById(R.id.friend_contact);
            inviteSendButton = (ImageButton) view.findViewById(R.id.invite_send_button);
            inviteToggleButton = (Button) view.findViewById(R.id.invite_toggle_button);
        }
    }
}
