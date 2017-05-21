//------------------------------------------------------------------------------
//
// Copyright (c) 2012, Starmount and Groupe Dynamite.
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

import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptExceptionCode;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptIdImage;
import com.gdyn.orpos.persistence.utility.GDYNARTSDatabaseIfc;

//------------------------------------------------------------------------------
/**
 * Data operation that reads tax exempt id image objects from the
 * database.
 * 
 * @author dteagle
 */
//------------------------------------------------------------------------------

public class GDYNJdbcReadTaxExemptIdImage extends JdbcDataOperation 
                                          implements GDYNARTSDatabaseIfc
{
    /** serial UID */
    private static final long serialVersionUID = -8451804261472871964L;
    
    protected static final Logger log = 
        Logger.getLogger(GDYNJdbcReadTaxExemptIdImage.class);
    
    //--------------------------------------------------------------------------
    /**
     * Executes this data operation and adds the result to the data 
     * transaction.
     * 
     * @param dataTransaction the current data transaction
     * @param dataConnection the database connection
     * @param action the operation to execute
     */
    public void execute(DataTransactionIfc dataTransaction,
                        DataConnectionIfc dataConnection,
                        DataActionIfc action)
                        throws DataException
    {
        if (log.isDebugEnabled()) log.debug("GDYNJdbcReadTaxExemptIdImage.execute()");
        
        JdbcDataConnection connection = (JdbcDataConnection)dataConnection;
        String operation = action.getDataOperationName();
        
        if(operation.equals("ReadTaxExemptIdImages"))
        {
            String countryCode = (String)action.getDataObject();
            dataTransaction.setResult(selectIdImages(connection, countryCode));
        }
        else if(operation.equals("ReadTaxExemptIdImageByName"))
        {
            GDYNTaxExemptSearchCriteriaIfc searchCriteria =
                (GDYNTaxExemptSearchCriteriaIfc)action.getDataObject();
            
            dataTransaction.setResult(selectIdImageByName(
                connection, searchCriteria.getIdImageName(), 
                searchCriteria.getCountryCode()));
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Selects a list of id images that match a country code.
     * @param countryCode
     * @return a serializable object
     * @throws DataException
     */
    @SuppressWarnings("unchecked")
    public Serializable selectIdImages(JdbcDataConnection dataConnection, 
                                       String countryCode) 
                                       throws DataException
    {
        SQLSelectStatement sql = buildSelectIdImages(countryCode);
        
        ResultSet rs = GDYNSqlHelper.doSQL(dataConnection, sql);
        List<GDYNTaxExemptIdImage> imageList = parseIdImageResults(rs);
        GDYNSqlHelper.cleanUp(rs);
        
        GDYNJdbcReadTaxExemptException exceptionOp = new GDYNJdbcReadTaxExemptException();

        for(GDYNTaxExemptIdImage idImage : imageList)
        {       
            List<GDYNTaxExemptExceptionCode> exceptions =
                (List<GDYNTaxExemptExceptionCode>)exceptionOp
                    .selectExceptionsByIdImage(dataConnection, 
                                               idImage.getTaxIdImageName(), 
                                               countryCode);

            idImage.setExceptionCodes(exceptions);
        }
        return (Serializable)imageList;
    }
    
    //--------------------------------------------------------------------------
    /**
     * @param dataConnection
     * @param idImageName
     * @param countryCode
     * @return
     * @throws DataException
     */
    @SuppressWarnings("unchecked")
    public Serializable selectIdImageByName(JdbcDataConnection dataConnection, 
                                            String idImageName, 
                                            String countryCode) 
                                            throws DataException
    {
        SQLSelectStatement sql = buildSelectIdImages(countryCode);
        
        sql.addQualifier(ALIAS_TAX_EXEMPT_CUST_ID_IMAGE, 
                         FIELD_TAX_ID_IMAGE_NAME, makeSafeString(idImageName));
        
        ResultSet rs = GDYNSqlHelper.doSQL(dataConnection, sql);
        List<GDYNTaxExemptIdImage> imageList = parseIdImageResults(rs);
        GDYNSqlHelper.cleanUp(rs);
        
        if(imageList != null && imageList.size() == 1)
        {
            GDYNTaxExemptIdImage idImage = imageList.get(0);
            GDYNJdbcReadTaxExemptException exceptionOp = new GDYNJdbcReadTaxExemptException();
     
            List<GDYNTaxExemptExceptionCode> exceptions =
                (List<GDYNTaxExemptExceptionCode>)exceptionOp
                    .selectExceptionsByIdImage(dataConnection, idImageName, countryCode);

            idImage.setExceptionCodes(exceptions);
            return (Serializable)idImage;
        }
        return null;
    }
    
    //--------------------------------------------------------------------------
    /**
     * Creates a SQL select statement for reading id image codes.
     * @param countryCode
     * @return a SQL select statement
     */
    public SQLSelectStatement buildSelectIdImages(String countryCode)
    {
        SQLSelectStatement sql = new SQLSelectStatement();
        sql.addTable(TABLE_TAX_EXEMPT_CUST_ID_IMAGE, ALIAS_TAX_EXEMPT_CUST_ID_IMAGE);
        
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_ID_IMAGE, FIELD_TAX_ID_IMAGE_NAME);
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_ID_IMAGE, FIELD_COUNTRY_CODE);
        sql.addColumn(ALIAS_TAX_EXEMPT_CUST_ID_IMAGE, FIELD_EFFECTIVE_DATE);
        
        sql.addQualifier(ALIAS_TAX_EXEMPT_CUST_ID_IMAGE, FIELD_COUNTRY_CODE, makeSafeString(countryCode));
        
        return sql;
    }
    
    //--------------------------------------------------------------------------
    /**
     * Parses a SQL result set into a list of id image objects.
     * @param rs the result set
     * @return a list of id image objects
     * @throws SQLException
     */
    protected List<GDYNTaxExemptIdImage> parseIdImageResults(ResultSet rs)
    throws DataException
    {
        List<GDYNTaxExemptIdImage> idImageList = 
                new ArrayList<GDYNTaxExemptIdImage>();
        
        GDYNTaxExemptIdImage idImage = null;
        
        try
        {
            while (rs.next())
            {
                idImage = new GDYNTaxExemptIdImage();
               
                int index = 0;
                idImage.setTaxIdImageName(getSafeString(rs, ++index));
                idImage.setCountryCode(getSafeString(rs, ++index));
                idImage.setEffectiveDate(get_360DateFromString(rs, ++index));
                
                idImageList.add(idImage);
            }
        }
        catch (SQLException e)
        {
            log.warn(e.toString());
            throw new DataException(DataException.SQL_ERROR, 
                        "GDYNJdbcReadTaxExemptIdImage.parseIdImageResults()", e);
        }      
        return idImageList;
    }
}
