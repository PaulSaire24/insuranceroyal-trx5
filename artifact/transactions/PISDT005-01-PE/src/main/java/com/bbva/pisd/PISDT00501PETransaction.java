package com.bbva.pisd;

import com.bbva.pisd.lib.r018.PISDR018;

import com.bbva.elara.domain.transaction.RequestHeaderParamsName;
import com.bbva.elara.domain.transaction.Severity;

import com.bbva.pisd.dto.insurance.blacklist.EntityOutBlackListDTO;
import com.bbva.pisd.dto.insurance.blacklist.InsuranceBlackListDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;

public class PISDT00501PETransaction extends AbstractPISDT00501PETransaction {

	private static final Logger LOGGER = LoggerFactory.getLogger(PISDT00501PETransaction.class);

	@Override
	public void execute() {
		LOGGER.info("Execution of PISDT00501PETransaction");
		LOGGER.info("Header traceId: {}", this.getRequestHeader().getHeaderParameter(RequestHeaderParamsName.REQUESTID));

		PISDR018 pisdR018 = this.getServiceLibrary(PISDR018.class);

		InsuranceBlackListDTO input = new InsuranceBlackListDTO();
		input.setCustomerId(this.getCustomerid());
		input.setIdentityDocument(this.getIdentitydocument());
		input.setBlackListType(this.getBlacklisttype());
		input.setBlockingCompany(this.getBlockingcompany());
		input.setProduct(this.getProduct());
		input.setSaleChannelId((String) this.getRequestHeader().getHeaderParameter(RequestHeaderParamsName.CHANNELCODE));
		input.setTraceId((String) this.getRequestHeader().getHeaderParameter(RequestHeaderParamsName.REQUESTID));

		EntityOutBlackListDTO blvalidation = pisdR018.executeBlackListValidation(input);

		if (isNull(blvalidation) || blvalidation.getData().isEmpty()) {
			setSeverity(Severity.ENR);
		} else {
			this.setEntityout(blvalidation);
		}
	}

}
