package com.bbva.pisd;

import com.bbva.pisd.lib.r018.PISDR018;
import com.bbva.elara.domain.transaction.RequestHeaderParamsName;
import com.bbva.elara.domain.transaction.Severity;
import com.bbva.pisd.dto.insurance.blacklist.EntityOutBlackListDTO;
import com.bbva.pisd.dto.insurance.blacklist.InsuranceBlackListDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get black list validation
 *
 */
public class PISDT00501PETransaction extends AbstractPISDT00501PETransaction {

	private static final Logger LOGGER = LoggerFactory.getLogger(PISDT00501PETransaction.class);

	@Override
	public void execute() {
		PISDR018 pisdR018 = this.getServiceLibrary(PISDR018.class);
		LOGGER.info("Execution of PISDT00501PETransaction");
		LOGGER.info("Header traceId: {}", this.getRequestHeader().getHeaderParameter(RequestHeaderParamsName.REQUESTID));
		InsuranceBlackListDTO input = new InsuranceBlackListDTO();
		input.setCustomerId(this.getCustomerid());
		input.setIdentityDocument(this.getIdentitydocument());
		input.setBlackListType(this.getBlacklisttype());
		input.setBlockingCompany(this.getBlockingcompany());
		input.setProduct(this.getProduct());
		input.setTraceId((String) this.getRequestHeader().getHeaderParameter(RequestHeaderParamsName.REQUESTID));

		EntityOutBlackListDTO blvalidation = pisdR018.executeBlackListValidation(input);
		if (blvalidation == null || blvalidation.getData().isEmpty()) {
			setSeverity(Severity.ENR);
		} else {
			this.setEntityout(blvalidation);
		}
	}

}
