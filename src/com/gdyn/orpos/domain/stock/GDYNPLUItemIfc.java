//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.stock;

import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.stock.StockItemIfc;

/**
 * Extended to add a stock item instance
 * 
 * @author lcatania
 *
 */
public interface GDYNPLUItemIfc extends PLUItemIfc
{
    /**
     * GDYNPLUItemIfc
     * void
     * @param stockItem
     */
    public void setStockItem(StockItemIfc stockItem);
    
   
}