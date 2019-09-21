package com.smithy.lappenlike.workingtitle;

import android.net.Uri;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class ProfileModel extends BaseObservable {

    private String username;
    private String profileDescription;

    public ProfileModel() {}

    public ProfileModel(String username, String profileDescription, Uri profileImageUri) {
        this.username = username;
        this.profileDescription = profileDescription;
    }

    @Bindable
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        notifyPropertyChanged(com.smithy.lappenlike.workingtitle.BR.username);
    }

    @Bindable
    public String getProfileDescription() {
        return profileDescription;
    }

    public void setProfileDescription(String profileDescription) {
        this.profileDescription = profileDescription;
        notifyPropertyChanged(com.smithy.lappenlike.workingtitle.BR.profileDescription);
    }
}
