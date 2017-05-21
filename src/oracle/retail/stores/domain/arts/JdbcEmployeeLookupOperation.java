/* ===========================================================================
* Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/modules/domain/src/oracle/retail/stores/domain/arts/JdbcEmployeeLookupOperation.java /rgbustores_13.4x_generic_branch/4 2011/09/22 17:33:03 tksharma Exp $
 * ===========================================================================
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED    (MM/DD/YY)
 *    masahu    07/07/11 - FORTIFY FIX: The sensitive SQLs get logged
 *    abondala  04/11/11 - XbranchMerge abondala_bug11827952-salting_passwords
 *                         from main
 *    abondala  03/25/11 - merging
 *    abondala  03/23/11 - Implemented salting for the passwords
 *    hyin      02/28/11 - convert to blob
 *    hyin      02/18/11 - add last login column
 *    hyin      01/28/11 - change fingerprint column to varchar to accommodate
 *                         with derby limitation
 *    blarsen   01/18/11 - Integrated changes to BlobFactory.
 *    blarsen   05/26/10 - Added support for new fingerprint biometrics column.
 *    blarsen   06/09/10 - XbranchMerge blarsen_biometrics-poc from
 *                         st_rgbustores_techissueseatel_generic_branch
 *    cgreene   05/28/10 - convert to oracle packaging
 *    cgreene   05/27/10 - convert to oracle packaging
 *    cgreene   05/27/10 - convert to oracle packaging
 *    cgreene   05/26/10 - convert to oracle packaging
 *    cgreene   04/28/10 - updating deprecated names
 *    cgreene   04/26/10 - XbranchMerge cgreene_tech75 from
 *                         st_rgbustores_techissueseatel_generic_branch
 *    cgreene   03/30/10 - remove deprecated ARTSDatabaseIfcs and change
 *                         SQLException to DataException
 *    abondala  01/03/10 - update header date
 *    abondala  01/29/09 - updated files related to hashing algorithm which can
 *                         be configured through properties file.
 *    glwang    12/01/08 - deprecated employee full name column
 *    blarsen   11/05/08 - deprecated getRole - it moved to JdbcReadRoles
 *
 * ===========================================================================
 * $Log:
 *  8    360Commerce 1.7         10/12/2006 8:17:52 AM  Christian Greene Adding
 *        new functionality for PasswordPolicy.  Employee password will now be
 *        persisted as a byte[] in hexadecimal.  Updates include UI changes,
 *       persistence changes, and AppServer configuration changes.  A database
 *        rebuild with the new SQL scripts will be required.
 *  7    360Commerce 1.6         9/29/2006 12:17:44 PM  Rohit Sachdeva  21237:
 *       Password Policy Service Persistence Updates
 *  6    360Commerce 1.5         7/25/2006 7:06:11 PM   Robert Zurga    .v7x
 *          1.3.1.0     6/22/2006 3:53:14 PM   Michael Wisbauer Added
 *       *       setter and check flag for retrieving all employees.  This is
 *       need for
 *       *       some process such as returns. need to retreive expired and
 *       inactive
 *       *       employees that are part of transaction data
 *
 *  5    360Commerce 1.4         7/25/2006 6:49:19 PM   Robert Zurga    Merge
 *       from JdbcEmployeeLookupOperation.java, Revision 1.3.1.0
 *  4    360Commerce 1.3         1/25/2006 4:11:07 PM   Brett J. Larsen merge
 *       7.1.1 changes (aka. 7.0.3 fixes) into 360Commerce view
 *  3    360Commerce 1.2         3/31/2005 4:28:36 PM   Robert Pearse
 *  2    360Commerce 1.1         3/10/2005 10:22:37 AM  Robert Pearse
 *  1    360Commerce 1.0         2/11/2005 12:11:53 PM  Robert Pearse
 * $:
 *  4    .v700     1.2.1.0     11/16/2005 16:27:29    Jason L. DeLeau 4215: Get
 *       rid of redundant ArtsDatabaseifc class
 *  3    360Commerce1.2         3/31/2005 15:28:36     Robert Pearse
 *  2    360Commerce1.1         3/10/2005 10:22:37     Robert Pearse
 *  1    360Commerce1.0         2/11/2005 12:11:53     Robert Pearse
 * $
 * Revision 1.11  2004/09/23 00:30:50  kmcbride
 * @scr 7211: Inserting serialVersionUIDs in these Serializable classes
 *
 * Revision 1.10  2004/08/18 01:53:33  kll
 * @scr 6826: extract roles specific to POS
 *
 * Revision 1.9  2004/07/28 16:14:27  kll
 * @scr 6566: protect field_employee_status_code in quotes
 *
 * Revision 1.8  2004/07/24 17:56:27  jriggins
 * @scr 6435 Added expired employee filtering
 *
 * Revision 1.7  2004/07/24 16:38:31  jriggins
 * @scr 6435 Added a filter for only active employees
 *
 * Revision 1.6  2004/02/19 23:36:46  jriggins
 * @scr 3782 this commit mainly deals with the database modifications needed for Enter New Password feature in Operator ID
 * Revision 1.5 2004/02/17 17:57:37
 * bwf @scr 0 Organize imports.
 *
 * Revision 1.4 2004/02/17 16:18:46 rhafernik @scr 0 log4j conversion
 *
 * Revision 1.3 2004/02/12 17:13:13 mcs Forcing head revision
 *
 * Revision 1.2 2004/02/11 23:25:23 bwf @scr 0 Organize imports.
 *
 * Revision 1.1.1.1 2004/02/11 01:04:26 cschellenger updating to pvcs
 * 360store-current
 *
 *
 *
 * Rev 1.0 Aug 29 2003 15:30:32 CSchellenger Initial revision.
 *
 * Rev 1.9 Jul 08 2003 15:39:34 epd Fixed problem with single quotes. Updated
 * to remove use of deprecated class Resolution for 2103: Enter an apostrophe
 * in the Employee ID field, database error is presented.
 *
 * Rev 1.8 Jun 03 2003 17:00:14 bwf Added table to getRole. Resolution for
 * 2686: Employee Search by name results in a database error.
 *
 * Rev 1.7 Apr 22 2003 12:32:28 adc Changes to accomodate new Back Office
 * requirements Resolution for 1935: Roles/Security updates
 *
 * Rev 1.6 Mar 21 2003 16:06:00 baa fix bug with flat files Resolution for POS
 * SCR-1843: Multilanguage support
 *
 * Rev 1.5 Mar 07 2003 17:04:54 baa code review changes for I18n Resolution for
 * POS SCR-1740: Code base Conversions
 *
 * Rev 1.4 Feb 26 2003 11:29:36 bwf Database Internationalization Resolution
 * for 1866: I18n Database support
 *
 * Rev 1.3 Jan 30 2003 16:31:40 baa add employee locale preferences to flat
 * files Resolution for POS SCR-1843: Multilanguage support
 *
 * Rev 1.2 Dec 18 2002 16:58:46 baa Add employee/customer language preferrence
 * support Resolution for POS SCR-1843: Multilanguage support
 *
 * Rev 1.1 Nov 27 2002 11:58:02 baa support for employee/customer locale
 * preference Resolution for POS SCR-1843: Multilanguage support
 *
 * Rev 1.0 Jun 03 2002 16:36:14 msg Initial revision.
 *
 * Rev 1.1 Mar 18 2002 22:45:30 msg - updated copyright
 *
 * Rev 1.0 Mar 18 2002 12:05:26 msg Initial revision.
 *
 * Rev 1.2 02 Nov 2001 17:09:06 dlr Implementing employee status functionality
 * 1 is active 2 is inactive Resolution for Domain SCR-12: Implement employee
 * status functionality
 *
 * Rev 1.1 27 Oct 2001 08:33:38 mpm Merged employee changes from Virginia ABC
 * demonstration.
 *
 * Rev 1.0 Sep 20 2001 15:59:08 msg Initial revision.
 *
 * ===========================================================================
 */
