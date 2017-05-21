/* ===========================================================================
* Copyright (c) 1998, 2011, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/modules/domain/src/oracle/retail/stores/domain/arts/JdbcReadItemSizes.java /rgbustores_13.4x_generic_branch/1 2011/05/04 11:49:01 mszekely Exp $
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
 *    ranojha   10/17/08 - Changes for code review
 *    ddbaker   10/09/08 - Update based on code review
 *
 *
     $Log:
      3    360Commerce 1.2         3/31/2005 4:28:40 PM   Robert Pearse   
      2    360Commerce 1.1         3/10/2005 10:22:43 AM  Robert Pearse   
      1    360Commerce 1.0         2/11/2005 12:11:59 PM  Robert Pearse   
     $
     Revision 1.8  2004/04/09 16:55:44  cdb
     @scr 4302 Removed double semicolon warnings.

     Revision 1.7  2004/03/26 21:18:22  cdb
     @scr 4204 Removing Tabs.

     Revision 1.6  2004/03/18 18:05:14  lzhao
     @scr 3840 Inquiry Options: Inventory Inquiry
     Code Review Follow Up

     Revision 1.5  2004/03/16 18:27:08  cdb
     @scr 0 Removed tabs from all java source code.

     Revision 1.4  2004/03/15 19:32:55  lzhao
     @scr 3840 Inquiry Options: Inventory Inquiry    
     change search by sizecode.

     Revision 1.3  2004/03/15 16:09:51  lzhao
     @scr 3840 Inquiry Options: Inventory Inquiry.
     Add/Remove comments.

     Revision 1.2  2004/03/12 23:02:57  lzhao
     @scr #3840 Inquiry Operations: Inventory Inquiry
     Add item size feature, get item size code based on table description.

     Revision 1.1  2004/02/18 22:42:14  epd
     @scr 3561 New data transaction reads all available size codes from database

     
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */     
package oracle.retail.stores.domain.arts;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import oracle.retail.stores.persistence.utility.ARTSDatabaseIfc;

import org.apache.log4j.Logger;

import oracle.retail.stores.common.sql.SQLSelectStatement;
import oracle.retail.stores.common.utility.LocaleMap;
import oracle.retail.stores.common.utility.LocaleRequestor;
import oracle.retail.stores.common.utility.LocaleUtilities;
import oracle.retail.stores.domain.utility.LocaleConstantsIfc;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.stock.ItemSizeIfc;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataConnectionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataTransactionIfc;

/**
 * Reads all the possible item sizes from the database
 * 
 * @author epd
 */
public class JdbcReadItemSizes extends JdbcDataOperation implements ARTSDatabaseIfc
{
    private static final long serialVersionUID = -5770123888043463055L;

    /**
     * The logger to which log messages will be sent.
     */
    private static final Logger logger = Logger.getLogger(JdbcReadItemSizes.class);

    /**
     * revision number of this class
     */
    public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/1 $";

    /* (non-Javadoc)
     * @see oracle.retail.stores.foundation.manager.ifc.data.DataOperationIfc#execute(oracle.retail.stores.foundation.manager.ifc.data.DataTransactionIfc, oracle.retail.stores.foundation.manager.ifc.data.DataConnectionIfc, oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc)
     */
    public void execute(DataTransactionIfc dt, 
                        DataConnectionIfc dc, 
                        DataActionIfc da) throws DataException
    {
        if (logger.isDebugEnabled()) logger.debug( "JdbcReadItem.execute");

        JdbcDataConnection connection = (JdbcDataConnection)dc;
        
        Object dataObject = da.getDataObject();
        if (dataObject != null && dataObject instanceof StringSearchCriteria)
        {
            StringSearchCriteria searchCriteria = null;

            // search for specific table size which has the table description
            searchCriteria = (StringSearchCriteria)dataObject;
            dt.setResult(readItemSize(connection, searchCriteria.getIdentifier(), searchCriteria.getLocaleRequestor()));
        }
        else if (dataObject != null && dataObject instanceof LocaleRequestor)
        {
            LocaleRequestor localeRequestor = (LocaleRequestor)dataObject;
            dt.setResult(readItemSizes(connection, localeRequestor));
        }
        else
        {
            logger.error("JdbcReadItem.execute: Invalid search object");
            throw new DataException("Invalid search object");
        }
        if (logger.isDebugEnabled()) logger.debug( "JdbcReadItem.execute");
    }
    
    /**
     * Method to retrieve and read Item Sizes.
     * @param connection
     * @return
     * @throws DataException
     * @deprecated As of release 13.1 Use @link JdbcReadItemSizes#readItemSizes(JdbcDataConnection, LocaleRequestor)
     */
    protected ItemSizeIfc[] readItemSizes(JdbcDataConnection connection)
    throws DataException
    {
    	return readItemSizes(connection, new LocaleRequestor(LocaleMap.getLocale(LocaleConstantsIfc.DEFAULT_LOCALE)));
    }
    
