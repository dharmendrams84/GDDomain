//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.manager.payment.fipay;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.common.context.BeanLocator;
import oracle.retail.stores.common.utility.Util;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.manager.payment.AuthorizationConstantsIfc;
import oracle.retail.stores.domain.manager.payment.AuthorizationConstantsIfc.TenderType;
import oracle.retail.stores.domain.manager.payment.AuthorizeTransferRequestIfc;
import oracle.retail.stores.domain.manager.payment.PaymentServiceRequestIfc;
import oracle.retail.stores.domain.manager.payment.PaymentServiceResponseIfc.ResponseCode;
import oracle.retail.stores.domain.utility.EYSDate;
import oracle.retail.stores.domain.utility.GiftCardIfc.StatusCode;
import oracle.retail.stores.domain.utility.LocaleConstantsIfc;
import oracle.retail.stores.foundation.manager.ifc.KeyStoreEncryptionManagerIfc;
import oracle.retail.stores.foundation.tour.gate.Gateway;
import oracle.retail.stores.foundation.utility.LocaleMap;

/**
 * Format utility to handle duplicate efforts across all messages.
 * Example: Getting the sequence number for the IxInvoice field
 * 
 * @author mlawrence
 * 
 */
@SuppressWarnings("deprecation")
public class GDYNFIPAYFormatUtils implements GDYNFIPAYFormatUtilsIfc, GDYNFIPAYRequestResponseConstantsIfc
{

    Locale locale = LocaleMap.getLocale(LocaleConstantsIfc.DEFAULT_LOCALE);
    
    public String TOKEN = "~";
    
    private static KeyStoreEncryptionManagerIfc encryptionManager = null;
    
    /**
     * Logger
     */
    public static final Logger logger = Logger.getLogger(GDYNFIPAYFormatUtils.class);


    /**
     * Gets the next sequence number from the ./sequenceNumbers.ser file.
     * GDYNFIPAYFormatUtils
     * 
     * @param sequencer
     * @param request
     * @return nextNumber
     */
    public String formatNextSequence(GDYNFIPAYSequenceNumberGenerator gdynfipaySequenceNumberGenerator,
            PaymentServiceRequestIfc request)
    {
        String key = (new StringBuilder()).append(request.getTransactionID()).toString();
        String nextNumber = gdynfipaySequenceNumberGenerator.getNextNumber(key);
        return nextNumber;
    }

    /**
     * Gets the next sequence number from the ./sequenceNumbers.ser file.
     * GDYNFIPAYFormatUtils
     * 
     * @param sequencer
     * @param request
     * @return nextNumber
     */
    public String formatNextSequenceWithoutTransactionID(
            GDYNFIPAYSequenceNumberGenerator gdynfipaySequenceNumberGenerator,
            PaymentServiceRequestIfc request)
    {
        String key = (new StringBuilder()).append(request.getStoreID()).append(request.getWorkstationID()).toString();
        String nextNumber = gdynfipaySequenceNumberGenerator.getNextNumber(key);
        return nextNumber;
    }

    /**
     * Converts the string token amount returned in the response to a CurrencyIfc value.
     * GDYNFIPAYFormatUtils
     * 
     * @param amount
     * @return
     */
    public CurrencyIfc getCurrency(String amount)
    {
        CurrencyIfc amountCurrency = null;

        if (!amount.isEmpty() && amount != null)
        {
            amountCurrency = DomainGateway.getBaseCurrencyInstance(amount);
        }
        else
        {
            amountCurrency = DomainGateway.getBaseCurrencyInstance();
        }

        return amountCurrency;

    }

    /**
     * Returns the date from a string value retrieved from the response in the string tokenizer.
     * GDYNFIPAYFormatUtils
     * 
     * @param date
     * @return dateOnly
     */
    public EYSDate getDate(String date)
    {

        EYSDate dateOnly = DomainGateway.getFactory().getEYSDateInstance();

        if (!date.isEmpty() && date != null)
        {
            dateOnly.initialize(date, EYSDate.TYPE_DATE_ONLY, locale);
        }
        else
        {
            dateOnly.initialize(EYSDate.TYPE_DATE_ONLY);
        }
        return dateOnly;

    }

    /**
     * Returns the date from a string value retrieved from the response in the string tokenizer.
     * GDYNFIPAYFormatUtils
     * 
     * @param time
     * @return timeOnly
     */
    public EYSDate getTime(String time)
    {

        EYSDate timeOnly = DomainGateway.getFactory().getEYSDateInstance();

        if (!time.isEmpty() && time != null)
        {
            timeOnly.initialize(time, EYSDate.TYPE_TIME_ONLY, locale);
        }
        else
        {
            timeOnly.initialize(EYSDate.TYPE_DATE_TIME);
        }

        return timeOnly;

    }

