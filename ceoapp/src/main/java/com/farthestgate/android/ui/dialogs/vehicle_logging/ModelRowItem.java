package com.farthestgate.android.ui.dialogs.vehicle_logging;


import com.farthestgate.android.model.VehicleModel;

/**
 * Created by Hanson Aboagye 04/2014
 *
 * THis class allows us to store data about the way this item should be displayed
 * <p/>
 * In differnt modes this class should allow us to draw a different button
 */
public class ModelRowItem extends VehicleModel
{


    public static enum ITEM_MODES {
        HEADER_NOT_PROVIDE,
        SINGLE_VIEW
    }

    private Boolean isUserPreferredModel;
    private ITEM_MODES MODE;


    /*
     * Constructor which converts data to this type of row item
     *
     * could be useful if we need to jump to this type of view
     */
    public ModelRowItem(ITEM_MODES mode, VehicleModel v) {
        modelMakeID = v.modelMakeID;
        modelName = v.modelName;
        MODE = mode;
        setIsUserPreferredModel(true);
    }

    public ModelRowItem(VehicleModel v) {
        modelMakeID = v.modelMakeID;
        modelName = v.modelName;
        MODE = ITEM_MODES.SINGLE_VIEW;
        setIsUserPreferredModel(true);
    }

    public VehicleModel getManufacturer()
    {
        VehicleModel v = new VehicleModel();
        modelMakeID = v.modelMakeID;
        modelName = v.modelName;

        return v;
    };


    public ITEM_MODES getMODE() {
        return MODE;
    }

    public void setMODE(ITEM_MODES MODE) {
        this.MODE = MODE;
    }


    public Boolean getIsUserPreferredModel() {
        return isUserPreferredModel;
    }

    public void setIsUserPreferredModel(Boolean isUserPreferredModel) {
        this.isUserPreferredModel = isUserPreferredModel;
    }

}
