package com.gdyn.orpos.domain.tax;

import java.util.HashMap;
import java.util.Locale;

import oracle.retail.stores.domain.tax.TaxInformationIfc;

public interface GDYNTaxInformationIfc extends TaxInformationIfc
{
    public String getTranslatedJurisdictionCode(Locale locale);
	public HashMap<String, String> getTranslatedJurisdictionCodes();
	public void setTranslatedJurisdictionCodes(HashMap<String, String> translatedJurisdictionCodes);
}