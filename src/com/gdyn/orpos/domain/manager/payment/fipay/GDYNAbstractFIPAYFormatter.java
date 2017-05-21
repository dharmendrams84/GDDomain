//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.manager.payment.fipay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.commext.message.formatter.FormatterIfc;
import oracle.retail.stores.common.context.BeanLocator;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.manager.payment.AuthorizationConstantsIfc;
import oracle.retail.stores.domain.manager.payment.AuthorizationConstantsIfc.TenderType;
import oracle.retail.stores.domain.manager.payment.AuthorizeTransferRequestIfc;
import oracle.retail.stores.domain.manager.payment.PaymentServiceResponseIfc.ResponseCode;
import oracle.retail.stores.domain.utility.EYSDate;
import oracle.retail.stores.domain.utility.GiftCardIfc.StatusCode;
import oracle.retail.stores.domain.utility.LocaleConstantsIfc;
import oracle.retail.stores.foundation.utility.LocaleMap;

import org.apache.log4j.Logger;

import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferResponseIfc;

/**
 * Abstract Formatter for all FIPAY Formatters. Reusable methods will go here.
 * 
 * @author mlawrence
 * 
 */
@SuppressWarnings("deprecation")
public abstract class GDYNAbstractFIPAYFormatter implements FormatterIfc, GDYNFIPAYRequestResponseConstantsIfc
{
    protected String timeOutInSeconds;
    protected GDYNFIPAYSequenceNumberGenerator sequencer;
    Locale locale = LocaleMap.getLocale(LocaleConstantsIfc.DEFAULT_LOCALE);

    protected String SWIPED = "Swiped";
    protected String INSERT = "Insert";

    /**
     * Logger
     */
    public static final Logger logger = Logger
            .getLogger(com.gdyn.orpos.domain.manager.payment.fipay.GDYNAbstractFIPAYFormatter.class);

    /**
     * Constructor
     */
    public GDYNAbstractFIPAYFormatter()
    {
        sequencer = getSequencer();

    }

    /**
     * @return the timeOutInSeconds
     */
    public String getTimeOutInSeconds()
    {
        return timeOutInSeconds;
    }

    /**
     * @param timeOutInSeconds
     *            the timeOutInSeconds to set
     */
    public void setTimeOutInSeconds(String timeOutInSeconds)
    {
        this.timeOutInSeconds = timeOutInSeconds;
    }

    /**
     * Get the sequence number generator for the IxInvoice field.
     * GDYNAbstractFIPAYFormatter
     * 
     * @return GDYNFIPAYSequenceNumberGenerator
     */
    protected synchronized GDYNFIPAYSequenceNumberGenerator getSequencer()
    {
        if (sequencer == null)
        {
            String key = "application_FIPAYIxInvoiceSequenceNumberGenerator";
            sequencer = (GDYNFIPAYSequenceNumberGenerator) BeanLocator.getApplicationBean(key);
        }
        return sequencer;
    }

    /**
     * Get the FIPAY Format Utils instance
     * GDYNAbstractFIPAYFormatter
     * 
     * @return GDYNFIPAYFormatUtilsIfc
     */
    protected GDYNFIPAYFormatUtilsIfc getFormatUtils()
    {
        return GDYNFIPAYFormatUtils.getInstance();
    }

    /**
     * Parses out a comma delimited response message.
     * GDYNAbstractFIPAYFormatter
     * 
     * @param responseMessage
     * @param delim
     * @return responseList
     */
    protected ArrayList<String> parseResponseMessage(String responseMessage, String delim)
    {
        ArrayList<String> responseList =
                new ArrayList<String>(Arrays.asList(responseMessage.split(delim)));

        return responseList;

    }

    /**
     * Retrieve the response transaction type
     * GDYNAbstractFIPAYFormatter
     * 
     * @param fipayResponse
     * @return
     */
    protected String retrieveMessageResponseTransactionType(String fipayResponse)
    {
        String messageResponseTransactionType = null;

        ArrayList<String> responseList = parseResponseMessage(fipayResponse, COMMA);
        messageResponseTransactionType = responseList.get(IX_CREDIT_DEBIT_TRANSACTION_TYPE_INDEX);

        return messageResponseTransactionType;
    }

