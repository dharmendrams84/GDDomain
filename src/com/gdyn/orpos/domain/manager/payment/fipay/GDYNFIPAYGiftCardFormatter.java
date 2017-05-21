//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.manager.payment.fipay;

import java.util.ArrayList;
import java.util.List;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.manager.payment.AuthorizationConstantsIfc.TenderType;
import oracle.retail.stores.domain.manager.payment.AuthorizeTransferRequestIfc;
import oracle.retail.stores.domain.manager.payment.AuthorizeTransferRequestIfc.RequestSubType;
import oracle.retail.stores.domain.manager.payment.PaymentServiceResponseIfc.ResponseCode;
import oracle.retail.stores.domain.utility.GiftCardIfc.StatusCode;

import org.apache.log4j.Logger;

import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferRequestIfc;
import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferResponseIfc;

/**
 * @author dteagle
 * 
 */
public class GDYNFIPAYGiftCardFormatter extends GDYNFIPAYCardAuthFormatter
{

    /** logger */
    private static final Logger logger =
            Logger.getLogger(GDYNFIPAYGiftCardFormatter.class);

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

        TenderType tenderType = authRequest.getRequestTenderType();

        if (TenderType.GIFT_CARD.equals(tenderType))
        {
            if (checkForDebitAckReq(authRequest))
            {
                requestList = formatDebitAckResponse(authRequest);
            }
            else
            {
                RequestSubType subType = authRequest.getRequestSubType();
                requestList = formatCommonRequest(fillList(CREDIT_DEBIT_FIELD_LENGTH), authRequest);

                // Field 1: IxTransactionType (M)
                requestList.set(IX_CREDIT_DEBIT_TRANSACTION_TYPE_INDEX,
                        CREDIT_DEBIT_AUTH_REQ_TRANSACTION_TYPE);

                // Field 17: IxInvoice (M) - Transaction Sequence Number + Generated
                // Sequence Number aka Journal Key
                requestList.set(IX_CREDIT_DEBIT_INVOICE_INDEX, getIxInvoice(authRequest));

                switch (subType)
                {
                // activate comes from a scanned card -- account number is
                // decrypted before sending
                    case Activate:
                        requestList.set(IX_CREDIT_DEBIT_TRAN_TYPE_INDEX, GC_ISSUE);
                        requestList.set(IX_CREDIT_DEBIT_ACCOUNT_INDEX,
                                getClearAccountNumber(authRequest.getAccountNumber()));
                        requestList.set(IX_CREDIT_DEBIT_AMOUNT_INDEX,
                                getFormatUtils().formatTenderAmount(authRequest));
                        break;
                    case Deactivate:
                        requestList.set(IX_CREDIT_DEBIT_TRAN_TYPE_INDEX, GC_VOID_ISSUE);
                        requestList.set(IX_CREDIT_DEBIT_ACCOUNT_INDEX,
                                getClearAccountNumber(authRequest.getAccountNumber()));
                        requestList.set(IX_CREDIT_DEBIT_AMOUNT_INDEX,
                                getFormatUtils().formatTenderAmount(authRequest));
                        break;
                    case Inquiry:
                        requestList.set(IX_CREDIT_DEBIT_TRAN_TYPE_INDEX, GC_BALANCE_INQUIRY);
                        requestList.set(IX_CREDIT_DEBIT_ACCOUNT_INDEX,
                                getClearAccountNumber(authRequest.getAccountNumber()));
                        break;
                    case AuthorizeSale:
                        requestList.set(IX_CREDIT_DEBIT_TRAN_TYPE_INDEX, GC_SALE);
                        requestList.set(IX_CREDIT_DEBIT_ACCOUNT_INDEX,
                                getClearAccountNumber(authRequest.getCardData().getEncryptedAcctNumber()));
                        requestList.set(IX_CREDIT_DEBIT_AMOUNT_INDEX,
                                getFormatUtils().formatTenderAmount(authRequest));
                        break;
                    case Redeem:
                        requestList.set(IX_CREDIT_DEBIT_TRAN_TYPE_INDEX, GC_CASH_OUT);
                        requestList.set(IX_CREDIT_DEBIT_ACCOUNT_INDEX,
                                getClearAccountNumber(authRequest.getAccountNumber()));
                        requestList.set(IX_CREDIT_DEBIT_AMOUNT_INDEX,
                                getFormatUtils().formatTenderAmount(authRequest));
                        break;
                    case RedeemVoid:
                        requestList.set(IX_CREDIT_DEBIT_TRAN_TYPE_INDEX, GC_VOID_REDEEM);
                        //becuase we changed Redeem to "CashOut", this will probably need to be changed
                        //to "VoidCashOut" or somesuch. Will need to test first.
                        requestList.set(IX_CREDIT_DEBIT_ACCOUNT_INDEX,
                                getClearAccountNumber(authRequest.getAccountNumber()));
                        requestList.set(IX_CREDIT_DEBIT_AMOUNT_INDEX,
                                getFormatUtils().formatTenderAmount(authRequest));
                        break;
                    case ReloadGiftCard:
                        requestList.set(IX_CREDIT_DEBIT_TRAN_TYPE_INDEX, GC_RELOAD);
                        requestList.set(IX_CREDIT_DEBIT_ACCOUNT_INDEX,
                                getClearAccountNumber(authRequest.getAccountNumber()));
                        requestList.set(IX_CREDIT_DEBIT_AMOUNT_INDEX,
                                getFormatUtils().formatTenderAmount(authRequest));
                        break;
                    case ReloadVoid:
                        requestList.set(IX_CREDIT_DEBIT_TRAN_TYPE_INDEX, GC_VOID_RELOAD);
                        requestList.set(IX_CREDIT_DEBIT_ACCOUNT_INDEX,
                                getClearAccountNumber(authRequest.getAccountNumber()));
                        requestList.set(IX_CREDIT_DEBIT_AMOUNT_INDEX,
                                getFormatUtils().formatTenderAmount(authRequest));
                        break;
                    case VoidGiftCard:
                        requestList.set(IX_CREDIT_DEBIT_TRAN_TYPE_INDEX, GC_VOID_SALE);
                        requestList.set(IX_CREDIT_DEBIT_ACCOUNT_INDEX,
                                getClearAccountNumber(authRequest.getCardData().getEncryptedAcctNumber()));
                        requestList.set(IX_CREDIT_DEBIT_AMOUNT_INDEX,
                                getFormatUtils().formatTenderAmount(authRequest));
                        break;
                    default:
                        logger.error("Unknown request subtype: " + subType);
                        break;
                }
            }
        }
        return requestList;
    }

    /**
     * Translates the Gift Card Response received from the FiPay service.
     * 
     * @param String
     *            messageResponse
     * @param GDYNAuthorizeTransferResponseIfc
     *            authResponse
     * @param GDYNFIPAYResponseIfc
     *            fipayResponse
     * @return GDYNAuthorizeTransferResponseIfc authResponse
     */
    protected GDYNAuthorizeTransferResponseIfc doTranslateResponse(
            String messageResponse,
            GDYNAuthorizeTransferResponseIfc authResponse,
            GDYNFIPAYResponseIfc fipayResponse)
    {
        ArrayList<String> responseFields = parseResponseMessage(messageResponse, COMMA);

        if (responseFields.size() < GIFT_CARD_RESPONSE_FIELD_LENGTH)
        {
            logger.error("Gift card response has less than expected data fields:  " + responseFields.size());
        }
        else
        {
            String responseType = responseFields.get(IX_CREDIT_DEBIT_TRANSACTION_TYPE_INDEX);
            logger.error("Here is the response Type:  " + responseType);

            if (responseType.equals(CREDIT_DEBIT_AUTH_RESP_TRANSACTION_TYPE))
            {
                GDYNAuthorizeTransferRequestIfc authRequest = (GDYNAuthorizeTransferRequestIfc) fipayResponse
                        .getSource();

                authResponse = translateCommonResponse(responseFields, authResponse);
                authResponse = translateGiftCardResponse(responseFields, authResponse, authRequest);
                authResponse.setResponseMessage(messageResponse);
            }
            else if (responseType.equals(DEBITACKREQ_RESPONSE_TRANSACTION_TYPE))
            {

            }
        }
        return authResponse;
    }

    /**
     * Get the Gift Card specific response fields
     * 
     * @param responseFields
     * @param authResponse
     * @param authRequest
     * @return GDYNAuthorizeTransferResponseIfc authResponse
     */
    protected GDYNAuthorizeTransferResponseIfc translateGiftCardResponse(ArrayList<String> responseFields,
            GDYNAuthorizeTransferResponseIfc authResponse, GDYNAuthorizeTransferRequestIfc authRequest)
    {
        if (responseFields == null)
        {
            logger.error("List of response fields is null.");
        }

        else if (authResponse == null)
        {
            logger.error("Transfer response object is null.");
        }

        else if (responseFields.size() < COMMON_RESPONSE_FIELD_LENGTH)
        {
            logger.error("List of response fields has insufficient fields. Must have at least 61.");
        }
        else
        {
            if (authRequest.getEntryMethod() != null)
            {
                authResponse.setEntryMethod(authRequest.getEntryMethod());
            }

            // Field 4: IxActionCode - (M)
            // Action Codes: 0 = Approved, 1 = Declined, 2 = Call Referral, 3 = Bank Down,
            // 6 = Formatting Error, 8 = Try Later, 10 = Timed Out, 12 = Approved Administration,
            // 14 = MAC Failure, 20 = Error such as Bad Swipe
            String IxActionCode = responseFields.get(IX_CREDIT_DEBIT_ACTION_CODE_INDEX);

            // Field 10: IxTranType - (M) Echoed from the request message.
            String IxTranType = responseFields.get(IX_CREDIT_DEBIT_TRAN_TYPE_INDEX);

            // Field 16: IxAmount (M) - Echoed from the request message.
            // Field 52: IxDepositData - (C) Used in EOD to return the FIPay Totals
            CurrencyIfc IxAmount = DomainGateway.getBaseCurrencyInstance();
            // CurrencyIfc IxDepositData = DomainGateway.getBaseCurrencyInstance();

            if (IxTranType.equals(GC_BALANCE_INQUIRY))
            {
                IxAmount = translateCurrencyAmount(responseFields.get(IX_CREDIT_DEBIT_DEPOSIT_DATA_INDEX));
                logger.debug("IxAmount from the response is:  " + IxAmount.toFormattedString());
                authResponse.setCurrentBalance(IxAmount);
                authResponse.setBaseAmount(IxAmount);
                logger.debug("IxAmount is now:  " + IxAmount.toFormattedString());

                ResponseCode responseCode = translateActionCode(IxActionCode);
                StatusCode statusCode = translateGiftCardStatusCode(responseCode);
                authResponse.setStatus(statusCode);
                
                if (ResponseCode.Referral.equals(responseCode))
                {
                    authResponse.setResponseCode(ResponseCode.Inactive);
                }
            }
            else if (IxTranType.equals(GC_SALE) || IxTranType.equals(GC_REDEEM))
            {
                IxAmount = translateCurrencyAmount(responseFields.get(IX_CREDIT_DEBIT_AMOUNT_INDEX));
                logger.debug("IxAmount from the response is:  " + IxAmount.toFormattedString());
                IxAmount = determineSaleRefund(IxTranType, IxAmount);
                authResponse.setBaseAmount(IxAmount);
                
                CurrencyIfc inquiryAmount = authRequest.getBalanceInquiryAmount();
                CurrencyIfc currentBalance = inquiryAmount.subtract(IxAmount);
                authResponse.setCurrentBalance(currentBalance);
                
                logger.debug("IxAmount is now:  " + IxAmount.toFormattedString());
                logger.debug("Current Balance is now:  " + currentBalance.toFormattedString());

                // IxDepositData =
                // translateGiftCardCurrentBalance(responseFields.get(IX_CREDIT_DEBIT_DEPOSIT_DATA_INDEX));
                // logger.debug("IxDepositData from the response is:  " + IxDepositData.toFormattedString());
                // authResponse.setCurrentBalance(IxDepositData);
                // logger.debug("IxDepositData is now:  " + IxAmount.toFormattedString());
            } else if (IxTranType.equals(GC_ISSUE)) {
                ResponseCode responseCode = translateActionCode(IxActionCode);
                StatusCode statusCode = translateGiftCardStatusCode(responseCode);
                authResponse.setStatus(statusCode);
            }
        }

        return authResponse;
    }

    /**
     * Checks for the Debit Acknowledgment Required
     * GDYNFIPAYGiftCardFormatter
     * boolean
     * 
     * @param authRequest
     * @return boolean false
     */
    protected boolean checkForDebitAckReq(AuthorizeTransferRequestIfc authRequest)
    {
        return false;
    }

    /**
     * Formats the Debit Acknowledgment Required field
     * GDYNFIPAYGiftCardFormatter
     * List<String>
     * 
     * @param authRequest
     * @return null
     */
    protected List<String> formatDebitAckResponse(AuthorizeTransferRequestIfc authRequest)
    {
        return null;
    }
}
