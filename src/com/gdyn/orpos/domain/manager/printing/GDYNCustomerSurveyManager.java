//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.domain.manager.printing;

import oracle.retail.stores.foundation.tour.manager.Manager;
import org.apache.log4j.Logger;

import com.gdyn.orpos.domain.manager.printing.GDYNCustomerSurveyWebCall.Status;

/**
 * This class manages the web service call. It can call either a threaded or
 * blocking version of the web service call. Use the blocking version when
 * the call happens at the last possible moment.
 * 
 * @author MSolis
 * 
 */
public class GDYNCustomerSurveyManager extends Manager implements GDYNCustomerSurveyManagerIfc
{
    class CustomerSurveyThread extends Thread
    {
        protected int timeOut;
        protected String url;
        protected GDYNCustomerSurveyWebCall srv;

        /**
         * Sets the web service call
         * CustomerSurveyThread
         * void
         * 
         * @param srv
         */
        public void setService(GDYNCustomerSurveyWebCall srv)
        {
            this.srv = srv;
        }

        /**
         * Makes the webservice call to retrieve the shortened Survey URL
         */
        public void run()
        {
            Status status = srv.makeCall();
            if (logger.isDebugEnabled())
            {
                logger.debug("The status of the threaded web call is: " + status.toString());
            }
        }
    }

    protected static Logger logger = Logger.getLogger(GDYNCustomerSurveyManager.class);

    /**
     * Value set in ClientConduit.xml
     */
    protected String linkShorterURLWebServiceURL;

    /**
     * Value set in ClientConduit.xml
     */
    protected int linkShorterTimeOut;

    /**
     * The unique customer survey invitation code.
     */
    protected GDYNUniqueCustomerInvitationID uniqueInvitationCode;

    /**
     * This is based on brand. This is used to create the URL needed by the web service.
     */
    protected String baseURL;

    /**
     * This is the shortened URL returned from a
     * successful web service call. Refer to status
     * for more information.
     */
    protected String shortURL;

    /**
     * This is the object that makes the web call.
     */
    protected GDYNCustomerSurveyWebCall customerSurveryWebCall;

    public GDYNCustomerSurveyManager()
    {

    }

    /**
     * This method will make the synchronous web service call. This is the non-threading version
     * of the call. It has more status accuracy than the threaded version. The
     * threaded version's response depends on load and the operating system.
     * 
     * GDYNCustomerSurveyReward
     * void
     * 
     * @param csm
     */
    public void makeWebCallNonThreading()
    {
        getWebCallService();
        Status status = customerSurveryWebCall.makeCall();
        this.shortURL = customerSurveryWebCall.getShortURL();

        if (logger.isDebugEnabled())
        {
            logger.debug("Customer survey web call status is: " + status.toString());
        }
    }

    /**
     * This method will use a threaded version of the web service call - asynchronous.
     * 
     * NOTE the use of join. The main event thread will wait the linkShorterTimeOut period
     * for the CustomerSurveyThread to finish. If it has not finished, then the main event thread
     * will see if the thread t is running. If so, it will close the connections of the web service.
     * That should allow thread t to exit its run method.
     * 
     */
    public void makeThreadingWebServiceCall()
    {
        CustomerSurveyThread t = new CustomerSurveyThread();
        getWebCallService();
        t.setService(this.customerSurveryWebCall);
        t.start();

        try
        {
            t.join(linkShorterTimeOut);
        }
        catch (InterruptedException e)
        {
        }
        if (customerSurveryWebCall.isRunning())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Customer survey web call timeout has expired. Killing connection to service.");
            }
            customerSurveryWebCall.stopRunning();
        }
        /**
         * Store the shortURL.
         */
        this.shortURL = customerSurveryWebCall.getShortURL();
    }

    private void getWebCallService()
    {
        String srvCallURL = getServiceURL();
        this.customerSurveryWebCall = new GDYNCustomerSurveyWebCall(srvCallURL, linkShorterTimeOut);
    }

    /**
     * Set the Customer Invitation ID
     */
    public void setCustomerInvitationID(GDYNUniqueCustomerInvitationID uniqueInvitationCode)
    {
        this.uniqueInvitationCode = uniqueInvitationCode;
    }

    /**
     * Sets the URL that was determined by a algorithm provided by the client.
     */
    public void setBaseURLByBrand(String baseURL)
    {
        this.baseURL = baseURL;
    }

    /**
     * This method puts together the parts to make up the URL.
     * The format of a working URL is:
     * http://sadshh01:8055/run/webservice/?url=http://garageexperience.ca/?CN=04241001131210112300000001111
     * 
     * GDYNCustomerSurveryManager
     * String
     * 
     * @return
     */
    protected String getServiceURL()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(linkShorterURLWebServiceURL);
        builder.append("?url=");
        builder.append(baseURL);
        builder.append(uniqueInvitationCode.getUniqueInvitationCode());

        return builder.toString();
    }

    /**
     * The status is set when GDYNCustomerSurveyWebCall makes the call.
     */
    public String getStatus()
    {
        return customerSurveryWebCall.getStatus().toString();
    }

    /**
     * Return the shortened URL. This will be stored in the transaction object.
     */
    public String getShortURL()
    {
        return this.shortURL;
    }

    /**
     * This is the URL to the actual web service.
     */
    public void setLinkShorterURLWebServiceURL(String linkShorterURLWebServiceURL)
    {
        this.linkShorterURLWebServiceURL = linkShorterURLWebServiceURL;
    }

    /**
     * This is the timeout amount for the web call.
     */
    public void setLinkShorterTimeOut(int linkShorterTimeOut)
    {
        this.linkShorterTimeOut = linkShorterTimeOut;
    }

}
