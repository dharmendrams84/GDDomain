//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.domain.tender;

import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferResponseIfc;

/**
 * Interface for tender objects that contain response objects for
 * receipt data passed back from AJB.
 * 
 * @author dteagle
 * 
 */
public interface GDYNTenderAuthReceiptIfc
{
    public GDYNAuthorizeTransferResponseIfc getResponseObject();

    public void setResponseObject(GDYNAuthorizeTransferResponseIfc response);
}