    /**
     * Retrieves the transaction type to determine what value to put into the request.
     * GDYNFIPAYCardAuthFormatter
     * String
     * 
     * @param authRequest
     * @return transaction type as string
     */
    protected String getIxTranType(AuthorizeTransferRequestIfc authRequest)
    {
        int transType = authRequest.getAuthorizationTransactionType();
        String result = "";

        switch (transType)
        {
            case AuthorizationConstantsIfc.TRANS_SALE:
            {
                result = SALE;
                break;
            }
            case AuthorizationConstantsIfc.TRANS_CREDIT:
            {
                result = REFUND;
                break;
            }
            case AuthorizationConstantsIfc.TRANS_VOID:
            case AuthorizationConstantsIfc.TRANS_CREDIT_VOID:
            {
                result = VOID;
                break;
            }
            default:
            {
                logger.error("Unknown transction type for authorization: " + transType);
                break;
            }

        }
        return result;
    }

    /**
     * Returns either the 'credit' or 'debit' value in the message depending
     * on the tender type.
     * GDYNAbstractFIPAYFormatter
     * 
     * @param authRequest
     * @return string
     */
    protected String getIxDebitCreditGiftCard(AuthorizeTransferRequestIfc authRequest)
    {
        TenderType tenderType = authRequest.getRequestTenderType();
        String result = "";

        if (TenderType.CREDIT.equals(tenderType))
        {
            result = CREDIT;
        }
        else if (TenderType.DEBIT.equals(tenderType))
        {
            result = DEBIT;
        }
        else if (TenderType.GIFT_CARD.equals(tenderType))
        {
            result = GIFT_CARD;
        }
        else
        {
            logger.error("Unknown tender type for authorization: " + tenderType);
        }
        return result;
    }

    /**
     * Retrieves the transaction type and determines what will be put into the field.
     * - If it is a VOID transaction it will set the original IxInvoice value sent to FIPAY
     * during the sale. This value will be stored in the Journal Key / Session ID.
     * 
     * GDYNFIPAYCardAuthFormatter
     * 
     * @param authRequest
     * @return account as string
     */
    protected String getIxAccount(AuthorizeTransferRequestIfc authRequest)
    {
        int transType = authRequest.getAuthorizationTransactionType();
        String result = "";

        if (transType == AuthorizationConstantsIfc.TRANS_VOID
                || transType == AuthorizationConstantsIfc.TRANS_CREDIT_VOID)
        {
            result = authRequest.getSessionID();
        }
        return result;
    }

    /**
     * Sets the unique sequence number based off of the transaction id and a
     * sequence number that is generated.
     * GDYNFIPAYCardAuthFormatter
     * 
     * @param authRequest
     * @return invoice number as string
     */
    protected String getIxInvoice(AuthorizeTransferRequestIfc authRequest)
    {
        String nextSequence = getFormatUtils().formatNextSequence(getSequencer(), authRequest);
        return authRequest.getTransactionID().concat(nextSequence);
    }

    /**
     * Sets the unique sequence number based off of the transaction id and a
     * sequence number that is generated.
     * GDYNFIPAYCardAuthFormatter
     * 
     * @param authRequest
     * @return invoice number as string
     */
    protected String getIxInvoiceWithoutTransactionID(AuthorizeTransferRequestIfc authRequest)
    {
        String nextSequence = getFormatUtils().formatNextSequenceWithoutTransactionID(getSequencer(), authRequest);
        return nextSequence;
    }

    /**
     * Gets the currency amount from the string tokenizer values from the response.
     * GDYNAbstractFIPAYFormatter
     * 
     * @param amount
     * @return
     */
    protected CurrencyIfc translateCurrencyAmount(String amount)
    {
        CurrencyIfc currencyAmount = getFormatUtils().translateCurrency(amount);
        return currencyAmount;
    }

