package com.smithy.lappenlike.workingtitle;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends BaseActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase fireInstance;
    private String userId;
    private DatabaseReference databaseRef;

    private TextView tv_profileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.addContentView(R.layout.profile_activity);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userId = user.getUid();
        fireInstance = FirebaseDatabase.getInstance();
        databaseRef = fireInstance.getReference("users/"+userId);

        tv_profileName = findViewById(R.id.tv_profileName);

        initProfileData();
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
    }

}
