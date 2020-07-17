package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Hanson on 06/05/2014.
 */
@Table(name = "VehicleMakeTable")
public class VehicleMakeTable extends Model {

    public static final String COL_VEHICLE_NAME = "VehicleMakeName";
    public static final String COL_VEHICLE_ID = "VehicleMakeId";

    @Column(name = COL_VEHICLE_NAME )
    public String vehicleMakeName;

    @Column(name = COL_VEHICLE_ID)
    public Integer vehicleMakeID;
}
