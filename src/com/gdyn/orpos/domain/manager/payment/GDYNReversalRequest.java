//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.manager.payment;

import oracle.retail.stores.domain.manager.payment.ReversalRequest;

//------------------------------------------------------------------------------
/**
 * @author dteagle
 */
//------------------------------------------------------------------------------

public class GDYNReversalRequest extends ReversalRequest 
                                 implements GDYNReversalRequestIfc
{

    /**
     * 
     */
    private static final long serialVersionUID = -9153432155150438302L;

    //--------------------------------------------------------------------------
    /**
     */
    public GDYNReversalRequest()
    {
        // TODO Auto-generated constructor stub
    }

    //--------------------------------------------------------------------------
    /**
     * @param requestType
     */
    public GDYNReversalRequest(RequestType requestType)
    {
        super(requestType);
        // TODO Auto-generated constructor stub
    }

}
