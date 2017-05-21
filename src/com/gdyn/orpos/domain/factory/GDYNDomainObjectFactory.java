/* ===========================================================================
 *   Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
 *   All rights reserved.
 *   
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

import oracle.retail.stores.domain.factory.DomainObjectFactory;
import oracle.retail.stores.domain.lineitem.ItemContainerProxyIfc;
import oracle.retail.stores.domain.lineitem.ItemTaxIfc;
import oracle.retail.stores.domain.manager.payment.AuthorizeCallReferralRequestIfc;
import oracle.retail.stores.domain.tax.InternalTaxEngineIfc;
import oracle.retail.stores.domain.tax.TaxInformationContainerIfc;
import oracle.retail.stores.domain.tax.TaxInformationIfc;
import oracle.retail.stores.domain.tender.TenderChargeIfc;
import oracle.retail.stores.domain.tender.TenderDebitIfc;
import oracle.retail.stores.domain.transaction.TransactionTaxIfc;

import com.gdyn.orpos.domain.arts.GDYNTaxExemptSearchCriteria;
import com.gdyn.orpos.domain.arts.GDYNTaxExemptSearchCriteriaIfc;
import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferRequest;
import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferRequestIfc;
import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferResponse;
import com.gdyn.orpos.domain.manager.payment.GDYNAuthorizeTransferResponseIfc;
import com.gdyn.orpos.domain.manager.payment.GDYNCallReferralRequest;
import com.gdyn.orpos.domain.stock.GDYNPLUItemIfc;
import com.gdyn.orpos.domain.tax.GDYNTaxAuthorityIfc;
import com.gdyn.orpos.domain.transaction.GDYNSaleReturnTransactionIfc;
import com.gdyn.orpos.domain.transaction.GDYNTransactionTotalsIfc;
import com.gdyn.orpos.domain.utility.GDYNGiftCardIfc;

/**
 * Extensions for GYDN modifications - AJB/FIPAY Tender Authorization - Customer
 * Survey Reward - Tax Exemption
 * 
 * @author mlawrence
 * 
 */
