package com.bbva.pisd;

import com.bbva.elara.transaction.AbstractTransaction;
import com.bbva.pisd.dto.insurance.blacklist.BlackListTypeDTO;
import com.bbva.pisd.dto.insurance.blacklist.BlockingCompanyDTO;
import com.bbva.pisd.dto.insurance.blacklist.EntityOutBlackListDTO;
import com.bbva.pisd.dto.insurance.commons.IdentityDocumentDTO;
import com.bbva.pisd.dto.insurance.commons.InsuranceProductDTO;

/**
 * In this class, the input and output data is defined automatically through the setters and getters.
 */
public abstract class AbstractPISDT00501PETransaction extends AbstractTransaction {

	public AbstractPISDT00501PETransaction(){
	}


	/**
	 * Return value for input parameter customerId
	 */
	protected String getCustomerid(){ return (String)this.getParameter("customerId"); }

	/**
	 * Return value for input parameter identityDocument
	 */
	protected IdentityDocumentDTO getIdentitydocument(){ return (IdentityDocumentDTO)this.getParameter("identityDocument"); }

	/**
	 * Return value for input parameter blackListType
	 */
	protected BlackListTypeDTO getBlacklisttype(){ return (BlackListTypeDTO)this.getParameter("blackListType"); }

	/**
	 * Return value for input parameter blockingCompany
	 */
	protected BlockingCompanyDTO getBlockingcompany(){ return (BlockingCompanyDTO)this.getParameter("blockingCompany"); }

	/**
	 * Return value for input parameter product
	 */
	protected InsuranceProductDTO getProduct(){ return (InsuranceProductDTO)this.getParameter("product"); }

	/**
	 * Set value for EntityOutBlackListDTO output parameter entityOut
	 */
	protected void setEntityout(final EntityOutBlackListDTO field){ this.addParameter("entityOut", field); }
}
