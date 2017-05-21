//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.tax;

import oracle.retail.stores.domain.tax.TaxConstantsIfc;

//------------------------------------------------------------------------------
/**
 * Extended to add Groupe Dynamite tax exempt constants.
 * @author dteagle
 */
//------------------------------------------------------------------------------

public interface GDYNTaxConstantsIfc extends TaxConstantsIfc
{
    // reusing the constant for TAX_MODE_OVERRIDE_RATE to 
    // get around issue of hard-coded tax indicator array in 
    // TaxConstantsIfc
    public static final int TAX_MODE_PARTIAL_EXEMPT = 3;
    
    public static final String EXEMPT_FULL = "full";
    public static final String EXEMPT_PARTIAL = "partial";
    public static final String TAX_RULE_NAME_CONTAINS = "GST";
}
