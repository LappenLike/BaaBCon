package com.smithy.lappenlike.workingtitle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smithy.lappenlike.workingtitle.ContactsFragments.Contacts;
import com.smithy.lappenlike.workingtitle.ContactsFragments.ContactsContainer;

public class BaseActivity extends AppCompatActivity {

    public static FirebaseAuth mAuth;
    public static FirebaseUser user;
    public static DatabaseReference databaseRef;
    public static DatabaseReference weaponRef;

    private Boolean firstConn = true;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference("users/"+user.getUid());

        weaponRef = FirebaseDatabase.getInstance().getReference("users/"+user.getUid()+"/weapons");

        //logt den User aus wenn keine Verbindung zur Datenbank besteht (instant!)
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                } else {
                    if(firstConn){ //needed because first invocation always shows "false", so user gets logged off after logging in
                        firstConn = false;
                    } else {
                        finish();
                        mAuth.signOut();
                        startActivity(new Intent(getApplicationContext(), Login.class));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Base cancelled.");
            }
        });

        mDrawerLayout = findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        initSidebar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //Wird in den andere Activities benötigt um die Seite zu initialisieren
    public void addContentView(int layoutId) {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(layoutId, null, false);
        mDrawerLayout.addView(contentView, 0);
    }

    //3 Striche oben Links toggeln die Sidebar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mToggle.onOptionsItemSelected(item);
    }

    private void initSidebar(){
        NavigationView menu = findViewById(R.id.menu);
        menu.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case(R.id.nav_profile):
                        startActivity(new Intent(getApplicationContext(),Profile.class));
                        break;
                    case(R.id.nav_search):
                        startActivity(new Intent(getApplicationContext(), Search.class));
                        break;
                    case(R.id.nav_offer):
                        startActivity(new Intent(getApplicationContext(), Offer.class));
                        break;
                    case(R.id.nav_jobs):
                        startActivity(new Intent(getApplicationContext(), Jobs.class));
                        break;
                    case(R.id.nav_notifications):
                        startActivity(new Intent(getApplicationContext(), Notifications.class));
                        break;
                    case(R.id.nav_contacts):
                        startActivity(new Intent(getApplicationContext(), ContactsContainer.class));
                        break;
                    case(R.id.nav_settings):
                        startActivity(new Intent(getApplicationContext(), Settings.class));
                        break;
                    case(R.id.nav_logout):
                        finish();
                        mAuth.signOut();
                        startActivity(new Intent(getApplicationContext(), Login.class));
                        break;
                }
                return true;
            }
        });
    }
}