public class GDYNDomainObjectFactory extends DomainObjectFactory implements
		GDYNDomainObjectFactoryIfc {
	protected String factoryID = "";

	private GDYNI18NDomainObjectFactory gdyni18NDomainObjectFactory = new GDYNI18NDomainObjectFactory();

	/**
	 * Constructor
	 */
	public GDYNDomainObjectFactory() {
		super();
	}

	/**
	 * Constructor calls the super class passing in the locale.
	 * 
	 * @param locale
	 */
	public GDYNDomainObjectFactory(Locale locale) {
		super(locale);
	}

	/**
	 * Extend the base AuthorizeTransferRequest
	 */
	public GDYNAuthorizeTransferRequestIfc getAuthorizeTransferRequestInstance() {
		return new GDYNAuthorizeTransferRequest();
	}

	/**
	 * Extend the base AuthorizeTransferResponse
	 */
	public GDYNAuthorizeTransferResponseIfc getAuthorizeTransferResponseInstance() {
		return new GDYNAuthorizeTransferResponse();
	}

	/**
	 * Overridden to use GDYNCallReferralRequest.
	 */
	@Override
	public AuthorizeCallReferralRequestIfc getAuthorizeCallReferralRequestInstance() {
		return new GDYNCallReferralRequest();
	}

	/**
	 * Overridden to use GDYNReversalRequest.
	 */
	@Override
	// public ReversalRequestIfc getReversalRequestInstance()
	// {
	// return new GDYNReversalRequest();
	// }
	/**
	 * Extend the base SaleReturnTransaction to support Customer Survey Reward. 
	 * Returns an instance of GDYNSaleReturnTransactionIfc class.
	 * 
	 * @return SaleReturnTransactionIfc instance
	 * @see oracle.retail.stores.domain.factory.DomainObjectFactoryIfc#getSaleReturnTransactionInstance()
	 */
	public GDYNSaleReturnTransactionIfc getSaleReturnTransactionInstance() {
		return this.gdyni18NDomainObjectFactory
				.getSaleReturnTransactionInstance(getLocale());
	}

	/**
	 * Extend the base TenderCharge
	 */
	public TenderChargeIfc getTenderChargeInstance() {
		return this.gdyni18NDomainObjectFactory
				.getTenderChargeInstance(getLocale());
	}

	public TenderDebitIfc getTenderDebitInstance() {
		return this.gdyni18NDomainObjectFactory
				.getTenderDebitInstance(getLocale());
	}

	/**
	 * Overridden to return Groupe Dynamite Transaction Tax object.
	 */
	@Override
	public TransactionTaxIfc getTransactionTaxInstance() {
		return this.gdyni18NDomainObjectFactory
				.getTransactionTaxInstance(getLocale());
	}

	/**
	 * Overridden to return Groupe Dynamite item tax object.
	 */
	@Override
	public ItemTaxIfc getItemTaxInstance() {
		return this.gdyni18NDomainObjectFactory.getItemTaxInstance(getLocale());
	}

	/**
	 * Overridden to return Groupe Dynamite item container.
	 * 
	 * @return a GDYNItemContainerIfc instance
	 */
	@Override
	public ItemContainerProxyIfc getItemContainerProxyInstance() {
		return this.gdyni18NDomainObjectFactory
				.getItemContainerProxyInstance(getLocale());
	}

	/**
	 * New GDYN tax exempt search criteria object.
	 */
	public GDYNTaxExemptSearchCriteriaIfc getTaxExemptSearchCriteriaInstance() {
		return new GDYNTaxExemptSearchCriteria();
	}

	// Begin GD-237: GD_Item lookup mismatch columns when viewing items returned
	// in item lookup by item description
	// lcatania (Starmount) Mar 22, 2013
	public GDYNPLUItemIfc getPLUItemInstance() {
		return this.gdyni18NDomainObjectFactory.getPLUItemInstance(getLocale());
	}

	// End GD-237: GD_Item lookup mismatch columns when viewing items returned
	// in item lookup by item description

	// Begin (GD-440) GD_CR 13 - Save Gift Card Authorization and Modify RTLog
	// dmartinez (Starmount) Feb 10, 2014
	public GDYNGiftCardIfc getGiftCardInstance() {
		return this.gdyni18NDomainObjectFactory
				.getGiftCardInstance(getLocale());
	}

	// End (GD-440)

	/**
	 * Returns an instance of TaxInformation
	 *
	 * @return TaxInformation instance
	 * @see oracle.retail.stores.domain.factory.DomainObjectFactoryIfc#getTaxInformationInstance()
	 */
	public TaxInformationIfc getTaxInformationInstance() {
		return this.gdyni18NDomainObjectFactory
				.getTaxInformationInstance(getLocale());
	}

	public GDYNTaxAuthorityIfc getTaxAuthorityInformationInstance() {
		return this.gdyni18NDomainObjectFactory
				.getTaxAuthorityInformationInstance(getLocale());
	}

	public TaxInformationContainerIfc getTaxInformationContainerInstance() {
		return this.gdyni18NDomainObjectFactory
				.getTaxInformationContainerInstance(getLocale());
	}

	// Pak add this
	// 559938 - ORPOS Tax Exemption with Retrieved Transaction - incorrect tax
	// amount
	// 01-22-2015
	public GDYNTransactionTotalsIfc getTransactionTotalsInstance() {
		return this.gdyni18NDomainObjectFactory
				.getTransactionTotalsInstance(getLocale());
	}

	/**
	 * Return an instance of TaxEngineIfc
	 *
	 * @return TaxEngineIfc instance
	 * @see oracle.retail.stores.domain.factory.DomainObjectFactoryIfc#getTaxEngineInstance()
	 */
	public InternalTaxEngineIfc getInternalTaxEngineInstance() {
		return this.gdyni18NDomainObjectFactory
				.getInternalTaxEngineInstance(getLocale());
	}

}
