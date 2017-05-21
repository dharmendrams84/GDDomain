/* ===========================================================================
* Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/modules/domain/src/oracle/retail/stores/domain/arts/JdbcSelectCustomers.java /rgbustores_13.4x_generic_branch/7 2011/11/07 13:28:29 asinton Exp $
 * ===========================================================================
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED    (MM/DD/YY)
 *    asinton   11/05/11 - fixed retrieval of customer tax ID.
 *    yiqzhao   10/12/11 - add first/last name virtual columns in pa_cnct for
 *                         improving performance
 *    cgreene   08/22/11 - removed deprecated methods
 *    mkutiana  08/17/11 - Removed deprecated Customer.ID_HSH_ACNT from DB and
 *                         all using classes
 *    masahu    07/21/11 - Encryption CR: POSLog/DTM and RM 2 POS Integration
 *                         fixes
 *    masahu    07/07/11 - FORTIFY FIX: The sensitive SQLs get logged
 *    cgreene   05/27/10 - convert to oracle packaging
 *    cgreene   05/27/10 - convert to oracle packaging
 *    cgreene   05/26/10 - convert to oracle packaging
 *    cgreene   04/28/10 - updating deprecated names
 *    cgreene   04/26/10 - XbranchMerge cgreene_tech75 from
 *                         st_rgbustores_techissueseatel_generic_branch
 *    cgreene   03/30/10 - remove deprecated ARTSDatabaseIfcs and change
 *                         SQLException to DataException
 *    mpbarnet  02/26/10 - In buildQuery(), add country code as a qualifier if
 *                         provided as a search criterion.
 *    abondala  01/03/10 - update header date
 *    mpbarnet  09/18/09 - In buildQuery(), make safe the address search
 *                         criteria.
 *    mkochumm  02/18/09 - retrieve customer preferred locale
 *    mahising  12/04/08 - JUnit fix and SQL fix
 *    mahising  11/13/08 - Added for Customer module for both ORPOS and ORCO
 *    acadar    11/03/08 - localization of reason codes for discounts and
 *                         merging to tip
 *
 * ===========================================================================
 * $Log:
 |    11   360Commerce 1.10        4/15/2008 12:53:53 PM  Charles D. Baker CR
 |         31088 - Corrected retrieval of customer account information to
 |         preserve null status in the database. This allows consistent
 |         persistence of updated customer information. Code reviewed by Leona
 |          and Dwight.
 |    10   360Commerce 1.9         3/5/2008 10:22:04 PM   Manikandan Chellapan
 |         CR#29874 Removed hardcoded select statement
 |    9    360Commerce 1.8         1/17/2008 5:24:06 PM   Alan N. Sinton  CR
 |         29954: Refactor of EncipheredCardData to implement interface and be
 |          instantiated using a factory.
 |    8    360Commerce 1.7         1/9/2008 10:54:51 AM   Alan N. Sinton  CR
 |         29849: Updated SQL to use new columns on customer table.
 |    7    360Commerce 1.6         12/18/2007 5:47:48 PM  Alan N. Sinton  CR
 |         29661: Changes per code review.
 |    6    360Commerce 1.5         9/20/2007 11:29:19 AM  Rohit Sachdeva
 |         28813: Initial Bulk Migration for Java 5 Source/Binary
 |         Compatibility of All Products
 |    5    360Commerce 1.4         6/15/2006 5:57:54 PM   Brett J. Larsen CR
 |         18490 - UDM - PA_CT.ST_CT renamed to STS_CT
 |    4    360Commerce 1.3         1/25/2006 4:11:25 PM   Brett J. Larsen merge
 |          7.1.1 changes (aka. 7.0.3 fixes) into 360Commerce view
 |    3    360Commerce 1.2         3/31/2005 4:28:45 PM   Robert Pearse
 |    2    360Commerce 1.1         3/10/2005 10:22:51 AM  Robert Pearse
 |    1    360Commerce 1.0         2/11/2005 12:12:05 PM  Robert Pearse
 |   $:
 |    4    .v700     1.2.1.0     11/16/2005 16:25:36    Jason L. DeLeau 4215:
 |         Get rid of redundant ArtsDatabaseifc class
 |    3    360Commerce1.2         3/31/2005 15:28:45     Robert Pearse
 |    2    360Commerce1.1         3/10/2005 10:22:51     Robert Pearse
 |    1    360Commerce1.0         2/11/2005 12:12:05     Robert Pearse
 |   $
 |   Revision 1.8  2004/08/09 14:41:06  kll
 |   @scr 6797: use factory to create customer domain object
 |
 |   Revision 1.7  2004/07/28 19:54:28  dcobb
 |   @scr 6355 Can still search on original business name after it was changed
 |   Modified JdbcSelectBusiness to search for name from pa_cnct table.
 |
 |   Revision 1.6  2004/04/09 16:55:45  cdb
 |   @scr 4302 Removed double semicolon warnings.
 |
 |   Revision 1.5  2004/02/17 17:57:36  bwf
 |   @scr 0 Organize imports.
 |
 |   Revision 1.4  2004/02/17 16:18:45  rhafernik
 |   @scr 0 log4j conversion
 |
 |   Revision 1.3  2004/02/12 17:13:19  mcs
 |   Forcing head revision
 |
 |   Revision 1.2  2004/02/11 23:25:22  bwf
 |   @scr 0 Organize imports.
 |
 |   Revision 1.1.1.1  2004/02/11 01:04:28  cschellenger
 |   updating to pvcs 360store-current
 |
 |
 |
 |    Rev 1.0   Aug 29 2003 15:33:12   CSchellenger
 | Initial revision.
 |
 |    Rev 1.2   Mar 20 2003 18:12:24   baa
 | Refactoring of customer screens
 | Resolution for POS SCR-2098: Refactoring of Customer Service Screens
 |
 |    Rev 1.1   Jul 07 2002 15:56:24   dfh
 | fixed in 5.1, needed here, reads email address for customer
 | Resolution for POS SCR-1738: Customer's email can't be displayed at customer contact screen
 |
 |    Rev 1.0   Jun 03 2002 16:40:40   msg
 | Initial revision.
 |
 |    Rev 1.2   18 May 2002 17:16:04   sfl
 | Using upper case in LIKE qualifier to handle the matching search situations when database
 | sever to be configured either case-sensitive or case-insensitive.
 | Resolution for POS SCR-1666: Employee - Search by employee name cannot find existing employees
 |
 |    Rev 1.1   Mar 18 2002 22:49:14   msg
 | - updated copyright
 |
 |    Rev 1.0   Mar 18 2002 12:08:54   msg
 | Initial revision.
 |
 |    Rev 1.0   Sep 20 2001 15:57:38   msg
 | Initial revision.
 |
 |    Rev 1.1   Sep 17 2001 12:33:50   msg
 | header update
 * ===========================================================================
 */
