/* ===========================================================================
* Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/modules/domain/src/oracle/retail/stores/domain/arts/JdbcSaveRole.java /rgbustores_13.4x_generic_branch/1 2011/05/04 11:49:05 mszekely Exp $
 * ===========================================================================
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED    (MM/DD/YY)
 *    npoola    08/25/10 - passed the connection object to the
 *                         IdentifierService getNextID method to use right
 *                         connection
 *    acadar    08/23/10 - changes for roles
 *    cgreene   05/28/10 - convert to oracle packaging
 *    cgreene   05/27/10 - convert to oracle packaging
 *    cgreene   05/27/10 - convert to oracle packaging
 *    cgreene   05/26/10 - convert to oracle packaging
 *    cgreene   04/26/10 - XbranchMerge cgreene_tech75 from
 *                         st_rgbustores_techissueseatel_generic_branch
 *    cgreene   03/30/10 - remove deprecated ARTSDatabaseIfcs and change
 *                         SQLException to DataException
 *    abondala  01/03/10 - update header date
 *    blarsen   11/04/08 - adding code to insert and update localized role
 *                         descriptions
 *
 * ===========================================================================
 * $Log:
 *    7    360Commerce 1.6         6/1/2006 12:49:56 PM   Charles D. Baker
 *         Remove unused imports
 *    6    360Commerce 1.5         6/1/2006 12:28:42 PM   Brendan W. Farrell
 *         Update comments.
 *    5    360Commerce 1.4         5/31/2006 5:04:01 PM   Brendan W. Farrell
 *         Move from party to id gen.
 *
 *    4    360Commerce 1.3         1/25/2006 4:11:23 PM   Brett J. Larsen merge
 *          7.1.1 changes (aka. 7.0.3 fixes) into 360Commerce view
 *    3    360Commerce 1.2         3/31/2005 4:28:44 PM   Robert Pearse
 *    2    360Commerce 1.1         3/10/2005 10:22:49 AM  Robert Pearse
 *    1    360Commerce 1.0         2/11/2005 12:12:03 PM  Robert Pearse
 *:
 *    4    .v700     1.2.1.0     11/16/2005 16:25:39    Jason L. DeLeau 4215:
 *         Get rid of redundant ArtsDatabaseifc class
 *    3    360Commerce1.2         3/31/2005 15:28:44     Robert Pearse
 *    2    360Commerce1.1         3/10/2005 10:22:49     Robert Pearse
 *    1    360Commerce1.0         2/11/2005 12:12:03     Robert Pearse
 *
 *   Revision 1.6.2.1  2004/10/15 18:50:24  kmcbride
 *   Merging in trunk changes that occurred during branching activity
 *
 *   Revision 1.7  2004/10/11 22:00:49  jdeleau
 *   @scr 7306 Fix roles not appearing after they are created
 *
 *   Revision 1.6  2004/04/09 16:55:47  cdb
 *   @scr 4302 Removed double semicolon warnings.
 *
 *   Revision 1.5  2004/02/17 17:57:37  bwf
 *   @scr 0 Organize imports.
 *
 *   Revision 1.4  2004/02/17 16:18:47  rhafernik
 *   @scr 0 log4j conversion
 *
 *   Revision 1.3  2004/02/12 17:13:18  mcs
 *   Forcing head revision
 *
 *   Revision 1.2  2004/02/11 23:25:25  bwf
 *   @scr 0 Organize imports.
 *
 *   Revision 1.1.1.1  2004/02/11 01:04:28  cschellenger
 *   updating to pvcs 360store-current
 *
 *
 *
 *    Rev 1.0   Aug 29 2003 15:32:56   CSchellenger
 * Initial revision.
 *
 *    Rev 1.1   Apr 22 2003 12:32:28   adc
 * Changes to accomodate new Back Office requirements
 * Resolution for 1935: Roles/Security updates
 *
 *    Rev 1.0   Jun 03 2002 16:40:06   msg
 * Initial revision.
 *
 *    Rev 1.2   16 May 2002 18:50:20   adc
 * db2 fixes
 * Resolution for Domain SCR-50: db2 port fixes
 *
 *    Rev 1.1   Mar 18 2002 22:48:42   msg
 * - updated copyright
 *
 *    Rev 1.0   Mar 18 2002 12:08:30   msg
 * Initial revision.
 *
 *    Rev 1.0   Sep 20 2001 15:57:02   msg
 * Initial revision.
 *
 *    Rev 1.1   Sep 17 2001 12:34:00   msg
 * header update
 * ===========================================================================
 */
