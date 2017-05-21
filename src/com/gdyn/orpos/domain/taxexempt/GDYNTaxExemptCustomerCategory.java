//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.taxexempt;

import java.io.Serializable;
import java.util.Locale;

import oracle.retail.stores.common.utility.LocalizedTextIfc;

//------------------------------------------------------------------------------
/**
 * Domain object for Groupe Dynamite tax exempt customer categories.
 * 
 * @author dteagle
 */
// ------------------------------------------------------------------------------
public class GDYNTaxExemptCustomerCategory implements Serializable
{
    /** serial UID */
    private static final long serialVersionUID = -6034842617126039733L;
    
    private String categoryCode;
    private String countryCode;
    private String taxAreaCode;
    private Integer categoryMessageId;
    private LocalizedTextIfc categoryName;
    private boolean active;
    private boolean expirationDateRequired;
    private boolean bandRegistryRequired;
    private boolean taxIdImageRequired;
    private LocalizedTextIfc receiptMessage;

    private GDYNTaxExemptCustomerCode customerCode;

    // --------------------------------------------------------------------------
    /**
     * @return the categoryCode
     */
    public String getCategoryCode()
    {
        return categoryCode;
    }

    // --------------------------------------------------------------------------
    /**
     * @param categoryCode
     *            the categoryCode to set
     */
    public void setCategoryCode(String categoryCode)
    {
        this.categoryCode = categoryCode;
    }

    // --------------------------------------------------------------------------
    /**
     * @return the country code
     */
    public String getCountryCode()
    {
        return countryCode;
    }

    // --------------------------------------------------------------------------
    /**
     * @param aValue
     *            the country code to set
     */
    public void setCountryCode(String aValue)
    {
        countryCode = aValue;
    }

    // --------------------------------------------------------------------------
    /**
     * @return the tax area code
     */
    public String getTaxAreaCode()
    {
        return taxAreaCode;
    }

    // --------------------------------------------------------------------------
    /**
     * @param aValue
     *            the tax area code to set
     */
    public void setTaxAreaCode(String aValue)
    {
        taxAreaCode = aValue;
    }

    // --------------------------------------------------------------------------
    /**
     * @return the categoryMessageId
     */
    public Integer getCategoryMessageId()
    {
        return categoryMessageId;
    }

    // --------------------------------------------------------------------------
    /**
     * @param categoryMessageId
     *            the categoryMessageId to set
     */
    public void setCategoryMessageId(Integer anId)
    {
        categoryMessageId = anId;
    }
    
    // --------------------------------------------------------------------------
    /**
     * @return the categoryName
     */
    public String getCategoryName()
    {
        return categoryName.getText();
    }

    //--------------------------------------------------------------------------
    /**
     * @param locale
     * @return
     */
    public String getCategoryName(Locale locale)
    {
        return categoryName.getText(locale);
    }
    // --------------------------------------------------------------------------
    /**
     * @param categoryName
     *            the categoryName to set
     */
    public void setCategoryName(LocalizedTextIfc localizedText)
    {
        categoryName = localizedText;
    }

    // --------------------------------------------------------------------------
    /**
     * @return the active flag
     */
    public boolean isActive()
    {
        return active;
    }

    // --------------------------------------------------------------------------
    /**
     * @param aValue
     *            the active flag to set
     */
    public void setActive(boolean aValue)
    {
        active = aValue;
    }

    // --------------------------------------------------------------------------
    /**
     * @return the expiration Date Required flag
     */
    public boolean isExpirationDateRequired()
    {
        return expirationDateRequired;
    }

    // --------------------------------------------------------------------------
    /**
     * @param aValue
     *            the expiration date required flag to set
     */
    public void setExpirationDateRequired(boolean aValue)
    {
        expirationDateRequired = aValue;
    }

    // --------------------------------------------------------------------------
    /**
     * @return the band or registry id required flag
     */
    public boolean isBandRegistryRequired()
    {
        return bandRegistryRequired;
    }

    // --------------------------------------------------------------------------
    /**
     * @param aValue
     *            the band or registry id required flag to set
     */
    public void setBandRegistryRequired(boolean aValue)
    {
        bandRegistryRequired = aValue;
    }

    //--------------------------------------------------------------------------
    /**
     * @return the tax id image required flag
     */
    public boolean isTaxIdImageRequired()
    {
        return taxIdImageRequired;
    }

    //--------------------------------------------------------------------------
    /**
     * @param aValue
     *            the tax id image required flag to set
     */
    public void setTaxIdImageRequired(boolean aValue)
    {
        taxIdImageRequired = aValue;
    }

    //--------------------------------------------------------------------------
    /**
     * @return the localized receipt message
     */
    public String getReceiptMessage()
    {
        return receiptMessage.getText();
    }
    
    //--------------------------------------------------------------------------
    /**
     * @param locale
     * @return
     */
    public String getReceiptMessage(Locale locale)
    {
        return receiptMessage.getText(locale);
    }

    // --------------------------------------------------------------------------
    /**
     * @param aValue
     *            the receipt message to set
     */
    public void setReceiptMessage(LocalizedTextIfc localizedText)
    {
        receiptMessage = localizedText;
    }

    // --------------------------------------------------------------------------
    /**
     * @return the customerCode
     */
    public GDYNTaxExemptCustomerCode getCustomerCode()
    {
        return customerCode;
    }

    // --------------------------------------------------------------------------
    /**
     * @param aCode
     *            the customerCode to set
     */
    public void setCustomerCode(GDYNTaxExemptCustomerCode aCode)
    {
        customerCode = aCode;
    }

    // --------------------------------------------------------------------------
    /**
     * Makes a clone of this object.
     * 
     * @return the clone
     */
    public Object clone()
    {
        GDYNTaxExemptCustomerCategory myClone = new GDYNTaxExemptCustomerCategory();
        setCloneAttributes(myClone);
        return myClone;
    }

    //--------------------------------------------------------------------------
    /**
     * Copies this object's attributes into a cloned version.
     * 
     * @param newClass
     */
    protected void setCloneAttributes(GDYNTaxExemptCustomerCategory newClass)
    {
        newClass.setCategoryCode(categoryCode);
        newClass.setCountryCode(countryCode);
        newClass.setTaxAreaCode(taxAreaCode);
        newClass.setCategoryMessageId(categoryMessageId);
        newClass.setCategoryName((LocalizedTextIfc)categoryName.clone());
        newClass.setActive(active);
        newClass.setExpirationDateRequired(expirationDateRequired);
        newClass.setBandRegistryRequired(bandRegistryRequired);
        newClass.setTaxIdImageRequired(taxIdImageRequired);
        newClass.setReceiptMessage((LocalizedTextIfc)receiptMessage.clone());

        newClass.setCustomerCode((GDYNTaxExemptCustomerCode)customerCode.clone());
    }
}