package oracle.retail.stores.domain.arts;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import oracle.retail.stores.persistence.utility.ARTSDatabaseIfc;

import org.apache.log4j.Logger;

import oracle.retail.stores.common.sql.SQLSelectStatement;
import oracle.retail.stores.common.utility.LocalizedCodeIfc;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.customer.CustomerConstantsIfc;
import oracle.retail.stores.domain.customer.CustomerGroupIfc;
import oracle.retail.stores.domain.customer.CustomerIfc;
import oracle.retail.stores.domain.customer.CustomerSort;
import oracle.retail.stores.domain.discount.DiscountRuleIfc;
import oracle.retail.stores.domain.utility.AddressIfc;
import oracle.retail.stores.domain.utility.PhoneIfc;
import oracle.retail.stores.foundation.factory.FoundationObjectFactory;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.foundation.manager.device.EncipheredCardDataIfc;
import oracle.retail.stores.foundation.manager.device.EncipheredDataIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataConnectionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataTransactionIfc;

/**
 * This operation selects Customer data from the database based on name,
 * address, and phone number.
 *
 * @version $Revision: /rgbustores_13.4x_generic_branch/7 $
 */
public class JdbcSelectCustomers extends JdbcDataOperation implements ARTSDatabaseIfc
{
    private static final long serialVersionUID = 5017561206166542639L;
    /** The logger to which log messages will be sent. */
    private static final Logger logger = Logger.getLogger(JdbcSelectCustomers.class);

