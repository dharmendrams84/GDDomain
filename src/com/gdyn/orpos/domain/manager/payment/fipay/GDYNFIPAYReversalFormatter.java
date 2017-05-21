//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.manager.payment.fipay;

import java.util.List;

import oracle.retail.stores.domain.manager.payment.AuthorizeTransferRequestIfc;
import oracle.retail.stores.domain.manager.payment.ReversalRequestIfc;

import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferResponseIfc;

//------------------------------------------------------------------------------
/**
 * Formatter for FIPAY reversals during cancels and voids.
 * @author dteagle
 */
//------------------------------------------------------------------------------

public class GDYNFIPAYReversalFormatter extends GDYNFIPAYCardAuthFormatter
{

    //--------------------------------------------------------------------------
    /**
     * Default constructor.
     */
    public GDYNFIPAYReversalFormatter()
    {
        super();
    }

    
    //--------------------------------------------------------------------------
    /** 
     * Formats a reversal request into a FIPAY request.
     */
    @Override
    protected List<String> doFormatMessage(AuthorizeTransferRequestIfc authRequest)
    {
        ReversalRequestIfc reversal = (ReversalRequestIfc)authRequest;
        
        List<String> requestList = 
            formatCommonRequest(fillList(REVERSAL_FIELD_LENGTH), reversal);

        // Field 1: IxTransactionType
        requestList.set(IX_CREDIT_DEBIT_TRANSACTION_TYPE_INDEX, 
                        CREDIT_DEBIT_AUTH_REQ_TRANSACTION_TYPE);

        // Field 6: IxDebitCredit - overwrite as 'Void'
        requestList.set(IX_CREDIT_DEBIT_INDEX, VOID);
        
        // Field 10: IxTranType
        requestList.set(IX_CREDIT_DEBIT_TRAN_TYPE_INDEX, getIxTranType(reversal));
        
        // Field 13: IxAccount - original transaction number (journal key)
        requestList.set(IX_CREDIT_DEBIT_ACCOUNT_INDEX, reversal.getJournalKey());
        
        // Field 16: IxAmount 
        requestList.set(IX_CREDIT_DEBIT_AMOUNT_INDEX, 
                        getFormatUtils().formatTenderAmount(reversal));
        
        // Field 17: IxInvoice
        requestList.set(IX_CREDIT_DEBIT_INVOICE_INDEX, getIxInvoice(reversal));
        
        // Field 24: IxOperator
        requestList.set(IX_CREDIT_DEBIT_OPERATOR_INDEX, reversal.getOperatorID());
        
        return requestList;
    }


    //--------------------------------------------------------------------------
    /** 
     * Translates the FIPAY response into a response object.
     */
    @Override
    protected GDYNAuthorizeTransferResponseIfc doTranslateResponse(String messageResponse,
                                                                   GDYNAuthorizeTransferResponseIfc authResponse,
                                                                   GDYNFIPAYResponseIfc fipayResponse)
    {
        // dteagle: as far as I can tell, the super method can handle
        // all of the responses for reversals. Leaving method here in case
        // we find out it can't.
        return super.doTranslateResponse(messageResponse, authResponse, fipayResponse);
    }

}
