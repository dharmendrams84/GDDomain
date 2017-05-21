//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.lineitem;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.commerceservices.ixretail.schematypes.Currency;
import oracle.retail.stores.domain.customer.CustomerIfc;
import oracle.retail.stores.domain.discount.AdvancedPricingRuleIfc;
import oracle.retail.stores.domain.discount.DiscountRuleIfc;
import oracle.retail.stores.domain.discount.TransactionDiscountStrategyIfc;
import oracle.retail.stores.domain.employee.EmployeeIfc;
import oracle.retail.stores.domain.lineitem.AbstractTransactionLineItemIfc;
import oracle.retail.stores.domain.lineitem.ItemContainerProxy;
import oracle.retail.stores.domain.lineitem.ItemTaxIfc;
import oracle.retail.stores.domain.lineitem.PriceAdjustmentLineItemIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItem;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.registry.RegistryIDIfc;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.tax.NewTaxRuleIfc;
import oracle.retail.stores.domain.tax.TaxIfc;
import oracle.retail.stores.domain.transaction.TransactionTaxIfc;
import oracle.retail.stores.domain.utility.CodeConstantsIfc;

import com.gdyn.orpos.domain.tax.GDYNTaxConstantsIfc;
import com.gdyn.orpos.domain.transaction.GDYNTransactionTax;
import com.gdyn.orpos.domain.transaction.GDYNTransactionTaxIfc;

//------------------------------------------------------------------------------
/**
 * Extended for Groupe Dynamite customer tax exempt functionality.
 * @author dteagle
 */
//------------------------------------------------------------------------------

