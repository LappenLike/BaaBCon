package com.smithy.lappenlike.workingtitle.ContactsFragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smithy.lappenlike.workingtitle.BaseActivity;
import com.smithy.lappenlike.workingtitle.R;

import java.util.HashMap;

public class Contacts extends Fragment {

    private View view;
    private ContactsContainer contactsContainer;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String userId;
    private DatabaseReference databaseRef;

    private LinearLayout contactsLinear;
    private FloatingActionButton addContact;
    private EditText input;
    private AlertDialog alert;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.contacts_activity, container, false);
        contactsContainer = (ContactsContainer) getActivity();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userId = user.getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference("users/"+userId);

        contactsLinear = view.findViewById(R.id.contactsLinear);
        addContact = view.findViewById(R.id.ab_addContact);
        input = new EditText(view.getContext());

        initContacts();
        initAddingContacts();
        return view;
    }

    private void initContacts(){
        databaseRef.child("contacts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    FirebaseDatabase.getInstance().getReference("users/" + snapshot.getValue()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            TextView contactUser = new TextView(getContext());
                            contactUser.setText((String) dataSnapshot.child("name").getValue());
                            contactsLinear.addView(contactUser);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void initAddingContacts(){
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

        builder.setTitle(getString(R.string.addContact_header));
        builder.setMessage(getString(R.string.addContact_text));

        input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(12) });
        input.setGravity(Gravity.CENTER);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().length() == 12){
                    input.setTextColor(ContextCompat.getColor(view.getContext(), R.color.correct));
                } else{
                    input.setTextColor(ContextCompat.getColor(view.getContext(), R.color.wrong));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {/*DoNo*/}

            @Override
            public void afterTextChanged(Editable editable) {/*DoNo*/}
        });
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.addContact_positive), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users/");
                usersRef.orderByChild("userId").equalTo(input.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snap : dataSnapshot.getChildren()){
                            databaseRef.child("contacts").push().setValue(snap.getKey());
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert = builder.create();
        alert.setCanceledOnTouchOutside(true);

        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View diag) {
                alert.show();
            }
        });
    }
}
