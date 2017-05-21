//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2014, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.domain.arts;

import org.apache.log4j.Logger;

import oracle.retail.stores.common.sql.SQLInsertStatement;
import oracle.retail.stores.domain.arts.JdbcSaveRedeemTransaction;
import oracle.retail.stores.domain.tender.TenderGiftCardIfc;
import oracle.retail.stores.domain.tender.TenderGiftCertificateIfc;
import oracle.retail.stores.domain.tender.TenderLineItemIfc;
import oracle.retail.stores.domain.tender.TenderStoreCreditIfc;
import oracle.retail.stores.domain.transaction.RedeemTransactionIfc;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;

/**
 * Extending the base JdbcSaveRedeemTransaction for Groupe Dynamite
 * - Gift Card authorization information persistence.
 * 
 * @author dmartinez
 * 
 */
public class GDYNJdbcSaveRedeemTransaction extends JdbcSaveRedeemTransaction
{

    private static final long serialVersionUID = -6465766092505583959L;
    
    /**
     * The logger to which log messages will be sent.
     */
    private static final Logger logger = Logger.getLogger(GDYNJdbcSaveRedeemTransaction.class);
    
    
    
    /**
    Inserts into the redeem_transaction table.
    <P>
    @param  dataConnection  the connection to the data source
    @param  transaction     The Redeem Transaction to update
    @exception DataException
 */
    public void insertRedeemTransaction(JdbcDataConnection dataConnection,
                                    RedeemTransactionIfc transaction)
                                    throws DataException
    {
        /*
         * Insert the transaction in the Transaction table first.
         */
        int currencyID;
        insertTransaction(dataConnection, transaction);

        SQLInsertStatement sql = new SQLInsertStatement();

        sql.setTable(TABLE_REDEEM_TRANSACTION);

        sql.addColumn(FIELD_RETAIL_STORE_ID, getStoreID(transaction));
        sql.addColumn(FIELD_WORKSTATION_ID, getWorkstationID(transaction));
        sql.addColumn(FIELD_BUSINESS_DAY_DATE, getBusinessDayString(transaction));
        sql.addColumn(FIELD_TRANSACTION_SEQUENCE_NUMBER,
                  getTransactionSequenceNumber(transaction));
        sql.addColumn(FIELD_REDEEM_STATE, getRedeemState());
        // TODO: add in for transaction reentry
        // sql.addColumn(FIELD_REENTRY, makeStringFromBoolean(transaction.isReentryMode()));

        TenderLineItemIfc redeemTender = getRedeemTender(transaction);

        if (redeemTender instanceof TenderStoreCreditIfc)
        {
            TenderStoreCreditIfc storeCredit = getTenderStoreCredit(transaction);
            // Set Store Credit state to Redeemed.
            storeCredit.setState(TenderStoreCreditIfc.REDEEM);
            sql.addColumn(FIELD_TENDER_TYPE_CODE,
                    makeSafeString(Integer.toString(TenderLineItemIfc.TENDER_TYPE_STORE_CREDIT)));
            sql.addColumn(FIELD_REDEEM_ID,
                    makeSafeString(storeCredit.getStoreCreditID()));
            sql.addColumn(FIELD_REDEEM_AMOUNT,
                    makeSafeStringFromCurrency(storeCredit.getAmount()));
            sql.addColumn(FIELD_REDEEM_FOREIGN_AMOUNT,
                    makeSafeStringFromCurrency(storeCredit.getAlternateCurrencyTendered()));
            sql.addColumn(FIELD_STORE_CREDIT_STATUS,
                    makeSafeString(storeCredit.getState()));
            sql.addColumn(FIELD_CUSTOMER_FIRST_NAME,
                    makeSafeString(storeCredit.getFirstName()));
            sql.addColumn(FIELD_CUSTOMER_LAST_NAME,
                    makeSafeString(storeCredit.getLastName()));
            sql.addColumn(FIELD_CUSTOMER_ID_TYPE,
                    makeSafeString(storeCredit.getPersonalIDType().getCode()));
            // +I18N
            if (storeCredit.getAlternateCurrencyTendered() != null)
            {
                currencyID = storeCredit.getAlternateCurrencyTendered().getType().getCurrencyId();
            }
            else
            {
                currencyID = storeCredit.getAmountTender().getType().getCurrencyId();
            }
            sql.addColumn(FIELD_CURRENCY_ID, currencyID);
            // -I18N
        }
        else if (redeemTender instanceof TenderGiftCardIfc)
        {
            TenderGiftCardIfc giftCard = (TenderGiftCardIfc) redeemTender;
            sql.addColumn(FIELD_TENDER_TYPE_CODE,
                    makeSafeString(Integer.toString(TenderLineItemIfc.TENDER_TYPE_GIFT_CARD)));
            sql.addColumn(FIELD_REDEEM_ID, makeSafeString(giftCard.getEncipheredCardData().getMaskedAcctNumber()));
            sql.addColumn(FIELD_GIFT_CARD_SERIAL_NUMBER, makeSafeString(giftCard.getEncipheredCardData()
                    .getEncryptedAcctNumber()));
            sql.addColumn(FIELD_REDEEM_AMOUNT, makeSafeStringFromCurrency(giftCard.getAmountTender()));
            // +I18N
            sql.addColumn(FIELD_CURRENCY_ID, giftCard.getAmountTender().getType().getCurrencyId());
            // -I18N
            sql.addColumn(FIELD_GIFT_CARD_ADJUDICATION_CODE, makeSafeString(giftCard.getAuthorizationCode()));            
            
        }
        else if (redeemTender instanceof TenderGiftCertificateIfc)
        {
            TenderGiftCertificateIfc giftCert = (TenderGiftCertificateIfc) redeemTender;
            sql.addColumn(FIELD_TENDER_TYPE_CODE,
                    makeSafeString(Integer.toString(TenderLineItemIfc.TENDER_TYPE_GIFT_CERTIFICATE)));
            sql.addColumn(FIELD_REDEEM_ID, makeSafeString(giftCert.getGiftCertificateNumber()));
            sql.addColumn(FIELD_ISSUING_STORE_NUMBER, makeSafeString(giftCert.getStoreNumber()));
            sql.addColumn(FIELD_REDEEM_AMOUNT, makeSafeStringFromCurrency(giftCert.getAmountTender()));
            sql.addColumn(FIELD_REDEEM_FOREIGN_AMOUNT,
                    makeSafeStringFromCurrency(giftCert.getAlternateCurrencyTendered()));
            sql.addColumn(FIELD_REDEEM_FACE_VALUE_AMOUNT, makeSafeStringFromCurrency(giftCert.getFaceValueAmount()));
            // +I18N
            if (giftCert.getAlternateCurrencyTendered() != null)
            {
                currencyID = giftCert.getAlternateCurrencyTendered().getType().getCurrencyId();
            }
            else
            {
                currencyID = giftCert.getAmountTender().getType().getCurrencyId();
            }
            sql.addColumn(FIELD_CURRENCY_ID, currencyID);
            // -I18N
        }

        sql.addColumn(FIELD_RECORD_LAST_MODIFIED_TIMESTAMP,
                getSQLCurrentTimestampFunction());
        sql.addColumn(FIELD_RECORD_CREATION_TIMESTAMP,
                getSQLCurrentTimestampFunction());

        try
        {
            dataConnection.execute(sql.getSQLString());
        }
        catch (DataException de)
        {
            logger.error(de);
            throw de;
        }
        catch (Exception e)
        {
            logger.error(e);
            throw new DataException(DataException.UNKNOWN, "insertRedeemTransaction", e);
        }
    }    
    

}