public class GDYNItemContainerProxy extends ItemContainerProxy 
                                    implements GDYNItemContainerProxyIfc
{
    /** serial UID */
    private static final long serialVersionUID = -5342757589103245108L;

    //--------------------------------------------------------------------------
    /**
     * Applies a GDYN customer tax exemption.
     * @param newTax a tax object to extract data from
     */
    public void setCustomerTaxExempt(GDYNTransactionTaxIfc newTax)
    {
        GDYNTransactionTaxIfc myTax = (GDYNTransactionTaxIfc)getTransactionTax();
        myTax.setReason(newTax.getReason());
        myTax.setIdExpirationDate(newTax.getIdExpirationDate());
        myTax.setBandRegistryId(newTax.getBandRegistryId());
        myTax.setTaxExemptIdImage(newTax.getTaxExemptIdImage());     
        myTax.setTaxMode(newTax.getTaxMode());
        myTax.setCustomerCategory(newTax.getCustomerCategory());
        
        //Pak not all items
        setTransactionTaxOnItems(false);
    }
    
    //--------------------------------------------------------------------------
    /**
     * Clears a customer tax exemption.
     */
    public void clearCustomerTaxExempt()
    {
        ((GDYNTransactionTaxIfc)getTransactionTax()).clearCustomerTaxExemption();
        
        setTransactionTaxOnItems(true);
    }
    
    //--------------------------------------------------------------------------
    /**
     * Overridden to pass customer tax exempt information down to the
     * item tax object.
     * @param srli the line item
     * @param pItem the PLU item
     * @param qty the item quantity
     */
    @Override
    protected void completeAddPLUItem(SaleReturnLineItemIfc srli,
                                      PLUItemIfc pItem, BigDecimal qty)
    {
        updateCustomerTaxExemption(srli, pItem);
        super.completeAddPLUItem(srli, pItem, qty);
    }

    //--------------------------------------------------------------------------
    /**
     * Set item tax objects based on transaction tax object's value.x
     * @param updateAllItemsFlag flag indicating all items should be updated
     */
    @Override
    public void setTransactionTaxOnItems(boolean updateAllItemsFlag)
    {
        ItemTaxIfc itemTax = null;
        for(AbstractTransactionLineItemIfc lineItem : lineItemsVector)
        {
            if(lineItem instanceof SaleReturnLineItem)
            {
                SaleReturnLineItem srli = (SaleReturnLineItem)lineItem;
            
                int refId = srli.getPriceAdjustmentReference();
                PriceAdjustmentLineItemIfc pali = retrievePriceAdjustmentByReference(refId);
                
                // if it's a sale item or a return item from a non-retrieved
                // transaction, apply the transaction tax
                if (srli.isSaleLineItem() ||
                    (srli.isReturnLineItem() && !srli.getReturnItem().isFromRetrievedTransaction()))
                {
                    itemTax = srli.getItemTax();
                    
                    if (transactionTax.getTaxMode() == GDYNTaxConstantsIfc.TAX_MODE_EXEMPT ||
                        transactionTax.getTaxMode() == GDYNTaxConstantsIfc.TAX_MODE_PARTIAL_EXEMPT)
                    {
                        updateCustomerTaxExemption(srli, srli.getPLUItem());
                        srli.clearTaxAmounts();
                    }
                    else if ((updateAllItemsFlag || itemTax.getTaxMode() == TaxIfc.TAX_MODE_STANDARD)
                            && (itemTax.getTaxMode() != TaxIfc.TAX_MODE_NON_TAXABLE)
                            && itemTax.getTaxMode() != TaxIfc.TAX_MODE_TOGGLE_OFF)
                    {
                        removeCustomerTaxExemption(srli);
                    }
                    if(pali!=null)
                    {
                       pali.setPriceAdjustSaleItem(srli);
                    }
                }
                // if it's a return item from a returned transaction
//                else if (srli.isReturnLineItem())
//                {
//                    itemTax = srli.getItemTax();
//                    
//                    if (transactionTax.getTaxMode() == TaxIfc.TAX_MODE_EXEMPT)
//                    {
//                        itemTax.setOverrideRate(0);
//                        itemTax.setOverrideAmount(zero);
//                        itemTax.getReason().setCode(CodeConstantsIfc.CODE_UNDEFINED);
//                        itemTax.setTaxScope(TaxIfc.TAX_SCOPE_TRANSACTION);
//                        srli.clearTaxAmounts();
//                        itemTax.setOriginalTaxMode(itemTax.getTaxMode());
//                        itemTax.setTaxMode(TaxIfc.TAX_MODE_EXEMPT);
//                    }
//                    if(pali!=null)
//                    {
//                       pali.setPriceAdjustSaleItem(srli);
//                    }
//                }
            }
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Updates the item tax with customer tax exempt information.
     * @param srli a line item
     * @param pItem a PLU item
     */
    protected void updateCustomerTaxExemption(SaleReturnLineItemIfc srli,
                                              PLUItemIfc pItem)
    {
        GDYNItemTaxIfc itemTax = (GDYNItemTaxIfc)srli.getItemTax();
        GDYNTransactionTaxIfc tTax = (GDYNTransactionTaxIfc)transactionTax;
        
        if(pItem.getTaxable() && tTax.hasCustomerTaxExemption())
        {
            itemTax.setOriginalTaxMode(itemTax.getTaxMode());
            itemTax.setTaxMode(tTax.getTaxMode());
            itemTax.setSendTaxRules(null);
            
            // not sure about these yet - dteagle
            //itemTax.setOverrideRate(0);
            //itemTax.setOverrideAmount(tTax.getOverrideAmount());
            //itemTax.getReason().setCode(CodeConstantsIfc.CODE_UNDEFINED);
            //itemTax.setTaxScope(TaxIfc.TAX_SCOPE_TRANSACTION);
            
            itemTax.setLookupTaxRules(pItem.getTaxRules());
            itemTax.setTaxProductCode(pItem.getTaxGroupID());
            itemTax.setTaxExemptCustomerCode(tTax.getCustomerCode());
            itemTax.applyExceptionCodes(tTax.getCustomerExceptions());
        }
    }
    
    //--------------------------------------------------------------------------
    /**
     * Removes the customer tax exempt data from the item tax.
     * @param srli
     */
    protected void removeCustomerTaxExemption(SaleReturnLineItemIfc srli)
    {
        GDYNItemTaxIfc itemTax = (GDYNItemTaxIfc)srli.getItemTax();
        
        itemTax.setTaxToggle(true);
        itemTax.setOverrideRate(transactionTax.getOverrideRate());
        itemTax.setOverrideAmount(transactionTax.getOverrideAmount());
        itemTax.getReason().setCode(CodeConstantsIfc.CODE_UNDEFINED);
        itemTax.setTaxScope(TaxIfc.TAX_SCOPE_TRANSACTION);
        itemTax.setOriginalTaxMode(itemTax.getTaxMode());
        itemTax.setTaxMode(transactionTax.getTaxMode());
        
        itemTax.clearCustomerTaxExemption();
    }
    
    //--------------------------------------------------------------------------
    /**
     * Makes a clone of this object.
     * @return the clone
     */
    public Object clone()
    {
        GDYNItemContainerProxy myClone = new GDYNItemContainerProxy();
        setCloneAttributes(myClone);
        return myClone;
    }
    
    //--------------------------------------------------------------------------
    /**
     * Copies this object's attributes into a cloned version.
     * @param newClass
     */
    protected void setCloneAttributes(GDYNItemContainerProxy newClass)
    {
        //super.setCloneAttributes(newClass);
     // begin setCloneAttributes()
        if (salesAssociate != null)
        {
            newClass.setSalesAssociate((EmployeeIfc)(salesAssociate.clone()));
        }
        newClass.setTransactionTax((TransactionTaxIfc)transactionTax.clone());
        // clone discounts
        TransactionDiscountStrategyIfc[] tds = getTransactionDiscounts();
        if (tds != null)
        {
            TransactionDiscountStrategyIfc[] tclone = new TransactionDiscountStrategyIfc[tds.length];
            for (int i = 0; i < tds.length; i++)
            {
                tclone[i] = (TransactionDiscountStrategyIfc)tds[i].clone();
            }
            newClass.setTransactionDiscounts(tclone);
        }
        // clone line items
        //newClass.setLineItems(cloneLineItems());
        //Pak add this here the clone line items abover will remove the item discount from the base class.
        //02/01/2015
        newClass.setLineItems(lineItemsVector);
        // confirm gift registry exists before cloning
        if (defaultRegistry != null)
        {
            newClass.setDefaultRegistry((RegistryIDIfc)defaultRegistry.clone());
        }
        Iterator<DiscountRuleIfc> i = advancedPricingRules();
        AdvancedPricingRuleIfc rule = null;
        while (i.hasNext())
        {
            rule = (AdvancedPricingRuleIfc)i.next();
            newClass.addAdvancedPricingRule((AdvancedPricingRuleIfc)rule.clone());
        }
        Iterator<DiscountRuleIfc> k = cspAdvancedPricingRules.values().iterator();
        AdvancedPricingRuleIfc csprule = null;
        while (k.hasNext())
        {
            csprule = (AdvancedPricingRuleIfc)k.next();
            newClass.addCSPAdvancedPricingRule((AdvancedPricingRuleIfc)csprule.clone());
        }

        // Clone the itemsByTaxGroup

        Enumeration<String> e = itemsByTaxGroup.keys();
        Hashtable<String, Vector<SaleReturnLineItemIfc>> ht = new Hashtable<String, Vector<SaleReturnLineItemIfc>>();
        SaleReturnLineItemIfc clonedItem = null;
        Vector<SaleReturnLineItemIfc> clonedVector = new Vector<SaleReturnLineItemIfc>();
        while (e.hasMoreElements())
        {
            String itemTaxGroup = e.nextElement();
            for (int j = 0; j < itemsByTaxGroup.get(itemTaxGroup).size(); j++)
            {
                clonedItem = (SaleReturnLineItemIfc)itemsByTaxGroup.get(itemTaxGroup).elementAt(j).clone();
                clonedVector.addElement(clonedItem);

            }
            ht.put(itemTaxGroup, clonedVector);
            clonedVector.clear();

        }
        newClass.setItemsByTaxGroup(ht);

        // confirm amountLineVoids exists before cloning
        if (amountLineVoids != null)
        {
            newClass.setAmountLineVoids((CurrencyIfc)amountLineVoids.clone());
        }
        // confirm unitsLineVoids exists before cloning
        if (unitsLineVoids != null)
        {
            newClass.setUnitsLineVoids(unitsLineVoids);
        }
        newClass.associateKitComponentsWithHeaders();
        if (customer != null)
        {
            newClass.linkCustomer((CustomerIfc)customer.clone());
        }
    }
    
    // Begin GD-303: changing tax indicator on a non-taxable item if non-taxable item added after tax exempt flow
    // lcatania (Starmount) Apr 30, 2013
    /**
     * Constructs item tax object based on transaction values.
     *
     * @param rate default tax rate
     * @param taxRules Default tax rules
     * @param taxable item taxable status
     * @return new ItemTax object
     */
    public ItemTaxIfc initializeItemTax(double rate, NewTaxRuleIfc[] taxRules, boolean taxable)
    {
        // construct item tax object based on transaction values
        ItemTaxIfc it = super.initializeItemTax(rate, taxRules, taxable);
        it.setTaxMode(TaxIfc.TAX_MODE_STANDARD);
        it.setTaxable(taxable);
        return (it);
    }
    // End GD-303: changing tax indicator on a non-taxable item if non-taxable item added after tax exempt flow
}
