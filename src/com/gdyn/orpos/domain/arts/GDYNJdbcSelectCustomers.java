//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.arts;

import java.sql.SQLException;
import java.util.Vector;

import oracle.retail.stores.common.sql.SQLSelectStatement;
import oracle.retail.stores.common.utility.Util;
import oracle.retail.stores.domain.arts.JdbcSelectCustomers;
import oracle.retail.stores.domain.customer.CustomerConstantsIfc;
import oracle.retail.stores.domain.customer.CustomerIfc;
import oracle.retail.stores.domain.utility.AddressIfc;
import oracle.retail.stores.domain.utility.EmailAddressConstantsIfc;
import oracle.retail.stores.domain.utility.EmailAddressIfc;
import oracle.retail.stores.domain.utility.PhoneIfc;
import oracle.retail.stores.foundation.manager.data.DataException;

import org.apache.log4j.Logger;

import com.gdyn.orpos.persistence.utility.GDYNARTSDatabaseIfc;

/**
 * This operation selects Customer data from the database based on name,
 * address, and phone number.
 * Extended to be able to search customers by email address
 */
public class GDYNJdbcSelectCustomers extends JdbcSelectCustomers implements GDYNARTSDatabaseIfc
{
    private static final long serialVersionUID = 5017561206166542639L;
    
    /** The logger to which log messages will be sent. */
    private static final Logger logger = Logger.getLogger(GDYNJdbcSelectCustomers.class);

    /**
     * Class constructor.
     */
    public GDYNJdbcSelectCustomers()
    {
        super();
        setName("GDYNJdbcSelectCustomers");
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


        if (logger.isDebugEnabled()) logger.debug( "GDYNJdbcSelectCustomers.buildQuery()");

        SQLSelectStatement sql = new SQLSelectStatement();

        // Add tables
        sql.addTable(TABLE_CUSTOMER, ALIAS_CUSTOMER);
        sql.addTable(TABLE_CONTACT, ALIAS_CONTACT);
        sql.addTable(TABLE_ADDRESS, ALIAS_ADDRESS);
        sql.addTable(TABLE_PHONE, ALIAS_PHONE);
        // Begin GD-343: Modify customer search/add screens for customer capture fields
        // lcatania (Starmount) Apr 5, 2013
        sql.addTable(TABLE_EMAIL_ADDRESS, ALIAS_EMAIL_ADDRESS);
        // End GD-343: Modify customer search/add screens for customer capture fields

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
                // Begin GD-380: Getting invalid data notice at tax exempt customer link 
                // if phone type is something other than "Home"
                // lcatania (Starmount) Apr 23, 2013
                // sql.addQualifier(ALIAS_PHONE, FIELD_PHONE_TYPE, makeSafeString(String.valueOf(phone.getPhoneType())));
                // End GD-380: Getting invalid data notice at tax exempt customer link...

                if(!Util.isEmpty(ac))
                {
                    sql.addQualifier("UPPER(" + ALIAS_PHONE + "." + FIELD_CONTACT_AREA_TELEPHONE_CODE + ") LIKE UPPER('" + ac + "%')");

                }

                if(!Util.isEmpty(pn))
                {
                    sql.addQualifier("UPPER(" + ALIAS_PHONE + "." + FIELD_CONTACT_LOCAL_TELEPHONE_NUMBER + ") LIKE UPPER('" + pn + "%')");
                }

            }
        }

        // Begin GD-343: Modify customer search/add screens for customer capture fields
        // lcatania (Starmount) Apr 5, 2013
        EmailAddressIfc email = posCustomer.getEmailAddress(EmailAddressConstantsIfc.EMAIL_ADDRESS_TYPE_HOME);

        // if there was an email address entered
        if(email != null)
        {
                sql.addJoinQualifier(ALIAS_CUSTOMER, FIELD_PARTY_ID, ALIAS_EMAIL_ADDRESS, FIELD_PARTY_ID);
                sql.addQualifier(ALIAS_EMAIL_ADDRESS, FIELD_EMAIL_ADDRESS_TYPE_CODE, makeSafeString(String.valueOf(email.getEmailAddressType())));

                if(!Util.isEmpty(email.getEmailAddress()))
                {
                    sql.addQualifier("UPPER(" + ALIAS_EMAIL_ADDRESS + "." + FIELD_EMAIL_ADDRESS + ") LIKE UPPER('" + email.getEmailAddress() + "%')");
                }
        }
        // End GD-343: Modify customer search/add screens for customer capture fields
        
        if (logger.isDebugEnabled())
        {
            logger.debug("GDYNJdbcSelectCustomers.buildQuery()");
            logger.debug("GDYNJdbcSelectCustomers.buildQuery():  " + sql.getSQLString());
        }

        /* Return Sql String : SELECT DISTINCT CU.ID_CT, CU.ID_PRTY, CU.ID_EM, CU.STS_CT, CU.ID_NCRPT_ACTN_CRD,
         * CU.ID_MSK_ACNT_CRD FROM PA_PHN PH, PA_CNCT CNT, LO_ADS AD, PA_CT CU
         * WHERE CU.ID_PRTY = CNT.ID_PRTY AND CU.ID_PRTY = AD.ID_PRTY AND CU.STS_CT <> 2 AND
         * UPPER(CNT.FN_CNCT) LIKE UPPER('FirstName%') AND UPPER(CNT.LN_CNCT) LIKE UPPER('LastName%') AND
         * UPPER(A1_CNCT) LIKE UPPER('AddressLine1%') AND UPPER(PC_CNCT) LIKE UPPER('PostalCode%') */

        return sql.getSQLString();
    }
}
