package com.gdyn.orpos.domain.arts;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import oracle.retail.stores.common.data.JdbcUtilities;
import oracle.retail.stores.common.identifier.IdentifierConstantsIfc;
import oracle.retail.stores.common.identifier.IdentifierServiceIfc;
import oracle.retail.stores.common.identifier.IdentifierServiceLocator;
import oracle.retail.stores.common.sql.SQLDeleteStatement;
import oracle.retail.stores.common.sql.SQLInsertStatement;
import oracle.retail.stores.common.sql.SQLSelectStatement;
import oracle.retail.stores.common.sql.SQLUpdateStatement;
import oracle.retail.stores.common.utility.StringUtils;
import oracle.retail.stores.common.utility.Util;
import oracle.retail.stores.domain.arts.ARTSCustomer;
import oracle.retail.stores.domain.arts.JdbcSaveCustomer;
import oracle.retail.stores.domain.customer.CustomerGroupIfc;
import oracle.retail.stores.domain.discount.DiscountRuleIfc;
import oracle.retail.stores.domain.utility.AddressIfc;
import oracle.retail.stores.domain.utility.CodeConstantsIfc;
import oracle.retail.stores.domain.utility.EmailAddressIfc;
import oracle.retail.stores.domain.utility.PhoneConstantsIfc;
import oracle.retail.stores.domain.utility.PhoneIfc;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.foundation.manager.device.EncipheredDataIfc;
import oracle.retail.stores.foundation.manager.ifc.KeyStoreEncryptionManagerIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataConnectionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataTransactionIfc;
import oracle.retail.stores.foundation.tour.gate.Gateway;
import oracle.retail.stores.persistence.utility.ARTSDatabaseIfc;

import org.apache.log4j.Logger;

/**
 * This operation takes a POS domain Customer and creates a new entry in the
 * database.
 * 
 * @version $Revision: /rgbustores_13.4x_generic_branch/10 $
 */
public class GDYNJdbcSaveCustomer extends JdbcSaveCustomer implements ARTSDatabaseIfc, CodeConstantsIfc
{
    private static final long serialVersionUID = 491296793326204649L;

    /** The logger to which log messages will be sent. */
    private static final Logger logger = Logger.getLogger(GDYNJdbcSaveCustomer.class);

