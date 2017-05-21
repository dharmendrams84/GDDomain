package com.gdyn.orpos.domain.arts;

import java.util.HashMap;
import java.util.HashSet;

import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.DataTransaction;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;

import com.gdyn.orpos.domain.tax.GDYNTaxAuthorityIfc;

/**
 * Class for reading information about a Tax Authority, given the Tax Authority ID
 * @author wfesto
 */
public class GDYNTaxAuthorityDataTransaction extends DataTransaction
{
	private static final long serialVersionUID = -7363447189498458831L;

    // --------------------------------------------------------------------------
    /**
     * Default constructor.
     */
    public GDYNTaxAuthorityDataTransaction()
    {
        super("TaxAuthorityDataTransaction");
    }
	
	public HashMap<Integer, GDYNTaxAuthorityIfc> getTaxAuthorities(HashSet<Integer> idSet) throws DataException
	{
		DataActionIfc[] dataActions = new DataActionIfc[1];
        dataActions[0] = createDataAction(idSet, "ReadTaxAuthorityInformation");
        setDataActions(dataActions);
		
		HashMap<Integer, GDYNTaxAuthorityIfc> authorityMap = (HashMap<Integer, GDYNTaxAuthorityIfc>) getDataManager().execute(this);
		
		return authorityMap;
	}
}
