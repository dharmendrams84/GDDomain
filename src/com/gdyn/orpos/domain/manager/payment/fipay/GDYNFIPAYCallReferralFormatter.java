//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.domain.manager.payment.fipay;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import oracle.retail.stores.domain.manager.payment.AuthorizeTransferRequestIfc;
import oracle.retail.stores.domain.manager.payment.AuthorizeTransferResponseIfc;

import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferRequestIfc;
import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferResponseIfc;
import com.gdyn.orpos.domain.manager.payment.GDYNCallReferralRequestIfc;

/**
 * Extended to format call referral requests and responses from AJB.
 * 
 * @author dteagle
 */
public class GDYNFIPAYCallReferralFormatter extends GDYNFIPAYCardAuthFormatter
{
    /** logger */
    private static final Logger logger = 
        Logger.getLogger(GDYNFIPAYCallReferralFormatter.class);
    
    /**
     * Overridden to replace response fields with call referral data and
     * re-send request as a store and forward request.
     * 
     * @param authRequest
     *            the request to store and forward
     * @return list of strings to format into request message
     */
    protected List<String> doFormatMessage(AuthorizeTransferRequestIfc authRequest)
    {
        List<String> messageList = null;

        if (authRequest instanceof GDYNCallReferralRequestIfc)
        {
            AuthorizeTransferResponseIfc response =
                    ((GDYNCallReferralRequestIfc) authRequest).getOriginalResponse();

            if (response == null)
            {
                logger.error("Original FIPAY response is null.");
            }
            else if (response.getResponseMessage() == null)
            {
                logger.error("Original FIPAY response message is null.");
            }
            else
            {
                messageList = parseResponseMessage(response.getResponseMessage(), COMMA);

                if (messageList.size() >= 21)
                {
                    // instructions from Filomena at AJB
                    // take the 101 response change the 101 to a 111
                    messageList.set(IX_CREDIT_DEBIT_TRANSACTION_TYPE_INDEX,
                            CREDIT_DEBIT_AUTH_SAF_REQ_TRANSACTION_TYPE);

                    // remove the value in fipay field 4
                    messageList.set(IX_CREDIT_DEBIT_ACTION_CODE_INDEX, "");

                    // populate fipay field 19 with the approval code obtained by phone
                    messageList.set(IX_CREDIT_DEBIT_FORCE_AUTH_CODE_INDEX,
                            ((GDYNCallReferralRequestIfc) authRequest).getApprovalCode());

                    // populate fipay field 21 with TELAUTH
                    StringBuilder sb = new StringBuilder("TELAUTH");
                    String options = messageList.get(IX_CREDIT_DEBIT_OPTIONS_INDEX);

                    if (options != null && !options.isEmpty())
                    {
                        sb.append(" ").append(options);
                    }
                    messageList.set(IX_CREDIT_DEBIT_OPTIONS_INDEX, sb.toString());
                }
            }
        }
        return messageList;
    }

    /**
     * Overridden to parse the 111 call referral response.
     * 
     * @param authResponse
     *            the response to send back to the application flow
     * @param fipayResponse
     *            the response data from the authorizer
     * @return the populated response
     */
    protected GDYNAuthorizeTransferResponseIfc doTranslateResponse(
            String messageResponse,
            GDYNAuthorizeTransferResponseIfc authResponse,
            GDYNFIPAYResponseIfc fipayResponse)
    {
        logger.debug("Call Referral formatter processing message:  " + messageResponse);

        GDYNAuthorizeTransferRequestIfc originalRequest =
                (GDYNAuthorizeTransferRequestIfc) fipayResponse.getSource();

        ArrayList<String> responseFields = parseResponseMessage(messageResponse, COMMA);

        String responseType = responseFields.get(IX_CREDIT_DEBIT_TRANSACTION_TYPE_INDEX);

        if (responseType.equals(CREDIT_DEBIT_AUTH_SAF_REQ_TRANSACTION_TYPE))
        {
            return translateCreditMessage(responseFields, authResponse, originalRequest);
        }
        return super.doTranslateResponse(messageResponse, authResponse, fipayResponse);
    }

}
