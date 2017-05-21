//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.manager.payment.fipay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import oracle.retail.stores.commext.message.MessageException;
import oracle.retail.stores.commext.message.MessageIfc;
import oracle.retail.stores.commext.message.MessageResponse;
import oracle.retail.stores.commext.message.MessageResponseIfc;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.manager.payment.AuthorizationConstantsIfc.TenderType;
import oracle.retail.stores.domain.manager.payment.AuthorizeTransferRequestIfc;

import org.apache.log4j.Logger;

import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferResponseIfc;

/**
 * Formats the card authorization message to send to AJB.
 * 
 * @author mlawrence
 * 
 */
public class GDYNFIPAYInitDebitFormatter extends GDYNAbstractFIPAYFormatter
{

    /**
     * Logger
     */
    public static final Logger logger = Logger
            .getLogger(com.gdyn.orpos.domain.manager.payment.fipay.GDYNFIPAYInitDebitFormatter.class);

    /**
     * Formats the message that will be relayed to the connector and sent to FIPAY.
     * 
     * @param MessageIfc
     *            message
     * @return GDYNFIPAYRequestIfc fipayRequest (Serializable)
     */
    public Serializable formatConnectorMessage(MessageIfc message) throws MessageException
    {
        GDYNFIPAYRequestIfc fipayRequest = null;

        if (message != null &&
                message.getMessageData() != null &&
                message.getMessageData() instanceof AuthorizeTransferRequestIfc)
        {
            AuthorizeTransferRequestIfc authRequest =
                    (AuthorizeTransferRequestIfc) message.getMessageData();

            List<String> requestList = doFormatMessage(authRequest);

            if (requestList != null && !requestList.isEmpty())
            {
                StringBuilder sb = new StringBuilder();

                for (String field : requestList)
                {
                    sb.append(field).append(COMMA);
                }
                logger.debug("The Credit/Debit Message being sent is:  " + sb);
                fipayRequest = new GDYNFIPAYRequest(sb.toString(), authRequest);
            }
        }
        return fipayRequest;
    }

    /**
     * Determines which type of request to format, based on the tender type.
     * 
     * @param authRequest
     * @return
     */
    protected List<String> doFormatMessage(AuthorizeTransferRequestIfc authRequest)
    {
        TenderType tenderType = authRequest.getRequestTenderType();

        if (TenderType.DEBIT.equals(tenderType))
        {
            return formatInitDebitMessage(authRequest, fillList(CREDIT_DEBIT_FIELD_LENGTH));
        }
        else
        {
            logger.error("Unknown tender type: " + tenderType);
        }
        return null;
    }

    /**
     * Builds the list of data fields that are sent to the FIPAY service.
     * Returned to the connector.
     * GDYNFIPAYCardAuthFormatter
     * void
     * (C) - Conditional
     * (M) - Mandatory
     * (O) - Optional
     * (R) - Reserved
     * 
     * @param authRequest
     * @return list of request fields
     */
    protected List<String> formatInitDebitMessage(AuthorizeTransferRequestIfc authRequest,
            List<String> requestList)
    {
        if (authRequest != null)
        {
            // Field 1: IxTransactionType (M)
            requestList.set(IX_CREDIT_DEBIT_TRANSACTION_TYPE_INDEX, CREDIT_DEBIT_AUTH_REQ_TRANSACTION_TYPE);

            // Field 5: IxTimeOut (M)
            requestList.set(IX_CREDIT_DEBIT_TIME_OUT_INDEX, getTimeOutInSeconds());

            // Field 6: IxDebitCredit (M) - Inserts a 'credit' or 'debit' string value
            requestList.set(IX_CREDIT_DEBIT_INDEX, DEBIT);

            // Field 7: IxTermId (R)

            // Field 8: IxStoreNumber (M)
            requestList.set(IX_CREDIT_DEBIT_STORE_NUMBER_INDEX, authRequest.getStoreID());

            // Field 9: IxTerminalNumber (M)
            requestList.set(IX_CREDIT_DEBIT_TERMINAL_NUMBER_INDEX, authRequest.getWorkstationID());

            // Field 10: IxTranType (M)
            requestList.set(IX_CREDIT_DEBIT_TRAN_TYPE_INDEX, INIT_DEBIT);
        }
        return requestList;
    }

    /**
     * Translates the message returned from the FIPAY Formatter and return it to the FIPAY Connector.
     * 
     * @param GDYNFIPAYResponseIfc
     *            response (Serializable)
     * @return MessageResponse
     */
    public MessageResponseIfc translateConnectorResponse(Serializable response)
            throws MessageException
    {
        GDYNAuthorizeTransferResponseIfc authResponse =
                (GDYNAuthorizeTransferResponseIfc) DomainGateway.getFactory()
                        .getAuthorizeTransferResponseInstance();

        GDYNFIPAYResponseIfc fipayResponse = (GDYNFIPAYResponseIfc) response;

        authResponse = doTranslateResponse(authResponse, fipayResponse);
        return new MessageResponse(authResponse);

    }

    /**
     * @param authResponse
     * @param fipayResponse
     * @return
     */
    protected GDYNAuthorizeTransferResponseIfc doTranslateResponse(
            GDYNAuthorizeTransferResponseIfc authResponse,
            GDYNFIPAYResponseIfc fipayResponse)
    {
        ArrayList<String> messageResponseList = fipayResponse.getResponseData();
        String responseTransactionType = null;

        for (String messageResponse : messageResponseList)
        {
            if (messageResponse != null && !messageResponse.isEmpty())
            {
                responseTransactionType = retrieveMessageResponseTransactionType(messageResponse);

                // Message Response 101
                if (responseTransactionType.equals(CREDIT_DEBIT_AUTH_RESP_TRANSACTION_TYPE))
                {
                    logger.debug("The Credit/Debit Response Message received is:  " + messageResponse);

                    ArrayList<String> responseList = parseResponseMessage(messageResponse, COMMA);

                    if (responseList.get(IX_CREDIT_DEBIT_INDEX).equals(DEBIT))
                    {
                        authResponse = translateCommonResponse(responseList, authResponse);
                        // Field 61: IxAdditionalMesg - (C) Picked up from Wanline.cfg table in Wansupp based on the action
                        // code.
                        String IxAddtionalMesg = responseList.get(IX_CREDIT_DEBIT_ADDTL_MESSAGE_INDEX);
                        authResponse.setResponseMessage(IxAddtionalMesg);
                    }
                }
                else
                {
                    logger.error("Unknown message response transaction type: " + responseTransactionType);
                }
            }

        }
        return authResponse;
    }
}
