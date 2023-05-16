package com.bbva.pisd.lib.r008;

import com.bbva.pisd.dto.insurance.bo.BlackListIndicatorBO;
import com.bbva.pisd.dto.insurance.bo.SelectionQuotationPayloadBO;
import com.bbva.pisd.dto.insurance.commons.IdentityDataDTO;
import com.bbva.pisd.dto.insurance.aso.CustomerListASO;
public interface PISDR008 {

	BlackListIndicatorBO executeGetBlackListIndicatorService(String customerId);
	SelectionQuotationPayloadBO executeGetBlackListRiskService(IdentityDataDTO input, String traceId);
	SelectionQuotationPayloadBO executeGetBlackListHealthService(IdentityDataDTO input, String traceId);
	CustomerListASO executeGetCustomerInformation(String customerId);
}
