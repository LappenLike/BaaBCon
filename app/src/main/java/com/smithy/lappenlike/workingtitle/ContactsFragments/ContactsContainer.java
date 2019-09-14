package com.smithy.lappenlike.workingtitle.ContactsFragments;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.smithy.lappenlike.workingtitle.BaseActivity;
import com.smithy.lappenlike.workingtitle.R;

public class ContactsContainer extends BaseActivity {

    private ContactsAdapter adapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.addContentView(R.layout.contacts_container_activity);
        adapter = new ContactsAdapter(getSupportFragmentManager(), 0);
        mViewPager = findViewById(R.id.contactsPager);
        setupViewPager(mViewPager);

    }

    private void setupViewPager(ViewPager viewPager){
        ContactsAdapter methodAdapter = new ContactsAdapter(getSupportFragmentManager(), 0);
        methodAdapter.addFragment(new Contacts(), "Contacts");
        methodAdapter.addFragment(new Contact(), "Contact");
        viewPager.setAdapter(methodAdapter);
    }

    public void setViewPager(int fragmentNumber){
        mViewPager.setCurrentItem(fragmentNumber);
    }
}
