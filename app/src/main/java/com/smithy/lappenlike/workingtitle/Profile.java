package com.smithy.lappenlike.workingtitle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class Profile extends BaseActivity {

    private final int CHOOSE_IMAGE = 100;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase fireInstance;
    private String userId;
    private DatabaseReference databaseRef;
    private StorageReference profileImageRef;

    private ProgressBar pb_progressProfile;

    private TextView tv_profileName;
    private ImageView iv_profileImage;

    private Uri profileImageUri;
    private String profileImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.addContentView(R.layout.profile_activity);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userId = user.getUid();
        fireInstance = FirebaseDatabase.getInstance();

        databaseRef = fireInstance.getReference("users/"+userId);
        profileImageRef = FirebaseStorage.getInstance().getReference("profilePics/"+user.getUid() + ".jpg");

        pb_progressProfile = findViewById(R.id.pb_progressProfile);

        tv_profileName = findViewById(R.id.tv_profileName);
        iv_profileImage = findViewById(R.id.iv_profileImage);

        initProfileData();
        initProfileImage();
    }

    private void initProfileData(){
        DatabaseReference nameRef = fireInstance.getReference("users/"+userId+"/name");
        nameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tv_profileName.setText(dataSnapshot.getValue(String.class));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext())
                        .load(uri.toString()) // Uri of the picture
                        .into(iv_profileImage);
            }
        });
    }

    private void initProfileImage(){
        iv_profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Profilbild Auswahl"), CHOOSE_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null ){
            profileImageUri = data.getData();
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), profileImageUri);
                uploadImage(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(final Bitmap imageBitmap){
        if(profileImageUri != null){
            pb_progressProfile.setVisibility(View.VISIBLE);
            profileImageRef.putFile(profileImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    profileImageUrl = taskSnapshot.getStorage().getDownloadUrl().toString();
                    uploadToFirebase();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(),getString(R.string.imageUpload_fail), Toast.LENGTH_SHORT).show();
                    System.out.println(e.getMessage());
                    pb_progressProfile.setVisibility(View.GONE);
                }
            });
            iv_profileImage.setImageBitmap(imageBitmap);
        }
    }

    private void uploadToFirebase(){
        UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(profileImageUrl)).build();
        user.updateProfile(changeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),getString(R.string.imageUpload_success), Toast.LENGTH_SHORT).show();
                    pb_progressProfile.setVisibility(View.GONE);
                }
            }
        });

    }
}