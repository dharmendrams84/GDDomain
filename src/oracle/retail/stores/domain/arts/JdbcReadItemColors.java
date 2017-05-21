/* ===========================================================================
* Copyright (c) 2008, 2011, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/modules/domain/src/oracle/retail/stores/domain/arts/JdbcReadItemColors.java /rgbustores_13.4x_generic_branch/1 2011/05/04 11:49:02 mszekely Exp $
 * ===========================================================================
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED    (MM/DD/YY)
 *    cgreene   05/28/10 - convert to oracle packaging
 *    cgreene   05/27/10 - convert to oracle packaging
 *    cgreene   05/27/10 - convert to oracle packaging
 *    cgreene   05/26/10 - convert to oracle packaging
 *    cgreene   04/26/10 - XbranchMerge cgreene_tech75 from
 *                         st_rgbustores_techissueseatel_generic_branch
 *    cgreene   03/30/10 - remove deprecated ARTSDatabaseIfcs and change
 *                         SQLException to DataException
 *    abondala  01/03/10 - update header date
 *    mchellap  10/16/08 - Advance Item Inquiry
 *    mchellap  10/16/08 - Advanced Item Inquiry
 *
 *
 * ===========================================================================
 */

package oracle.retail.stores.domain.arts;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

import oracle.retail.stores.common.sql.SQLSelectStatement;
import oracle.retail.stores.common.utility.LocaleMap;
import oracle.retail.stores.common.utility.LocaleRequestor;
import oracle.retail.stores.common.utility.LocaleUtilities;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.stock.ItemColorIfc;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataConnectionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataTransactionIfc;
import oracle.retail.stores.persistence.utility.ARTSDatabaseIfc;

/**
 * Class that contains the database calls for reading item colors.
 */
public class JdbcReadItemColors extends JdbcDataOperation implements ARTSDatabaseIfc
{
    private static final long serialVersionUID = 6643133355449708746L;

    /**
     * Executes the SQL statements against the database.
     * 
     * @param dataTransaction The data transaction
     * @param dataConnection The connection to the data source
     * @param action The information passed by the valet
     * @exception DataException upon error
     */
    public void execute(DataTransactionIfc dataTransaction,
                        DataConnectionIfc dataConnection,
                        DataActionIfc action)
                        throws DataException
    {
        if (logger.isDebugEnabled()) logger.debug("Entering JdbcReadColorList.execute()");

        JdbcDataConnection connection = (JdbcDataConnection)dataConnection;
        Object dataObject = action.getDataObject();

        if (dataObject != null && dataObject instanceof LocaleRequestor)
        {
            LocaleRequestor localeRequestor = (LocaleRequestor)dataObject;
            dataTransaction.setResult(selectItemColorList(connection, localeRequestor));
        }
        else
        {
            logger.error("JdbcReadItem.execute: Invalid search object");
            throw new DataException("Invalid search object");
        }

        if (logger.isDebugEnabled()) logger.debug( "Exiting JdbcReadColorList.execute()");
    }

    /**
        Returns a list of item types.
        @param  dataConnection      connection to the db
        @return ItemColorIfc[]     the list of item types
        @exception DataException upon error
     */
    public ItemColorIfc[] selectItemColorList(JdbcDataConnection dataConnection, LocaleRequestor localeRequestor)
                                                throws DataException
    {

        SQLSelectStatement sql = new SQLSelectStatement();

        // add tables
        sql.addTable(TABLE_COLOR);
        // add columns
        sql.addColumn(FIELD_COLOR_CODE);
        LinkedList<ItemColorIfc> itemColorList = new LinkedList<ItemColorIfc>();


        try
        {
            dataConnection.execute(sql.getSQLString());
            ResultSet rs = (ResultSet)dataConnection.getResult();

            while (rs.next())
            {
                int index = 0;
                ItemColorIfc itemColor = DomainGateway.getFactory().getItemColorInstance();
                itemColor.setIdentifier(getSafeString(rs,++index));
                itemColorList.add(itemColor);
            }
            rs.close();
        }
        catch (SQLException se)
        {
            dataConnection.logSQLException(se, "selectItemColorList");
            throw new DataException(DataException.SQL_ERROR, "selectItemColorList", se);
        }
        catch (DataException de)
        {
            // not found is regarded to be Ok here
            if (de.getErrorCode() != DataException.NO_DATA)
            {
                throw de;
            }
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "selectItemColorList", e);
        }

        for (Iterator<ItemColorIfc> i = itemColorList.iterator(); i.hasNext();)
        {
            readI8ItemColor(dataConnection,
                    i.next(),
                    localeRequestor);
        }
        // return result
        ItemColorIfc[] result = new ItemColorIfc[itemColorList.size()];
        result = (ItemColorIfc[])itemColorList.toArray(result);
        return result;
    }

    /**
     * read the item which contains the table description
     * @param connection
     * @param itemColor
     * @param localeRequestor
     * @throws DataException
     */
    // Begin GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item description
    // lcatania (Starmount) Mar 22, 2013
    // Changed method visibility from protected to public
    public void readI8ItemColor(JdbcDataConnection connection,
                                       ItemColorIfc itemColor,
                                       LocaleRequestor localeRequestor) throws DataException
    // End GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item description
    {
        SQLSelectStatement sql = new SQLSelectStatement();

        // Table to select from
        sql.addTable(TABLE_COLOR_I8);

        // add column
        sql.addColumn(FIELD_LOCALE);
        sql.addColumn(FIELD_COLOR_NAME);
        sql.addColumn(FIELD_COLOR_DESCRIPTION);

        // add identifier qualifier
        sql.addQualifier(FIELD_COLOR_CODE, inQuotes(itemColor.getIdentifier()));

        //  add qualifier for locale
        sql.addQualifier(FIELD_LOCALE + " " + buildINClauseString(LocaleMap.getBestMatch("", localeRequestor.getLocales())));

        try
        {
            // execute sql
            String sqlString = sql.getSQLString();
            connection.execute(sqlString);
            ResultSet rs = (ResultSet)connection.getResult();

            Locale locale = null;
            // parse result set
            while (rs.next())
            {
                locale = LocaleUtilities.getLocaleFromString(getSafeString(rs, 1));
                itemColor.setName(locale, getSafeString(rs, 2));
                itemColor.setDescription(locale, getSafeString(rs, 3));

            }
            rs.close();
        }
        catch (SQLException se)
        {
            connection.logSQLException(se, "readItemColor");
            throw new DataException(DataException.SQL_ERROR, "readItemColor", se);
        }
        catch (DataException de)
        {
            // not found is regarded to be Ok here
            if (de.getErrorCode() != DataException.NO_DATA)
            {
                throw de;
            }
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "readItemColor", e);
        }

    }

}