    /**
     * Method to read ItemSizes based on localeRequestor.
     * @param connection
     * @param localeRequestor
     * @return
     * @throws DataException
     */
    protected ItemSizeIfc[] readItemSizes(JdbcDataConnection connection, LocaleRequestor localeRequestor)
    throws DataException
    {
        SQLSelectStatement sql = new SQLSelectStatement();
        // add table
        sql.addTable(TABLE_SIZE);
        // add column
        sql.addColumn(FIELD_SIZE_TABLE_CODE);
        sql.addColumn(FIELD_SIZE_CODE);
        // add qualifier

        List<ItemSizeIfc> sizes = new LinkedList<ItemSizeIfc>();
        try
        {
            // execute sql
            String sqlString = sql.getSQLString();
            connection.execute(sqlString);
            ResultSet rs = (ResultSet)connection.getResult();
            
            // parse result set
            while (rs.next())
            {
                ItemSizeIfc itemSize = DomainGateway.getFactory().getItemSizeInstance();
                itemSize.setTableIdentifier(getSafeString(rs, 1));
                itemSize.setSizeCode(getSafeString(rs, 2));
                sizes.add(itemSize);
            }
            rs.close();
            
        }
        catch (SQLException se)
        {
            connection.logSQLException(se, "readItemSizes");
            throw new DataException(DataException.SQL_ERROR, "readItemSizes", se);
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
            throw new DataException(DataException.UNKNOWN, "readItemSizes", e);
        }
        
        for (Iterator<ItemSizeIfc> i = sizes.iterator(); i.hasNext();)
        {
            readI8ItemSize(connection,
                    i.next(), 
                    localeRequestor);
        }
        // return result
        ItemSizeIfc[] result = new ItemSizeIfc[sizes.size()];
        result = (ItemSizeIfc[])sizes.toArray(result);
        return result;
    }
    
    /**
     * read the item which contains the table description
     * @param connection
     * @param tableDescription
     * @return ItemSizeIfc
     * @throws DataException
     * @deprecated As of release 13.1 Use @link JdbcReadItemSizes#readItemSize(JdbcDataConnection, String, LocaleRequestor)
     */
    protected ItemSizeIfc readItemSize(JdbcDataConnection connection,
                                       String sizeCode) throws DataException
    {
    	return readItemSize(connection, sizeCode, new LocaleRequestor(LocaleMap.getLocale(LocaleConstantsIfc.DEFAULT_LOCALE)));
    }

    /**
     * read the item which contains the table description
     * @param connection
     * @param tableDescription
     * @return
     * @throws DataException
     */
    protected ItemSizeIfc readItemSize(JdbcDataConnection connection,
                                       String sizeCode,
                                       LocaleRequestor localeRequestor) throws DataException
    {
        ItemSizeIfc itemSize = DomainGateway.getFactory().getItemSizeInstance();
        
        SQLSelectStatement sql = new SQLSelectStatement();
        // add table
        sql.addTable(TABLE_SIZE);
        
        // add column
        sql.addColumn(FIELD_SIZE_TABLE_CODE);
        sql.addColumn(FIELD_SIZE_CODE);
        
        // add qualifier
        sql.addQualifier(FIELD_SIZE_CODE+ " = '" + sizeCode + "'");
        
        try
        {
            // execute sql
            String sqlString = sql.getSQLString();
            connection.execute(sqlString);
            ResultSet rs = (ResultSet)connection.getResult();
            
            // parse result set
            if (rs.next())
            {
                itemSize.setTableIdentifier(getSafeString(rs, 1));
                itemSize.setSizeCode(getSafeString(rs, 2));
            }
            rs.close();
            
        }
        catch (SQLException se)
        {
            connection.logSQLException(se, "readItemSize");
            throw new DataException(DataException.SQL_ERROR, "readItemSize", se);
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
            throw new DataException(DataException.UNKNOWN, "readItemSize", e);
        }
        
        readI8ItemSize(connection,
                itemSize, 
                localeRequestor);
        return itemSize;
    }

    /**
     * read the item which contains the table description
     * @param connection
     * @param itemSize
     * @param localeRequestor
     * @throws DataException
     */
    // Begin GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item description
    // lcatania (Starmount) Mar 22, 2013
    // Changed method visibility from protected to public
    public void readI8ItemSize(JdbcDataConnection connection, 
                                       ItemSizeIfc itemSize,
                                       LocaleRequestor localeRequestor) throws DataException
    // End GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item description
    {
        SQLSelectStatement sql = new SQLSelectStatement();

        // Table to select from
        sql.addTable(TABLE_SIZE_I8);

        // add column
        sql.addColumn(FIELD_LOCALE);
        sql.addColumn(FIELD_SIZE_TABLE_DESCRIPTION);
        sql.addColumn(FIELD_SIZE_TABLE_NAME);

        // add identifier qualifier
        sql.addQualifier(FIELD_SIZE_CODE, inQuotes(itemSize.getSizeCode()));

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
                itemSize.setDescription(locale, getSafeString(rs, 2));
                itemSize.setName(locale, getSafeString(rs, 3));
            }
            rs.close();
        }
        catch (SQLException se)
        {
            connection.logSQLException(se, "readItemSize");
            throw new DataException(DataException.SQL_ERROR, "readItemSize", se);
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
            throw new DataException(DataException.UNKNOWN, "readItemSize", e);
        }
        
    }    
}