    /**
     * Class constructor.
     */
    public JdbcSelectCustomers()
    {
        super();
        setName("JdbcSelectCustomers");
    }

    /**
     * Execute the SQL statements against the database.
     *
     * @param dataTransaction
     * @param dataConnection
     * @param action
     * @exception DataException
     */
    public void execute(DataTransactionIfc dataTransaction, DataConnectionIfc dataConnection,DataActionIfc action)  throws DataException
    {
        // get the input Customer object that contains search criteria
        CustomerIfc posCustomer = (CustomerIfc) action.getDataObject();

        // Remove elements from previous searches.
        Vector<CustomerIfc> customers = new Vector<CustomerIfc>();

        // attempt the database access
        try
        {
            // build an SQL query String
            String sqlString = buildQuery(posCustomer, customers);
            logger.debug("The SQL String is:  " + sqlString);
            
            // execute the SQL query
            dataConnection.execute(sqlString, false);

            // get the result set
            ResultSet rs = (ResultSet)dataConnection.getResult();

            // loop through the result set, building the returned set of Customers
            String encryptedHouseAccount = null;
            String maskedHouseAccount = null;
            String encryptedTaxID = null;
            String maskedTaxID = null;
            while(rs.next())
            {
                // create a new Customer object
                CustomerIfc customer =
                    DomainGateway.getFactory().getCustomerInstance();

                // set the Customer ID and Record ID (party ID) that met the search criteria
                customer.setCustomerID(getSafeString(rs,1));
                customer.setRecordID(getSafeString(rs,2));
                customer.setEmployeeID(getSafeString(rs,3));
                customer.setStatus(rs.getInt(4));
                // CR 31088. The enciphered card data account number fields are null
                // by default. Do not use getSafeString to get them or the data values
                // in the database will be changed, affecting the POSLog formatting.
                encryptedHouseAccount = rs.getString(5);
                maskedHouseAccount = rs.getString(6);

                // Adding tax ID and pricing group ID in customer
                maskedTaxID = rs.getString(7);
                encryptedTaxID = rs.getString(8);
                int pricingGroupId = rs.getInt(9);
                if (pricingGroupId > 0)
                    customer.setPricingGroupID(pricingGroupId);

                EncipheredCardDataIfc cardData =
                    FoundationObjectFactory.getFactory().createEncipheredCardDataInstance(
                                           encryptedHouseAccount,
                                           maskedHouseAccount,
                                           null);
                customer.setHouseCardData(cardData);

                EncipheredDataIfc customerTaxID =
                        FoundationObjectFactory.getFactory()
                        .createEncipheredDataInstance(
                                           encryptedTaxID,
                                           maskedTaxID);
                customer.setEncipheredTaxID(customerTaxID);

                // add the Customer object to the returned set
                customers.addElement(customer);
            }

            if (customers.size() == 0)
            {
                throw new DataException(DataException.NO_DATA,
                                        "No customers were found proccessing the result set in JdbcSelectCustomers.");
            }

            // Get contact, address, and phone info for the Customers
            for (CustomerIfc customer : customers)
            {
                // We're going to issue three statements:  first to retrieve the
                // contact information, second to retrieve the addresses, and third
                // to retrieve the phone numbers.
                int partyID = Integer.parseInt(customer.getRecordID());
                selectContactInfo(dataConnection, customer, partyID);

                //set customer preferred locale
                selectCustomerLocale(dataConnection, customer, partyID);

                // set Addresses to reflect input search parameters
                customer.setAddresses(posCustomer.getAddresses());
                selectAddressInfo(dataConnection, customer, partyID);
                // set email address
                selectEmailInfo(dataConnection, customer, partyID);

                selectPhoneInfo(dataConnection, customer, partyID);
                selectGroupInfo(dataConnection, customer);
            }

            // sort the returned set of Customers
            CustomerSort.sort(customers);

            dataTransaction.setResult(customers);
        }
        catch(SQLException e)
        {
            ((JdbcDataConnection)dataConnection).logSQLException(e, "Processing result set.");
            throw new DataException(DataException.SQL_ERROR,
                                    "An SQL Error occurred proccessing the result set from selecting customers in JdbcSelectCustomers.", e);
        }

    }