package oracle.retail.stores.domain.arts;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import oracle.retail.stores.common.identifier.IdentifierConstantsIfc;
import oracle.retail.stores.common.identifier.IdentifierServiceIfc;
import oracle.retail.stores.common.identifier.IdentifierServiceLocator;
import oracle.retail.stores.common.sql.SQLInsertStatement;
import oracle.retail.stores.common.sql.SQLUpdateStatement;
import oracle.retail.stores.domain.employee.RoleFunctionIfc;
import oracle.retail.stores.domain.employee.RoleIfc;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.persistence.utility.ARTSDatabaseIfc;

import org.apache.log4j.Logger;

/**
 * This operation saves the employee role table.
 *
 * @version $Revision: /rgbustores_13.4x_generic_branch/1 $
 */
public abstract class JdbcSaveRole extends JdbcDataOperation implements ARTSDatabaseIfc
{
    private static final long serialVersionUID = -4464286991803002069L;

    /**
     * The logger to which log messages will be sent.
     */
    private static final Logger logger = Logger.getLogger(JdbcSaveRole.class);

    /**
     * revision number supplied by Team Connection
     */
    public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/1 $";

    /**
     * Class constructor.
     */
    public JdbcSaveRole()
    {
    }

    /**
     * Executes the update statement against the db.
     *
     * @param connection a JdbcDataConnection object
     * @param role the object to put in the DB.
     * @exception DataException upon error
     */
    protected Integer updateRole(JdbcDataConnection connection, RoleIfc role)
        throws DataException
    {
        // Put away the role record
        // Define the table
        SQLUpdateStatement sql = new SQLUpdateStatement();
        sql.setTable(TABLE_ROLE);

        // Add columns and their values and qualifiers
        sql.addColumn(FIELD_ROLE_DESCRIPTION, getString(role.getTitle()));
        sql.addQualifier(FIELD_ROLE_ID + " = " + role.getRoleID());

        logger.debug("JdbcSaveRoles" + sql.getSQLString());
        // Execute the SQL statement
        connection.execute(sql.getSQLString());

        // Begin GD-323:  Referential Integrity error when selecting Done in Roles or unknown exception
        // mlawrence (Starmount) May 13, 2013
//        if (connection.getUpdateCount() < 1)
//        {
//            logger.error( "Role update description count was not greater than 0");
//            throw new DataException(DataException.UNKNOWN, "Role update description count was not greater than 0");
//        }
        // End GD-323: Referential Integrity error when selecting Done in Roles or unknown exception

        // Put away the associated Role function objects
        RoleFunctionIfc[] funcArray = role.getFunctions();
        for (int i = 0; i < funcArray.length; i++)
        {
            RoleFunctionIfc func = funcArray[i];
            // Define the table
            sql = new SQLUpdateStatement();
            sql.setTable(TABLE_ROLE_ACCESS);

            // Add columns and their values and qualifiers
            sql.addColumn(FIELD_GROUP_WRITE_ACCESS_LEVEL_FLAG,
                          makeSafeString(getBoolean(func.getAccess())));
            sql.addColumn(FIELD_ID_APPLICATION, role.getApplicationId());
            sql.addQualifier(FIELD_ROLE_ID + " = " + role.getRoleID());
            sql.addQualifier(FIELD_GROUP_RESOURCE_ID + " = " + func.getFunctionID());

            String sqlStr = null;
            sqlStr = sql.getSQLString();
            logger.debug("JdbcSaveRoles" + sql.getSQLString());

            connection.execute(sqlStr);

            // Begin GD-323:  Referential Integrity error when selecting Done in Roles or unknown exception
            // mlawrence (Starmount) May 13, 2013
//            if (connection.getUpdateCount() < 1)
//            {
//                logger.error( "Role update description count was not greater than 0");
//                throw new DataException(DataException.UNKNOWN, "Role update description count was not greater than 0");
//            }
            // End GD-323: Referential Integrity error when selecting Done in Roles or unknown exception
        }

        updateRoleDescriptions(connection, role);

        return Integer.valueOf(0);

    }

