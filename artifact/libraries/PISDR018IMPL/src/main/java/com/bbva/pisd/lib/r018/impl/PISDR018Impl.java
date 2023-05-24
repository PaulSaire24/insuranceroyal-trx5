package com.bbva.pisd.lib.r018.impl;

import com.bbva.pisd.dto.insurance.aso.CustomerListASO;
import com.bbva.pisd.dto.insurance.blacklist.BlackListTypeDTO;
import com.bbva.pisd.dto.insurance.blacklist.BlockingCompanyDTO;
import com.bbva.pisd.dto.insurance.blacklist.EntityOutBlackListDTO;
import com.bbva.pisd.dto.insurance.blacklist.InsuranceBlackListDTO;
import com.bbva.pisd.dto.insurance.bo.SelectionQuotationPayloadBO;
import com.bbva.pisd.dto.insurance.bo.BlackListIndicatorBO;
import com.bbva.pisd.dto.insurance.commons.DocumentTypeDTO;
import com.bbva.pisd.dto.insurance.commons.IdentityDataDTO;
import com.bbva.pisd.dto.insurance.commons.IdentityDocumentDTO;
import com.bbva.pisd.dto.insurance.commons.InsuranceProductDTO;
import com.bbva.pisd.dto.insurance.utils.PISDConstants;
import com.bbva.pisd.dto.insurance.utils.PISDErrors;
import com.bbva.pisd.dto.insurance.utils.PISDValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;

public class PISDR018Impl extends PISDR018Abstract {

	private static final Logger LOGGER = LoggerFactory.getLogger(PISDR018Impl.class);

	@Override
	public EntityOutBlackListDTO executeBlackListValidation(InsuranceBlackListDTO input) {
		LOGGER.info("***** PISDR018Impl - executeBlackListValidation START ***** input: {}", input);
		if (input == null) {
			return null;
		}
		EntityOutBlackListDTO out = null;
		InsuranceBlackListDTO response = null;
		List<InsuranceBlackListDTO> data = new ArrayList<>();

		response = consultExternalBlackList(input);

		if (response == null) {
			return null;
		}

		response.setIdentityDocument(this.getValidIdentityDocument(input.getIdentityDocument()));
		out = new EntityOutBlackListDTO();
		data.add(response);
		out.setData(data);

		LOGGER.info("***** PISDR018Impl - executeBlackListValidation END ***** out: {}", out);
		return out;
	}

	private InsuranceBlackListDTO consultExternalBlackList(InsuranceBlackListDTO input) {
		LOGGER.info("***** PISDR018Impl - consultExternalBlackList START *****");
		if (input.getBlockingCompany() == null || input.getBlockingCompany().getId() == null) {
			input.setBlockingCompany(new BlockingCompanyDTO(PISDConstants.BLACKLIST_COMPANY_RIMAC));
		}
		if (input.getProduct() == null || input.getProduct().getId() == null) {
			input.setProduct(new InsuranceProductDTO(PISDConstants.HEALTH_RIMAC, null, null));
		}

		if (input.getIdentityDocument() == null || input.getIdentityDocument().getDocumentType() == null
				|| input.getIdentityDocument().getDocumentType().getId() == null
				|| input.getIdentityDocument().getDocumentNumber() == null) {
			return null;
		}
		InsuranceBlackListDTO response = null;
		switch (input.getBlockingCompany().getId()) {
			case PISDConstants.BLACKLIST_COMPANY_RIMAC:
				if (input.getBlackListType() == null || input.getBlackListType().getId() == null) {
					input.setBlackListType(new BlackListTypeDTO("3"));
				}
				response = getBlackListValidationRimac(input.getCustomerId(), input.getProduct().getId(),
						input.getIdentityDocument(), input.getBlackListType().getId(), input.getTraceId());
				break;
			case PISDConstants.BLACKLIST_COMPANY_CHUBB:
				break;
			default:
				break;
		}
		LOGGER.info("***** PISDR018Impl - consultExternalBlackList END ***** response: {}", response);
		return response;
	}

	private IdentityDocumentDTO getValidIdentityDocument(IdentityDocumentDTO input) {
		LOGGER.info("***** PISDR018Impl - getValidIdentityDocument START ***** input: {}", input);
		IdentityDocumentDTO out = new IdentityDocumentDTO(new DocumentTypeDTO(""), "");
		if (input == null) {
			return out;
		}
		if (input.getDocumentNumber() != null) {
			out.setNumber(input.getDocumentNumber());
		}
		if (input.getDocumentType() != null && input.getDocumentType().getId() != null) {
			out.getDocumentType().setId(input.getDocumentType().getId());
		}
		LOGGER.info("***** PISDR018Impl - getValidIdentityDocument END ***** out: {}", out);
		return out;
	}