    /**
       Read from the contact table. <P>
       @param  the data connection on which to execute.
       @param  cusomer to hold the data
       @param  unique identifier for a party
       @exception  DataException thrown when an error occurs executing the
       against the DataConnection
       @exception  SQLException thrown when an error occurs with the
       ResultSet
     */
    public void selectContactInfo(DataConnectionIfc dataConnection, CustomerIfc customer, int partyID) throws DataException, SQLException
    {
        String sqlString = ReadARTSCustomerSQL.selectContactInfoSQL(partyID);

        dataConnection.execute(sqlString, false);

        ResultSet rs = (ResultSet)dataConnection.getResult();

        ReadARTSCustomerSQL.readContactResultsForCustomer(rs, customer);

        rs.close();
    }

    /**
       Read from the customer table. <P>
       @param  the data connection on which to execute.
       @param  cusomer to hold the data
       @param  unique identifier for a party
       @exception  DataException thrown when an error occurs executing the
       against the DataConnection
       @exception  SQLException thrown when an error occurs with the
       ResultSet
     */
    public void selectCustomerLocale(DataConnectionIfc dataConnection, CustomerIfc customer, int partyID) throws DataException, SQLException
    {
        String sqlString = ReadARTSCustomerSQL.selectCustomerLocaleSQL(partyID);

        dataConnection.execute(sqlString);

        ResultSet rs = (ResultSet)dataConnection.getResult();

        ReadARTSCustomerSQL.readCustomerLocaleForCustomer(rs, customer);

        rs.close();
    }

    /**
       Select all addresses from the address table. <P>
       @param  the data connection on which to execute.
       @param  customer to hold the data
       @param  unique identifier for a party
       @exception  DataException thrown when an error occurs executing the
       against the DataConnection
       @exception  SQLException thrown when an error occurs with the
       ResultSet
     */
    public void selectAddressInfo(DataConnectionIfc dataConnection, CustomerIfc customer, int partyID) throws DataException, SQLException
    {
        if (logger.isDebugEnabled()) logger.debug( "JdbcReadCustomer.selectAddressInfo");

        String sqlString = ReadARTSCustomerSQL.selectAddressInfoSQL(partyID, customer);

        dataConnection.execute(sqlString, false);

        ResultSet rs = (ResultSet)dataConnection.getResult();

        ReadARTSCustomerSQL.readAddressResultsForCustomer(rs, customer);

        rs.close();

        if (logger.isDebugEnabled()) logger.debug( "JdbcReadCustomer.selectAddressInfo");
    }

    /**
       Select all email addresses from the email address table. <P>
       @param  the data connection on which to execute.
       @param  customer to hold the data
       @param  unique identifier for a party
       @exception  DataException thrown when an error occurs executing the
       against the DataConnection
       @exception  SQLException thrown when an error occurs with the
       ResultSet
     */
    public void selectEmailInfo
    (
     DataConnectionIfc dataConnection,
     CustomerIfc       customer,
     int               partyID
    )   throws DataException, SQLException
    {
        if (logger.isDebugEnabled()) logger.debug( "JdbcSelectCustomers.selectEmailInfo");

        String sqlString = ReadARTSCustomerSQL.selectEmailInfoSQL(partyID);

        dataConnection.execute(sqlString, false);

        ResultSet rs = (ResultSet)dataConnection.getResult();

        ReadARTSCustomerSQL.readEmailResultsForCustomer(rs, customer);

        rs.close();

        if (logger.isDebugEnabled()) logger.debug( "JdbcSelectCustomers.selectEmailInfo");
    }

