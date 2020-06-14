package com.wong.imageupload;

public class ItemBean {
    private boolean isButton; // 是否是添加图片的按钮
    private ImageFileBean imageFileBean;

    public ItemBean(){}
    public ItemBean(ImageFileBean bean,boolean isButton){
        this.imageFileBean = bean;
        this.isButton = isButton;
    }

    public ImageFileBean getImageFileBean() {
        return imageFileBean;
    }

    public void setImageFileBean(ImageFileBean imageFileBean) {
        this.imageFileBean = imageFileBean;
    }

    public boolean isButton() {
        return isButton;
    }

    public void setButton(boolean button) {
        isButton = button;
    }
}