    /**
     * Get the date only from the string tokenizer.
     * GDYNAbstractFIPAYFormatter
     * 
     * @param date
     * @return
     */
    protected EYSDate getDateOnly(String date)
    {
        EYSDate dateOnly = getFormatUtils().getDate(date);
        return dateOnly;
    }

    /**
     * Get the time only from the string tokenizer.
     * GDYNAbstractFIPAYFormatter
     * 
     * @param date
     * @return
     */
    protected EYSDate getTimeOnly(String time)
    {
        EYSDate timeOnly = getFormatUtils().getTime(time);
        return timeOnly;
    }

    /**
     * Returns a boolean value of true if SAFable appears in the response.
     * GDYNAbstractFIPAYFormatter
     * 
     * @param nextToken
     * @return isSAFEligible
     */
    protected boolean getSAFEligible(String safAble)
    {
        boolean isSAFEligible = false;

        if (safAble.equals(SAF_ABLE))
        {
            isSAFEligible = true;
        }

        return isSAFEligible;
    }

    /**
     * Map the action code returned by FIPay to the Response Codes per APF
     * GDYNAbstractFIPAYFormatter
     * 
     * @param IxActionCode
     * @param authResponse
     * @return
     */
    protected ResponseCode translateActionCode(String IxActionCode)
    {
        ResponseCode responseCode = ResponseCode.Unknown;

        responseCode = getFormatUtils().translateActionCode(IxActionCode);
        logger.debug("Translated Action Code: FIPAY = " + IxActionCode +
                ", ResponseCode = " + responseCode.toString());
        return responseCode;
    }

    /**
     * Map the IxDebitCredit returned by FIPay to the appropriate tender type.
     * GDYNAbstractFIPAYFormatter
     * 
     * @param IxDebitCredit
     * @param authResponse
     * @return tenderType
     */
    protected TenderType translateTenderType(String IxDebitCredit)
    {
        TenderType tenderType = null;

        tenderType = getFormatUtils().translateTenderType(IxDebitCredit);

        return tenderType;
    }

    /**
     * 
     * GDYNAbstractFIPAYFormatter
     * StringBuffer
     * 
     * @param authRequest
     * @return
     */
    protected StringBuffer formatTenderAmount(AuthorizeTransferRequestIfc authRequest, StringBuffer creditMessage)
    {
        String tenderAmount = getFormatUtils().formatTenderAmount(authRequest);

        creditMessage.append(tenderAmount).append(COMMA);
        return creditMessage;
    }

    /**
     * Splits a receipt text string into 41 character lines.
     * 
     * @param receiptText
     * @return
     */
    protected String[] formatReceiptText(String receiptText)
    {
        String[] result = new String[0];

        if (receiptText != null && receiptText.length() > 0)
        {
            ArrayList<String> receiptLines = new ArrayList<String>();
            String line = new String();

            while (receiptText.length() >= RECEIPT_LINE)
            {
                line = receiptText.substring(0, RECEIPT_LINE);
                receiptText = receiptText.substring(RECEIPT_LINE);
                receiptLines.add(line);
            }
            result = receiptLines.toArray(result);
        }
        return result;
    }

    /**
     * Creates a string list populated with empty strings.
     * 
     * @param size
     *            the size of the list
     * @return list of empty strings
     */
    protected List<String> fillList(int size)
    {
        List<String> requestList = getFormatUtils().fillList(size);
        return requestList;
    }

    /**
     * Checks the IxTranType and sets the currency to either a negative or a positive amount.
     * GDYNAbstractFIPAYFormatter
     * CurrencyIfc
     * 
     * @param IxTranType
     * @param IxAmount
     * @return
     */
    protected CurrencyIfc determineSaleRefund(String IxTranType, CurrencyIfc IxAmount)
    {
        if (IxTranType.equals(REFUND))
        {
            IxAmount = IxAmount.negate();
        }
        return IxAmount;
    }

