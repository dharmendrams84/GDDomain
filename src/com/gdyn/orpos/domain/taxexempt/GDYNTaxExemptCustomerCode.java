//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.taxexempt;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import oracle.retail.stores.common.utility._360DateIfc;

//------------------------------------------------------------------------------
/**
 * Domain object for Groupe Dynamite tax exempt customer codes.
 * @author dteagle
 */
//------------------------------------------------------------------------------
public class GDYNTaxExemptCustomerCode implements Serializable
{
    /** serial UID */
    private static final long serialVersionUID = 4318059096030332552L;
    
    private String categoryCode;
    private String countryCode;
    private String taxAreaCode;   
    private _360DateIfc effectiveDate;
    private String applicationMethod;
    private BigDecimal partialTaxRate;
    
    private List<GDYNTaxExemptExceptionCode> exceptionCodes;

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
     * @return the applicationMethod
     */
    public String getApplicationMethod()
    {
        return applicationMethod;
    }
    
    //--------------------------------------------------------------------------
    /**
     * @param aValue the applicationMethod to set
     */
    public void setApplicationMethod(String aValue)
    {
        applicationMethod = aValue;
    }

    //--------------------------------------------------------------------------
    /**
     * @return the partialTaxRate
     */
    public BigDecimal getPartialTaxRate()
    {
        return partialTaxRate;
    }

    //--------------------------------------------------------------------------
    /**
     * @param aRate the partialTaxRate to set
     */
    public void setPartialTaxRate(BigDecimal aRate)
    {
        partialTaxRate = aRate;
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
        GDYNTaxExemptCustomerCode myClone = new GDYNTaxExemptCustomerCode();
        setCloneAttributes(myClone);
        return myClone;
    }
    
    //--------------------------------------------------------------------------
    /**
     * Copies this object's attributes into a cloned version.
     * @param newClass
     */
    protected void setCloneAttributes(GDYNTaxExemptCustomerCode newClass)
    {
        newClass.setCategoryCode(categoryCode);
        newClass.setCountryCode(countryCode);
        newClass.setTaxAreaCode(taxAreaCode);   
        newClass.setEffectiveDate(effectiveDate);
        newClass.setApplicationMethod(applicationMethod);
        newClass.setPartialTaxRate(partialTaxRate);
        
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
