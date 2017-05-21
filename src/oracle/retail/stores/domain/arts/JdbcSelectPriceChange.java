/* ===========================================================================
* Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/modules/domain/src/oracle/retail/stores/domain/arts/JdbcSelectPriceChange.java /rgbustores_13.4x_generic_branch/2 2011/09/21 14:03:00 cgreene Exp $
 * ===========================================================================
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED    (MM/DD/YY)
 *    cgreene   09/21/11 - removed deprated Jdbc operations for pricechange
 *    cgreene   05/26/10 - convert to oracle packaging
 *    cgreene   05/25/10 - refactored similar methods
 *    abondala  01/03/10 - update header date
 *    jswan     12/16/09 - Code review changes
 *    jswan     12/15/09 - Fixed commented out code.
 *    jswan     12/14/09 - Modifications for 'Min return price for X days'
 *                         feature.
 *    cgreene   09/25/09 - XbranchMerge cgreene_bug-8931126 from
 *                         rgbustores_13.1x_branch
 *    cgreene   09/24/09 - refactor SQL statements up support
 *                         preparedStatements for updates and inserts to
 *                         improve dept hist perf
 *    cgreene   04/14/09 - convert pricingGroupID to integer instead of string
 *    npoola    02/24/09 - Calendar and Date are converted to Timestamp in
 *                         Price Change
 *    cgreene   03/01/09 - XbranchMerge cgreene_bug-7691031 from
 *                         rgbustores_13.0.1_branch
 *    cgreene   02/11/09 - convert to using qualifier for prepared statements
 *    cgreene   01/22/09 - Derby can't seem to handle using the view well, so
 *                         re-do queries to use JOINs
 *    cgreene   01/18/09 - Restructured queries to take advantage of price
 *                         change views. Only parse expirationDate if type is
 *                         TPC.
 *    npoola    01/05/09 - fixed the Price Promotion name in receipt and
 *                         receipt PDO Issue
 *    npoola    11/30/08 - CSP POS and BO changes
 *
 * ===========================================================================
 * $Log:
 *  6    360Commerce 1.5         4/12/2008 5:42:56 PM   Christian Greene 31304
 *       Updatet to us "AMT", "PCT" and "AMTREPL" at codes for promotion
 *       types.
 *  5    360Commerce 1.4         3/28/2008 12:32:28 AM  Sujay Beesnalli Forward
 *        ported from v12x (CR# 30894). RPM Promotion IDs were not being
 *       persisted for items sold that had a promotion.
 *  4    360Commerce 1.3         11/15/2007 11:35:25 AM Christian Greene Belize
 *        merge - update PLU to calculate price base dupon effective
 *       pricechanges
 *  3    360Commerce 1.2         6/1/2007 3:09:43 PM    Christian Greene
 *       Backing out PLU to pre-v1.0.0.414 version code
 *  2    360Commerce 1.1         5/31/2007 5:27:59 PM   Anda D. Cadar   changed
 *        code to use FIELD_PERMANENT_PRICE_CHANGE_SALE_UNIT_AMOUNT
 *  1    360Commerce 1.0         5/31/2007 6:18:01 AM   Christian Greene
 *       initial version
 * $
 * ===========================================================================
 */
package oracle.retail.stores.domain.arts;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import oracle.retail.stores.common.sql.SQLParameterValue;
import oracle.retail.stores.common.sql.SQLSelectStatement;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.event.EventConstantsIfc;
import oracle.retail.stores.domain.event.ItemPriceMaintenanceEventIfc;
import oracle.retail.stores.domain.event.PriceChangeConstantsIfc;
import oracle.retail.stores.domain.event.PriceChangeIfc;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.store.StoreIfc;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataConnectionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataTransactionIfc;
import oracle.retail.stores.foundation.utility.Util;
import oracle.retail.stores.persistence.utility.ARTSDatabaseIfc;

/**
 * @author cgreene
 * @since 12.0.1
 */
