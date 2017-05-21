//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package oracle.retail.stores.domain.manager.payment;

import java.io.Serializable;

import oracle.retail.stores.domain.utility.EYSDate;

/**
 * This class is being brought in from base product due to not being able to extend.
 * Adding the InitDebit request type for the MessageRouter.
 * 
 * @author mlawrence
 * 
 */
public interface PaymentServiceResponseIfc extends Serializable
{

    public abstract ResponseCode getResponseCode();

    public abstract void setResponseCode(ResponseCode responsecode);

    public abstract String getResponseMessage();

    public abstract void setResponseMessage(String s);

    public abstract EYSDate getResponseTime();

    public abstract void setResponseTime(EYSDate eysdate);

    public static enum ResponseCode
    {
        Active,
        AlreadyActive,
        Approved,
        ApprovedFloorLimit,
        ApprovedSplitTender,
        CallCenter, 
        CardNumError,
        CheckVelocity, 
        ConfigurationError, 
        Deactive,
        Declined, 
        DeviceTimeout, 
        Duplicate, 
        ErrorOrRetry, 
        Expended, 
        Expired, 
        FirstTimeUsage, 
        FloorLimit,
        HoldCall, 
        InquiryForTenderFailed,
        InquiryForTenderCanceledByCustomer, 
        InquiryForTenderSucceeded, 
        Inactive, 
        Invalid,
        InvalidCredentials, 
        InvalidMerchCall, 
        InvalidPIN, 
        InvalidTransaction, 
        MACFailure,
        MaxPINTryDecline,
        NoMoreLoadsAllowed,
        NotFound, 
        Offline, 
        PartialApproval, 
        PositiveIDRequired, 
        Referral, 
        Reload, 
        RequestNotSupported,
        Success, 
        TerminalIdError, 
        Timeout,
        Undefined, 
        Unknown, 
        UnsupportedRequest
    }
}