    /**
     * Saves the customer information to the database.
     * <p>
     *
     * @param dataConnection The connection to the data source
     * @param artsCustomer The customer information to save
     * @exception DataException upon error
     */
    public void saveCustomer(JdbcDataConnection dataConnection, ARTSCustomer artsCustomer) throws DataException
    {
        try
        {
            int partyID = getPartyIDFromCustomer(dataConnection, artsCustomer);
            artsCustomer.setPartyId(partyID);

            if (partyID == 0)
            {
                artsCustomer.setPartyId(generatePartyID(dataConnection));
                // get customer id
                artsCustomer.getPosCustomer().setCustomerID(artsCustomer.getPosCustomer().getCustomerID());

                insertParty(dataConnection, artsCustomer);
                insertCustomer(dataConnection, artsCustomer);
                insertContact(dataConnection, artsCustomer);
                insertAddress(dataConnection, artsCustomer);
                insertEmailAddress(dataConnection, artsCustomer);
                insertPhone(dataConnection, artsCustomer);
                insertGroups(dataConnection, artsCustomer);
                insertBusinessInfo(dataConnection, artsCustomer);
            }
            else
            {
                updateCustomer(dataConnection, artsCustomer);
                updateContact(dataConnection, artsCustomer);
                updateAddress(dataConnection, artsCustomer);
                updateEmailAddress(dataConnection, artsCustomer);
                updatePhone(dataConnection, artsCustomer);
                updateGroups(dataConnection, artsCustomer);
                updateBusinessInfo(dataConnection, artsCustomer);
            }
        }
        catch (DataException de)
        {
            throw de;
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, e.toString());
        }
    }

    /**
     * Inserts into the customer table.
     * <P>
     * 
     * @param dataConnection The connection to the data source
     * @param artsCustomer The customer information to save
     * @exception DataException upon error
     */
    protected void insertCustomer(JdbcDataConnection dataConnection,
                                  ARTSCustomer artsCustomer)
        throws DataException
    {
        SQLInsertStatement sql = new SQLInsertStatement();

        // Table
        sql.setTable(TABLE_CUSTOMER);

        // Fields
        sql.addColumn(FIELD_CUSTOMER_ID, getCustomerID(artsCustomer));
        sql.addColumn(FIELD_PARTY_ID, getPartyID(artsCustomer));
        sql.addColumn(FIELD_CUSTOMER_NAME, getCustomerName(artsCustomer));
        sql.addColumn(FIELD_CUSTOMER_STATUS, getCustomerStatus(artsCustomer));
        sql.addColumn(FIELD_EMPLOYEE_ID, getEmployeeID(artsCustomer));
        sql.addColumn(FIELD_HOUSE_ACCOUNT_NUM, getHouseAccountNumber(artsCustomer));
        sql.addColumn(FIELD_MASKED_HOUSE_ACCOUNT_NUM, getMaskedHouseAccountNumber(artsCustomer));
        if (getCustomerLocale(artsCustomer) != null)
        {
          sql.addColumn(FIELD_LOCALE, getCustomerLocale(artsCustomer));
        }
        //Pak commented 02/05/14
        //Mansi uncommented /14/04/2015
        sql.addColumn(FIELD_ENCRYPTED_CUSTOMER_TAX_ID, getEncryptedCustomerTaxID(artsCustomer));
        sql.addColumn(FIELD_HASHED_CUSTOMER_TAX_ID, getHashedCustomerTaxID(artsCustomer));
        sql.addColumn(FIELD_MASKED_CUSTOMER_TAX_ID, getMaskedCustomerTaxID(artsCustomer));
        // if pricing group selected is not None
        if (artsCustomer.getPosCustomer().getPricingGroupID() != null
                && artsCustomer.getPosCustomer().getPricingGroupID() > 0)
        {
            sql.addColumn(FIELD_CUSTOMER_PRICING_GROUP_ID, artsCustomer.getPosCustomer().getPricingGroupID());
        }
        else
        {
            sql.addColumn(FIELD_CUSTOMER_PRICING_GROUP_ID, null);
        }

        try
        {
            dataConnection.execute(sql.getSQLString(), false);
        }
        catch (DataException de)
        {
            logger.error(de);
            throw de;
        }
        catch (Exception e)
        {
            logger.error(e);
            throw new DataException(DataException.UNKNOWN, "insertCustomer", e);
        }
    }

    /**
     * Updates the customer table.
     *
     * @param dataConnection The connection to the data source
     * @param artsCustomer The customer information to save
     * @exception DataException upon error
     */
    protected void updateCustomer(JdbcDataConnection dataConnection, ARTSCustomer artsCustomer) throws DataException
    {
        SQLUpdateStatement sql = new SQLUpdateStatement();

        // Table
        sql.setTable(TABLE_CUSTOMER);

        // Fields
        sql.addColumn(FIELD_CUSTOMER_STATUS, getCustomerStatus(artsCustomer));
        sql.addColumn(FIELD_EMPLOYEE_ID, getEmployeeID(artsCustomer));
        sql.addColumn(FIELD_LOCALE, getCustomerLocale(artsCustomer));
        sql.addColumn(FIELD_CUSTOMER_NAME, getCustomerName(artsCustomer));
        sql.addColumn(FIELD_HOUSE_ACCOUNT_NUM, getHouseAccountNumber(artsCustomer));
        sql.addColumn(FIELD_MASKED_HOUSE_ACCOUNT_NUM, getMaskedHouseAccountNumber(artsCustomer));
        //Pak commented 02/05/14
        //Mansi uncommented 14/04/2015
        sql.addColumn(FIELD_ENCRYPTED_CUSTOMER_TAX_ID, getEncryptedCustomerTaxID(artsCustomer));
        sql.addColumn(FIELD_HASHED_CUSTOMER_TAX_ID, getHashedCustomerTaxID(artsCustomer));
        sql.addColumn(FIELD_MASKED_CUSTOMER_TAX_ID, getMaskedCustomerTaxID(artsCustomer));
        // if pricing group selected is not None
        if (artsCustomer.getPosCustomer().getPricingGroupID() != null
                && artsCustomer.getPosCustomer().getPricingGroupID() > 0)
        {
            sql.addColumn(FIELD_CUSTOMER_PRICING_GROUP_ID, artsCustomer.getPosCustomer().getPricingGroupID());
        }
        else
        {
            sql.addColumn(FIELD_CUSTOMER_PRICING_GROUP_ID, null);
        }
        sql.addColumn(FIELD_CUSTOMER_TLOG_BATCH_IDENTIFIER, ID_CT_BATCH_DEFAULT);
        // Qualifiers
        sql.addQualifier(FIELD_CUSTOMER_ID + " = " + getCustomerID(artsCustomer));
        sql.addQualifier(FIELD_PARTY_ID + " = " + getPartyID(artsCustomer));

        try
        {
            dataConnection.execute(sql.getSQLString(), false);
        }
        catch (DataException de)
        {
            logger.error("" + de + "");
            throw de;
        }
        catch (Exception e)
        {
            logger.error("" + e + "");
            throw new DataException(DataException.UNKNOWN, "updateCustomer", e);
        }

        if (0 >= dataConnection.getUpdateCount())
        {
            throw new DataException(DataException.NO_DATA, "Unable to update customer.");
        }
    }
}
