package com.farthestgate.android.ui.pcn;

import com.farthestgate.android.model.Contravention;

import java.util.List;

public class PCNUtils {

    private PCNUtils() {};


    public static int getPosition(String contraventionCode, List<Contravention> contraventionDataList)
    {
        for(int pos= 0; pos<contraventionDataList.size(); pos++){
            if(contraventionDataList.get(pos).contraventionCode.equalsIgnoreCase(contraventionCode)){
                return pos;
            }
        }
        return -1;
    }
}