public class JdbcSelectPriceChange extends JdbcDataOperation
    implements EventConstantsIfc, ARTSDatabaseIfc
{
    private static final long serialVersionUID = 3181486930203527239L;

    /** Revision number of this class. */
    public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/2 $";

    /**
     * Updates a {@link PLUItemIfc} with {@link PriceChangeIfc} information. The
     * item in the data action should contain an item id and store id for search
     * purposes.
     *  
     * @see oracle.retail.stores.foundation.manager.ifc.data.DataOperationIfc#execute(oracle.retail.stores.foundation.manager.ifc.data.DataTransactionIfc, oracle.retail.stores.foundation.manager.ifc.data.DataConnectionIfc, oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc)
     */
    public void execute(DataTransactionIfc dt, DataConnectionIfc dc, DataActionIfc da) throws DataException
    {
        JdbcDataConnection connection = (JdbcDataConnection) dc;
        PLUItemIfc searchObj = (PLUItemIfc) da.getDataObject();
        PriceChangeIfc[] changes = readTemporaryPriceChanges(connection, searchObj, Calendar.getInstance()); // this is not timezone safe
        searchObj.setTemporaryPriceChanges(changes);
        changes = readPermanentPriceChanges(connection, searchObj, Calendar.getInstance()); // this is not timezone safe
        searchObj.setPermanentPriceChanges(changes);
        dt.setResult(searchObj);
    }

    /**
     * Read a collection of permanent price changes based on the PLUItemIfc specified.
     * The store id and item id attribute of this object are required as
     * search parameters.
     * 
     * @param dataConnection the data connection to use
     * @param pluItem the item to search for its price changes
     * @return PriceChangeIfc[]
     */
    // Begin GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item description
    // lcatania (Starmount) Mar 22, 2013
    // Changed method visibility from protected to public
    public PriceChangeIfc[] readPermanentPriceChanges(JdbcDataConnection dataConnection, PLUItemIfc pluItem, Calendar when)
        throws DataException
    // End GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item description
    {
         try
        {
            // build sql
            SQLSelectStatement sql = buildSelectPermanentPriceChanges(pluItem.getStoreID(), pluItem.getItemID(), when);
            // execute sql
            dataConnection.execute(sql.getSQLString(), sql.getParameterValues());
            // parse results
            List<PriceChangeIfc> results = parsePermanentPriceChangeResults(dataConnection, pluItem);
            return results.toArray(new PriceChangeIfc[results.size()]);
        }
        catch (SQLException se)
        {
            throw new DataException(DataException.SQL_ERROR, "readPermanentPriceChanges", se);
        }
        catch (DataException de)
        {
            throw de;
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "readPermanentPriceChanges", e);
        }
    }

    /**
     * Read a collection of temporary price changes based on the PLUItemIfc specified.
     * The store id and item id attribute of this object are required as
     * search parameters.
     * 
     * @param dataConnection the data connection to use
     * @param pluItem the item to search for its price changes
     * @return PriceChangeIfc[]
     */
    protected PriceChangeIfc[] readTemporaryPriceChanges(JdbcDataConnection dataConnection, PLUItemIfc pluItem, Calendar when)
        throws DataException
    {
        try
        {
            // build sql
            SQLSelectStatement sql = buildSelectTemporaryPriceChanges(pluItem.getStoreID(), pluItem.getItemID(), when);
            // execute sql
            dataConnection.execute(sql.getSQLString(), sql.getParameterValues());
            // parse results
            List<PriceChangeIfc> results = parseTemporaryPriceChangeResults(dataConnection, pluItem);
            return results.toArray(new PriceChangeIfc[results.size()]);
        }
        catch (SQLException se)
        {
            throw new DataException(DataException.SQL_ERROR, "readTemporaryPriceChanges", se);
        }
        catch (DataException de)
        {
            throw de;
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "readTemporaryPriceChanges", e);
        }
    }

    /**
     * Read a collection of temporary price changes based on the PLUItemIfc specified.
     * The store id and item id attribute of this object are required as
     * search parameters.
     * 
     * @param dataConnection the data connection to use
     * @param pluItem the item to search for its price changes
     * @return PriceChangeIfc[]
     */
    // Begin GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item description
    // lcatania (Starmount) Mar 22, 2013
    // Changed method visibility from protected to public
    public PriceChangeIfc[] readAllTemporaryPriceChanges(JdbcDataConnection dataConnection, PLUItemIfc pluItem, Calendar when)
        throws DataException
    // End GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item description
    {
        try
        {
            // build sql
            SQLSelectStatement sql = buildSelectAllTemporaryPriceChanges(pluItem.getStoreID(), pluItem.getItemID(), when);
            // execute sql
            dataConnection.execute(sql.getSQLString(), sql.getParameterValues());
            // parse results
            List<PriceChangeIfc> results = parseTemporaryPriceChangeResults(dataConnection, pluItem);
            return results.toArray(new PriceChangeIfc[results.size()]);
        }
        catch (SQLException se)
        {
            throw new DataException(DataException.SQL_ERROR, "readTemporaryPriceChanges", se);
        }
        catch (DataException de)
        {
            throw de;
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "readTemporaryPriceChanges", e);
        }
    }

    /**
     * Return a SQLSelectStatement object to select permanent price changes
     * based on the store and item id. Example SQL query follows.
     * <p><blockquote><pre>
     * SELECT
     *      E.ID_EV, TY_EV_MNT, TS_EV_MNT_EF, UN_PRI_EV, UN_DG_LS_PRC,
     *      MO_CHN_PRN_UN_PRC, TY_CHN_PRN_UN_PRC, PPCI.MO_OVRD_PRC
     * FROM CO_EV E
     * JOIN CO_EV_MNT EVMNT ON EVMNT.ID_EV = E.ID_EV
     *      AND EVMNT.ID_STR_RT = E.ID_STR_RT
     * JOIN MA_PRC_ITM IPM ON IPM.ID_EV = EVMNT.ID_EV
     *      AND IPM.ID_STR_RT = EVMNT.ID_STR_RT
     * JOIN TR_CHN_PRN_PRC PPC ON PPC.ID_EV = IPM.ID_EV
     *      AND PPC.ID_STR_RT = IPM.ID_STR_RT
     * JOIN MA_ITM_PRN_PRC_ITM PPCI ON PPCI.ID_EV = PPC.ID_EV
     *      AND PPCI.ID_STR_RT = PPC.ID_STR_RT
     * WHERE TS_EV_MNT_EF <= '2009-01-21-14.39.37.640000' 
     * AND PPCI.ID_STR_RT = '00692' 
     * AND PPCI.ID_ITM = '13001227';
     * </pre></blockquote>
     *
     * @param storeId
     * @param itemId
     * @return
     */
    protected SQLSelectStatement buildSelectPermanentPriceChanges(String storeId, String itemId, Calendar when)
    {
        SQLSelectStatement sql = new SQLSelectStatement();
        // add tables
        sql.addTable(TABLE_EVENT, ALIAS_EVENT);
        // add columns
        sql.addColumn(ALIAS_EVENT, FIELD_EVENT_EVENT_ID);
        sql.addColumn(FIELD_MAINTENANCE_EVENT_TYPE_CODE);
        sql.addColumn(FIELD_MAINTENANCE_EVENT_EFFECTIVE_DATE);
        sql.addColumn(FIELD_MAINTENANCE_EVENT_EXPIRATION_DATE);
        sql.addColumn(FIELD_ITEM_PRICE_MAINTENANCE_EVENT_PRIORITY);
        sql.addColumn(FIELD_ITEM_PRICE_MAINTENANCE_PRICE_LAST_DIGIT);
        sql.addColumn(FIELD_PERMANENT_PRICE_CHANGE_SALE_UNIT_AMOUNT);
        sql.addColumn(FIELD_PERMANENT_PRICE_CHANGE_SALE_UNIT_AMOUNT_TYPE_CODE);
        sql.addColumn(ALIAS_PERMANENT_PRICE_CHANGE_ITEM, FIELD_PERMANENT_PRICE_CHANGE_ITEM_PRICE_OVERRIDE_AMOUNT);
        // joins
        sql.addOuterJoinQualifier(" JOIN " + TABLE_MAINTENANCE_EVENT + " " + ALIAS_MAINTENANCE_EVENT + 
                " ON " + ALIAS_MAINTENANCE_EVENT + "." + FIELD_EVENT_EVENT_ID + " = " + ALIAS_EVENT + "." + FIELD_EVENT_EVENT_ID +
                " AND " + ALIAS_MAINTENANCE_EVENT + "." + FIELD_RETAIL_STORE_ID + " = " + ALIAS_EVENT + "." + FIELD_RETAIL_STORE_ID);
        sql.addOuterJoinQualifier(" JOIN " + TABLE_ITEM_PRICE_MAINTENANCE + " " + ALIAS_ITEM_PRICE_MAINTENANCE + 
                " ON " + ALIAS_ITEM_PRICE_MAINTENANCE + "." + FIELD_EVENT_EVENT_ID + " = " + ALIAS_MAINTENANCE_EVENT + "." + FIELD_EVENT_EVENT_ID +
                " AND " + ALIAS_ITEM_PRICE_MAINTENANCE + "." + FIELD_RETAIL_STORE_ID + " = " + ALIAS_MAINTENANCE_EVENT + "." + FIELD_RETAIL_STORE_ID);
        sql.addOuterJoinQualifier(" JOIN " + TABLE_PERMANENT_PRICE_CHANGE + " " + ALIAS_PERMANENT_PRICE_CHANGE + 
                " ON " + ALIAS_PERMANENT_PRICE_CHANGE + "." + FIELD_EVENT_EVENT_ID + " = " + ALIAS_ITEM_PRICE_MAINTENANCE + "." + FIELD_EVENT_EVENT_ID +
                " AND " + ALIAS_PERMANENT_PRICE_CHANGE + "." + FIELD_RETAIL_STORE_ID + " = " + ALIAS_ITEM_PRICE_MAINTENANCE + "." + FIELD_RETAIL_STORE_ID);
        sql.addOuterJoinQualifier(" JOIN " + TABLE_PERMANENT_PRICE_CHANGE_ITEM + " " + ALIAS_PERMANENT_PRICE_CHANGE_ITEM + 
                " ON " + ALIAS_PERMANENT_PRICE_CHANGE_ITEM + "." + FIELD_EVENT_EVENT_ID + " = " + ALIAS_PERMANENT_PRICE_CHANGE + "." + FIELD_EVENT_EVENT_ID +
                " AND " + ALIAS_PERMANENT_PRICE_CHANGE_ITEM + "." + FIELD_RETAIL_STORE_ID + " = " + ALIAS_PERMANENT_PRICE_CHANGE + "." + FIELD_RETAIL_STORE_ID);
        // add qualifiers
        sql.addQualifier(new SQLParameterValue(FIELD_MAINTENANCE_EVENT_EFFECTIVE_DATE + " <= ?", calendarToTimestamp(when)));
        sql.addQualifier(new SQLParameterValue(ALIAS_PERMANENT_PRICE_CHANGE_ITEM, FIELD_PERMANENT_PRICE_CHANGE_ITEM_RETAIL_STORE_ID, storeId));
        sql.addQualifier(new SQLParameterValue(ALIAS_PERMANENT_PRICE_CHANGE_ITEM, FIELD_PERMANENT_PRICE_CHANGE_ITEM_ITEM_ID, itemId));
        return sql;
    }

    /**
     * Return a SQLSelectStatement object to select temporary price changes
     * based on the store and item id. Example SQL query follows.
     * <p><blockquote><pre>
     * SELECT
     *      E.ID_EV, TY_EV_MNT, TS_EV_MNT_EF, TS_EV_MNT_EP, UN_PRI_EV,
     *      UN_DG_LS_PRC, MO_CHN_PRN_UN_PRC, TY_CHN_PRN_UN_PRC,
     *      TPCI.MO_OVRD_PRC, ID_PRM, ID_PRM_CMP, ID_PRM_CMP_DTL 
     * FROM CO_EV E
     * JOIN CO_EV_MNT EVMNT ON EVMNT.ID_EV = E.ID_EV
     *      AND EVMNT.ID_STR_RT = E.ID_STR_RT
     * JOIN MA_PRC_ITM IPM ON IPM.ID_EV = EVMNT.ID_EV
     *      AND IPM.ID_STR_RT = EVMNT.ID_STR_RT
     * JOIN TR_CHN_TMP_PRC TPC ON TPC.ID_EV = IPM.ID_EV
     *      AND TPC.ID_STR_RT = IPM.ID_STR_RT
     * JOIN MA_ITM_TMP_PRC_CHN TPCI ON TPCI.ID_EV = TPC.ID_EV
     *      AND TPCI.ID_STR_RT = TPC.ID_STR_RT
     * WHERE TS_EV_MNT_EF <= '2009-01-21-14.39.37.640000' 
     * AND TS_EV_MNT_EP >= '2009-01-21-14.39.43.484000' 
     * AND TCPI.ID_STR_RT = '00692' 
     * AND TCPI.ID_ITM = '13001227';
     * </pre></blockquote>
     * 
     * @param storeId
     * @param itemId
     * @param when
     * @param returnExpired set to true to also retrieve expired price changes.
     * @return
     */
    protected SQLSelectStatement buildSelectTemporaryPriceChanges(String storeId,
              String itemId, Calendar when, boolean returnExpired)
    {
        SQLSelectStatement sql = new SQLSelectStatement();
        // add tables
        sql.addTable(TABLE_EVENT, ALIAS_EVENT);
        // add columns
        sql.addColumn(ALIAS_EVENT, FIELD_EVENT_EVENT_ID);
        sql.addColumn(ALIAS_EVENT, FIELD_EVENT_NAME);
        sql.addColumn(FIELD_MAINTENANCE_EVENT_TYPE_CODE);
        sql.addColumn(FIELD_MAINTENANCE_EVENT_EFFECTIVE_DATE);
        sql.addColumn(FIELD_MAINTENANCE_EVENT_EXPIRATION_DATE);
        sql.addColumn(FIELD_ITEM_PRICE_MAINTENANCE_EVENT_PRIORITY);
        sql.addColumn(FIELD_ITEM_PRICE_MAINTENANCE_PRICE_LAST_DIGIT);
        sql.addColumn(FIELD_TEMPORARY_PRICE_CHANGE_SALE_UNIT_AMOUNT);
        sql.addColumn(FIELD_TEMPORARY_PRICE_CHANGE_SALE_UNIT_AMOUNT_TYPE_CODE);
        sql.addColumn(ALIAS_TEMPORARY_PRICE_CHANGE_ITEM, FIELD_TEMPORARY_PRICE_CHANGE_ITEM_PRICE_OVERRIDE_AMOUNT);
        sql.addColumn(FIELD_PROMOTION_ID);
        sql.addColumn(FIELD_PROMOTION_COMPONENT_ID);
        sql.addColumn(FIELD_PROMOTION_COMPONENT_DETAIL_ID);
        sql.addColumn(ALIAS_ITEM_PRICE_MAINTENANCE, FIELD_CUSTOMER_PRICING_GROUP_ID);
        // joins
        sql.addOuterJoinQualifier(" JOIN " + TABLE_MAINTENANCE_EVENT + " " + ALIAS_MAINTENANCE_EVENT + 
                " ON " + ALIAS_MAINTENANCE_EVENT + "." + FIELD_EVENT_EVENT_ID + " = " + ALIAS_EVENT + "." + FIELD_EVENT_EVENT_ID +
                " AND " + ALIAS_MAINTENANCE_EVENT + "." + FIELD_RETAIL_STORE_ID + " = " + ALIAS_EVENT + "." + FIELD_RETAIL_STORE_ID);
        sql.addOuterJoinQualifier(" JOIN " + TABLE_ITEM_PRICE_MAINTENANCE + " " + ALIAS_ITEM_PRICE_MAINTENANCE + 
                " ON " + ALIAS_ITEM_PRICE_MAINTENANCE + "." + FIELD_EVENT_EVENT_ID + " = " + ALIAS_MAINTENANCE_EVENT + "." + FIELD_EVENT_EVENT_ID +
                " AND " + ALIAS_ITEM_PRICE_MAINTENANCE + "." + FIELD_RETAIL_STORE_ID + " = " + ALIAS_MAINTENANCE_EVENT + "." + FIELD_RETAIL_STORE_ID);
        sql.addOuterJoinQualifier(" JOIN " + TABLE_TEMPORARY_PRICE_CHANGE + " " + ALIAS_TEMPORARY_PRICE_CHANGE + 
                " ON " + ALIAS_TEMPORARY_PRICE_CHANGE + "." + FIELD_EVENT_EVENT_ID + " = " + ALIAS_ITEM_PRICE_MAINTENANCE + "." + FIELD_EVENT_EVENT_ID +
                " AND " + ALIAS_TEMPORARY_PRICE_CHANGE + "." + FIELD_RETAIL_STORE_ID + " = " + ALIAS_ITEM_PRICE_MAINTENANCE + "." + FIELD_RETAIL_STORE_ID);
        sql.addOuterJoinQualifier(" JOIN " + TABLE_TEMPORARY_PRICE_CHANGE_ITEM + " " + ALIAS_TEMPORARY_PRICE_CHANGE_ITEM + 
                " ON " + ALIAS_TEMPORARY_PRICE_CHANGE_ITEM + "." + FIELD_EVENT_EVENT_ID + " = " + ALIAS_TEMPORARY_PRICE_CHANGE + "." + FIELD_EVENT_EVENT_ID +
                " AND " + ALIAS_TEMPORARY_PRICE_CHANGE_ITEM + "." + FIELD_RETAIL_STORE_ID + " = " + ALIAS_TEMPORARY_PRICE_CHANGE + "." + FIELD_RETAIL_STORE_ID);
        // add qualifiers
        sql.addQualifier(new SQLParameterValue(FIELD_MAINTENANCE_EVENT_EFFECTIVE_DATE + " <= ?", calendarToTimestamp(when)));
        if (!returnExpired)
        {
            sql.addQualifier(new SQLParameterValue(FIELD_MAINTENANCE_EVENT_EXPIRATION_DATE + " >= ?", calendarToTimestamp(when)));
        }
        sql.addQualifier(new SQLParameterValue(ALIAS_TEMPORARY_PRICE_CHANGE_ITEM, FIELD_TEMPORARY_PRICE_CHANGE_ITEM_RETAIL_STORE_ID, storeId));
        sql.addQualifier(new SQLParameterValue(ALIAS_TEMPORARY_PRICE_CHANGE_ITEM, FIELD_TEMPORARY_PRICE_CHANGE_ITEM_ITEM_ID, itemId));
        return sql;
    }

    /**
     * Calls buildSelectTemporaryPriceChanges with <code>false</code> for returnExpired
     */
    protected SQLSelectStatement buildSelectTemporaryPriceChanges(String storeId, String itemId, Calendar when)
    {
        return buildSelectTemporaryPriceChanges(storeId, itemId, when, true);
    }

    /**
     * Calls buildSelectTemporaryPriceChanges with <code>true</code> for returnExpired
     */
    protected SQLSelectStatement buildSelectAllTemporaryPriceChanges(String storeId, String itemId, Calendar when)
    {
        return buildSelectTemporaryPriceChanges(storeId, itemId, when, true);
    }

    protected List<PriceChangeIfc> parsePermanentPriceChangeResults(DataConnectionIfc dataConnection, PLUItemIfc pluItem)
        throws DataException, SQLException
    {
        List<PriceChangeIfc> results = new ArrayList<PriceChangeIfc>();
        List<ItemPriceMaintenanceEventIfc> maintenanceEvents = new ArrayList<ItemPriceMaintenanceEventIfc>();
    
        ResultSet rs = (ResultSet)dataConnection.getResult();
        if (rs != null)
        {
            while (rs.next())
            {
                // event id
                int eventID = rs.getInt(FIELD_EVENT_EVENT_ID);
                ItemPriceMaintenanceEventIfc event = getItemPriceMaintenanceEvent(maintenanceEvents, eventID);
                if (event == null)
                {
                    event = createItemPriceMaintenanceEvent(rs, eventID, pluItem.getStoreID(), false);
                    maintenanceEvents.add(event);
                }
                // price change
                PriceChangeIfc priceChange = DomainGateway.getFactory().getPriceChangeInstance();
                // override amount
                BigDecimal amt = rs.getBigDecimal(FIELD_PERMANENT_PRICE_CHANGE_SALE_UNIT_AMOUNT);
                if (amt != null)
                {
                    priceChange.setOverridePriceAmount(DomainGateway.getBaseCurrencyInstance(amt));
                }
                // use override price amount instead of sale unit amount if available
                amt = rs.getBigDecimal(FIELD_TEMPORARY_PRICE_CHANGE_ITEM_PRICE_OVERRIDE_AMOUNT);
                if (amt != null)
                {
                    priceChange.setOverridePriceAmount(DomainGateway.getBaseCurrencyInstance(amt));
                }
    
                priceChange.setItem(pluItem);
                event.addItem(priceChange);
                results.add(priceChange);
            }
        }
        return results;
    }
    
    protected List<PriceChangeIfc> parseTemporaryPriceChangeResults(DataConnectionIfc dataConnection, PLUItemIfc pluItem)
        throws DataException, SQLException
    {
        List<PriceChangeIfc> results = new ArrayList<PriceChangeIfc>();
        List<ItemPriceMaintenanceEventIfc> maintenanceEvents = new ArrayList<ItemPriceMaintenanceEventIfc>();
    
        ResultSet rs = (ResultSet)dataConnection.getResult();
        if (rs != null)
        {
            while (rs.next())
            {
                // event id
                int eventID = rs.getInt(FIELD_EVENT_EVENT_ID);
                ItemPriceMaintenanceEventIfc event = getItemPriceMaintenanceEvent(maintenanceEvents, eventID);
                if (event == null)
                {
                    event = createItemPriceMaintenanceEvent(rs, eventID, pluItem.getStoreID(), true);
                    maintenanceEvents.add(event);
                }
                // price change
                PriceChangeIfc priceChange = DomainGateway.getFactory().getPriceChangeInstance();
                // override amount
                BigDecimal amt = rs.getBigDecimal(FIELD_TEMPORARY_PRICE_CHANGE_ITEM_PRICE_OVERRIDE_AMOUNT);
                if (amt != null)
                {
                    priceChange.setOverridePriceAmount(DomainGateway.getBaseCurrencyInstance(amt));
                }
                priceChange.setPromotionId(rs.getLong(FIELD_PROMOTION_ID));
                priceChange.setPromotionComponentId(rs.getLong(FIELD_PROMOTION_COMPONENT_ID));
                priceChange.setPromotionComponentDetailId(rs.getLong(FIELD_PROMOTION_COMPONENT_DETAIL_ID));
                priceChange.setPromotionName(rs.getString(FIELD_EVENT_NAME));
				BigDecimal pricingGroupID = rs.getBigDecimal(FIELD_CUSTOMER_PRICING_GROUP_ID);
				if (pricingGroupID != null)
				{
				    priceChange.setPricingGroupID(pricingGroupID.intValue());
				}
                priceChange.setItem(pluItem);
                event.addItem(priceChange);
                results.add(priceChange);
            }
        }
        return results;
    }
    
    /**
     * Retrieves the source-code-control system revision number.
     * @return String representation of revision number
     */
    protected String getRevisionNumber()
    {
        return revisionNumber;
    }
    
    /**
     * Returns the string representation of this object.
     * @return String representation of object
     */
    public String toString()
    {
        return Util.classToStringHeader("JdbcSelectPriceChange",
                                        getRevisionNumber(),
                                        hashCode()).toString() ;
    }

    /**
     * Return a new ItemPriceMaintenanceEventIfc
     * 
     * @param rs
     * @param eventID
     * @param storeID
     * @return
     * @throws SQLException
     */
    private ItemPriceMaintenanceEventIfc createItemPriceMaintenanceEvent(ResultSet rs, int eventID, String storeID, boolean isTemporary)
        throws SQLException
    {
        ItemPriceMaintenanceEventIfc event = DomainGateway.getFactory().getItemPriceMaintenanceEventInstance();
        event.setEventID(eventID);

        // store id
        StoreIfc store = DomainGateway.getFactory().getStoreInstance();
        store.setStoreID(storeID);
        event.setStore(store);

        // type code
        String typeCode = rs.getString(FIELD_MAINTENANCE_EVENT_TYPE_CODE);
        event.setTypeCode(EventConstantsIfc.EVENT_TYPE_UNDEFINED);
        for (int i = 0; i < EventConstantsIfc.EVENT_TYPE_CODE.length; i++)
        {
            if (EventConstantsIfc.EVENT_TYPE_CODE[i].equals(typeCode))
            {
                event.setTypeCode(i);
                break;
            }
        }

        // effective date
        Timestamp effectiveDate = rs.getTimestamp(FIELD_MAINTENANCE_EVENT_EFFECTIVE_DATE);
        event.setEffectiveDateTimestamp(timestampToEYSDate(effectiveDate));

        // expiry date
        Timestamp expirationDate = rs.getTimestamp(FIELD_MAINTENANCE_EVENT_EXPIRATION_DATE);
        if (expirationDate != null)
        {
            event.setExpirationDateTimestamp(timestampToEYSDate(expirationDate));
        }

        BigDecimal amt = null;
        if (isTemporary)
        {
            amt = rs.getBigDecimal(FIELD_TEMPORARY_PRICE_CHANGE_SALE_UNIT_AMOUNT);
            typeCode = rs.getString(FIELD_TEMPORARY_PRICE_CHANGE_SALE_UNIT_AMOUNT_TYPE_CODE);
        }
        else
        {
            amt = rs.getBigDecimal(FIELD_PERMANENT_PRICE_CHANGE_SALE_UNIT_AMOUNT);
            typeCode = rs.getString(FIELD_PERMANENT_PRICE_CHANGE_SALE_UNIT_AMOUNT_TYPE_CODE);
        }
        event.setSaleUnitAmount(amt);
        event.setApplicationCode(PriceChangeConstantsIfc.APPLICATION_CODE_UNDEFINED);
        for (int i = 0; i < PriceChangeConstantsIfc.APPLICATION_CODE.length; i++)
        {
            if (PriceChangeConstantsIfc.APPLICATION_CODE[i].equals(typeCode))
            {
                event.setApplicationCode(i);
                break;
            }
        }

        // priority
        event.setPriority(rs.getInt(FIELD_ITEM_PRICE_MAINTENANCE_EVENT_PRIORITY));
        // last digit
        String lastDigit = rs.getString(FIELD_ITEM_PRICE_MAINTENANCE_PRICE_LAST_DIGIT);
        if (lastDigit == null)
        {
            event.setLastPriceDigit(-1);
        }
        else
        {
            event.setLastPriceDigit(Integer.parseInt(lastDigit));
        }
        return event;
    }

    /**
     * Return a ItemPriceMaintenanceEventIfc if it exists in the list. Checking
     * by event id.
     * 
     * @param maintenanceEvents
     * @param eventID
     * @return ItemPriceMaintenanceEventIfc
     */
    private ItemPriceMaintenanceEventIfc getItemPriceMaintenanceEvent(List<ItemPriceMaintenanceEventIfc> maintenanceEvents, int eventID)
    {
        for (Iterator<ItemPriceMaintenanceEventIfc> iter = maintenanceEvents.iterator(); iter.hasNext();)
        {
            ItemPriceMaintenanceEventIfc event = iter.next();
            if (event.getEventID() == eventID)
            {
                return event;
            }
        }
        return null;
    }

}
