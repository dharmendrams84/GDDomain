package com.gdyn.orpos.domain.tax;

public class GDYNTaxAuthority implements GDYNTaxAuthorityIfc
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4522940569389647756L;
	
	protected int id;
	protected String jurisdictionTypeCode;
	protected String name;
	protected int roundingCode;
	protected int roundingDigitQuantity;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getJurisdictionTypeCode() {
		return jurisdictionTypeCode;
	}
	public void setJurisdictionTypeCode(String jurisdictionTypeCode) {
		this.jurisdictionTypeCode = jurisdictionTypeCode;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getRoundingCode() {
		return roundingCode;
	}
	public void setRoundingCode(int roundingCode) {
		this.roundingCode = roundingCode;
	}
	public int getRoundingDigitQuantity() {
		return roundingDigitQuantity;
	}
	public void setRoundingDigitQuantity(int roundingDigitQuantity) {
		this.roundingDigitQuantity = roundingDigitQuantity;
	}
	
	
}
