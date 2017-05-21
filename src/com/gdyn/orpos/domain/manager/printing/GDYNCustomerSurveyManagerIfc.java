//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.manager.printing;

import oracle.retail.stores.foundation.tour.manager.ManagerIfc;

/**
 * Interface for a GDYNCustomerSurveryManager instance.
 * 
 * @author MSolis
 * 
 */
public interface GDYNCustomerSurveyManagerIfc extends ManagerIfc
{
    /**
     * The name of the manager defined in ClientConduit.xml
     */
    public static final String TYPE = "GDYNCustomerSurveyManager";

    /**
     * This is a parameter that represents the base URL by brand. This
     * becomes part of the query string parameter that is needed by
     * the URL to the web service.
     * 
     * GDYNCustomerSurveryManagerIfc
     * void
     * 
     * @param baseURL
     */
    public void setBaseURLByBrand(String baseURL);

    /**
     * Return the shortened URL. This will be stored in the transaction object.
     */
    public String getShortURL();

    /**
     * The status is set when GDYNCustomerSurveyWebCall makes the call.
     */
    public String getStatus();

    /**
     * Set the Customer Invitation ID
     */
    public void setCustomerInvitationID(GDYNUniqueCustomerInvitationID uic);

    /**
     * This is the URL to the actual web service.
     */
    public void setLinkShorterURLWebServiceURL(String linkShorterURLWebServiceURL);

    /**
     * This is the timeout amount for the web call.
     */
    public void setLinkShorterTimeOut(int linkShorterTimeOut);

    /**
     * This method will use a threaded version of the web service call - asynchronous.
     * 
     * NOTE the use of join. The main event thread will wait the linkShorterTimeOut period
     * for the CustomerSurveyThread to finish. If it has not finished, then the main event thread
     * will see if the thread t is running. If so, it will close the connections of the web service.
     * That should allow thread t to exit its run method.
     * 
     */    
    public void makeWebCallNonThreading();
}
