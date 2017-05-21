//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.manager.printing;

/**
 * Contains length constants
 * 
 * @author MSolis
 *
 */
public interface GDYNUniqueCustomerInvitationIDIfc
{
    /**
     * The maximum length of the UIC.
     */
    public static int MAX_LENGTH = 29;
    
    /**
     * The following is the length of each field 
     */
    public static int STORE_ID_LENGTH = 5;
    public static int REGISTER_ID_LENGTH = 3;
    public static int TXN_DATE_LENGTH = 6;
    public static int TXN_TIME_LENGTH = 6;
    public static int ASSOCIATE_LENGTH = 6;
    public static int TXN_TYPE_LENGTH = 1;
    public static int INCENTIVE_TYPE_LENGTH = 1;
    public static int CHECK_DIGIT_LENGTH = 1;
}
