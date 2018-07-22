/*
 * Copyright (c)
 * *********
 *  Created by Saksham Negi on 18/7/18 11:37 PM
 *  2018 . All rights reserved.
 *  Last modified 18/7/18 11:37 PM
 */

package com.sakshamnegi.example.firebase;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.sakshamnegi.example.firebase.adapter.ImageAdapter;
import com.sakshamnegi.example.firebase.model.Upload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ShowUploadsActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener {

    ProgressBar mProgressCircle;
    RecyclerView mRecyclerView;
    ImageAdapter mAdapter;

    ValueEventListener mDBListener;//To store value event listener and remove it when app is closed to preven
    //stacking of many listeners

    DatabaseReference mDatabaseRef;
    FirebaseStorage mStorage;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private StorageTask downloadTask;

    ArrayList<Upload> mUploadList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_uploads);

        mProgressCircle = findViewById(R.id.progress_circle);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mUploadList = new ArrayList<>();

        mAdapter = new ImageAdapter(ShowUploadsActivity.this,mUploadList);

        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(ShowUploadsActivity.this);


        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Uploads by "+ currentUser.getDisplayName());


        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mUploadList.clear();
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    Upload upload = postSnapshot.getValue(Upload.class);
                    upload.setmKey(postSnapshot.getKey());
                    mUploadList.add(upload);
                }

                mAdapter.notifyDataSetChanged();//To notify adapter of any changes in database

                mProgressCircle.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(ShowUploadsActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();

                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Upload selectedItem = mUploadList.get(position);
        Toast.makeText(this,"Long press for choices on " + selectedItem.getName().toUpperCase() ,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDownloadClick(int position) {

        if (downloadTask != null && downloadTask.isInProgress())

        {
            Toast.makeText(ShowUploadsActivity.this, "Download already in Progress", Toast.LENGTH_SHORT).show();
        }


        else
        {

            Upload selectedItem = mUploadList.get(position);
            final String selectedKey = selectedItem.getmKey();
            StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getUrl());


            //make directory to store images
            String folder_main = "EsDrive Downloads";

            final File directory = new File(Environment.getExternalStorageDirectory(), folder_main);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            //CODE TO DOWNLOAD FILE


            try {
                final File localFile = File.createTempFile(selectedItem.getName(), ".png", directory);

                downloadTask= imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // SAVE THIS IMAGE TO STORAGE

                        Toast.makeText(ShowUploadsActivity.this, "Saved to " + directory, Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(ShowUploadsActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // progress percentage
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                        // percentage in progress dialog
                        Toast.makeText(ShowUploadsActivity.this, "Downloading " + ((int) progress) + "%...", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //CODE END

    @Override
    public void onDeleteClick(int position) {
        Upload selectedItem = mUploadList.get(position);
        final String selectedKey = selectedItem.getmKey();

        StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getUrl());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseRef.child(selectedKey).removeValue();
                Toast.makeText(ShowUploadsActivity.this,"Deleted Image Successfully",Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseRef.removeEventListener(mDBListener);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

}
