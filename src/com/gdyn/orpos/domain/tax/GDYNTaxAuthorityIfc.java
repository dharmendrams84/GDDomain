package com.gdyn.orpos.domain.tax;

import java.io.Serializable;

public interface GDYNTaxAuthorityIfc extends Serializable
{
	public int getId();
	public void setId(int id);
	public String getJurisdictionTypeCode();
	public void setJurisdictionTypeCode(String jurisdictionTypeCode);
	public String getName();
	public void setName(String name);
	public int getRoundingCode();
	public void setRoundingCode(int roundingCode);
	public int getRoundingDigitQuantity();
	public void setRoundingDigitQuantity(int roundingDigitQuantity);
}