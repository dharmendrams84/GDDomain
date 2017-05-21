//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.manager.payment.fipay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

//-------------------------------------------------------------------------
/**
 * Returns a unique sequence number given a String value as a key.
 * Will roll the sequence number back to 1 when the sequence reaches the
 * resetValue passed to the constructor as a parameter.
 **/
// -------------------------------------------------------------------------

public class GDYNFIPAYSequenceNumberGenerator
{
    /** Logger */
    private Logger logger = Logger
            .getLogger(com.gdyn.orpos.domain.manager.payment.fipay.GDYNFIPAYSequenceNumberGenerator.class);

    protected Map<String, Integer> map = new HashMap<String, Integer>();
    protected int rollover = 999999;

    /**
     * Location and name of the sequence number file on the hard disk. The default
     * is set to "./sequenceNumbers.ser (in the current working directory)
     */
    protected String sequenceNumberFile = "./sequenceNumbers.ser";

    // ---------------------------------------------------------------------
    /**
     * Constructs a SequenceNumberGenerator
     * 
     * @param int rollover value
     **/
    // ---------------------------------------------------------------------
    public GDYNFIPAYSequenceNumberGenerator(int rollover)
    {
        this.rollover = rollover;
        readPersistedSequenceNumbers();
    }

    // ---------------------------------------------------------------------
    /**
     * Constructs a SequenceNumberGenerator
     * 
     * @param int rollover value
     * @param sequenceNumberFile
     **/
    // ---------------------------------------------------------------------
    public GDYNFIPAYSequenceNumberGenerator(int rollover, String sequenceNumberFile)
    {
        this.rollover = rollover;
        this.sequenceNumberFile = sequenceNumberFile;
        readPersistedSequenceNumbers();
    }

    // ---------------------------------------------------------------------
    /**
     * Returns the next SequenceNumber for a given key.
     * 
     * @param String
     *            key indicating which sequence number to return.
     * @return numeric String containing sequence number
     **/
    // ---------------------------------------------------------------------
    public synchronized String getNextNumber(String key)
    {
        int value = 1;

        if (map.containsKey(key))
        {
            value = map.get(key);
            if (value == rollover)
            {
                value = -1;
            }
            value = value + 1;
            map.put(key, value);
        }
        else
        {
            map.put(key, value);
        }
        // save the sequence numbers
        writePersistedSequenceNumbers();
        return Integer.toString(value);
    }

    /**
     * Reads the persisted sequence numbers from the hard disk if present. This is
     * read only once when this SequenceNumberGenerator is created.
     */
    @SuppressWarnings("unchecked")
    protected void readPersistedSequenceNumbers()
    {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try
        {
            File file = new File(this.sequenceNumberFile);
            if (file.exists())
            {
                fis = new FileInputStream(file);
                ois = new ObjectInputStream(fis);
                Object obj = ois.readObject();
                if (obj instanceof Map)
                {
                    map = (Map<String, Integer>) obj;
                }
                else
                {
                    logger.error("sequenceNumberFile is not of type Map.");
                }
            }
        }
        catch (FileNotFoundException fnfe)
        {
            logger.error("FileNotFoundException caught while trying to read sequence numbers.", fnfe);
        }
        catch (IOException ioe)
        {
            logger.error("IOException caught while trying to read sequence numbers.", ioe);
        }
        catch (ClassNotFoundException cnfe)
        {
            logger.error("ClassNotFoundException caught while trying to read sequence numbers.", cnfe);
        }
        finally
        {
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch (IOException ioe)
                {
                    logger.error("IOException caught while trying to close FileInputStream.");
                }
            }
            if (ois != null)
            {
                try
                {
                    ois.close();
                }
                catch (IOException ioe)
                {
                    logger.error("IOException caught while trying to close ObjectInputStream.");
                }
            }
        }
    }

    /**
     * Reads the persisted sequence numbers from the hard disk if present.
     */
    protected void writePersistedSequenceNumbers()
    {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try
        {
            File file = new File(this.sequenceNumberFile);
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(map);
            oos.flush();
        }
        catch (IOException ioe)
        {
            logger.error("IOException caught while trying to write sequence numbers.", ioe);
        }
        finally
        {
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (IOException ioe)
                {
                    logger.error("IOException caught while trying to close FileOutputStream.");
                }
            }
            if (oos != null)
            {
                try
                {
                    oos.close();
                }
                catch (IOException ioe)
                {
                    logger.error("IOException caught while trying to close ObjectOutputStream.");
                }
            }
        }
    }
}
