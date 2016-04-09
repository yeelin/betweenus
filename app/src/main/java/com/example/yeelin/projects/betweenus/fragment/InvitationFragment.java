package com.example.yeelin.projects.betweenus.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
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

    //save instance state
    private static final String STATE_INVITE_TOGGLE = InvitationFragment.class.getSimpleName() + ".inviteToggle";
    private static final String STATE_INPUT_STRING = InvitationFragment.class.getSimpleName() + ".inputString";

    //member variables
    private ArrayList<SimplifiedBusiness> selectedItems;
    private InvitationFragmentListener listener;
    private boolean inviteByText = true;
    private String inputString = "";

    /**
     * Listener interface for fragments or activities interested in events from this fragment
     */
    public interface InvitationFragmentListener {
        void onInviteByTextMessage(@Nullable String friendName, @Nullable String friendPhone);
        void onInviteByEmail(@Nullable String friendName, @Nullable String friendEmail);
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

        if (savedInstanceState != null) {
            inviteByText = savedInstanceState.getBoolean(STATE_INVITE_TOGGLE, inviteByText);
            inputString = savedInstanceState.getString(STATE_INPUT_STRING, inputString);
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
        //inflate the fragment's view
        final View view = inflater.inflate(R.layout.fragment_invitation, container, false);

        //setup the listview header
        final ListView listView = (ListView) view.findViewById(R.id.selected_items_listView);
        final View header = inflater.inflate(R.layout.fragment_invitation_header, listView, false);
        listView.addHeaderView(header, null, false);

        //setup view holder
        ViewHolder viewHolder = new ViewHolder(view, header);
        view.setTag(viewHolder);

        return view;
    }

    /**
     * Configure the view
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //get the view holder
        final ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) return;

        //set up the listview
        SimplifiedBusinessAdapter simplifiedBusinessAdapter = new SimplifiedBusinessAdapter(viewHolder.selectedItemsListView.getContext(), selectedItems);
        viewHolder.selectedItemsListView.setAdapter(simplifiedBusinessAdapter);

        //setup listener for contact field
        viewHolder.friendContact.setOnEditorActionListener(this);

        //set up listener for buttons
        viewHolder.inviteSendButton.setOnClickListener(this);
        viewHolder.inviteToggleButton.setOnClickListener(this);

        //do remaining setup based on current toggle state
        toggleViews(false);
    }

    /**
     * Read the user's input string so that we can save it out later
     */
    @Override
    public void onPause() {
        final ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) return;

        if (viewHolder.friendContact.getText().length() > 0)
            inputString = viewHolder.friendContact.getText().toString();

        super.onPause();
    }

    /**
     * Save out the toggle state and user's input string in case of configuration change
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_INVITE_TOGGLE, inviteByText);
        outState.putString(STATE_INPUT_STRING, inputString);
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
        return false;
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
                toggleViews(true);
                break;

            default:
                break;
        }
    }

    /**
     * Updates the views based on whether we are inviting by text or email.
     * @param resetInput
     */
    private void toggleViews(boolean resetInput) {
        final ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) return;

        if (inviteByText) {
            //update toggle button to show alternative option
            viewHolder.inviteToggleButton.setText(R.string.invite_by_email);
            //update send button
            viewHolder.inviteSendButton.setImageResource(R.drawable.ic_action_chat);
            //update contact field
            //viewHolder.friendContact.setHint(R.string.friend_phone);
            viewHolder.contactTextInputLayout.setHint(getString(R.string.friend_phone));
            viewHolder.friendContact.setInputType(InputType.TYPE_CLASS_PHONE);
        }
        else {
            //update toggle button to show alternative option
            viewHolder.inviteToggleButton.setText(R.string.invite_by_text);
            //update send button
            viewHolder.inviteSendButton.setImageResource(R.drawable.ic_communication_email);
            //update contact field
            //viewHolder.friendContact.setHint(R.string.friend_email);
            viewHolder.contactTextInputLayout.setHint(getString(R.string.friend_email));
            viewHolder.friendContact.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }

        if (resetInput)
            inputString = "";
        viewHolder.friendContact.setText(inputString);
    }

    /**
     * Ask the listener to send the invite by the selected option
     */
    private void sendInvite() {
        ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) return;

        String friendName = viewHolder.friendName.getText().length() <= 0 ? null : viewHolder.friendName.getText().toString();
        String friendContact = viewHolder.friendContact.getText().length() <= 0 ? null : viewHolder.friendContact.getText().toString();
        
        if (inviteByText) {
            Log.d(TAG, "sendInvite: Text invite: " + selectedItems);
            listener.onInviteByTextMessage(friendName, friendContact);
        }
        else {
            Log.d(TAG, "sendInvite: Email invite: " + selectedItems);
            listener.onInviteByEmail(friendName, friendContact);
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
        final TextInputLayout nameTextInputLayout;
        final EditText friendName;
        final TextInputLayout contactTextInputLayout;
        final EditText friendContact;
        final ImageButton inviteSendButton;
        final Button inviteToggleButton;

        /**
         * Constructor
         * @param fragmentView fragment view
         * @param listViewHeader header of the listview
         */
        ViewHolder(View fragmentView, View listViewHeader) {
            //set up references to the listview and empty view
            selectedItemsListView = (ListView) fragmentView.findViewById(R.id.selected_items_listView);
            //empty view not needed as there are checks in place to get user to select first before inviting friend
            //selectedItemsListView.setEmptyView(fragmentView.findViewById(R.id.selected_items_empty));

            //set up references to components in the listview header
            nameTextInputLayout = (TextInputLayout) listViewHeader.findViewById(R.id.name_textInput_layout);
            friendName = (EditText) listViewHeader.findViewById(R.id.friend_name);
            contactTextInputLayout = (TextInputLayout) listViewHeader.findViewById(R.id.contact_textInput_layout);
            friendContact = (EditText) listViewHeader.findViewById(R.id.friend_contact);

            inviteSendButton = (ImageButton) listViewHeader.findViewById(R.id.invite_send_button);
            inviteToggleButton = (Button) listViewHeader.findViewById(R.id.invite_toggle_button);
        }
    }
}
