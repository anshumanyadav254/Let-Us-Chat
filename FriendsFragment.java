package com.luciferhacker.letuschat;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment implements MyStringsConstant{

    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private View mMainView;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsList = (RecyclerView) mMainView.findViewById(R.id.fragmentFriends_friendsList_recyclerView);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child(strFRIENDS_DATABASE).child(mCurrentUserId);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(strUSERS_DATABASE);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart(){
        super.onStart();
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.user_single_layout,
                FriendsViewHolder.class,
                mFriendsDatabase
        )   {
            @Override
            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, final Friends friends, int i) {

                friendsViewHolder.setDate(friends.getDate());

                final String listUserId = getRef(i).getKey();
                mUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child(strNAME).getValue(String.class);
                        String userThumb = dataSnapshot.child(strTHUMB_IMAGE).getValue(String.class);

                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setUserImage(userThumb,getContext());

                        if(dataSnapshot.hasChild(strONLINE)){
                            String userOnline = dataSnapshot.child(strONLINE).getValue().toString();
                            friendsViewHolder.setUserOnlineStatus(userOnline);
                        }

                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[] {"Open Profile", "Send Massege"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Option");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(which == 0){
                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra(strUSER_ID, listUserId);
                                            startActivity(profileIntent);
                                        }

                                        else if (which == 1){

                                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                chatIntent.putExtra(strUSER_ID, listUserId);
                                                startActivity(chatIntent);

                                        }
                                    }
                                });

                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };

        mFriendsList.setAdapter(friendsRecyclerViewAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder (View itemView){
            super(itemView);
            mView = itemView;
        }

        public void setDate(String date){
            TextView userDateView = (TextView) mView.findViewById(R.id.userSingleLayout_status_textView);
            userDateView.setText(date);
        }

        public void setName(String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.userSingleLayout_profileName_textView);
            userNameView.setText(name);
        }

        public void setUserImage(String thumb, Context ctx){
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.userSingleLayout_profileImage_circleImageView);
            Picasso.get().load(thumb).placeholder(R.drawable.default_avatar).into(userImageView);
        }

        public void setUserOnlineStatus (String onlineStatus){
            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.userSingleLayout_onlineIcon_imageView);
            if(onlineStatus.equals(strTRUE)){
                userOnlineView.setVisibility(View.VISIBLE);
            } else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
