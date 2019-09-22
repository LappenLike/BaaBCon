package com.smithy.lappenlike.workingtitle.ContactsFragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.smithy.lappenlike.workingtitle.R;

import java.util.Iterator;

public class Contacts extends Fragment {

    private View view;
    private ContactsContainer contactsContainer;

    private ConstraintLayout contactsLayout;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String userId;
    private DatabaseReference userRef;

    private ProgressBar pb_contactsProgress;
    private LinearLayout contactsLinear;
    private FloatingActionButton addContact;
    private EditText input;
    private AlertDialog alert;

    private ImageView cardPic;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.contacts_activity, container, false);
        contactsContainer = (ContactsContainer) getActivity();

        contactsLayout = view.findViewById(R.id.contactsLayout);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userId = user.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users/"+userId);

        contactsLinear = view.findViewById(R.id.contactsLinear);
        addContact = view.findViewById(R.id.ab_addContact);
        input = new EditText(view.getContext());

        pb_contactsProgress = view.findViewById(R.id.pb_contactsProgress);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        initContacts();
        initAddingContacts();
    }

    private void initContacts(){
        userRef.child("contacts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> snapshots = dataSnapshot.getChildren().iterator();
                createCardView(snapshots);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void createCardView(final Iterator<DataSnapshot> snapshots){
        FirebaseDatabase.getInstance().getReference("users/" + snapshots.next().getValue()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                CardView contactCard = new CardView(view.getContext());
                CardView.LayoutParams layoutParams = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT,
                        Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics())));
                contactCard.setLayoutParams(layoutParams);
                contactCard.setRadius(20);
                contactCard.setBackgroundColor(getResources().getColor(R.color.cardViewColor1));

                LinearLayout cardLinear = new LinearLayout(view.getContext());
                cardLinear.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

                TextView nameText = new TextView(view.getContext());
                TableLayout.LayoutParams textParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
                int padding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                textParams.weight = 1;
                nameText.setLayoutParams(textParams);
                nameText.setPadding(padding,padding,padding,padding);
                int textSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
                nameText.setTextSize(textSize);
                nameText.setTypeface(null, Typeface.BOLD);
                nameText.setGravity(Gravity.CENTER_VERTICAL);
                nameText.setText(dataSnapshot.child("name").getValue(String.class));

                cardPic = new ImageView(view.getContext());
                TableLayout.LayoutParams picParams = new TableLayout.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT);
                picParams.weight = 3;
                int picPadding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));
                cardPic.setPadding(picPadding,picPadding,picPadding,picPadding);
                cardPic.setLayoutParams(picParams);
                cardPic.setBackgroundColor(getResources().getColor(R.color.cardViewColor2));

                cardLinear.addView(nameText);
                cardLinear.addView(cardPic);
                contactCard.addView(cardLinear);
                contactsLinear.addView(contactCard);

                StorageReference contactImageRef = FirebaseStorage.getInstance().getReference("profilePics/"+dataSnapshot.getKey() + ".jpg");
                contactImageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Glide.with(view.getContext())
                                    .load(task.getResult().toString())
                                    .into(cardPic);
                            if(snapshots.hasNext()){
                                createCardView(snapshots);
                            }
                        } else{
                            cardPic.setImageDrawable(getResources().getDrawable(R.drawable.default_profile));
                            if(snapshots.hasNext()){
                                createCardView(snapshots);
                            }
                        }
                    }
                });
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
                pb_contactsProgress.setVisibility(View.VISIBLE);
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users/");
                usersRef.orderByChild("userId").equalTo(input.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildrenCount()>0){
                            //always 1, find a better way
                            for(final DataSnapshot snap : dataSnapshot.getChildren()){
                                userRef.child("contacts").push().setValue(snap.getKey()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Snackbar.make(contactsLayout, getString(R.string.userAdded, snap.child("name")), Snackbar.LENGTH_SHORT).show();
                                        }else{
                                            Snackbar.make(contactsLayout, getString(R.string.noUserFound), Snackbar.LENGTH_SHORT).show();
                                        }
                                        pb_contactsProgress.setVisibility(View.GONE);
                                    }
                                });
                            }
                        } else{
                            Snackbar.make(contactsLayout, R.string.noUserFound, Snackbar.LENGTH_SHORT).show();
                            pb_contactsProgress.setVisibility(View.GONE);
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