    /**
     * Formats the common data fields for an AJB request.
     * 
     * @param requestList
     * @param authRequest
     * @return
     */
    protected List<String> formatCommonRequest(List<String> requestList,
            AuthorizeTransferRequestIfc authRequest)
    {
        if (authRequest != null)
        {
            // Field 1: IxTransactionType (M) -- filled in by specific request formatter
            // Field 2: Reserved
            // Field 3: Reserved
            // Field 4: IxAction Code (R) - Used only for the response

            // Field 5: IxTimeOut (M)
            requestList.set(IX_CREDIT_DEBIT_TIME_OUT_INDEX, getTimeOutInSeconds());

            // Field 6: IxDebitCredit (M)
            requestList.set(IX_CREDIT_DEBIT_INDEX, getIxDebitCreditGiftCard(authRequest));

            // Field 7: Reserved

            // Field 8: IxStoreNumber (M)
            requestList.set(IX_CREDIT_DEBIT_STORE_NUMBER_INDEX, authRequest.getStoreID());

            // Field 9: IxTerminalNumber (M)
            requestList.set(IX_CREDIT_DEBIT_TERMINAL_NUMBER_INDEX, authRequest.getWorkstationID());

            // Field 10: IxTranType (M) - filled in by specific request formatter
            // Field 11: Reserved
            // Field 12: Reserved
            // Field 13: IxAccount (C) used only for manual entry
            // Field 14: IxExpDate (C) - used only for manual entry
            // Field 15: IxSwipe (C) - Not used -- swiping is on the device only.
            // Field 16: IxAmount (M) - filled in by specific request formatter
            // Field 17: IxInvoice (M) -- filled in by specific request formatter
        }
        return requestList;
    }

