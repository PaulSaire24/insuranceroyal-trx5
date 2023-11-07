package com.bbva.pisd.lib.r018.util;

import com.bbva.elara.configuration.manager.application.ApplicationConfigurationService;

import com.bbva.pisd.dto.insurance.aso.CustomerListASO;
import com.bbva.pisd.dto.insurance.aso.GetContactDetailsASO;
import com.bbva.pisd.dto.insurance.blacklist.BlackListTypeDTO;
import com.bbva.pisd.dto.insurance.blacklist.InsuranceBlackListDTO;

import com.bbva.pisd.dto.insurance.bo.BlackListIndicatorBO;

import com.bbva.pisd.dto.insurance.bo.BlackListRiskRimacBO;
import com.bbva.pisd.dto.insurance.bo.SelectionQuotationPayloadBO;
import com.bbva.pisd.dto.insurance.bo.customer.CustomerBO;
import com.bbva.pisd.dto.insurance.commons.DocumentTypeDTO;
import com.bbva.pisd.dto.insurance.commons.IdentityDataDTO;
import com.bbva.pisd.dto.insurance.commons.IdentityDocumentDTO;

import com.bbva.pisd.dto.insurance.mock.MockDTO;

import com.bbva.pisd.dto.insurance.utils.PISDConstants;

import com.bbva.pisd.lib.r008.PISDR008;
import com.bbva.pisd.lib.r018.EntityMock;
import com.bbva.pisd.lib.r018.impl.util.MapperHelper;