package oracle.retail.stores.domain.arts;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import oracle.retail.stores.common.data.JdbcUtilities;
import oracle.retail.stores.common.sql.SQLSelectStatement;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.employee.EmployeeIfc;
import oracle.retail.stores.domain.employee.EmployeeTypeEnum;
import oracle.retail.stores.domain.employee.RoleIfc;
import oracle.retail.stores.domain.utility.EYSDate;
import oracle.retail.stores.domain.utility.LocaleUtilities;
import oracle.retail.stores.domain.utility.PersonNameIfc;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataConnectionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataOperationIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataTransactionIfc;
import oracle.retail.stores.foundation.utility.Util;
import oracle.retail.stores.persistence.utility.ARTSDatabaseIfc;
import oracle.retail.stores.persistence.utility.DatabaseBlobHelperFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * The JdbcEmployeeLookupOperation implements the employee lookup JDBC data
 * store operation.
 *
 * @version $Revision: /rgbustores_13.4x_generic_branch/4 $
 */
public class JdbcEmployeeLookupOperation
    extends JdbcDataOperation
    implements DataOperationIfc, ARTSDatabaseIfc
{
    // This id is used to tell the compiler not to generate a new serialVersionUID.
    static final long serialVersionUID = -6210806128663386042L;
    /** revision number of this class */
    public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/4 $";
    /** The logger to which log messages will be sent. */
    private static final Logger logger = Logger.getLogger(JdbcEmployeeLookupOperation.class);

    private boolean retreiveAllEmployees = false;

    /**
     * Class constructor.
     */
    public JdbcEmployeeLookupOperation()
    {
        super();
        setName("JdbcEmployeeLookupOperation");
    }

    /**
     * Set all data members should be set to their initial state.
     *
     * @exception DataException
     */
    public void initialize() throws DataException
    {
        // no action taken here
    }

    /**
     * This method is used to execute a specific operation for a specific
     * transaction against a specific datastore. <B>Pre-Condition</B>
     * <UL>
     * <LI>The DataTransactionIfc contains any application-specific data
     * elements.
     * <LI>The DataConnectionIfc is valid.
     * <LI>The DataActionIfc contains the necessary DataObjects.
     * </UL>
     * <B>Post-Condition</B>
     * <UL>
     * <LI>The appropriate data operations have been executed by the
     * DataConnection using the input data provided by the DataTransactionIfc
     * and the DataActionIfc.
     * <LI>Any results have been posted to the DataTransactionIfc.
     * </UL>
     *
     * @param dt
     *          The DataTransactionIfc that provides a place to post results.
     * @param dc
     *          The DataConnection that provides a connection to the datastore.
     * @param da
     *          The DataActionIfc that provides specific input data for this
     *          operation.
     * @exception DataException
     *              is thrown if the operation cannot be completed.
     */
    public void execute(
        DataTransactionIfc transaction,
        DataConnectionIfc connection,
        DataActionIfc action)
        throws DataException
    {
        EmployeeIfc employee = null;
        EmployeeTransaction eTransaction = (EmployeeTransaction) transaction;

        switch (eTransaction.getQueryType())
        {
            case QueryTypeIfc.LOGINID :
                String employeeLoginId = (String) action.getDataObject();
                employee = selectEmployeeByLoginID(connection, employeeLoginId);
                break;

            case QueryTypeIfc.NAME :
                PersonNameIfc name = (PersonNameIfc) action.getDataObject();
                employee = selectEmployeeByName(connection, name);
                break;

            case QueryTypeIfc.NUMBER :
                String employeeNumber = (String) action.getDataObject();
                employee = selectEmployeeByNumber(connection, employeeNumber);
                break;

            default :
                employeeLoginId = (String) action.getDataObject();
                employee = selectEmployeeByLoginID(connection, employeeLoginId);
                break;

        }

        transaction.setResult(employee);
    }

    /**
     * Sets up the where clause to find employee by login id
     *
     * @return Employee employee object
     * @param connection
     *          data connection interface object
     * @param loginId
     *          user login ID
     * @exception DataException
     */
    public EmployeeIfc selectEmployeeByLoginID(
        DataConnectionIfc connection,
        String loginID)
        throws DataException
    {
        String whereClause = "";
        //only add this when loginID is not empty
        if (StringUtils.isNotEmpty(loginID))
        {
            whereClause = "ID_LOGIN=" + makeSafeString(loginID);
        }

        return (selectEmployee(connection, whereClause));
    }

    /**
     * Sets up the where clause to find employee by Employee id
     *
     * @return Employee employee object
     * @param connection
     *          data connection interface object
     * @param employeeID
     *          employee ID
     * @exception DataException
     */
    public EmployeeIfc selectEmployeeByNumber(
        DataConnectionIfc connection,
        String employeeID)
        throws DataException
    {
        String whereClause = "ID_EM=" + makeSafeString(employeeID);
        return (selectEmployee(connection, whereClause));
    }

    /**
     * Sets up the where clause to find employee by employee name
     *
     * @return Employee employee object
     * @param connection
     *          data connection interface object
     * @param name
     *          employee name
     * @exception DataException
     */
    public EmployeeIfc selectEmployeeByName(
        DataConnectionIfc connection,
        PersonNameIfc name)
        throws DataException
    {
        if (name == null) {
            throw new IllegalArgumentException("Employee Search is terribly wrong. Employee name is not defined.");
        }
        //String whereClause = "NM_EM=" + makeSafeString(name);
        // depreacated nm_em column HPQC 441
        StringBuilder whereClause = new StringBuilder("FN_EM= ");
        whereClause.append(makeSafeString(name.getFirstName()));
        whereClause.append(" AND MD_EM = ");
        whereClause.append(makeSafeString(name.getMiddleName()));
        whereClause.append(" AND LN_EM = ");
        whereClause.append(makeSafeString(name.getLastName()));

        return (selectEmployee(connection, whereClause.toString()));
    }

    /**
     * Executes the select employee SQL statement based on the where clause
     *
     * @return Employee employee object
     * @param connection
     *          data connection interface object
     * @param whereClause
     *          one of the where clauses created above
     * @exception DataException
     */
    public EmployeeIfc selectEmployee(
        DataConnectionIfc connection,
        String whereClause)
        throws DataException
    {

        SQLSelectStatement sql = new SQLSelectStatement();

        /*
         * Define table
         */
        sql.addTable(TABLE_EMPLOYEE, ALIAS_EMPLOYEE);

        /*
         * Add columns and their values
         */
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_ID);               //1
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_ID_ALT);           //2
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_ACCESS_PASSWORD);  //3
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_ID_LOGIN);         //4
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_LAST_NAME);        //5
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_FIRST_NAME);       //6
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_MIDDLE_NAME);      //7
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_STATUS_CODE);      //8
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_LOCALE);                    //9
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_ROLE);             //10
        sql.addColumn(
            ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_NUMBER_OF_DAYS_VALID);       //11
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_EXPIRATION_DATE);  //12
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_TYPE);             //13
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_STORE_ID);         //14
        sql.addColumn(
            ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_NEW_PASSWORD_REQUIRED);      //15
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_NUMBER_FAILED_PASSWORDS);     //16
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_PASSWORD_CREATED_TIMESTAMP);  //17
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_FINGERPRINT_BIOMETRICS);      //18
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_LAST_LOGIN_TIMESTAMP);        //19
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_PASSWORD_SALT);               //20
        // Begin GD-49: Develop Employee Discount Module
        // lcatania (Starmount) Mar 7, 2013
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_NAME);               //21
        // End GD-49: Develop Employee Discount Module

        if (StringUtils.isNotEmpty(whereClause))
        {
            sql.addQualifier(whereClause);
        }

        if(!retreiveAllEmployees)
        {
            // Only active employees if operator is search for employee.
            sql.addQualifier(ALIAS_EMPLOYEE, FIELD_EMPLOYEE_STATUS_CODE, inQuotes(Integer.toString(EmployeeIfc.LOGIN_STATUS_ACTIVE)));
        }
        // Extract data from the result set.
        EmployeeIfc employee = null;
        PersonNameIfc employeeName = null;

        try
        {
            connection.execute(sql.getSQLString(), false);
            ResultSet rs = (ResultSet) connection.getResult();
            EYSDate systemDate = DomainGateway.getFactory().getEYSDateInstance();
            if (rs.next())
            {
                // Filter out expired employees
                EYSDate expirationDate = getEYSDateFromString(rs, 12);
                boolean expired = true;
                if (expirationDate == null || systemDate.before(expirationDate))
                {
                    expired = false;
                }
                if(!expired || retreiveAllEmployees)
                {

                    employee = DomainGateway.getFactory().getEmployeeInstance();
                    employeeName =
                        DomainGateway.getFactory().getPersonNameInstance();
                    // set data elements in the employee. The numbers are the
                    // column
                    //order in the table.
                    employee.setEmployeeID(getSafeString(rs, 1));
                    employee.setAlternateID(getSafeString(rs, 2));
                    String hex = rs.getString(3);
                    employee.setPasswordBytes(JdbcUtilities.base64decode(hex));
                    employee.setLoginID(getSafeString(rs, 4));
                    employeeName.setLastName(getSafeString(rs, 5));
                    employeeName.setFirstName(getSafeString(rs, 6));
                    employeeName.setMiddleName(getSafeString(rs, 7));
                    employee.setLoginStatus(
                        Integer.parseInt(getSafeString(rs, 8)));

                    String language = getSafeString(rs, 9);

                    // Do these out of order because the call after this block will
                    // cause the ResultSet to be closed
                    employee.setDaysValid(rs.getInt(11));
                    employee.setExpirationDate(expirationDate);
                    employee.setType(
                        EmployeeTypeEnum.getEnumForDBVal(rs.getInt(13)));
                    employee.setStoreID(getSafeString(rs, 14));
                    employee.setPasswordChangeRequired(
                        getBooleanFromString(rs, 15));
                    employee.setNumberFailedPasswords(rs.getInt(16));
                    employee.setPasswordCreationDate(timestampToDate(rs.getTimestamp(17)));
                    employee.setEmployeePasswordSalt(getSafeString(rs, 20));
                    // Begin GD-49: Develop Employee Discount Module
                    // lcatania (Starmount) Mar 7, 2013
                    employeeName.setFullName(getSafeString(rs, 21));
                    // End GD-49: Develop Employee Discount Module

                    byte[] fingerprintBiometrics = DatabaseBlobHelperFactory.getInstance().getDatabaseBlobHelper(
                                ((JdbcDataConnection)connection).getConnection()).loadBlob(rs, FIELD_EMPLOYEE_FINGERPRINT_BIOMETRICS);
                    employee.setFingerprintBiometrics(fingerprintBiometrics);

                    employee.setLastLoginTime(timestampToDate(rs.getTimestamp(FIELD_EMPLOYEE_LAST_LOGIN_TIMESTAMP)));

                    if (!Util.isEmpty(language))
                    {
                        try
                        {
                            employee.setPreferredLocale(
                                LocaleUtilities.getLocaleFromString(language));
                        }
                        catch (IllegalArgumentException e)
                        {
                            logger.warn(
                                "JdbcEmployeeLookupOperation.execute(): Employee preferredLocale is not valid");
                        }
                    }
                    employee.setRole(JdbcReadRoles.getRole(rs.getInt(10), connection));

                    employee.setPersonName(employeeName);
                }
            }
        }
        catch (SQLException e)
        {
            ((JdbcDataConnection) connection).logSQLException(
                e,
                "Processing result set.");
            throw new DataException(
                DataException.SQL_ERROR,
                "An SQL Error occurred proccessing the result set from selecting an employee in JdbcEmployeeLookupOperation.",
                e);
        }
        catch (NumberFormatException e)
        {
            logger.error("Error occurred parding employee information.", e);
            throw new DataException(
                DataException.DATA_FORMAT,
                "Found an unexpected numeric data format in JdbcEmployeeLookupOperation.",
                e);
        }

        if (employee == null)
        {
            throw new DataException(
                DataException.NO_DATA,
                "No employee was found proccessing the result set in JdbcEmployeeLookupOperation.");
        }

        return employee;
    }

    /**
     * Finds the role information associated with the current employee.
     *
     * @return Employee employee object
     * @param roleID
     *          roleid
     * @param connection
     *          data connection interface object
     * @exception DataException
     * @deprecated deprecated as of 13.1 - moved to JdbcReadRoles
     */
    static public RoleIfc getRole(int roleID, DataConnectionIfc dataConnection)
        throws DataException
    {
        RoleIfc[] roles = null;

        SQLSelectStatement sql = new SQLSelectStatement();

        /*
         * Define table
         */
        sql.addTable(TABLE_ROLE, ALIAS_ROLE);
        sql.addTable(TABLE_ROLE_ACCESS, ALIAS_ROLE_ACCESS);
        sql.addTable(TABLE_RESOURCE, ALIAS_RESOURCE);

        /*
         * Add columns and their values
         */
        sql.addColumn(ALIAS_ROLE + "." + FIELD_ROLE_ID); //1
        sql.addColumn(FIELD_ROLE_DESCRIPTION); //2
        sql.addColumn(FIELD_GROUP_RESOURCE_ID); //3
        sql.addColumn(FIELD_GROUP_WRITE_ACCESS_LEVEL_FLAG); //4
        sql.addColumn(FIELD_GROUP_RESOURCE_DESCRIPTION); //5
        sql.addColumn(ALIAS_RESOURCE + "." + FIELD_GROUP_PARENT_NAME); //6
        sql.addColumn(ALIAS_ROLE_ACCESS + "." + FIELD_GROUP_VISIBILITY_CODE);
        //7
        sql.addColumn(ALIAS_ROLE_ACCESS + "." + FIELD_PARENT_RESOURCE_ID); //8
        sql.addColumn(ALIAS_ROLE_ACCESS + "." + FIELD_ID_APPLICATION);

        /*
         * Add Qualifier(s)
         */
        sql.addQualifier(ALIAS_ROLE + "." + FIELD_ROLE_ID + " = " + roleID);

        sql.addQualifier(
            ALIAS_ROLE
                + "."
                + FIELD_ROLE_ID
                + " = "
                + ALIAS_ROLE_ACCESS
                + "."
                + FIELD_ROLE_ID);

        sql.addQualifier(
            ALIAS_RESOURCE
                + "."
                + FIELD_PARENT_RESOURCE_ID
                + " = "
                + ALIAS_ROLE_ACCESS
                + "."
                + FIELD_PARENT_RESOURCE_ID);
//      short-term solution, hardcoded to extract roles specific to POS
        sql.addQualifier(ALIAS_ROLE_ACCESS + "." + FIELD_ID_APPLICATION + " = 2");

        try
        {
            dataConnection.execute(sql.getSQLString());
            ResultSet rs = (ResultSet) dataConnection.getResult();
            roles = JdbcReadRoles.processResults(rs);
            if (roles.length == 0)
            {
                logger.error(
                    "JdbcEmployeeLookupOperation.getRole(): No role row is available for an employee.");
                throw new DataException(
                    DataException.REFERENTIAL_INTEGRITY_ERROR,
                    "No role row is available for an employee.");
            }
            else if (roles.length > 1)
            {
                logger.error(
                    "JdbcEmployeeLookupOperation.getRole(): More than one role row available for an employee.");
                throw new DataException(
                    DataException.REFERENTIAL_INTEGRITY_ERROR,
                    "More than one role row available for an employee.");
            }
        }
        catch (SQLException se)
        {
            logger.error("", se);
            throw new DataException(
                DataException.SQL_ERROR,
                "Read Role SQL error",
                se);
        }

        return roles[0];
    }

    /**
     * Finds the role information associated with the current employee.
     *
     * @return Employee employee object
     * @param roleID
     *          roleid
     * @param connection
     *          data connection interface object
     * @param Locale
     *          sqllocale
     * @exception DataException
     * @deprecated deprecated as of 13.1 - locales now specified using LocaleRequestor
     */
    static public RoleIfc getRole(
        int roleID,
        DataConnectionIfc dataConnection,
        Locale sqlLocale)
        throws DataException
    {
        RoleIfc[] roles = null;
        SQLSelectStatement sql = new SQLSelectStatement();

        /*
         * Define table
         */
        sql.addTable(TABLE_ROLE, ALIAS_ROLE);
        sql.addTable(TABLE_ROLE_ACCESS, ALIAS_ROLE_ACCESS);
        sql.addTable(TABLE_RESOURCE, ALIAS_RESOURCE);

        /*
         * Add columns and their values
         */
        sql.addColumn(ALIAS_ROLE + "." + FIELD_ROLE_ID); //1
        sql.addColumn(FIELD_ROLE_DESCRIPTION); //2
        sql.addColumn(FIELD_GROUP_RESOURCE_ID); //3
        sql.addColumn(FIELD_GROUP_WRITE_ACCESS_LEVEL_FLAG); //4
        sql.addColumn(FIELD_GROUP_RESOURCE_DESCRIPTION); //5
        sql.addColumn(ALIAS_RESOURCE + "." + FIELD_GROUP_PARENT_NAME); //6
        sql.addColumn(ALIAS_ROLE_ACCESS + "." + FIELD_GROUP_VISIBILITY_CODE);
        //7
        sql.addColumn(ALIAS_ROLE_ACCESS + "." + FIELD_PARENT_RESOURCE_ID); //8

        /*
         * Add Qualifier(s)
         */
        sql.addQualifier(ALIAS_ROLE + "." + FIELD_ROLE_ID + " = " + roleID);
        sql.addQualifier(
            ALIAS_ROLE
                + "."
                + FIELD_ROLE_ID
                + " = "
                + ALIAS_ROLE_ACCESS
                + "."
                + FIELD_ROLE_ID);
        sql.addQualifier(
            ALIAS_RESOURCE
                + "."
                + FIELD_PARENT_RESOURCE_ID
                + " = "
                + ALIAS_ROLE_ACCESS
                + "."
                + FIELD_PARENT_RESOURCE_ID);

        try
        {
            dataConnection.execute(sql.getSQLString(), false);
            ResultSet rs = (ResultSet) dataConnection.getResult();
            roles = JdbcReadRoles.processResults(rs, dataConnection, sqlLocale);
            if (roles.length == 0)
            {
                logger.error(
                    "JdbcEmployeeLookupOperation.getRole(): No role row is available for an employee.");
                throw new DataException(
                    DataException.REFERENTIAL_INTEGRITY_ERROR,
                    "No role row is available for an employee.");
            }
            else if (roles.length > 1)
            {
                logger.error(
                    "JdbcEmployeeLookupOperation.getRole(): More than one role row available for an employee.");
                throw new DataException(
                    DataException.REFERENTIAL_INTEGRITY_ERROR,
                    "More than one role row available for an employee.");
            }
        }
        catch (SQLException se)
        {
            logger.error("", se);
            throw new DataException(
                DataException.SQL_ERROR,
                "Read Role SQL error",
                se);
        }
        return roles[0];
    }

    public boolean isRetreiveAllEmployees()
    {
        return retreiveAllEmployees;
    }

    public void setRetreiveAllEmployees(boolean retreiveAllEmployees)
    {
        this.retreiveAllEmployees = retreiveAllEmployees;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        // result string
        String strResult =
            new String(
                "Class:  JdbcEmployeeLookupOperation (Revision "
                    + getRevisionNumber()
                    + ")"
                    + hashCode());

        // pass back result
        return (strResult);
    }

    /**
     * Returns the revision number of the class.
     *
     * @param none
     * @return String representation of revision number
     */
    public String getRevisionNumber()
    {
        // return string
        return (revisionNumber);
    }
}
