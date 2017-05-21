//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.arts;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import oracle.retail.stores.common.sql.SQLSelectStatement;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;


//------------------------------------------------------------------------------
/**
 * Useful SQL methods.
 * @author dteagle
 */
//------------------------------------------------------------------------------

public class GDYNSqlHelper
{
    private static final Logger log = Logger.getLogger(GDYNSqlHelper.class);
    
    //--------------------------------------------------------------------------
    /**
     * @param dataConnection
     * @param sql
     * @return
     * @throws DataException
     */
    public static ResultSet doSQL(JdbcDataConnection dataConnection, 
                                  SQLSelectStatement sql)
                                  throws DataException
    {
        String sqlString = sql.getSQLString();
        dataConnection.execute(sqlString, sql.getParameterValues());
        ResultSet rs = (ResultSet)dataConnection.getResult();
        return rs;
    }
    
    //--------------------------------------------------------------------------
    /**
     * @param rs
     * @throws DataException
     */
    public static void cleanUp(ResultSet rs) throws DataException
    {
        try
        {
            rs.close();
        }
        catch (SQLException se)
        {
            log.warn(se.toString());
            throw new DataException(DataException.SQL_ERROR, 
                                    "GDYNSqlHelper.cleanup()", se);
        }
        catch (Exception e)
        {
            log.warn(e.toString());
            throw new DataException(DataException.UNKNOWN, 
                                    "GDYNSqlHelper.cleanup()", e);
        }
    }
}
