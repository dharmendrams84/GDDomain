// ------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
// ------------------------------------------------------------------------------

package com.gdyn.orpos.domain.lineitem;

import java.util.List;

import oracle.retail.stores.domain.lineitem.ItemTaxIfc;
import oracle.retail.stores.domain.tax.NewTaxRuleIfc;

import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptCustomerCode;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptExceptionCode;

//------------------------------------------------------------------------------
/**
 * Extends ItemTaxIfc to account for GDYN tax exemptions.
 * @author dteagle
 */
//------------------------------------------------------------------------------

public interface GDYNItemTaxIfc extends ItemTaxIfc
{
    //--------------------------------------------------------------------------
    /**
     * Returns true if this item's tax product code is in the list of
     * exceptions.
     * @return true if exception, false if not
     */
    public abstract boolean isException();

    //--------------------------------------------------------------------------
    /**
     * Stores the tax rules retrieved from the database during the PLU
     * lookup.
     * @param rules the PLU tax rules
     */
    public abstract void setLookupTaxRules(NewTaxRuleIfc[] rules);

    //--------------------------------------------------------------------------
    /**
     * Creates a map from the list of tax exempt exception codes.
     * @param codes the exception codes
     */
    public abstract void applyExceptionCodes(List<GDYNTaxExemptExceptionCode> codes);

    //--------------------------------------------------------------------------
    /**
     * Sets the tax product code from the ADP tax data in the item.
     * @param aCode the tax product code
     */
    public abstract void setTaxProductCode(Integer aCode);

    //--------------------------------------------------------------------------
    /**
     * @return the taxExemptCustomerCode
     */
    public abstract GDYNTaxExemptCustomerCode getTaxExemptCustomerCode();

    //--------------------------------------------------------------------------
    /**
     * @param taxExemptCustomerCode the taxExemptCustomerCode to set
     */
    public abstract void setTaxExemptCustomerCode(GDYNTaxExemptCustomerCode aCode);
    
    //--------------------------------------------------------------------------
    /**
     * Clears the customer tax exempt data from the item tax object.
     */
    public abstract void clearCustomerTaxExemption();

}