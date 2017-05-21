package com.gdyn.orpos.domain.tax;

import com.gdyn.orpos.domain.transaction.GDYNTransactionTotals;

import oracle.retail.stores.domain.lineitem.TaxLineItemInformationIfc;
import oracle.retail.stores.domain.transaction.TransactionTaxIfc;

public interface GDYNInternalTaxEngineIfc
{

    void calculateTax(TaxLineItemInformationIfc[] items, GDYNTransactionTotals gdynTransactionTotals,
            TransactionTaxIfc tax);

}