    /**
     * Map the action code returned by FIPay to the Response Codes per APF
     * GDYNAbstractFIPAYFormatter
     * 
     * @param IxActionCode
     * @param authResponse
     * @return
     */
    public ResponseCode translateActionCode(String IxActionCode)
    {
        ResponseCode responseCode = ResponseCode.Unknown;

        if (IxActionCode.equals(CREDIT_DEBIT_APPROVED))
        {
            responseCode = ResponseCode.Approved;
        }
        else if (IxActionCode.equals(CREDIT_DEBIT_DECLINED))
        {
            responseCode = ResponseCode.Declined;
        }
        else if (IxActionCode.equals(CREDIT_DEBIT_CALL_REFERRAL))
        {
            responseCode = ResponseCode.Referral;
        }
        else if (IxActionCode.equals(CREDIT_DEBIT_BANK_DOWN))
        {
            responseCode = ResponseCode.Offline;
        }
        else if (IxActionCode.equals(CREDIT_DEBIT_FORMATTING_ISSUE))
        {
            responseCode = ResponseCode.ConfigurationError;
        }
        else if (IxActionCode.equals(CREDIT_DEBIT_TRY_AGAIN))
        {
            responseCode = ResponseCode.ErrorOrRetry;
        }
        else if (IxActionCode.equals(CREDIT_DEBIT_TIME_OUT))
        {
            responseCode = ResponseCode.Timeout;
        }
        else if (IxActionCode.equals(CREDIT_DEBIT_ADMIN_APPROVED))
        {
            responseCode = ResponseCode.Approved;
        }
        else if (IxActionCode.equals(CREDIT_DEBIT_MAC_FAILURE))
        {
            responseCode = ResponseCode.MACFailure;
        }
        else if (IxActionCode.equals(CREDIT_DEBIT_ERROR))
        {
            responseCode = ResponseCode.DeviceTimeout;
        }

        return responseCode;
    }

    /**
     * Translate the tender type from the IxDebitCredit. Can be either
     * Credit, Debit or Gift Card.
     * GDYNFIPAYFormatUtils
     * 
     * @param IxDebitCredit
     * @return
     */
    public TenderType translateTenderType(String IxDebitCredit)
    {
        TenderType tenderType = null;

        if (IxDebitCredit.equals(CREDIT))
        {
            tenderType = TenderType.CREDIT;
        }
        else if (IxDebitCredit.equalsIgnoreCase(DEBIT))
        {
            tenderType = TenderType.DEBIT;
        }
        else if (IxDebitCredit.equalsIgnoreCase(GIFT_CARD))
        {
            tenderType = TenderType.GIFT_CARD;
        }

        return tenderType;
    }

    /**
     * Get the request amount from the original request.
     * GDYNFIPAYFormatUtils
     * 
     * @param request
     * @return
     */
    public CurrencyIfc getRequestAmount(AuthorizeTransferRequestIfc request)
    {
        CurrencyIfc requestAmount = DomainGateway.getBaseCurrencyInstance();

        if (request.getAlternateAmount() == null)
        {
            requestAmount = request.getBaseAmount();
        }
        else
        {
            requestAmount = request.getAlternateAmount();
        }

        return requestAmount;
    }

    /**
     * Format the tender amount being sent to FIPAY.
     * GDYNFIPAYFormatUtils
     * 
     * @param request
     * @return
     */
    public String formatTenderAmount(AuthorizeTransferRequestIfc request)
    {
        CurrencyIfc requestAmount = getRequestAmount(request);
        return formatAmount(requestAmount);
    }

    /**
     * Format the tender amount being sent to FIPAY.
     * GDYNFIPAYFormatUtils
     * 
     * @param amount
     * @return
     */
    public String formatAmount(CurrencyIfc amount)
    {
        String unformatted = amount.getStringValue();
        StringBuilder formatted = new StringBuilder();
        if (unformatted != null && unformatted.length() > 0)
        {
            int indexOfDot = unformatted.indexOf(".");
            if (indexOfDot >= 0 && indexOfDot < unformatted.length())
            {
                formatted.append(unformatted.substring(0, indexOfDot));
                formatted.append(unformatted.substring(indexOfDot + 1));
            }
        }
        return formatted.toString();
    }

    /**
     * Translates the response amount.
     * GDYNFIPAYFormatUtils
     * CurrencyIfc
     * 
     * @param amount
     * @return
     */
    public CurrencyIfc translateCurrency(String amount)
    {
        CurrencyIfc aCurrency = DomainGateway.getBaseCurrencyInstance();
        if (!Util.isEmpty(amount))
        {
            boolean negative = amount.startsWith("1") && amount.length() == 11;

            if (negative)
            {
                amount = amount.substring(1);
            }

            if (!negative && amount.contains("-"))
            {
                negative = true;
                if (amount.length() >= amount.indexOf("-"))
                {
                    amount = amount.substring(amount.indexOf("-"));
                }
            }

            StringBuilder sb = new StringBuilder();

            switch (amount.length())
            {
                case 0: // '\0'
                    sb.append("0.00");
                    negative = false;
                    break;

                case 1: // '\001'
                    sb.append("0.0").append(amount);
                    break;

                case 2: // '\002'
                    sb.append("0.").append(amount);
                    break;

                default:
                    sb.append(amount.substring(0, amount.length() - 2)).append(".")
                            .append(amount.substring(amount.length() - 2));
                    break;
            }
            if (negative)
            {
                for (; sb.charAt(0) == '0'; sb.deleteCharAt(0))
                    ;
            }
            aCurrency.setStringValue(sb.toString());

            if (negative)
            {
                aCurrency = aCurrency.negate();
            }
        }
        return aCurrency;
    }

