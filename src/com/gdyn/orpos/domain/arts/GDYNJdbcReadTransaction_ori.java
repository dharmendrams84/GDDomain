//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.domain.arts;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.common.sql.SQLSelectStatement;
import oracle.retail.stores.common.utility.LocaleMap;
import oracle.retail.stores.common.utility.LocaleRequestor;
import oracle.retail.stores.common.utility.LocalizedCodeIfc;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.arts.JdbcReadTransaction;
import oracle.retail.stores.domain.customer.CustomerIfc;
import oracle.retail.stores.domain.customer.CustomerInfoIfc;
import oracle.retail.stores.domain.customer.IRSCustomerIfc;
import oracle.retail.stores.domain.discount.DiscountRuleConstantsIfc;
import oracle.retail.stores.domain.discount.DiscountTargetIfc;
import oracle.retail.stores.domain.discount.ItemDiscountStrategyIfc;
import oracle.retail.stores.domain.discount.TransactionDiscountStrategyIfc;
import oracle.retail.stores.domain.employee.EmployeeIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.domain.registry.RegistryIDIfc;
import oracle.retail.stores.domain.returns.ReturnTenderDataElementIfc;
import oracle.retail.stores.domain.tax.TaxIfc;
import oracle.retail.stores.domain.tax.TaxInformationIfc;
import oracle.retail.stores.domain.tender.TenderLineItemIfc;
import oracle.retail.stores.domain.transaction.OrderTransaction;
import oracle.retail.stores.domain.transaction.OrderTransactionIfc;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.domain.transaction.TransactionConstantsIfc;
import oracle.retail.stores.domain.transaction.TransactionIfc;
import oracle.retail.stores.domain.transaction.TransactionTaxIfc;
import oracle.retail.stores.domain.utility.CodeConstantsIfc;
import oracle.retail.stores.domain.utility.EYSDate;
import oracle.retail.stores.domain.utility.EntryMethod;
import oracle.retail.stores.domain.utility.LocaleConstantsIfc;
import oracle.retail.stores.domain.utility.PersonNameIfc;
import oracle.retail.stores.domain.utility.SecurityOverrideIfc;
import oracle.retail.stores.foundation.factory.FoundationObjectFactory;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.foundation.manager.device.EncipheredDataIfc;
import oracle.retail.stores.foundation.utility.Util;

import org.apache.log4j.Logger;

import com.gdyn.orpos.domain.tax.GDYNTaxConstantsIfc;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptCustomerCategory;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptIdImage;
import com.gdyn.orpos.domain.transaction.GDYNSaleReturnTransactionIfc;
import com.gdyn.orpos.domain.transaction.GDYNTransactionTaxIfc;
import com.gdyn.orpos.persistence.utility.GDYNARTSDatabaseIfc;

/**
 * Extending the base GDYNJdbcSaveRetailTransaction for Groupe Dynamite
 * - CSAT: Customer Survey
 * - Tax Exemption
 * 
 * @author MSolis
 * 
 */
public class GDYNJdbcReadTransaction_ori extends JdbcReadTransaction implements GDYNARTSDatabaseIfc
{
    private static final long serialVersionUID = -4335057678565257140L;

    private static Logger logger = Logger.getLogger(GDYNJdbcReadTransaction_ori.class);

    private String taxExemptCategory;

    private String taxIdImageName;

    private EYSDate taxIDExpiryDate;

    private String taxExemptBandRegistry;

    /**
     * Selects from the retail transaction table.
     * 
     * NOTE: This transaction can any class that extends SaleReturnTransactionIfc
     * 
     * @param dataConnection
     *            a connection to the database
     * @param transaction
     *            the base transaction
     * @param retrieveStoreCoupons
     *            designates whether or not to retrieve store
     *            coupon line items
     * @param localeRequestor
     *            The request locales
     * @exception DataException
     *                thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    @Override
    protected void selectSaleReturnTransaction(JdbcDataConnection dataConnection, SaleReturnTransactionIfc transaction,
            LocaleRequestor localeRequestor, boolean retrieveStoreCoupons) throws DataException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("GDYNJdbcReadTransaction.selectSaleReturnTransaction()");
        }

        SQLSelectStatement sql = new SQLSelectStatement();

        /*
         * Add Table(s)
         */
        sql.addTable(TABLE_RETAIL_TRANSACTION, ALIAS_RETAIL_TRANSACTION);
        sql.addTable(TABLE_ADDRESS, ALIAS_ADDRESS);
        sql.addTable(TABLE_RETAIL_STORE, ALIAS_RETAIL_STORE);

