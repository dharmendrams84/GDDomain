//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.arts;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import oracle.retail.stores.common.sql.SQLSelectStatement;
import oracle.retail.stores.domain.arts.JdbcDataOperation;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataConnectionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataTransactionIfc;

import org.apache.log4j.Logger;

import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptCustomerCode;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptExceptionCode;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptIdImage;
import com.gdyn.orpos.persistence.utility.GDYNARTSDatabaseIfc;

//------------------------------------------------------------------------------
/**
 * Executes database calls for reading tax exempt customer code 
 * exceptions.
 * 
 * @author dteagle
 */
//------------------------------------------------------------------------------

public class GDYNJdbcReadTaxExemptException extends JdbcDataOperation 
                                           implements GDYNARTSDatabaseIfc
{
    /** serial UID */
    private static final long serialVersionUID = -5079140464121202257L;
    
    protected static final Logger log = 
        Logger.getLogger(GDYNJdbcReadTaxExemptException.class);
    
    //--------------------------------------------------------------------------
    /**
     * Executes the data operation.
     */
    public void execute(DataTransactionIfc dataTransaction,
                        DataConnectionIfc dataConnection,
                        DataActionIfc action)
                        throws DataException
    {
        if (log.isDebugEnabled()) log.debug("GDYNJdbcReadTaxExemptException.execute()");
        
        JdbcDataConnection connection = (JdbcDataConnection)dataConnection;
        String operation = action.getDataOperationName();
        
        if(operation.equals("ReadTaxExemptExceptionCodesForCustomerCode"))
        {
            GDYNTaxExemptCustomerCode customerCode = 
                (GDYNTaxExemptCustomerCode)action.getDataObject();
            
            dataTransaction.setResult(
                selectExceptionsByCategory(connection,
                                           customerCode.getCountryCode(),
                                           customerCode.getTaxAreaCode(),
                                           customerCode.getCategoryCode()));
        }
        else if(operation.equals("ReadTaxExemptExceptionCodesForImageName"))
        {
            GDYNTaxExemptIdImage idImage = 
                (GDYNTaxExemptIdImage)action.getDataObject();
            
            dataTransaction.setResult(
                selectExceptionsByIdImage(connection,
                                          idImage.getTaxIdImageName(),
                                          idImage.getCountryCode()));
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Selects a list of exceptions that match a customer category code.
     * @param dataConnection
     * @param countryCode
     * @param taxArea
     * @param categoryCode
     * @return
     * @throws DataException
     */
    public Serializable selectExceptionsByCategory(JdbcDataConnection dataConnection, 
                                                   String countryCode, 
                                                   String taxArea,
                                                   String categoryCode) 
                                                   throws DataException
    {
        SQLSelectStatement sql = buildSelectExceptions();
        
        sql.addQualifier(ALIAS_TAX_EXEMPT_EXCEPTION_CODE, FIELD_CATEGORY_CODE, makeSafeString(categoryCode));
        sql.addQualifier(ALIAS_TAX_EXEMPT_EXCEPTION_CODE, FIELD_COUNTRY_CODE, makeSafeString(countryCode));
        sql.addQualifier(ALIAS_TAX_EXEMPT_EXCEPTION_CODE, FIELD_TAX_AREA_CODE, makeSafeString(taxArea));
        
        ResultSet rs = GDYNSqlHelper.doSQL(dataConnection, sql);
        List<GDYNTaxExemptExceptionCode> exceptionList = parseExceptionResults(rs);
        GDYNSqlHelper.cleanUp(rs);
        
        return (Serializable)exceptionList;
    }

    //--------------------------------------------------------------------------
    /**
     * Selects a list of exceptions that match an id image name.
     * @param dataConnection
     * @param imageName
     * @param countryCode
     * @return
     * @throws DataException
     */
    public Serializable selectExceptionsByIdImage(JdbcDataConnection dataConnection, 
                                                  String imageName,
                                                  String countryCode) 
                                                  throws DataException
    {
        SQLSelectStatement sql = buildSelectExceptions();
        
        sql.addQualifier(ALIAS_TAX_EXEMPT_EXCEPTION_CODE, FIELD_IMAGE_CODE, makeSafeString(imageName));
        sql.addQualifier(ALIAS_TAX_EXEMPT_EXCEPTION_CODE, FIELD_COUNTRY_CODE, makeSafeString(countryCode));
        
        ResultSet rs = GDYNSqlHelper.doSQL(dataConnection, sql);
        List<GDYNTaxExemptExceptionCode> exceptionList = parseExceptionResults(rs);
        GDYNSqlHelper.cleanUp(rs);
        
        return (Serializable)exceptionList;
    }
    
    //--------------------------------------------------------------------------
    /**
     * Creates the base select statement for exceptions.
     * @return a sql select statement
     */
    public SQLSelectStatement buildSelectExceptions()
    {
        SQLSelectStatement sql = new SQLSelectStatement();
        sql.addTable(TABLE_TAX_EXEMPT_EXCEPTION_CODE, ALIAS_TAX_EXEMPT_EXCEPTION_CODE);
        
        sql.addColumn(ALIAS_TAX_EXEMPT_EXCEPTION_CODE, FIELD_COUNTRY_CODE);
        sql.addColumn(ALIAS_TAX_EXEMPT_EXCEPTION_CODE, FIELD_TAX_AREA_CODE);
        sql.addColumn(ALIAS_TAX_EXEMPT_EXCEPTION_CODE, FIELD_CATEGORY_CODE);
        sql.addColumn(ALIAS_TAX_EXEMPT_EXCEPTION_CODE, FIELD_IMAGE_CODE);
        sql.addColumn(ALIAS_TAX_EXEMPT_EXCEPTION_CODE, FIELD_TAX_PRODUCT_CODE);
        sql.addColumn(ALIAS_TAX_EXEMPT_EXCEPTION_CODE, FIELD_EFFECTIVE_DATE);
        
        return sql;
    }
    
    //--------------------------------------------------------------------------
    /**
     * @param rs
     * @return
     * @throws SQLException
     */
    protected List<GDYNTaxExemptExceptionCode> parseExceptionResults(ResultSet rs)
    throws DataException
    {
        List<GDYNTaxExemptExceptionCode> exceptionList = 
                new ArrayList<GDYNTaxExemptExceptionCode>();
        
        GDYNTaxExemptExceptionCode exception = null;
        
        try
        {
            while (rs.next())
            {
                exception = new GDYNTaxExemptExceptionCode();
               
                int index = 0;
                exception.setCountryCode(getSafeString(rs, ++index));
                exception.setTaxAreaCode(getSafeString(rs, ++index));
                exception.setCategoryCode(getSafeString(rs, ++index));
                exception.setImageCode(getSafeString(rs, ++index));
                exception.setTaxProductCode(rs.getInt(++index));
                exception.setEffectiveDate(get_360DateFromString(rs, ++index));
                
                exceptionList.add(exception);
            }
        }
        catch (SQLException e)
        {
            log.warn(e.toString());
            throw new DataException(DataException.SQL_ERROR, 
                        "GDYNJdbcReadTaxExemptException.parseExceptionResults()", e);
        }      
        return exceptionList;
    }
}