    /**
       Select all phone numbers from the phone table. <P>
       @param  the data connection on which to execute.
       @param  cusomer to hold the data
       @param  unique identifier for a party
       @exception  DataException thrown when an error occurs executing the
       against the DataConnection
       @exception  SQLException thrown when an error occurs with the
       ResultSet
     */
    public void selectPhoneInfo
    (
     DataConnectionIfc dataConnection,
     CustomerIfc       customer,
     int               partyID
     )   throws DataException, SQLException
    {
        // Need to figure out what to do with telephone numbers in
        // address table.
        String sqlString = ReadARTSCustomerSQL.selectPhoneInfoSQL(partyID);

        dataConnection.execute(sqlString, false);

        ResultSet rs = (ResultSet)dataConnection.getResult();

        ReadARTSCustomerSQL.readPhoneResultsForCustomer(rs, customer);

        rs.close();
    }

    /**
     *Select all the groups assigned to this customer. <P>
     *@param  dataConnection The data connection on which to execute.
     *@param  customer  The output Customer with retrieved data
     *@exception  DataException thrown when an error occurs executing the
     *against the DataConnection
     *@exception  SQLException thrown when an error occurs with the
     *ResultSet
     */
    public void selectGroupInfo(DataConnectionIfc dataConnection, CustomerIfc customer)
        throws DataException, SQLException
    {
        if (logger.isDebugEnabled()) logger.debug( "JdbcSelectCustomers.selectGroupInfo");
        // build SQL for groups
        SQLSelectStatement sql =
            ReadARTSCustomerSQL.buildCustomerGroupSQL(customer.getCustomerID(), customer.getLocaleRequestor());

        // execute and retrieve results
        dataConnection.execute(sql.getSQLString());
        ResultSet rs = (ResultSet) dataConnection.getResult();

        // parse result set
        CustomerGroupIfc[] groups = ReadARTSCustomerSQL.readCustomerGroupResultSet(rs);

        rs.close();

        if (groups != null)
        {
            customer.setCustomerGroups(groups);
            selectDiscountInfo(dataConnection, customer);
        }

        if (logger.isDebugEnabled()) logger.debug( "JdbcSelectCustomers.selectGroupInfo");
    }



    /**
     *Select all the discount rules available to this customer. <P>
     *@param  dataConnection The data connection on which to execute.
     *@param  customer  The output Customer with retrieved data
     *@exception  DataException thrown when an error occurs executing the
     *against the DataConnection
     *@exception  SQLException thrown when an error occurs with the
     *ResultSet
    */
   public void selectDiscountInfo(DataConnectionIfc dataConnection, CustomerIfc customer)
       throws DataException, SQLException
   {
       if (logger.isDebugEnabled()) logger.debug( "JdbcSelectCustomers.selectDiscountInfo");

       ResultSet rs = null;
       DiscountRuleIfc[] discounts = null;
       DiscountRuleIfc discount = null;
       SQLSelectStatement sql = null;
       // loop through groups for discount rules
       CustomerGroupIfc[] groups = customer.getCustomerGroups();
       int numGroups = 0;
       if (groups != null)
       {
           numGroups = groups.length;
       }
       LocalizedCodeIfc reasonCode = null;
       for (int i = 0; i < numGroups; i++)
       {
           sql = ReadARTSCustomerSQL.buildCustomerDiscountSQL(groups[i].getGroupID());

           dataConnection.execute(sql.getSQLString());
           rs = (ResultSet) dataConnection.getResult();
           discounts = ReadARTSCustomerSQL.readCustomerDiscountResultSet(rs);

           if (discounts != null)
           {
               for (int j = 0; j < discounts.length; j++ )
               {
                   discount = discounts[j];
                   sql = ReadARTSCustomerSQL.buildLocalizedRuleSQL(discount.getRuleID(), customer.getLocaleRequestor());
                   dataConnection.execute(sql.getSQLString());
                   rs = (ResultSet) dataConnection.getResult();
                   discount = ReadARTSCustomerSQL.readLocalizedRule(rs, discount);
                   reasonCode = DomainGateway.getFactory().getLocalizedCode();
                   reasonCode.setCode(groups[i].getGroupID());
                   reasonCode.setText(groups[i].getLocalizedNames());
                   discount.setReason(reasonCode);
                   groups[i].addDiscountRule(discount);
               }
           }
           rs.close();
       }

       if (logger.isDebugEnabled()) logger.debug( "JdbcSelectCustomers.selectDiscountInfo");
   }