    /**
     * Map Response Codes to the Financial Network Status
     * GDYNAbstractFIPAYFormatter
     * 
     * @param ResponseCode
     * @return int financialNetworkStatus
     */
    public int translateFinancialNetworkStatus(ResponseCode responseCode)
    {
        int financialNetworkStatus = 0;
        
        if (responseCode == ResponseCode.Approved)
        {
            financialNetworkStatus = AuthorizationConstantsIfc.ONLINE;
        }
        else if (responseCode == ResponseCode.Declined)
        {
            financialNetworkStatus = AuthorizationConstantsIfc.ONLINE;
        }
        else if (responseCode == ResponseCode.Referral)
        {
            financialNetworkStatus = AuthorizationConstantsIfc.ONLINE;
        }
        else if (responseCode == ResponseCode.Offline)
        {
            financialNetworkStatus = AuthorizationConstantsIfc.BANK_OFFLINE;
        }
        else if (responseCode == ResponseCode.ConfigurationError)
        {
            financialNetworkStatus = AuthorizationConstantsIfc.ONLINE;
        }
        else if (responseCode == ResponseCode.ErrorOrRetry)
        {
            financialNetworkStatus = AuthorizationConstantsIfc.ONLINE;
        }
        else if (responseCode == ResponseCode.Timeout)
        {
            financialNetworkStatus = AuthorizationConstantsIfc.SWITCH_OFFLINE;
        }
        else if (responseCode == ResponseCode.MACFailure)
        {
            financialNetworkStatus = AuthorizationConstantsIfc.ONLINE;
        }
        else if (responseCode == ResponseCode.DeviceTimeout)
        {
            financialNetworkStatus = AuthorizationConstantsIfc.PAYMENT_APPLICATION_OFFLINE;
        }

        return financialNetworkStatus;
    }

    /**
     * Gets the FIPAY Format Utils instance
     * GDYNFIPAYFormatUtils
     * GDYNFIPAYFormatUtilsIfc
     * 
     * @return
     */
    public static GDYNFIPAYFormatUtilsIfc getInstance()
    {
        return (GDYNFIPAYFormatUtils) BeanLocator.getApplicationBean("application_FIPAYFormatUtils");
    }

    /**
     * Translates the current balance amount.
     * GDYNFIPAYFormatUtils
     * CurrencyIfc
     * 
     * @param amount
     * @return
     */
    public CurrencyIfc translateGiftCardCurrentBalance(String amount)
    {
        CurrencyIfc currentBalance = DomainGateway.getBaseCurrencyInstance();
        StringTokenizer stringToken = new StringTokenizer(amount, TOKEN);
        
        String currentBalanceString = stringToken.nextToken();
        currentBalance = translateCurrency(currentBalanceString);
        
        return currentBalance;
    }

    /**
     * Map Action Response Codes to the Gift Card Status
     * GDYNAbstractFIPAYFormatter
     * 
     * @param ResponseCode
     * @return StatusCode statusCode
     */
    public StatusCode translateGiftCardStatusCode(ResponseCode responseCode)
    {
        StatusCode statusCode = StatusCode.Unknown;
        
        if (responseCode == ResponseCode.Approved)
        {
            statusCode = StatusCode.Active;
        }
        else if (responseCode == ResponseCode.Declined)
        {
            statusCode = StatusCode.Expired;
        }
        else if (responseCode == ResponseCode.Offline)
        {
            statusCode = StatusCode.Timeout;
        }
        else if (responseCode == ResponseCode.Timeout)
        {
            statusCode = StatusCode.Timeout;
        }
        else if (responseCode == ResponseCode.DeviceTimeout)
        {
            statusCode = StatusCode.Timeout;
        }

        return statusCode;
    }

    /**
     * Creates a string list populated with empty strings.
     * 
     * @param size
     *            the size of the list
     * @return list of empty strings
     */
    public List<String> fillList(int size)
    {
        String[] fillArray = new String[size];
        Arrays.fill(fillArray, "");
        return Arrays.asList(fillArray);
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

        if (encryptionManager == null)
        {
            encryptionManager =
                    (KeyStoreEncryptionManagerIfc) Gateway.getDispatcher().getManager(
                            "KeyStoreEncryptionManager");
        }
        try
        {
            byte[] clearBytes = Base64.decodeBase64(encAccountNumber.getBytes());
            clearBytes = encryptionManager.decrypt(clearBytes);
            clear = new String(clearBytes);
            clearBytes = null;
        }
        catch (Exception e)
        {
            logger.error("Encryption manager error.", e);
        }
        return clear;
    }
}
