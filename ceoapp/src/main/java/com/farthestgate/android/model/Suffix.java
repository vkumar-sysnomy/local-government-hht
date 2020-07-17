package com.farthestgate.android.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Administrator on 1/4/2018.
 */

public class Suffix {

    @SerializedName("suffixes")
    @Expose
    public List<ContraventionSuffix> contraventionSuffixes = null;
    @SerializedName("footway-suffixes")
    @Expose
    public List<FootwaySuffix> footwaySuffixes = null;
}