import com.bbva.rbvd.lib.r046.RBVDR046;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MapperHelperTest {

    private final MapperHelper mapperHelper = new MapperHelper();
    private final String introductionMessage = "something went wrong:";
    private final String closingMessage = "please solve the problem.";
    private ApplicationConfigurationService applicationConfigurationService;
    private PISDR008 pisdR008;
    private RBVDR046 rbvdr046;
    private MockDTO mockDTO;
    private InsuranceBlackListDTO insuranceBlackList;
    private EntityMock entityMock;
    private CustomerBO customerInformation;
    private CustomerListASO customerInformation1;
    private SelectionQuotationPayloadBO rimacNegativeResponse;


    @Before
    public void setUp() throws IOException {
        applicationConfigurationService = mock(ApplicationConfigurationService.class);
        mapperHelper.setApplicationConfigurationService(applicationConfigurationService);

        when(this.applicationConfigurationService.getProperty("channels-to-check-custInfo")).thenReturn("PC-BI");

        pisdR008 = mock(PISDR008.class);
        mapperHelper.setPisdR008(pisdR008);

        rbvdr046 = mock(RBVDR046.class);
        mapperHelper.setRbvdR046(rbvdr046);

        mockDTO = MockDTO.getInstance();
        entityMock=EntityMock.getInstance();
        insuranceBlackList = new InsuranceBlackListDTO();
        insuranceBlackList.setBlackListType(new BlackListTypeDTO("blackListId"));
        insuranceBlackList.setIdentityDocument(new IdentityDocumentDTO(new DocumentTypeDTO("L"), "documentNumber"));
        insuranceBlackList.setTraceId("traceId");

        rimacNegativeResponse = mockDTO.getBlackListValidationNegativeRimacMockResponse();
        customerInformation1= mockDTO.getCustomerDataResponse();
        customerInformation= entityMock.getCustomerDataResponseBO();

        List<ContactDetailsBO> contactDetailsBO = new ArrayList<>();
        GetContactDetailsASO contactDetailsASO = new GetContactDetailsASO();
        contactDetailsASO.setData(contactDetailsBO);

        when(this.applicationConfigurationService.getProperty("introduction-message")).thenReturn(introductionMessage);
        when(this.applicationConfigurationService.getProperty("closing-message")).thenReturn(closingMessage);
        when(this.applicationConfigurationService.getProperty("regex-email")).
                thenReturn("^[a-zA-Z0-9.!#$%&'*+\\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");
        when(this.applicationConfigurationService.getProperty("regex-phone")).
                thenReturn("^[0-9]{1,13}+$");
        when(rbvdr046.executeGetContactDetailsService(anyString())).thenReturn(contactDetailsASO);
    }

    @Test
    public void createBlackListRimacRequest_OK() {
        IdentityDocumentDTO identityDocument = new IdentityDocumentDTO();
        identityDocument.setDocumentNumber("documentNumber");
        identityDocument.setDocumentType(new DocumentTypeDTO("DNI"));

        when(this.applicationConfigurationService.getProperty("DNI")).thenReturn("L");

        String blackListType = "blackListType";

        IdentityDataDTO validation = this.mapperHelper.createBlackListRimacRequest(identityDocument, blackListType);

        assertNotNull(validation.getNroDocumento());
        assertNotNull(validation.getTipoDocumento());
        assertNotNull(validation.getTipoLista());

        assertEquals(identityDocument.getDocumentNumber(), validation.getNroDocumento());
        assertEquals("L", validation.getTipoDocumento());
        assertEquals(blackListType, validation.getTipoLista());
    }

    @Test
    public void createResponseToIneligibleCustomer_OK() {
        BlackListIndicatorBO blackListIndicator = new BlackListIndicatorBO();
        blackListIndicator.setIndicatorId("indicatorId");
        blackListIndicator.setIsActive(true);

        InsuranceBlackListDTO validation = this.mapperHelper.createResponseToIneligibleCustomer(blackListIndicator);

        assertNotNull(validation.getId());
        assertNotNull(validation.getDescription());
        assertNotNull(validation.getIsBlocked());

        assertEquals(blackListIndicator.getIndicatorId(), validation.getId());
        assertEquals("", validation.getDescription());
        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());

        blackListIndicator.setIsActive(false);

        validation = this.mapperHelper.createResponseToIneligibleCustomer(blackListIndicator);

        assertEquals(PISDConstants.LETTER_NO, validation.getIsBlocked());
    }

    @Test
    public void createResponseBlackListServiceHealthProduct_OK() throws IOException {

        BlackListRiskRimacBO rimacResponse = mockDTO.getBlackListRiskRimacMockResponse();
        SelectionQuotationPayloadBO selectionQuotationPayload = rimacResponse.getPayload().get(0);

        //CLIENT AVAILABLE CASE
        InsuranceBlackListDTO validation = this.mapperHelper.createResponseBlackListBBVAService(this.insuranceBlackList, selectionQuotationPayload);

        assertNotNull(validation.getId());
        assertNotNull(validation.getIdentityDocument());
        assertNotNull(validation.getIdentityDocument().getDocumentType());
        assertNotNull(validation.getIdentityDocument().getDocumentType().getId());
        assertNotNull(validation.getIdentityDocument().getNumber());
        assertNotNull(validation.getBlackListType());
        assertNotNull(validation.getBlackListType().getId());
        assertNotNull(validation.getIsBlocked());
        assertNotNull(validation.getDescription());

        assertEquals(this.insuranceBlackList.getTraceId(), validation.getId());
        assertEquals(this.insuranceBlackList.getIdentityDocument().getDocumentNumber(),
                validation.getIdentityDocument().getNumber());
        assertEquals(this.insuranceBlackList.getBlackListType().getId(),
                validation.getBlackListType().getId());
        assertEquals(PISDConstants.LETTER_NO, validation.getIsBlocked());
        assertEquals("", validation.getDescription());

        selectionQuotationPayload = mockDTO.getBlackListValidationPositiveRimacMockResponse();

        //CLIENT UNAVAILABLE CASE
        validation = this.mapperHelper.createResponseBlackListBBVAService(this.insuranceBlackList, selectionQuotationPayload);

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(selectionQuotationPayload.getMensaje(), validation.getDescription());
    }

    @Test
    public void createResponseBlackListBBVAServiceOtherProducts_ClientUnavailable() throws IOException {
        SelectionQuotationPayloadBO rimacResponse = this.mockDTO.getBlackListValidationPositiveRimacMockResponse();

        InsuranceBlackListDTO validation = this.mapperHelper.createResponseBlackListBBVAService(this.insuranceBlackList, rimacResponse, null);

        assertNotNull(validation.getIsBlocked());
        assertNotNull(validation.getDescription());

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(rimacResponse.getMensaje(), validation.getDescription());
    }

    @Test
    public void createResponseBlackListBBVAServiceOtherProducts_ClientAvailable_WithoutChannelValidation() {
        this.insuranceBlackList.setSaleChannelId("TM");

        InsuranceBlackListDTO validation = this.mapperHelper.createResponseBlackListBBVAService(this.insuranceBlackList, rimacNegativeResponse, null);

        assertNotNull(validation.getIsBlocked());
        assertNotNull(validation.getDescription());

        assertEquals(PISDConstants.LETTER_NO, validation.getIsBlocked());
        assertEquals("", validation.getDescription());
    }

    @Test
    public void createResponseBlackListBBVAServiceOtherProducts_ClientAvailable_RucDocument() {
        this.insuranceBlackList.setSaleChannelId("PC");

        this.insuranceBlackList.getIdentityDocument().getDocumentType().setId("RUC");

        InsuranceBlackListDTO validation = this.mapperHelper.createResponseBlackListBBVAService(this.insuranceBlackList, rimacNegativeResponse, null);

        assertNotNull(validation.getIsBlocked());
        assertNotNull(validation.getDescription());

        assertEquals(PISDConstants.LETTER_NO, validation.getIsBlocked());
        assertEquals("", validation.getDescription());
    }



    @Test
    public void createResponseBlackListBBVAServiceOtherProducts_ClientUnavailable_MissingCustomerBasicInformation() {
        String nameValidation = "Revisar nombres completos";

        when(this.applicationConfigurationService.getProperty("customer-name-message-key")).
                thenReturn(nameValidation);

        String genderValidation = "Revisar sexo";

        when(this.applicationConfigurationService.getProperty("gender-message-key")).
                thenReturn(genderValidation);

        String finalMessage = "";

        this.insuranceBlackList.setSaleChannelId("PC");

        this.customerInformation.setFirstName(null);

        finalMessage = introductionMessage + "\n" + nameValidation + "\n" + closingMessage;

        //Missing first name
        InsuranceBlackListDTO validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertNotNull(validation.getIsBlocked());
        assertNotNull(validation.getDescription());

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(finalMessage, validation.getDescription());

        this.customerInformation.setFirstName("firstName");
        this.customerInformation.setLastName(null);

        //Missing last name
        validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(finalMessage, validation.getDescription());

        this.customerInformation.getGender().setId(null);

        //Missing name and gender
        validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        finalMessage = introductionMessage + "\n" + nameValidation + "\n" + genderValidation + "\n" + closingMessage;

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(finalMessage, validation.getDescription());
    }

    @Test
    public void createResponseBlackListBBVAServiceOtherProducts_ClientUnavailable_MissingContactDetails() {
        String cellPhoneValidation = "Revisar Celular";

        when(this.applicationConfigurationService.getProperty("cellphone-message-key")).
                thenReturn(cellPhoneValidation);

        String emailValidation = "Revisar Correo";

        when(this.applicationConfigurationService.getProperty("email-message-key")).
                thenReturn(emailValidation);

        String finalMessage = "";

        this.insuranceBlackList.setSaleChannelId("PC");

        this.customerInformation.getContactDetails().get(1).setContact(null);

        finalMessage = introductionMessage + "\n" + cellPhoneValidation + "\n" + closingMessage;

        //Missing mobile number
        InsuranceBlackListDTO validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertNotNull(validation.getIsBlocked());
        assertNotNull(validation.getDescription());

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(finalMessage, validation.getDescription());

        this.customerInformation.getContactDetails().get(2).setContact(null);

        finalMessage = introductionMessage + "\n" + cellPhoneValidation + "\n" + emailValidation + "\n" + closingMessage;

        //Missing mobile number and email
        validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(finalMessage, validation.getDescription());

        //Empty contactDetails
        this.customerInformation.getContactDetails().get(1).getContactType().setId(null);
        this.customerInformation.getContactDetails().get(2).getContactType().setId(null);

        validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(finalMessage, validation.getDescription());
    }




    @Test
    public void createResponseBlackListBBVAServiceOtherProducts_ClientUnavailable_MissingCustomerBasicInformation_AndAddress() {

        String genderMessage = "Revisar sexo";

        when(this.applicationConfigurationService.getProperty("gender-message-key")).
                thenReturn(genderMessage);

        String addressMessage = "Revisar datos de direccion";

        when(this.applicationConfigurationService.getProperty("address-message-key"))
                .thenReturn(addressMessage);

        String finalMessage = "";

        this.insuranceBlackList.setSaleChannelId("PC");

        this.customerInformation.getGender().setId(null);
        this.customerInformation.getAddresses().get(0).getLocation().setGeographicGroups(null);

        finalMessage = introductionMessage + "\n" + genderMessage + "\n" + addressMessage + "\n" + closingMessage;

        //Missing gender and address
        InsuranceBlackListDTO validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertNotNull(validation.getIsBlocked());
        assertNotNull(validation.getDescription());

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(finalMessage, validation.getDescription());
    }

    @Test
    public void createResponseBlackListBBVAServiceOtherProducts_ClientUnavailable_MissingAllRequiredCustomerInformation() {

        String documentMessage = "Revisar nro de documento";

        when(this.applicationConfigurationService.getProperty("identity-document-message-key")).
                thenReturn(documentMessage);

        String genderMessage = "Revisar sexo";

        when(this.applicationConfigurationService.getProperty("gender-message-key")).
                thenReturn(genderMessage);

        String addressMessage = "Revisar datos de direccion";

        when(this.applicationConfigurationService.getProperty("address-message-key"))
                .thenReturn(addressMessage);

        String cellPhoneMessage = "Revisar datos de contacto";

        when(this.applicationConfigurationService.getProperty("cellphone-message-key")).
                thenReturn(cellPhoneMessage);

        String finalMessage = "";

        this.insuranceBlackList.setSaleChannelId("PC");

        this.customerInformation.getIdentityDocuments().get(0).setDocumentNumber(null);
        this.customerInformation.getGender().setId(null);
        this.customerInformation.getContactDetails().get(1).setContact(null);
        this.customerInformation.getAddresses().get(0).getLocation().setGeographicGroups(null);

        finalMessage = introductionMessage + "\n" + documentMessage + "\n" +
                genderMessage + "\n" + cellPhoneMessage + "\n" + addressMessage + "\n" + closingMessage;

        //Missing document number, gender, contact details and address
        InsuranceBlackListDTO validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertNotNull(validation.getIsBlocked());
        assertNotNull(validation.getDescription());

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(finalMessage, validation.getDescription());
    }

    @Test
    public void createResponseBlackListBBVAServiceOtherProducts_ClientAvailable() {
        this.insuranceBlackList.setSaleChannelId("PC");

        when(this.pisdR008.executeGetCustomerInformation(anyString())).thenReturn(this.customerInformation1);

        //invoking listCustomerInformation
        InsuranceBlackListDTO validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, null);

        assertNotNull(validation.getIsBlocked());
        assertNotNull(validation.getDescription());

        assertEquals(PISDConstants.LETTER_NO, validation.getIsBlocked());
        assertEquals("", validation.getDescription());

        when(this.pisdR008.executeGetCustomerInformation(anyString())).thenReturn(null);

        //null listCustomerInformation response
        validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, null);

        assertEquals(PISDConstants.LETTER_NO, validation.getIsBlocked());
        assertEquals("", validation.getDescription());
    }

    @Test
    public void createResponseBlackListBBVAServiceOtherProducts_ClientUnavailable_BadFormatPhoneContactDetails() {
        String cellPhoneValidation = "Revisar Celular";

        when(this.applicationConfigurationService.getProperty("cellphone-message-key")).
                thenReturn(cellPhoneValidation);

        String finalMessage = "";

        this.insuranceBlackList.setSaleChannelId("PC");

        this.customerInformation.getData().get(0).getContactDetails().get(1).setContact("ABC");

        finalMessage = introductionMessage + "\n" + cellPhoneValidation + "\n" + closingMessage;

        //Missing mobile number
        InsuranceBlackListDTO validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertNotNull(validation.getIsBlocked());
        assertNotNull(validation.getDescription());

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(finalMessage, validation.getDescription());
    }

    @Test
    public void createResponseBlackListBBVAServiceOtherProducts_ClientUnavailable_BadFormatEmailContactDetails() {

        String emailValidation = "Revisar Correo";

        when(this.applicationConfigurationService.getProperty("email-message-key")).
                thenReturn(emailValidation);

        String finalMessage = "";

        this.insuranceBlackList.setSaleChannelId("PC");

        this.customerInformation.getData().get(0).getContactDetails().get(2).setContact("NESTOR257");

        finalMessage = introductionMessage + "\n" + emailValidation + "\n" + closingMessage;

        //Missing mobile number and email
        InsuranceBlackListDTO validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(finalMessage, validation.getDescription());

    }

    @Test
    public void createResponseBlackListBBVAServiceOtherProducts_ClientUnavailable_BadFormatEmailAndPhoneContactDetails() {
        String cellPhoneValidation = "Revisar Celular";

        when(this.applicationConfigurationService.getProperty("cellphone-message-key")).
                thenReturn(cellPhoneValidation);

        String emailValidation = "Revisar Correo";

        when(this.applicationConfigurationService.getProperty("email-message-key")).
                thenReturn(emailValidation);

        String finalMessage = "";

        this.insuranceBlackList.setSaleChannelId("PC");

        finalMessage = introductionMessage + "\n" + cellPhoneValidation + "\n" + emailValidation + "\n" + closingMessage;

        //Empty contactDetails
        this.customerInformation.getData().get(0).getContactDetails().get(1).getContactType().setId(null);
        this.customerInformation.getData().get(0).getContactDetails().get(2).getContactType().setId(null);

        InsuranceBlackListDTO validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(finalMessage, validation.getDescription());
    }

}
