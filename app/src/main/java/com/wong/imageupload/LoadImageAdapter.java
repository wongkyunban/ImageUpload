package com.wong.imageupload;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LoadImageAdapter extends RecyclerView.Adapter<LoadImageAdapter.ButtonViewHolder> {


    private final static int BUTTON_TYPE = 100;
    private final static int IMAGE_TYPE = 200;
    private List<ItemBean> list = null;

    private OnImageItemClickListener onImageItemClickListener;

    public LoadImageAdapter(List<ItemBean> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case BUTTON_TYPE:
                View buttonView = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_image_button_item, parent, false);
                return new ButtonViewHolder(buttonView);
            default:
                View imageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
                return new ImageViewHolder(imageView);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ButtonViewHolder holder, final int position) {
        if (!list.get(position).isButton()) {
            ImageViewHolder imageViewHolder = (ImageViewHolder)holder;
            // 显示图片
            imageViewHolder.mIVImg.setImageBitmap(list.get(position).getImageFileBean().getBitmap());
            boolean startUpload = list.get(position).getImageFileBean().isStartUpload();
            boolean isUpload = list.get(position).getImageFileBean().isUpload();
            if(startUpload && !isUpload){
                imageViewHolder.mPB.setVisibility(View.VISIBLE);
            }else{
                imageViewHolder.mPB.setVisibility(View.GONE);
            }
            // 点击删除按钮
            imageViewHolder.mIVDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onImageItemClickListener != null) {
                        onImageItemClickListener.onDelete(v, list.get(position), position);
                    }
                }
            });
        }

        holder.mIVImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onImageItemClickListener != null) {
                    onImageItemClickListener.onClick(v, list.get(position), position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).isButton()) {
            return BUTTON_TYPE;
        } else {
            return IMAGE_TYPE;
        }
    }

    public void setOnImageItemClickListener(OnImageItemClickListener onImageItemClickListener) {
        this.onImageItemClickListener = onImageItemClickListener;
    }

    static class ButtonViewHolder extends RecyclerView.ViewHolder {

        ImageView mIVImg;
        View view;

        public ButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            mIVImg = itemView.findViewById(R.id.iv_img);
        }
    }

    static class ImageViewHolder extends ButtonViewHolder {
        ImageView mIVDel;
        ProgressBar mPB;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            mIVDel = itemView.findViewById(R.id.iv_delete);
            mPB = itemView.findViewById(R.id.pb_bar);
        }
    }

    public interface OnImageItemClickListener {
        void onClick(View view, ItemBean itemBean, int position);

        void onDelete(View view, ItemBean itemBean, int position);
    }
}
