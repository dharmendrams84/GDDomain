//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package oracle.retail.stores.domain.manager.payment;

import java.io.Serializable;

import oracle.retail.stores.domain.manager.payment.event.PaymentEventListener;
import oracle.retail.stores.domain.store.WorkstationIfc;

/**
 * This class is being brought in from base product due to not being able to extend.
 * Adding the InitDebit request type for the MessageRouter.
 * 
 * @author mlawrence
 * 
 */
public interface PaymentServiceRequestIfc extends Serializable
{

    public abstract RequestType getRequestType();

    public abstract void setRequestType(RequestType requesttype);

    public abstract String getStoreID();

    public abstract String getTransactionID();

    public abstract String getWorkstationID();

    public abstract WorkstationIfc getWorkstation();

    public abstract boolean isTrainingMode();

    public abstract boolean isTransReentryMode();

    public abstract void setTransactionID(String s);

    public abstract void setWorkstation(WorkstationIfc workstationifc);

    public abstract PaymentServiceResponseIfc newResponseInstance();

    public abstract PaymentEventListener[] getPaymentEventListeners();

    public abstract void setPaymentEventListeners(PaymentEventListener apaymenteventlistener[]);

    public static enum RequestType
    {
        AuthorizeCallReferral,
        AuthorizeCard,
        AuthorizeCardRefund,
        AuthorizeCardRefundWithToken,
        AuthorizeCheck,
        AuthorizeCheckCallReferral,
        AuthorizeECheck,
        CardTokenInquiry,
        CustomerInteraction,
        GetSignature,
        GiftCard,
        InstantCreditApplication,
        InstantCreditApplicationInquiry,
        InstantCreditInquiry,
        ReverseCard,
        ReverseECheck,
        ReverseGiftCard,
        StatusInquiry,
        GetCardToken,
        InitDebit,
        EndOfDay
    }
}
