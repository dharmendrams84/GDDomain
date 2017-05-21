//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.domain.manager.payment.fipay;

import java.util.ArrayList;

import oracle.retail.stores.domain.manager.payment.PaymentServiceRequestIfc;

/**
 * This class handles carrying the request from the Formatter to the Connector.
 * 
 * @author mlawrence
 * 
 */
public class GDYNFIPAYResponse implements GDYNFIPAYResponseIfc
{

    /**
     * Serial ID
     */
    private static final long serialVersionUID = 1946910572547367062L;

    // Response message in the appropriate String format for FIPAY
    ArrayList<String> messageResponseList = new ArrayList<String>();

    // Original Request message (AuthorizeTransferRequestIfc)
    protected PaymentServiceRequestIfc source;

    // private int timeOutInSeconds;

    public GDYNFIPAYResponse(ArrayList<String> messageResponseList, PaymentServiceRequestIfc source)
    {
        this.messageResponseList = messageResponseList;
        this.source = source;
    }

    /**
     * @return the messageResponseList
     */
    public ArrayList<String> getResponseData()
    {
        return messageResponseList;
    }

    /**
     * @param messageResponseList
     *            the messageResponseList to set
     */
    public void setResponseData(ArrayList<String> messageResponseList)
    {
        this.messageResponseList = messageResponseList;
    }

    /**
     * @return the source
     */
    public PaymentServiceRequestIfc getSource()
    {
        return source;
    }

    /**
     * @param source
     *            the source to set
     */
    public void setSource(PaymentServiceRequestIfc source)
    {
        this.source = source;
    }

}
