//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.manager.payment;

import oracle.retail.stores.domain.manager.payment.AuthorizeTransferResponseIfc;

/**
 * Retrieves the response information process by the Authorization Payment Foundation.
 * Carries the information to the client for interrogation/processing.
 * 
 * @author mlawrence
 * 
 */
public interface GDYNAuthorizeTransferResponseIfc extends AuthorizeTransferResponseIfc
{

    /**
     * @return the isSAFEligible
     */
    public boolean isSAFEligible();

    /**
     * @param isSAFEligible the isSAFEligible to set
     */
    public void setSAFEligible(boolean isSAFEligible);

	public abstract void setMerchantCopy(String[] merchantCopy);

	public abstract String[] getMerchantCopy();

	public abstract void setCustomerCopy(String[] customerCopy);

	public abstract String[] getCustomerCopy();
	
    /**
     * @return the endOfDayAuthorizationReport
     */
    public abstract String[] getEndOfDayAuthorizationSummaryReport();

    /**
     * @param endOfDayAuthorizationReport
     *            the endOfDayAuthorizationReport to set
     */
    public abstract void setEndOfDayAuthorizationSummaryReport(String[] endOfDayAuthorizationReport);


}
