package com.gdyn.orpos.domain.arts;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;

import oracle.retail.stores.common.sql.SQLSelectStatement;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.arts.JdbcDataOperation;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataConnectionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataTransactionIfc;

import org.apache.log4j.Logger;

import com.gdyn.orpos.domain.factory.GDYNDomainObjectFactory;
import com.gdyn.orpos.domain.tax.GDYNTaxAuthorityIfc;
import com.gdyn.orpos.persistence.utility.GDYNARTSDatabaseIfc;

public class GDYNJdbcReadTaxAuthority extends JdbcDataOperation implements GDYNARTSDatabaseIfc 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -6441970877313456456L;
	protected static final Logger logger = Logger.getLogger(GDYNJdbcReadTaxAuthority.class);

    // --------------------------------------------------------------------------
    /**
     *
     */
    public void execute(DataTransactionIfc dataTransaction, DataConnectionIfc dataConnection, DataActionIfc action) throws DataException
    {
    	JdbcDataConnection connection = (JdbcDataConnection) dataConnection;
        Set<Integer> idSet = (Set<Integer>) action.getDataObject();
        HashMap<Integer, GDYNTaxAuthorityIfc> authorityMap = this.getTaxAuthority(idSet, connection);
        dataTransaction.setResult(authorityMap);
    }
    
    protected HashMap<Integer, GDYNTaxAuthorityIfc> getTaxAuthority(Set<Integer> idSet, JdbcDataConnection connection) throws DataException
    {
    	SQLSelectStatement sql = new SQLSelectStatement();
    	sql.setTable(TABLE_TAX_AUTHORITY);
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append('(');
   		for (Integer id : idSet) {
   			sb.append(id).append(", ");
   		}
   		sb.deleteCharAt(sb.length() - 1);
   		sb.deleteCharAt(sb.length() - 1);
   		sb.append(')');
    	sql.addQualifier(FIELD_TAX_AUTHORITY_ID + " IN " + sb.toString());
    	
    	sql.addColumn(FIELD_TAX_AUTHORITY_ID);
    	sql.addColumn(FIELD_PARTY_ROLE_TYPE_CODE);
    	sql.addColumn(FIELD_TAX_AUTHORITY_NAME);
    	
    	String sqlString = sql.getSQLString();
    	
    	GDYNDomainObjectFactory factory = (GDYNDomainObjectFactory) DomainGateway.getFactory();
    	HashMap<Integer, GDYNTaxAuthorityIfc> authorityMap = new HashMap<Integer, GDYNTaxAuthorityIfc>(idSet.size());
    	
    	connection.execute(sqlString);
    	
    	ResultSet rs = (ResultSet) connection.getResult();
    	try {
			while (rs.next()) {
				GDYNTaxAuthorityIfc authority = factory.getTaxAuthorityInformationInstance();
				
				int idx = 0;
				
				int id = rs.getInt(++idx);
				String jurisdiction = rs.getString(++idx);
				String name = rs.getString(++idx);
				
				authority.setId(id);
				authority.setJurisdictionTypeCode(jurisdiction);
				authority.setName(name);
				
				authorityMap.put(id, authority);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return authorityMap;
    }
    
}