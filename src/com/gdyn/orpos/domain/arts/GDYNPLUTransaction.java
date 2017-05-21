//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.arts;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.domain.arts.PLUTransaction;
import oracle.retail.stores.domain.transaction.SearchCriteriaIfc;
import oracle.retail.stores.foundation.manager.data.DataAction;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
// Begin GD-49: Develop Employee Discount Module
// lcatania (Starmount) Mar 1, 2013

/**
 * Extended to add a new operation to retrieve the original price of an item
 * 
 * @author lcatania
 *
 */
public class GDYNPLUTransaction extends PLUTransaction
{
    /**
     * This id is used to tell the compiler not to generate a new serialVersionUID.
     */
    private static final long serialVersionUID = 3315170717135035024L;

    /**
     * GDYNPLUTransaction
     * CurrencyIfc
     * @param inquiry - Retail Store ID and Item ID to lookup
     * @return The original price of the item
     * @throws DataException
     */
    public CurrencyIfc getItemOriginalPrice(SearchCriteriaIfc inquiry)
            throws DataException
    {
        DataAction dataAction = new DataAction();
        dataAction.setDataObject(inquiry);
        dataAction.setDataOperationName("ReadItemOriginalPrice");
        DataActionIfc dataActions[] = new DataActionIfc[1];
        dataActions[0] = dataAction;
        setDataActions(dataActions);
        CurrencyIfc itemOriginalPrice = (CurrencyIfc) getDataManager().execute(this);
        return itemOriginalPrice;
    }
    
    public CurrencyIfc getMerchandiseHierarchyView(SearchCriteriaIfc inquiry)
            throws DataException
    {
        DataAction dataAction = new DataAction();
        dataAction.setDataObject(inquiry);
        dataAction.setDataOperationName("ReadItemOriginalPrice");
        DataActionIfc dataActions[] = new DataActionIfc[1];
        dataActions[0] = dataAction;
        setDataActions(dataActions);
        CurrencyIfc itemOriginalPrice = (CurrencyIfc) getDataManager().execute(this);
        return itemOriginalPrice;
    }

}

// End GD-49: Develop Employee Discount Module