//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.domain.manager.printing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * This class has two ways of being used: one blocks until the web call returns, the other
 * spawns a thread to perform the web call. The threaded version will need to be called well before
 * the transaction object is written to the database. If called at the last possible 
 * moment, use the blocking version.
 * 
 * @author MSolis
 *
 */
public class GDYNCustomerSurveyWebCall
{
    enum Status
    {
        INIT, TIME_OUT, CONNECT_ERROR, BAD_URL, SUCCESS
    };

    /** The logger to which log messages will be sent */
    protected static final Logger logger = Logger.getLogger(GDYNCustomerSurveyWebCall.class);
    
    protected String url;

    protected int timeout;

    /**
     * Returns the status of the web service call. Refer to Status enum.
     */
    protected Status status;

    protected Date startTime;

    protected Date stopTime;

    protected boolean running;

    /**
     * The short URL returned from the web call. Make sure to
     * the verify the status is set to Status.SUCCESS.
     */
    protected String shortURL;

    /**
     * The URL providing the service.
     */
    protected URL srv;

    /**
     * The connection to the URL
     */
    protected URLConnection con;

    /**
     * The buffered reader for this connection to the URL.
     */
    protected BufferedReader buf;

    /**
     * Pass in the entire URL.
     * 
     * @param url The actual url to call with all get parameters.
     * @param timeout The timeout value set by the manager.
     */
    public GDYNCustomerSurveyWebCall(String url, int timeout)
    {
        this.url = url;
        this.timeout = timeout;
        this.status = Status.INIT;
    }

    /**
     * This method makes the web service call. During testing, we found the web service
     * was returning blank lines and lines with lots of spaces. We also found the shortened
     * URL to be embedded in html tags. This method stips the html tags, blank lines, and
     * space characters.
     * 
     * NOTE: We are using the connection and read timeout methods of the URLConnection object.
     * These timeouts are added together. Opening the connection should take longer than reading
     * the reply. From networking, DNS needs to be called and the URL cached at all routers
     * along the path from the store to the home office. 
     * 
     * As a design note, a thread can call this method and then wait for this thread
     * to return within the timeout period. The trade off is the time to start the thread 
     * and then switching back between the two threads to force a close. 
     * 
     * GDYNCustomerSurveyWebCall
     * Status
     * @return
     */
    public synchronized Status makeCall()
    {
        running = true;
        try
        {
            String inputLine;
            String line;
            StringBuilder results = new StringBuilder();

            srv = new URL(url);
            startTime = new Date();
            URLConnection con = srv.openConnection();
            con.setConnectTimeout(timeout);
            con.setReadTimeout(timeout);
            buf = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));

            while ((inputLine = buf.readLine()) != null)
            {
                line = inputLine.trim();
                if (line.length() > 0 && !line.equalsIgnoreCase("<html>")
                        && !line.equalsIgnoreCase("</html>"))
                {
                    results.append(inputLine.trim());
                }
            }

            stopTime = new Date();
            buf.close();
            shortURL = results.toString();
            status = Status.SUCCESS;
            if (logger.isDebugEnabled())
            {
                logger.debug("Customer survey URL: " + url);
                logger.debug("Customer survey short URL: " + shortURL);
                logger.debug("Customer survey web call time: " + getRunningTime());
            }
        }
        catch (MalformedURLException e)
        {
            stopTime = new Date();
            status = Status.BAD_URL;
        }
        catch (SocketTimeoutException e)
        {
            stopTime = new Date();
            // We were not able to connect and make the request.
            status = Status.TIME_OUT;
        }
        catch (IOException e)
        {
            stopTime = new Date();
            // We were not able to connect and make the request.
            status = Status.CONNECT_ERROR;
        }

        running = false;
        return getStatus();
    }

    public synchronized boolean isRunning()
    {
        return running;
    }

    /**
     * Close the connection and release resources.
     * 
     * GDYNCSATWebCall
     * void
     */
    public void stopRunning()
    {
        stopTime = new Date();
        status = Status.TIME_OUT;
        if (logger.isDebugEnabled())
        {
            logger.debug("An external thread is calling GDYNCSATWebCall.stopRunning()");
        }

        if (buf != null)
        {
            try
            {
                buf.close();
            }
            catch (IOException e)
            {
            }
        }
        if (con != null)
        {
            con = null;
        }
        if (srv != null)
        {
            srv = null;
        }
    }

    /**
     * Returns the short URL.
     * 
     * GDYNCSATWebCall
     * String
     * 
     * @return
     */
    public synchronized String getShortURL()
    {
        return shortURL;
    }


    /**
     * Returns the status of the web call.
     * 
     * GDYNCSATWebCall
     * Status
     * 
     * @return
     */
    public synchronized Status getStatus()
    {
        return status;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }
    
    public synchronized long getRunningTime()
    {
        if (startTime == null || stopTime == null)
        {
            return -1;
        }
        
        return stopTime.getTime() - startTime.getTime();
    }
}
