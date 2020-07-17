package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Hanson Aboagye on 21/04/2014.
 */
@Table(name = "ContraventionsTable")
public class ContraventionsTable extends Model
{
    public static final String COL_CONTRAVENTION_CODE = "ContraventionCode";
    public static final String COL_AGREEMENT_CODE = "AgreementCode";
    public static final String COL_CONTRAVENTION_DESC = "ContraventionDesc";
    public static final String COL_CONTRAVENTION_ENFORCEMENT_TYPE = "EnforcementType";
    public static final String COL_CONTRAVENTION_SUFFIXES = "Suffixes";

    @Column(name = COL_CONTRAVENTION_CODE)
    private String contraventionCode;

    @Column(name = COL_CONTRAVENTION_DESC)
    private String contraventionDescription;

    @Column(name = COL_CONTRAVENTION_ENFORCEMENT_TYPE)
    private Integer enforcementType;

    @Column(name = COL_CONTRAVENTION_SUFFIXES)
    private String Suffixes;

    @Column(name = COL_AGREEMENT_CODE)
    private String AgreementCode;

    public String getContraventionCode()
    {
        return contraventionCode;
    }

    public void setContraventionCode(String contraventionCode)
    {
        this.contraventionCode = contraventionCode;
    }

    public String getContraventionDescription()
    {
        return contraventionDescription;
    }

    public void setContraventionDescription(String contraventionDescription)
    {
        this.contraventionDescription = contraventionDescription;
    }

    public Integer getEnforcementType()
    {
        return enforcementType;
    }

    public void setEnforcementType(Integer enforcementType)
    {
        this.enforcementType = enforcementType;
    }

    public String getSuffixes()
    {
        return Suffixes;
    }

    public void setSuffixes(String suffixes)
    {
        Suffixes = suffixes;
    }

    public String getAgreementCode() {
        return AgreementCode;
    }

    public void setAgreementCode(String agreementCode) {
        this.AgreementCode = agreementCode;
    }
}
