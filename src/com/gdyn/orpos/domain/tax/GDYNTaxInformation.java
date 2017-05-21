package com.gdyn.orpos.domain.tax;

import java.util.HashMap;
import java.util.Locale;

import oracle.retail.stores.common.utility.Util;
import oracle.retail.stores.domain.tax.TaxInformation;

public class GDYNTaxInformation extends TaxInformation implements GDYNTaxInformationIfc 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1804105106437938960L;
	private HashMap<String, String> translatedJurisdictionCodes;

    public Object clone()
    {
        TaxInformation newClass = new GDYNTaxInformation();
        setCloneAttributes(newClass);
        return newClass;
    }
	
    /**
     * Set the attributes for a clone <P>
     *
     *  @param newClass  Class to put this information into
     */
    public void setCloneAttributes(GDYNTaxInformation newClass)
    {
    	super.setCloneAttributes(newClass);
    	
    	newClass.setTranslatedJurisdictionCodes(this.translatedJurisdictionCodes);
    }

    public String getTranslatedJurisdictionCode(Locale locale)
    {
    	String returnVal = null;

    	if (locale != null && this.translatedJurisdictionCodes != null) {
    		returnVal = translatedJurisdictionCodes.get(locale.getLanguage());
    	}
    	
    	if (Util.isEmpty(returnVal)) {
    		returnVal = "   ";
    	}
    	
    	return returnVal;
    }
    
	public HashMap<String, String> getTranslatedJurisdictionCodes() {
		return translatedJurisdictionCodes;
	}

	public void setTranslatedJurisdictionCodes(HashMap<String, String> translatedJurisdictionCodes) {
		this.translatedJurisdictionCodes = translatedJurisdictionCodes;
	}

}
