package com.wong.imageupload;

import android.graphics.Bitmap;

import java.io.File;

public class ImageFileBean {
    private File file;
    private boolean isUpload = false; //标识该文件是否上传
    private Bitmap bitmap;
    private boolean startUpload;

    public ImageFileBean(File file, int pg) {
        this.file = file;
    }

    public ImageFileBean(File file, Bitmap bitmap,boolean isUpload) {
        this.file = file;
        this.isUpload = isUpload;
        this.bitmap = bitmap;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public boolean isStartUpload() {
        return startUpload;
    }

    public void setStartUpload(boolean startUpload) {
        this.startUpload = startUpload;
    }
}
