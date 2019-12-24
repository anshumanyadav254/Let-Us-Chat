package com.luciferhacker.letuschat;

import android.app.ProgressDialog;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity implements MyStringsConstant {

    private TextInputLayout mProfileName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateAccountButton;
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersDatabase;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mToolbar = (Toolbar) findViewById(R.id.register_appbar_include);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(strRegister);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mProgressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        mProfileName = (TextInputLayout) findViewById(R.id.register_profileName_textInputLayout);
        mEmail = (TextInputLayout) findViewById(R.id.register_email_textInputLayout);
        mPassword = (TextInputLayout) findViewById(R.id.register_password_textInputLayout);
        mCreateAccountButton = (Button) findViewById(R.id.register_createAccount_button);

        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String displayName = mProfileName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(displayName) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {
                    mProgressDialog.setTitle("Registering User");
                    mProgressDialog.setMessage("please wait while we create your account");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();

                    registerUser(displayName, email, password);
                }
            }
        });
    }

    // Create a new createAccount method which takes in an email address and password,
    // validates them and then creates a new user with the createUserWithEmailAndPassword method.
    private void registerUser(final String displayName, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String currentUserId = currentUser.getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    UsersDatabase = FirebaseDatabase.getInstance().getReference().child(strUSERS_DATABASE).child(currentUserId);
                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put(strNAME, displayName);
                    userMap.put(strSTATUS, "Hi there I'm using LetUsChat.");
                    userMap.put(strIMAGE, strDEFAULT);
                    userMap.put(strTHUMB_IMAGE, strDEFAULT);
                    userMap.put(strDEVICE_TOKEN, deviceToken);

                    UsersDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mProgressDialog.dismiss();
                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });

                } else {

                    mProgressDialog.hide();
                    // If sign in fails, display a message to the user.
                    Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
                // ...
            }
        });
    }
}
