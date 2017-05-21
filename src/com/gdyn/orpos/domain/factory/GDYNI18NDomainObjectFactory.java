/* ===========================================================================
 *   Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
 *   All rights reserved.
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED    (MM/DD/YY)
 *    pwai  01/22/15 - method is added for this GD 559938 - ORPOS Tax Exemption with Retrieved Transaction - incorrect tax amount
 *    
 * ===========================================================================
 */
package com.gdyn.orpos.domain.factory;

import java.util.Locale;

import oracle.retail.stores.domain.factory.I18NDomainObjectFactory;
import oracle.retail.stores.domain.lineitem.ItemContainerProxyIfc;
import oracle.retail.stores.domain.lineitem.ItemTaxIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItem;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.tax.InternalTaxEngineIfc;
import oracle.retail.stores.domain.tax.TaxInformationContainerIfc;
import oracle.retail.stores.domain.tax.TaxInformationIfc;
import oracle.retail.stores.domain.tender.TenderChargeIfc;
import oracle.retail.stores.domain.tender.TenderDebitIfc;
import oracle.retail.stores.domain.transaction.TransactionTaxIfc;

import com.gdyn.orpos.domain.lineitem.GDYNItemContainerProxy;
import com.gdyn.orpos.domain.lineitem.GDYNItemTax;
import com.gdyn.orpos.domain.stock.GDYNPLUItem;
import com.gdyn.orpos.domain.stock.GDYNPLUItemIfc;
import com.gdyn.orpos.domain.tax.GDYNInternalTaxEngine;
import com.gdyn.orpos.domain.tax.GDYNTaxAuthority;
import com.gdyn.orpos.domain.tax.GDYNTaxAuthorityIfc;
import com.gdyn.orpos.domain.tax.GDYNTaxInformation;
import com.gdyn.orpos.domain.tax.GDYNTaxInformationContainer;
import com.gdyn.orpos.domain.tender.GDYNTenderCharge;
import com.gdyn.orpos.domain.tender.GDYNTenderDebit;
import com.gdyn.orpos.domain.transaction.GDYNSaleReturnTransaction;
import com.gdyn.orpos.domain.transaction.GDYNSaleReturnTransactionIfc;
import com.gdyn.orpos.domain.transaction.GDYNTransactionTax;
import com.gdyn.orpos.domain.transaction.GDYNTransactionTotals;
import com.gdyn.orpos.domain.transaction.GDYNTransactionTotalsIfc;
import com.gdyn.orpos.domain.utility.GDYNGiftCardIfc;
import com.gdyn.orpos.domain.utility.GiftCard.GDYNGiftCard;

/**
 * Extensions for GDYN modifications
 * - AJB/FIPAY Tender Authorization
 * - Customer Survey Reward
 * - Tax Exemption
 * - Custom GDYNGiftCard added.
 * 
 * @author mlawrence
 * 
 */
public class GDYNI18NDomainObjectFactory extends I18NDomainObjectFactory
{
    /**
     * Get the GDYNTenderCharge object.
     */
    public TenderChargeIfc getTenderChargeInstance(Locale locale)
    {
        return new GDYNTenderCharge();
    }

    /**
     * Returns a GDYNTenderDebit object.
     */
    public TenderDebitIfc getTenderDebitInstance(Locale locale)
    {
        return new GDYNTenderDebit();
    }

    /**
     * Extend the base SaleReturnTransaction to support Customer Survey Reward.
     * Return a new GDYNSaleReturnTransaction object.
     */
    public GDYNSaleReturnTransactionIfc getSaleReturnTransactionInstance(Locale locale)
    {
        return new GDYNSaleReturnTransaction();
    }

    /**
     * Overridden to return an instance of a GDYNTransactionTaxIfc.
     * @return a transaction tax object
     */
    @Override
    public TransactionTaxIfc getTransactionTaxInstance(Locale locale)
    {
        return new GDYNTransactionTax();
    }

    /**
     * Overridden to return an instance of GDYNItemTax.
     * @param locale a locale object
     * @return an item tax object
     */
    @Override
    public ItemTaxIfc getItemTaxInstance(Locale locale)
    {
        return new GDYNItemTax();
    }

    /**
     * Overridden to return Groupe Dynamite item container.
     * @param locale a locale object
     * @return a GDYNItemContainerIfc instance
     */
    @Override
    public ItemContainerProxyIfc getItemContainerProxyInstance(Locale locale)
    {
        return new GDYNItemContainerProxy();
    }
    
    // Begin GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item description
    // lcatania (Starmount) Mar 22, 2013
    public GDYNPLUItemIfc getPLUItemInstance(Locale locale)
    {
        return new GDYNPLUItem();
    }
    // End GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item description
    
    
    // Begin (GD-440) GD_CR 13 - Save Gift Card Authorization and Modify RTLog
    // dmartinez (Starmount) Feb 10, 2014
    public GDYNGiftCardIfc getGiftCardInstance(Locale locale)
    {
        return new GDYNGiftCard();
    }
    // End (GD-440)
    
    public TaxInformationIfc getTaxInformationInstance(Locale locale)
    {
        return new GDYNTaxInformation();
    }

    public GDYNTaxAuthorityIfc getTaxAuthorityInformationInstance(Locale locale)
    {
    	return new GDYNTaxAuthority();
    }
    
    public TaxInformationContainerIfc getTaxInformationContainerInstance(Locale locale)
    {
      return new GDYNTaxInformationContainer();
    }
  
    //Pak add this 
    //559938 - ORPOS Tax Exemption with Retrieved Transaction - incorrect tax amount
    // 01-22-2015
    public GDYNTransactionTotalsIfc getTransactionTotalsInstance(Locale locale)
    {
        return new GDYNTransactionTotals();
    }
    
    /**
     * Return an instance of TaxEngineIfc
     *
     * @param locale Locale to get an object for.
     * @return TaxEngineIfc instance
     * @see oracle.retail.stores.domain.factory.DomainObjectFactoryIfc#getTaxEngineInstance(java.util.Locale)
     */
    public InternalTaxEngineIfc getInternalTaxEngineInstance(Locale locale)
    {
        return new GDYNInternalTaxEngine();
    }
    
    public SaleReturnLineItemIfc getSaleReturnLineItemInstance(Locale locale){
    	return new SaleReturnLineItem();
    }
}
