package com.luciferhacker.letuschat;

import android.app.ProgressDialog;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity implements MyStringsConstant {

    private Toolbar mToolbar;
    private TextInputLayout mStatus;
    private Button mSaveStatusButton;
    private DatabaseReference mUsersDatabase;
    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = mCurrentUser.getUid();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(strUSERS_DATABASE).child(currentUserId);

        mToolbar = (Toolbar) findViewById(R.id.status_appbar_include);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(strAccount_Status);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatus = (TextInputLayout) findViewById(R.id.status_status_textInputLayout);
        mSaveStatusButton = (Button) findViewById(R.id.status_saveStatus_button);

        String statusValue = getIntent().getStringExtra(strSTATUS_VALUE);
        mStatus.getEditText().setText(statusValue);

        mSaveStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgressDialog = new ProgressDialog(StatusActivity.this);
                mProgressDialog.setTitle("Saving Changes");
                mProgressDialog.setMessage("please wait !");
                mProgressDialog.show();

                String status = mStatus.getEditText().getText().toString();
                mUsersDatabase.child(strSTATUS).setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mProgressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_LONG).show();

                            Intent settingIntent = new Intent(StatusActivity.this, SettingActivity.class);
                            startActivity(settingIntent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });
            }
        });


    }
}
