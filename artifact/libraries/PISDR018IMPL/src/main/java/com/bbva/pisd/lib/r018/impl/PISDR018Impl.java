package com.bbva.pisd.lib.r018.impl;

import com.bbva.pisd.dto.insurance.aso.CustomerListASO;

import com.bbva.pisd.dto.insurance.blacklist.BlackListTypeDTO;
import com.bbva.pisd.dto.insurance.blacklist.EntityOutBlackListDTO;
import com.bbva.pisd.dto.insurance.blacklist.InsuranceBlackListDTO;
import com.bbva.pisd.dto.insurance.blacklist.BlockingCompanyDTO;

import com.bbva.pisd.dto.insurance.bo.SelectionQuotationPayloadBO;
import com.bbva.pisd.dto.insurance.bo.BlackListIndicatorBO;

import com.bbva.pisd.dto.insurance.commons.InsuranceProductDTO;
import com.bbva.pisd.dto.insurance.commons.IdentityDataDTO;

import com.bbva.pisd.dto.insurance.utils.PISDConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static java.util.Collections.singletonList;

import static java.util.Objects.nonNull;
import static java.util.Objects.isNull;

public class PISDR018Impl extends PISDR018Abstract {

	private static final Logger LOGGER = LoggerFactory.getLogger(PISDR018Impl.class);

	@Override
	public EntityOutBlackListDTO executeBlackListValidation(final InsuranceBlackListDTO input) {
		LOGGER.info("***** PISDR018Impl - executeBlackListValidation START ***** input: {}", input);

		this.validateBlockingCompany(input);
		this.validateProduct(input);
		this.validateBlackListType(input);

		InsuranceBlackListDTO response = this.validateCustomerAvailability(input);

		input.getIdentityDocument().setNumber(input.getIdentityDocument().getDocumentNumber());

		response.setIdentityDocument(input.getIdentityDocument());

		EntityOutBlackListDTO out = new EntityOutBlackListDTO();
		out.setData(singletonList(response));

		LOGGER.info("***** PISDR018Impl - executeBlackListValidation END ***** out: {}", out);
		return out;
	}

	private void validateBlockingCompany(final InsuranceBlackListDTO input) {
		if(isNull(input.getBlockingCompany()) || isNull(input.getBlockingCompany().getId())) {
			input.setBlockingCompany(new BlockingCompanyDTO(PISDConstants.BLACKLIST_COMPANY_RIMAC));
		}
	}

	private void validateProduct(InsuranceBlackListDTO input) {
		if(isNull(input.getProduct()) || isNull(input.getProduct().getId())) {
			input.setProduct(new InsuranceProductDTO(PISDConstants.HEALTH_RIMAC, null, null));
		}
	}

	private void validateBlackListType(final InsuranceBlackListDTO input) {
		if (isNull(input.getBlackListType()) || isNull(input.getBlackListType().getId())) {
			input.setBlackListType(new BlackListTypeDTO("3"));
		}
	}

	private InsuranceBlackListDTO validateCustomerAvailability(final InsuranceBlackListDTO requestBody) {
		LOGGER.info("***** PISDR018Impl - getBlackListValidationRimac START *****");

		final String productId = requestBody.getProduct().getId();
		final String blackListType = requestBody.getBlackListType().getId();

		IdentityDataDTO identityData = this.mapperHelper.
				createBlackListRimacRequest(requestBody.getIdentityDocument(), blackListType);

		if(productId.equals(PISDConstants.HEALTH_RIMAC)) {
			LOGGER.info("***** PISDR018Impl - getBlackListValidationRimac | Healthy validation *****");
			SelectionQuotationPayloadBO rimacResponse = this.pisdR008.executeGetBlackListHealthService(identityData, requestBody.getTraceId());
			return this.mapperHelper.createResponseBlackListBBVAService(requestBody, rimacResponse);
		} else {
			InsuranceBlackListDTO indicator = this.consultBBVABlackList(requestBody.getCustomerId());
			if (nonNull(indicator) && PISDConstants.LETTER_SI.equals(indicator.getIsBlocked())) {
				LOGGER.info("***** PISDR018Impl - executeBlackListValidation ***** Inelegible customer!!!");
				return indicator;
			}
			CustomerListASO customerInformation = null;
			if (isVidadinamicoOrEasyYes(productId)) {
				LOGGER.info("***** PISDR018Impl - getBlackListValidationRimac | Life validation *****");

				customerInformation = this.pisdR008.executeGetCustomerInformation(requestBody.getCustomerId());

				// default birthdate
				identityData.setFechaNacimiento("1995-04-02");

				if(Objects.nonNull(customerInformation)){
					identityData.setFechaNacimiento(customerInformation.getData().get(0).getBirthData().getBirthDate());
				}

				identityData.setProducto(productId);
			}
			SelectionQuotationPayloadBO rimacResponse = this.pisdR008.executeGetBlackListRiskService(identityData, requestBody.getTraceId());
			LOGGER.info("***** PISDR018Impl - getBlackListValidationRimac END *****");
			return this.mapperHelper.createResponseBlackListBBVAService(requestBody, rimacResponse, customerInformation);
		}
	}

	private static boolean isVidadinamicoOrEasyYes(String productId) {
		return productId.equals(PISDConstants.ProductEasyYesLife.EASY_YES_RIMAC) || productId.equals(PISDConstants.ProductVidaDinamicoLife.VIDA_DINAMICO);
	}

	private InsuranceBlackListDTO consultBBVABlackList(String customerId) {
		BlackListIndicatorBO indicator = this.pisdR008.executeGetBlackListIndicatorService(customerId);
		if(nonNull(indicator)) {
			return this.mapperHelper.createResponseToIneligibleCustomer(indicator);
		}
		return null;
	}

}

