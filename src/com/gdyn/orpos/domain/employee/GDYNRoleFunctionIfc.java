//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.employee;

import oracle.retail.stores.domain.employee.RoleFunctionIfc;

/**
 * Extending RoleFunctionIfc to add new ones for Groupe Dynamite
 * - Non-Retrieved Return
 * - Employee Transaction Percent Discount
 * - Debit Reversal Retry Cancel
 * 
 * @author mlawrence
 *
 */
public interface GDYNRoleFunctionIfc extends RoleFunctionIfc
{
    // When adding a new security role we need to add one to this number.
    public static final int MAXIMUM_ACS_ID              		= 189;
    
    public static final int NON_RETRIEVED_RETURN        		= 185;
    public static final int EMPLOYEE_TRANSACTIONS       		= 186;
    public static final int RETRY_CANCEL_DEBIT_REVERSAL 		= 187;
    public static final int CREDIT_SETTLEMENT_RETRY_CONTNIUE 	= 188;
    public static final int MODIFY_STORE_OPENING_DATE           = 189;
}
