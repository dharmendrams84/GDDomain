//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.lineitem;

import oracle.retail.stores.domain.lineitem.ItemContainerProxyIfc;

import com.gdyn.orpos.domain.transaction.GDYNTransactionTaxIfc;

//------------------------------------------------------------------------------
/**
 * Extended to add Groupe Dynamite customer tax exemption.
 * @author dteagle
 */
//------------------------------------------------------------------------------

public interface GDYNItemContainerProxyIfc extends ItemContainerProxyIfc
{
    //--------------------------------------------------------------------------
    /**
     * Applies a GDYN customer tax exemption.
     * @param newTax a tax object to extract data from
     */
    void setCustomerTaxExempt(GDYNTransactionTaxIfc newTax);

    
    //--------------------------------------------------------------------------
    /**
     * Removes a customer tax exemption.
     */
    void clearCustomerTaxExempt();
}