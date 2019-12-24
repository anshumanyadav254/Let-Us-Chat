package com.luciferhacker.letuschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements MyStringsConstant {

    private Toolbar mToolbar;
    private TextView mProfileName;
    private TextView mLastSeen;
    private CircleImageView mProfileImage;

    private ImageButton mAddButton;
    private ImageButton mSendMessageButton;
    private EditText mTypeText;

    private DatabaseReference mRootReferenceDatabase;
    private FirebaseUser mCurrentUser;
    private String mChatUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootReferenceDatabase = FirebaseDatabase.getInstance().getReference();

        mChatUserId = getIntent().getStringExtra(strUSER_ID);

        mToolbar = (Toolbar)findViewById(R.id.chat_appbar_include);
        setSupportActionBar(mToolbar);
        ActionBar customActionBar = getSupportActionBar();
        customActionBar.setDisplayHomeAsUpEnabled(true);
        customActionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle(mChatUserId);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customActionBarView = inflater.inflate(R.layout.chat_custom_appbar, null);
        customActionBar.setCustomView(customActionBarView);

        // CUSTOM ACTION BAR ITEM
        mProfileName = (TextView)findViewById(R.id.chatCustomAppbar_profileName_textView);
        mLastSeen = (TextView)findViewById(R.id.chatCustomAppbar_lastSeen_textView);
        mProfileImage = (CircleImageView) findViewById(R.id.chatCustomAppbar_profileImage_circleImageView);

        mAddButton = (ImageButton) findViewById(R.id.chat_addButton_imageButton);
        mSendMessageButton = (ImageButton) findViewById(R.id.chat_sendButton_imageButton);
        mTypeText = (EditText) findViewById(R.id.chat_typeText_textView);

        //==========[ START. CHAT APPBAR ACCESSING DATA ]===============//
        mRootReferenceDatabase.child(strUSERS_DATABASE).child(mChatUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(mCurrentUser != null) {

                    String online = dataSnapshot.child(strONLINE).getValue().toString();
                    String image = dataSnapshot.child(strIMAGE).getValue().toString();
                    String name = dataSnapshot.child(strNAME).getValue().toString();

                    mProfileName.setText(name);
                    Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                    if (online.equals(strTRUE)) {
                        mLastSeen.setText(strONLINE);

                    } else {

                        GetTimeAgo getTimeAgoObject = new GetTimeAgo();
                        long lastTime = Long.parseLong(online);
                        String lastSeenTime = getTimeAgoObject.getTimeAgo(lastTime, getApplicationContext());
                        mLastSeen.setText(lastSeenTime);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }); //==========[ END. CHAT APPBAR ACCESSING DATA ]===============//


        //==========[ START. NEW FRIENDS FIRST TIME CHAT DATABASE ADDING DATA ]===============//
        mRootReferenceDatabase.child(strCHAT_DATABASE).child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mChatUserId)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put(strSEEN, strFALSE);
                    chatAddMap.put(strTIME_STAMP, ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put(strCHAT_DATABASE+"/"+mCurrentUser.getUid()+"/"+mChatUserId,chatAddMap);
                    chatUserMap.put(strCHAT_DATABASE+"/"+mChatUserId+"/"+mCurrentUser.getUid(),chatAddMap);

                    mRootReferenceDatabase.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }); //==========[ END. NEW FRIENDS FIRST TIME CHAT DATABASE ADDING DATA ]===============//

        // COMMENT
        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });


    }

    private void sendMessage() {
        String message = mTypeText.getText().toString();

        if(!TextUtils.isEmpty(message)){

            String currentUserRef = strMESSAGES_DATABASE+"/"+mCurrentUser.getUid()+"/"+mChatUserId;
            String chatUserRef    = strMESSAGES_DATABASE+"/"+mChatUserId+"/"+mCurrentUser.getUid();

            DatabaseReference userMessagePush = mRootReferenceDatabase.child(strMESSAGES_DATABASE).child(mCurrentUser.getUid()).child(mChatUserId).push();
            String pushId = userMessagePush.getKey();

            Map messageMap = new HashMap();
            messageMap.put(strMESSAGE, message);
            messageMap.put(strSEEN, strFALSE);
            messageMap.put(strTYPE, strTEXT);
            messageMap.put(strTIME, ServerValue.TIMESTAMP);

            Map chatUserMap = new HashMap();
            chatUserMap.put(currentUserRef+"/"+pushId, messageMap);
            chatUserMap.put(chatUserRef+"/"+pushId, messageMap);

            mRootReferenceDatabase.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    if(databaseError != null){
                        Log.d("CHAT LOG", databaseError.getMessage());
                    }
                    Toast.makeText(ChatActivity.this, "message sent", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
