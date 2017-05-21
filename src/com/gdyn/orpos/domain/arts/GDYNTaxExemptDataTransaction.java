//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.arts;

import java.util.List;
import java.util.Locale;

import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.DataTransaction;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;

import org.apache.log4j.Logger;

import com.gdyn.orpos.domain.taxexempt.GDYNHandleReturnPOJO;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptCustomerCategory;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptCustomerCode;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptExceptionCode;
import com.gdyn.orpos.domain.taxexempt.GDYNTaxExemptIdImage;
import com.gdyn.orpos.domain.transaction.GDYNTransactionTaxIfc;

//------------------------------------------------------------------------------
/**
 * Data transaction for accessing Groupe Dynamite tax exempt data commands.
 * 
 * @author dteagle
 */
// ------------------------------------------------------------------------------

public class GDYNTaxExemptDataTransaction extends DataTransaction
{
    /**
     * Serial ID
     */
    private static final long serialVersionUID = 812920154178363373L;

    private static Logger log =
            Logger.getLogger(GDYNTaxExemptDataTransaction.class);

    // --------------------------------------------------------------------------
    /**
     * Default constructor.
     */
    public GDYNTaxExemptDataTransaction()
    {
        super("TaxExemptDataTransaction");
    }

    // --------------------------------------------------------------------------
    /**
     * @param locale
     * @return
     * @throws DataException
     */
    @SuppressWarnings("unchecked")
    public List<GDYNTaxExemptCustomerCategory> getCustomerCategories(
            GDYNTaxExemptSearchCriteriaIfc criteria) throws DataException
    {
        if (log.isDebugEnabled())
            log.debug("GDYNTaxExemptDataTransaction.getCustomerCategories");

        DataActionIfc[] dataActions = new DataActionIfc[1];
        dataActions[0] = createDataAction(criteria, "ReadTaxExemptCustomerCategories");
        setDataActions(dataActions);

        List<GDYNTaxExemptCustomerCategory> categoryList =
                (List<GDYNTaxExemptCustomerCategory>) getDataManager().execute(this);

        if (categoryList == null || categoryList.isEmpty())
        {
            throw new DataException(DataException.NO_DATA,
                    "No Caegory List was returned to TaxExemptDataTransaction.");
        }
        return categoryList;
    }

 // For issue 681146- Calling ReadTaxExemptCustomerCategoriesCat 
    
	public GDYNTransactionTaxIfc getCustomerCategoriesCat(
			GDYNHandleReturnPOJO searchObj) throws DataException
    {
        if (log.isDebugEnabled())
            log.debug("GDYNTaxExemptDataTransaction.getCustomerCategoriesCat");

        DataActionIfc[] dataActions = new DataActionIfc[1];
        dataActions[0] = createDataAction(searchObj, "ReadTaxExemptCustomerCategoriesCat");
        setDataActions(dataActions);

        GDYNTransactionTaxIfc categoryList =
                (GDYNTransactionTaxIfc) getDataManager().execute(this);

        return categoryList;
        
    }
    // --------------------------------------------------------------------------
    /**
     * @return
     * @throws DataException
     */
    public List<GDYNTaxExemptCustomerCode> getCustomerCodes(
            GDYNTaxExemptSearchCriteriaIfc criteria) throws DataException
    {
        // "ReadTaxExemptCustomerCodes"
        return null;
    }

    // --------------------------------------------------------------------------
    /**
     * @param category
     * @return
     * @throws DataException
     */
    public GDYNTaxExemptCustomerCode getCustomerCodesForCategory(
            GDYNTaxExemptCustomerCategory category) throws DataException
    {
        if (log.isDebugEnabled())
            log.debug("GDYNTaxExemptDataTransaction.getCustomerCodesForCategory");

        DataActionIfc[] dataActions = new DataActionIfc[1];
        dataActions[0] = createDataAction(category, "ReadTaxExemptCustomerCodeForCategory");
        setDataActions(dataActions);

        GDYNTaxExemptCustomerCode customerCode =
                (GDYNTaxExemptCustomerCode) getDataManager().execute(this);

        if (customerCode == null)
        {
            throw new DataException(DataException.NO_DATA,
                    "No customer code was returned to TaxExemptDataTransaction.");
        }
        return customerCode;
    }

    // --------------------------------------------------------------------------
    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<GDYNTaxExemptIdImage> getIdImages(Locale locale)
            throws DataException
    {
        String country = locale.getCountry();

        if (log.isDebugEnabled())
            log.debug("GDYNTaxExemptDataTransaction.getIdImages");

        DataActionIfc[] dataActions = new DataActionIfc[1];
        dataActions[0] = createDataAction(country, "ReadTaxExemptIdImages");
        setDataActions(dataActions);

        List<GDYNTaxExemptIdImage> idImageList =
                (List<GDYNTaxExemptIdImage>) getDataManager().execute(this);

        return idImageList;
    }

    // --------------------------------------------------------------------------
    /**
     * @param code
     * @return
     * @throws DataException
     */
    @SuppressWarnings("unchecked")
    public List<GDYNTaxExemptExceptionCode> getExceptionCodesForCustomerCode(
            GDYNTaxExemptCustomerCode code)
            throws DataException
    {
        if (log.isDebugEnabled())
            log.debug("GDYNTaxExemptDataTransaction.getExceptionCodesForCustomerCode");

        DataActionIfc[] dataActions = new DataActionIfc[1];
        dataActions[0] = createDataAction(code, "ReadTaxExemptExceptionCodesForCustomerCode");
        setDataActions(dataActions);

        List<GDYNTaxExemptExceptionCode> exceptionList =
                (List<GDYNTaxExemptExceptionCode>) getDataManager().execute(this);

        if (exceptionList == null || exceptionList.isEmpty())
        {
            throw new DataException(DataException.NO_DATA,
                    "No exception List was returned to TaxExemptDataTransaction.");
        }
        return exceptionList;
    }
}
