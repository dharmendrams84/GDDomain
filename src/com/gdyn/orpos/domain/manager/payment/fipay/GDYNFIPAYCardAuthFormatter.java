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
import oracle.retail.stores.domain.manager.payment.AuthorizeRequestIfc;
import oracle.retail.stores.domain.manager.payment.AuthorizeTransferRequestIfc;
import oracle.retail.stores.domain.manager.payment.AuthorizeTransferResponseIfc;
import oracle.retail.stores.domain.manager.payment.ReversalRequestIfc;
import oracle.retail.stores.domain.utility.EntryMethod;

import org.apache.log4j.Logger;

import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferRequestIfc;
import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferResponseIfc;

/**
 * Formats the card authorization message to send to AJB.
 * 
 * @author mlawrence
 * 
 */
public class GDYNFIPAYCardAuthFormatter extends GDYNAbstractFIPAYFormatter
{

    /**
     * Logger
     */
    private static final Logger logger = Logger
            .getLogger(com.gdyn.orpos.domain.manager.payment.fipay.GDYNFIPAYCardAuthFormatter.class);

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

        if (message == null)
        {
            logger.error("Formatter received null message.");
        }
        else if (message.getMessageData() == null)
        {
            logger.error("Formatter message has no data.");
        }
        else if (!(message.getMessageData() instanceof AuthorizeTransferRequestIfc))
        {
            logger.error("Formatter message data does not implement AuthorizeTransferRequestIfc");
        }
        else
        {
            AuthorizeTransferRequestIfc authRequest =
                    (AuthorizeTransferRequestIfc) message.getMessageData();

            List<String> requestList = doFormatMessage(authRequest);

            if (requestList == null)
            {
                logger.error("Formatter request list is null.");
            }
            else if (requestList.isEmpty())
            {
                logger.error("Formatter request list is empty.");
            }
            else
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

        if (TenderType.CREDIT.equals(tenderType))
        {
            return formatCreditMessage(authRequest, fillList(CREDIT_DEBIT_FIELD_LENGTH));
        }
        else if (TenderType.DEBIT.equals(tenderType))
        {
            return formatDebitMessage(authRequest, fillList(CREDIT_DEBIT_FIELD_LENGTH));
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
    protected List<String> formatCreditMessage(AuthorizeTransferRequestIfc authRequest, List<String> requestList)
    {
        if (authRequest == null)
        {
            logger.error("Authorization request for credit message is null.");
        }
        else
        {
            requestList = formatCommonRequest(requestList, authRequest);

            // Field 1: IxTransactionType (M)
            requestList.set(IX_CREDIT_DEBIT_TRANSACTION_TYPE_INDEX, CREDIT_DEBIT_AUTH_REQ_TRANSACTION_TYPE);

            // Field 10: IxTranType (M)
            requestList.set(IX_CREDIT_DEBIT_TRAN_TYPE_INDEX, getIxTranType(authRequest));

            // Field 16: IxAmount (M) - Two (2) decimals places are assumed so the
            // decimal is not required. For example, if the amount is for $89.75,
            // the field would contain 8975
            requestList.set(IX_CREDIT_DEBIT_AMOUNT_INDEX, getFormatUtils().formatTenderAmount(authRequest));

            // Field 17: IxInvoice (M) - Transaction Sequence Number + Generated
            // Sequence Number aka Journal Key
            requestList.set(IX_CREDIT_DEBIT_INVOICE_INDEX, getIxInvoice(authRequest));
        }
        return requestList;
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
    protected List<String> formatDebitMessage(AuthorizeTransferRequestIfc authRequest,
            List<String> requestList)
    {
        if (authRequest == null)
        {
            logger.error("Authorization request for debit message is null.");
        }
        else
        {
            requestList = formatCommonRequest(requestList, authRequest);

            // Field 1: IxTransactionType (M)
            requestList.set(IX_CREDIT_DEBIT_TRANSACTION_TYPE_INDEX, CREDIT_DEBIT_AUTH_REQ_TRANSACTION_TYPE);

            // Field 10: IxTranType (M)
            requestList.set(IX_CREDIT_DEBIT_TRAN_TYPE_INDEX, getIxTranType(authRequest));

            // Field 16: IxAmount (M) - Two (2) decimals places are assumed so the
            // decimal is not required. For example, if the amount is for $89.75,
            // the field would contain 8975
            requestList.set(IX_CREDIT_DEBIT_AMOUNT_INDEX, getFormatUtils().formatTenderAmount(authRequest));

            // Field 17: IxInvoice (M) - Transaction Sequence Number + Generated Sequence Number
            requestList.set(IX_CREDIT_DEBIT_INVOICE_INDEX, getIxInvoice(authRequest));
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
                //Pak add the following replacment
                messageResponse.replaceAll("]]>&#28;", "");
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
        ArrayList<String> responseFields = parseResponseMessage(messageResponse, COMMA);

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
                authResponse = determineCreditDebitMessage(responseFields, authResponse, fipayResponse);
                authResponse.setResponseMessage(messageResponse);
            }
            // Message Response 102
            else if (responseTransactionType.equals(FORMATTED_STORE_RECEIPT_TRANSACTION_TYPE))
            {
                authResponse = translateStoreReceipt(responseFields, authResponse);
            }
            // Message Response 103
            else if (responseTransactionType.equals(FORMATTED_CUSTOMER_RECEIPT_TRANSACTION_TYPE))
            {
                authResponse = translateCustomerReceipt(responseFields, authResponse);
            }
            // Message Response 105
            else if (responseTransactionType.equals(PIN_PAD_PROGRESS_MESSAGES_TRANSACTION_TYPE))
            {

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
     * 
     * GDYNFIPAYCardAuthFormatter
     * GDYNAuthorizeTransferResponseIfc
     * 
     * @param messageResponse
     * @param authResponse
     * @return
     */
    protected GDYNAuthorizeTransferResponseIfc determineCreditDebitMessage(
            List<String> responseFields,
            GDYNAuthorizeTransferResponseIfc authResponse,
            GDYNFIPAYResponseIfc fipayResponse)
    {
        AuthorizeRequestIfc originalRequest;
        if (fipayResponse.getSource() instanceof ReversalRequestIfc)
        {
            originalRequest = (ReversalRequestIfc) fipayResponse.getSource();
        }
        else
        {
            originalRequest =
                    (GDYNAuthorizeTransferRequestIfc) fipayResponse.getSource();
        }

        if (responseFields.get(IX_CREDIT_DEBIT_INDEX).equals(CREDIT))
        {
            authResponse = translateCreditMessage(responseFields, authResponse, originalRequest);
        }
        else if (responseFields.get(IX_CREDIT_DEBIT_INDEX).equals(DEBIT))
        {
            authResponse = translateDebitMessage(responseFields, authResponse, originalRequest);
        }
        return authResponse;
    }

    /**
     * 
     * GDYNFIPAYCardAuthFormatter
     * 
     * @param fipayResponse
     * @param authResponse
     * @return
     */
    protected AuthorizeTransferResponseIfc translateSalesAudit(GDYNFIPAYResponseIfc fipayResponse,
            AuthorizeTransferResponseIfc authResponse)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 
     * GDYNFIPAYCardAuthFormatter
     * 
     * @param messageResponse
     * @param authResponse
     * @return
     */
    protected GDYNAuthorizeTransferResponseIfc translateCustomerReceipt(
            List<String> responseFields,
            GDYNAuthorizeTransferResponseIfc authResponse)
    {
        if (responseFields.size() < FORMATTED_RECEIPT)
        {
            logger.error("Response list has no receipt data field.");
        }
        else
        {
            authResponse.setCustomerCopy(
                    formatReceiptText(responseFields.get(FORMATTED_RECEIPT)));
        }
        return authResponse;
    }

    /**
     * 
     * GDYNFIPAYCardAuthFormatter
     * 
     * @param messageResponse
     * @param authResponse
     * @return
     */
    protected GDYNAuthorizeTransferResponseIfc translateStoreReceipt(
            List<String> responseFields,
            GDYNAuthorizeTransferResponseIfc authResponse)
    {
        if (responseFields.size() < FORMATTED_RECEIPT)
        {
            logger.error("Response list has no receipt data field.");
        }
        else
        {
            authResponse.setMerchantCopy(
                    formatReceiptText(responseFields.get(FORMATTED_RECEIPT)));
        }
        return authResponse;
    }

    /**
     * Translates the response returned from FIPAY for Debit Card Authorization.
     * GDYNFIPAYCardAuthFormatter
     * AuthorizeTransferResponseIfc
     * 
     * @param responseList
     * 
     * @param authResponse
     * @param originalRequest
     * @return authResponse
     */
    protected GDYNAuthorizeTransferResponseIfc translateDebitMessage(
            List<String> responseList,
            GDYNAuthorizeTransferResponseIfc authResponse,
            AuthorizeRequestIfc originalRequest)
    {
        logger.debug("The size of the 101 response is:  " + responseList.size());

        // Set the transaction id from the request on the response.
        String transcactionId = originalRequest.getTransactionID();
        authResponse.setTransactionId(transcactionId);

        authResponse = translateCommonResponse(responseList, authResponse);

        if (responseList.size() < IX_CREDIT_DEBIT_FIELD12_RESERVED_INDEX)
        {
            logger.error("Debit response has less than expected data fields.");
        }
        else
        {
            // Field 12: Reserved (Card Type)
            String tenderSubType = responseList.get(IX_CREDIT_DEBIT_FIELD12_RESERVED_INDEX);
            authResponse.setTenderSubType(tenderSubType);

            // Field 21: IxOptions - (C) Echoed from the request message unless it is EMV in which there will be
            // information regarding the EMV card.
            String IxOptions = responseList.get(IX_CREDIT_DEBIT_OPTIONS_INDEX);
            logger.debug("IxOptions contains:  " + IxOptions);
            authResponse = getIxOptions(authResponse, IxOptions);

        }
        return authResponse;
    }

    /**
     * Translates the response returned from FIPAY for Credit Card Authorization.
     * GDYNFIPAYCardAuthFormatter
     * AuthorizeTransferResponseIfc
     * 
     * @param responseList
     * @param authResponse
     * @param originalRequest
     * @return authResponse
     */
    protected GDYNAuthorizeTransferResponseIfc translateCreditMessage(
            List<String> responseList,
            GDYNAuthorizeTransferResponseIfc authResponse,
            AuthorizeRequestIfc originalRequest)
    {
        int size = responseList.size();
        logger.debug("The size of the 101 response is:  " + size);

        // Set the transaction id from the request on the response.
        String transcactionId = originalRequest.getTransactionID();
        authResponse.setTransactionId(transcactionId);

        authResponse = translateCommonResponse(responseList, authResponse);

        if (responseList.size() < IX_CREDIT_DEBIT_FIELD12_RESERVED_INDEX)
        {
            logger.error("Credit response has less than expected data fields.");
        }
        else
        {
            // Field 12: Reserved (Card Type)
            String tenderSubType = responseList.get(IX_CREDIT_DEBIT_FIELD12_RESERVED_INDEX);
            authResponse.setTenderSubType(tenderSubType);
            logger.debug("The Reserved 12 Position is:  (Tender Sub Type/Card Type)  " + tenderSubType);

            // Field 21: IxOptions - (C) Echoed from the request message unless it is EMV in which there will be
            // information regarding the EMV card.
            String IxOptions = responseList.get(IX_CREDIT_DEBIT_OPTIONS_INDEX);
            logger.debug("IxOptions contains:  " + IxOptions);
            authResponse = getIxOptions(authResponse, IxOptions);

            if (responseList.size() > NON_ERROR_RETRY_RESPONSE_FIELD_LENGTH)
            {
                // Field 89: IXIsSAFable - (C) If the transaction is eligible to be
                // SAFed, then the verbiage SAFable will appear in this field.
                // Otherwise, this field will be empty indicating that the transaction
                // may not be SAFed. The POS application may SAF the transaction by
                // sending a subsequent 111 request.
                boolean isSAFEligible = getSAFEligible(responseList.get(IX_CREDIT_IS_SAFABLE_INDEX));
                authResponse.setSAFEligible(isSAFEligible);
            }
        }
        return authResponse;
    }

    /**
     * Get the Entry Method from the IxOptions field
     * GDYNFIPAYCardAuthFormatter
     * GDYNAuthorizeTransferResponseIfc
     * 
     * @param authResponse
     * @param IxOptions
     * @return
     */
    protected GDYNAuthorizeTransferResponseIfc getIxOptions(GDYNAuthorizeTransferResponseIfc authResponse,
            String IxOptions)
    {
        // GD-424 : dteagle
        // changed to indexOf instead of equals -- wasn't finding 'swiped' 
        // string in some cases
        if( IxOptions != null )
        {
            if( IxOptions.indexOf( "_Swiped" ) != -1 )
            {
                authResponse.setEntryMethod( EntryMethod.Swipe );
            }
            else if( IxOptions.indexOf( "_Insert" ) != -1 )
            {
                authResponse.setEntryMethod( EntryMethod.ICC );
            }
            // null entry method defaults to 'Manual'
        }
        // end GD-424
        return authResponse;
    }
}
