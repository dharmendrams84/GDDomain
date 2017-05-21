//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.stock;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.common.utility.LocalizedTextIfc;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.stock.PLUItem;
import oracle.retail.stores.domain.stock.StockItemIfc;
import oracle.retail.stores.foundation.utility.Util;

import org.apache.log4j.Logger;

// Begin GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item description
// lcatania (Starmount) Mar 22, 2013

/**
 * Extended to add a stock item instance
 * 
 * @author lcatania
 *
 */
public class GDYNPLUItem extends PLUItem
        implements GDYNPLUItemIfc
{

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger("com.gdyn.orpos.domain.stock.GDYNPLUItem");
    static final long serialVersionUID = 7321887861981917535L;
    protected StockItemIfc stockItem;
    
    //protected String itemDivision;
    
    public GDYNPLUItem()
    {
        super();
        stockItem = null;
    }

    public Object clone()
    {
        GDYNPLUItem newItem = new GDYNPLUItem();
        setCloneAttributes(newItem);
        return newItem;
    }

    public boolean equals(Object obj)
    {
        boolean result = false;
        if (this == obj)
        {
            result = true;
        }
        else
        {
            GDYNPLUItem itemIn = (GDYNPLUItem) obj;
            result = super.equals(obj)
                    && Util.isObjectEqual(getStockItem(), itemIn.getStockItem());
        }

        return result;
    }

    public void setStockItem(StockItemIfc stockItem)
    {
        this.stockItem = stockItem;
    }

	
	
    public StockItemIfc getStockItem()
    {
        if (stockItem == null)
        {
            stockItem = DomainGateway.getFactory().getStockItemInstance();
            stockItem.initialize(getItemID(), getLocalizedDescriptions(), (CurrencyIfc) getPrice().clone(),
                    getTaxGroupID(), getTaxable(), getDepartmentID());
        }

        return stockItem;
    }

    public void initialize(String id, LocalizedTextIfc desc, CurrencyIfc prc, int taxGroup, boolean taxFlag, String dept)
    {
        setItemID(id);
        setLocalizedDescriptions(desc);
        setPrice(prc);
        setTaxGroupID(taxGroup);
        setDepartmentID(dept);
        setTaxable(taxFlag);

        getStockItem().initialize(getItemID(), getLocalizedDescriptions(), (CurrencyIfc) getPrice().clone(),
                getTaxGroupID(), getTaxable(), getDepartmentID());
    }

    public void setCloneAttributes(GDYNPLUItem newClass)
    {
        super.setCloneAttributes(newClass);
        newClass.manufacturerItemUPC= manufacturerItemUPC;  
        if (stockItem != null){
            newClass.stockItem = (StockItemIfc) stockItem.clone();
         }
      //  newClass.itemDivision=itemDivision;
    }

    public String toString()
    {
        String superResult = super.toString();
        StringBuffer strResult = new StringBuffer(superResult);
        if (stockItem == null)
        {
            strResult.append("stockItem:[null]").append(Util.EOL);
        }
        else
        {
            strResult.append("stockItem:[").append(Util.EOL);
            strResult.append(stockItem.toString());
            //strResult.append(itemDivision);
        }

        return strResult.toString();
    }


}