    /**
     * Updates the role's localized descriptions. Assumption: all localized
     * descriptions sent from the UI exist in the database
     *
     * @param connection a JdbcDataConnection object
     * @param role the object to put in the DB.
     * @exception DataException upon error
     */
    protected Integer updateRoleDescriptions(JdbcDataConnection connection, RoleIfc role)
        throws DataException
    {

        Set<Locale> locales = role.getLocalizedTitles().getLocales();
        Iterator<Locale> i = locales.iterator();
        while (i.hasNext())
        {
            Locale locale = i.next();
            String localizedDescription = role.getLocalizedTitles().getText(locale);
            if (localizedDescription != null && !localizedDescription.equals(""))
            {

                SQLUpdateStatement sql = new SQLUpdateStatement();

                sql.setTable(TABLE_ROLE_I8);

                sql.addColumn(FIELD_ROLE_DESCRIPTION, getString(localizedDescription));

                sql.addQualifier(FIELD_LOCALE + " = " + makeSafeString(locale.toString()));
                sql.addQualifier(FIELD_ROLE_ID + " = " + role.getRoleID());

                String sqlString = sql.getSQLString();
                logger.debug("JdbcSaveRoles" + sql.getSQLString());

                connection.execute(sqlString);

                // Begin GD-323:  Referential Integrity error when selecting Done in Roles or unknown exception
                // mlawrence (Starmount) May 13, 2013
//                if (connection.getUpdateCount() < 1)
//                {
//                    logger.error( "Role update description count was not greater than 0");
//                    throw new DataException(DataException.UNKNOWN, "Role update description count was not greater than 0");
//                }
                // End GD-323: Referential Integrity error when selecting Done in Roles or unknown exception
            }

        }

        return Integer.valueOf(0);
    }

    /**
     * Executes the insert statement against the db.
     *
     * @param connection a Jdbcconnection object
     * @param role the object to put in the DB.
     * @exception DataException upon error
     */
    protected Integer insertRole(JdbcDataConnection connection, RoleIfc role)
        throws DataException
    {
        // Get a new role id from the Database
        int roleID = generateRoleID(connection);
        role.setRoleID(roleID);

        // Put away the role record
        // Define the table
        SQLInsertStatement sql = new SQLInsertStatement();
        sql.setTable(TABLE_ROLE);

        // Add columns and their values and qualifiers
        sql.addColumn(FIELD_ROLE_ID, Integer.toString(roleID));
        // role.getTitle() is not localized, to get the localized value use getLocalizedTitle(Locale)
        sql.addColumn(FIELD_ROLE_DESCRIPTION, makeSafeString(role.getTitle()));

        logger.debug("JdbcSaveRoles" + sql.getSQLString());

        // Execute the SQL statement
        connection.execute(sql.getSQLString());


        // Begin GD-323:  Referential Integrity error when selecting Done in Roles or unknown exception
        // mlawrence (Starmount) May 13, 2013
//        if (connection.getUpdateCount() < 1)
//        {
//            logger.error( "Role update description count was not greater than 0");
//            throw new DataException(DataException.UNKNOWN, "Role update description count was not greater than 0");
//        }
        // End GD-323: Referential Integrity error when selecting Done in Roles or unknown exception

        // Put away the associated Role function objects
        RoleFunctionIfc[] funcArray = role.getFunctions();
        for (int i = 0; i < funcArray.length; i++)
        {
            RoleFunctionIfc func = funcArray[i];
            // Define the table
            sql = new SQLInsertStatement();
            sql.setTable(TABLE_ROLE_ACCESS);

            // Add columns and their values and qualifiers
            sql.addColumn(FIELD_GROUP_RESOURCE_ID, Integer.toString(func.getFunctionID()));
            sql.addColumn(FIELD_ROLE_ID, Integer.toString(roleID));
            // funct.getTitle() - returns the description that is a key in the corresponding I18N table
            sql.addColumn(FIELD_GROUP_RESOURCE_DESCRIPTION, makeSafeString(func.getTitle()));
            sql.addColumn(FIELD_GROUP_WRITE_ACCESS_LEVEL_FLAG,
                          makeSafeString(getBoolean(func.getAccess())));
            // resource id
            sql.addColumn(FIELD_PARENT_RESOURCE_ID, func.getFunctionGroup().getGroupID());
            // visibility level
            sql.addColumn(FIELD_GROUP_VISIBILITY_CODE, func.getVisibilityLevel());
            sql.addColumn(FIELD_ID_APPLICATION, role.getApplicationId());
            
            logger.debug("JdbcSaveRoles" + sql.getSQLString());

            connection.execute(sql.getSQLString());

            // Begin GD-323:  Referential Integrity error when selecting Done in Roles or unknown exception
            // mlawrence (Starmount) May 13, 2013
//            if (connection.getUpdateCount() < 1)
//            {
//                logger.error( "Role update description count was not greater than 0");
//                throw new DataException(DataException.UNKNOWN, "Role update description count was not greater than 0");
//            }
            // End GD-323: Referential Integrity error when selecting Done in Roles or unknown exception
        }

        insertRoleDescriptions(connection, role);

        return Integer.valueOf(0);

    }