    /**
       @throws SQLException
     * @exception  Exception thrown when an error occurs
     */
    @SuppressWarnings("deprecation")
    protected String buildQuery(CustomerIfc posCustomer, Vector<CustomerIfc> customers)
        throws DataException
    {

        AddressIfc addr;
        String firstName;
        String lastName;
        String line1 = "";
        String postalCode;
        String postalCodeExt;
        String countryCode;
        Vector<String> lines;
        Vector<AddressIfc> addrVector;


        if (logger.isDebugEnabled()) logger.debug( "JdbcSelectCustomers.buildQuery()");

        SQLSelectStatement sql = new SQLSelectStatement();

        // Add tables
        sql.addTable(TABLE_CUSTOMER, ALIAS_CUSTOMER);
        sql.addTable(TABLE_CONTACT, ALIAS_CONTACT);
        sql.addTable(TABLE_ADDRESS, ALIAS_ADDRESS);
        sql.addTable(TABLE_PHONE, ALIAS_PHONE);

        // Set distinct flag to true
        sql.setDistinctFlag(true);

        // Add Coloumns
        sql.addColumn(ALIAS_CUSTOMER, FIELD_CUSTOMER_ID);
        sql.addColumn(ALIAS_CUSTOMER, FIELD_PARTY_ID);
        sql.addColumn(ALIAS_CUSTOMER, FIELD_EMPLOYEE_ID);
        sql.addColumn(ALIAS_CUSTOMER, FIELD_CUSTOMER_STATUS);
        sql.addColumn(ALIAS_CUSTOMER, FIELD_HOUSE_ACCOUNT_NUM);
        sql.addColumn(ALIAS_CUSTOMER, FIELD_MASKED_HOUSE_ACCOUNT_NUM);

        // Adding two columns tax id and pricing group
        sql.addColumn(ALIAS_CUSTOMER, FIELD_MASKED_CUSTOMER_TAX_ID);
        sql.addColumn(ALIAS_CUSTOMER, FIELD_ENCRYPTED_CUSTOMER_TAX_ID);
        sql.addColumn(ALIAS_CUSTOMER, FIELD_CUSTOMER_PRICING_GROUP_ID);

        // Add where clause elements
        sql.addJoinQualifier(ALIAS_CUSTOMER,FIELD_PARTY_ID,ALIAS_CONTACT,FIELD_PARTY_ID);
        sql.addJoinQualifier(ALIAS_CUSTOMER,FIELD_PARTY_ID,ALIAS_ADDRESS,FIELD_PARTY_ID);
        sql.addQualifier(ALIAS_CUSTOMER + "." + FIELD_CUSTOMER_STATUS + " <> " + CustomerConstantsIfc.CUSTOMER_STATUS_DELETED);

        // if a first name is given, include it in the lookup
        firstName = posCustomer.getFirstName();
        if (firstName != null &&
            firstName.length() > 0)
        {

            // Add customer first name to query
            sql.addQualifier(ALIAS_CONTACT + "." + FIELD_CONTACT_FIRST_NAME_UPPER_CASE + " LIKE UPPER(" +  makeSafeString(firstName + "%") + ")");

        }

        // if a last name is given, include it in the lookup
        lastName = posCustomer.getLastName();
        if (lastName != null &&
            lastName.length() > 0)
        {
            // Add customer last name to query
            sql.addQualifier(ALIAS_CONTACT + "." + FIELD_CONTACT_LAST_NAME_UPPER_CASE + " LIKE UPPER(" +  makeSafeString(lastName + "%") + ")");

        }

        // if any address line parameters are given, include them in the lookup
        addrVector = posCustomer.getAddresses();

        // if there is at least one Address object in the Vector
        if (addrVector.size() >= 1)
        {
            // Get Address object
            addr = addrVector.get(0);

            // Get Vector of address lines
            lines = addr.getLines();

            // if there is at least one address line in the Vector
            if (lines.size() >= 1)
            {
                // Get address lines
                line1 = lines.get(0);
            }

            // get the other search parameters from the Address object
            postalCode = addr.getPostalCode();
            postalCodeExt = addr.getPostalCodeExtension();
            countryCode = addr.getCountry();

            // append the postal code extension to the postal code
            if (postalCodeExt != null && postalCodeExt.length() > 0)
                postalCode = postalCode.concat("-" + postalCodeExt);

            // if there was a search parameter for Address Line 1
            if (line1 != null && line1.length() > 0)
            {

                sql.addQualifier("UPPER(" +  FIELD_TILL_PAYMENT_ADDRESS_LINE_1+ ") LIKE UPPER(" +  makeSafeString(line1 + "%") +")");

            }


            // if there was a search parameter for postal code
            if (postalCode != null && postalCode.length() > 0)
            {

                sql.addQualifier("UPPER(" +  FIELD_CONTACT_POSTAL_CODE + ") LIKE UPPER('" +  postalCode + "%')");

            }

            // if there was a search parameter for country
            if (countryCode != null && countryCode.length() > 0)
            {

                sql.addQualifier("UPPER(" + FIELD_CONTACT_COUNTRY + ") LIKE UPPER ('" + countryCode + "%')");

            }

        }

        PhoneIfc phone = null;
        Vector<PhoneIfc> phones = posCustomer.getPhones();

        // if there was a phone number entered
        if(phones != null && phones.size() > 0)
        {

            phone = phones.get(0);
            if(phone != null)
            {
                String ac = phone.getAreaCode();
                String pn = phone.getPhoneNumber();

                sql.addJoinQualifier(ALIAS_CUSTOMER, FIELD_PARTY_ID, ALIAS_PHONE, FIELD_PARTY_ID);
                sql.addQualifier(ALIAS_PHONE, FIELD_PHONE_TYPE, makeSafeString(String.valueOf(phone.getPhoneType())));

                if(ac != null)
                {
                    sql.addQualifier("UPPER(" + ALIAS_PHONE + "." + FIELD_CONTACT_AREA_TELEPHONE_CODE + ") LIKE UPPER('" + ac + "%')");

                }

                if(pn != null)
                {
                    sql.addQualifier("UPPER(" + ALIAS_PHONE + "." + FIELD_CONTACT_LOCAL_TELEPHONE_NUMBER + ") LIKE UPPER('" + pn + "%')");
                }

            }
        }

        if (logger.isDebugEnabled())
        {
            logger.debug( "JdbcSelectCustomers.buildQuery()");
        }

        /* Return Sql String : SELECT DISTINCT CU.ID_CT, CU.ID_PRTY, CU.ID_EM, CU.STS_CT, CU.ID_NCRPT_ACTN_CRD,
         * CU.ID_MSK_ACNT_CRD FROM PA_PHN PH, PA_CNCT CNT, LO_ADS AD, PA_CT CU
         * WHERE CU.ID_PRTY = CNT.ID_PRTY AND CU.ID_PRTY = AD.ID_PRTY AND CU.STS_CT <> 2 AND
         * UPPER(CNT.FN_CNCT) LIKE UPPER('FirstName%') AND UPPER(CNT.LN_CNCT) LIKE UPPER('LastName%') AND
         * UPPER(A1_CNCT) LIKE UPPER('AddressLine1%') AND UPPER(PC_CNCT) LIKE UPPER('PostalCode%') */

        return sql.getSQLString();
    }

    /**
       Set all data members should be set to their initial state. <P>
       <B>Pre-Condition</B>
       <UL>
       <LI>
       All processing must be complete
       <LI>
       </UL>
       <B>Post-Condition</B>
       <UL>
       <LI>
       All data member have been returned to the initial state.
       </UL>
     */
    public void initialize() throws DataException
    {
    }
}
