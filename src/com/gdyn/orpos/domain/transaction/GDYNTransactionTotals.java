/* ===========================================================================
* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/modules/domain/src/oracle/retail/stores/domain/transaction/TransactionTotals.java /rgbustores_13.4x_generic_branch/10 2012/02/16 17:18:00 mkutiana Exp $
 * ===========================================================================
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED    (MM/DD/YY)
 *    pwai  01/22/15 - this class is added for this GD 559938 - ORPOS Tax Exemption with Retrieved Transaction - incorrect tax amount
 * ===========================================================================
 */
package com.gdyn.orpos.domain.transaction;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.Vector;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.discount.TransactionDiscountStrategyIfc;
import oracle.retail.stores.domain.lineitem.AbstractTransactionLineItemIfc;
import oracle.retail.stores.domain.lineitem.ItemPriceIfc;
import oracle.retail.stores.domain.lineitem.OrderLineItemIfc;
import oracle.retail.stores.domain.lineitem.PriceAdjustmentLineItemIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.lineitem.TaxLineItemInformationIfc;
import oracle.retail.stores.domain.stock.AlterationPLUItemIfc;
import oracle.retail.stores.domain.stock.GiftCardPLUItemIfc;
import oracle.retail.stores.domain.tax.InternalTaxEngineIfc;
import oracle.retail.stores.domain.transaction.TransactionTaxIfc;
import oracle.retail.stores.domain.transaction.TransactionTotals;

import org.apache.log4j.Logger;

import com.gdyn.orpos.domain.factory.GDYNDomainObjectFactory;
import com.gdyn.orpos.domain.tax.GDYNInternalTaxEngineIfc;

/**
 * This class holds and computes the transaction totals based on the line items,
 * the discounts and the taxes.
 *
 * @version $Revision: /rgbustores_13.4x_generic_branch/10 $
 */
public class GDYNTransactionTotals extends TransactionTotals implements GDYNTransactionTotalsIfc
{
    // This id is used to tell the compiler not to generate a new serialVersionUID.
    static final long serialVersionUID = 4731987200275355460L;

    /** Debug logger */
    private static final Logger logger = Logger.getLogger(GDYNTransactionTotals.class);

    /**
     * revision number supplied by source-code control system
     */
    public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/10 $";
    
    protected InternalTaxEngineIfc taxEngine;
    
