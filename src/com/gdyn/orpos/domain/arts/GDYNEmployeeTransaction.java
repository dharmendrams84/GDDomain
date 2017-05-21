package com.gdyn.orpos.domain.arts;

import java.io.Serializable;

import oracle.retail.stores.domain.arts.EmployeeTransaction;
import oracle.retail.stores.domain.employee.EmployeeIfc;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;

public class GDYNEmployeeTransaction extends EmployeeTransaction
{
	public static String lookupEmployeeSkipRole = "lookupEmployeeSkipRole";

	public EmployeeIfc lookupEmployeeSkipRole(final String employeeID) throws DataException
	{
		// creates an anonymous DataActionIfc object.
		DataActionIfc[] dataActions = new DataActionIfc[1];
		dataActions[0] = new DataActionIfc()
		{
			public Serializable getDataObject()
			{
				return employeeID;
			}
			public String getDataOperationName()
			{
				// this name corresponds to an operation in the quarry datascript.xml
				return lookupEmployeeSkipRole;
			}
		};

		setDataActions(dataActions);
		EmployeeIfc employee = (EmployeeIfc) getDataManager().execute(this);
		 
		return employee;
	}
}
