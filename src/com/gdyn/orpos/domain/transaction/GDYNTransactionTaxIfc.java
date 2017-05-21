//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.transaction;

import java.util.List;

import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptCustomerCategory;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptCustomerCode;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptExceptionCode;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptIdImage;

import oracle.retail.stores.common.utility._360DateIfc;
import oracle.retail.stores.domain.transaction.TransactionTaxIfc;

//------------------------------------------------------------------------------
/**
 * Interface for Groupe Dynamite transaction tax objects. Includes additional
 * data for customer-based tax exemptions.
 * 
 * @author dteagle
 */
//------------------------------------------------------------------------------

public interface GDYNTransactionTaxIfc extends TransactionTaxIfc
{
    GDYNTaxExemptCustomerCategory getCustomerCategory();
    void setCustomerCategory(GDYNTaxExemptCustomerCategory aCategory);
    
    GDYNTaxExemptCustomerCode getCustomerCode();
    
    List<GDYNTaxExemptExceptionCode> getCustomerExceptions();
    
    _360DateIfc getIdExpirationDate();
    void setIdExpirationDate(_360DateIfc aDate);
    
    String getBandRegistryId();
    void setBandRegistryId(String anId);
    
    void setTaxExemptIdImage(GDYNTaxExemptIdImage image);
    GDYNTaxExemptIdImage getTaxExemptIdImage();
    
    String getTaxExemptIdImageName();
    
    boolean hasCustomerTaxExemption();
    
    void clearCustomerTaxExemption();
}
