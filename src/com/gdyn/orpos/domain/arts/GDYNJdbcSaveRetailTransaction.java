//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.domain.arts;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.common.sql.SQLInsertStatement;
import oracle.retail.stores.common.sql.SQLUpdatableStatementIfc;
import oracle.retail.stores.domain.arts.JdbcSaveRetailTransaction;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.lineitem.AbstractTransactionLineItemIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.order.OrderConstantsIfc;
import oracle.retail.stores.domain.order.OrderDeliveryDetailIfc;
import oracle.retail.stores.domain.order.OrderRecipientIfc;
import oracle.retail.stores.domain.transaction.LayawayPaymentTransactionIfc;
import oracle.retail.stores.domain.transaction.OrderTransaction;
import oracle.retail.stores.domain.transaction.OrderTransactionIfc;
import oracle.retail.stores.domain.transaction.RetailTransactionIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.domain.transaction.TenderableTransactionIfc;
import oracle.retail.stores.domain.transaction.TransactionConstantsIfc;
import oracle.retail.stores.domain.transaction.TransactionIfc;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.persistence.utility.ARTSDatabaseIfc;

import com.gdyn.orpos.domain.transaction.GDYNSaleReturnTransactionIfc;
import com.gdyn.orpos.persistence.utility.GDYNARTSDatabaseIfc;

import org.apache.log4j.Logger;

/**
 * Extending the base GDYNJdbcSaveRetailTransaction for Groupe Dynamite - CSAT:
 * Customer Survey
 * 
 * @author MSolis
 * 
 */
