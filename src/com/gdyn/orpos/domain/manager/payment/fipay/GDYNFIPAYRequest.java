//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.domain.manager.payment.fipay;

import oracle.retail.stores.domain.manager.payment.PaymentServiceRequestIfc;

/**
 * This class handles carrying the request from the Formatter to the Connector.
 * @author mlawrence
 *
 */
public class GDYNFIPAYRequest implements GDYNFIPAYRequestIfc
{
    
    /**
     * Serial ID
     */
    private static final long serialVersionUID = 4851963952623961988L;
    
    // Request Message in the appropriate String format
    protected String requestData;
    
    // Original Message (AuthorizeTransferRequestIfc)
    protected PaymentServiceRequestIfc source;
    
//    private int timeOutInSeconds;
    
    public GDYNFIPAYRequest(String data, PaymentServiceRequestIfc source)
    {
        this.requestData = data;
        this.source = source;
    }

    /**
     * @return the requestData
     */
    public String getRequestData()
    {
        return requestData;
    }

    /**
     * @param requestData the requestData to set
     */
    public void setRequestData(String requestData)
    {
        this.requestData = requestData;
    }

    /**
     * @return the source
     */
    public PaymentServiceRequestIfc getSource()
    {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(PaymentServiceRequestIfc source)
    {
        this.source = source;
    }

}
