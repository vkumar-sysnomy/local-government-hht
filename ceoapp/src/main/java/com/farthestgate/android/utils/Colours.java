package com.farthestgate.android.utils;

import android.graphics.Color;

import java.util.HashMap;

/**
 * Created by Hanson Aboagye on 19/04/2014.
 */
public class Colours
{
    public static HashMap<String,Integer> colourNames;

    public static Colours newInstance()
    {
        Colours _colours = new Colours();
        HashMap<String,Integer> setColours = new HashMap<String, Integer>();

        setColours.put("Beige",Color.parseColor("#F5F1DE"));
        setColours.put("Black",Color.parseColor("#000000"));
        setColours.put("Blue",Color.parseColor("#0000FF"));
        setColours.put("Bronze",Color.parseColor("#cd7f32"));
        setColours.put("Brown",Color.parseColor("#a52a2a"));
        setColours.put("Burgundy",Color.parseColor("#800020"));
        setColours.put("Cream",Color.parseColor("#fffdd0"));
        setColours.put("Gold",Color.parseColor("#ffd700"));
        setColours.put("Green",Color.parseColor("#00FF00"));
        setColours.put("Grey",Color.parseColor("#a1a1a1"));
        setColours.put("Maroon",Color.parseColor("#800000"));
        setColours.put("Mauve",Color.parseColor("#e0b0ff"));
        setColours.put("Orange",Color.parseColor("#ffa500"));
        setColours.put("Pink",Color.parseColor("#ffc0cb"));
        setColours.put("Purple",Color.parseColor("#800080"));
        setColours.put("Red",Color.parseColor("#FF0000"));
        setColours.put("Silver",Color.parseColor("#c0c0c0"));
        setColours.put("Turquoise",Color.parseColor("#30d5c8"));
        setColours.put("Unspecified",Color.parseColor("#342509"));
        setColours.put("White",Color.parseColor("#FFFFFF"));
        setColours.put("Yellow",Color.parseColor("#ffff00"));

        _colours.colourNames = setColours;


        return _colours;
    }
}
