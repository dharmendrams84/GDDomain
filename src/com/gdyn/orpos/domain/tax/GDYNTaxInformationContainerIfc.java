package com.gdyn.orpos.domain.tax;

import oracle.retail.stores.domain.tax.TaxInformationContainerIfc;
import oracle.retail.stores.domain.tax.TaxInformationIfc;

public abstract interface GDYNTaxInformationContainerIfc
  extends TaxInformationContainerIfc
{
  public abstract TaxInformationIfc[] getTaxInformationGroupedByTaxAuthORPer();
}
