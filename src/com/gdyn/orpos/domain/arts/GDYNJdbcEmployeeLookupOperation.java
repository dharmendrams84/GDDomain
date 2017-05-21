package com.gdyn.orpos.domain.arts;

import java.sql.ResultSet;
import java.sql.SQLException;

import oracle.retail.stores.common.sql.SQLSelectStatement;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.arts.JdbcDataOperation;
import oracle.retail.stores.domain.employee.EmployeeIfc;
import oracle.retail.stores.domain.employee.EmployeeTypeEnum;
import oracle.retail.stores.domain.utility.LocaleUtilities;
import oracle.retail.stores.domain.utility.PersonNameIfc;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataConnectionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataOperationIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataTransactionIfc;
import oracle.retail.stores.foundation.utility.Util;
import oracle.retail.stores.persistence.utility.ARTSDatabaseIfc;

import org.apache.log4j.Logger;

public class GDYNJdbcEmployeeLookupOperation extends JdbcDataOperation implements DataOperationIfc, ARTSDatabaseIfc
{
	Logger logger = Logger.getLogger(GDYNJdbcEmployeeLookupOperation.class);

	public void execute(DataTransactionIfc transaction, DataConnectionIfc dataConnection, DataActionIfc dataAction) throws DataException 
	{
		EmployeeIfc employee = null;
		String employeeID = (String) dataAction.getDataObject();
		
		employee = readEmployee(dataConnection, employeeID);
		
		transaction.setResult(employee);
	}
	
	protected EmployeeIfc readEmployee(DataConnectionIfc connection, String employeeID) throws DataException
	{
		SQLSelectStatement sql = new SQLSelectStatement();
		
		sql.setTable(TABLE_EMPLOYEE, ALIAS_EMPLOYEE);
		
		sql.addQualifier(FIELD_EMPLOYEE_ID, inQuotes(employeeID));
		sql.addQualifier(FIELD_EMPLOYEE_STATUS_CODE, inQuotes(Integer.toString(EmployeeIfc.LOGIN_STATUS_ACTIVE)));
		
        /*
         * Add columns and their values
         */
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_ID);
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_ID_ALT);
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_ID_LOGIN);
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_LAST_NAME);
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_FIRST_NAME);
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_MIDDLE_NAME);
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_NAME);
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_STATUS_CODE);
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_LOCALE);
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_TYPE);
        sql.addColumn(ALIAS_EMPLOYEE + "." + FIELD_EMPLOYEE_STORE_ID);

		
		EmployeeIfc employee = null;
		
        try {
            connection.execute(sql.getSQLString(), false);
            ResultSet rs = (ResultSet) connection.getResult();
            
            if (rs.next()) {
            	employee = DomainGateway.getFactory().getEmployeeInstance();
        		PersonNameIfc employeeName = DomainGateway.getFactory().getPersonNameInstance();
        		
                employee.setEmployeeID(getSafeString(rs, 1));
                employee.setAlternateID(getSafeString(rs, 2));
                employee.setLoginID(getSafeString(rs, 3));
                
                String lastName = getSafeString(rs, 4);
                String firstName = getSafeString(rs, 5);
                employeeName.setLastName(lastName);
                employeeName.setFirstName(firstName);
                employeeName.setMiddleName(getSafeString(rs, 6));
                String fullName = getSafeString(rs, 7);
                if (Util.isEmpty(fullName) && (!Util.isEmpty(firstName) && !Util.isEmpty(lastName))) {
                	fullName = firstName + ' ' + lastName;
                }
                employeeName.setFullName(fullName);
                employee.setPersonName(employeeName);
                
                employee.setLoginStatus(Integer.parseInt(getSafeString(rs, 8)));
                
                String language = getSafeString(rs, 9);
                if (!Util.isEmpty(language)) {
                    try {
                        employee.setPreferredLocale(LocaleUtilities.getLocaleFromString(language));
                    } catch (IllegalArgumentException e) {
                        logger.warn("JdbcEmployeeLookupOperation.execute(): Employee preferredLocale is not valid");
                    }
                }
                
                employee.setType(EmployeeTypeEnum.getEnumForDBVal(rs.getInt(10)));
                employee.setStoreID(getSafeString(rs, 11));
            }
            
        } catch (SQLException e) {
        	logger.warn("Unable to read employee: ", e);
        }
		
		return employee;
	}

}
