//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.transaction;

import java.util.List;

import oracle.retail.stores.common.utility._360DateIfc;
import oracle.retail.stores.domain.transaction.TransactionTax;

import com.gdyn.orpos.domain.tax.GDYNTaxConstantsIfc;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptCustomerCategory;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptCustomerCode;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptExceptionCode;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptIdImage;

//------------------------------------------------------------------------------
/**
 * Extends the ORPOS TransactionTax to add Groupe Dynamite customer tax
 * exempt data.
 * 
 * @author dteagle
 */
//------------------------------------------------------------------------------

public class GDYNTransactionTax extends TransactionTax 
                                implements GDYNTransactionTaxIfc
{
    /** serial UID */
    private static final long serialVersionUID = 1478640587923343330L;

    private GDYNTaxExemptCustomerCategory customerCategory;

    private _360DateIfc idExpirationDate;
    private String bandRegistryId;
    private GDYNTaxExemptIdImage taxExemptIdImage;
      
    //--------------------------------------------------------------------------
    /** 
     * Clears a customer tax exemption.
     */
    public void clearCustomerTaxExemption()
    {
        if (taxMode == GDYNTaxConstantsIfc.TAX_MODE_EXEMPT ||
            taxMode == GDYNTaxConstantsIfc.TAX_MODE_PARTIAL_EXEMPT)
        {
            resetStandardTax();
            
            setCustomerCategory(null);
            setIdExpirationDate(null);
            setBandRegistryId(null);
            setTaxExemptIdImage(null);     
        }
    }
    
    //--------------------------------------------------------------------------
    /** 
     * @return the customer category 
     */
    public GDYNTaxExemptCustomerCategory getCustomerCategory()
    {
        return customerCategory;
    }
    /** 
     * @param aCategory the customer category to set. 
     */
    public void setCustomerCategory(GDYNTaxExemptCustomerCategory aCategory)
    {
        customerCategory = aCategory;
    }

    //--------------------------------------------------------------------------
    /** 
     * @return the customer code 
     */
    public GDYNTaxExemptCustomerCode getCustomerCode()
    {
        if(customerCategory != null) 
        {
            return customerCategory.getCustomerCode();
        }
        return null;
    }

    //--------------------------------------------------------------------------
    /** 
     * @return the customer tax exempt exceptions 
     */
    public List<GDYNTaxExemptExceptionCode> getCustomerExceptions()
    {
        if(customerCategory != null)
        {
            return customerCategory.getCustomerCode().getExceptionCodes();
        }
        return null;
    }
    
    //--------------------------------------------------------------------------
    /** 
     * @return the tax exempt id expiration date 
     */
    public _360DateIfc getIdExpirationDate()
    {
        return idExpirationDate;
    }
    /** 
     * @param aDate the tax exempt id expiration date to set. 
     */
    public void setIdExpirationDate(_360DateIfc aDate)
    {
        idExpirationDate = aDate;
    }

    //--------------------------------------------------------------------------
    /** 
     * @return the customer band or registry id 
     */
    public String getBandRegistryId()
    {
        return bandRegistryId;
    }
    /** 
     * @param anId the customer band or registry id to set 
     */
    public void setBandRegistryId(String anId)
    {
        bandRegistryId = anId;
    }

    //--------------------------------------------------------------------------
    /** 
     * @return the name of the tax id image 
     */
    public String getTaxExemptIdImageName()
    {
        if(taxExemptIdImage != null)
        {
            return taxExemptIdImage.getTaxIdImageName();
        }
        return null;
    }
    /** 
     * @return the tax id image 
     */
    public GDYNTaxExemptIdImage getTaxExemptIdImage()
    {
        return taxExemptIdImage;
    }
    /** 
     * @param aCode the id image tax exempt code to set 
     */
    public void setTaxExemptIdImage(GDYNTaxExemptIdImage image)
    {
        taxExemptIdImage = image;
    }
    
    //--------------------------------------------------------------------------
    /** 
     * Checks to see if this tax has a customer tax exemption applied.
     * @return true if exemption, false if not
     */
    public boolean hasCustomerTaxExemption()
    {
        if((getTaxMode() == GDYNTaxConstantsIfc.TAX_MODE_EXEMPT ||
            getTaxMode() == GDYNTaxConstantsIfc.TAX_MODE_PARTIAL_EXEMPT) &&
           getReason() != null)
        {
            return true;
        }
        return false;
    }

    //--------------------------------------------------------------------------
    /**
     * Makes a clone of this object.
     * @return the clone
     */
    public Object clone()
    {
        GDYNTransactionTax myClone = new GDYNTransactionTax();
        setCloneAttributes(myClone);
        return myClone;
    }
    
    //--------------------------------------------------------------------------
    /**
     * Copies this object's attributes into a cloned version.
     * @param newClass
     */
    protected void setCloneAttributes(GDYNTransactionTax newClass)
    {
        super.setCloneAttributes(newClass);
        
        newClass.setCustomerCategory(this.customerCategory);
        newClass.setIdExpirationDate(this.idExpirationDate);
        newClass.setBandRegistryId(this.bandRegistryId);
        newClass.setTaxExemptIdImage(this.taxExemptIdImage);
    }
}
