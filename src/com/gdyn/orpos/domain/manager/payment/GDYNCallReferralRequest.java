//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.manager.payment;

import oracle.retail.stores.domain.manager.payment.AuthorizeTransferResponseIfc;
import oracle.retail.stores.domain.manager.payment.PaymentServiceResponseIfc.ResponseCode;

/**
 * Extended to add the original response message sent back from AJB.
 * 
 * @author dteagle
 */
public class GDYNCallReferralRequest extends GDYNAuthorizeTransferRequest
        implements GDYNCallReferralRequestIfc
{
    /** serial id */
    private static final long serialVersionUID = 3375408328850253598L;

    /** the approval code retrieved during the call referral */
    protected String approvalCode;

    /** the text of the original response message from AJB */
    protected AuthorizeTransferResponseIfc originalResponse;

    /** the original response code from AJB */
    protected ResponseCode originalResponseCode;

    /**
     * 
     */
    public String getApprovalCode()
    {
        return approvalCode;
    }

    /**
     * 
     */
    public void setApprovalCode(String value)
    {
        approvalCode = value;
    }

    /**
     * 
     */
    public ResponseCode getOriginalResponseCode()
    {
        return originalResponseCode;
    }

    /**
     * 
     */
    public void setOriginalResponseCode(ResponseCode value)
    {
        originalResponseCode = value;
    }

    /**
     * 
     */
    public AuthorizeTransferResponseIfc getOriginalResponse()
    {
        return originalResponse;
    }

    /**
     * 
     */
    public void setOriginalResponse(AuthorizeTransferResponseIfc response)
    {
        originalResponse = response;
    }

}
