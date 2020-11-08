package com.example.whats_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private Button updateAccountBtn;
    private EditText username, userStatus;
    private CircleImageView userProfileImage;

    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference userProfileImageReference;
    private ProgressDialog progressDialog;

    private Toolbar SettingToolbar;

    private final int GalleryPick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();
        //username.setVisibility(View.INVISIBLE);

        updateAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSetting();
            }
        });
        retrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
    }

    private void retrieveUserInfo() {
        rootRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists())
                                && (dataSnapshot.hasChild("name")
                                && (dataSnapshot.hasChild("image")))) {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrievesStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            username.setText(retrieveUserName);
                            userStatus.setText(retrievesStatus);
                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                        } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrievesStatus = dataSnapshot.child("status").getValue().toString();

                            username.setText(retrieveUserName);
                            userStatus.setText(retrievesStatus);
                        } else {
                            username.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingActivity.this,
                                    "Please set & update your profile information...",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            Uri ImageUri = data.getData();

            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                progressDialog.setTitle("Set Profile image");
                //progressDialog.setMessage("Please wait");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();

                StorageReference filePath = userProfileImageReference.child(currentUserId + ".jpg");

                final UploadTask uploadTask = filePath.putFile(resultUri);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String download_url = uri.toString();
                                rootRef.child("Users").child(currentUserId).child("image")
                                        .setValue(download_url)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SettingActivity.this,
                                                            "Image save in database, Successfully",
                                                            Toast.LENGTH_SHORT).show();
                                                } else {
                                                    String msg = task.getException().toString();
                                                    Toast.makeText(SettingActivity.this,
                                                            "Error: " + msg,
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                                progressDialog.dismiss();
                                            }
                                        });
                            }
                        });
                    }
                });
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String msg = e.toString();
                        Toast.makeText(SettingActivity.this,
                                "Error: " + msg, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                });

            }
        }
    }

    private void UpdateSetting() {
        String setUsername = username.getText().toString();
        String setStatus = userStatus.getText().toString();
        if (TextUtils.isEmpty(setUsername) || TextUtils.isEmpty(setStatus)) {
            Toast.makeText(this, "Enter data", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserId);
            profileMap.put("name", setUsername);
            profileMap.put("status", setStatus);
            rootRef.child("Users").child(currentUserId).updateChildren(profileMap).
                    addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //SendUserToMainActivity();
                                Toast.makeText(SettingActivity.this, "Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                String msg = task.getException().toString();
                                Toast.makeText(SettingActivity.this, "Error: " + msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    private void InitializeFields() {
        updateAccountBtn = findViewById(R.id.update_setting_button);
        username = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);
        progressDialog = new ProgressDialog(this);

        SettingToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }

    /*private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }*/
}