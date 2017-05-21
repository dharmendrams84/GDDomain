//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.manager.payment;

import oracle.retail.stores.domain.manager.payment.AuthorizeRequestIfc;
import oracle.retail.stores.domain.manager.payment.AuthorizeTransferResponseIfc;
import oracle.retail.stores.domain.manager.payment.PaymentManager;

/**
 * Extending this class due to the fact that Oracle has a known bug that has made the send
 * method of the base class private. 
 * - Reversal Printing
 * 
 * @author mlawrence
 *
 */
public class GDYNPaymentManager extends PaymentManager implements GDYNPaymentManagerIfc
{
    /**
     * Creating this method due to the fact that the base reversal does not provide a response object.
     * Also having to instead call the send method as the reversal would have I have to call the authorize 
     * method due to the fact that is not private and provides a response object.
     * 
     * AuthorizeTransferResponseIfc
     * @param reversalRequest
     * @return
     */
    public AuthorizeTransferResponseIfc reversalFIPAY(AuthorizeRequestIfc reversalRequest)
    {
        AuthorizeTransferResponseIfc response;
        response = (AuthorizeTransferResponseIfc) authorize(reversalRequest);
        return response;
    }

}
