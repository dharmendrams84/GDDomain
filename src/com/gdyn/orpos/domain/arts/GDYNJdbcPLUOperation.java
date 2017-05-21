//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.arts;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import oracle.retail.stores.common.sql.SQLParameterIfc;
import oracle.retail.stores.common.sql.SQLParameterValue;
import oracle.retail.stores.common.sql.SQLSelectStatement;
import oracle.retail.stores.common.utility.LocaleRequestor;
import oracle.retail.stores.common.utility.LocaleUtilities;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.arts.JdbcPLUOperation;
import oracle.retail.stores.domain.arts.JdbcReadItemColors;
import oracle.retail.stores.domain.arts.JdbcReadItemSizes;
import oracle.retail.stores.domain.arts.JdbcReadItemStyles;
import oracle.retail.stores.domain.arts.JdbcSelectPriceChange;
import oracle.retail.stores.domain.arts.PLURequestor;
import oracle.retail.stores.domain.discount.DiscountRuleConstantsIfc;
import oracle.retail.stores.domain.event.PriceChangeIfc;
import oracle.retail.stores.domain.stock.GiftCardPLUItemIfc;
import oracle.retail.stores.domain.stock.ItemClassificationConstantsIfc;
import oracle.retail.stores.domain.stock.ItemColorIfc;
import oracle.retail.stores.domain.stock.ItemKitIfc;
import oracle.retail.stores.domain.stock.ItemSizeIfc;
import oracle.retail.stores.domain.stock.ItemStyleIfc;
import oracle.retail.stores.domain.stock.KitComponentIfc;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.stock.ProductGroupConstantsIfc;
import oracle.retail.stores.domain.stock.StockItemIfc;
import oracle.retail.stores.domain.stock.UnitOfMeasureIfc;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.foundation.utility.LocaleMap;
import oracle.retail.stores.persistence.utility.ARTSDatabaseIfc;

import com.gdyn.orpos.domain.stock.GDYNPLUItem;
import com.gdyn.orpos.domain.stock.GDYNPLUItemIfc;

/**
 * GDYNJdbcPLUOperation extended to retrieve color, size and style
 */
