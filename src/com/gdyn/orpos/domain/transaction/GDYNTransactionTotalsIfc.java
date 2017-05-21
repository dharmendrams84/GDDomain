/* ===========================================================================
 *   Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
 *   All rights reserved.
 *   
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED    (MM/DD/YY)
 *    pwai  01/22/15 - this class is added for this GD 559938 - ORPOS Tax Exemption with Retrieved Transaction - incorrect tax amount
 *    
 * ===========================================================================
 */
package com.gdyn.orpos.domain.transaction;

import java.util.Locale;

import com.gdyn.orpos.domain.tax.GDYNTaxInformationContainerIfc;

import oracle.retail.stores.domain.tax.TaxInformationContainerIfc;
import oracle.retail.stores.domain.transaction.TransactionTotalsIfc;

public interface GDYNTransactionTotalsIfc extends TransactionTotalsIfc
{
    
}
