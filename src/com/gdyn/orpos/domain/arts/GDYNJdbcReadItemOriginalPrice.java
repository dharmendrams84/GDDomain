//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.arts;

import java.sql.ResultSet;
import java.sql.SQLException;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.common.sql.SQLSelectStatement;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.arts.JdbcPLUOperation;

import oracle.retail.stores.domain.transaction.SearchCriteriaIfc;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataConnectionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataTransactionIfc;

// Begin GD-49: Develop Employee Discount Module
// lcatania (Starmount) Mar 4, 2013

/**
 * A new operation to retrieve the original price of an item
 * 
 * @author lcatania
 *
 */
public class GDYNJdbcReadItemOriginalPrice extends JdbcPLUOperation
{
    /**
     * This id is used to tell the compiler not to generate a new serialVersionUID.
     */
    private static final long serialVersionUID = 8057826928091802441L;

    /**
     * Executes the SQL statements against the database.
     * 
     * @param dataTransaction
     *            The data transaction
     * @param dataConnection
     *            The connection to the data source
     * @param action
     *            The information passed by the valet
     * @exception DataException
     *                upon error
     */
    @Override
    public void execute(DataTransactionIfc dataTransaction,
            DataConnectionIfc dataConnection,
            DataActionIfc action)
            throws DataException
    {
        if (logger.isDebugEnabled())
            logger.debug("Entering GDYNJdbcReadItemOriginalPrice.execute");
        logger.info("1.Entering GDYNJdbcReadItemOriginalPrice.execute");
        System.out.println("Inside execute method GDYNJdbcReadItemOriginalPrice");
        JdbcDataConnection connection = (JdbcDataConnection) dataConnection;
        SearchCriteriaIfc itemInfo = (SearchCriteriaIfc) action.getDataObject();

        // Get the original price of the item
      //  CurrencyIfc itemOriginalPrice = readItemOriginalPrice(connection, itemInfo);

        readItemOriginalPrice(connection);
        // Set results
     //   dataTransaction.setResult(itemOriginalPrice);

        if (logger.isDebugEnabled())
            logger.debug("Exiting GDYNJdbcReadItemOriginalPrice.execute");
    }

