package com.luciferhacker.letuschat;

import android.app.ProgressDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements MyStringsConstant {

    private ImageView mProfileImage;
    private TextView mProfileName, mStatus, mTotalFriends;
    private Button mSendFriendsRequestButton, mDeclineFriendsRequestButton;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendsRequestDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mNotificationsDatabase;
    private DatabaseReference mRootReferenceDatabase;
    private FirebaseUser mCurrentUser;
    private String mCurrentState;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mProfileImage = (ImageView) findViewById(R.id.profile_profileImage_circleImageView);
        mProfileName = (TextView) findViewById(R.id.profile_profileName_textView);
        mStatus = (TextView) findViewById(R.id.profile_status_textView);
        mTotalFriends = (TextView) findViewById(R.id.profile_totalFriends_textView);
        mSendFriendsRequestButton = (Button) findViewById(R.id.profile_sendFriendRequest_button);
        mDeclineFriendsRequestButton = (Button) findViewById(R.id.profile_declineFriendRequest_button);

        mDeclineFriendsRequestButton.setVisibility(View.INVISIBLE);
        mDeclineFriendsRequestButton.setEnabled(false);

        mCurrentState = strNOT_FRIENDS;

        final String userId = getIntent().getStringExtra(strUSER_ID);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(strUSERS_DATABASE).child(userId);
        mFriendsRequestDatabase = FirebaseDatabase.getInstance().getReference().child(strFRIENDS_REQUEST_DATABASE);
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child(strFRIENDS_DATABASE);
        mNotificationsDatabase = FirebaseDatabase.getInstance().getReference().child(strNOTIFICATIONS_DATABASE);
        mRootReferenceDatabase = FirebaseDatabase.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProgressDialog = new ProgressDialog(ProfileActivity.this);
        mProgressDialog.setTitle("Loading User Profile");
        mProgressDialog.setMessage("please wait !");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String displayName = strDEFAULT;
                String status = strDEFAULT;
                String image = strDEFAULT;

                if (dataSnapshot.exists()) {
                    displayName = dataSnapshot.child(strNAME).getValue(String.class);
                    status = dataSnapshot.child(strSTATUS).getValue(String.class);
                    image = dataSnapshot.child(strIMAGE).getValue(String.class);
                }

                mProfileName.setText(displayName);
                mStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);


                /* ============================== ON DATA CHANGE IN DATABASE ==========================*/

                mFriendsRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(userId)) {
                            String requestType = dataSnapshot.child(userId).child(strREQUEST_TYPE).getValue().toString();

                            if (requestType.equals(strRECEIVED)) {
                                mCurrentState = strREQUEST_RECEIVED;
                                mSendFriendsRequestButton.setText(strAccept_Friend_Request);

                                mDeclineFriendsRequestButton.setVisibility(View.VISIBLE);
                                mDeclineFriendsRequestButton.setEnabled(true);

                            } else if (requestType.equals(strSENT)) {
                                mCurrentState = strREQUEST_SENT;
                                mSendFriendsRequestButton.setText(strCancel_Friend_Request);

                                mDeclineFriendsRequestButton.setVisibility(View.INVISIBLE);
                                mDeclineFriendsRequestButton.setEnabled(false);
                            }

                            mProgressDialog.dismiss();
                        } else {

                            mFriendsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(userId)) {

                                        mCurrentState = strFRIENDS;
                                        mSendFriendsRequestButton.setText(strUnfriend);

                                        mDeclineFriendsRequestButton.setVisibility(View.INVISIBLE);
                                        mDeclineFriendsRequestButton.setEnabled(false);

                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                    mProgressDialog.dismiss();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        mProgressDialog.dismiss();
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mProgressDialog.dismiss();
            }
        });


        /*==================== FUNCTIONS OF SEND FRIENDS REQUEST BUTTON ======================*/
        mSendFriendsRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSendFriendsRequestButton.setEnabled(false);


                /* ---------------------- [ 1. NOT FRIENDS STATE ] ------------------ */
                /* ---------------------- To SENT A FRIENDS REQUEST ------------------ */
                if (mCurrentState.equals(strNOT_FRIENDS)) {

                    DatabaseReference newNotificationsReference = mRootReferenceDatabase.child(strNOTIFICATIONS_DATABASE).child(userId).push();
                    String notificationsId = newNotificationsReference.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put(strFROM, mCurrentUser.getEmail());
                    notificationData.put(strTYPE, strREQUEST_RECEIVED);

                    Map sentRequestMap = new HashMap();
                    sentRequestMap.put(strFRIENDS_REQUEST_DATABASE + "/" + mCurrentUser.getUid() + "/" + userId + "/" + strREQUEST_TYPE, strSENT);
                    sentRequestMap.put(strFRIENDS_REQUEST_DATABASE + "/" + userId + "/" + mCurrentUser.getUid() + "/" + strREQUEST_TYPE, strRECEIVED);
                    // sentRequestMap.put(strNOTIFICATIONS_DATABASE + "/" + userId + "/" + notificationsId, notificationData);

                    mRootReferenceDatabase.updateChildren(sentRequestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError == null){

                                mCurrentState = strREQUEST_SENT;
                                mSendFriendsRequestButton.setText(strCancel_Friend_Request);

                                mDeclineFriendsRequestButton.setVisibility(View.INVISIBLE);
                                mDeclineFriendsRequestButton.setEnabled(false);

                                Toast.makeText(ProfileActivity.this, "Request Send", Toast.LENGTH_LONG).show();

                            } else {

                                Toast.makeText(ProfileActivity.this, "Failed Sending Request", Toast.LENGTH_LONG).show();
                            }
                            mSendFriendsRequestButton.setEnabled(true);
                        }
                    });
                }

                /* ---------------------- [ 2. REQUEST SENT STATE ] ------------------*/
                /* ---------------------- TO CANCEL SENT FRIENDS REQUEST ------------------ */
                if (mCurrentState.equals(strREQUEST_SENT)) {

                    Map cancelSentRequestMap = new HashMap();
                    cancelSentRequestMap.put(strFRIENDS_REQUEST_DATABASE + "/" + mCurrentUser.getUid() + "/" + userId, null);
                    cancelSentRequestMap.put(strFRIENDS_REQUEST_DATABASE + "/" + userId + "/" + mCurrentUser.getUid(), null);

                    mRootReferenceDatabase.updateChildren(cancelSentRequestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError == null){

                                mCurrentState = strNOT_FRIENDS;
                                mSendFriendsRequestButton.setText(strSend_Friend_Request);

                                mDeclineFriendsRequestButton.setVisibility(View.INVISIBLE);
                                mDeclineFriendsRequestButton.setEnabled(false);

                                Toast.makeText(ProfileActivity.this, "Cancelled Sent Friends Request", Toast.LENGTH_LONG).show();

                            } else {

                                Toast.makeText(ProfileActivity.this, "Error Cancelling Sent Request Failed", Toast.LENGTH_LONG).show();
                            }
                            mSendFriendsRequestButton.setEnabled(true);
                        }
                    });

                }

                /* --------------------- [ 3. FRIENDS REQUEST RECEIVED STATE ] -------------- */
                /* ---------------------- TO ACCEPT FRIENDS REQUEST ------------------ */
                if (mCurrentState.equals(strREQUEST_RECEIVED)) {

                    final String currentDateAndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                    Map acceptRequestMap = new HashMap();
                    acceptRequestMap.put(strFRIENDS_DATABASE + "/" + mCurrentUser.getUid() + "/" + userId + "/" + strDATE, currentDateAndTime);
                    acceptRequestMap.put(strFRIENDS_DATABASE + "/" + userId + "/" + mCurrentUser.getUid() + "/" + strDATE, currentDateAndTime);

                    acceptRequestMap.put(strFRIENDS_REQUEST_DATABASE + "/" + mCurrentUser.getUid() + "/" + userId, null);
                    acceptRequestMap.put(strFRIENDS_REQUEST_DATABASE + "/" + userId + "/" + mCurrentUser.getUid(), null);

                    mRootReferenceDatabase.updateChildren(acceptRequestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError == null){

                                mCurrentState = strFRIENDS;
                                mSendFriendsRequestButton.setText(strUnfriend);

                                mDeclineFriendsRequestButton.setVisibility(View.INVISIBLE);
                                mDeclineFriendsRequestButton.setEnabled(false);

                                Toast.makeText(ProfileActivity.this, "Request Accepted", Toast.LENGTH_LONG).show();

                            } else {

                                Toast.makeText(ProfileActivity.this, "Error Request Accepting Failed", Toast.LENGTH_LONG).show();
                            }
                            mSendFriendsRequestButton.setEnabled(true);
                        }
                    });
                }

                /* -------------------------- [ 4. FRIENDS ] ----------------------- */
                /* ---------------------- TO UN-FRIENDS A ALREADY FRIENDS LIST ------------------ */
                if (mCurrentState.equals(strFRIENDS)) {

                    Map unFriendsMap = new HashMap();
                    unFriendsMap.put(strFRIENDS_DATABASE + "/" + mCurrentUser.getUid() + "/" + userId, null);
                    unFriendsMap.put(strFRIENDS_DATABASE + "/" + userId + "/" + mCurrentUser.getUid(), null);

                    mRootReferenceDatabase.updateChildren(unFriendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError == null){

                                mCurrentState = strNOT_FRIENDS;
                                mSendFriendsRequestButton.setText(strSend_Friend_Request);

                                mDeclineFriendsRequestButton.setVisibility(View.INVISIBLE);
                                mDeclineFriendsRequestButton.setEnabled(false);

                                Toast.makeText(ProfileActivity.this, "Now UnFriends", Toast.LENGTH_LONG).show();

                            } else {

                                Toast.makeText(ProfileActivity.this, "Error UnFriends Failed", Toast.LENGTH_LONG).show();
                            }
                            mSendFriendsRequestButton.setEnabled(true);
                        }
                    });

                }


                /* ======================================  1. Not Friend State
                if (mCurrentState.equals(strNOT_FRIENDS)) {

                    mFriendsRequestDatabase.child(mCurrentUser.getUid()).child(userId).child(strREQUEST_TYPE)
                            .setValue(strSENT).addOnCompleteListener(new OnCompleteListener<Void>() {

                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                mFriendsRequestDatabase.child(userId).child(mCurrentUser.getUid()).child(strREQUEST_TYPE)
                                        .setValue(strRECEIVED).addOnSuccessListener(new OnSuccessListener<Void>() {

                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String, String> notificationData = new HashMap<>();
                                        notificationData.put(strFROM, mCurrentUser.getEmail());
                                        notificationData.put(strTYPE, strREQUEST_RECEIVED);

                                        mNotificationsDatabase.child(userId).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mCurrentState = strREQUEST_SENT;
                                                mSendFriendsRequestButton.setText(strCancel_Friend_Request);

                                                mDeclineFriendsRequestButton.setVisibility(View.INVISIBLE);
                                                mDeclineFriendsRequestButton.setEnabled(false);

                                                Toast.makeText(ProfileActivity.this, "Request Send", Toast.LENGTH_LONG).show();

                                            }
                                        });
                                    }
                                });

                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed Sending Request", Toast.LENGTH_LONG).show();
                            }
                            mSendFriendsRequestButton.setEnabled(true);

                        }
                    });
                }
                ======================================== */

                /* =================================== 2. Cancel Request State

                if (mCurrentState.equals(strREQUEST_SENT)) {
                    mFriendsRequestDatabase.child(mCurrentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendsRequestDatabase.child(userId).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mSendFriendsRequestButton.setEnabled(true);
                                    mCurrentState = strNOT_FRIENDS;
                                    mSendFriendsRequestButton.setText(strSend_Friend_Request);

                                    mDeclineFriendsRequestButton.setVisibility(View.INVISIBLE);
                                    mDeclineFriendsRequestButton.setEnabled(false);

                                }
                            });
                        }
                    });
                }
                =======================================*/

                /* ========================================== 3. Request Receive State

                if (mCurrentState.equals(strREQUEST_RECEIVED)) {
                    final String currentDateAndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                    mFriendsDatabase.child(mCurrentUser.getUid()).child(userId).child(strDATE).setValue(currentDateAndTime).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendsDatabase.child(userId).child(mCurrentUser.getUid()).child(strDATE).setValue(currentDateAndTime).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendsRequestDatabase.child(mCurrentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mFriendsRequestDatabase.child(userId).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mSendFriendsRequestButton.setEnabled(true);
                                                    mCurrentState = strFRIENDS;
                                                    mSendFriendsRequestButton.setText(strUnfriend);

                                                    mDeclineFriendsRequestButton.setVisibility(View.INVISIBLE);
                                                    mDeclineFriendsRequestButton.setEnabled(false);

                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });

                    Toast.makeText(getApplicationContext(), currentDateAndTime, Toast.LENGTH_SHORT).show();

                }
                =====================================*/

                /* =====================================  4. Friends State (Unfriend a already friends)

                if (mCurrentState.equals(strFRIENDS)) {

                    mFriendsDatabase.child(mCurrentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendsDatabase.child(userId).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mSendFriendsRequestButton.setEnabled(true);
                                    mCurrentState = strNOT_FRIENDS;
                                    mSendFriendsRequestButton.setText(strSend_Friend_Request);

                                    mDeclineFriendsRequestButton.setVisibility(View.INVISIBLE);
                                    mDeclineFriendsRequestButton.setEnabled(false);
                                }
                            });
                        }
                    });

                    Toast.makeText(getApplicationContext(), "Unfriends Successfully", Toast.LENGTH_SHORT).show();

                }
                ====================================*/

            }
        });


        /*===================== FUNCTION OF DECLINE FRIENDS REQUEST BUTTON ====================*/
        mDeclineFriendsRequestButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                /*------------------------- [ 5. REQUEST RECEIVED STATE ] --------------------*/
                /*------------------------- TO DECLINE FRIENDS REQUEST --------------------*/
                if (mCurrentState.equals(strREQUEST_RECEIVED)) {

                    Map declineRequestMap = new HashMap();
                    declineRequestMap.put(strFRIENDS_REQUEST_DATABASE + "/" + mCurrentUser.getUid() + "/" + userId, null);
                    declineRequestMap.put(strFRIENDS_REQUEST_DATABASE + "/" + userId + "/" + mCurrentUser.getUid(), null);

                    mRootReferenceDatabase.updateChildren(declineRequestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError == null){

                                mCurrentState = strNOT_FRIENDS;
                                mSendFriendsRequestButton.setText(strSend_Friend_Request);
                                mSendFriendsRequestButton.setEnabled(true);

                                mDeclineFriendsRequestButton.setVisibility(View.INVISIBLE);
                                mDeclineFriendsRequestButton.setEnabled(false);

                                Toast.makeText(ProfileActivity.this, "Declined Friends Request", Toast.LENGTH_LONG).show();

                            } else {

                                Toast.makeText(ProfileActivity.this, "Error Decline Failed", Toast.LENGTH_LONG).show();
                            }
                            mSendFriendsRequestButton.setEnabled(true);
                        }
                    });
                }


                /*====================== 5. Decline Request State
                if (mCurrentState.equals(strREQUEST_RECEIVED)) {
                    mFriendsRequestDatabase.child(mCurrentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {

                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendsRequestDatabase.child(userId).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {

                                @Override
                                public void onSuccess(Void aVoid) {
                                    mSendFriendsRequestButton.setEnabled(true);
                                    mCurrentState = strNOT_FRIENDS;
                                    mSendFriendsRequestButton.setText(strSend_Friend_Request);

                                    mDeclineFriendsRequestButton.setVisibility(View.INVISIBLE);
                                    mDeclineFriendsRequestButton.setEnabled(false);

                                }
                            });
                        }
                    });
                }
                ==================================*/

            }
        });
    }
}
