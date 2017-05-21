/* ===========================================================================
* Copyright (c) 2004, 2011, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/modules/domain/src/oracle/retail/stores/domain/tax/InternalTaxEngine.java /rgbustores_13.4x_generic_branch/5 2011/09/22 18:34:15 sgu Exp $
 * ===========================================================================
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED    (MM/DD/YY)
 *    pwai       03/25/2015 - extend this class and change the collectTaxRulesAdditems method to retain the
 *                            original tax line item
 *
 * ===========================================================================
 * $Log:
 11   360Commerce 1.10        8/7/2007 11:48:28 AM   Alan N. Sinton  CR 28170:
 *    Removed condition that checked if taxable item is not a Kit Header when
 *    doing a tax exempt tax calculation.
 10   360Commerce 1.9         5/14/2007 6:08:34 PM   Sandy Gu        update
 *    inclusive information in financial totals and history tables
 9    360Commerce 1.8         5/7/2007 2:21:04 PM    Sandy Gu        enhance
 *    shipping method retrieval and internal tax engine to handle tax rules
 8    360Commerce 1.7         4/30/2007 5:38:35 PM   Sandy Gu        added api
 *    to handle inclusive tax
 7    360Commerce 1.6         4/2/2007 5:50:05 PM    Snowber Khan    CR 25856 -
 *     Updating to preserve "tax exempt amount" for record keeping - without
 *    treating it as a charged tax., CR 25856 - Updated to handle exemption of
 *     default tax rules.
 *
 6    360Commerce 1.5         2/13/2006 4:06:25 PM   Edward B. Thorne Merge
 *    from InternalTaxEngine.java, Revision 1.3.1.0
 5    360Commerce 1.4         2/9/2006 4:12:58 PM    Rohit Sachdeva  10589:
 *    Crash being fixed
 4    360Commerce 1.3         1/25/2006 4:11:04 PM   Brett J. Larsen merge
 *    7.1.1 changes (aka. 7.0.3 fixes) into 360Commerce view
 3    360Commerce 1.2         3/31/2005 4:28:24 PM   Robert Pearse
 2    360Commerce 1.1         3/10/2005 10:22:09 AM  Robert Pearse
 1    360Commerce 1.0         2/11/2005 12:11:26 PM  Robert Pearse
 *:
 4    .v700     1.2.1.0     1/6/2006 12:37:30      Deepanshu       CR 6017:
 *    Calculate and save tax exempt
 3    360Commerce1.2         3/31/2005 15:28:24     Robert Pearse
 2    360Commerce1.1         3/10/2005 10:22:09     Robert Pearse
 1    360Commerce1.0         2/11/2005 12:11:26     Robert Pearse
 *
 *
 * ===========================================================================
 */
package com.gdyn.orpos.domain.tax;

import java.util.HashMap;

import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.factory.DomainObjectFactoryIfc;
import oracle.retail.stores.domain.lineitem.TaxLineItemInformationIfc;
import oracle.retail.stores.domain.tax.InternalTaxEngine;
import oracle.retail.stores.domain.tax.RunTimeTaxRuleIfc;
import oracle.retail.stores.domain.tax.TaxConstantsIfc;
import oracle.retail.stores.domain.tax.TaxRuleItemContainer;
import oracle.retail.stores.domain.tax.TaxRuleItemContainerIfc;
import oracle.retail.stores.domain.transaction.TransactionTaxIfc;

/**
 * @version 1.0
 * @created 13-Apr-2004 6:40:27 PM
 */
public class GDYNInternalTaxEngine extends InternalTaxEngine 
{
    // This id is used to tell
    // the compiler not to generate a
    // new serialVersionUID.
    //
    static final long serialVersionUID = 5052346861071900704L;


    /**
     * Default constructor
     */
    public GDYNInternalTaxEngine()
    {
        super();
    }

    /**
     * Collect tax rules into appropriate groups for calculation
     *
     * @param lineItems
     * @param transactionTax
     * @return Array of tax rules
     * @see oracle.retail.stores.domain.tax.InternalTaxEngineIfc#collectTaxRulesAddItems(oracle.retail.stores.domain.lineitem.TaxLineItemInformationIfc[], oracle.retail.stores.domain.transaction.TransactionTaxIfc)
     */
    public TaxRuleItemContainerIfc[] collectTaxRulesAddItems(TaxLineItemInformationIfc[] lineItems,
                TransactionTaxIfc transactionTax)
    {
        DomainObjectFactoryIfc factory = DomainGateway.getFactory();
        HashMap<String,TaxRuleItemContainerIfc> rules = new HashMap<String,TaxRuleItemContainerIfc>();

        RunTimeTaxRuleIfc[] taxRules = transactionTax.getActiveTaxRules();
        boolean haveTransactionTaxRules = false;
        TaxRuleItemContainer[] transactionTaxRuleItemContainer = null;

        if(taxRules != null && taxRules.length > 0)
        {
            haveTransactionTaxRules = true;
            for(int i = 0; i < taxRules.length; i++)
            {
                TaxRuleItemContainerIfc taxRuleItemContainer = factory.getTaxRuleItemContainerInstance();
                taxRuleItemContainer.setTaxRule(taxRules[i]);
                taxRuleItemContainer.setTaxScope(TaxConstantsIfc.TAX_SCOPE_TRANSACTION);
                rules.put(taxRules[i].getUniqueID(), taxRuleItemContainer);
            }
            transactionTaxRuleItemContainer = rules.values().toArray(new TaxRuleItemContainer[0]);
        }

        //?? If have transaction override do have go through
        //still might for returns
        for( int i = 0; i < lineItems.length; i++)
        {
            boolean addRules = true;

            //Pak comment out the following if statement 
            //becuase we want to keep the original tax line item 
            //without the original tax line item, it is causing an issue with RTlog
            //03/25/2015
           /* if(haveTransactionTaxRules)
            {
                addRules = lineItems[i].canTransactionOverrideTaxRules() == false;
            }

            if(addRules)
            {*/
                taxRules = lineItems[i].getActiveTaxRules();
                for(int j = 0; j < taxRules.length; j++)
                {
                    TaxRuleItemContainerIfc testRuleItemContainer = rules.get(taxRules[j].getUniqueID());
                    // Has rule already been used? If not add it
                    if(testRuleItemContainer == null)
                    {
                        testRuleItemContainer = factory.getTaxRuleItemContainerInstance();
                        testRuleItemContainer.setTaxRule(taxRules[j]);
                        testRuleItemContainer.addItem(lineItems[i], taxRules);
                        testRuleItemContainer.setTaxScope(TaxConstantsIfc.TAX_SCOPE_ITEM);
                        rules.put(taxRules[j].getUniqueID(), testRuleItemContainer);
                    }
                    // Otherwise add this line item to be used with the rule
                    else
                    {
                        testRuleItemContainer.addItem(lineItems[i], taxRules);
                    }
                }
            //}
            //else 
                if (haveTransactionTaxRules)
            {
                for(int j = 0; j < transactionTaxRuleItemContainer.length; j++)
                {
                    transactionTaxRuleItemContainer[j].addItem(lineItems[i], transactionTax.getActiveTaxRules());
                }
            }

            lineItems[i].clearTaxAmounts();
        }
        return rules.values().toArray(new TaxRuleItemContainerIfc[0]);
    }

}
