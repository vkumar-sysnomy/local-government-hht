package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Hanson on 06/05/2014.
 */
@Table(name = "VehicleModelTable")
public class VehicleModelTable extends Model {


    public static final String COL_MODEL_NAME = "VehicleModelName";
    public static final String COL_MODEL_ID = "ModelMakeID";

    @Column(name = COL_MODEL_ID)
    public Integer modelMakeID;

    @Column(name = COL_MODEL_NAME)
    public String modelName;
}
