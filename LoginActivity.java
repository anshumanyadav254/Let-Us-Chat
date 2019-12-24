package com.luciferhacker.letuschat;

import android.app.ProgressDialog;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity implements MyStringsConstant {

    private Toolbar mToolbar;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mLoginButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mToolbar = (Toolbar) findViewById(R.id.login_appbar_include);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(strLogin);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mEmail = (TextInputLayout) findViewById(R.id.login_email_textInputLayout);
        mPassword = (TextInputLayout) findViewById(R.id.login_password_textInputLayout);
        mLoginButton = (Button) findViewById(R.id.login_login_button);

        mProgressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(strUSERS_DATABASE);

        mLoginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {
                    mProgressDialog.setTitle("Logging In");
                    mProgressDialog.setMessage("please wait while we login");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();

                    loggingUser(email, password);
                }
            }

            private void loggingUser(String email, String password) {

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            mProgressDialog.dismiss();

                            String currentUserId = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            mUsersDatabase.child(currentUserId).child(strDEVICE_TOKEN).setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();

                                }
                            });

                        } else {
                            mProgressDialog.hide();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}