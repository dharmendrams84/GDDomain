//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.factory;

import oracle.retail.stores.domain.factory.DomainObjectFactoryIfc;
import oracle.retail.stores.domain.tax.InternalTaxEngineIfc;

import com.gdyn.orpos.domain.arts.GDYNTaxExemptSearchCriteriaIfc;
import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferRequestIfc;
import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferResponseIfc;
import com.gdyn.orpos.domain.transaction.GDYNSaleReturnTransactionIfc;
import com.gdyn.orpos.domain.transaction.GDYNTransactionTotalsIfc;
import com.gdyn.orpos.domain.utility.GDYNGiftCardIfc;

/**
 * Extensions for GDYN modifications
 * - AJB/FIPAY Tender Authorization
 * - Customer Survey Reward
 * - Tax Exemption
 * 
 * @author mlawrence
 * 
 */
public interface GDYNDomainObjectFactoryIfc extends DomainObjectFactoryIfc
{
    /**
     * Extend the base AuthorizeTransferRequest
     */
    public GDYNAuthorizeTransferRequestIfc getAuthorizeTransferRequestInstance();

    /**
     * Extend the base AuthorizeTransferResponse
     */
    public GDYNAuthorizeTransferResponseIfc getAuthorizeTransferResponseInstance();
    
    /**
     * Extend the base SaleReturnTransaction to support Customer Survey Reward.
     */
    public GDYNSaleReturnTransactionIfc getSaleReturnTransactionInstance();  
    
    /**
     * New Tax Exempt search criteria
     */
    public GDYNTaxExemptSearchCriteriaIfc getTaxExemptSearchCriteriaInstance();
    
    public GDYNGiftCardIfc getGiftCardInstance();
    public GDYNTransactionTotalsIfc getTransactionTotalsInstance();
    public InternalTaxEngineIfc getInternalTaxEngineInstance();
     
}
