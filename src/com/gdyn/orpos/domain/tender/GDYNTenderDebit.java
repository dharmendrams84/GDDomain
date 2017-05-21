//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.domain.tender;

import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferResponseIfc;

import oracle.retail.stores.domain.tender.TenderDebit;

/**
 * Groupe Dynamite tender debit object.
 * 
 * @author dteagle
 * 
 */
public class GDYNTenderDebit extends TenderDebit implements GDYNTenderAuthReceiptIfc
{
    private static final long serialVersionUID = -1359068550189549425L;

    /** response object from AJB */
    private GDYNAuthorizeTransferResponseIfc responseObject;

    /**
     * Returns the response object.
     * 
     * @return the response object
     */
    public GDYNAuthorizeTransferResponseIfc getResponseObject()
    {
        return responseObject;
    }

    /**
     * Sets the response object.
     * 
     * @param response
     *            a response object
     */
    public void setResponseObject(GDYNAuthorizeTransferResponseIfc response)
    {
        responseObject = response;
    }

}
