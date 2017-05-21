//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.manager.payment;

import oracle.retail.stores.domain.manager.ifc.PaymentManagerIfc;
import oracle.retail.stores.domain.manager.payment.AuthorizeRequestIfc;
import oracle.retail.stores.domain.manager.payment.AuthorizeTransferResponseIfc;

public interface GDYNPaymentManagerIfc extends PaymentManagerIfc
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
    public AuthorizeTransferResponseIfc reversalFIPAY(AuthorizeRequestIfc reversalRequest);

}
