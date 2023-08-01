package com.bbva.pisd.lib.r018.util;

import com.bbva.elara.configuration.manager.application.ApplicationConfigurationService;

import com.bbva.pisd.dto.insurance.aso.CustomerListASO;
import com.bbva.pisd.dto.insurance.blacklist.BlackListTypeDTO;
import com.bbva.pisd.dto.insurance.blacklist.InsuranceBlackListDTO;

import com.bbva.pisd.dto.insurance.bo.BlackListIndicatorBO;

import com.bbva.pisd.dto.insurance.bo.BlackListRiskRimacBO;
import com.bbva.pisd.dto.insurance.bo.SelectionQuotationPayloadBO;
import com.bbva.pisd.dto.insurance.commons.DocumentTypeDTO;
import com.bbva.pisd.dto.insurance.commons.IdentityDataDTO;
import com.bbva.pisd.dto.insurance.commons.IdentityDocumentDTO;

import com.bbva.pisd.dto.insurance.mock.MockDTO;

import com.bbva.pisd.dto.insurance.utils.PISDConstants;

import com.bbva.pisd.lib.r008.PISDR008;
import com.bbva.pisd.lib.r018.impl.util.MapperHelper;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MapperHelperTest {

    private final MapperHelper mapperHelper = new MapperHelper();
    private ApplicationConfigurationService applicationConfigurationService;
    private PISDR008 pisdR008;

    private MockDTO mockDTO;
    private InsuranceBlackListDTO insuranceBlackList;
    private CustomerListASO customerInformation;

    private SelectionQuotationPayloadBO rimacNegativeResponse;


    @Before
    public void setUp() throws IOException {
        applicationConfigurationService = mock(ApplicationConfigurationService.class);
        mapperHelper.setApplicationConfigurationService(applicationConfigurationService);

        when(this.applicationConfigurationService.getProperty("channels-to-check-custInfo")).thenReturn("PC-BI");

        pisdR008 = mock(PISDR008.class);
        mapperHelper.setPisdR008(pisdR008);

        mockDTO = MockDTO.getInstance();

        insuranceBlackList = new InsuranceBlackListDTO();
        insuranceBlackList.setBlackListType(new BlackListTypeDTO("blackListId"));
        insuranceBlackList.setIdentityDocument(new IdentityDocumentDTO(new DocumentTypeDTO("L"), "documentNumber"));
        insuranceBlackList.setTraceId("traceId");

        rimacNegativeResponse = mockDTO.getBlackListValidationNegativeRimacMockResponse();

        customerInformation = mockDTO.getCustomerDataResponse();
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
    public void createResponseBlackListBBVAServiceOtherProducts_ClientUnavailable_MissingIdentityDocument() {
        String messageValidation = "Revisar nro de documento";

        when(this.applicationConfigurationService.getProperty("identity-document-message-key")).
                thenReturn(messageValidation);

        this.insuranceBlackList.setSaleChannelId("PC");

        this.customerInformation.getData().get(0).getIdentityDocuments().get(0).setDocumentNumber(null);

        //Missing document number
        InsuranceBlackListDTO validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertNotNull(validation.getIsBlocked());
        assertNotNull(validation.getDescription());

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(messageValidation, validation.getDescription());

        this.customerInformation.getData().get(0).getIdentityDocuments().get(0).setDocumentNumber("documentNumber");
        this.customerInformation.getData().get(0).getIdentityDocuments().get(0).getDocumentType().setId(null);

        //Missing document type
        validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(messageValidation, validation.getDescription());
    }

    @Test
    public void createResponseBlackListBBVAServiceOtherProducts_ClientUnavailable_MissingCustomerBasicInformation() {
        String messageValidation = "Revisar nombres completos";

        when(this.applicationConfigurationService.getProperty("customer-name-message-key")).
                thenReturn(messageValidation);

        this.insuranceBlackList.setSaleChannelId("PC");

        this.customerInformation.getData().get(0).setFirstName(null);

        //Missing first name
        InsuranceBlackListDTO validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertNotNull(validation.getIsBlocked());
        assertNotNull(validation.getDescription());

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(messageValidation, validation.getDescription());

        this.customerInformation.getData().get(0).setFirstName("firstName");
        this.customerInformation.getData().get(0).setLastName(null);

        //Missing last name
        validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(messageValidation, validation.getDescription());

        messageValidation = "Revisar sexo";

        when(this.applicationConfigurationService.getProperty("gender-message-key")).
                thenReturn(messageValidation);

        this.customerInformation.getData().get(0).setFirstName("firstName");
        this.customerInformation.getData().get(0).setLastName("lastName");
        this.customerInformation.getData().get(0).getGender().setId(null);

        //Missing gender
        validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(messageValidation, validation.getDescription());
    }

    @Test
    public void createResponseBlackListBBVAServiceOtherProducts_ClientUnavailable_MissingContactDetails() {
        String messageValidation = "Revisar datos de contacto";

        when(this.applicationConfigurationService.getProperty("contact-detail-message-key")).
                thenReturn(messageValidation);

        this.insuranceBlackList.setSaleChannelId("PC");

        this.customerInformation.getData().get(0).getContactDetails().get(1).setContact(null);

        //Missing mobile number
        InsuranceBlackListDTO validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertNotNull(validation.getIsBlocked());
        assertNotNull(validation.getDescription());

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(messageValidation, validation.getDescription());

        this.customerInformation.getData().get(0).getContactDetails().get(1).setContact("999888777");
        this.customerInformation.getData().get(0).getContactDetails().get(2).setContact(null);

        //Missing email
        validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(messageValidation, validation.getDescription());

        //Empty contactDetails
        this.customerInformation.getData().get(0).getContactDetails().get(1).getContactType().setId(null);
        this.customerInformation.getData().get(0).getContactDetails().get(2).getContactType().setId(null);

        validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(messageValidation, validation.getDescription());
    }

    @Test
    public void createResponseBlackListBBVAServiceOtherProducts_ClientUnavailable_MissingAddress() {
        String messageValidation = "Revisar datos de direccion";

        when(this.applicationConfigurationService.getProperty("address-message-key"))
                .thenReturn(messageValidation);

        this.insuranceBlackList.setSaleChannelId("PC");

        this.customerInformation.getData().get(0).getAddresses().get(0).getLocation().getGeographicGroups().get(2).setName(null);

        //Missing department
        InsuranceBlackListDTO validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertNotNull(validation.getIsBlocked());
        assertNotNull(validation.getDescription());

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(messageValidation, validation.getDescription());

        this.customerInformation.getData().get(0).getAddresses().get(0).getLocation().getGeographicGroups().get(2).setName("LIMA");
        this.customerInformation.getData().get(0).getAddresses().get(0).getLocation().getGeographicGroups().get(3).setName(null);

        //Missing province
        validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(messageValidation, validation.getDescription());

        this.customerInformation.getData().get(0).getAddresses().get(0).getLocation().getGeographicGroups().get(2).setName("LIMA");
        this.customerInformation.getData().get(0).getAddresses().get(0).getLocation().getGeographicGroups().get(3).setName("LIMA");
        this.customerInformation.getData().get(0).getAddresses().get(0).getLocation().getGeographicGroups().get(4).setName(null);

        //Missing district
        validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(messageValidation, validation.getDescription());

        this.customerInformation.getData().get(0).getAddresses().get(0).getLocation().getGeographicGroups().get(2).getGeographicGroupType().setId("UNCATEGORIZED");
        this.customerInformation.getData().get(0).getAddresses().get(0).getLocation().getGeographicGroups().get(3).getGeographicGroupType().setId("UNCATEGORIZED");
        this.customerInformation.getData().get(0).getAddresses().get(0).getLocation().getGeographicGroups().get(4).getGeographicGroupType().setId("UNCATEGORIZED");

        //Empty address
        validation = this.mapperHelper.
                createResponseBlackListBBVAService(this.insuranceBlackList, this.rimacNegativeResponse, this.customerInformation);

        assertEquals(PISDConstants.LETTER_SI, validation.getIsBlocked());
        assertEquals(messageValidation, validation.getDescription());
    }

    @Test
    public void createResponseBlackListBBVAServiceOtherProducts_ClientAvailable() {
        this.insuranceBlackList.setSaleChannelId("PC");

        when(this.pisdR008.executeGetCustomerInformation(anyString())).thenReturn(this.customerInformation);

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


}
