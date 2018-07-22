/*
 * Copyright (c)
 * *********
 *  Created by Saksham Negi on 18/7/18 11:57 PM
 *  2018 . All rights reserved.
 *  Last modified 18/7/18 11:57 PM
 */

package com.sakshamnegi.example.firebase.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sakshamnegi.example.firebase.R;
import com.sakshamnegi.example.firebase.model.Upload;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    OnItemClickListener mListener;
    Context mContext;
    ArrayList<Upload> mUploadList;

    public ImageAdapter(Context context, ArrayList<Upload> uploadList){
        mContext = context;
        mUploadList = uploadList;

    }


    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.image_item,parent,false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {

        Upload currentUpload = mUploadList.get(position);
        holder.textViewName.setText(currentUpload.getName());
        Picasso.with(mContext).load(currentUpload.getUrl()).placeholder(R.mipmap.ic_launcher).fit()
                .centerCrop().into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return mUploadList.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener,MenuItem.OnMenuItemClickListener{

        public TextView textViewName;
        public ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.txt_image_name);
            imageView = itemView.findViewById(R.id.img_view_upload);

            itemView.setOnClickListener(this);

            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mListener != null){
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION){
                    mListener.onItemClick(position);
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Select Action");
            MenuItem doWhatever = menu.add(Menu.NONE, 1, 1,"Download");
            MenuItem delete = menu.add(Menu.NONE, 2, 2, "Delete");

            doWhatever.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);

        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if(mListener != null){
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION){
                    switch (item.getItemId()){
                        case 1:
                            mListener.onDownloadClick(position);
                            return true;
                        case 2:
                            mListener.onDeleteClick(position);
                            return true;
                    }
                }
                }
            return false;
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position);

        void onDownloadClick(int position);

        void onDeleteClick(int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;

    }


}
