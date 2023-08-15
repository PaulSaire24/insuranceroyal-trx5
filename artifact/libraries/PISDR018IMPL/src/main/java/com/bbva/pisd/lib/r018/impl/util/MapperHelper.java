package com.bbva.pisd.lib.r018.impl.util;

import com.bbva.elara.configuration.manager.application.ApplicationConfigurationService;

import com.bbva.pisd.dto.insurance.aso.CustomerListASO;
import com.bbva.pisd.dto.insurance.blacklist.BlackListTypeDTO;
import com.bbva.pisd.dto.insurance.blacklist.InsuranceBlackListDTO;

import com.bbva.pisd.dto.insurance.bo.BlackListIndicatorBO;

import com.bbva.pisd.dto.insurance.bo.ContactDetailsBO;
import com.bbva.pisd.dto.insurance.bo.GeographicGroupsBO;
import com.bbva.pisd.dto.insurance.bo.SelectionQuotationPayloadBO;
import com.bbva.pisd.dto.insurance.bo.customer.CustomerBO;
import com.bbva.pisd.dto.insurance.commons.IdentityDataDTO;
import com.bbva.pisd.dto.insurance.commons.IdentityDocumentDTO;

import com.bbva.pisd.dto.insurance.utils.PISDConstants;
import com.bbva.pisd.lib.r008.PISDR008;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static org.springframework.util.StringUtils.isEmpty;

public class MapperHelper {

    private static final String WHITESPACE_CHARACTER = "";
    private static final String HYPHEN_CHARACTER = "-";
    private static final String LINE_BREAK = "\n";
    private static final String RUC_DOCUMENT = "RUC";
    private ApplicationConfigurationService applicationConfigurationService;

    private PISDR008 pisdR008;

    public IdentityDataDTO createBlackListRimacRequest(final IdentityDocumentDTO identityDocument, final String blackListType) {
        IdentityDataDTO identityData = new IdentityDataDTO();
        identityData.setNroDocumento(identityDocument.getDocumentNumber());
        identityData.setTipoDocumento(this.applicationConfigurationService.getProperty(identityDocument.getDocumentType().getId()));
        identityData.setTipoLista(blackListType);
        return identityData;
    }

    public InsuranceBlackListDTO createResponseToIneligibleCustomer(final BlackListIndicatorBO indicator) {
        InsuranceBlackListDTO responseIneligibleCustomer  = new InsuranceBlackListDTO();
        responseIneligibleCustomer.setId(indicator.getIndicatorId());
        responseIneligibleCustomer.setDescription("");
        responseIneligibleCustomer.setIsBlocked(Boolean.TRUE.equals(indicator.getIsActive()) ? PISDConstants.LETTER_SI : PISDConstants.LETTER_NO);
        return responseIneligibleCustomer;
    }

    public InsuranceBlackListDTO createResponseBlackListBBVAService(final InsuranceBlackListDTO requestBody,
                                                                    final SelectionQuotationPayloadBO rimacResponse,
                                                                    CustomerListASO customerInformation) {
        if(!PISDConstants.BLACKLIST_BLOCKED.equals(rimacResponse.getStatus())) {
            return this.validateChannels(requestBody, customerInformation);
        }
        InsuranceBlackListDTO responseBlackList = this.commonCreateResponseBlackList(requestBody);
        responseBlackList.setIsBlocked(PISDConstants.LETTER_SI);
        responseBlackList.setDescription(rimacResponse.getMensaje());
        return responseBlackList;
    }

    public InsuranceBlackListDTO createResponseBlackListBBVAService(final InsuranceBlackListDTO requestBody,
                                                                    final SelectionQuotationPayloadBO rimacResponse) {
        InsuranceBlackListDTO responseBlackList = this.commonCreateResponseBlackList(requestBody);
        if(PISDConstants.BLACKLIST_BLOCKED.equals(rimacResponse.getStatus())) {
            responseBlackList.setIsBlocked(PISDConstants.LETTER_SI);
            responseBlackList.setDescription(rimacResponse.getMensaje());
        } else {
            responseBlackList.setIsBlocked(PISDConstants.LETTER_NO);
            responseBlackList.setDescription("");
        }
        return responseBlackList;
    }

    /**
     * @author P030557 - This method is used when the customer is blocked due to its basic information is missing
     * @param requestBody it's required to put some information in the response object
     * @param messageValidation this message will be used to indicate that some basic customer information is missing
     * @return the object response with the detail of the blocking
     */
    private InsuranceBlackListDTO createResponseMissingCustomerInformation(final InsuranceBlackListDTO requestBody, final String messageValidation) {
        InsuranceBlackListDTO responseBlackList = this.commonCreateResponseBlackList(requestBody);
        responseBlackList.setIsBlocked(PISDConstants.LETTER_SI);
        responseBlackList.setDescription(messageValidation);
        return responseBlackList;
    }

    private InsuranceBlackListDTO validateChannels(final InsuranceBlackListDTO requestBody, CustomerListASO customerInformation) {
        String channels = this.applicationConfigurationService.getProperty("channels-to-check-custInfo");
        List<String> channelsList = asList(channels.split(HYPHEN_CHARACTER));

        String documentType = requestBody.getIdentityDocument().getDocumentType().getId();

        /* Se añade validacion de RUC */
        if(channelsList.contains(requestBody.getSaleChannelId()) && !RUC_DOCUMENT.equals(documentType)) {
            String messageValidation = this.getMessageValidation(customerInformation, requestBody);
            if(!isEmpty(messageValidation)) {
                return this.createResponseMissingCustomerInformation(requestBody, messageValidation);
            }
        }

        InsuranceBlackListDTO responseBlackList = this.commonCreateResponseBlackList(requestBody);
        responseBlackList.setIsBlocked(PISDConstants.LETTER_NO);
        responseBlackList.setDescription("");
        return responseBlackList;
    }

