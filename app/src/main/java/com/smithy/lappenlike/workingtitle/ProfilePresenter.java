package com.smithy.lappenlike.workingtitle;

import android.content.Context;

public class ProfilePresenter implements ActivityContract.Presenter {
    private ActivityContract.View view;
    private Context context;

    public ProfilePresenter(ActivityContract.View view, Context context){
        this.view = view;
        this.context = context;
    }
}
