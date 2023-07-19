package com.bbva.pisd.lib.r018.impl;

import com.bbva.pisd.dto.insurance.aso.CustomerListASO;
import com.bbva.pisd.dto.insurance.blacklist.BlackListTypeDTO;
import com.bbva.pisd.dto.insurance.blacklist.BlockingCompanyDTO;
import com.bbva.pisd.dto.insurance.blacklist.EntityOutBlackListDTO;
import com.bbva.pisd.dto.insurance.blacklist.InsuranceBlackListDTO;
import com.bbva.pisd.dto.insurance.bo.ContactDetailsBO;
import com.bbva.pisd.dto.insurance.bo.GeographicGroupsBO;
import com.bbva.pisd.dto.insurance.bo.SelectionQuotationPayloadBO;
import com.bbva.pisd.dto.insurance.bo.BlackListIndicatorBO;
import com.bbva.pisd.dto.insurance.bo.customer.CustomerBO;
import com.bbva.pisd.dto.insurance.commons.DocumentTypeDTO;
import com.bbva.pisd.dto.insurance.commons.IdentityDataDTO;
import com.bbva.pisd.dto.insurance.commons.IdentityDocumentDTO;
import com.bbva.pisd.dto.insurance.commons.InsuranceProductDTO;
import com.bbva.pisd.dto.insurance.utils.PISDConstants;
import com.bbva.pisd.dto.insurance.utils.PISDErrors;
import com.bbva.pisd.dto.insurance.utils.PISDValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;

public class PISDR018Impl extends PISDR018Abstract {

	private static final Logger LOGGER = LoggerFactory.getLogger(PISDR018Impl.class);
	private static final String CUSTOMER_VALIDATION_MESSAGE = "CUSTOMER_VALIDATION_MESSAGE";
    private static final String MOBILE_NUMBER = "MOBILE_NUMBER";
	private static final String EMAIL = "EMAIL";
	private static final String DISTRICT = "DISTRICT";
	private static final String PROVINCE = "PROVINCE";
	private static final String DEPARTMENT = "DEPARTMENT";
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
		SelectionQuotationPayloadBO resp;
		InsuranceBlackListDTO response = null;
		IdentityDataDTO input = new IdentityDataDTO();
		input.setNroDocumento(document.getDocumentNumber());
		input.setTipoDocumento(applicationConfigurationService.getProperty(document.getDocumentType().getId()));
		input.setTipoLista(bltype);

