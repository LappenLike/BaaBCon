package com.smithy.lappenlike.workingtitle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class Jobs extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.addContentView(R.layout.jobs_activity);
    }
}
