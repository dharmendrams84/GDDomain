//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.arts;

import java.io.Serializable;
import java.util.Locale;

//------------------------------------------------------------------------------
/**
 * Interface for objects used in searching for Groupe Dynamite tax exempt
 * data.
 * 
 * @author dteagle
 */
//------------------------------------------------------------------------------

public interface GDYNTaxExemptSearchCriteriaIfc extends Serializable
{
    //----------- category code property ---------------------------------------
    String getCategoryCode();
    void setCategoryCode(String aCode);

    //----------- country code property ----------------------------------------
    String getCountryCode();
    void setCountryCode(String aCode);
    
    //----------- image name property ------------------------------------------
    String getIdImageName();
    void setIdImageName(String aValue);
    
    //----------- tax area code property ---------------------------------------
    String getTaxAreaCode();
    void setTaxAreaCode(String aCode);
    
    //----------- locale property ----------------------------------------------
    Locale getLocale();
    void setLocale(Locale aLocale);
    
    //----------- read fully property ------------------------------------------
    boolean isReadFully();
    void setReadFully(boolean aValue);
    
}