		CustomerListASO customerList = this.pisdR008.executeGetCustomerInformation(customerId);
		validateQueryCustomerResponse(customerList);

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
				validateBirthDate(customerList);
				input.setFechaNacimiento(customerList.getData().get(0).getBirthData().getBirthDate());
			}
			resp = pisdR008.executeGetBlackListRiskService(input, traceId);
			LOGGER.info("***** PISDR018Impl - getBlackListValidationRimac - default - END *****");
		}
		//validación de datos obligatorios del cliente y respuesta de Rimac
		if(validateMissingCustomerData(customerList.getData().get(0))){
			response = new InsuranceBlackListDTO();
			response.setIsBlocked(PISDConstants.LETTER_SI);
			response.setDescription(applicationConfigurationService.getProperty(CUSTOMER_VALIDATION_MESSAGE));
		}else if (resp != null && resp.getStatus().equals(PISDConstants.BLACKLIST_BLOCKED)) {
			response = new InsuranceBlackListDTO();
			response.setIsBlocked(PISDConstants.LETTER_SI);
			response.setDescription(resp.getMensaje());
		} else if (resp != null && !resp.getStatus().equals(PISDConstants.BLACKLIST_BLOCKED)){
			response = new InsuranceBlackListDTO();
			response.setIsBlocked(PISDConstants.LETTER_NO);
			response.setDescription("");
		}

		if(!Objects.isNull(response)){
			response.setId(shortStringTo(traceId, 36));
			response.setIdentityDocument(document);
			response.setBlackListType(new BlackListTypeDTO(bltype));
		}

		LOGGER.info("***** PISDR018Impl - getBlackListValidationRimac END ***** resp: {}", resp);
		return response;
	}

	private boolean validateMissingCustomerData(CustomerBO customer){
		return !validateIdentityDocument(customer) || !validatePersonalData(customer) || !validateContactDetails(customer)
				|| !validateAddress(customer);
	}

	private boolean validateIdentityDocument(CustomerBO customer){
		return !Objects.isNull(customer.getIdentityDocuments()) && !customer.getIdentityDocuments().isEmpty() &&
				!Objects.isNull(customer.getIdentityDocuments().get(0).getDocumentNumber()) && !customer.getIdentityDocuments().get(0).getDocumentNumber().isEmpty() &&
				!Objects.isNull(customer.getIdentityDocuments().get(0).getDocumentType()) && !Objects.isNull(customer.getIdentityDocuments().get(0).getDocumentType().getId()) &&
				!customer.getIdentityDocuments().get(0).getDocumentType().getId().isEmpty();
	}

	private boolean validatePersonalData(CustomerBO customer){
		return !Objects.isNull((customer.getFirstName())) && !customer.getFirstName().isEmpty()  && !Objects.isNull(customer.getLastName()) &&
				!customer.getLastName().isEmpty() && !Objects.isNull(customer.getSecondLastName()) && !customer.getSecondLastName().isEmpty() &&
				!Objects.isNull(customer.getGender()) && !Objects.isNull(customer.getGender().getId()) && !customer.getGender().getId().isEmpty();
	}

	private boolean validateContactDetails(CustomerBO customer){
		if(Objects.isNull(customer.getContactDetails()) || customer.getContactDetails().isEmpty() ||
				customer.getContactDetails().size() < 2){
			return false;
		}

		ContactDetailsBO celular = customer.getContactDetails().stream().filter(
						c -> !Objects.isNull(c.getContactType()) && !Objects.isNull(c.getContactType().getId()) &&
								MOBILE_NUMBER.equals(c.getContactType().getId())).findFirst().
				orElse(null);
		ContactDetailsBO correo = customer.getContactDetails().stream().filter(
						c -> !Objects.isNull(c.getContactType()) && !Objects.isNull(c.getContactType().getId()) &&
								EMAIL.equals(c.getContactType().getId())).findFirst().
				orElse(null);

		return !Objects.isNull(celular) && !Objects.isNull(correo) && !Objects.isNull(celular.getContact()) &&
				!Objects.isNull(correo.getContact()) && !celular.getContact().isEmpty() && !correo.getContact().isEmpty();
	}

	private boolean validateAddress(CustomerBO customer){
		if(Objects.isNull(customer.getAddresses()) || customer.getAddresses().isEmpty() ||
				Objects.isNull(customer.getAddresses().get(0).getLocation()) ||
				Objects.isNull(customer.getAddresses().get(0).getLocation().getGeographicGroups()) ||
				customer.getAddresses().get(0).getLocation().getGeographicGroups().size() < 3 ||
				Objects.isNull(customer.getAddresses().get(0).getLocation().getAdditionalInformation()) ||
				customer.getAddresses().get(0).getLocation().getAdditionalInformation().isEmpty()){
			return false;
		}

		GeographicGroupsBO district = customer.getAddresses().get(0).getLocation().getGeographicGroups().stream().filter(
						a -> !Objects.isNull(a.getGeographicGroupType()) && !Objects.isNull(a.getGeographicGroupType().getId()) &&
								DISTRICT.equals(a.getGeographicGroupType().getId())).findFirst().
				orElse(null);
		GeographicGroupsBO province = customer.getAddresses().get(0).getLocation().getGeographicGroups().stream().filter(
						a -> !Objects.isNull(a.getGeographicGroupType()) && !Objects.isNull(a.getGeographicGroupType().getId()) &&
								PROVINCE.equals(a.getGeographicGroupType().getId())).findFirst().
				orElse(null);
		GeographicGroupsBO department = customer.getAddresses().get(0).getLocation().getGeographicGroups().stream().filter(
						a -> !Objects.isNull(a.getGeographicGroupType()) && !Objects.isNull(a.getGeographicGroupType().getId()) &&
								DEPARTMENT.equals(a.getGeographicGroupType().getId())).findFirst().
				orElse(null);

		return !Objects.isNull(district) && !Objects.isNull(province) && !Objects.isNull(department) &&
				!district.getName().isEmpty() && !Objects.isNull(district.getName()) &&
				!province.getName().isEmpty() && !Objects.isNull(province.getName()) &&
				!department.getName().isEmpty() && !Objects.isNull(department.getName());
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
				response.setDescription("");
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
	}

	private void validateBirthDate(CustomerListASO customerList){
		if(isNull(customerList.getData().get(0).getBirthData()) || isNull(customerList.getData().get(0).getBirthData().getBirthDate()) ||
				customerList.getData().get(0).getBirthData().getBirthDate().length() != 10){
			throw PISDValidation.build(PISDErrors.ERROR_BIRTHDATE_BLACKLIST);
		}
	}
}

