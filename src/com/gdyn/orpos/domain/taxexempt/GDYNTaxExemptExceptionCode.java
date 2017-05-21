//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.taxexempt;

import java.io.Serializable;

import oracle.retail.stores.common.utility._360DateIfc;


//------------------------------------------------------------------------------
/**
 * Domain object for Groupe Dynamite tax exempt exception codes.
 * @author dteagle
 */
//------------------------------------------------------------------------------
public class GDYNTaxExemptExceptionCode implements Serializable
{
    /** serial UID */
    private static final long serialVersionUID = -6514860340507462069L;
    
    private String countryCode;
    private String taxAreaCode; 
    private String categoryCode;
    private String imageCode;
    private Integer taxProductCode;
    private _360DateIfc effectiveDate;
    
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
     * @return the tax area code
     */
    public String getTaxAreaCode()
    {
        return taxAreaCode;
    }
    
    //--------------------------------------------------------------------------
    /**
     * @param aValue the tax area code to set
     */
    public void setTaxAreaCode(String aValue)
    {
        taxAreaCode = aValue;
    }

    //--------------------------------------------------------------------------
    /**
     * @return the categoryCode
     */
    public String getCategoryCode()
    {
        return categoryCode;
    }
    
    //--------------------------------------------------------------------------
    /**
     * @param categoryCode the categoryCode to set
     */
    public void setCategoryCode(String categoryCode)
    {
        this.categoryCode = categoryCode;
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
     * @return the imageCode
     */
    public String getImageCode()
    {
        return imageCode;
    }

    //--------------------------------------------------------------------------
    /**
     * @param aValue the imageCode to set
     */
    public void setImageCode(String aValue)
    {
        imageCode = aValue;
    }

    //--------------------------------------------------------------------------
    /**
     * @return the taxProductCode
     */
    public Integer getTaxProductCode()
    {
        return taxProductCode;
    }

    //--------------------------------------------------------------------------
    /**
     * @param aValue the taxProductCode to set
     */
    public void setTaxProductCode(Integer aValue)
    {
        taxProductCode = aValue;
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
     * Makes a clone of this object.
     * @return the clone
     */
    public Object clone()
    {
        GDYNTaxExemptExceptionCode myClone = new GDYNTaxExemptExceptionCode();
        setCloneAttributes(myClone);
        return myClone;
    }
    
    //--------------------------------------------------------------------------
    /**
     * Copies this object's attributes into a cloned version.
     * @param newClass
     */
    protected void setCloneAttributes(GDYNTaxExemptExceptionCode newClass)
    {
        newClass.setCategoryCode(categoryCode);
        newClass.setCountryCode(countryCode);
        newClass.setTaxAreaCode(taxAreaCode); 
        newClass.setImageCode(imageCode);
        newClass.setTaxProductCode(taxProductCode);
        newClass.setEffectiveDate(effectiveDate);
    }
}
