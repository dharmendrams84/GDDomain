//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.manager.payment.fipay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.commext.message.MessageException;
import oracle.retail.stores.domain.manager.payment.AuthorizationConstantsIfc.TenderType;
import oracle.retail.stores.domain.manager.payment.AuthorizeRequestIfc;
import oracle.retail.stores.domain.manager.payment.AuthorizeTransferRequestIfc;

import org.apache.log4j.Logger;

import AJBComm.CAFipay;
import AJBComm.CAFipayNetworkException;
import AJBComm.CAFipayTimeoutException;

import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferRequestIfc;

/**
 * Gift Card Connector to handle only Gift Card interactions. This will allow for multiple calls
 * for tender since a Balance Inquiry request/response needs to occur first before making the tender
 * authorization request. This will determine if the full amount of the tender can be paid for by
 * the current gift card or if another from of tender is needed to complete the sale.
 * 
 * @author mlawrence
 * 
 */
public class GDYNFIPAYGiftCardConnector extends GDYNFIPAYConnector
{
    /**
     * Logger
     */
    public static final Logger logger = Logger.getLogger(GDYNFIPAYGiftCardConnector.class);

    GDYNFIPAYFormatUtilsIfc fipayUtils = GDYNFIPAYFormatUtils.getInstance();

    /**
     * This method sends the message to the FIPay payment processor
     * 
     * @param
     * @return
     */
    protected Serializable send(Serializable message) throws MessageException
    {
        GDYNFIPAYRequest fipayRequest = (GDYNFIPAYRequest) message;
        GDYNFIPAYRequest inquiryRequest = null;

        GDYNFIPAYResponseIfc responseInquiry = null;
        GDYNFIPAYResponseIfc response = null;

        boolean isGiftCardTender = determineMessageType(fipayRequest);

        if (isGiftCardTender)
        {
            // build request
            inquiryRequest = buildInquiryRequest(fipayRequest);
            responseInquiry = sendMessage(inquiryRequest);

            if (responseInquiry != null && !responseInquiry.getResponseData().isEmpty())
            {
                // Get the message list from the response and loop through
                ArrayList<String> messageResponseList = responseInquiry.getResponseData();

                for (String messageResponse : messageResponseList)
                {
                    if (messageResponse == null || messageResponse.isEmpty())
                    {
                        logger.warn("Message response list contains null response -- skipping.");
                    }
                    else
                    {
                        logger.debug("Response Message received:  " + messageResponse);

                        // Parse the response fields into a reusable array list to get the amount per
                        // a index
                        ArrayList<String> responseFields = parseMessage(messageResponse, COMMA);

                        if (responseFields.size() < GIFT_CARD_RESPONSE_FIELD_LENGTH)
                        {
                            logger.error("Gift card response has less than expected data fields:  "
                                    + responseFields.size());
                        }
                        else
                        {
                            // Get the inquiry amount
                            CurrencyIfc inquiryAmount = fipayUtils.translateCurrency(responseFields
                                    .get(IX_CREDIT_DEBIT_DEPOSIT_DATA_INDEX));
                            logger.debug("Inquiry Amount for GCRD Tender Sale is:  " + inquiryAmount.getStringValue());

                            // Get the tender sale amount from the auth request
                            GDYNAuthorizeTransferRequestIfc tenderSaleAuthRequest = (GDYNAuthorizeTransferRequestIfc) fipayRequest
                                    .getSource();
                            CurrencyIfc saleAmount = tenderSaleAuthRequest.getBaseAmount();
                            logger.debug("Sale Amount for GCRD Tender Sale is:  " + saleAmount.getStringValue());

                            // If the inquiry amount is less than the tender sale amount then use
                            // the inquiry amount for the tender amount. Otherwise send the sale tender
                            // amount.
                            if (inquiryAmount.compareTo(saleAmount) == CurrencyIfc.LESS_THAN)
                            {
                                fipayRequest = updateTenderAmount(inquiryAmount, fipayRequest);
                                tenderSaleAuthRequest.setBaseAmount(inquiryAmount);
                            }
                            // Send the tender authorize sale request
                            tenderSaleAuthRequest.setBalanceInquiryAmount(inquiryAmount);
                            fipayRequest.setSource(tenderSaleAuthRequest);
                            response = sendMessage(fipayRequest);

                        }
                    }
                }
            }
        }
        else
        {
            response = sendMessage(fipayRequest);
        }

        return response;

    }

    /**
     * Update the request message with the balance inquiry amount.
     * 
     * @param inquiryAmount
     * @param fipayRequest
     * @return
     */
    protected GDYNFIPAYRequest updateTenderAmount(CurrencyIfc inquiryAmount, GDYNFIPAYRequest fipayRequest)
    {
        String inquiryAmountString = fipayUtils.formatAmount(inquiryAmount);

        String message = fipayRequest.getRequestData();
        ArrayList<String> messageList = parseMessage(message, COMMA);
        messageList.set(IX_CREDIT_DEBIT_AMOUNT_INDEX, inquiryAmountString);

        StringBuilder sb = new StringBuilder();

        for (String field : messageList)
        {
            sb.append(field).append(COMMA);
        }
        fipayRequest.setRequestData(sb.toString());

        return fipayRequest;
    }

    /**
     * Build the request message for the balance inquiry to see what balance
     * is on the card.
     * 
     * @param fipayRequest
     * @return
     */
    protected GDYNFIPAYRequest buildInquiryRequest(GDYNFIPAYRequest fipayRequest)
    {
        GDYNFIPAYRequest fipayInquiryRequest = null;

        GDYNAuthorizeTransferRequestIfc authRequest = (GDYNAuthorizeTransferRequestIfc) fipayRequest.getSource();

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
            logger.debug("The Gift Card Balance Inquiry for Tender Message being sent is:  " + sb);
            fipayInquiryRequest = new GDYNFIPAYRequest(sb.toString(), authRequest);
        }

