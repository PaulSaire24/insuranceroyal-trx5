package com.bbva.pisd.lib.r018.impl.util;

import com.bbva.elara.configuration.manager.application.ApplicationConfigurationService;

import com.bbva.pisd.dto.insurance.aso.CustomerListASO;

import com.bbva.pisd.dto.insurance.blacklist.BlackListTypeDTO;
import com.bbva.pisd.dto.insurance.blacklist.InsuranceBlackListDTO;

import com.bbva.pisd.dto.insurance.bo.BlackListIndicatorBO;
import com.bbva.pisd.dto.insurance.bo.LocationBO;
import com.bbva.pisd.dto.insurance.bo.ContactDetailsBO;
import com.bbva.pisd.dto.insurance.bo.SelectionQuotationPayloadBO;

import com.bbva.pisd.dto.insurance.bo.customer.CustomerBO;

import com.bbva.pisd.dto.insurance.commons.IdentityDataDTO;
import com.bbva.pisd.dto.insurance.commons.IdentityDocumentDTO;

import com.bbva.pisd.dto.insurance.utils.PISDConstants;
import com.bbva.pisd.lib.r008.PISDR008;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static org.springframework.util.StringUtils.isEmpty;
import static org.springframework.util.StringUtils.stripFilenameExtension;

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
                                                                    CustomerBO customerInformation) {
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

    private InsuranceBlackListDTO validateChannels(final InsuranceBlackListDTO requestBody, CustomerBO customerInformation) {
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

    private String getMessageValidation(CustomerBO customerInformation, final InsuranceBlackListDTO requestBody) {
        if(isNull(customerInformation)) {
            customerInformation = this.pisdR008.executeGetCustomerHost(requestBody.getCustomerId());
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

    private String validateMissingCustomerData(final CustomerBO customerInformation){
        if(nonNull(customerInformation)) {
            CustomerBO customer = customerInformation;

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
                final String missingInformation = String.join(LINE_BREAK,validationMessages);
                final String introductionMessage = this.applicationConfigurationService.getProperty("introduction-message");
                final String closingMessage = this.applicationConfigurationService.getProperty("closing-message");
                return introductionMessage + LINE_BREAK + missingInformation + LINE_BREAK + closingMessage;
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
        StringBuilder message = new StringBuilder();
        if(isEmpty(customer.getFirstName()) || isEmpty(customer.getLastName())) {
            final String nameMessage = this.applicationConfigurationService.getProperty("customer-name-message-key");
            message.append(nameMessage);
        }
        if(isEmpty(customer.getGender().getId())) {
            final String genderMessage = this.applicationConfigurationService.getProperty("gender-message-key");
            if(message.length() != 0) message.append(LINE_BREAK);
            message.append(genderMessage);
        }
        return message.toString();
    }

    private String validateContactDetails(final CustomerBO customer){

        Map<String, String> contactDetailsEmail = customer.getContactDetails().
                stream().
                filter(contactDetail -> "EMAIL".equalsIgnoreCase(contactDetail.getContactType().getId())).
                filter(this::validateMail).
                collect(groupingBy(
                        contactDetail -> contactDetail.getContactType().getId(),
                        mapping(ContactDetailsBO::getContact, new SingletonStringCollector())
                ));

        Map<String, String> contactDetailsPhone = customer.getContactDetails().
                stream().
                filter(contactDetail -> "MOBILE_NUMBER".equalsIgnoreCase(contactDetail.getContactType().getId())  && nonNull(contactDetail.getContact())).
                filter(this::validatePhone).
                collect(groupingBy(
                        contactDetail -> contactDetail.getContactType().getId(),
                        mapping(ContactDetailsBO::getContact, new SingletonStringCollector())
                ));
        StringBuilder message = new StringBuilder();

        if(isEmpty(contactDetailsPhone.get("MOBILE_NUMBER"))) {
            final String cellphoneMessage = this.applicationConfigurationService.getProperty("cellphone-message-key");
            message.append(cellphoneMessage);
        }

        if(isEmpty(contactDetailsEmail.get("EMAIL"))) {
            final String emailMessge = this.applicationConfigurationService.getProperty("email-message-key");
            if(message.length() != 0) message.append(LINE_BREAK);
            message.append(emailMessge);
        }

        return message.toString();
    }

    private boolean validateMail(ContactDetailsBO mail) {
        Pattern pattern = Pattern.compile(this.applicationConfigurationService.getProperty("regex-email"));
        Matcher matcher = pattern.matcher(StringUtils.defaultIfEmpty(mail.getContact(),""));
        return matcher.find();
    }

    private boolean validatePhone(ContactDetailsBO phone) {
        Pattern pattern = Pattern.compile(this.applicationConfigurationService.getProperty("regex-phone"));
        Matcher matcher = pattern.matcher(StringUtils.defaultIfEmpty(phone.getContact(),""));
        return matcher.find();
    }

    private String validateAddress(final CustomerBO customer){

        final String message = this.applicationConfigurationService.getProperty("address-message-key");

        final String defaultValue = "xdepurar";

        LocationBO customerLocation = customer.getAddresses().get(0).getLocation();

        if(CollectionUtils.isEmpty(customerLocation.getGeographicGroups()) ||
                defaultValue.equalsIgnoreCase(customerLocation.getGeographicGroups().get(0).getName())) {
            return message;
        } else {
            return WHITESPACE_CHARACTER;
        }

    }

    public void setApplicationConfigurationService(ApplicationConfigurationService applicationConfigurationService) {
        this.applicationConfigurationService = applicationConfigurationService;
    }

    public void setPisdR008(PISDR008 pisdR008) {
        this.pisdR008 = pisdR008;
    }

}
