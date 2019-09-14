package com.smithy.lappenlike.workingtitle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class Settings extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.addContentView(R.layout.settings_activity);
    }
}
