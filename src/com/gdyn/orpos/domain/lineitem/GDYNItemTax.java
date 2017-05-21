//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.lineitem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gdyn.orpos.domain.tax.GDYNTaxConstantsIfc;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptCustomerCode;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptExceptionCode;

import oracle.retail.stores.domain.lineitem.ItemTax;
import oracle.retail.stores.domain.tax.AbstractTaxRateCalculator;
import oracle.retail.stores.domain.tax.NewTaxRuleIfc;
import oracle.retail.stores.domain.tax.RunTimeTaxRuleIfc;
import oracle.retail.stores.domain.tax.TaxIfc;

//------------------------------------------------------------------------------
/**
 * Overrides ItemTax to account for GDYN tax exemptions.
 * 
 * @author dteagle
 */
// ------------------------------------------------------------------------------

public class GDYNItemTax extends ItemTax implements GDYNItemTaxIfc, GDYNTaxConstantsIfc
{
    /** serial UID */
    private static final long serialVersionUID = 4727988831539025573L;

    /** the tax product code from the PLU tax data */
    private Integer taxProductCode;

    /** the tax exempt customer code */
    private GDYNTaxExemptCustomerCode taxExemptCustomerCode;

    /** the lookup tax rules from the PLU tax data */
    private NewTaxRuleIfc[] lookupTaxRules;

    /** a map of exception codes */
    private Map<Integer, GDYNTaxExemptExceptionCode> exceptionMap;

    // --------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see com.gdyn.orpos.domain.lineitem.GDYNItemTaxIfc#isException()
     */
    public boolean isException()
    {
        if (exceptionMap != null)
        {
            GDYNTaxExemptExceptionCode code = exceptionMap.get(taxProductCode);

            if (code != null)
            {
                return true;
            }
        }
        return false;
    }

    // --------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gdyn.orpos.domain.lineitem.GDYNItemTaxIfc#setLookupTaxRules(oracle.retail.stores.domain.tax.NewTaxRuleIfc[])
     */
    public void setLookupTaxRules(NewTaxRuleIfc[] rules)
    {
        lookupTaxRules = rules;
    }

    // --------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see com.gdyn.orpos.domain.lineitem.GDYNItemTaxIfc#applyExceptionCodes(java.util.List)
     */
    public void applyExceptionCodes(List<GDYNTaxExemptExceptionCode> codes)
    {
        if (codes != null && !codes.isEmpty())
        {
            exceptionMap = new HashMap<Integer, GDYNTaxExemptExceptionCode>();

            for (GDYNTaxExemptExceptionCode code : codes)
            {
                exceptionMap.put(code.getTaxProductCode(), code);
            }
        }
    }

    // --------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see com.gdyn.orpos.domain.lineitem.GDYNItemTaxIfc#setTaxProductCode(java.lang.String)
     */
    public void setTaxProductCode(Integer aCode)
    {
        taxProductCode = aCode;
    }

    // --------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see com.gdyn.orpos.domain.lineitem.GDYNItemTaxIfc#getTaxExemptCustomerCode()
     */
    public GDYNTaxExemptCustomerCode getTaxExemptCustomerCode()
    {
        return taxExemptCustomerCode;
    }

