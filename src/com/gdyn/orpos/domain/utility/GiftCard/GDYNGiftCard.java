//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2014, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.domain.utility.GiftCard;

import com.gdyn.orpos.domain.utility.GDYNGiftCardIfc;

import oracle.retail.stores.domain.utility.GiftCard;

/**
 * - (GD-440) GD_CR 13 - Save Gift Card Authorization and Modify RTLog. 
 * 
 * - Extending the base GiftCardIfc for Group Dynamite.
 * - Adding Authorization Number.
 *
 * @author dmartinez (Starmount) Feb 10, 2014.
 */
public class GDYNGiftCard extends GiftCard implements GDYNGiftCardIfc
{
    private static final long serialVersionUID = -3881201749343845214L;
    protected String authorizationNumber = "";
    protected String authorizationResponse = "";
   
    
    /**
     * @return the authorizationResponse
     */
    public String getAuthorizationResponse()
    {
        return authorizationResponse;
    }

    /**
     * @param authorizationResponse the authorizationResponse to set
     */
    public void setAuthorizationResponse(String authorizationResponse)
    {
        this.authorizationResponse = authorizationResponse;
    }

    public String getAuthorizationNumber()
    {
        return authorizationNumber;
    }
    
    public void setAuthorizationNumber(String authorizationNumber)
    {
        this.authorizationNumber = authorizationNumber;
        
    }

}
