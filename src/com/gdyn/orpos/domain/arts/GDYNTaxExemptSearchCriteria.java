//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.arts;

import java.util.Locale;

//------------------------------------------------------------------------------
/**
 * Search criteria object for Groupe Dynamite tax exemption data.
 * 
 * @author dteagle
 */
// ------------------------------------------------------------------------------

public class GDYNTaxExemptSearchCriteria implements GDYNTaxExemptSearchCriteriaIfc
{
    /** serial UID */
    private static final long serialVersionUID = 4275393210585236695L;

    /** the category code to search for */
    private String categoryCode;

    /** the country code to search by */
    private String countryCode;

    /** the id image name to search by */
    private String idImageName;

    /** the tax area code to search by */
    private String taxAreaCode;

    /** the locale to search by */
    private Locale locale;

    /** flag indicating whether or not to read all child objects */
    private boolean readFully;

    // ----------- category code property ---------------------------------------
    public String getCategoryCode()
    {
        return categoryCode;
    }

    public void setCategoryCode(String aCode)
    {
        categoryCode = aCode;
    }

    // ----------- country code property ----------------------------------------
    public String getCountryCode()
    {
        return countryCode;
    }

    public void setCountryCode(String aCode)
    {
        countryCode = aCode;
    }

    // ----------- image name property ------------------------------------------
    public String getIdImageName()
    {
        return idImageName;
    }

    public void setIdImageName(String aValue)
    {
        idImageName = aValue;
    }

    // ----------- tax area code property ---------------------------------------
    public String getTaxAreaCode()
    {
        return taxAreaCode;
    }

    public void setTaxAreaCode(String aCode)
    {
        taxAreaCode = aCode;
    }

    // ----------- locale property ----------------------------------------------
    public Locale getLocale()
    {
        return locale;
    }

    public void setLocale(Locale aLocale)
    {
        locale = aLocale;
    }

    // ----------- read fully property ------------------------------------------
    public boolean isReadFully()
    {
        return readFully;
    }

    public void setReadFully(boolean aValue)
    {
        readFully = aValue;
    }
}
