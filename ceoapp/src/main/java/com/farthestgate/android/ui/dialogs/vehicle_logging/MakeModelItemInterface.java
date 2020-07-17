package com.farthestgate.android.ui.dialogs.vehicle_logging;

/**
 * Created by Hanson on 11/02/14.
 */
public interface MakeModelItemInterface
{
    interface OnMakeModelsRowItemClick {
        public void OnItemClick(MakeRowItem item);

        public void OnCheckBoxClick();

        public void OnDeleteClick();
    }
}
