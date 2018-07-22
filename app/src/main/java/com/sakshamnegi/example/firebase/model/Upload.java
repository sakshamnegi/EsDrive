/*
 * Copyright (c)
 * *********
 *  Created by Saksham Negi on 18/7/18 2:41 PM
 *  2018 . All rights reserved.
 *  Last modified 14/7/18 10:01 PM
 */

package com.sakshamnegi.example.firebase.model;

import com.google.firebase.database.Exclude;

public class Upload {

    String name, url, mKey;

    public  Upload(){
        //empty constructor needed by firebase
    }

    public Upload(String name, String url) {
        if(name.trim().equals("")){
            name = "No Name";
        }
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Exclude
    public void setmKey(String mKey) {
        this.mKey = mKey;
    }

    @Exclude
    public String getmKey() {

        return mKey;
    }
}
