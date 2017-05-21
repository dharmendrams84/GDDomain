//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.manager.payment.fipay;

import java.util.List;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.domain.manager.payment.AuthorizationConstantsIfc.TenderType;
import oracle.retail.stores.domain.manager.payment.AuthorizeTransferRequestIfc;
import oracle.retail.stores.domain.manager.payment.PaymentServiceRequestIfc;
import oracle.retail.stores.domain.manager.payment.PaymentServiceResponseIfc.ResponseCode;
import oracle.retail.stores.domain.utility.EYSDate;
import oracle.retail.stores.domain.utility.GiftCardIfc.StatusCode;

public interface GDYNFIPAYFormatUtilsIfc
{

    /**
     * Gets the next sequence number from the ./sequenceNumbers.ser file.
     * GDYNFIPAYFormatUtils
     * 
     * @param gdynfipaySequenceNumberGenerator
     * @param request
     * @return nextNumber
     */
    public String formatNextSequence(GDYNFIPAYSequenceNumberGenerator gdynfipaySequenceNumberGenerator,
            PaymentServiceRequestIfc request);

    /**
     * Gets the next sequence number from the ./sequenceNumbers.ser file.
     * GDYNFIPAYFormatUtils
     * 
     * @param sequencer
     * @param request
     * @return nextNumber
     */
    public String formatNextSequenceWithoutTransactionID(
            GDYNFIPAYSequenceNumberGenerator gdynfipaySequenceNumberGenerator, PaymentServiceRequestIfc request);

    /**
     * Converts the string token amount returned in the response to a CurrencyIfc value.
     * GDYNFIPAYFormatUtils
     * 
     * @param amount
     * @return
     */
    public CurrencyIfc getCurrency(String amount);

    /**
     * Returns the date from a string value retrieved from the response in the string tokenizer.
     * GDYNFIPAYFormatUtils
     * 
     * @param date
     * @return dateOnly
     */
    public EYSDate getDate(String date);

    /**
     * Returns the time from a string value retrieved from the response in the string tokenizer.
     * GDYNFIPAYFormatUtils
     * 
     * @param time
     * @return timeOnly
     */
    public EYSDate getTime(String time);

    /**
     * Map the action code returned by FIPay to the Response Codes per APF
     * GDYNAbstractFIPAYFormatter
     * 
     * @param IxActionCode
     * @param authResponse
     * @return
     */
    public ResponseCode translateActionCode(String IxActionCode);

    /**
     * Translate the tender type from the IxDebitCredit. Can be either
     * Credit, Debit or Gift Card.
     * GDYNFIPAYFormatUtils
     * 
     * @param IxDebitCredit
     * @return
     */
    public TenderType translateTenderType(String IxDebitCredit);

    /**
     * Get the request amount from the original request.
     * GDYNFIPAYFormatUtils
     * 
     * @param request
     * @return
     */
    public CurrencyIfc getRequestAmount(AuthorizeTransferRequestIfc request);

    /**
     * Format the tender amount being sent to FIPAY.
     * GDYNFIPAYFormatUtils
     * 
     * @param request
     * @return
     */
    public String formatTenderAmount(AuthorizeTransferRequestIfc request);

    /**
     * Format the tender amount being sent to FIPAY.
     * GDYNFIPAYFormatUtils
     * 
     * @param amount
     * @return
     */
    public String formatAmount(CurrencyIfc amount);

    /**
     * Translates the response amount.
     * GDYNFIPAYFormatUtils
     * CurrencyIfc
     * 
     * @param amount
     * @return
     */
    public CurrencyIfc translateCurrency(String amount);
    
    /**
     * Map Response Codes to the Financial Network Status
     * GDYNAbstractFIPAYFormatter
     * 
     * @param ResponseCode
     * @return int financialNetworkStatus
     */
    public int translateFinancialNetworkStatus(ResponseCode responseCode);

    /**
     * Translates the response current balance amount.
     * GDYNFIPAYFormatUtils
     * CurrencyIfc
     * 
     * @param amount
     * @return
     */
    public CurrencyIfc translateGiftCardCurrentBalance(String amount);

    /**
     * Map Action Response Codes to the Gift Card Status
     * GDYNAbstractFIPAYFormatter
     * 
     * @param ResponseCode
     * @return StatusCode statusCode
     */
    public StatusCode translateGiftCardStatusCode(ResponseCode responseCode);

    /**
     * Creates a string list populated with empty strings.
     * 
     * @param size
     *            the size of the list
     * @return list of empty strings
     */
    public List<String> fillList(int size);
    
    /**
     * Decrypt the gift card number that is encrypted in the system.
     * GDYNFIPAYGiftCardFormatter
     * String
     * 
     * @param encAccountNumber
     * @return
     */
    public String getClearAccountNumber(String encAccountNumber);
}