	private InsuranceBlackListDTO getBlackListValidationRimac(String customerId, String productId,
															  IdentityDocumentDTO document, String bltype, String traceId) {
		LOGGER.info("***** PISDR018Impl - getBlackListValidationRimac START *****");
		SelectionQuotationPayloadBO resp = null;
		if (productId == null || document == null) {
			return null;
		}
		InsuranceBlackListDTO response = null;
		IdentityDataDTO input = new IdentityDataDTO();
		input.setNroDocumento(document.getDocumentNumber());
		input.setTipoDocumento(applicationConfigurationService.getProperty(document.getDocumentType().getId()));
		input.setTipoLista(bltype);
		if(productId.equals(PISDConstants.HEALTH_RIMAC)) {
			resp = pisdR008.executeGetBlackListHealthService(input, traceId);
			LOGGER.info("***** PISDR018Impl - getBlackListValidationRimac - SALUD - END *****");}
		else{
			InsuranceBlackListDTO indicator = consultBBVABlackList(customerId);
			if (indicator != null && indicator.getIsBlocked().equals(PISDConstants.LETTER_SI)) {
				LOGGER.info("***** PISDR018Impl - executeBlackListValidation indicator-IsActive ***** indicator: {}", indicator);
				return indicator;
			}
			if (productId.equals(PISDConstants.ProductEasyYesLife.EASY_YES_RIMAC)) {
				input.setProducto(PISDConstants.ProductEasyYesLife.EASY_YES_RIMAC);
				CustomerListASO customerList = this.pisdR008.executeGetCustomerInformation(customerId);
				validateQueryCustomerResponse(customerList);
				input.setFechaNacimiento(customerList.getData().get(0).getBirthData().getBirthDate());
			}
			resp = pisdR008.executeGetBlackListRiskService(input, traceId);
			LOGGER.info("***** PISDR018Impl - getBlackListValidationRimac - default - END *****");
		}
		if (resp != null) {
			response = new InsuranceBlackListDTO();
			if (resp.getStatus().equals(PISDConstants.BLACKLIST_BLOCKED)) {
				response.setIsBlocked(PISDConstants.LETTER_SI);
				response.setDescription(resp.getMensaje());
			} else {
				response.setIsBlocked(PISDConstants.LETTER_NO);
				response.setDescription("");
			}

			response.setId(shortStringTo(traceId, 36));
			response.setIdentityDocument(document);
			response.setBlackListType(new BlackListTypeDTO(bltype));
		}
		LOGGER.info("***** PISDR018Impl - getBlackListValidationRimac END ***** resp: {}", resp);
		return response;
	}

	private InsuranceBlackListDTO consultBBVABlackList(String customerId) {
		BlackListIndicatorBO indicator = null;
		if (customerId != null) {
			LOGGER.info("***** PISDR018Impl - executeBlackListValidation executeGetBlackListIndicatorService *****");
			indicator = pisdR008.executeGetBlackListIndicatorService(customerId);
			LOGGER.info(
					"***** PISDR018Impl - executeBlackListValidation executeGetBlackListIndicatorService ***** indicator: {}",
					indicator);
		}
		InsuranceBlackListDTO response = null;
		if (indicator != null) {
			response = new InsuranceBlackListDTO();
			response.setId(indicator.getIndicatorId());
			if (Boolean.TRUE.equals(indicator.getIsActive())) {
				response.setDescription(PISDConstants.BLACKLIST_MSJ_REJECT);
				response.setIsBlocked(PISDConstants.LETTER_SI);
			} else {
				response.setDescription("");
				response.setIsBlocked(PISDConstants.LETTER_NO);
			}
		}
		return response;
	}

	private String shortStringTo(String str, int i) {
		if (str == null) { return ""; }
		String resp = str;
		if (str.length()>i) {
			resp = str.substring(0,i);
		}
		return resp;
	}
	private void validateQueryCustomerResponse(CustomerListASO customerList) {
		if (customerList == null || isEmpty(customerList.getData())) {
			throw PISDValidation.build(PISDErrors.ERROR_CONNECTION_VALIDATE_CUSTOMER_SERVICE);
		}
		if(!validateBirthDate(customerList)){
			throw PISDValidation.build(PISDErrors.ERROR_BIRTHDATE_BLACKLIST);
		}
	}

	private boolean validateBirthDate(CustomerListASO customerList){
		return !isNull(customerList.getData().get(0).getBirthData()) && !isNull(customerList.getData().get(0).getBirthData().getBirthDate()) &&
				customerList.getData().get(0).getBirthData().getBirthDate().length() == 10 && validateBirthDateFormat(customerList.getData().get(0).getBirthData().getBirthDate());
	}

	private boolean validateBirthDateFormat(String fecha) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			dateFormat.setLenient(false);
			dateFormat.parse(fecha);
		} catch (ParseException e) {
			return false;
		}
		return true;
	}
}

