//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2014, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.domain.utility;

import oracle.retail.stores.domain.utility.GiftCardIfc;

/**
 * - (GD-440) GD_CR 13 - Save Gift Card Authorization and Modify RTLog. 
 * 
 * - Extending the base GiftCardIfc for Group Dynamite.
 * - Adding Authorization Number.
 *
 * @author dmartinez (Starmount) Feb 10, 2014.
 */
public interface GDYNGiftCardIfc extends GiftCardIfc
{
    public abstract String getAuthorizationNumber();
    
    public abstract void setAuthorizationNumber(String authorizationNumber);
    
    public abstract String getAuthorizationResponse();
    
    public abstract void setAuthorizationResponse(String authorizationResponse);

}