    private String getMessageValidation(CustomerListASO customerInformation, final InsuranceBlackListDTO requestBody) {
        if(isNull(customerInformation)) {
            customerInformation = this.pisdR008.executeGetCustomerInformation(requestBody.getCustomerId());
        }
        return this.validateMissingCustomerData(customerInformation);
    }

    private InsuranceBlackListDTO commonCreateResponseBlackList(final InsuranceBlackListDTO requestBody) {
        InsuranceBlackListDTO responseBlackList = new InsuranceBlackListDTO();

        responseBlackList.setId(requestBody.getTraceId());

        IdentityDocumentDTO identityDocument = requestBody.getIdentityDocument();
        identityDocument.setNumber(identityDocument.getDocumentNumber());

        responseBlackList.setIdentityDocument(identityDocument);

        BlackListTypeDTO blackListType = new BlackListTypeDTO();
        blackListType.setId(requestBody.getBlackListType().getId());

        responseBlackList.setBlackListType(blackListType);

        return responseBlackList;
    }

    private String validateMissingCustomerData(final CustomerListASO customerInformation){
        if(nonNull(customerInformation)) {
            CustomerBO customer = customerInformation.getData().get(0);

            List<String> validationMessages = new ArrayList<>();
            String messageValidateIdentityDocument = this.validateIdentityDocument(customer);
            if(!isEmpty(messageValidateIdentityDocument)) {
                validationMessages.add(messageValidateIdentityDocument);
            }
            String messageValidateCustomerBasicInformation = this.validateCustomerBasicInformation(customer);
            if(!isEmpty(messageValidateCustomerBasicInformation)) {
                validationMessages.add(messageValidateCustomerBasicInformation);
            }
            String messageContactDetails = this.validateContactDetails(customer);
            if(!isEmpty(messageContactDetails)) {
                validationMessages.add(messageContactDetails);
            }
            String messageAddress = this.validateAddress(customer);
            if(!isEmpty(messageAddress)) {
                validationMessages.add(messageAddress);
            }

            /* Se concatenan los mensajes de validación con salto de línea */
            if(!validationMessages.isEmpty()){
                return String.join(LINE_BREAK,validationMessages);
            }

            return WHITESPACE_CHARACTER;
        }
        return WHITESPACE_CHARACTER;
    }

    private String validateIdentityDocument(final CustomerBO customer){
        if(isEmpty(customer.getIdentityDocuments().get(0).getDocumentNumber()) ||
                isEmpty(customer.getIdentityDocuments().get(0).getDocumentType().getId())) {
            return this.applicationConfigurationService.getProperty("identity-document-message-key");
        } else {
            return WHITESPACE_CHARACTER;
        }
    }

    private String validateCustomerBasicInformation(final CustomerBO customer){
        if(isEmpty(customer.getFirstName()) || isEmpty(customer.getLastName())) {
            return this.applicationConfigurationService.getProperty("customer-name-message-key");
        }
        return isEmpty(customer.getGender().getId()) ?
                this.applicationConfigurationService.getProperty("gender-message-key") : WHITESPACE_CHARACTER;
    }

    private String validateContactDetails(final CustomerBO customer){

        Map<String, String> contactDetails = customer.getContactDetails().
                stream().
                filter(contactDetail -> nonNull(contactDetail.getContactType().getId())).
                collect(groupingBy(
                        contactDetail -> contactDetail.getContactType().getId(),
                        mapping(ContactDetailsBO::getContact, new SingletonStringCollector())
                ));

        boolean isntSomeContactDetailMissing =  contactDetails.entrySet().stream().noneMatch(entry -> isEmpty(entry.getValue()));

        if(!contactDetails.isEmpty() && isntSomeContactDetailMissing) {
            return WHITESPACE_CHARACTER;
        } else {
            return this.applicationConfigurationService.getProperty("contact-detail-message-key");
        }
    }

    private String validateAddress(final CustomerBO customer){
        Map<String, String> location = customer.getAddresses().get(0).getLocation().getGeographicGroups().
                stream().
                filter(geographicGroupsBO -> addressKey(geographicGroupsBO.getGeographicGroupType().getId())).
                collect(groupingBy(
                        geographicGroup -> geographicGroup.getGeographicGroupType().getName(),
                        mapping(GeographicGroupsBO::getName, new SingletonStringCollector())
                ));

        boolean isntSomeAddressInformationMissing = location.entrySet().stream().noneMatch(entry -> isEmpty(entry.getValue()));

        if(!location.isEmpty() && isntSomeAddressInformationMissing) {
            return WHITESPACE_CHARACTER;
        } else {
            return this.applicationConfigurationService.getProperty("address-message-key");
        }
    }

    private boolean addressKey(final String geographicGroupType) {
        return "DEPARTMENT".equals(geographicGroupType) || "PROVINCE".equals(geographicGroupType) || "DISTRICT".equals(geographicGroupType);
    }

    public void setApplicationConfigurationService(ApplicationConfigurationService applicationConfigurationService) {
        this.applicationConfigurationService = applicationConfigurationService;
    }

    public void setPisdR008(PISDR008 pisdR008) {
        this.pisdR008 = pisdR008;
    }

}