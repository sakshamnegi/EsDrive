/*
 * Copyright (c)
 * *********
 *  Created by Saksham Negi on 19/7/18 2:56 PM
 *  2018 . All rights reserved.
 *  Last modified 19/7/18 2:56 PM
 */

package com.sakshamnegi.example.firebase;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sakshamnegi.example.firebase.model.Upload;
import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity {

    private final static int PICK_IMAGE_REQUEST=123;

    Button btnChoose, btnUpload;
    TextView txtShow;
    EditText edtName;
    ImageView imageView;

    //uri to store file
    private Uri mImageUri;

    //firebase objects
    private StorageReference storageReference;
    private DatabaseReference mDatabaseRef;
    FirebaseUser currentUser ;

    private UploadTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

            //init Views
        btnChoose = (Button) findViewById(R.id.buttonChoose);
        btnUpload = (Button) findViewById(R.id.buttonUpload);
        imageView = (ImageView) findViewById(R.id.imageView);
        edtName = (EditText) findViewById(R.id.editText);
        txtShow = (TextView) findViewById(R.id.textViewShow);

        //init Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Uploads by "+ currentUser.getDisplayName());
        storageReference = FirebaseStorage.getInstance().getReference("Uploads by "+ currentUser.getDisplayName());   //we'll save it to this folder


        // Get intent, action and MIME type
        // from other apps trying to upload image
        Intent otherIntent = getIntent();
        String action = otherIntent.getAction();
        String type = otherIntent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(otherIntent); // Handle single image being sent
            }

        }



        //Display welcome message
        Toast.makeText(HomeActivity.this,"Welcome "+currentUser.getDisplayName(),Toast.LENGTH_SHORT).show();

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();

            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUploadTask != null && mUploadTask.isInProgress())

                {
                    Toast.makeText(HomeActivity.this,"Upload in Progress",Toast.LENGTH_SHORT).show();
                }
                else
                    uploadFile();
            }
        });

        txtShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUploadsActivity();
            }
        });
    }


    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data !=null && data.getData() != null){
            mImageUri = data.getData();

            Picasso.with(this).load(mImageUri).into(imageView);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == R.id.menu_logout)
        {
            //logout user
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // logged out
                            Toast.makeText(HomeActivity.this,"Logged Out Successfully",Toast.LENGTH_SHORT).show();
                            Intent mainIntent = new Intent(HomeActivity.this,MainActivity.class);
                            startActivity(mainIntent);
                            finish();
                        }
                    });

        }
        return true;
    }

    //GOOGLE IT
    private String getFileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(cr.getType(uri));

    }


    private void uploadFile() {
        if(mImageUri != null){

            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));

            mUploadTask = fileReference.putFile(mImageUri);

            mUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri taskResult = task.getResult();
                        Upload upload = new Upload(edtName.getText().toString().trim(),taskResult.toString());
                        //Firebase database key
                        String uploadId = mDatabaseRef.push().getKey();
                        mDatabaseRef.child(uploadId).setValue(upload);
                        Toast.makeText(HomeActivity.this,"Upload successful",Toast.LENGTH_LONG).show();
                        mImageUri = null;
                        edtName.setText("");
                        imageView.setImageDrawable(null);

                    }
                }
            });


        }

        else{
            Toast.makeText(this,"No file selected",Toast.LENGTH_SHORT).show();
        }
    }



    private void openUploadsActivity() {

        Intent intent = new Intent(this,ShowUploadsActivity.class);
        startActivity(intent);
    }

    void handleSendImage(Intent intent) {

        if(currentUser != null) {

            // User logged in. Show image and upload
            Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageUri != null) {
                // Update UI to reflect image being shared
                mImageUri = imageUri;
                Picasso.with(this).load(mImageUri).into(imageView);
            }
        }

        else //Not logged in
            Toast.makeText(this,"First login and then try again",Toast.LENGTH_SHORT).show();
    }

}