@SuppressWarnings("deprecation")
public class GDYNJdbcPLUOperation extends JdbcPLUOperation
        implements ARTSDatabaseIfc, ProductGroupConstantsIfc, DiscountRuleConstantsIfc
{
    // ID for compatible serialization
    private static final long serialVersionUID = 6476326562623L;
    
    protected static final Logger logger = Logger.getLogger(GDYNJdbcPLUOperation.class);

    /**
     * Selects items from the Stock Item table.
     * <p>
     * <blockquote>
     * 
     * <pre>
     * SELECT SITM.LU_UOM_SLS, SITM.FL_VLD_SRZ_ITM, UOM.FL_UOM_ENG_MC, UOM_I8.LCL, UOM_I8.NM_UOM, UOM_I8.DE_UOM, UOM.FL_DFLT_UOM, SITM.ED_CLR, SITM.ED_SZ, SITM.LU_STYL, SITM.FL_FE_RSTK, SITM.QW_ITM_PCK, SITM.FL_SRZ_CRT_EXT, SITM.CD_SRZ_CPT_TM, SL.NM_SRZ_ITM_LB
     * FROM CO_UOM UOM
     * JOIN CO_UOM_I8 UOM_I8 ON UOM.LU_UOM = UOM_I8.LU_UOM
     *      AND UOM_I8.LCL IN ('en')
     * JOIN AS_ITM_STK SITM ON SITM.LU_UOM_SLS = UOM.LU_UOM
     * LEFT JOIN AS_ITM_SRZ_LB_I8 SL ON SITM.ID_SRZ_ITM_LB = SL.ID_SRZ_ITM_LB
     *      AND SL.LCL IN ('en')
     * WHERE SITM.ID_ITM = '1234';
     * </pre>
     * 
     * </blockquote>
     * 
     * @param dataConnection
     *            a connection to the database
     * @param itemID
     *            the item ID
     * @param pluItem
     *            the PLU Item
     * @exception DataException
     *                thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public void selectStockItem(JdbcDataConnection dataConnection, String itemID, PLUItemIfc pluItem,
            LocaleRequestor localeRequestor) throws DataException
    {
        SQLSelectStatement sql = new SQLSelectStatement();

        // add tables
        sql.addTable(TABLE_UNIT_OF_MEASURE, ALIAS_UNIT_OF_MEASURE);

        // add columns
        sql.addColumn(ALIAS_STOCK_ITEM + "." + FIELD_STOCK_ITEM_SALE_UNIT_OF_MEASURE_CODE);
        sql.addColumn(ALIAS_STOCK_ITEM + "." + FIELD_SERIALIZED_ITEM_VALIDATION_FLAG);
        sql.addColumn(ALIAS_UNIT_OF_MEASURE + "." + FIELD_UNIT_OF_MEASURE_ENGLISH_METRIC_FLAG);
        sql.addColumn(ALIAS_UNIT_OF_MEASURE_I8 + "." + FIELD_LOCALE);
        sql.addColumn(ALIAS_UNIT_OF_MEASURE_I8 + "." + FIELD_UNIT_OF_MEASURE_NAME);
        sql.addColumn(ALIAS_UNIT_OF_MEASURE_I8 + "." + FIELD_UNIT_OF_MEASURE_DESCRIPTION);
        sql.addColumn(ALIAS_UNIT_OF_MEASURE + "." + FIELD_DEFAULT_UNIT_OF_MEASURE_FLAG);
        sql.addColumn(ALIAS_STOCK_ITEM, FIELD_COLOR_CODE);
        sql.addColumn(ALIAS_STOCK_ITEM, FIELD_SIZE_CODE);
        sql.addColumn(ALIAS_STOCK_ITEM, FIELD_STYLE_CODE);
        sql.addColumn(ALIAS_STOCK_ITEM + "." + FIELD_STOCK_ITEM_RESTOCKING_FEE_FLAG);
        sql.addColumn(ALIAS_STOCK_ITEM + "." + FIELD_PACK_ITEM_WEIGHT_COUNT);

        // Adding columns for serialisation
        sql.addColumn(ALIAS_STOCK_ITEM + "." + FIELD_SERIALIZED_ITEM_EXTERNAL_SYSTEM_CREATE_UIN);
        sql.addColumn(ALIAS_STOCK_ITEM + "." + FIELD_SERIALIZED_ITEM_CAPTURE_TIME);
        sql.addColumn(ALIAS_SERIALIZED_ITEM_LABEL_I8 + "." + FIELD_UIN_LABEL_NAME);

        // add joins
        sql.addOuterJoinQualifier(" JOIN " + TABLE_UNIT_OF_MEASURE_I8 + " " + ALIAS_UNIT_OF_MEASURE_I8 +
                " ON " + ALIAS_UNIT_OF_MEASURE + "." + FIELD_UNIT_OF_MEASURE_CODE +
                " = " + ALIAS_UNIT_OF_MEASURE_I8 + "." + FIELD_UNIT_OF_MEASURE_CODE +
                " AND " + ALIAS_UNIT_OF_MEASURE_I8 + "." + FIELD_LOCALE +
                buildINClauseString(LocaleMap.getBestMatch("", localeRequestor.getLocales())));

        sql.addOuterJoinQualifier(" JOIN " + TABLE_STOCK_ITEM + " " + ALIAS_STOCK_ITEM +
                " ON " + ALIAS_STOCK_ITEM + "." + FIELD_STOCK_ITEM_SALE_UNIT_OF_MEASURE_CODE +
                " = " + ALIAS_UNIT_OF_MEASURE + "." + FIELD_UNIT_OF_MEASURE_CODE);

        sql.addOuterJoinQualifier(" LEFT JOIN " + TABLE_SERIALIZED_ITEM_LABEL_I8 + " " + ALIAS_SERIALIZED_ITEM_LABEL_I8
                +
                " ON " + ALIAS_STOCK_ITEM + "." + FIELD_UIN_LABEL_ID +
                " = " + ALIAS_SERIALIZED_ITEM_LABEL_I8 + "." + FIELD_UIN_LABEL_ID +
                " AND " + ALIAS_SERIALIZED_ITEM_LABEL_I8 + "." + FIELD_LOCALE +
                buildINClauseString(LocaleMap.getBestMatch("", localeRequestor.getLocales())));

        // for the specific item
        sql.addQualifier(new SQLParameterValue(ALIAS_STOCK_ITEM, FIELD_ITEM_ID, itemID));

        try
        {
            ResultSet rs = execute(dataConnection, sql);
            Locale locale = null;
            UnitOfMeasureIfc pluUOM = DomainGateway.getFactory().getUnitOfMeasureInstance();

            while (rs.next())
            {
                // parse the result set
                int index = 0;
                String uomCode = getSafeString(rs, ++index);
                boolean isSerializedItem = getBooleanFromString(rs, ++index);
                boolean isMetric = getBooleanFromString(rs, ++index);
                locale = LocaleUtilities.getLocaleFromString(getSafeString(rs, ++index));
                String uomName = getSafeString(rs, ++index);
                /* String uomDescription = */getSafeString(rs, ++index);
                boolean isDefaultUOM = getBooleanFromString(rs, ++index);
                // Begin GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item description
                // lcatania (Starmount) Mar 22, 2013
                // commented out
                String colorCode = getSafeString(rs, ++index);
                String sizeCode = getSafeString(rs, ++index);
                String styleCode = getSafeString(rs, ++index);
                // End GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item
                // description
                boolean hasRestockingFee = getBooleanFromString(rs, ++index);
                BigDecimal itemWeight = getBigDecimal(rs, ++index);
                boolean isExternalSystemCreateUINAllowed = getBooleanFromString(rs, ++index);
                String serialEntryTime = getSafeString(rs, ++index);
                String UINLabel = getSafeString(rs, ++index);

                // Determine if we need to make the unit of measure object for
                // this PLU item
                // if the uom is not the default value, then make the uom
                // reference
                if (!isDefaultUOM) // not the default, make uom reference object
                {
                    pluUOM.setName(locale, uomName);
                    pluUOM.setMetric(isMetric);
                    pluUOM.setUnitID(uomCode);
                    pluItem.setUnitOfMeasure(pluUOM);
                }

                // Begin GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item
                // description
                // lcatania (Starmount) Mar 22, 2013
                if (pluItem instanceof GDYNPLUItem)
                {
                    GDYNPLUItem gdynPluItem = (GDYNPLUItem) pluItem;
                    StockItemIfc stockItem = gdynPluItem.getStockItem();
                    stockItem.getItemColor().setIdentifier(colorCode);
                    stockItem.getItemSize().setIdentifier(sizeCode);
                    stockItem.getItemSize().setSizeCode(sizeCode);
                    stockItem.getItemStyle().setIdentifier(styleCode);
                }
                // End GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item
                // description

                pluItem.setItemWeight(itemWeight);
                pluItem.getItemClassification().setSerializedItem(isSerializedItem);
                pluItem.getItemClassification().setRestockingFeeFlag(hasRestockingFee);
                pluItem.getItemClassification().setExternalSystemCreateUIN(isExternalSystemCreateUINAllowed);
                pluItem.getItemClassification().setSerialEntryTime(serialEntryTime);
                pluItem.getItemClassification().setUINLabel(UINLabel);
            }
        }
        catch (DataException de)
        {
            logger.warn(de.toString());
            throw de;
        }
        catch (SQLException se)
        {
            dataConnection.logSQLException(se, "StockItem lookup");
            throw new DataException(DataException.SQL_ERROR, "StockItem lookup", se);
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "StockItem lookup", e);
        }
    }

    /**
     * Selects items from the POS Identity and Item tables.
     * 
     * @param dataConnection
     *            a connection to the database
     * @param andQualifiers
     *            a list of qualifiers used in WHERE clause -- the qualifiers in the list will be separated by "AND".
     * @param orQualifiers
     *            a list of qualifiers used in WHERE clause -- the qualifiers in the list will be separated by "OR".
     * @param pluRequestor
     *            the plu requestor
     * @param sqlLocale
     *            the LocaleRequestor object
     * @return An array of PLUItems with associated advanced pricing rules
     * @exception DataException
     *                thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public PLUItemIfc[] selectPLUItem(JdbcDataConnection dataConnection, List<SQLParameterIfc> andQualifiers,
            List<SQLParameterIfc> orQualifiers,
            PLURequestor pluRequestor, LocaleRequestor sqlLocale) throws DataException
    {
    	logger.info("inside selectPLUItem method");
        if (usesWildcards(andQualifiers) || usesWildcards(orQualifiers))
        {
            // wildcards were used in the query
            // call selectPLUItems() to retrieve all the matching items
            return selectPLUItems(dataConnection, andQualifiers, orQualifiers, pluRequestor, sqlLocale);
        }

        SQLSelectStatement sql = buildSelectPLUItemSQL(andQualifiers, pluRequestor, sqlLocale);

        // create a reference for the item
        ArrayList<PLUItemIfc> list = new ArrayList<PLUItemIfc>();

        try
        {
            // execute the query and get the result set
            ResultSet rs = execute(dataConnection, sql);

            while (rs.next())
            {
                PLUItemIfc pluItem = createPLUItem(rs);
                list.add(pluItem);
                logger.info("from GDYNPLUOperation");
                //(dataConnection, pluItem.getItemID());
            }

            JdbcSelectPriceChange selectPriceChange = new JdbcSelectPriceChange();
            for (Iterator<PLUItemIfc> iter = list.iterator(); iter.hasNext();)
            {
                PLUItemIfc pluItem = iter.next();

                // populate price changes
                if (pluRequestor == null || pluRequestor.containsRequestType(PLURequestor.RequestType.Price))
                {
                    PriceChangeIfc[] changes = selectPriceChange.readPermanentPriceChanges(dataConnection, pluItem,
                            Calendar.getInstance()); // this is not timezone safe
                    pluItem.setPermanentPriceChanges(changes);
                    changes = selectPriceChange.readAllTemporaryPriceChanges(dataConnection, pluItem,
                            Calendar.getInstance()); // this is not timezone safe
                    pluItem.setTemporaryPriceChangesAndTemporaryPriceChangesForReturns(changes);
                }

                // update gift card amounts
                if (pluItem instanceof GiftCardPLUItemIfc)
                {
                    GiftCardPLUItemIfc gci = (GiftCardPLUItemIfc) pluItem;
                    gci.getGiftCard().setReqestedAmount(pluItem.getSellingPrice());
                }

                if (pluRequestor == null || pluRequestor.containsRequestType(PLURequestor.RequestType.Planogram))
                {
                    applyPlanogramIDs(dataConnection, pluItem);
                }

                if (pluItem.getItemClassification().getItemType() == ItemClassificationConstantsIfc.TYPE_STOCK)
                {
                    if (pluRequestor == null || pluRequestor.containsRequestType(PLURequestor.RequestType.StockItem))
                    {
                        selectStockItem(dataConnection, pluItem.getItemID(), pluItem, sqlLocale);
                    }
                }
                if (pluRequestor == null
                        || pluRequestor.containsRequestType(PLURequestor.RequestType.LocalizedDescription))
                {
                    applyLocaleDependentDescriptions(dataConnection, pluItem, sqlLocale);
                }
                if (pluRequestor == null || pluRequestor.containsRequestType(PLURequestor.RequestType.ItemImage))
                {
                    applyItemImages(dataConnection, pluItem, sqlLocale);
                }

                // this method is called for regular item search from the sale screen
               // applyManufacturer(dataConnection, pluItem, andQualifiers, orQualifiers, sqlLocale);
                applyManufacturer(dataConnection, pluItem, andQualifiers, sqlLocale);

                // Set discount rules if item is eligible and rules have been
                // requested by the caller
                // 24OCT07 Even if item is non-discountable, it is allowed to be
                // a source in a rule, so always get the rules. CR29301
                if (pluRequestor == null
                        || pluRequestor.containsRequestType(PLURequestor.RequestType.AdvancedPricingRules)) // &&
                                                                                                            // (pluItem.isDiscountEligible()
                                                                                                            // ||
                // pluItem.getItem().isStoreCoupon()))
                {
                    applyAdvancedPricingRules(dataConnection, pluItem, sqlLocale);
                }

                // and components to the ItemKit
                if (pluItem.isKitHeader())
                {
                    if (pluRequestor == null
                            || pluRequestor.containsRequestType(PLURequestor.RequestType.KitComponents))
                    {
                        KitComponentIfc[] kitComps = selectKitComponents(dataConnection, pluItem.getItemID(), sqlLocale);
                        ((ItemKitIfc) pluItem).addComponentItems(kitComps);
                    }
                }

                // 27192
                if (pluRequestor == null || pluRequestor.containsRequestType(PLURequestor.RequestType.POSDepartment))
                {
                    getDepartmentByDeptID(dataConnection, pluItem, sqlLocale);
                }

                // Begin GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item
                // description
                // lcatania (Starmount) Mar 21, 2013
                if (pluRequestor == null || pluRequestor.containsRequestType(PLURequestor.RequestType.StockItem))
                {
                    getColorByColorCode(dataConnection, pluItem, sqlLocale);
                    getSizeBySizeCode(dataConnection, pluItem, sqlLocale);
                    getStyleByStyleCode(dataConnection, pluItem, sqlLocale);
                }
                // End GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item
                // description
            }
            
            
        }
        catch (DataException de)
        {
            logger.warn(de.toString());
            throw de;
        }
        catch (SQLException se)
        {
            dataConnection.logSQLException(se, "PLUItem lookup");
            throw new DataException(DataException.SQL_ERROR, "PLUItem lookup", se);
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "PLUItem lookup", e);
        }

        if (list.size() == 0)
        {
            throw new DataException(DataException.NO_DATA,
                    "No PLU was found processing the result set in GDYNJdbcPLUOperation.");
        }

        return list.toArray(new PLUItemIfc[list.size()]);
    }

    private ResultSet execute(JdbcDataConnection dataConnection, SQLSelectStatement[] sql)
    {
        // TODO Auto-generated method stub
        return null;
    }

    // Begin GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item description
    // lcatania (Starmount) Mar 22, 2013
    /**
     * Selects the color for the given PLUItem.
     * 
     * @param dataConnection
     *            a connection to the database
     * @param pluItem
     *            the PLU Item
     */
    public void getColorByColorCode(JdbcDataConnection dataConnection, PLUItemIfc pluItem,
            LocaleRequestor sqlLoclae)
    {
        if (logger.isDebugEnabled())
            logger.debug("GDYNJdbcPLUOperation.getColorByColorCode() begins");

        if (pluItem instanceof GDYNPLUItem)
        {
            JdbcReadItemColors readColor = new JdbcReadItemColors();
            GDYNPLUItemIfc gdynPluItem = (GDYNPLUItemIfc) pluItem;
            ItemColorIfc itemColor = gdynPluItem.getStockItem().getItemColor();
            try
            {
                readColor.readI8ItemColor(dataConnection, itemColor, sqlLoclae);
                gdynPluItem.getStockItem().setItemColor(itemColor);
            }
            catch (DataException de)
            {
                logger.error("Could not not read color for item: " + pluItem.getItemID() + " with color code: "
                        + itemColor.getIdentifier(), de);
            }
        }

        if (logger.isDebugEnabled())
            logger.debug("GDYNJdbcPLUOperation.getColorByColorCode() ends");
    }

    /**
     * Selects the size for the given PLUItem.
     * 
     * @param dataConnection
     *            a connection to the database
     * @param pluItem
     *            the PLU Item
     */
    public void getSizeBySizeCode(JdbcDataConnection dataConnection, PLUItemIfc pluItem,
            LocaleRequestor sqlLoclae)
    {
        if (logger.isDebugEnabled())
            logger.debug("GDYNJdbcPLUOperation.getSizeBySizeCode() begins");

        if (pluItem instanceof GDYNPLUItem)
        {
            JdbcReadItemSizes readSize = new JdbcReadItemSizes();
            GDYNPLUItemIfc gdynPluItem = (GDYNPLUItemIfc) pluItem;
            ItemSizeIfc itemSize = gdynPluItem.getStockItem().getItemSize();
            try
            {
                readSize.readI8ItemSize(dataConnection, itemSize, sqlLoclae);
                gdynPluItem.getStockItem().setItemSize(itemSize);
            }
            catch (DataException de)
            {
                logger.error("Could not not read size for item: " + pluItem.getItemID() + " with size code: "
                        + itemSize.getIdentifier(), de);
            }
        }

        if (logger.isDebugEnabled())
            logger.debug("GDYNJdbcPLUOperation.getSizeBySizeCode() ends");
    }

    /**
     * Selects the style for the given PLUItem.
     * 
     * @param dataConnection
     *            a connection to the database
     * @param pluItem
     *            the PLU Item
     */
    public void getStyleByStyleCode(JdbcDataConnection dataConnection, PLUItemIfc pluItem,
            LocaleRequestor sqlLoclae)
    {
        if (logger.isDebugEnabled())
            logger.debug("GDYNJdbcPLUOperation.getStyleByStyleCode() begins");

        if (pluItem instanceof GDYNPLUItem)
        {
            JdbcReadItemStyles readStyle = new JdbcReadItemStyles();
            GDYNPLUItemIfc gdynPluItem = (GDYNPLUItemIfc) pluItem;
            ItemStyleIfc itemStyle = gdynPluItem.getStockItem().getItemStyle();
            try
            {
                readStyle.readI8ItemStyle(dataConnection, itemStyle, sqlLoclae);
                gdynPluItem.getStockItem().setItemStyle(itemStyle);
            }
            catch (DataException de)
            {
                logger.error("Could not not read style for item: " + pluItem.getItemID() + " with style code: "
                        + itemStyle.getIdentifier(), de);
            }
        }

        if (logger.isDebugEnabled())
            logger.debug("GDYNJdbcPLUOperation.getStyleByStyleCode() ends");
    }
    
    public static  String readCpnAttrDtls1(JdbcDataConnection dataConnection,String cpnId)
            throws DataException
    {
    	
    	 logger.info("inside readCpnAttrDtls JdbcPLUOperation");
    	 System.out.println("inside readCpnAttrDtls JdbcPLUOperation");
    	
        // add tables
        SQLSelectStatement sql = new SQLSelectStatement();
        sql.addTable("CT_COUPON_ATTR");
        sql.addColumn("COUPON_ID");
        sql.addColumn("COUPON_TYPE");
        sql.addColumn("ITM_APPLY_TO");
        sql.addColumn("MIN_MO_TH");
        sql.addColumn("MAX_MO_TH");
        sql.addColumn("MAX_DISC_AMT");
        sql.addColumn("WS_VLDT_FLAG");
        sql.addColumn("TS_CRT_RCRD");
        sql.addColumn("TS_MDF_RCRD");
                
        logger.info( "from readCpnAttrDtls view couponId  "+cpnId);
        sql.addQualifier("COUPON_ID", cpnId);
        
        String divisionId= "";
        
        // perform the query
        try
        {
            String sqlToRun = sql.getSQLString();
            logger.info("sqlQuery for coupon To Run "+sqlToRun);
            dataConnection.execute(sqlToRun);
            ResultSet rs = (ResultSet) dataConnection.getResult();
            
            while (rs.next())
            {
            System.out.println("Details from readCpnAttrDtls  JdbcPLUOperation "+getSafeString(rs, 1)+" : "+getSafeString(rs, 2)+" : "+getSafeString(rs, 3)+ " : "+getSafeString(rs,4)); 
             
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

        //System.out.println("After getting resultset divisionId "+divisionId);
        logger.info("After getting resultset divisionId "+divisionId);
       return divisionId; 
      
    }

    
    // End GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item description
}
