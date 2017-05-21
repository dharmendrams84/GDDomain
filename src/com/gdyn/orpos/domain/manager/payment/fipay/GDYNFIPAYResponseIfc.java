//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.domain.manager.payment.fipay;

import java.io.Serializable;
import java.util.ArrayList;

import oracle.retail.stores.domain.manager.payment.PaymentServiceRequestIfc;

/**
 * Contains the response information for the FIPAY service.
 * 
 * @author mlawrence
 * 
 */
public interface GDYNFIPAYResponseIfc extends Serializable
{
    /**
     * @return the messageResponseList
     */
    public ArrayList<String> getResponseData();

    /**
     * @param messageResponseList
     *            the messageResponseList to set
     */
    public void setResponseData(ArrayList<String> messageResponseList);

    /**
     * @return the source
     */
    public PaymentServiceRequestIfc getSource();

    /**
     * @param source
     *            the source to set
     */
    public void setSource(PaymentServiceRequestIfc source);

}