    // --------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see com.gdyn.orpos.domain.lineitem.GDYNItemTaxIfc#setTaxExemptCustomerCode(com.gdyn.orpos.domain.taxexempt.
     * GDYNTaxExemptCustomerCode)
     */
    public void setTaxExemptCustomerCode(GDYNTaxExemptCustomerCode aCode)
    {
        taxExemptCustomerCode = aCode;
    }

    // --------------------------------------------------------------------------
    /**
     * Overrides ItemTax to check for GDYN tax exemption.
     * 
     * @return an array of tax rules
     */
    public RunTimeTaxRuleIfc[] getActiveTaxRules()
    {
        if (getTaxMode() == GDYNTaxConstantsIfc.TAX_MODE_EXEMPT ||
                getTaxMode() == GDYNTaxConstantsIfc.TAX_MODE_PARTIAL_EXEMPT)
        {
            return generateExemptTaxRules();
        }
        else
        {
            return super.getActiveTaxRules();
        }
    }

    // --------------------------------------------------------------------------
    /**
     * Clears the customer tax exempt data from the item tax object.
     */
    public void clearCustomerTaxExemption()
    {
        lookupTaxRules = null;
        taxProductCode = -1;
        taxExemptCustomerCode = null;
        exceptionMap = null;
    }

    // --------------------------------------------------------------------------
    /**
     * Creates the exempt tax rules based off of the tax rules passed
     * in from the PLU lookup.
     * 
     * @return an array of tax rules
     */
    protected RunTimeTaxRuleIfc[] generateExemptTaxRules()
    {
        List<RunTimeTaxRuleIfc> result = new ArrayList<RunTimeTaxRuleIfc>();
        
        // if the item is an exception to the exemption, skip over everything
        // and return null.
        if (!isException())
        {
            if (lookupTaxRules != null)
            {
                // tax mode exempt = customer code with an application
                // method of "Full"
                if (getTaxMode() == TAX_MODE_EXEMPT)
                {
                    // if the tax mode is exempt, the tax rate is
                    // set to zero for all of the tax rules (GST, PST, etc).
                    for (NewTaxRuleIfc rule : lookupTaxRules)
                    {
                        adjustTaxRate(rule, BigDecimal.ZERO);
                        result.add(rule);
                    }
                }
                // tax mode partial = customer code with an application
                // method of "Partial"
                else if (getTaxMode() == TAX_MODE_PARTIAL_EXEMPT)
                {
                    // if there is only one PLU tax rule (HST), then we
                    // override the rate with the one from the customer
                    // code.
                    if (lookupTaxRules.length == 1 && taxExemptCustomerCode != null)
                    {
                        adjustTaxRate(lookupTaxRules[0], taxExemptCustomerCode.getPartialTaxRate());
                        result.add(lookupTaxRules[0]);
                    }
                    // if there are two PLU tax rules, then we keep the first
                    // one (GST) and set the second one (PST) to zero.
                    else if (lookupTaxRules.length == 2)
                    {
                    	//Ashwini added OR condition to fix taxes flipping issue in FR
                        if (lookupTaxRules[0].getTaxRuleName().contains(TAX_RULE_NAME_CONTAINS) ||
                        		lookupTaxRules[0].getTaxRuleName().equals(defaultTaxRules[0].getTaxRuleName()))
                        {
                            result.add(lookupTaxRules[0]);

                            adjustTaxRate(lookupTaxRules[1], BigDecimal.ZERO);
                            result.add(lookupTaxRules[1]);
                        }
                        else
                        {
                            result.add(lookupTaxRules[1]);

                            adjustTaxRate(lookupTaxRules[0], BigDecimal.ZERO);
                            result.add(lookupTaxRules[0]);
                        }
                    }
                }
            }
        }
        if (result.size() > 0)
        {
            return result.toArray(new RunTimeTaxRuleIfc[result.size()]);
        }
        // for all other cases not explicitly covered here, returning null
        // will make the line item use the normal tax rates contained in
        // the PLU item.
        return null;
    }

    // --------------------------------------------------------------------------
    /**
     * Changes the tax rate in the rule's tax calculator.
     * 
     * @param rule
     *            the tax rule
     * @param newRate
     *            the new rate
     */
    protected void adjustTaxRate(NewTaxRuleIfc rule, BigDecimal newRate)
    {
        if (rule.getTaxCalculator() != null && rule.getTaxCalculator() instanceof AbstractTaxRateCalculator)
        {
            if (newRate == null)
            {
                newRate = BigDecimal.ZERO;
            }
            ((AbstractTaxRateCalculator) rule.getTaxCalculator()).setTaxRate(newRate);
        }
    }

    // --------------------------------------------------------------------------
    /**
     * Makes a clone of this object.
     * 
     * @return the clone
     */
    public Object clone()
    {
        GDYNItemTax myClone = new GDYNItemTax();
        setCloneAttributes(myClone);
        return myClone;
    }

    // --------------------------------------------------------------------------
    /**
     * Copies this object's attributes into a cloned version.
     * 
     * @param newClass
     */
    protected void setCloneAttributes(GDYNItemTax newClass)
    {
        super.setCloneAttributes(newClass);

        if (taxProductCode != null)
            newClass.setTaxProductCode(taxProductCode);

        if (lookupTaxRules != null)
        {
            newClass.setLookupTaxRules(lookupTaxRules.clone());
        }
        if (taxExemptCustomerCode != null)
        {
            GDYNTaxExemptCustomerCode codeClone =
                    (GDYNTaxExemptCustomerCode) taxExemptCustomerCode.clone();

            newClass.setTaxExemptCustomerCode(codeClone);

            newClass.applyExceptionCodes(codeClone.getExceptionCodes());
        }
    }
    
    /**
     * Method to default display string function.
     *
     * @return String representation of object
     * 
     */
  public String toString()
    {
        StringBuilder strResult = new StringBuilder("Class:  ItemTax  " + hashCode());
        strResult.append("\n\tDefault Rate:       [").append(defaultRate).append("]");
        strResult.append("\n\tDefault Tax Rules:  [").append(defaultTaxRules).append("]");
        strResult.append("\n\tOverride Rate:      [").append(overrideRate).append("]");
        //02/05/14 Pak put the condition below to make sure it is not null or will cause exception 
        // and not able to save transaction to CO
        if(overrideAmount != null)
            strResult.append("\n\tOverride Amount:    [").append(overrideAmount.toString()).append("]");
        strResult.append("\n\tReason:             [").append(reason).append("]");
        strResult.append("\n\tToggle:             [").append(taxToggle).append("]");
        strResult.append("\n\tTax Mode:           [").append(TaxIfc.TAX_MODE_DESCRIPTOR[taxMode]).append("]");
        strResult.append("\n\tTax Scope:          [").append(TaxIfc.TAX_SCOPE_DESCRIPTOR[taxScope]).append("]");
        strResult.append("\n\tTaxable:            [").append(taxable).append("]");
        strResult.append("\t\nExternalTaxEnabled: [").append(externalTaxEnabled).append("]");
        strResult.append("\t\nitemTaxAmount:      [").append(itemTaxAmount).append("]");
        strResult.append("\t\nitemInclusiveTaxAmount:      [").append(itemInclusiveTaxAmount).append("]");
        strResult.append("\t\nTaxByTaxJurisdiction: [").append(taxByTaxJurisdiction).append("]");
        // pass back result
        return strResult.toString();
    }
}
