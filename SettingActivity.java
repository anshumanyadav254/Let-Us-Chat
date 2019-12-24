package com.luciferhacker.letuschat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingActivity extends AppCompatActivity implements MyStringsConstant {

    private Toolbar mToolbar;
    private CircleImageView mProfileImage;
    private TextView mProfileName;
    private TextView mStatus;
    private Button mChangeStatusButton, mChangeImageButton;
    private DatabaseReference mUsersDatabase;
    private FirebaseUser mCurrentUser;
    private StorageReference mImageStorage;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mToolbar = (Toolbar) findViewById(R.id.setting_appbar_include);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(strAccount_Settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mProfileImage = (CircleImageView) findViewById(R.id.setting_profileImage_circleImageView);
        mProfileName = (TextView) findViewById(R.id.setting_profileName_textView);
        mStatus = (TextView) findViewById(R.id.setting_status_textView);
        mChangeStatusButton = (Button) findViewById(R.id.setting_changeStatus_button);
        mChangeImageButton = (Button) findViewById(R.id.setting_changeImage_button);

        mImageStorage = FirebaseStorage.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        String currentUserId = mCurrentUser.getUid();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(strUSERS_DATABASE).child(currentUserId);
        mUsersDatabase.keepSynced(true);

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    String name = dataSnapshot.child(strNAME).getValue(String.class);
                    final String image = dataSnapshot.child(strIMAGE).getValue(String.class);
                    String status = dataSnapshot.child(strSTATUS).getValue(String.class);
                    String thumbImage = dataSnapshot.child(strTHUMB_IMAGE).getValue(String.class);

                    mProfileName.setText(name);
                    mStatus.setText(status);

                    if (!image.equals(strDEFAULT)) {
                        Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(mProfileImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mChangeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String statusValue = mStatus.getText().toString();
                Intent statusIntent = new Intent(SettingActivity.this, StatusActivity.class);
                statusIntent.putExtra(strSTATUS_VALUE, statusValue);
                startActivity(statusIntent);

            }
        });

        mChangeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(SettingActivity.this);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(SettingActivity.this);
                mProgressDialog.setTitle("Uploading Profile Image");
                mProgressDialog.setMessage("please wait");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();
                File thumbImageFilePathFileRef = new File(resultUri.getPath());

                String currentUserId = mCurrentUser.getUid();
                Bitmap thumbImageBitmap = new Compressor(this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(thumbImageFilePathFileRef);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumbImageByte = baos.toByteArray();

                final StorageReference filePath = mImageStorage.child(strPROFILE_IMAGES).child(strPROFILE_ORIGINAL_IMAGES).child(currentUserId + ".jpg");
                final StorageReference thumbImageFilePathStorageReference = mImageStorage.child(strPROFILE_IMAGES).child(strPROFILE_THUMB_IMAGES).child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String imageDownloadUrl = uri.toString();

                                    UploadTask uploadTask = thumbImageFilePathStorageReference.putBytes(thumbImageByte);

                                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> thumb_task) {
                                            if (thumb_task.isSuccessful()) {
                                                thumbImageFilePathStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        String thumbImageDownloadUrl = uri.toString();

                                                        if (thumb_task.isSuccessful()) {

                                                            Map updateHashMap = new HashMap();
                                                            updateHashMap.put(strIMAGE, imageDownloadUrl);
                                                            updateHashMap.put(strTHUMB_IMAGE, thumbImageDownloadUrl);


                                                            mUsersDatabase.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        mProgressDialog.dismiss();
                                                                        Toast.makeText(SettingActivity.this, "Profile Image Updated", Toast.LENGTH_LONG).show();
                                                                    } else {
                                                                        mProgressDialog.dismiss();
                                                                        Toast.makeText(SettingActivity.this, "failed", Toast.LENGTH_LONG).show();
                                                                    }

                                                                }
                                                            });

                                                        } else {
                                                            mProgressDialog.dismiss();
                                                            Toast.makeText(SettingActivity.this, "Thumb downloadUrl failed", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });

                                            } else {
                                                mProgressDialog.dismiss();
                                                Toast.makeText(SettingActivity.this, "Thumb Upload failed", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });


                                }
                            });

                        } else {
                            Toast.makeText(SettingActivity.this, "Upload Image failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(SettingActivity.this, "Crop Image Activity failed" + error, Toast.LENGTH_LONG).show();
            }

        }
    }
}