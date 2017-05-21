//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.domain.manager.printing;

import org.apache.log4j.Logger;

/**
 * This class represents the Unique Customer Invitation Code.
 * 
 * @author MSolis
 * 
 */
public class GDYNUniqueCustomerInvitationID implements GDYNUniqueCustomerInvitationIDIfc
{
    /**
     * The logger to which log messages will be sent.
     */
    private static final Logger logger = Logger.getLogger(GDYNUniqueCustomerInvitationID.class);

    /**
     * Store Id is numeric(5) with left padding using zeros.
     */
    protected String storeID;

    /**
     * Register Id is numeric(3) with left padding using zeros.
     */
    protected String registerID;

    /**
     * Numeric(6) - This is formatted according to yymmdd format with zero fill.
     * January 27, 2013 results in 130127
     * 
     * The length of this string is 6.
     */
    protected String transDate;

    /**
     * Numeric(6) - This is formatted according to hh24miss where each group is left padded
     * with zeros.
     * 12:15:32 AM results in 001532
     * 3:18:02 PM results in 151802
     * 
     * The length of this string is 6.
     */
    protected String transTime;

    /**
     * Numeric(6) left padded with 0.
     */
    protected String associate;

    /**
     * 1 = sale
     * 2 = return
     */
    protected String transType;

    /**
     * The UIC.
     */
    protected String uniqueInvitationCode;

    /**
     * Numeric passed in by the parameter.
     */
    protected String incentiveType;

    /**
     * Calculated using supplied algorithm
     */
    protected String checkDigit;

    public GDYNUniqueCustomerInvitationID()
    {
        /**
         * We are setting checkDigit to a default value of zero. Refer to
         * getUIC() and setCheckDigit().
         */
        this.checkDigit = new String("0");
    }

    /**
     * Sum up all the odds character multiplied by two.
     * Add to the sum the values of every even character.
     * Take the last character of the running sum and
     * subtract it from 10. This value is the check digit.
     * 
     * GDYNUniqueCustomerInvitationID
     * void
     */
    public void setCheckDigit()
    {
        buildUniqueInvitationCode();
        String uic_String = getUniqueInvitationCode();
        int[] uniqueInvitationCode = new int[MAX_LENGTH];

        for (int i = 0; i < MAX_LENGTH; i++)
        {
            uniqueInvitationCode[i] = Character.getNumericValue(uic_String.charAt(i));
        }

        int odd = 0;
        int even = 0;

        /**
         * The value of checkDigit is set in the constructor to zero.
         */
        for (int i = 0; i < MAX_LENGTH; i = i + 2)
        {
            odd += uniqueInvitationCode[i] * 2;
        }

        for (int i = 1; i < MAX_LENGTH; i = i + 2)
        {
            even += uniqueInvitationCode[i];
        }

        int sum = odd + even;
        // Extract the rightmost digit from the total.
        int lastDigit = sum % 10;
        int checkSum = (lastDigit == 0) ? 0 : 10 - lastDigit;

        this.checkDigit = String.valueOf(checkSum);
        buildUniqueInvitationCode();
    }

    /**
     * This method assembles the UIC value. Refer to setCheckDigit().
     * 
     * GDYNUniqueCustomerInvitationID
     * void
     */
    private void buildUniqueInvitationCode()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(getStoreID());
        builder.append(getRegisterID());
        builder.append(getTransDate());
        builder.append(getTransTime());
        builder.append(getAssociate());
        builder.append(getTransType());
        builder.append(getIncentiveType());
        builder.append(getCheckDigit());

        uniqueInvitationCode = builder.toString().trim();

        /**
         * The database insert will fail if this is larger than 29.
         */
        if (uniqueInvitationCode.length() != MAX_LENGTH)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("UIC length is incorrect. Current length is: " + uniqueInvitationCode.length());
                logger.error("UIC: " + uniqueInvitationCode);
            }
        }
    }

    /**
     * The UIC is represented as fields of Strings. This method
     * combines all the elements into one String of length 29.
     * 
     * NOTE: The checkDigit value will not be set PROPERLY until
     * this.setCheckDigit() is called first.
     * 
     * GDYNUniqueCustomerInvitationID
     * String
     * 
     * @return
     */
    public String getUniqueInvitationCode()
    {
        return this.uniqueInvitationCode;
    }

    public String getStoreID()
    {
        return storeID;
    }

    /**
     * Some field have zeros for left padding. Take in a string, determine
     * its length, and pad accordingly.
     * 
     * GDYNUniqueCustomerInvitationID
     * String
     * 
     * @param value
     * @param maxLength
     * @return
     */
    private String leftPad(String value, int maxLength)
    {
        String temp = value.trim();
        int len = temp.length();
        int pad = maxLength - len;
        if (len > maxLength)
        {
            logger.error(value + " has a max length of " + maxLength);
            throw new IllegalArgumentException(value + " exceeds length of " + maxLength);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pad; i++)
        {
            sb.append('0');
        }
        sb.append(temp);

        return sb.toString();
    }

    public void setStoreId(String storeID)
    {
        this.storeID = leftPad(storeID, STORE_ID_LENGTH);
    }

    public String getRegisterID()
    {
        return registerID;
    }

    public void setRegisterId(String registerID)
    {
        this.registerID = leftPad(registerID, REGISTER_ID_LENGTH);
    }

    public String getTransDate()
    {
        return transDate;
    }

    public void setTransDate(String transDate)
    {
        this.transDate = transDate;
    }

    public String getTransTime()
    {
        return transTime;
    }

    /**
     * This will need to be pre-formatted .....
     * GDYNCustomerSurveyRequest
     * void
     * 
     * @param txnTime
     */
    public void setTransTime(String transTime)
    {
        this.transTime = transTime;
    }

    public String getAssociate()
    {
        return associate;
    }

    public void setAssociate(String associate)
    {
        this.associate = leftPad(associate, ASSOCIATE_LENGTH);
    }

    public String getTransType()
    {
        return transType;
    }

    public void setTransType(String transType)
    {
        this.transType = transType;
    }

    public String getIncentiveType()
    {
        return incentiveType;
    }

    /**
     * Set the CSAT incentive type. Set in parameters.
     * 
     * GDYNUniqueCustomerInvitationID
     * void
     * 
     * @param incentiveType
     *            Parameter value
     */
    public void setIncentiveType(String incentiveType)
    {
        this.incentiveType = incentiveType;
    }

    public String getCheckDigit()
    {
        return checkDigit;
    }

}
