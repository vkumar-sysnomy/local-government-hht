package com.farthestgate.android.ui.dialogs.vehicle_logging;

import com.farthestgate.android.model.VehicleManufacturer;

/**
 * Created by Hanson Aboagye 04/2014
 *
 * THis class allows us to store data about the way this item should be displayed
 * <p/>
 * In differnt modes this class should allow us to draw a different button
 */
public class MakeRowItem extends VehicleManufacturer
{


    public static enum ITEM_MODES {
        HEADER_PROVIDE,
        HEADER_NOT_PROVIDE,
        SINGLE_VIEW,
        EXPAND_VIEW
    }

    private Boolean isUserPreferredManufacturer;
    private ITEM_MODES MODE;


    /*
     * Constructor which converts data to this type of row item
     *
     * could be useful if we need to jump to this type of view
     */
    public MakeRowItem(ITEM_MODES mode, VehicleManufacturer v) {
        id = v.id;
        name = v.name;
        MODE = mode;
        setIsUserPreferredManufacturer(true);
    }

    public MakeRowItem(VehicleManufacturer v) {
        id = v.id;
        name = v.name;
        MODE = ITEM_MODES.EXPAND_VIEW;
        setIsUserPreferredManufacturer(true);
    }

    public VehicleManufacturer getManufacturer()
    {
        VehicleManufacturer v = new VehicleManufacturer();
        v.name = this.name;
        v.id = this.id;

        return v;
    };


    public ITEM_MODES getMODE() {
        return MODE;
    }

    public void setMODE(ITEM_MODES MODE) {
        this.MODE = MODE;
    }


    public Boolean getIsUserPreferredManufacturer() {
        return isUserPreferredManufacturer;
    }

    public void setIsUserPreferredManufacturer(Boolean isUserPreferredManufacturer) {
        this.isUserPreferredManufacturer = isUserPreferredManufacturer;
    }

}
