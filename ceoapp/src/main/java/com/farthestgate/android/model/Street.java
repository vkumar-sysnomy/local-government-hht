package com.farthestgate.android.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 21/09/15.
 */
public class Street {
    public String nodeRef;
    public String USRN;
    public String streetName;
    public Boolean removalsAllowed;
    //JK: street Enforcement Patterns
    public List<EnforcementPattern> enforcementPattern = new ArrayList<EnforcementPattern>();
    public String contraventions;
    public String contraJson;
    public int verrusCode;
    public List<WarningNotice> warningNoticeConfiguration = new ArrayList<WarningNotice>();
    public String owningcpz;

    public Street(){
    }
    public Street(StreetCPZ street) {

        USRN =street.streetusrn;
        streetName=street.streetname;
        removalsAllowed =street.removalsallowed;
        enforcementPattern = street.streetEnforcementPattern;
        nodeRef =street.noderef;
        owningcpz = street.owningcpz;
        contraventions =street.contraventions;
        contraJson =street.contraJSON;
        verrusCode =street.verrus_code;
        warningNoticeConfiguration = street.warningNoticeConfiguration;
    }
}
