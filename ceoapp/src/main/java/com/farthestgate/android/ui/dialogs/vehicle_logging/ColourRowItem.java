package com.farthestgate.android.ui.dialogs.vehicle_logging;

/**
 * Created by Hanson Aboagye 04/2014
 *
 * THis class allows us to store data about the way this item should be displayed
 * <p/>
 * In differnt modes this class should allow us to draw a different button
 */
public class ColourRowItem
{
    /*
     * Constructor which converts data to this type of row item
     *
     * could be useful if we need to jump to this type of view
     */

    private String colourName;
    private int colourValue;

    public ColourRowItem() {

    }


    public ColourRowItem(String colourV)
    {
        colourName = colourV;
    }


    public String getColourName()
    {
        return colourName;
    }

    public void setColourName(String colourName)
    {
        this.colourName = colourName;
    }

    public int getColourValue()
    {
        return colourValue;
    }

    public void setColourValue(int colourValue)
    {
        this.colourValue = colourValue;
    }
}
