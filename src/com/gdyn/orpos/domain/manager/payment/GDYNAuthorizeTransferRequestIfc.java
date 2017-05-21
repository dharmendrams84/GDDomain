//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.manager.payment;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.domain.manager.payment.AuthorizeTransferRequestIfc;

/**
 * Carries around the tender authorization request information retrieved from the
 * client and passes it to the Authorization Payment Foundation.
 * 
 * @author mlawrence
 * 
 */
public interface GDYNAuthorizeTransferRequestIfc extends AuthorizeTransferRequestIfc
{
    /**
     * @return the isSAFEligible
     */
    public boolean isSAFEligible();

    /**
     * @param isSAFEligible the isSAFEligible to set
     */
    public void setSAFEligible(boolean isSAFEligible);
    
    /**
     * Set the balance inquiry amount
     * 
     * @param amount
     */
    public void setBalanceInquiryAmount(CurrencyIfc amount);

    /**
     * Set the balance inquiry amount
     *
     * @return balanceInquiryAmount
     */
    public CurrencyIfc getBalanceInquiryAmount();

}
