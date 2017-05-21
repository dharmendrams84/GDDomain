//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.domain.manager.payment.fipay;

import java.io.Serializable;

import oracle.retail.stores.domain.manager.payment.PaymentServiceRequestIfc;

/**
 * Contains the request information for the FIPAY service.
 * 
 * @author mlawrence
 * 
 */
public interface GDYNFIPAYRequestIfc extends Serializable
{
    /**
     * @return the requestData
     */
    public String getRequestData();

    /**
     * @param requestData the requestData to set
     */
    public void setRequestData(String requestData);
    /**
     * @return the source
     */
    public PaymentServiceRequestIfc getSource();

    /**
     * @param source the source to set
     */
    public void setSource(PaymentServiceRequestIfc source);

}