    /**
     * Populates the response object with data from the AJB response fields.
     * 
     * @param responseList
     *            list of response fields
     * @param authResponse
     *            the response object
     * @return the populated auth response
     */
    protected GDYNAuthorizeTransferResponseIfc translateCommonResponse(
            List<String> responseList,
            GDYNAuthorizeTransferResponseIfc authResponse)
    {

        if (responseList == null)
        {
            logger.error("List of response fields is null.");
        }

        else if (authResponse == null)
        {
            logger.error("Transfer response object is null.");
        }

        else if (responseList.size() < COMMON_RESPONSE_FIELD_LENGTH)
        {
            logger.error("List of response fields has insufficient fields. Must have at least 61.");
        }
        else
        {
            // // Field 1: IxTransactionType - (M)
            // String IxTransactionType = responseList.get(IX_CREDIT_DEBIT_TRANSACTION_TYPE_INDEX);

            // Field 4: IxActionCode - (M)
            // Action Codes: 0 = Approved, 1 = Declined, 2 = Call Referral, 3 = Bank Down,
            // 6 = Formatting Error, 8 = Try Later, 10 = Timed Out, 12 = Approved Administration,
            // 14 = MAC Failure, 20 = Error such as Bad Swipe
            String IxActionCode = responseList.get(IX_CREDIT_DEBIT_ACTION_CODE_INDEX);
            ResponseCode responseCode = translateActionCode(IxActionCode);
            authResponse.setResponseCode(responseCode);
            logger.error("Here is the action code:  " + IxActionCode);
            authResponse.setFinancialNetworkStatus(translateFinancialNetworkStatus(authResponse.getResponseCode()));

            // // Field 5: IxTimeOut - (C) Echoed from the request message.
            // String IxTimeOut = responseList.get(IX_CREDIT_DEBIT_TIME_OUT_INDEX);

            // Field 6: IxDebitCredit - (M) Echoed from the request message.
            String IxDebitCredit = responseList.get(IX_CREDIT_DEBIT_INDEX);
            TenderType tenderType = translateTenderType(IxDebitCredit);
            authResponse.setTenderType(tenderType);

            // // Field 7: IxTermId - (O) FiPAY inserts the terminal ID from the storedet.cfg file in the response.
            // String IxTermId = responseList.get(IX_DEBIT_TERM_ID_INDEX);
            //
            // // Field 8: IxStoreNumber - (M) Echoed from the request message.
            // String IxStoreNumber = responseList.get(IX_CREDIT_DEBIT_STORE_NUMBER_INDEX);
            //
            // // Field 9: IxTerminalNumber - (M) Echoed from the request message.
            // String IxTerminalNumber = responseList.get(IX_CREDIT_DEBIT_TERMINAL_NUMBER_INDEX);
            //
            // Field 10: IxTranType - (M) Echoed from the request message.
            String IxTranType = responseList.get(IX_CREDIT_DEBIT_TRAN_TYPE_INDEX);

            // Field 13: IxAccount - (C) Void Transactions original transaction sequence number + generated
            // sequence number or Credit Card Number (So far I am only seeing the Masked CC Number)
            String IxAccount = responseList.get(IX_CREDIT_DEBIT_ACCOUNT_INDEX);
            authResponse.setMaskedAccountNumber(IxAccount);

            // // Field 14: IxExpDate (C) - Manual Entry which is no longer applicable
            // // since swiping is on the device only.
            // String IxExpDate = responseList.get(IX_CREDIT_DEBIT_EXP_DATE_INDEX);
            //
            // // Field 15: IxSwipe (C) - Echoed from the request message.
            // String IxSwipe = responseList.get(IX_CREDIT_DEBIT_SWIPE_INDEX);

            // Field 16: IxAmount (M) - Echoed from the request message.
            CurrencyIfc IxAmount = DomainGateway.getBaseCurrencyInstance();
            IxAmount = translateCurrencyAmount(responseList.get(IX_CREDIT_DEBIT_AMOUNT_INDEX));
            logger.debug("IxAmount from the response is:  " + IxAmount.toFormattedString());
            IxAmount = determineSaleRefund(IxTranType, IxAmount);
            authResponse.setBaseAmount(IxAmount);
            logger.debug("IxAmount is now:  " + IxAmount.toFormattedString());

            // Field 17: IxInvoice (M) - Echoed from the request message.
            String IxInvoice = responseList.get(IX_CREDIT_DEBIT_INVOICE_INDEX);
            authResponse.setJournalKey(IxInvoice);

            // // Field 18: IxTranLanguage - (C) Echoed from the request message.
            // String IxTranLanguage = responseList.get(IX_CREDIT_DEBIT_TRAN_LANG_INDEX);
            //
            // // Field 19: IxForceAuthCode - (C) If the auth code is sent in the request, it will be echoed back
            // // in the response. If FORCED is passed, FIPay will replace the auth code with one generated using
            // // HHMSL.
            // String IxForceAuthCode = responseList.get(IX_CREDIT_DEBIT_FORCE_AUTH_CODE_INDEX);
            //
            // // Field 20: IxOrgAmount - (C) Echoed from the request message.
            // CurrencyIfc IxOrgAmount = translateCurrencyAmount(responseList.get(IX_CREDIT_DEBIT_ORIG_AMOUNT_INDEX));
            //
            // Field 21: IxOptions - (C) Echoed from the request message unless it is EMV in which there will be
            // information regarding the EMV card.
            
            // // Field 24: IxOperator - (C)
            // String IxOperator = responseList.get(IX_CREDIT_DEBIT_OPERATOR_INDEX);
            //
            // // Field 32: IxStoreAddress - (C) Echoed from the request message.
            // String IxStoreAddress = responseList.get(IX_CREDIT_DEBIT_STORE_ADDRESS_INDEX);

            // Field 36: IxSpdhCode - (M) Financial Institutions response code.
            // String IxSpdhCode = responseList.get(IX_CREDIT_DEBIT_SPDH_CODE_INDEX);

            // Field 37: IxAuthCode - (C) Authorization number returned by financial institution for approved
            // transactions only.
            String IxAuthCode = responseList.get(IX_CREDIT_DEBIT_AUTH_CODE_INDEX);
            authResponse.setAuthorizationCode(IxAuthCode);

            // // Field 38: IxReceiptDisplay - (O)
            // String IxReceiptDisplay = responseList.get(IX_CREDIT_DEBIT_RECEIPT_DISPLAY_INDEX);
            //
            // // Field 44: IxPS2000 - (C) Echoed from the request message.
            // String IxPS2000 = responseList.get(IX_CREDIT_DEBIT_CREDIT_PS2000_INDEX);

            // Field 46: IxSeqNumber - (M) Sequence Number returned by financial institution on transaction
            // response.
            String IxSeqNumber = responseList.get(IX_CREDIT_DEBIT_CREDIT_SEQ_NUMBER_INDEX);
            authResponse.setRetrievalReferenceNumber(IxSeqNumber);

            // // Field 47: IxBatchNumber - (C) Contains the batch number, which is a portion of the IxSeqNumber.
            // String IxBatchNumber = responseList.get(IX_CREDIT_DEBIT_CREDIT_BATCH_NUMBER_INDEX);
            //
            // // Field 49: IxLanguage - (C) Echoed from the request message.
            // String IxLanguage = responseList.get(IX_CREDIT_DEBIT_LANGUAGE_INDEX);

            // Field 50: IxDate - (C) Echoed from the request message.
            EYSDate IxDate = getDateOnly(responseList.get(IX_CREDIT_DEBIT_DATE_INDEX));
            authResponse.setLocalDate(IxDate.toFormattedString(locale));

            // Field 51: IxTime - (C) Echoed from the request message.
            EYSDate IxTime = getTimeOnly(responseList.get(IX_CREDIT_DEBIT_TIME_INDEX));
            authResponse.setLocalTime(IxTime.toFormattedString(locale));

            // // Field 52: IxDepositData - (C) Used in EOD to return the FIPay Totals
            // String IxDepositData = responseList.get(IX_CREDIT_DEBIT_DEPOSIT_DATA_INDEX);
            //
            // // Field 53: IxISOResp - (C) Financial Institutions ISO Response Code
            // String IxISOResp = responseList.get(IX_CREDIT_DEBIT_ISO_RESP_INDEX);
            //
            // // Field 61: IxAdditionalMesg - (C) Picked up from Wanline.cfg table in Wansupp based on the action
            // // code.
            // String IxAddtionalMesg = responseList.get(IX_CREDIT_DEBIT_ADDTL_MESSAGE_INDEX);
        }
        logger.debug("Here is the auth response:  " + authResponse.toString());
        return authResponse;
    }

