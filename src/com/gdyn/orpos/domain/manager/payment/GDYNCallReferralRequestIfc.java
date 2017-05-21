//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.domain.manager.payment;

import oracle.retail.stores.domain.manager.payment.AuthorizeCallReferralRequestIfc;
import oracle.retail.stores.domain.manager.payment.AuthorizeTransferResponseIfc;

/**
 * @author dteagle
 * 
 */
public interface GDYNCallReferralRequestIfc extends
        AuthorizeCallReferralRequestIfc
{
    /**
     * Get the original response from the message.
     * GDYNCallReferralRequestIfc
     * AuthorizeTransferResponseIfc
     * @return
     */
    public AuthorizeTransferResponseIfc getOriginalResponse();

    /**
     * Set the original response from the message.
     * GDYNCallReferralRequestIfc
     * void
     * @param response
     */
    public void setOriginalResponse(AuthorizeTransferResponseIfc response);
}