    /**
     * Reads item original price
     * 
     * @param dataConnection
     *            a connection to the database
     * @param info
     *            the item lookup key
     * @return The original price
     * @exception DataException
     *                thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public CurrencyIfc readItemOriginalPrice1(JdbcDataConnection dataConnection, SearchCriteriaIfc info)
            throws DataException
    {
    	
    	logger.info("1.Entering GDYNJdbcReadItemOriginalPrice.readItemOriginalPrice");
    	System.out.println("1.Entering GDYNJdbcReadItemOriginalPrice.readItemOriginalPrice");
    	
        String itemNumber = info.getItemNumber();
        String storeID = info.getStoreNumber();
        
        // Get the effective date from the oldest maintenance event for the item and retail store specified
        // to use it as an argument for the main query
        String firstMaintenanceEventEffectiveDate = getFirstMaintenanceEventEffectiveDate(dataConnection, info);

        SQLSelectStatement sql = new SQLSelectStatement();

        // add tables
        sql.addTable(TABLE_EVENT, ALIAS_EVENT);
        sql.addTable(TABLE_MAINTENANCE_EVENT, ALIAS_MAINTENANCE_EVENT);
        sql.addTable(TABLE_ITEM_PRICE_MAINTENANCE, ALIAS_ITEM_PRICE_MAINTENANCE);
        sql.addTable(TABLE_PERMANENT_PRICE_CHANGE, ALIAS_PERMANENT_PRICE_CHANGE);
        sql.addTable(TABLE_PERMANENT_PRICE_CHANGE_ITEM, ALIAS_PERMANENT_PRICE_CHANGE_ITEM);
        
        // add columns
        sql.addColumn(ALIAS_PERMANENT_PRICE_CHANGE + "." + FIELD_PERMANENT_PRICE_CHANGE_SALE_UNIT_AMOUNT);
        
        // add join qualifiers
        sql.addJoinQualifier(ALIAS_EVENT, FIELD_EVENT_EVENT_ID,
                ALIAS_MAINTENANCE_EVENT, FIELD_EVENT_EVENT_ID);
        sql.addJoinQualifier(ALIAS_EVENT, FIELD_EVENT_RETAIL_STORE_ID,
                ALIAS_MAINTENANCE_EVENT, FIELD_EVENT_RETAIL_STORE_ID);
        sql.addJoinQualifier(ALIAS_MAINTENANCE_EVENT, FIELD_EVENT_EVENT_ID, ALIAS_ITEM_PRICE_MAINTENANCE, FIELD_EVENT_EVENT_ID);
        sql.addJoinQualifier(ALIAS_MAINTENANCE_EVENT, FIELD_EVENT_RETAIL_STORE_ID, ALIAS_ITEM_PRICE_MAINTENANCE, FIELD_EVENT_RETAIL_STORE_ID);
        sql.addJoinQualifier(ALIAS_ITEM_PRICE_MAINTENANCE, FIELD_EVENT_EVENT_ID, ALIAS_PERMANENT_PRICE_CHANGE, FIELD_EVENT_EVENT_ID);
        sql.addJoinQualifier(ALIAS_ITEM_PRICE_MAINTENANCE, FIELD_EVENT_RETAIL_STORE_ID, ALIAS_PERMANENT_PRICE_CHANGE, FIELD_EVENT_RETAIL_STORE_ID);
        sql.addJoinQualifier(ALIAS_PERMANENT_PRICE_CHANGE, FIELD_EVENT_EVENT_ID, ALIAS_PERMANENT_PRICE_CHANGE_ITEM, FIELD_EVENT_EVENT_ID);
        sql.addJoinQualifier(ALIAS_PERMANENT_PRICE_CHANGE, FIELD_EVENT_RETAIL_STORE_ID, ALIAS_PERMANENT_PRICE_CHANGE_ITEM, FIELD_EVENT_RETAIL_STORE_ID);
        
        // add qualifiers
        sql.addQualifier(ALIAS_PERMANENT_PRICE_CHANGE_ITEM, FIELD_EVENT_RETAIL_STORE_ID, "'" + storeID + "'");
        sql.addQualifier(ALIAS_PERMANENT_PRICE_CHANGE_ITEM, FIELD_ITEM_ID, "'" + itemNumber + "'");
        sql.addQualifier(ALIAS_MAINTENANCE_EVENT + "." + FIELD_MAINTENANCE_EVENT_EFFECTIVE_DATE + "<= TO_TIMESTAMP('" + firstMaintenanceEventEffectiveDate + "', 'YYYY-MM-DD HH24:MI:SS.FF')");

        // perform the query
        CurrencyIfc itemOriginalPrice = null;
        try
        {
            String sqlToRun = sql.getSQLString();
            dataConnection.execute(sqlToRun);

            ResultSet rs = (ResultSet) dataConnection.getResult();

            while (rs.next())
            {
                String itemOriginalPriceString = getSafeString(rs, 1);
                itemOriginalPrice = DomainGateway.getBaseCurrencyInstance(itemOriginalPriceString);
            }
            rs.close();
        }
        catch (DataException de)
        {
            logger.warn(de);
            throw de;
        }
        catch (SQLException se)
        {
            dataConnection.logSQLException(se, "GDYNJdbcReadItemOriginalPrice");
            throw new DataException(DataException.SQL_ERROR, "ReadItemOriginalPrice", se);
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "ReadItemOriginalPrice", e);
        }

        if (itemOriginalPrice == null)
        {
            throw new DataException(DataException.NO_DATA,
                    "No Price was found processing the result set in GDYNJdbcReadItemOriginalPrice.");
        }
        return itemOriginalPrice;
    }

    public void readItemOriginalPrice(JdbcDataConnection dataConnection )
            throws DataException
    {
    	
    	System.out.println("inside readItemDivision");
        // add tables
        SQLSelectStatement sql = new SQLSelectStatement();
        sql.addTable("MERCHANDISE_HIERARCHY_VIEW");
        sql.addColumn("ID_MRHRC_GP");
        sql.addColumn("DIV");
        sql.addColumn("DIVDESC");
        sql.addColumn("GRP");
        sql.addColumn("GRPDESC");
        sql.addColumn("DPT");
        sql.addColumn("DPTDESC");
        sql.addColumn("CLS");
        sql.addColumn("CLSDESC");
        sql.addColumn("SCL");
        sql.addColumn("SCLDESC");
        
        String merhGrpIdVal = "5:025000040004";
        String merchGrpId ="'"+merhGrpIdVal+"'";
        sql.addQualifier("ID_MRHRC_GP", merchGrpId);
        
        
        
        // perform the query
       // CurrencyIfc itemOriginalPrice = null;
        try
        {
            String sqlToRun = sql.getSQLString();
            dataConnection.execute(sqlToRun);
            ResultSet rs = (ResultSet) dataConnection.getResult();
            System.out.println("After getting resultset ");
            while (rs.next())
            {
              System.out.println(getSafeString(rs, 1)+" : "+getSafeString(rs, 2)+" : "+getSafeString(rs, 3)+ " : "+getSafeString(rs,4));     
            }
            rs.close();
        }
        catch (DataException de)
        {
            logger.warn(de);
            throw de;
        }
        catch (SQLException se)
        {
            dataConnection.logSQLException(se, "GDYNJdbcReadItemOriginalPrice");
            throw new DataException(DataException.SQL_ERROR, "ReadItemOriginalPrice", se);
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "ReadItemOriginalPrice", e);
        }

        
       // return itemOriginalPrice;
    }


    
    
    /**
     * Reads the effective date from the oldest maintenance event for the item and retail store specified
     * 
     * @param dataConnection
     *            a connection to the database
     * @param info
     *            the item lookup key
     * @return The effective date from the oldest maintenance event
     * @exception DataException
     *                thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public String getFirstMaintenanceEventEffectiveDate(JdbcDataConnection dataConnection, SearchCriteriaIfc info)
            throws DataException
    {
    	logger.info("1.Entering GDYNJdbcReadItemOriginalPrice.getFirstMaintenanceEventEffectiveDate");
    	
    	String itemNumber = info.getItemNumber();
        String storeID = info.getStoreNumber();

        SQLSelectStatement sql = new SQLSelectStatement();

        // add tables
        sql.addTable(TABLE_MAINTENANCE_EVENT, ALIAS_MAINTENANCE_EVENT);
        sql.addTable(TABLE_PERMANENT_PRICE_CHANGE_ITEM, ALIAS_PERMANENT_PRICE_CHANGE_ITEM);

        // add columns
        sql.addColumn("MIN(" + ALIAS_MAINTENANCE_EVENT + "." + FIELD_MAINTENANCE_EVENT_EFFECTIVE_DATE + ") AS MIN_EFFECTIVE_DATE"); 
        
        // add join qualifiers
        sql.addJoinQualifier(ALIAS_PERMANENT_PRICE_CHANGE_ITEM, FIELD_EVENT_EVENT_ID,
                ALIAS_MAINTENANCE_EVENT, FIELD_EVENT_EVENT_ID);
        sql.addJoinQualifier(ALIAS_PERMANENT_PRICE_CHANGE_ITEM, FIELD_EVENT_RETAIL_STORE_ID,
                ALIAS_MAINTENANCE_EVENT, FIELD_EVENT_RETAIL_STORE_ID);
        
        // add qualifier
        sql.addQualifier(ALIAS_PERMANENT_PRICE_CHANGE_ITEM, FIELD_EVENT_RETAIL_STORE_ID, "'" + storeID + "'");
        sql.addQualifier(ALIAS_PERMANENT_PRICE_CHANGE_ITEM, FIELD_ITEM_ID, "'" + itemNumber + "'");

        // perform the query
        String firstMaintenanceEventEffectiveDate = null;
        try
        {
            String sqlToRun = sql.getSQLString();
            dataConnection.execute(sqlToRun);

            ResultSet rs = (ResultSet) dataConnection.getResult();

            while (rs.next())
            {
                firstMaintenanceEventEffectiveDate = getSafeString(rs, 1);
            }
            rs.close();
        }
        catch (DataException de)
        {
            logger.warn(de);
            throw de;
        }
        catch (SQLException se)
        {
            dataConnection.logSQLException(se, "GDYNJdbcReadItemOriginalPrice");
            throw new DataException(DataException.SQL_ERROR, "ReadItemOriginalPrice", se);
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "ReadItemOriginalPrice", e);
        }

        if (firstMaintenanceEventEffectiveDate.isEmpty())
        {
            throw new DataException(DataException.NO_DATA,
                    "No Event was found processing the result set in GDYNJdbcReadItemOriginalPrice.");
        }

        return firstMaintenanceEventEffectiveDate;
    }
}

// End GD-49: Develop Employee Discount Module