    public GDYNTransactionTotals()
    {
        taxEngine = ((GDYNDomainObjectFactory)DomainGateway.getFactory()).getInternalTaxEngineInstance();
    }
    /**
     * Updates transaction totals.
     * <B>Pre-Condition(s)</B>
     * <UL>
     * <LI>none
     * </UL>
     * <B>Post-Condition(s)</B>
     * <UL>
     * <LI>transaction totals updated
     * </UL>
     * comment: GD needs to do this on the transaction level and we can only use the computeTaxes method
     * 
     * @param lineItems vector of line items
     * @param discounts array of transaction discounts
     * @param tax transaction tax object
     */
    public void updateTransactionTotals(AbstractTransactionLineItemIfc[] lineItems,
                                        TransactionDiscountStrategyIfc[] discounts,
                                        TransactionTaxIfc tax)
    {

        // local item references
        numItems = lineItems.length;
        // remove order line items that have been canceled
        Vector<AbstractTransactionLineItemIfc> totalableLineItems = new Vector<AbstractTransactionLineItemIfc>();
        // walk current lineItems, add non-canceled and not-price adjustment
        // ones to totalableLineItems
        for (int i = 0; i < numItems; i++)
        {
            // Add the item unless it is a price adjustment instance or a
            // canceled order line item
            if (!(lineItems[i] instanceof PriceAdjustmentLineItemIfc)
                    && (!(lineItems[i] instanceof OrderLineItemIfc) || ((OrderLineItemIfc) lineItems[i])
                            .getItemStatus() != OrderLineItemIfc.ORDER_ITEM_STATUS_CANCELED))
            {
                totalableLineItems.add(lineItems[i]);
            }
        }

        // reset new lineItems size
        numItems = totalableLineItems.size();

        // initialize values
        subtotal.setZero();
        saleSubtotal.setZero();
        returnSubtotal.setZero();
        discountTotal.setZero();
        saleDiscountTotal.setZero();
        saleDiscountAndPromotionTotal.setZero();
        transactionDiscountTotal.setZero();
        itemDiscountTotal.setZero();
        restockingFeeTotal.setZero();
        returnDiscountTotal.setZero();
        taxTotal.setZero();
        inclusiveTaxTotal.setZero();
        taxTotalUI.setZero();
        taxExceptionsTotal.setZero();
        quantityTotal = BigDecimal.ZERO;
        quantitySale = BigDecimal.ZERO;
        taxInformationContainer.reset();

        // calculate sub totals
        calculateSubtotals(totalableLineItems);

        // set grand total
        calculateGrandTotal();

        if (discounts != null)
        {
            if (discounts.length > 0)
            {
                // calculate discounts
                discountCalculator.calculateDiscounts(this, discounts, totalableLineItems);
            }
        }

        // add the discount total to the promotions
        amountOffTotal = amountOffTotal.add(discountTotal);

        // update taxes if not tax exempt
        if (tax != null)
        {
            // update taxes
            // calculateTaxes(totalableLineItems, tax);
            
            //Pak commented out the following and just use for the compute Taxes
          /*if (tax.getTaxMode() == GDYNTaxConstantsIfc.TAX_MODE_EXEMPT
                    || tax.getTaxMode() == GDYNTaxConstantsIfc.TAX_MODE_PARTIAL_EXEMPT)
            {
                
                computeExemptTaxes(totalableLineItems, tax);
            }
            else
            {*/
                computeTaxes(totalableLineItems, tax);
            //}
        }
        else
        {
            throw new NullPointerException("Null TransactionTax in TransactionTotals.updateTransactionTotals()");
        }

        // reset grand total
        calculateGrandTotal();

        // set balance due to grand total
        balanceDue.setStringValue(grandTotal.getStringValue());
        // subtract amount tendered (calculated elsewhere)
        balanceDue = grandTotal.subtract(amountTender);
    }
    /**
     * Calculates subtotals, both overall and by sale or return. Also calculates
     * quantity total.
     * <B>Pre-Condition(s)</B>
     * <UL>
     * <LI>none
     * </UL>
     * <B>Post-Condition(s)</B>
     * <UL>
     * <LI>subtotal calculated
     * <LI>saleSubtotal calculated
     * <LI>returnSubtotal calculated
     * <LI>discountTotal calculated (incremental value)
     * <LI>saleDiscountTotal calculated (incremental value)
     * <LI>saleDiscountAndPromotionTotal calculated (incremental value)
     * <LI>returnDiscountTotal calculated (incremental value)
     * </UL>
     *
     * @param lineItems vector of line items
     */
    protected void calculateSubtotals(Vector<AbstractTransactionLineItemIfc> lineItems)
    {
        // local reference to line item
        SaleReturnLineItemIfc li = null;
        CurrencyIfc extendedSellingPrice = DomainGateway.getBaseCurrencyInstance();
        CurrencyIfc itemDiscountAmount = DomainGateway.getBaseCurrencyInstance();
        Enumeration<AbstractTransactionLineItemIfc> e = lineItems.elements();
        discountEligibleSubtotal.setZero();
        amountOffTotal.setZero();
        // initially assume that all items have unit of measure as units
        setAllItemUOMUnits(true);
        // loop through line items , capturing sale, return subtotals and item
        // discounts
        while (e.hasMoreElements())
        {
            li = (SaleReturnLineItemIfc)e.nextElement();
            if (li.isUnitOfMeasureItem())
            {
                setAllItemUOMUnits(false);
            }

            if (!li.isKitHeader() && !li.isPriceAdjustmentLineItem())
            {
                BigDecimal itemQuantity = li.getItemQuantityDecimal();

                extendedSellingPrice = li.getExtendedSellingPrice();
                itemDiscountAmount = li.getItemDiscountAmount();
                subtotal = subtotal.add(extendedSellingPrice);
                discountTotal = discountTotal.add(itemDiscountAmount);;
                boolean incrementQuantity = true;
                // check if sale or returned item
                if (itemQuantity.signum() > 0)
                {
                    if (li.getPLUItem() != null && li.getPLUItem().hasTemporaryPriceChanges())
                    {
                        // get the permanent price and mutliply it by the
                        // quantity.
                        CurrencyIfc extendedPermanentPrice = li.getItemPrice().getPermanentSellingPrice()
                                .multiply(li.getItemQuantityDecimal()).abs();
                        // subtract the selling price from the full permanent
                        // price
                        amountOffTotal = amountOffTotal
                                .add(extendedPermanentPrice.subtract(extendedSellingPrice.abs()));
                        saleDiscountAndPromotionTotal = saleDiscountAndPromotionTotal
                        .add(extendedPermanentPrice.subtract(extendedSellingPrice.abs()));
                    }
                    
                    saleSubtotal = saleSubtotal.add(extendedSellingPrice);
                    saleDiscountTotal = saleDiscountTotal.add(itemDiscountAmount);
                    saleDiscountAndPromotionTotal = saleDiscountAndPromotionTotal.add(itemDiscountAmount);
                    
                    // reset item transaction discount
                    li.clearTransactionDiscounts();
                    li.recalculateItemTotal();

                    // bump up item quantity
                    if (incrementQuantity && li.isServiceItem())
                    {
                        incrementQuantity = this.isNonMerchandiseQuantityIncremented();
                    }
                    // gift card issue and reload will not count as
                    // units sold
                    // Alterations will not count as units sold
                    if ((li.getPLUItem() instanceof GiftCardPLUItemIfc)
                            || (li.getPLUItem() instanceof AlterationPLUItemIfc))
                    {
                        incrementQuantity = false;
                    }
                    if (incrementQuantity)
                    {
                        quantityTotal = quantityTotal.add(itemQuantity);
                        if (!li.getPLUItem().isStoreCoupon())
                        {
                            quantitySale = quantitySale.add(itemQuantity);
                        }
                    }

                    // if item eligible for discounts, save total
                    if (li.isDiscountEligible())
                    {
                        discountEligibleSubtotal = discountEligibleSubtotal.add(extendedSellingPrice);
                        discountEligibleSubtotal = discountEligibleSubtotal.subtract(itemDiscountAmount);
                    }

                }
                // handle returned item
                else
                {
                    returnSubtotal = returnSubtotal.add(extendedSellingPrice);
                    returnDiscountTotal = returnDiscountTotal.add(itemDiscountAmount);                 

                    
                    // get item quantity totaled, but use absolute value
                    // Non Merchandise Items could be returned, hence
                    // checking
                    if (li.isServiceItem() && !this.isNonMerchandiseQuantityIncremented())
                    {
                        incrementQuantity = false;
                    }
                    if (incrementQuantity)
                    {
                        quantityTotal = quantityTotal.subtract(itemQuantity);
                    }
                }

                // reset item discount total
                li.setItemDiscountTotal(itemDiscountAmount);

                // roll up item discounts
                itemDiscountTotal = itemDiscountTotal.add(itemDiscountAmount);

                // if it is a returned item calculate the restocking fee total
                if (itemQuantity.signum() < 0)
                {
                    ItemPriceIfc itemPrice = li.getItemPrice();
                    if (itemPrice != null)
                    {
                        CurrencyIfc itemExtendedRestockingFee = itemPrice.getExtendedRestockingFee();
                        if (itemExtendedRestockingFee != null)
                        {
                            restockingFeeTotal = restockingFeeTotal.add(itemExtendedRestockingFee);
                        }
                    }
                }
            }
        }

    }
    protected void computeTaxes(Vector<AbstractTransactionLineItemIfc> lineItems, TransactionTaxIfc tax)
    {
        TaxLineItemInformationIfc[] items = lineItems.toArray(new TaxLineItemInformationIfc[lineItems.size()]);
        taxEngine.calculateTax(items, this, tax);
    }
}
