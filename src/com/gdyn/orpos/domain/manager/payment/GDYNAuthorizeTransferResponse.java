//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.manager.payment;

import oracle.retail.stores.domain.manager.payment.AuthorizeTransferResponse;

/**
 * Retrieves the response information process by the Authorization Payment Foundation.
 * Carries the information to the client for interrogation/processing.
 * 
 * @author mlawrence
 * 
 */
public class GDYNAuthorizeTransferResponse extends AuthorizeTransferResponse implements
        GDYNAuthorizeTransferResponseIfc
{

    /**
     * Serial ID
     */
    private static final long serialVersionUID = 3290343398200439560L;

    protected boolean isSAFEligible;

    protected String[] customerCopy;
    protected String[] merchantCopy;

    protected String[] endOfDayAuthorizationReport;

    /**
     * @return the isSAFEligible
     */
    public boolean isSAFEligible()
    {
        return isSAFEligible;
    }

    /**
     * @param isSAFEligible
     *            the isSAFEligible to set
     */
    public void setSAFEligible(boolean isSAFEligible)
    {
        this.isSAFEligible = isSAFEligible;
    }

    /**
     * @return the customerCopy
     */
    public String[] getCustomerCopy()
    {
        return customerCopy;
    }

    /**
     * @param customerCopy
     *            the customerCopy to set
     */
    public void setCustomerCopy(String[] customerCopy)
    {
        this.customerCopy = customerCopy;
    }

    /**
     * @return the merchantCopy
     */
    public String[] getMerchantCopy()
    {
        return merchantCopy;
    }

    /**
     * @param merchantCopy
     *            the merchantCopy to set
     */
    public void setMerchantCopy(String[] merchantCopy)
    {
        this.merchantCopy = merchantCopy;
    }

    /**
     * @return the endOfDayAuthorizationReport
     */
    public String[] getEndOfDayAuthorizationSummaryReport()
    {
        return endOfDayAuthorizationReport;
    }

    /**
     * @param endOfDayAuthorizationReport
     *            the endOfDayAuthorizationReport to set
     */
    public void setEndOfDayAuthorizationSummaryReport(String[] endOfDayAuthorizationReport)
    {
        this.endOfDayAuthorizationReport = endOfDayAuthorizationReport;
    }

}
