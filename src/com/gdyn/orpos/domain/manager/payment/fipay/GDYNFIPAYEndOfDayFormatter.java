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
import oracle.retail.stores.domain.manager.payment.AuthorizeTransferRequestIfc;

import org.apache.log4j.Logger;

import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferResponseIfc;

/**
 * Formats the End of Day message to send to AJB.
 * 
 * @author mlawrence
 * 
 */
public class GDYNFIPAYEndOfDayFormatter extends GDYNAbstractFIPAYFormatter
{

    /**
     * Logger
     */
    public static final Logger logger = Logger
            .getLogger(com.gdyn.orpos.domain.manager.payment.fipay.GDYNFIPAYEndOfDayFormatter.class);

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

            List<String> requestList = formatEndOfDayMessage(authRequest);

            if (requestList != null && !requestList.isEmpty())
            {
                StringBuilder sb = new StringBuilder();

                for (String field : requestList)
                {
                    sb.append(field).append(COMMA);
                }
                logger.debug("The End of Day Message being sent is:  " + sb);
                fipayRequest = new GDYNFIPAYRequest(sb.toString(), authRequest);
            }
        }
        return fipayRequest;
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
    protected List<String> formatEndOfDayMessage(AuthorizeTransferRequestIfc authRequest)
    {
        List<String> requestList = null;
        requestList = fillList(CREDIT_DEBIT_FIELD_LENGTH);

        if (authRequest != null)
        {
            // Field 1: IxTransactionType (M)
            requestList.set(IX_CREDIT_DEBIT_TRANSACTION_TYPE_INDEX, CREDIT_DEBIT_AUTH_REQ_TRANSACTION_TYPE);

            // Field 5: IxTimeOut (M)
            requestList.set(IX_CREDIT_DEBIT_TIME_OUT_INDEX, getTimeOutInSeconds());

            // Field 6: IxDebitCredit (M) - Inserts a 'credit' or 'debit' string value
            requestList.set(IX_CREDIT_DEBIT_INDEX, CREDIT);

            // Field 8: IxStoreNumber (M)
            requestList.set(IX_CREDIT_DEBIT_STORE_NUMBER_INDEX, authRequest.getStoreID());

            // Field 9: IxTerminalNumber (M)
            requestList.set(IX_CREDIT_DEBIT_TERMINAL_NUMBER_INDEX, authRequest.getWorkstationID());

            // Field 10: IxTranType (M)
            requestList.set(IX_CREDIT_DEBIT_TRAN_TYPE_INDEX, EOD);

            // Field 17: IxInvoice (M) - Transaction Sequence Number + Generated Sequence Number
            requestList.set(IX_CREDIT_DEBIT_INVOICE_INDEX, getIxInvoiceWithoutTransactionID(authRequest));
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

        ArrayList<String> messageResponseList = fipayResponse.getResponseData();

        for (String messageResponse : messageResponseList)
        {
            if (messageResponse == null || messageResponse.isEmpty())
            {
                logger.warn("Message response list contains null response -- skipping.");
            }
            else
            {
                logger.debug("Response Message received:  " + messageResponse);
                authResponse = doTranslateResponse(messageResponse, authResponse, fipayResponse);
            }
        }
        return new MessageResponse(authResponse);

    }

    /**
     * @param authResponse
     * @param fipayResponse
     * @return
     */
    protected GDYNAuthorizeTransferResponseIfc doTranslateResponse(
            String messageResponse,
            GDYNAuthorizeTransferResponseIfc authResponse,
            GDYNFIPAYResponseIfc fipayResponse)
    {
        String transactionTypeLine = messageResponse.substring(0,3);
        String endOfDayLine = messageResponse.substring(3, messageResponse.length());
        ArrayList<String> responseFields = new ArrayList<String>(); 

        responseFields.add(transactionTypeLine);
        responseFields.add(endOfDayLine);
        
        if (responseFields.size() < IX_CREDIT_DEBIT_TRANSACTION_TYPE_INDEX)
        {
            logger.error("Response list has no transaction type data field.");
        }
        else
        {
            logger.debug("Card Auth formatter processing message:  " + messageResponse);

            String responseTransactionType = responseFields.get(IX_CREDIT_DEBIT_TRANSACTION_TYPE_INDEX);

            // Message Response 101
            if (responseTransactionType.equals(CREDIT_DEBIT_AUTH_RESP_TRANSACTION_TYPE))
            {
                // authResponse = determineCreditDebitMessage(responseFields, authResponse, fipayResponse);
                // authResponse.setResponseMessage(messageResponse);
            }
            // Message Response 102
            else if (responseTransactionType.equals(FORMATTED_END_OF_DAY_AUTH_SUMMARY_REP_TRAN_TYPE))
            {
                authResponse = translateEndOfDaySummaryReport(responseFields, authResponse);
            }
            // Message Response 115
            else if (responseTransactionType.equals(SALES_AUDIT_RECORD_TRANSACTION_TYPE))
            {
                // authResponse = translateSalesAudit(messageResponse, authResponse);
                // AJB has informed us to ignore the 115 response message. Keeping as place holder for a bit.
            }
            else
            {
                logger.error("Unknown message response transaction type: " + responseTransactionType);
            }
        }
        return authResponse;
    }

    /**
     * Sets the End Of Day Authorizations Summary Report returned from the
     * End of Day Fipay Request
     * GDYNFIPAYCardAuthFormatter
     * 
     * @param messageResponse
     * @param authResponse
     * @return
     */
    protected GDYNAuthorizeTransferResponseIfc translateEndOfDaySummaryReport(
            List<String> responseFields,
            GDYNAuthorizeTransferResponseIfc authResponse)
    {
        if (responseFields.size() < FORMATTED_RECEIPT)
        {
            logger.error("Response list has no receipt data field.");
        }
        else
        {
            authResponse.setEndOfDayAuthorizationSummaryReport(
                    formatReceiptText(responseFields.get(FORMATTED_RECEIPT)));
        }
        return authResponse;
    }

}
