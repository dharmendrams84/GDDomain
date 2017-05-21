//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.taxexempt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import oracle.retail.stores.common.utility._360DateIfc;

//------------------------------------------------------------------------------
/**
 * Domain object for Groupe Dynamite tax exempt id images.
 * @author dteagle
 */
//------------------------------------------------------------------------------
public class GDYNTaxExemptIdImage implements Serializable
{
    /** serial UID */
    private static final long serialVersionUID = 4840161795262732638L;
    
    private String taxIdImageName;
    private String countryCode;  
    private _360DateIfc effectiveDate;
    
    private List<GDYNTaxExemptExceptionCode> exceptionCodes;

    //--------------------------------------------------------------------------
    /**
     * @return the taxIdImageName
     */
    public String getTaxIdImageName()
    {
        return taxIdImageName;
    }
    
    //--------------------------------------------------------------------------
    /**
     * @param aValue the taxIdImageName to set
     */
    public void setTaxIdImageName(String aValue)
    {
        taxIdImageName = aValue;
    }
    
    //--------------------------------------------------------------------------
    /**
     * @return the country code
     */
    public String getCountryCode()
    {
        return countryCode;
    }
    
    //--------------------------------------------------------------------------
    /**
     * @param aValue the country code to set
     */
    public void setCountryCode(String aValue)
    {
        countryCode = aValue;
    }
    
    //--------------------------------------------------------------------------
    /**
     * @return the effectiveDate
     */
    public _360DateIfc getEffectiveDate()
    {
        return effectiveDate;
    }
    
    //--------------------------------------------------------------------------
    /**
     * @param aValue the effectiveDate to set
     */
    public void setEffectiveDate(_360DateIfc aValue)
    {
        effectiveDate = aValue;
    } 
    
    //--------------------------------------------------------------------------
    /**
     * @return the exceptionCodes
     */
    public List<GDYNTaxExemptExceptionCode> getExceptionCodes()
    {
        return exceptionCodes;
    }

    //--------------------------------------------------------------------------
    /**
     * @param aList the list of exceptionCodes to set
     */
    public void setExceptionCodes(List<GDYNTaxExemptExceptionCode> aList)
    {
        exceptionCodes = aList;
    }
    
    //--------------------------------------------------------------------------
    /**
     * Makes a clone of this object.
     * @return the clone
     */
    public Object clone()
    {
        GDYNTaxExemptIdImage myClone = new GDYNTaxExemptIdImage();
        setCloneAttributes(myClone);
        return myClone;
    }
    
    //--------------------------------------------------------------------------
    /**
     * Copies this object's attributes into a cloned version.
     * @param newClass
     */
    protected void setCloneAttributes(GDYNTaxExemptIdImage newClass)
    {
        newClass.setTaxIdImageName(taxIdImageName);
        newClass.setCountryCode(countryCode);
        newClass.setEffectiveDate(effectiveDate);
        
        if(exceptionCodes != null && !exceptionCodes.isEmpty())
        {
            List<GDYNTaxExemptExceptionCode> newList = 
                new ArrayList<GDYNTaxExemptExceptionCode>();
            
            for(GDYNTaxExemptExceptionCode myCode : exceptionCodes)
            {
                newList.add((GDYNTaxExemptExceptionCode)myCode.clone());
            }
            newClass.setExceptionCodes(newList);
        }
    }
}
