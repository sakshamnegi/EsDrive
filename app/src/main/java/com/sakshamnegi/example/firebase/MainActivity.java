/*
 * Copyright (c)
 * *********
 *  Created by Saksham Negi on 14/7/18 9:50 PM
 *  2018 . All rights reserved.
 *  Last modified 14/7/18 9:50 PM
 */

package com.sakshamnegi.example.firebase;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int SIGN_IN_REQ_CODE = 100;

    List<AuthUI.IdpConfig> mProviders = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build(), new AuthUI.IdpConfig.EmailBuilder().build());
    FirebaseUser mCurrentUser;
    CardView crdLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mCurrentUser != null) {
            //UserLogged In
            loginUser();

        }
        crdLogin = findViewById(R.id.card_login);
        crdLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Prompt to login or signup
                startActivityForResult(AuthUI.getInstance().
                        createSignInIntentBuilder().setAvailableProviders(mProviders).build(), SIGN_IN_REQ_CODE);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQ_CODE) {

            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                //sign in OK
                loginUser();
            } else {
                //sign in Failed
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Cancelled by user", Toast.LENGTH_SHORT).show();
                }
                else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();

                }
            }
        }


    }

    private void loginUser() {
        Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(homeIntent);
        finish();
    }

}
