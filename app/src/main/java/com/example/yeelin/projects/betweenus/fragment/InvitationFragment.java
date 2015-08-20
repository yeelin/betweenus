package com.example.yeelin.projects.betweenus.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.adapter.SimplifiedBusiness;
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

    /**
     * Listener interface for fragments or activities interested in events from this fragment
     */
    public interface InvitationFragmentListener {
        public void onInviteByTextMessage(String friendPhone, ArrayList<SimplifiedBusiness> selectedItems);
        public void onInviteByEmail(String friendEmail, ArrayList<SimplifiedBusiness> selectedItems);
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
            listener.onInviteByTextMessage(viewHolder.friendPhone.getText().toString(), selectedItems);
        }
        else if (viewHolder.friendEmail.getText().length() > 0) {
            //email friend
            listener.onInviteByEmail(viewHolder.friendEmail.getText().toString(), selectedItems);
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

        final EditText friendPhone;
        final EditText friendEmail;
        final Button inviteButton;

        ViewHolder(View view) {
            selectedItemsListView = (ListView) view.findViewById(R.id.selected_items_listView);
            selectedItemsListView.setEmptyView(view.findViewById(R.id.selected_items_empty));

            friendPhone = (EditText) view.findViewById(R.id.friend_phone);
            friendEmail = (EditText) view.findViewById(R.id.friend_email);
            inviteButton = (Button) view.findViewById(R.id.invite_send_button);
        }
    }
}