    /**
     * Inserts the role's localized descriptions
     *
     * @param connection a Jdbcconnection object
     * @param role the object to put in the DB.
     * @exception DataException upon error
     */
    protected Integer insertRoleDescriptions(JdbcDataConnection connection, RoleIfc role)
        throws DataException
    {

        Set<Locale> locales = role.getLocalizedTitles().getLocales();
        Iterator<Locale> i = locales.iterator();
        while (i.hasNext())
        {
            Locale locale = i.next();

            SQLInsertStatement sql = new SQLInsertStatement();

            sql.setTable(TABLE_ROLE_I8);

            sql.addColumn(FIELD_ROLE_ID, Integer.toString(role.getRoleID()));
            sql.addColumn(FIELD_LOCALE, makeSafeString(locale.toString()));
            sql.addColumn(FIELD_ROLE_DESCRIPTION, makeSafeString(role.getLocalizedTitles().getText(locale)));

            logger.debug("JdbcSaveRoles" + sql.getSQLString());

            connection.execute(sql.getSQLString());

            // Begin GD-323:  Referential Integrity error when selecting Done in Roles or unknown exception
            // mlawrence (Starmount) May 13, 2013
//            if (connection.getUpdateCount() < 1)
//            {
//                logger.error( "Role update description count was not greater than 0");
//                throw new DataException(DataException.UNKNOWN, "Role update description count was not greater than 0");
//            }
            // End GD-323: Referential Integrity error when selecting Done in Roles or unknown exception

        }

        return Integer.valueOf(0);
    }


    /**
       Generates a quoted string
       <P>
       @param  String  string to quote
       @return String  quoted string
     */
    protected String getString(String string)
    {
        return ("'" + string + "'");
    }

    /**
       Generates a 0 or 1 depending on boolean value
       <P>
       @param  boolean value
       @return String  "1" if true
     */
    protected String getBoolean(boolean b)
    {
        String ret = "0";
        if (b)
        {
            ret = "1";
        }

        return ret;
    }

    /**
       Generates a unique role id.  Uses {@link IdentifierServiceIfc}

       @param  connection      The connection to the data source
       @return the party_id
       @exception DataException upon error
     */
    protected int generateRoleID(JdbcDataConnection connection)
        throws DataException
    {
        return IdentifierServiceLocator.getIdentifierService().getNextID(connection.getConnection(), IdentifierConstantsIfc.COUNTER_WORK_GROUP);
    }

    /**
       Returns a string representation of this object.
       <P>
       @param none
       @return String representation of object
     */
    @Override
    public String toString()
    {
        // result string
        String strResult = new String("Class:  JdbcSaveRole (Revision " +
                                      getRevisionNumber() +
                                      ")" + hashCode());

        // pass back result
        return(strResult);
    }

    /**
       Returns the revision number of the class.
       <P>
       @param none
       @return String representation of revision number
     */
    public String getRevisionNumber()
    {
        // return string
        return(revisionNumber);
    }

}
