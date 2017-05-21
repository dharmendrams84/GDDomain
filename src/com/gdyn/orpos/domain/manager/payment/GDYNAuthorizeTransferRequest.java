//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.manager.payment;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.domain.manager.payment.AuthorizeTransferRequest;

/**
 * Carries around the tender authorization request information retrieved from the
 * client and passes it to the Authorization Payment Foundation.
 * 
 * @author mlawrence
 * 
 */
public class GDYNAuthorizeTransferRequest extends AuthorizeTransferRequest implements GDYNAuthorizeTransferRequestIfc
{

    /**
     * Serial ID
     */
    private static final long serialVersionUID = -5949123861999603637L;

    protected boolean isSAFEligible;
    protected CurrencyIfc balanceInquiryAmount;

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
     * Set the balance inquiry amount
     * 
     * @param amount
     */
    public void setBalanceInquiryAmount(CurrencyIfc amount)
    {
        if (amount.signum() == -1)
        {
            throw new IllegalArgumentException("Requested transfer amount should not be negative.");
        }
        else
        {
            balanceInquiryAmount = amount;
            return;
        }
    }

    /**
     * Set the balance inquiry amount
     *
     * @return balanceInquiryAmount
     */
    public CurrencyIfc getBalanceInquiryAmount()
    {
        return balanceInquiryAmount;
    }

}
