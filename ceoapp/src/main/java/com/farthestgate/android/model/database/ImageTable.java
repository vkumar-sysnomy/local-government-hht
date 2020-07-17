package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "ImageTable")
public class ImageTable extends Model {

    //public static final String COL_PARAM_ID = "PARAM_ID";
    public static final String COL_IMAGE_NAME = "IMG_NAME";
    public static final String COL_RETRY_COUNT = "RETRY_COUNT";

/*
    @Column(name = COL_PARAM_ID)
    private String paramID;*/

    @Column(name = COL_IMAGE_NAME)
    private String imageName;

    @Column(name = COL_RETRY_COUNT)
    private int retryCount;

    public ImageTable(){ super(); }

   /* public String getImgParamId() {
        return paramID;
    }

    public void setImgParamId(String paramId) {
        this.paramID = paramId;
    }*/

    public String getImageName(){
        return imageName;
    }

    public void setImageName(String imageName)
    {
        this.imageName = imageName;
    }

    public int getRetryCount()
    {
        return retryCount;
    }

    public void setRetryCount(int retryCount)
    {
        this.retryCount = retryCount;
    }




}
