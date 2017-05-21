//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.manager.payment.fipay;

import java.io.Serializable;
import java.util.ArrayList;

import oracle.retail.stores.commext.connector.Connector;
import oracle.retail.stores.commext.message.MessageException;

import org.apache.log4j.Logger;

import AJBComm.CAFipay;
import AJBComm.CAFipayNetworkException;
import AJBComm.CAFipayTimeoutException;

public class GDYNFIPAYConnector extends Connector implements GDYNFIPAYRequestResponseConstantsIfc
{
    /**
     * Logger
     */
    public static final Logger logger = Logger.getLogger(GDYNFIPAYConnector.class);

    // IP and Port to the FIPAY Device
    protected String hostName = "127.0.0.1";
    protected int port = 24900;
    protected int messageResponseTimeout = 60000;

    /**
     * Not used for FIPAY
     */
    protected void closeConnector()
    {
    }

    /**
     * Not used for FIPAY
     */
    protected void openConnector() throws MessageException
    {
    }

    /**
     * This method sends the message to the FIPay payment processor
     * 
     * @param
     * @return
     */
    protected Serializable send(Serializable message) throws MessageException
    {
        // Call the AJBComm class
        boolean fipayResponse = false;
        GDYNFIPAYResponseIfc response = null;

        CAFipay fipay = new CAFipay(hostName, port);
        GDYNFIPAYRequest fipayRequest = (GDYNFIPAYRequest) message;

        String requestMessage = fipayRequest.getRequestData();

        try
        {
            logger.debug("The FIPAY Request Message being sent is:  " + message);
            fipayResponse = fipay.SEND_MSGAPI(requestMessage);
        }
        catch (CAFipayNetworkException e)
        {
            logger.error("The FIPAY Request Message has a network connection error:  " + e.getErrorCode()
                    + e.getErrorDescription());
            e.printStackTrace();
        }
        catch (CAFipayTimeoutException e)
        {
            logger.error("The FIPAY Request Message has a timeout error:  " + e);
            e.printStackTrace();
        }

        if (fipayResponse)
        {
            try
            {
                boolean messageEvent = false;
                long startTime = System.currentTimeMillis();
                long elapsedTime;

                ArrayList<String> messageResponseList = new ArrayList<String>();

                do
                {
                    String responseFIPAY = fipay.RECV_MSGAPI();

                    messageResponseList.add(responseFIPAY);

                    if (responseFIPAY.startsWith(CREDIT_DEBIT_AUTH_RESP_TRANSACTION_TYPE) ||
                            responseFIPAY.startsWith(CREDIT_DEBIT_AUTH_SAF_REQ_TRANSACTION_TYPE))
                    {
                        messageEvent = true;
                    }

                    logger.debug("The FIPAY Response Message retrieved is:  " + responseFIPAY);

                    elapsedTime = System.currentTimeMillis() - startTime;
                }
                while ((elapsedTime < messageResponseTimeout) &&
                        (messageEvent != true));

                response = new GDYNFIPAYResponse(messageResponseList, fipayRequest.getSource());

            }
            catch (CAFipayNetworkException e)
            {
                logger.debug("The FIPAY Response Message has a network connection error:  " + e);
                e.printStackTrace();
            }
        }

        return response;
    }

    /**
     * Not used for FIPAY
     */
    protected void updateConnector() throws MessageException
    {
    }

    /**
     * Get the IP Address of the FIPAY Service
     * GDYNFIPAYConnector
     * String
     * 
     * @return
     */
    public String getHostName()
    {
        return hostName;
    }

    /**
     * Set the IP Address of the FIPAY Service
     * GDYNFIPAYConnector
     * void
     * 
     * @param hostName
     */
    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    /**
     * Get the Port of the FIPAY Service
     * GDYNFIPAYConnector
     * int
     * 
     * @return
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Set the Port of the FIPAY Service
     * GDYNFIPAYConnector
     * void
     * 
     * @param port
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * @return the messageResponseTimeout
     */
    public int getMessageResponseTimeout()
    {
        return messageResponseTimeout;
    }

    /**
     * @param messageResponseTimeout
     *            the messageResponseTimeout to set
     */
    public void setMessageResponseTimeout(int messageResponseTimeout)
    {
        this.messageResponseTimeout = messageResponseTimeout;
    }

}
