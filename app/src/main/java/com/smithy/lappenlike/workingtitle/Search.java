package com.smithy.lappenlike.workingtitle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class Search extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.addContentView(R.layout.search_activity);
    }
}