        return fipayInquiryRequest;
    }

    /**
     * Determines if the request is an inquiry or a tender.
     * 
     * @param fipayRequest
     * @return boolean
     */
    protected boolean determineMessageType(GDYNFIPAYRequest fipayRequest)
    {
        boolean isMessageTenderGiftCard = false;
        AuthorizeRequestIfc request = (AuthorizeRequestIfc) fipayRequest.getSource();

        if (request instanceof GDYNAuthorizeTransferRequestIfc)
        {
            GDYNAuthorizeTransferRequestIfc requestAuthorization = (GDYNAuthorizeTransferRequestIfc) request;
            if (TenderType.GIFT_CARD.equals(requestAuthorization.getRequestTenderType()))
            {
                if (AuthorizeTransferRequestIfc.RequestSubType.AuthorizeSale.equals(requestAuthorization
                        .getRequestSubType()))
                {
                    isMessageTenderGiftCard = true;
                }
            }
            logger.debug("What is this a Gift Card Tender Sub Type?:  " + requestAuthorization.getRequestSubType());
        }

        return isMessageTenderGiftCard;
    }

    /**
     * 
     * @param requestMessage
     * @return
     */
    public GDYNFIPAYResponseIfc sendMessage(GDYNFIPAYRequest fipayRequest)
    {
        boolean fipayResponse = false;
        GDYNFIPAYResponseIfc response = null;
        CAFipay fipay = new CAFipay(hostName, port);

        String requestMessage = fipayRequest.getRequestData();

        try
        {
            logger.debug("The FIPAY Request Message being sent is:  " + requestMessage);
            fipayResponse = fipay.SEND_MSGAPI(requestMessage);
        }
        catch (CAFipayNetworkException e)
        {
            logger.error("The FIPAY Request Message has a network connection error:  " + e.getErrorCode()
                    + e.getErrorDescription());
            e.printStackTrace();
        }
        catch (CAFipayTimeoutException e)
        {
            logger.error("The FIPAY Request Message has a timeout error:  " + e);
            e.printStackTrace();
        }

        if (fipayResponse)
        {
            try
            {
                boolean messageEvent = false;
                long startTime = System.currentTimeMillis();
                long elapsedTime;

                ArrayList<String> messageResponseList = new ArrayList<String>();

                do
                {
                    String responseFIPAY = fipay.RECV_MSGAPI();

                    messageResponseList.add(responseFIPAY);

                    if (responseFIPAY.startsWith(CREDIT_DEBIT_AUTH_RESP_TRANSACTION_TYPE) ||
                            responseFIPAY.startsWith(CREDIT_DEBIT_AUTH_SAF_REQ_TRANSACTION_TYPE))
                    {
                        messageEvent = true;
                    }

                    logger.debug("The FIPAY Response Message retrieved is:  " + responseFIPAY);

                    elapsedTime = System.currentTimeMillis() - startTime;
                }
                while ((elapsedTime < messageResponseTimeout) &&
                        (messageEvent != true));

                response = new GDYNFIPAYResponse(messageResponseList, fipayRequest.getSource());

            }
            catch (CAFipayNetworkException e)
            {
                logger.debug("The FIPAY Response Message has a network connection error:  " + e);
                e.printStackTrace();
            }
        }

        return response;
    }

    /**
     * Format the Gift Card Message with the information passed in the request.
     * 
     * @param AuthorizeTransferRequestIfc
     *            authRequest
     * @return requestList
     */
    protected List<String> doFormatMessage(AuthorizeTransferRequestIfc authRequest)
    {
        List<String> requestList = null;

        requestList = fipayUtils.fillList(CREDIT_DEBIT_FIELD_LENGTH);

        // Field 1: IxTransactionType (M)
        requestList.set(IX_CREDIT_DEBIT_TRANSACTION_TYPE_INDEX,
                CREDIT_DEBIT_AUTH_REQ_TRANSACTION_TYPE);

        // Field 5: IxTimeOut (M)
        requestList.set(IX_CREDIT_DEBIT_TIME_OUT_INDEX, Integer.toString(getMessageResponseTimeout()));

        // Field 6: IxDebitCredit (M)
        requestList.set(IX_CREDIT_DEBIT_INDEX, GIFT_CARD);

        // Field 7: Reserved

        // Field 8: IxStoreNumber (M)
        requestList.set(IX_CREDIT_DEBIT_STORE_NUMBER_INDEX, authRequest.getStoreID());

        // Field 9: IxTerminalNumber (M)
        requestList.set(IX_CREDIT_DEBIT_TERMINAL_NUMBER_INDEX, authRequest.getWorkstationID());

        // Field 17: IxInvoice (M) - Transaction Sequence Number + Generated
        // Sequence Number aka Journal Key
        // requestList.set(IX_CREDIT_DEBIT_INVOICE_INDEX, getIxInvoice(authRequest));

        requestList.set(IX_CREDIT_DEBIT_TRAN_TYPE_INDEX, GC_BALANCE_INQUIRY);

        requestList.set(IX_CREDIT_DEBIT_ACCOUNT_INDEX,
                fipayUtils.getClearAccountNumber(authRequest.getCardData().getEncryptedAcctNumber()));

        return requestList;
    }

    /**
     * Parses out a comma delimited message.
     * GDYNAbstractFIPAYFormatter
     * 
     * @param message
     * @param delim
     * @return responseList
     */
    protected ArrayList<String> parseMessage(String message, String delim)
    {
        ArrayList<String> responseList =
                new ArrayList<String>(Arrays.asList(message.split(delim)));

        return responseList;

    }
}