    /**
     * Map Response Codes to the Financial Network Status
     * GDYNAbstractFIPAYFormatter
     * 
     * @param ResponseCode
     * @return int financialNetworkStatus
     */
    protected int translateFinancialNetworkStatus(ResponseCode responseCode)
    {
        int financialStatusNetwork = 0;

        financialStatusNetwork = getFormatUtils().translateFinancialNetworkStatus(responseCode);

        return financialStatusNetwork;
    }

    /**
     * Gets the current balance amount from the string tokenizer values from the response.
     * GDYNAbstractFIPAYFormatter
     * 
     * @param amount
     * @return
     */
    protected CurrencyIfc translateGiftCardCurrentBalance(String amount)
    {
        CurrencyIfc currencyAmount = getFormatUtils().translateGiftCardCurrentBalance(amount);
        return currencyAmount;
    }

    /**
     * Map Action Response Codes to the Gift Card Status
     * GDYNAbstractFIPAYFormatter
     * 
     * @param ResponseCode
     * @return StatusCode statusCode
     */
    protected StatusCode translateGiftCardStatusCode(ResponseCode responseCode)
    {
        StatusCode statusCode = StatusCode.Unknown;

        statusCode = getFormatUtils().translateGiftCardStatusCode(responseCode);

        return statusCode;
    }

    /**
     * Decrypt the gift card number that is encrypted in the system.
     * GDYNFIPAYGiftCardFormatter
     * String
     * 
     * @param encAccountNumber
     * @return
     */
    public String getClearAccountNumber(String encAccountNumber)
    {
        String clear = null;

        clear = getFormatUtils().getClearAccountNumber(encAccountNumber);

        return clear;
    }
}
