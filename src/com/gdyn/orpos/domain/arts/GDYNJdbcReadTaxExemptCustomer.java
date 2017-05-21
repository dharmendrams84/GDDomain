//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.arts;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import oracle.retail.stores.common.sql.SQLParameterIfc;
import oracle.retail.stores.common.sql.SQLParameterValue;
import oracle.retail.stores.common.sql.SQLSelectStatement;
import oracle.retail.stores.common.utility.LocaleMap;
import oracle.retail.stores.common.utility.LocaleUtilities;
import oracle.retail.stores.common.utility.LocalizedTextIfc;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.arts.JdbcDataOperation;
import oracle.retail.stores.domain.utility.LocaleConstantsIfc;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataConnectionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataTransactionIfc;

import org.apache.log4j.Logger;

import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptCustomerCategory;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptCustomerCode;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptExceptionCode;
import com.gdyn.orpos.persistence.utility.GDYNARTSDatabaseIfc;

//------------------------------------------------------------------------------
/**
 * Executes database calls for reading tax exempt customer categories
 * and codes.
 * 
 * @author dteagle
 */
// ------------------------------------------------------------------------------

public class GDYNJdbcReadTaxExemptCustomer extends JdbcDataOperation
        implements GDYNARTSDatabaseIfc
{
    /** serial UID */
    private static final long serialVersionUID = -7929687915352990463L;

    protected static final Logger log =
            Logger.getLogger(GDYNJdbcReadTaxExemptCustomer.class);

    // --------------------------------------------------------------------------
    /**
     *
     */
    public void execute(DataTransactionIfc dataTransaction,
            DataConnectionIfc dataConnection,
            DataActionIfc action)
            throws DataException
    {
        if (log.isDebugEnabled())
            log.debug("GDYNJdbcReadTaxExemptCustomer.execute()");

        JdbcDataConnection connection = (JdbcDataConnection) dataConnection;
        String operation = action.getDataOperationName();

        GDYNTaxExemptSearchCriteriaIfc criteria =
                (GDYNTaxExemptSearchCriteriaIfc) action.getDataObject();

        if (operation.equals("ReadTaxExemptCustomerCategories"))
        {
            dataTransaction.setResult(
                    selectCustomerCategories(connection,
                            criteria.getCountryCode(),
                            criteria.getTaxAreaCode(),
                            criteria.getLocale(),
                            criteria.isReadFully()));
        }
        else if (operation.equals("ReadTaxExemptCategoryByCode"))
        {
            try
            {
                dataTransaction.setResult(
                        selectCategoryByCode(connection,
                                criteria.getCategoryCode(),
                                criteria.getCountryCode(),
                                criteria.getTaxAreaCode(),
                                criteria.getLocale(),
                                criteria.isReadFully()));
            }
            catch (SQLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if (operation.equals("ReadTaxExemptCustomerCodes"))
        {

        }
        else if (operation.equals("ReadTaxExemptCustomerCodeForCategory"))
        {
            dataTransaction.setResult(
                    selectCustomerCodeByCategory(connection,
                            criteria.getCategoryCode(),
                            criteria.getCountryCode(),
                            criteria.getTaxAreaCode(),
                            criteria.isReadFully()));
        }
    }

    // --------------------------------------------------------------------------
    /**
     * @param dataConnection
     * @param countryCode
     * @param taxArea
     * @param locale
     * @param populate
     * @return
     * @throws DataException
     */
    public Serializable selectCustomerCategories(JdbcDataConnection dataConnection,
            String countryCode, String taxArea,
            Locale locale, boolean populate)
            throws DataException
    {
        SQLSelectStatement sql = buildSelectCustomerCategories(countryCode, taxArea, locale);

        ResultSet rs = GDYNSqlHelper.doSQL(dataConnection, sql);
        List<GDYNTaxExemptCustomerCategory> categoryList = parseCategoryResults(rs);
        GDYNSqlHelper.cleanUp(rs);

        if (categoryList != null && !categoryList.isEmpty())
        {
            GDYNTaxExemptCustomerCode code = null;

            for (GDYNTaxExemptCustomerCategory category : categoryList)
            {
                readCategoryInternationalizedValues(dataConnection, category);

                if (populate)
                {
                    code = (GDYNTaxExemptCustomerCode) selectCustomerCodeByCategory(
                            dataConnection, category.getCategoryCode(),
                            category.getCountryCode(), category.getTaxAreaCode(),
                            populate);

                    category.setCustomerCode(code);
                }
            }
        }
        return (Serializable) categoryList;
    }

    // --------------------------------------------------------------------------
    /**
     * @param dataConnection
     * @param categoryCode
     * @param countryCode
     * @param taxArea
     * @param locale
     * @param populate
     * @return
     * @throws DataException
     * @throws SQLException 
     */
    public Serializable selectCategoryByCode(JdbcDataConnection dataConnection,
            String categoryCode, String countryCode,
            String taxArea, Locale locale, boolean populate)
            throws DataException, SQLException
    {
        SQLSelectStatement sql = buildSelectCustomerCategories(countryCode, taxArea, locale);

        sql.addQualifier(ALIAS_TAX_EXEMPT_CUST_CATEGORY,
                FIELD_CATEGORY_CODE, makeSafeString(categoryCode));
        
        String sqlstring = sql.getSQLString();
        PreparedStatement prepareSql =  dataConnection.getConnection().prepareCall(sqlstring);
        prepareSql.setString(1, (String) sql.getParameterValues().get(0));
        prepareSql.setString(2, (String) sql.getParameterValues().get(1));
        ResultSet rs = prepareSql.executeQuery();
        //dataConnection.execute(sql.getSQLString(), sql.getParameterValues());
        //ResultSet rs = (ResultSet)dataConnection.getResult();
        
        //ResultSet rs = GDYNSqlHelper.doSQL(dataConnection, sql);
        List<GDYNTaxExemptCustomerCategory> categoryList = parseCategoryResults(rs);
        GDYNSqlHelper.cleanUp(rs);

        if (categoryList != null && categoryList.size() == 1)
        {
            GDYNTaxExemptCustomerCategory category = categoryList.get(0);

            readCategoryInternationalizedValues(dataConnection, category);

            if (populate)
            {
                GDYNTaxExemptCustomerCode code =
                        (GDYNTaxExemptCustomerCode) selectCustomerCodeByCategory(
                                dataConnection, category.getCategoryCode(),
                                category.getCountryCode(), category.getTaxAreaCode(), populate);

                category.setCustomerCode(code);
            }
            return (Serializable) category;
        }
        return null;
    }

    // --------------------------------------------------------------------------
    /**
     * @param dataConnection
     * @param category
     * @throws DataException
     */
    public void readCategoryInternationalizedValues(JdbcDataConnection dataConnection,
            GDYNTaxExemptCustomerCategory category)
            throws DataException
    {
        SQLSelectStatement sql = buildSelectCategoryI18N(category.getCategoryMessageId());

        ResultSet rs = GDYNSqlHelper.doSQL(dataConnection, sql);
        parseCategoryI18NResults(rs, category);
        GDYNSqlHelper.cleanUp(rs);
    }

    // --------------------------------------------------------------------------
    /**
     * @param dataConnection
     * @param countryCode
     * @param taxArea
     * @return
     * @throws DataException
     */
    public Serializable selectCustomerCodes(JdbcDataConnection dataConnection,
            String countryCode, String taxArea)
            throws DataException
    {
        // TODO: not implemented yet. May remove later if not needed.
        return null;
    }

    // --------------------------------------------------------------------------
    /**
     * @param dataConnection
     * @param categoryCode
     * @param countryCode
     * @param taxAreaCode
     * @param populate
     * @return
     * @throws DataException
     */
    @SuppressWarnings("unchecked")
    public Serializable selectCustomerCodeByCategory(JdbcDataConnection dataConnection,
            String categoryCode,
            String countryCode,
            String taxAreaCode,
            boolean populate)
            throws DataException
    {
        SQLSelectStatement sql = buildSelectCustomerCode(categoryCode,
                countryCode,
                taxAreaCode);

        ResultSet rs = GDYNSqlHelper.doSQL(dataConnection, sql);
        GDYNTaxExemptCustomerCode customerCode = parseCustomerCodeResults(rs);
        GDYNSqlHelper.cleanUp(rs);

        if (populate)
        {
            GDYNJdbcReadTaxExemptException exceptionOp = new GDYNJdbcReadTaxExemptException();

            List<GDYNTaxExemptExceptionCode> exceptions =
                    (List<GDYNTaxExemptExceptionCode>) exceptionOp
                            .selectExceptionsByCategory(dataConnection, countryCode,
                                    taxAreaCode, categoryCode);

            customerCode.setExceptionCodes(exceptions);
        }
        return (Serializable) customerCode;
    }

    // --------------------------------------------------------------------------
    /**
     * @param country
     * @param locale
     * @return
     */
    public SQLSelectStatement buildSelectCustomerCategories(String countryCode,
            String taxArea,
            Locale locale)
    {
        SQLSelectStatement sql = new SQLSelectStatement();
        sql.addTable(TABLE_TAX_EXEMPT_CUST_CATEGORY, ALIAS_TAX_EXEMPT_CUST_CATEGORY);

        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_CATEGORY, FIELD_CATEGORY_CODE);
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_CATEGORY, FIELD_COUNTRY_CODE);
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_CATEGORY, FIELD_TAX_AREA_CODE);
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_CATEGORY, FIELD_CATEGORY_MSG_ID);
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_CATEGORY, FIELD_ACTIVE_FLAG);
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_CATEGORY, FIELD_EXPIRE_DATE_REQ_FLAG);
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_CATEGORY, FIELD_BAND_REGISTRY_REQ_FLAG);
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_CATEGORY, FIELD_TAX_ID_IMAGE_REQ_FLAG);

        sql.addQualifier(ALIAS_TAX_EXEMPT_CUST_CATEGORY, FIELD_ACTIVE_FLAG, makeStringFromBoolean(true));
        sql.addQualifier(ALIAS_TAX_EXEMPT_CUST_CATEGORY, FIELD_COUNTRY_CODE, makeSafeString(countryCode));
        
        //search by ItemID OR PosItemID
        List<SQLParameterIfc> orQualifiers = new ArrayList<SQLParameterIfc>(2);
        orQualifiers.add(new SQLParameterValue(ALIAS_TAX_EXEMPT_CUST_CATEGORY, FIELD_TAX_AREA_CODE, taxArea));
        orQualifiers.add(new SQLParameterValue(ALIAS_TAX_EXEMPT_CUST_CATEGORY, FIELD_TAX_AREA_CODE, TAX_PRODUCT_AREA_ALL));

        sql.addOrQualifiers(orQualifiers);
        
        return sql;
    }

    // --------------------------------------------------------------------------
    /**
     * @param messageId
     * @return
     */
    public SQLSelectStatement buildSelectCategoryI18N(int messageId)
    {
        SQLSelectStatement sql = new SQLSelectStatement();

        sql.addTable(TABLE_TAX_EXEMPT_CUST_CATEGORY_I18N, ALIAS_TAX_EXEMPT_CUST_CATEGORY_I18N);

        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_CATEGORY_I18N, FIELD_CATEGORY_MSG_ID);
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_CATEGORY_I18N, FIELD_CUST_CATEGORY_LOCALE);
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_CATEGORY_I18N, FIELD_COUNTRY_CODE);
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_CATEGORY_I18N, FIELD_CATEGORY_NAME);
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_CATEGORY_I18N, FIELD_RECEIPT_MSG);

        sql.addQualifier(ALIAS_TAX_EXEMPT_CUST_CATEGORY_I18N, FIELD_CATEGORY_MSG_ID, messageId);

        return sql;
    }

    // --------------------------------------------------------------------------
    /**
     * @param code
     * @param country
     * @param taxArea
     * @return
     */
    public SQLSelectStatement buildSelectCustomerCode(String code, String country, String taxArea)
    {
        SQLSelectStatement sql = new SQLSelectStatement();
        sql.addTable(TABLE_TAX_EXEMPT_CUST_CODE, ALIAS_TAX_EXEMPT_CUST_CODE);
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_CODE, FIELD_CATEGORY_CODE);
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_CODE, FIELD_COUNTRY_CODE);
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_CODE, FIELD_TAX_AREA_CODE);
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_CODE, FIELD_EFFECTIVE_DATE);
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_CODE, FIELD_APPLICATION_METHOD);
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_CODE, FIELD_PARTIAL_RATE);

        sql.addQualifier(ALIAS_TAX_EXEMPT_CUST_CODE, FIELD_CATEGORY_CODE, makeSafeString(code));
        sql.addQualifier(ALIAS_TAX_EXEMPT_CUST_CODE, FIELD_COUNTRY_CODE, makeSafeString(country));
        sql.addQualifier(ALIAS_TAX_EXEMPT_CUST_CODE, FIELD_TAX_AREA_CODE, makeSafeString(taxArea));

        return sql;
    }

    // --------------------------------------------------------------------------
    /**
     * @param rs
     * @return
     * @throws SQLException
     */
    protected List<GDYNTaxExemptCustomerCategory> parseCategoryResults(ResultSet rs)
            throws DataException
    {
        List<GDYNTaxExemptCustomerCategory> categoryList =
                new ArrayList<GDYNTaxExemptCustomerCategory>();

        GDYNTaxExemptCustomerCategory category = null;

        try
        {
            while (rs.next())
            {
                category = new GDYNTaxExemptCustomerCategory();

                int index = 0;
                category.setCategoryCode(getSafeString(rs, ++index));
                category.setCountryCode(getSafeString(rs, ++index));
                category.setTaxAreaCode(getSafeString(rs, ++index));
                category.setCategoryMessageId(rs.getInt(++index));
                category.setActive(getBooleanFromString(rs, ++index));
                category.setExpirationDateRequired(getBooleanFromString(rs, ++index));
                category.setBandRegistryRequired(getBooleanFromString(rs, ++index));
                category.setTaxIdImageRequired(getBooleanFromString(rs, ++index));

                categoryList.add(category);
            }
        }
        catch (SQLException e)
        {
            log.warn(e.toString());
            throw new DataException(DataException.SQL_ERROR,
                    "GDYNJdbcReadTaxExempt.parseCategoryResults()", e);
        }
        return categoryList;
    }

    // --------------------------------------------------------------------------
    /**
     * @param rs
     * @param category
     * @return
     * @throws DataException
     */
    protected void parseCategoryI18NResults(ResultSet rs,
            GDYNTaxExemptCustomerCategory category)
            throws DataException
    {
        LocalizedTextIfc nameText = DomainGateway.getFactory().getLocalizedText();
        LocalizedTextIfc receiptText = DomainGateway.getFactory().getLocalizedText();
        nameText.setDefaultLocale(LocaleMap.getLocale(LocaleConstantsIfc.RECEIPT));
        receiptText.setDefaultLocale(LocaleMap.getLocale(LocaleConstantsIfc.RECEIPT));

        try
        {
            while (rs.next())
            {
                String localeString = getSafeString(rs, 2);
                String countryCode = getSafeString(rs, 3);
                String nameValue = getSafeString(rs, 4);
                String receiptValue = getSafeString(rs, 5);

                String localeCountry = localeString + "_" + countryCode;
                Locale locale = LocaleUtilities.getLocaleFromString(localeCountry);

                nameText.putText(locale, nameValue);
                receiptText.putText(locale, receiptValue);
            }
            category.setCategoryName(nameText);
            category.setReceiptMessage(receiptText);
        }
        catch (SQLException e)
        {
            log.warn(e.toString());
            throw new DataException(DataException.SQL_ERROR,
                    "GDYNJdbcReadTaxExempt.parseCategoryI18NResults()", e);
        }
    }

    // --------------------------------------------------------------------------
    /**
     * @param rs
     * @return
     * @throws DataException
     */
    protected GDYNTaxExemptCustomerCode parseCustomerCodeResults(ResultSet rs)
            throws DataException
    {
        GDYNTaxExemptCustomerCode code = null;

        try
        {

            while (rs.next())
            {

                int index = 0;
                code = new GDYNTaxExemptCustomerCode();

                code.setCategoryCode(getSafeString(rs, ++index));
                code.setCountryCode(getSafeString(rs, ++index));
                code.setTaxAreaCode(getSafeString(rs, ++index));
                code.setEffectiveDate(get_360DateFromString(rs, ++index));
                code.setApplicationMethod(getSafeString(rs, ++index));
                code.setPartialTaxRate(rs.getBigDecimal(++index));
            }
        }
        catch (SQLException e)
        {
            log.warn(e.toString());
            throw new DataException(DataException.SQL_ERROR,
                    "GDYNJdbcReadTaxExempt.parseCustomerCodeResults()", e);
        }

        return code;
    }
}
