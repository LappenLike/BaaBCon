
package com.smithy.lappenlike.workingtitle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SignUp extends AppCompatActivity {

    private EditText et_pw;
    private EditText et_email;
    private EditText et_username;
    private ProgressBar pb_progress;
    private Button backButton;
    private Button signupButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);

        mAuth = FirebaseAuth.getInstance();

        et_pw = findViewById(R.id.et_pw);
        et_email = findViewById(R.id.et_email);
        et_username = findViewById(R.id.et_username);
        pb_progress =  findViewById(R.id.pb_progress);

        backButton();
        signupUser();
    }

    //Startet einen Intent zurück auf die Login-Page
    private void backButton(){
        backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getApplicationContext(), Login.class));
                    }
                }
        );
    }

    //Setzt einen ClickListener auf den Registrieren Button und führt validUser und registerUser aus
    private void signupUser(){
        signupButton = findViewById(R.id.btn_register);
        signupButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // nur wenn die validUser true zurück gibt, wird der User registriert
                        if(validUser()){
                            registerTheUser();
                        }
                    }
                }
        );
    }

    //Registriert den User in der Firebase Datenbank
    private void registerTheUser(){
        pb_progress.setVisibility(View.VISIBLE);
        String password = et_pw.getText().toString().trim();
        String email = et_email.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    setAllDatabasePaths();
                    pb_progress.setVisibility(View.GONE);
                    finish();
                    Toast.makeText(getApplicationContext(),getString(R.string.registration_success), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), Profile.class));
                }else{
                    if(task.getException() instanceof FirebaseAuthUserCollisionException){
                        Toast.makeText(getApplicationContext(),getString(R.string.email_in_use), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(),task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    //Prüft, ob die Eingabewerte in den 2 Feldern valide sind
    private boolean validUser(){
        boolean validRegister = true;
        String password = et_pw.getText().toString().trim();
        String email = et_email.getText().toString().trim();
        String username = et_username.getText().toString().trim();

        if(password.isEmpty()){
            et_pw.setError(getString(R.string.err_password_required));
            et_pw.requestFocus();
            validRegister = false;
        } else if(password.length() < 6){
            et_pw.setError(getString(R.string.err_pw_too_short));
            et_pw.requestFocus();
        }

        if(email.isEmpty()){
            et_email.setError(getString(R.string.err_email_required));
            et_email.requestFocus();
            validRegister = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            et_email.setError(getString(R.string.err_email_invalid));
            et_email.requestFocus();
            validRegister = false;
        }

        if(username.isEmpty()){
            et_username.setError(getString(R.string.err_username_required));
            et_username.requestFocus();
            validRegister = false;
        } else if (username.length() <= 2) {
            et_username.setError(getString(R.string.err_username_invalid));
            et_username.requestFocus();
            validRegister = false;;
        }
        return validRegister;
    }

    private void setAllDatabasePaths(){
        final FirebaseUser user = mAuth.getCurrentUser();;
        final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users/"+user.getUid());

        databaseRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                databaseRef.child("name").setValue(et_username.getText().toString().trim());
                DateFormat df = DateFormat.getTimeInstance();
                df.setTimeZone(TimeZone.getTimeZone("gmt"));
                String gmtTime = df.format(new Date());
                databaseRef.child("creationDate").setValue(gmtTime);
                String currentTime = String.valueOf(System.currentTimeMillis());
                databaseRef.child("creationDateMillis").setValue(currentTime);
                databaseRef.child("userId").setValue(currentTime.substring(1));
                return Transaction.success(mutableData);
            }
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {/*DoNo*/}
        });
    }

}
