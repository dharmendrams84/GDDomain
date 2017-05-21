//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.domain.tender;

import oracle.retail.stores.domain.tender.TenderCharge;

import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferResponseIfc;

/**
 * Domain object for the tender authorization.
 * 
 * @author mlawrence
 * 
 */
public class GDYNTenderCharge extends TenderCharge implements GDYNTenderAuthReceiptIfc
{
    /**
	 * Serial ID
	 */
	private static final long serialVersionUID = -1192820981760211646L;
	
	/** response object from AJB */
    private GDYNAuthorizeTransferResponseIfc responseObject;
    
    /** 
     * Returns the response object.
     * @return the response object
     */
	public GDYNAuthorizeTransferResponseIfc getResponseObject() 
    { 
    	return responseObject; 
    }
    
    /**
     * Sets the response object.
     * @param response a response object
     */
    public void setResponseObject(GDYNAuthorizeTransferResponseIfc response)
    {
    	responseObject = response;
    }
	
}
