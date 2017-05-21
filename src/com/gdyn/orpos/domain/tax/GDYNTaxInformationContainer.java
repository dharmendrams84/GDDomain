package com.gdyn.orpos.domain.tax;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.factory.DomainObjectFactoryIfc;
import oracle.retail.stores.domain.tax.TaxConstantsIfc;
import oracle.retail.stores.domain.tax.TaxInformationContainer;
import oracle.retail.stores.domain.tax.TaxInformationIfc;

public class GDYNTaxInformationContainer
  extends TaxInformationContainer
  implements GDYNTaxInformationContainerIfc, TaxConstantsIfc
{
  static final long serialVersionUID = -3294495793913012219L;
  
  public Object clone()
  {
    GDYNTaxInformationContainer newClass = new GDYNTaxInformationContainer();
    setCloneAttributes(newClass);
    return newClass;
  }
  
  public void setCloneAttributes(GDYNTaxInformationContainer newClass)
  {
    super.setCloneAttributes(newClass);
  }
  
  public TaxInformationIfc[] getTaxInformationGroupedByTaxAuthORPer()
  {
    List<TaxInformationIfc> newTaxInfoList = null;
    TaxInformationIfc[] newTaxInfoArray = (TaxInformationIfc[])null;
    TaxInformationIfc[] taxInfoArray = getTaxInformation();
    if ((taxInfoArray != null) && (taxInfoArray.length > 0))
    {
      newTaxInfoList = new ArrayList(taxInfoArray.length);
      for (TaxInformationIfc taxInfo : taxInfoArray)
      {
        boolean duplicate = false;
        for (int i = 0; (i < newTaxInfoList.size()) && (!duplicate); i++)
        {
          /*duplicate = (((TaxInformationIfc)newTaxInfoList.get(i)).getTaxAuthorityID() == taxInfo.getTaxAuthorityID()) && 
            (((TaxInformationIfc)newTaxInfoList.get(i)).getTaxPercentage().equals(taxInfo.getTaxPercentage()));*/
          //Pak commented out the above getTaxPercentage condition because there are two HST tax line items 
          // and one with 15% and one with 0%. However the tax code are the same, therefore, we want to
          // eliminated the 0% tax line
          duplicate = (((TaxInformationIfc)newTaxInfoList.get(i)).getTaxAuthorityID() == taxInfo.getTaxAuthorityID());
          if ((duplicate) && 
            (((TaxInformationIfc)newTaxInfoList.get(i)) != null))
          {
            CurrencyIfc newTaxAmount = DomainGateway.getBaseCurrencyInstance();
            newTaxAmount = ((TaxInformationIfc)newTaxInfoList.get(i)).getTaxAmount().add(taxInfo.getTaxAmount());
            ((TaxInformationIfc)newTaxInfoList.get(i)).setTaxAmount(newTaxAmount);
          }
        }
        //if it is not duplicate, is not full exemption rule  and is not partial exemption rule 03/31/2015
        if (!duplicate && !taxInfo.getTaxRuleName().equalsIgnoreCase("Tax exempt tax toggle off")
                && !taxInfo.getTaxRuleName().equalsIgnoreCase("Item Tax override by percent"))
        {
          GDYNTaxInformationIfc newTaxInfo = (GDYNTaxInformationIfc)DomainGateway.getFactory().getTaxInformationInstance();
          newTaxInfo.setTaxAuthorityID(taxInfo.getTaxAuthorityID());
          newTaxInfo.setTaxRuleName(taxInfo.getTaxRuleName());
          newTaxInfo.setTaxPercentage(taxInfo.getTaxPercentage());
          newTaxInfo.setTaxAmount(taxInfo.getTaxAmount());
          if ((taxInfo instanceof GDYNTaxInformationIfc)) {
            newTaxInfo.setTranslatedJurisdictionCodes(((GDYNTaxInformationIfc)taxInfo).getTranslatedJurisdictionCodes());
          }
          newTaxInfoList.add(newTaxInfo);
        }
      }
      if (newTaxInfoList.size() > 0)
      {
        Collections.sort(newTaxInfoList);
        newTaxInfoArray = (TaxInformationIfc[])newTaxInfoList.toArray(new TaxInformationIfc[newTaxInfoList.size()]);
      }
    }
    return newTaxInfoArray;
  }
}
