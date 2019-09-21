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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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
import com.smithy.lappenlike.workingtitle.databinding.ProfileActivityBinding;

import java.io.IOException;

public class Profile extends BaseActivity implements ActivityContract.View {

    private final int CHOOSE_IMAGE = 100;

    private ProfileActivityBinding binding;
    private ProfileModel profileModel;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase fireInstance;
    private String userId;
    private DatabaseReference userRef;
    private StorageReference profileImageRef;

    private ConstraintLayout profileLayout;

    private ProgressBar pb_progressProfile;
    private TextView tv_profileDescription;
    private ImageView iv_profileImage;

    private Uri profileImageUri;
    private String profileImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.addContentView(R.layout.profile_activity);
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.profile_activity, (ConstraintLayout) findViewById(R.id.profileLayout), true);

        ProfilePresenter presenter = new ProfilePresenter(this, getApplicationContext());
        binding.setPresenter(presenter);
        profileModel = new ProfileModel();
        binding.setProfileModel(profileModel);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userId = user.getUid();
        fireInstance = FirebaseDatabase.getInstance();
        userRef = fireInstance.getReference("users/"+userId);

        profileLayout = findViewById(R.id.profileLayout);

        databaseRef = fireInstance.getReference("users/"+userId);
        profileImageRef = FirebaseStorage.getInstance().getReference("profilePics/"+user.getUid() + ".jpg");

        pb_progressProfile = findViewById(R.id.pb_progressProfile);

        tv_profileDescription = findViewById(R.id.tv_profileDescription);
        iv_profileImage = findViewById(R.id.iv_profileImage);

    }

    @Override
    protected void onStart(){
        super.onStart();
        initProfileData();
        initProfileImage();
    }

    private void initProfileData(){
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                profileModel.setUsername(dataSnapshot.child("name").getValue(String.class));
                profileModel.setProfileDescription(dataSnapshot.child("profileDescription").getValue(String.class));
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
                    Snackbar.make(profileLayout, R.string.imageUpload_fail, Snackbar.LENGTH_SHORT).show();
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
                    Snackbar.make(profileLayout, R.string.imageUpload_success, Snackbar.LENGTH_SHORT).show();
                    pb_progressProfile.setVisibility(View.GONE);
                }
            }
        });

    }
}