        /*
         * Add Columns
         */
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_CUSTOMER_ID);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_IRS_CUSTOMER_ID);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_GIFT_REGISTRY_ID);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_SUSPENDED_TRANSACTION_REASON_CODE);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_EMPLOYEE_ID);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_SEND_PACKAGE_COUNT);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_SEND_CUSTOMER_TYPE);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_SEND_CUSTOMER_PHYSICALLY_PRESENT);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_TRANSACTION_LEVEL_SEND);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_ORDER_ID);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_ENCRYPTED_PERSONAL_ID_NUMBER);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_MASKED_PERSONAL_ID_NUMBER);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_PERSONAL_ID_REQUIRED_TYPE);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_PERSONAL_ID_STATE);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_PERSONAL_ID_COUNTRY);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_AGE_RESTRICTED_DOB);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_EXTERNAL_ORDER_ID);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_EXTERNAL_ORDER_NUMBER);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_CONTRACT_SIGNATURE_REQUIRED_FLAG);
        sql.addColumn(ALIAS_ADDRESS + "." + FIELD_CONTACT_CITY);
        sql.addColumn(ALIAS_ADDRESS + "." + FIELD_CONTACT_STATE);
        sql.addColumn(ALIAS_ADDRESS + "." + FIELD_CONTACT_COUNTRY);
        sql.addColumn(ALIAS_ADDRESS + "." + FIELD_CONTACT_POSTAL_CODE);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_TRANSACTION_RETURN_TICKET);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_TRANSACTION_LEVEL_GIFT_RECEIPT_FLAG);
        // Begin GD-50: CSAT
        // Moises Solis (Starmount) Dec 18, 2012
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_SURVEY_CUSTOMER_INVITE_ID);
        sql.addColumn(ALIAS_RETAIL_TRANSACTION + "." + FIELD_SURVEY_CUSTOMER_INVITE_URL);
        // End GD-50: CSAT

        /*
         * Add Qualifier(s)
         */
        sql.addQualifier(ALIAS_RETAIL_TRANSACTION + "." + FIELD_RETAIL_STORE_ID + " = " + getStoreID(transaction));
        sql.addQualifier(ALIAS_RETAIL_TRANSACTION + "." + FIELD_WORKSTATION_ID + " = " + getWorkstationID(transaction));
        sql.addQualifier(ALIAS_RETAIL_TRANSACTION + "." + FIELD_BUSINESS_DAY_DATE + " = "
                + getBusinessDayString(transaction));
        sql.addQualifier(ALIAS_RETAIL_TRANSACTION + "." + FIELD_TRANSACTION_SEQUENCE_NUMBER + " = "
                + getTransactionSequenceNumber(transaction));
        sql.addQualifier(ALIAS_RETAIL_TRANSACTION + "." + FIELD_RETAIL_STORE_ID + " = " + ALIAS_RETAIL_STORE + "."
                + FIELD_RETAIL_STORE_ID);
        sql.addQualifier(ALIAS_RETAIL_STORE + "." + FIELD_PARTY_ID + " = " + ALIAS_ADDRESS + "." + FIELD_PARTY_ID);

        try
        {
            dataConnection.execute(sql.getSQLString());

            ResultSet rs = (ResultSet) dataConnection.getResult();

            if (!rs.next())
            {
                logger.warn("retail transaction not found!");
                throw new DataException(DataException.NO_DATA, "transaction not found");
            }

            int index = 0;
            String customerId = getSafeString(rs, ++index);
            String irsCustomerId = getSafeString(rs, ++index);
            String giftRegistryID = getSafeString(rs, ++index);
            String suspendReasonCode = getSafeString(rs, ++index);
            String salesAssociateID = getSafeString(rs, ++index);
            int sendPackagesCount = rs.getInt(++index);
            String sendCustomerType = getSafeString(rs, ++index);
            String sendCustomerPhysicallyPresent = getSafeString(rs, ++index);
            String transactionLevelSend = getSafeString(rs, ++index);
            String orderID = getSafeString(rs, ++index);
            String encryptedPersonalIDNumber = getSafeString(rs, ++index);
            String maskedPersonalIDNumber = getSafeString(rs, ++index);
            String personalIDType = getSafeString(rs, ++index);
            String personalIDState = getSafeString(rs, ++index);
            String personalIDCountry = getSafeString(rs, ++index);
            EYSDate ageRestrictedDOB = getEYSDateFromString(rs, ++index);
            String externalOrderID = getSafeString(rs, ++index);
            String externalOrderNumber = getSafeString(rs, ++index);
            boolean requireServiceContractFlag = getBooleanFromString(rs, ++index);
            String storeCity = getSafeString(rs, ++index);
            String storeState = getSafeString(rs, ++index);
            String storeCountry = getSafeString(rs, ++index);
            String storePostalCode = getSafeString(rs, ++index);
            String returnTicket = getSafeString(rs, ++index);
            String transactionGiftReceiptAssigned = getSafeString(rs, ++index);
            // Begin GD-50: CSAT
            // Moises Solis (Starmount) Dec 18, 2012
            String customerSurveryID = getSafeString(rs, ++index);
            String customerSurveryURL = getSafeString(rs, ++index);
            // End GD-50: CSAT

            rs.close();
            // If there is a customer Id associated with the transaction,
            // read the customer information and attach the Customer to the
            // transaction
            if (customerId != null && customerId.length() > 0)
            {
                // linkCustomer(customer)
                boolean isLayawayTransaction = (transaction.getTransactionType() == TransactionConstantsIfc.TYPE_LAYAWAY_INITIATE || transaction
                        .getTransactionType() == TransactionConstantsIfc.TYPE_LAYAWAY_PAYMENT);
                CustomerIfc customer = readCustomer(dataConnection, customerId, isLayawayTransaction, localeRequestor);
                transaction.setCustomer(customer);
                transaction.setCustomerId(customerId);
            }
            if (irsCustomerId != null && irsCustomerId.length() > 0)
            {
                IRSCustomerIfc irsCustomer = readIRSCustomer(dataConnection, irsCustomerId);

                // Read Localized personald ID Code
                irsCustomer.setLocalizedPersonalIDCode(getInitializedLocalizedReasonCode(dataConnection, transaction
                        .getTransactionIdentifier().getStoreID(), irsCustomer.getLocalizedPersonalIDCode().getCode(),
                        CodeConstantsIfc.CODE_LIST_PAT_CUSTOMER_ID_TYPES, localeRequestor));

                transaction.setIRSCustomer(irsCustomer);
            }

            // If there is a default gift registry associated with the
            // transaction, instantiate the GiftRegistry
            if (!(Util.isEmpty(giftRegistryID)))
            {
                RegistryIDIfc registry = instantiateGiftRegistry();
                registry.setID(giftRegistryID);
                transaction.setDefaultRegistry(registry);
            }

            // Read Localized Reason Code
            transaction.setSuspendReason(getInitializedLocalizedReasonCode(dataConnection, transaction
                    .getTransactionIdentifier()
                    .getStoreID(), suspendReasonCode,
                    CodeConstantsIfc.CODE_LIST_TRANSACTION_SUSPEND_REASON_CODES, localeRequestor));

            try
            {
                transaction.setSalesAssociate(getEmployee(dataConnection, salesAssociateID));
            }
            catch (DataException checkEmployeeNotFound)
            {
                // Since empty/not found Sales Associate id could exist in
                // transaction and the
                // sales associate id here is retrieved from particular
                // transaction saved,
                // transaction is set with employee object using the sales
                // associate id
                // retrieved. For error codes other than for not found, data
                // exception is thrown
                if (checkEmployeeNotFound.getErrorCode() == DataException.NO_DATA)
                {
                    PersonNameIfc name = DomainGateway.getFactory().getPersonNameInstance();
                    EmployeeIfc employee = DomainGateway.getFactory().getEmployeeInstance();
                    employee.setEmployeeID(salesAssociateID);
                    name.setFirstName(salesAssociateID);
                    employee.setPersonName(name);
                    transaction.setSalesAssociate(employee);
                }
                else
                {
                    throw checkEmployeeNotFound;
                }
            }

            /*
             * Transaction Tax MUST BE FIRST! When we add the line items or send
             * items, the default tax information has to be setup.
             */
            TransactionTaxIfc transactionTax = selectTaxLineItem(dataConnection, transaction,
                    getLocaleRequestor(transaction));

            if (transactionTax.getTaxMode() == TaxIfc.TAX_MODE_EXEMPT
                    || transactionTax.getTaxMode() == GDYNTaxConstantsIfc.TAX_MODE_PARTIAL_EXEMPT)
            {
                selectTaxExemptionModifier(dataConnection, transaction, transactionTax);
            }
            transaction.setTransactionTax(transactionTax);

            // Set the shipping information.
            if (sendPackagesCount > 0)
            {
                transaction.setSendPackageCount(sendPackagesCount);
                readTransactionShippings(dataConnection, transaction, localeRequestor);
            }

            if (sendCustomerType.equals("0"))
            {
                transaction.setSendCustomerLinked(true);
            }
            else
            {
                transaction.setSendCustomerLinked(false);
            }
            if (sendCustomerPhysicallyPresent.equals("1"))
            {
                transaction.setCustomerPhysicallyPresent(true);
            }
            else
            {
                transaction.setCustomerPhysicallyPresent(false);
            }
            if (transactionLevelSend.equals("1"))
            {
                transaction.getTransactionTotals().setTransactionLevelSendAssigned(true);
            }
            else
            {
                transaction.getTransactionTotals().setTransactionLevelSendAssigned(false);
            }
            // Set the store address information
            transaction.getWorkstation().getStore().getAddress().setCity(storeCity);
            transaction.getWorkstation().getStore().getAddress().setState(storeState);
            transaction.getWorkstation().getStore().getAddress().setCountry(storeCountry);
            transaction.getWorkstation().getStore().getAddress().setPostalCode(storePostalCode);

            transaction.setReturnTicket(returnTicket);

            if (transactionGiftReceiptAssigned.equals("1"))
            {
                transaction.setTransactionGiftReceiptAssigned(true);
            }
            else
            {
                transaction.setTransactionGiftReceiptAssigned(false);
            }
            // Set the personal ID information
            if (!(Util.isEmpty(maskedPersonalIDNumber)))
            {
                CustomerInfoIfc customerInfo = transaction.getCustomerInfo();
                if (customerInfo == null)
                {
                    customerInfo = DomainGateway.getFactory().getCustomerInfoInstance();
                }

                // Read Localized Reason Code
                if (!Util.isEmpty(personalIDType))
                {
                    customerInfo.setLocalizedPersonalIDType(getInitializedLocalizedReasonCode(dataConnection,
                            transaction
                                    .getTransactionIdentifier().getStoreID(), personalIDType,
                            CodeConstantsIfc.CODE_LIST_CHECK_ID_TYPES, localeRequestor));
                }

                EncipheredDataIfc personalIDNumber = FoundationObjectFactory.getFactory()
                        .createEncipheredDataInstance(encryptedPersonalIDNumber, maskedPersonalIDNumber);
                customerInfo.setPersonalID(personalIDNumber);
                customerInfo.setPersonalIDState(personalIDState);
                customerInfo.setPersonalIDCountry(personalIDCountry);
                transaction.setCustomerInfo(customerInfo);
            }

            // Set the age restricted DOB
            transaction.setAgeRestrictedDOB(ageRestrictedDOB);

            // Set external order info
            transaction.setExternalOrderID(externalOrderID);
            transaction.setExternalOrderNumber(externalOrderNumber);
            transaction.setRequireServiceContractFlag(requireServiceContractFlag);

            // Read Transaction Discounts
            TransactionDiscountStrategyIfc[] transactionDiscounts;
            transactionDiscounts = selectDiscountLineItems(dataConnection, transaction, localeRequestor);
            transaction.addTransactionDiscounts(transactionDiscounts);

            // Read sale return line items

            SaleReturnLineItemIfc[] lineItems = selectSaleReturnLineItems(dataConnection, transaction, localeRequestor,
                    retrieveStoreCoupons);

            if (transaction instanceof OrderTransaction &&
                    transaction.getTransactionStatus() != TransactionConstantsIfc.STATUS_SUSPENDED)// logic added to
            // eliminate kitItem.
            {
                ArrayList<SaleReturnLineItemIfc> arrayOfLineItem = new ArrayList<SaleReturnLineItemIfc>();
                for (int i = 0; i < lineItems.length; i++)
                {
                    if (!(lineItems[i].getPLUItem().isKitHeader()))
                    {
                        arrayOfLineItem.add(lineItems[i]);
                    }

                }
                SaleReturnLineItemIfc[] pdoLineItems = new SaleReturnLineItemIfc[arrayOfLineItem.size()];
                arrayOfLineItem.toArray(pdoLineItems);
                lineItems = pdoLineItems;
            }
            SaleReturnLineItemIfc[] deletedLineItems = selectDeletedSaleReturnLineItems(dataConnection, transaction,
                    localeRequestor);
            transaction.setLineItems(lineItems);
            if (deletedLineItems != null)
            {
                if (deletedLineItems.length > 0)
                {
                    for (int i = 0; i < deletedLineItems.length; i++)
                    {
                        transaction.addDeletedLineItems(deletedLineItems[i]);
                    }
                }
            }

            // if the transaction is an order transaction
            if (transaction.getTransactionType() == TransactionIfc.TYPE_ORDER_CANCEL
                    || transaction.getTransactionType() == TransactionIfc.TYPE_ORDER_PARTIAL
                    || transaction.getTransactionType() == TransactionIfc.TYPE_ORDER_COMPLETE
                    || transaction.getTransactionType() == TransactionIfc.TYPE_ORDER_INITIATE)
            {
                OrderTransactionIfc orderTransaction = (OrderTransactionIfc) transaction;
                orderTransaction.setOrderID(orderID);
                orderTransaction.getOrderStatus().setTrainingModeFlag(transaction.isTrainingMode());
                selectOrderStatusForTransaction(dataConnection, orderTransaction);
                selectOrderLineItemsByRef(dataConnection, transaction);
                selectDeliveryDetails(dataConnection, transaction);
                selectRecipientDetail(dataConnection, transaction);
            }

            // Read tender line items
            TenderLineItemIfc[] tenderLineItems = selectTenderLineItems(dataConnection, transaction);
            transaction.setTenderLineItems(tenderLineItems);

            // Read tenders for return items in the trans
            if (transaction.hasReturnItems())
            {
                ReturnTenderDataElementIfc[] returnTenders = readReturnTenders(dataConnection, transaction);
                transaction.appendReturnTenderElements(returnTenders);
            }

            // Begin GD-50: CSAT
            // Moises Solis (Starmount) Dec 18, 2012
            if (transaction instanceof GDYNSaleReturnTransactionIfc)
            {
                GDYNSaleReturnTransactionIfc txn = (GDYNSaleReturnTransactionIfc) transaction;
                txn.setSurveyCustomerInviteID(customerSurveryID);
                txn.setSurveyCustomerInviteURL(customerSurveryURL);
            }
            // End GD-50: CSAT

        }
        catch (DataException de)
        {
            logger.error("" + de + "");
            throw de;
        }
        catch (SQLException se)
        {
            dataConnection.logSQLException(se, "retail transaction table");
            throw new DataException(DataException.SQL_ERROR, "retail transaction table", se);
        }
        catch (Exception e)
        {
            logger.error("" + Util.throwableToString(e) + "");
            throw new DataException(DataException.UNKNOWN, "retail transaction table", e);
        }

        if (logger.isDebugEnabled())
            logger.debug("JdbcReadTransaction.selectSaleReturnTransaction()");
    }

    /**
     * Get the tax information for an individual line item.
     * 
     * @param dataConnection
     *            a connection to the database
     * @param transaction
     *            the retail transaction
     * @param lineItemSequenceNumber
     * @return The Tax Information
     * @throws DataException
     */
    protected TaxInformationIfc[] selectSaleReturnLineItemTaxInformation(JdbcDataConnection dataConnection,
            SaleReturnTransactionIfc transaction, int lineItemSequenceNumber) throws DataException
    {
        if (logger.isDebugEnabled())
            logger.debug("JdbcReadTransaction.selectSaleReturnLineItemTaxInformation()");

        SQLSelectStatement sql = new SQLSelectStatement();

        sql.addTable(TABLE_SALE_RETURN_TAX_LINE_ITEM, ALIAS_SALE_RETURN_TAX_LINE_ITEM);

        sql.setDistinctFlag(true);
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_SALE_RETURN_TAX_AMOUNT); // MO_TX_RTN_SLS
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_TAX_AUTHORITY_ID); // ID_ATHY_TX
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_TAX_GROUP_ID); // ID_GP_TX
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_TAX_TYPE); // TY_TX
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_TAX_HOLIDAY); // FL_TX_HDY
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_TAX_RULE_NAME); // NM_RU_TX
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_TAX_PERCENTAGE); // PE_TX
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_SALE_RETURN_TAX_AMOUNT); // MO_TX_RTN_SLS
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_TAXABLE_SALE_RETURN_AMOUNT); // MO_TXBL_RTN_SLS
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_UNIQUE_ID); // ID_UNQ
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_TAX_MODE); // TX_MOD
        sql.addColumn(ALIAS_SALE_RETURN_TAX_LINE_ITEM, FIELD_FLG_TAX_INCLUSIVE); // FL_TX_INC

        // Tax Exemptions
        sql.addColumn(FIELD_TAX_ID_IMAGE_NAME);
        sql.addColumn(FIELD_TAX_EXEMPT_BAND_COUNCIL_REGISTRY);
        sql.addColumn(FIELD_TAX_ID_EXPIRY_DATE);
        sql.addColumn(FIELD_CATEGORY_CODE);

        sql.addQualifier(ALIAS_SALE_RETURN_TAX_LINE_ITEM + "." + FIELD_RETAIL_STORE_ID + "=" + getStoreID(transaction));
        sql.addQualifier(ALIAS_SALE_RETURN_TAX_LINE_ITEM + "." + FIELD_WORKSTATION_ID + " = "
                + getWorkstationID(transaction));
        sql.addQualifier(ALIAS_SALE_RETURN_TAX_LINE_ITEM + "." + FIELD_BUSINESS_DAY_DATE + " = "
                + getBusinessDayString(transaction));
        sql.addQualifier(ALIAS_SALE_RETURN_TAX_LINE_ITEM + "." + FIELD_TRANSACTION_SEQUENCE_NUMBER + " = "
                + getTransactionSequenceNumber(transaction));
        sql.addQualifier(ALIAS_SALE_RETURN_TAX_LINE_ITEM + "." + FIELD_RETAIL_TRANSACTION_LINE_ITEM_SEQUENCE_NUMBER
                + " = " + lineItemSequenceNumber);

        ArrayList<TaxInformationIfc> taxInfoList = new ArrayList<TaxInformationIfc>();

        try
        {
            dataConnection.execute(sql.getSQLString());
            ResultSet rs = (ResultSet) dataConnection.getResult();

            while (rs.next())
            {
                TaxInformationIfc taxInformation = DomainGateway.getFactory().getTaxInformationInstance();
                int index = 0;
                taxInformation.setTaxAmount(getCurrencyFromDecimal(rs, ++index));
                taxInformation.setTaxAuthorityID(rs.getInt(++index));
                taxInformation.setTaxGroupID(rs.getInt(++index));
                taxInformation.setTaxTypeCode(rs.getInt(++index));
                taxInformation.setTaxHoliday(getBooleanFromString(rs, ++index));
                taxInformation.setTaxRuleName(getSafeString(rs, ++index));

                BigDecimal perc = getBigDecimal(rs, ++index, TAX_PERCENTAGE_SCALE);
                taxInformation.setTaxPercentage(perc);

                taxInformation.setTaxAmount(getCurrencyFromDecimal(rs, ++index));
                taxInformation.setTaxableAmount(getCurrencyFromDecimal(rs, ++index));
                taxInformation.setUniqueID(getSafeString(rs, ++index));
                taxInformation.setTaxMode(rs.getInt(++index));
                taxInformation.setInclusiveTaxFlag(getBooleanFromString(rs, ++index));
                taxInfoList.add(taxInformation);

                // Tax Exemptions
                taxIdImageName = getSafeString(rs, ++index);
                taxExemptBandRegistry = getSafeString(rs, ++index);
                taxIDExpiryDate = getEYSDateFromString(rs, ++index);
                taxExemptCategory = getSafeString(rs, ++index);

            }
            rs.close();

            // Tax Exemptions
            if (transaction.getTransactionTax() != null)
            {
                TransactionTaxIfc transactionTax = transaction.getTransactionTax();
                if (transactionTax instanceof GDYNTransactionTaxIfc)
                {
                    GDYNTransactionTaxIfc transactionTaxExemptions = (GDYNTransactionTaxIfc) transactionTax;
                    transactionTaxExemptions.setBandRegistryId(taxExemptBandRegistry);
                    transactionTaxExemptions.setIdExpirationDate(taxIDExpiryDate);

                    populateTaxExemption(dataConnection,
                            (GDYNTransactionTaxIfc) transactionTax,
                            taxExemptCategory,
                            taxIdImageName);
                }
            }

        }
        catch (SQLException se)
        {
            throw new DataException(DataException.SQL_ERROR, "selectSaleReturnLineItemTaxByTaxAuthority", se);
        }
        catch (DataException de)
        {
            throw de;
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "selectSaleReturnLineItemTaxByTaxAuthority", e);
        }

        TaxInformationIfc[] results = new TaxInformationIfc[taxInfoList.size()];
        for (int i = 0; i < results.length; i++)
        {
            results[i] = taxInfoList.get(i);
        }
        return results;
    }

    // --------------------------------------------------------------------------
    /**
     * @param dataConnection
     * @param tax
     * @param categoryCode
     * @param imageName
     * @throws DataException
     * @throws SQLException 
     */
    protected void populateTaxExemption(JdbcDataConnection dataConnection,
            GDYNTransactionTaxIfc tax,
            String categoryCode,
            String imageName)
            throws DataException, SQLException
    {
        Locale locale = LocaleMap.getLocale(LocaleConstantsIfc.DATABASE);
        String countryCode = locale.getCountry();
        String taxArea = "";

        if (categoryCode != null && !categoryCode.isEmpty())
        {
            GDYNJdbcReadTaxExemptCustomer customerOp =
                    new GDYNJdbcReadTaxExemptCustomer();

            GDYNTaxExemptCustomerCategory category =
                    (GDYNTaxExemptCustomerCategory) customerOp.selectCategoryByCode(
                            dataConnection, categoryCode, countryCode, taxArea, locale, true);

            tax.setCustomerCategory(category);
        }
        if (imageName != null && !imageName.isEmpty())
        {
            GDYNJdbcReadTaxExemptIdImage imageOp = new GDYNJdbcReadTaxExemptIdImage();

            GDYNTaxExemptIdImage idImage =
                    (GDYNTaxExemptIdImage) imageOp.selectIdImageByName(
                            dataConnection, imageName, countryCode);

            tax.setTaxExemptIdImage(idImage);
        }

    }

    // Begin GD-49: Develop Employee Discount Module
    // lcatania (Starmount) Mar 7, 2013
    /**
     * Reads from the retail price modifier table.
     *
     * @param dataConnection the connection to the data source
     * @param transaction the transaction coming from business logic
     * @param lineItem the sale/return line item
     * @param localeRequestor the requested locales
     * @return Array of discount strategies
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    protected ItemDiscountStrategyIfc[] selectRetailPriceModifiers(JdbcDataConnection dataConnection,
            TransactionIfc transaction, SaleReturnLineItemIfc lineItem, LocaleRequestor localeRequestor)
            throws DataException
    {

        if (logger.isDebugEnabled())
            logger.debug("GDYNJdbcReadTransaction.selectRetailPriceModifiers()");

        SQLSelectStatement sql = new SQLSelectStatement();
        /*
         * Add Table(s)
         */
        sql.addTable(TABLE_RETAIL_PRICE_MODIFIER);
        /*
         * Add Column(s)
         */
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DERIVATION_RULE_ID);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_REASON_CODE);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_PERCENT);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_AMOUNT);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_SEQUENCE_NUMBER);
        sql.addColumn(FIELD_PRICE_DERIVATION_RULE_METHOD_CODE);
        sql.addColumn(FIELD_PRICE_DERIVATION_RULE_ASSIGNMENT_BASIS_CODE);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_EMPLOYEE_ID);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DAMAGE_DISCOUNT);
        sql.addColumn(FIELD_PCD_INCLUDED_IN_BEST_DEAL);
        sql.addColumn(FIELD_ADVANCED_PRICING_RULE);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_REFERENCE_ID);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_REFERENCE_ID_TYPE_CODE);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_DISCOUNT_TYPE_CODE);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_STOCK_LEDGER_ACCOUNTING_DISPOSITION_CODE);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_OVERRIDE_EMPLOYEE_ID);
        sql.addColumn(FIELD_RETAIL_PRICE_MODIFIER_OVERRIDE_ENTRY_METHOD_CODE);
        sql.addColumn(FIELD_PROMOTION_ID);
        sql.addColumn(FIELD_PROMOTION_COMPONENT_ID);
        sql.addColumn(FIELD_PROMOTION_COMPONENT_DETAIL_ID);

        /*
         * Add Qualifier(s)
         */
        sql.addQualifier(FIELD_RETAIL_STORE_ID + " = " + getStoreID(transaction));
        sql.addQualifier(FIELD_WORKSTATION_ID + " = " + getWorkstationID(transaction));
        sql.addQualifier(FIELD_BUSINESS_DAY_DATE + " = " + getBusinessDayString(transaction));
        sql.addQualifier(FIELD_TRANSACTION_SEQUENCE_NUMBER + " = " + getTransactionSequenceNumber(transaction));
        sql.addQualifier(FIELD_RETAIL_TRANSACTION_LINE_ITEM_SEQUENCE_NUMBER + " = " + lineItem.getLineNumber());
        /*
         * Add Ordering
         */
        sql.addOrdering(FIELD_RETAIL_PRICE_MODIFIER_SEQUENCE_NUMBER + " ASC");

        Vector<ItemDiscountStrategyIfc> itemDiscounts = new Vector<ItemDiscountStrategyIfc>();
        String reasonCodeString = "";
        try
        {
            dataConnection.execute(sql.getSQLString());
            logger.debug("GDYNJdbcReadTransaction.selectRetailPriceModifiers() SQL:  " + sql.getSQLString());
            ResultSet rs = (ResultSet)dataConnection.getResult();

            while (rs.next())
            {
                int index = 0;
                int ruleID = rs.getInt(++index);
                reasonCodeString = getSafeString(rs, ++index);
                BigDecimal percent = getBigDecimal(rs, ++index);
                // CurrencyIfc amount = getCurrencyFromDecimal(rs, ++index);
                CurrencyIfc amount = getLongerCurrencyFromDecimal(rs, ++index);
                index = index + 1;
                int methodCode = rs.getInt(++index);
                int assignmentBasis = rs.getInt(++index);
                String discountEmployeeID = getSafeString(rs, ++index);
                boolean isDamageDiscount = getBooleanFromString(rs, ++index);
                boolean isIncludedInBestDealFlag = getBooleanFromString(rs, ++index);
                boolean isAdvancedPricingRuleFlag = getBooleanFromString(rs, ++index);
                String referenceID = rs.getString(++index);
                String referenceIDCodeStr = getSafeString(rs, ++index);
                int typeCode = rs.getInt(++index);
                int accountingCode = rs.getInt(++index);
                String overrideEmployeeID = getSafeString(rs, ++index);
                int overrideEntryMethod = rs.getInt(++index);
                int promotionId = rs.getInt(++index);
                int promotionComponentId = rs.getInt(++index);
                int promotionComponentDetailId = rs.getInt(++index);

                LocalizedCodeIfc localizedCode = DomainGateway.getFactory().getLocalizedCode();

                // Determine type
                if (ruleID == 0) // price override
                {
                    localizedCode = getInitializedLocalizedReasonCode(dataConnection, transaction.getTransactionIdentifier()
                            .getStoreID(), reasonCodeString, CodeConstantsIfc.CODE_LIST_PRICE_OVERRIDE_REASON_CODES,
                            localeRequestor);
                    lineItem.modifyItemPrice(amount, localizedCode);
                    if (!Util.isEmpty(overrideEmployeeID))
                    {
                        SecurityOverrideIfc override = DomainGateway.getFactory().getSecurityOverrideInstance();
                        override.setAuthorizingEmployee(overrideEmployeeID);
                        override.setEntryMethod(EntryMethod.getEntryMethod(overrideEntryMethod));
                        lineItem.getItemPrice().setPriceOverrideAuthorization(override);
                    }
                }
                else
                // item discount
                {
                    // Determine type of discount
                    ItemDiscountStrategyIfc itemDiscount = null;
                    String ruleIDString = Integer.toString(ruleID);
                    String codeListType = CodeConstantsIfc.CODE_LIST_ITEM_DISCOUNT_BY_AMOUNT;
                    if (isDamageDiscount)
                    {
                        codeListType = CodeConstantsIfc.CODE_LIST_DAMAGE_DISCOUNT_REASON_CODES;
                    }
                    else if (assignmentBasis == DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE)
                    {
                        codeListType = CodeConstantsIfc.CODE_LIST_EMPLOYEE_DISCOUNT_REASON_CODES;
                    }
                    else if (isAdvancedPricingRuleFlag)
                    {
                        codeListType = CodeConstantsIfc.CODE_LIST_ADVANCED_PRICING_REASON_CODES;
                    }
                    else if (accountingCode == DiscountRuleConstantsIfc.ACCOUNTING_METHOD_MARKDOWN)
                    {
                        codeListType = CodeConstantsIfc.CODE_LIST_MARKDOWN_AMOUNT_REASON_CODES;
                    }

                    switch (methodCode)
                    {
                        case DISCOUNT_METHOD_PERCENTAGE:
                        {

                            if (isDamageDiscount)
                            {
                                codeListType = CodeConstantsIfc.CODE_LIST_DAMAGE_DISCOUNT_REASON_CODES;
                            }
                            else if (assignmentBasis == DiscountRuleConstantsIfc.ASSIGNMENT_EMPLOYEE)
                            {
                                codeListType = CodeConstantsIfc.CODE_LIST_EMPLOYEE_DISCOUNT_REASON_CODES;
                            }
                            else if (isAdvancedPricingRuleFlag)
                            {
                                codeListType = CodeConstantsIfc.CODE_LIST_ADVANCED_PRICING_REASON_CODES;
                            }
                            else if (accountingCode == DiscountRuleConstantsIfc.ACCOUNTING_METHOD_MARKDOWN)
                            {
                                codeListType = CodeConstantsIfc.CODE_LIST_MARKDOWN_PERCENT_REASON_CODES;
                            }
                            else
                            {
                                codeListType = CodeConstantsIfc.CODE_LIST_ITEM_DISCOUNT_BY_PERCENTAGE;
                            }

                            itemDiscount = DomainGateway.getFactory().getItemDiscountByPercentageInstance();
                            itemDiscount.setRuleID(ruleIDString);
                            itemDiscount.setDiscountRate(percent.movePointLeft(2));
                            localizedCode = getLocalizedReasonCode(dataConnection, transaction
                                    .getTransactionIdentifier().getStoreID(), reasonCodeString, codeListType,
                                    localeRequestor, ruleIDString);

                            // discount names and reason code names are the same
                            // for manual and advanced pricing discounts
                            if (localizedCode != null)
                            {
                                itemDiscount.setLocalizedNames(localizedCode.getText());
                            }
                            else
                            {
                                localizedCode = DomainGateway.getFactory().getLocalizedCode();
                                localizedCode.setCode(reasonCodeString);
                            }
                            itemDiscount.setReason(localizedCode);

                            itemDiscount.setAssignmentBasis(assignmentBasis);
                            itemDiscount.setDiscountEmployee(discountEmployeeID);
//                            setDiscountEmployeeIDOnTransaction(transaction, discountEmployeeID);
                            itemDiscount.setDamageDiscount(isDamageDiscount);
                            itemDiscount.setTypeCode(typeCode);
                            itemDiscount.setAccountingMethod(accountingCode);
                            break;
                        }
                        case DISCOUNT_METHOD_AMOUNT:
                        {
                            if (amount.signum() == CurrencyIfc.POSITIVE || amount.signum() == CurrencyIfc.ZERO)
                            {
                                itemDiscount = DomainGateway.getFactory().getItemDiscountByAmountInstance();
                                itemDiscount.setDiscountAmount(amount);
                                itemDiscount.setRuleID(ruleIDString);
                                localizedCode = getLocalizedReasonCode(dataConnection, transaction
                                        .getTransactionIdentifier().getStoreID(), reasonCodeString, codeListType,
                                        localeRequestor, ruleIDString);

                                // discount names and reason code names are the
                                // same, so set it here .
                                if (localizedCode != null)
                                {
                                    itemDiscount.setLocalizedNames(localizedCode.getText());
                                }
                                else
                                {
                                    localizedCode = DomainGateway.getFactory().getLocalizedCode();
                                    localizedCode.setCode(reasonCodeString);
                                }
                                itemDiscount.setReason(localizedCode);

                                itemDiscount.setAssignmentBasis(assignmentBasis);
                                itemDiscount.setDiscountEmployee(discountEmployeeID);
//                                setDiscountEmployeeIDOnTransaction(transaction, discountEmployeeID);
                                itemDiscount.setDamageDiscount(isDamageDiscount);
                                itemDiscount.setTypeCode(typeCode);
                                itemDiscount.setAccountingMethod(accountingCode);
                            }
                            else if (amount.signum() == CurrencyIfc.NEGATIVE)
                            {
                                itemDiscount = DomainGateway.getFactory()
                                        .getReturnItemTransactionDiscountAuditInstance();
                                itemDiscount.setDiscountAmount(amount);
                                itemDiscount.setRuleID(ruleIDString);
                                itemDiscount.setDiscountMethod(DiscountRuleConstantsIfc.DISCOUNT_METHOD_AMOUNT);
                                localizedCode = getLocalizedReasonCode(dataConnection, transaction
                                        .getTransactionIdentifier().getStoreID(), reasonCodeString, codeListType,
                                        localeRequestor, ruleIDString);
                                // we do not have a way to distinguish between
                                // manual item discounts and markdowns, so if
                                // above call failed try
                                // to get the reason codes for the markdowns
                                if (localizedCode == null)
                                {
                                    localizedCode = getLocalizedReasonCode(dataConnection, transaction
                                            .getTransactionIdentifier().getStoreID(), reasonCodeString,
                                            CodeConstantsIfc.CODE_LIST_MARKDOWN_AMOUNT_REASON_CODES, localeRequestor,
                                            ruleIDString);
                                }

                                if (localizedCode != null)
                                {// discount names and reason code names are the
                                 // same, so set it here for manual discounts.
                                 // for
                                 // adv. pricing rule, we already retieved
                                 // the localized names through jdbc plu
                                 // operation
                                    itemDiscount.setLocalizedNames(localizedCode.getText());
                                }
                                else
                                {
                                    localizedCode = DomainGateway.getFactory().getLocalizedCode();
                                    localizedCode.setCode(reasonCodeString);
                                }
                                itemDiscount.setReason(localizedCode);

                                itemDiscount.setAssignmentBasis(assignmentBasis);
                                itemDiscount.setDiscountEmployee(discountEmployeeID);
//                                setDiscountEmployeeIDOnTransaction(transaction, discountEmployeeID);
                                itemDiscount.setDamageDiscount(isDamageDiscount);
                                itemDiscount.setTypeCode(typeCode);
                                itemDiscount.setAccountingMethod(accountingCode);
                            }
                            break;
                        }
                        case DISCOUNT_METHOD_FIXED_PRICE:
                        {
                            itemDiscount = DomainGateway.getFactory().getItemDiscountByFixedPriceStrategyInstance();
                            itemDiscount.setDiscountAmount(amount);
                            itemDiscount.setRuleID(ruleIDString);
                            localizedCode = getLocalizedReasonCode(dataConnection, transaction
                                    .getTransactionIdentifier().getStoreID(), reasonCodeString, codeListType,
                                    localeRequestor, ruleIDString);
                            if (localizedCode != null)
                            {// discount names and reason code names are the
                             // same, so set it here for manual discounts. for
                             // adv. pricing rule, we already retieved the
                             // localized names through jdbc plu operation
                                itemDiscount.setLocalizedNames(localizedCode.getText());
                                localizedCode.setCode(reasonCodeString);
                            }
                            else
                            {
                                localizedCode = DomainGateway.getFactory().getLocalizedCode();
                                localizedCode.setCode(reasonCodeString);
                            }
                            itemDiscount.setReason(localizedCode);

                            itemDiscount.setAssignmentBasis(assignmentBasis);
                            itemDiscount.setTypeCode(typeCode);
                            itemDiscount.setAccountingMethod(accountingCode);
                            break;
                        }
                    }// end switch methodCode

                    // ReferenceID and TypeCode
                    if (itemDiscount != null)
                    {
                        itemDiscount.setReferenceID(referenceID);

                        if (referenceIDCodeStr == null)
                        {
                            itemDiscount.setReferenceIDCode(0);
                        }
                        else
                        {
                            for (int i = 0; i < DiscountRuleConstantsIfc.REFERENCE_ID_TYPE_CODE.length; i++)
                            {
                                if (referenceIDCodeStr
                                        .equalsIgnoreCase(DiscountRuleConstantsIfc.REFERENCE_ID_TYPE_CODE[i]))
                                {
                                    itemDiscount.setReferenceIDCode(i);
                                }
                            }
                        }
                        itemDiscount.setAdvancedPricingRule(isAdvancedPricingRuleFlag);

                        if (isAdvancedPricingRuleFlag)
                        {
                            ((DiscountTargetIfc) lineItem).applyAdvancedPricingDiscount(itemDiscount);
                        }

                        itemDiscount.setIncludedInBestDeal(isIncludedInBestDealFlag);

                        // Set Temporary Price Change Promotion IDs
                        itemDiscount.setPromotionId(promotionId);
                        itemDiscount.setPromotionComponentId(promotionComponentId);
                        itemDiscount.setPromotionComponentDetailId(promotionComponentDetailId);

                        itemDiscounts.addElement(itemDiscount);
                    }
                    else
                    // itemDiscount == null
                    {
                        logger.error("Unknown type of itemDiscount:  reasonCode=" + reasonCodeString
                                + " percent=" + percent + " amount=" + amount + "");
                    }
                }
            }// end while (rs.next())
            rs.close();
        }
        catch (SQLException exc)
        {
            dataConnection.logSQLException(exc, "Processing result set.");
            throw new DataException(DataException.SQL_ERROR, "selectRetailPriceModifiers", exc);
        }

        // put vector into array
        ItemDiscountStrategyIfc[] discounts = null;
        int numDiscounts = itemDiscounts.size();

        if (numDiscounts > 0)
        {
            discounts = new ItemDiscountStrategyIfc[numDiscounts];
            itemDiscounts.copyInto(discounts);
            setDiscountEmployeeIDOnTransaction(discounts, transaction, dataConnection);
        }

        if (logger.isDebugEnabled())
            logger.debug("JdbcReadTransaction.selectRetailPriceModifiers()");

        return (discounts);
    }

    /**
     * 
     * GDYNJdbcReadTransaction
     * void
     * 
     * @param transaction
     * @param discountEmployee
     * @throws DataException 
     */
    protected void setDiscountEmployeeIDOnTransaction(ItemDiscountStrategyIfc[] discounts, TransactionIfc transaction, JdbcDataConnection dataConnection) throws DataException
    {
        if ((discounts != null) && (discounts.length > 0))
        {
            // find the percent discount stategy that is a discount.
            for (int j = 0; j < discounts.length; j++)
            {
                ItemDiscountStrategyIfc itemDiscount = discounts[j];
                String discountEmployee = itemDiscount.getDiscountEmployeeID();
                EmployeeIfc employee = getEmployee(dataConnection, discountEmployee);
                
                if (!Util.isEmpty(employee.getEmployeeID()) && (transaction instanceof SaleReturnTransactionIfc)
                        && ((SaleReturnTransactionIfc) transaction).getEmployeeDiscountID() == null)
                {
                    ((SaleReturnTransactionIfc) transaction).setEmployeeDiscountID(employee.getEmployeeID());
                    if (transaction instanceof GDYNSaleReturnTransactionIfc)
                    {
                        ((GDYNSaleReturnTransactionIfc) transaction).setEmployeeDiscountName(employee.getPersonName()
                                .getFullName());
                    }
                }
            }
        }
     }

    /**
     * 
     * GDYNJdbcReadTransaction
     * EmployeeIfc
     * 
     * @param connection
     * @param rs
     * @param index
     * @return
     * @throws DataException
     */
    protected EmployeeIfc readEmployee(JdbcDataConnection connection, ResultSet rs, int index) throws DataException
    {
        EmployeeIfc employee = DomainGateway.getFactory().getEmployeeInstance();
        PersonNameIfc employeeName = DomainGateway.getFactory().getPersonNameInstance();
        try
        {
            // Begin GD-418: GD_Discounts not being applied properly during return
            // mlawrence (Starmount) Jul 23, 2013
            // Retrieving only data elements in the employee table that are needed for the discount function.
            employee.setEmployeeID(getSafeString(rs, ++index));
            employeeName.setFullName(getSafeString(rs, ++index));
            employeeName.setLastName(getSafeString(rs, ++index));
            employeeName.setFirstName(getSafeString(rs, ++index));
            employeeName.setMiddleName(getSafeString(rs, ++index));
            employee.setPersonName(employeeName);
            // End GD-418: GD_Discounts not being applied properly during return
        }
        catch (SQLException e)
        {
            ((JdbcDataConnection) connection).logSQLException(
                    e,
                    "Processing result set.");
            throw new DataException(
                    DataException.SQL_ERROR,
                    "An SQL Error occurred proccessing the result set from reading an employee in GDYNJdbcReadTransaction.",
                    e);
        }
        catch (NumberFormatException e)
        {
            logger.error("Error occurred reading employee information.", e);
            throw new DataException(
                    DataException.DATA_FORMAT,
                    "Found an unexpected numeric data format in GDYNJdbcReadTransaction.",
                    e);
        }

        return employee;
    }
    // End GD-49: Develop Employee Discount Module
}
