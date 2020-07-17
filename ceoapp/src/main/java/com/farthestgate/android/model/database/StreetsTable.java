package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.farthestgate.android.model.StreetCPZ;

/**
 * Created by Hanson Aboagye on 19/04/2014.
 */
@Table(name = "StreetsTable")
public class StreetsTable extends Model
{

    public static final String COL_STREET_NODEREF = "NodeRef";
    public static final String COL_STREET_USRN = "USRN";
    public static final String COL_STREET_NAME = "StreetName";
    public static final String COL_STREET_CONTRA = "Contraventions";
    public static final String COL_STREET_REMOVALS = "RemovalsAllowed";
    public static final String COL_STREET_ENFORCEMENTID = "EnforcementPatternID";   // pointer to pattern table
    public static final String COL_STREET_CONTRA_JSON = "ContraventionsJSON";
    public static final String COL_STREET_VERRUS_CODE = "VerrusCode";


    @Column(name = COL_STREET_NODEREF)
    private String nodeRef;

    @Column(name = COL_STREET_USRN)
    private String USRN;

    @Column(name = COL_STREET_NAME)
    private String streetName;

    @Column(name = COL_STREET_REMOVALS)
    private Boolean removalsAllowed;

    @Column(name = COL_STREET_ENFORCEMENTID)
    private int enforcementPattern;


    @Column(name = COL_STREET_CONTRA)
    private String contraventions;


    @Column(name = COL_STREET_CONTRA_JSON)
    private String contraJson;

    @Column(name = COL_STREET_VERRUS_CODE)
    private int verrusCode;

    /**
     * Creates the table
     */
    public StreetsTable(){
        super();
    }

    public StreetsTable(StreetCPZ street) {
        super();
        setUSRN(street.streetusrn);
        setStreetName(street.streetname);
        setRemovalsAllowed(street.removalsallowed);
        setEnforcementPattern(1);  //TODO: add pattern tables and mechanism for adding patterns
        setNodeRef(street.noderef);
        setContraventions(street.contraventions);
        setContraJson(street.contraJSON);
        setVerrusCode(street.verrus_code);
    }
    public String getStreetName()
    {
        return streetName;
    }

    public void setStreetName(String streetName)
    {
        this.streetName = streetName;
    }

    public String getUSRN()
    {
        return USRN;
    }

    public void setUSRN(String USRN)
    {
        this.USRN = USRN;
    }

    public Boolean getRemovalsAllowed()
    {
        return removalsAllowed;
    }

    public void setRemovalsAllowed(Boolean removalsAllowed)
    {
        this.removalsAllowed = removalsAllowed;
    }

    public int getEnforcementPattern()
    {
        return enforcementPattern;
    }

    public void setEnforcementPattern(int enforcementPattern)
    {
        this.enforcementPattern = enforcementPattern;
    }


    public String getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(String nodeRef) {
        this.nodeRef = nodeRef;
    }


    public String getContraventions() {
        return contraventions;
    }

    public void setContraventions(String contraventions) {
        this.contraventions = contraventions;
    }

    public String getContraJson() {
        return contraJson;
    }

    public void setContraJson(String contraJson) {
        this.contraJson = contraJson;
    }

    public int getVerrusCode() {
        return verrusCode;
    }

    public void setVerrusCode(int verrusCode) {
        this.verrusCode = verrusCode;
    }
}
