/**
 * 
 */
package com.gdyn.orpos.domain.taxexempt;

import java.io.Serializable;

import com.gdyn.orpos.domain.transaction.GDYNTransactionTaxIfc;

/**
 * @author Mansi
 *
 */
public class GDYNHandleReturnPOJO implements Serializable 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String taxExemptCategoryCode;
	private String storeNum;
	private GDYNTransactionTaxIfc txnTax;
	private boolean populate;
	
	public String getTaxExemptCategoryCode() {
		return taxExemptCategoryCode;
	}
	
	public void setTaxExemptCategoryCode(String taxExemptCategoryCode) {
		this.taxExemptCategoryCode = taxExemptCategoryCode;
	}
	public String getStoreNum() {
		return storeNum;
        
	}
	public void setStoreNum(String storeNum) {
		this.storeNum = storeNum;
	
	}
	public GDYNTransactionTaxIfc getTxnTax() {
		return txnTax;
	}
	public void setTxnTax(GDYNTransactionTaxIfc txnTax) {
		this.txnTax = txnTax;
	}
	public boolean isPopulate() {
		return populate;
	}
	public void setPopulate(boolean populate) {
		this.populate = populate;
	}
	
}
