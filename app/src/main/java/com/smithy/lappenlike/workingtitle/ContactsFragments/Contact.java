package com.smithy.lappenlike.workingtitle.ContactsFragments;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smithy.lappenlike.workingtitle.R;

public class Contact extends Fragment {

    private View view;
    private ContactsContainer contactsContainer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.contact_activity, container, false);
        contactsContainer = (ContactsContainer) getActivity();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

}