public class GDYNJdbcSaveRetailTransaction extends JdbcSaveRetailTransaction
		implements GDYNARTSDatabaseIfc {
	private static final long serialVersionUID = -5417950602291052637L;

	/**
	 * The logger to which log messages will be sent.
	 */
	private static final Logger logger = Logger
			.getLogger(GDYNJdbcSaveRetailTransaction.class);

	/*
	 * Constants used in Loyalty transaction table
	 */

	private static final String LOYALTY_EXPORT_FLAG = "0";

	/**
	 * Inserts into the retail_transaction table.
	 * 
	 * @param dataConnection
	 *            the connection to the data source
	 * @param transaction
	 *            The Retail Transaction to update
	 * @exception DataException
	 */
	@Override
	public void insertRetailTransaction(JdbcDataConnection dataConnection,
			TenderableTransactionIfc transaction) throws DataException {
		/*
		 * Insert the transaction in the Transaction table first.
		 */
		insertTransaction(dataConnection, transaction);

		// if a suspended transaction save all applicable advanced pricing rules
		if (transaction.getTransactionStatus() == TransactionIfc.STATUS_SUSPENDED
				&& transaction instanceof SaleReturnTransactionIfc) {

			((SaleReturnTransactionIfc) transaction).clearBestDealDiscounts();
			insertAdvancedPricingRules(dataConnection, transaction);
		}

		SQLInsertStatement sql = new SQLInsertStatement();

		// Table
		sql.setTable(TABLE_RETAIL_TRANSACTION);

		// Fields
		sql.addColumn(FIELD_RETAIL_STORE_ID, getStoreID(transaction));
		sql.addColumn(FIELD_WORKSTATION_ID, getWorkstationID(transaction));
		sql.addColumn(FIELD_TRANSACTION_SEQUENCE_NUMBER,
				getTransactionSequenceNumber(transaction));
		sql.addColumn(FIELD_BUSINESS_DAY_DATE,
				getBusinessDayString(transaction));
		sql.addColumn(FIELD_CUSTOMER_ID, getCustomerID(transaction));
		sql.addColumn(FIELD_SUSPENDED_TRANSACTION_REASON_CODE,
				getSuspendedTransactionReasonCode(transaction));

		addTransactionTotalsColumns((SQLUpdatableStatementIfc) sql, transaction);

		sql.addColumn(FIELD_ENCRYPTED_PERSONAL_ID_NUMBER,
				getEncryptedPersonalIDNumber(transaction));
		sql.addColumn(FIELD_MASKED_PERSONAL_ID_NUMBER,
				getMaskedPersonalIDNumber(transaction));
		sql.addColumn(FIELD_PERSONAL_ID_REQUIRED_TYPE,
				getPersonalIDType(transaction));
		sql.addColumn(FIELD_PERSONAL_ID_STATE, getPersonalIDState(transaction));
		sql.addColumn(FIELD_PERSONAL_ID_COUNTRY,
				getPersonalIDCountry(transaction));
		sql.addColumn(FIELD_RECORD_LAST_MODIFIED_TIMESTAMP,
				getSQLCurrentTimestampFunction());
		sql.addColumn(FIELD_RECORD_CREATION_TIMESTAMP,
				getSQLCurrentTimestampFunction());
		sql.addColumn(FIELD_SEND_PACKAGE_COUNT,
				getSendPackageCount(transaction));

		if (transaction instanceof SaleReturnTransactionIfc) {
			SaleReturnTransactionIfc srTransaction = (SaleReturnTransactionIfc) transaction;
			if (srTransaction.getAgeRestrictedDOB() != null) {
				sql.addColumn(FIELD_AGE_RESTRICTED_DOB,
						getAgeRestrictedDOB(srTransaction));
			}

			if (srTransaction.isSendCustomerLinked()) {
				sql.addColumn(FIELD_SEND_CUSTOMER_TYPE, "'0'");
			} else {
				sql.addColumn(FIELD_SEND_CUSTOMER_TYPE, "'1'");
			}
			if (srTransaction.isCustomerPhysicallyPresent()) {
				sql.addColumn(FIELD_SEND_CUSTOMER_PHYSICALLY_PRESENT, "'1'");
			} else {
				sql.addColumn(FIELD_SEND_CUSTOMER_PHYSICALLY_PRESENT, "'0'");
			}
			if (transaction.getTransactionTotals()
					.isTransactionLevelSendAssigned()) {
				sql.addColumn(FIELD_TRANSACTION_LEVEL_SEND, "'1'");
			} else {
				sql.addColumn(FIELD_TRANSACTION_LEVEL_SEND, "'0'");
			}
			if (srTransaction.isTransactionGiftReceiptAssigned()) {
				sql.addColumn(FIELD_TRANSACTION_LEVEL_GIFT_RECEIPT_FLAG, "'1'");
			} else {
				sql.addColumn(FIELD_TRANSACTION_LEVEL_GIFT_RECEIPT_FLAG, "'0'");
			}

			// insert external order info
			sql.addColumn(FIELD_EXTERNAL_ORDER_ID,
					getExternalOrderID(srTransaction));
			sql.addColumn(FIELD_EXTERNAL_ORDER_NUMBER,
					getExternalOrderNumber(srTransaction));
			sql.addColumn(FIELD_CONTRACT_SIGNATURE_REQUIRED_FLAG,
					makeStringFromBoolean(srTransaction
							.requireServiceContract()));
		}

		// if it's a retail transaction, add these columns
		if (transaction instanceof RetailTransactionIfc) {
			RetailTransactionIfc rt = (RetailTransactionIfc) transaction;
			sql.addColumn(FIELD_EMPLOYEE_ID, getSalesAssociateID(rt));
			sql.addColumn(FIELD_GIFT_REGISTRY_ID, getGiftRegistryString(rt));
			sql.addColumn(FIELD_ORDER_ID, makeSafeString(rt.getOrderID()));
		}

		if (transaction.getTransactionType() == TransactionConstantsIfc.TYPE_ORDER_PARTIAL
				|| transaction.getTransactionType() == TransactionConstantsIfc.TYPE_ORDER_COMPLETE) {
			CurrencyIfc appliedDeposit = getAppliedOrderDeposit(transaction);
			sql.addColumn(FIELD_DEPOSIT_AMOUNT_APPLIED,
					appliedDeposit.getStringValue());
		}

		if (transaction.getTransactionType() == TransactionConstantsIfc.TYPE_ORDER_INITIATE
				&& ((OrderTransactionIfc) transaction).getOrderType() == OrderConstantsIfc.ORDER_TYPE_ON_HAND) {
			CurrencyIfc appliedDeposit = getAppliedOrderDeposit(transaction);
			sql.addColumn(FIELD_DEPOSIT_AMOUNT_APPLIED,
					appliedDeposit.getStringValue());
		}
		if (transaction instanceof LayawayPaymentTransactionIfc) {
			LayawayPaymentTransactionIfc lt = (LayawayPaymentTransactionIfc) transaction;
			if (lt.getLayaway() != null) {
				sql.addColumn(FIELD_LAYAWAY_ID, makeSafeString(lt.getLayaway()
						.getLayawayID()));
			}
		}

		if (transaction.getIRSCustomer() != null) {
			sql.addColumn(FIELD_IRS_CUSTOMER_ID, getIRSCustomerID(transaction));
		}

		if (transaction.getReturnTicket() != null) {
			sql.addColumn(FIELD_TRANSACTION_RETURN_TICKET,
					makeSafeString(transaction.getReturnTicket()));
		}

		// Begin GD-50: CSAT
		// Moises Solis (Starmount) Dec 18, 2012
		if (transaction instanceof GDYNSaleReturnTransactionIfc) {
			GDYNSaleReturnTransactionIfc gdynTxn = (GDYNSaleReturnTransactionIfc) transaction;
			sql.addColumn(FIELD_SURVEY_CUSTOMER_INVITE_ID,
					getSurveyCustomerInviteID(gdynTxn));
			sql.addColumn(FIELD_SURVEY_CUSTOMER_INVITE_URL,
					getSurveyCustomerInviteURL(gdynTxn));

			// modified by Vivek to save the LoyaltyTransactions.
			if (gdynTxn.getTransactionType() == TransactionConstantsIfc.TYPE_SALE
					|| gdynTxn.getTransactionType() == TransactionConstantsIfc.TYPE_RETURN) {
				insertLoyaltyTransaction(dataConnection, gdynTxn);
			}
		}
		// End GD-50: CSAT

		try {
			dataConnection.execute(sql.getSQLString());
		} catch (DataException de) {
			logger.error(de.toString());
			throw de;
		} catch (Exception e) {
			logger.error(e.toString());
			throw new DataException(DataException.UNKNOWN,
					"insertRetailTransaction", e);
		}

		if ((transaction.getTransactionTotals().getItemSendPackagesCount() > 0)) {
			saveTransactionShippings(dataConnection, transaction);
		}

		// save delivery detail into database
		if (transaction instanceof OrderTransactionIfc
				&& (transaction.getTransactionType() == TransactionConstantsIfc.TYPE_ORDER_INITIATE && ((OrderTransactionIfc) transaction)
						.getOrderType() == OrderConstantsIfc.ORDER_TYPE_ON_HAND)) {
			OrderTransaction orderTransaction = (OrderTransaction) transaction;
			Collection<OrderDeliveryDetailIfc> orderDeliveryDetailCollection = orderTransaction
					.getDeliveryDetails();
			if (orderDeliveryDetailCollection != null) {
				Iterator<OrderDeliveryDetailIfc> orderDeliveryDetailsIterator = orderDeliveryDetailCollection
						.iterator();
				while (orderDeliveryDetailsIterator.hasNext()) {
					OrderDeliveryDetailIfc orderDeliveryDetail = orderDeliveryDetailsIterator
							.next();
					insertOrderDeliveryDetail(dataConnection, orderTransaction,
							orderDeliveryDetail);
				}
			}
		}

		// save order recepient detail into database
		if (transaction instanceof OrderTransactionIfc
				&& ((transaction.getTransactionType() == TransactionConstantsIfc.TYPE_ORDER_PARTIAL || transaction
						.getTransactionType() == TransactionConstantsIfc.TYPE_ORDER_COMPLETE) && ((OrderTransactionIfc) transaction)
						.getOrderType() == OrderConstantsIfc.ORDER_TYPE_ON_HAND)) {
			OrderTransaction orderTransaction = (OrderTransaction) transaction;
			Collection<OrderRecipientIfc> orderRecipientCollection = orderTransaction
					.getOrderRecipients();
			if (orderRecipientCollection != null) {
				Iterator<OrderRecipientIfc> orderRecipientsIterator = orderRecipientCollection
						.iterator();
				int receiptID = 0;
				while (orderRecipientsIterator.hasNext()) {
					OrderRecipientIfc orderRecipient = orderRecipientsIterator
							.next();
					ResultSet rs = readOrderRecipientDetail(dataConnection,
							orderTransaction);
					try {
						if (!rs.next()) {
							orderRecipient.setRecipientDetailID(receiptID);
						} else {
							while (rs.next()) {
								receiptID = rs
										.getInt(ARTSDatabaseIfc.FIELD_ORDER_RECIPIENT_ID);
							}
							receiptID++;
							orderRecipient.setRecipientDetailID(receiptID);
						}
						insertOrderRecipientDetail(dataConnection,
								orderTransaction, orderRecipient);
					} catch (SQLException se) {
						dataConnection.logSQLException(se,
								"read RecipientDetail");
						throw new DataException(DataException.SQL_ERROR,
								"read RecipientDetail", se);
					} catch (Exception e) {
						throw new DataException(DataException.UNKNOWN,
								"read RecipientDetail", e);
					}
				}
			}
		}

	}

	public void insertLoyaltyTransaction(JdbcDataConnection dataConnection,
			GDYNSaleReturnTransactionIfc gdynTxn) throws DataException {

		logger.info("Saving the Loyalty Transaction Inside : insertLoyaltyTransaction method");

		SQLInsertStatement sql = new SQLInsertStatement();

		// Table
		sql.setTable(TABLE_LOYALTY_TRANSACTION_RECORD);

		// Fields
		sql.addColumn(FIELD_RETAIL_STORE_ID, getStoreID(gdynTxn));
		sql.addColumn(FIELD_WORKSTATION_ID, getWorkstationID(gdynTxn));
		sql.addColumn(FIELD_TRANSACTION_SEQUENCE_NUMBER,
				getTransactionSequenceNumber(gdynTxn));
		sql.addColumn(FIELD_BUSINESS_DAY_DATE, getBusinessDayString(gdynTxn));
		sql.addColumn(FIELD_TRANSACTION_TYPE_CODE, getTransactionType(gdynTxn));
		sql.addColumn(FIELD_LOYALTY_ID, getLoyaltyID(gdynTxn));
		sql.addColumn(FIELD_ORIGINAL_LOYALTY_ID, getOriginalLoyaltyID(gdynTxn));
		sql.addColumn(FIELD_LOYALTY_EMAIL_ID, getLoyaltyEmailID(gdynTxn));
		sql.addColumn(FIELD_TRANSACTION_EXPORT_FLAG, LOYALTY_EXPORT_FLAG);
		sql.addColumn(FIELD_RECORD_CREATION_TIMESTAMP,
				getSQLCurrentTimestampFunction());
		sql.addColumn(FIELD_TRANSACTION_EXPORT_RECORD, null); // This should be
																// null in BO
																// Database

		try {
			dataConnection.execute(sql.getSQLString());
		} catch (DataException de) {
			logger.error(de.toString());
			throw de;
		} catch (Exception e) {
			logger.error(e.toString());
			throw new DataException(DataException.UNKNOWN,
					"insertLoyaltyTransaction", e);
		}

	}

	/**
	 * This method in the parent class is private.
	 * 
	 * @param transaction
	 * @return
	 */
	protected CurrencyIfc getAppliedOrderDeposit(
			TenderableTransactionIfc transaction) {
		CurrencyIfc depositPaid = DomainGateway.getBaseCurrencyInstance();
		AbstractTransactionLineItemIfc[] lineItems = ((OrderTransactionIfc) transaction)
				.getLineItems();
		depositPaid.setZero();
		for (int i = 0; i < lineItems.length; i++) {
			depositPaid = depositPaid
					.add(((SaleReturnLineItemIfc) lineItems[i])
							.getOrderItemStatus().getDepositAmount());
		}
		return depositPaid;
	}

	/**
	 * Gets the Customer Survey Invitation URL GDYNJdbcSaveRetailTransaction
	 * String
	 * 
	 * @param transaction
	 * @return
	 */
	protected String getSurveyCustomerInviteURL(
			GDYNSaleReturnTransactionIfc transaction) {
		String url = null;

		if (transaction.getSurveyCustomerInviteURL() != null) {
			url = transaction.getSurveyCustomerInviteURL();
		}

		if (url == null) {
			url = "null";
		} else {
			url = "'" + url + "'";
		}
		return (url);
	}

	/**
	 * Gets the Customer Survey Customer Invitation Id
	 * GDYNJdbcSaveRetailTransaction String
	 * 
	 * @param transaction
	 * @return
	 */
	protected String getSurveyCustomerInviteID(
			GDYNSaleReturnTransactionIfc transaction) {
		String id = null;

		if (transaction.getSurveyCustomerInviteID() != null) {
			id = transaction.getSurveyCustomerInviteID();
		}

		if (id == null) {
			id = "null";
		} else {
			id = "'" + id + "'";
		}
		return (id);
	}

	protected String getLoyaltyEmailID(GDYNSaleReturnTransactionIfc transaction) {

		return "'" + transaction.getLoyaltyEmailID() + "'";
	}

	protected String getLoyaltyID(GDYNSaleReturnTransactionIfc transaction) {

		if(transaction.getLoyaltyID() !=null && transaction.getLoyaltyID().equalsIgnoreCase("") ) {
			return null;
		}
		else {
			return transaction.getLoyaltyID();
		}
	}

	protected String getOriginalLoyaltyID(GDYNSaleReturnTransactionIfc transaction) {

		if(transaction.getOriginalLoyaltyID() !=null && transaction.getOriginalLoyaltyID().equalsIgnoreCase("")) {
			return null;
		}
		else {
			return transaction.getOriginalLoyaltyID();
		}
	}
}
