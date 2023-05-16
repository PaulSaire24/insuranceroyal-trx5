package com.bbva.pisd.lib.r008;

import com.bbva.elara.domain.transaction.Context;
import com.bbva.elara.domain.transaction.ThreadContext;

import com.bbva.elara.utility.api.connector.APIConnector;
import com.bbva.pisd.dto.insurance.amazon.SignatureAWS;
import com.bbva.pisd.dto.insurance.aso.BlackListASO;
import com.bbva.pisd.dto.insurance.aso.CustomerListASO;
import com.bbva.pisd.dto.insurance.bo.BlackListHealthRimacBO;
import com.bbva.pisd.dto.insurance.bo.BlackListIndicatorBO;
import com.bbva.pisd.dto.insurance.bo.BlackListRiskRimacBO;
import com.bbva.pisd.dto.insurance.bo.SelectionQuotationPayloadBO;
import com.bbva.pisd.dto.insurance.commons.IdentityDataDTO;
import com.bbva.pisd.dto.insurance.mock.MockDTO;
import com.bbva.pisd.lib.r008.factory.ApiConnectorFactoryMock;
import com.bbva.pisd.lib.r008.impl.PISDR008Impl;
import com.bbva.pisd.lib.r014.PISDR014;
import com.bbva.pisd.mock.MockBundleContext;
import com.bbva.pisd.mock.MockService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/META-INF/spring/PISDR008-app.xml",
		"classpath:/META-INF/spring/PISDR008-app-test.xml",
		"classpath:/META-INF/spring/PISDR008-arc.xml",
		"classpath:/META-INF/spring/PISDR008-arc-test.xml" })
public class PISDR008Test {

	private static final Logger LOGGER = LoggerFactory.getLogger(PISDR008Test.class);

	private PISDR008Impl pisdr008 = new PISDR008Impl();

	private PISDR014 pisdr014;

	private MockDTO mockDTO;
	private MockService mockService;
	private APIConnector internalApiConnector;
	private APIConnector externalApiConnector;

	private BlackListASO responseBlackListASO;
	private BlackListHealthRimacBO responseBlackListHealthRimac;
	private BlackListRiskRimacBO responseBlackListRiskRimac;

	private BlackListRiskRimacBO responseBlackListRiskEasyYesRimac;

	private CustomerListASO customerList;
	private static final String MESSAGE_EXCEPTION = "CONNECTION ERROR";
	@Before
	public void setUp() throws IOException {
		ThreadContext.set(new Context());

		MockBundleContext mockBundleContext = mock(MockBundleContext.class);

		ApiConnectorFactoryMock apiConnectorFactoryMock = new ApiConnectorFactoryMock();
		internalApiConnector = apiConnectorFactoryMock.getAPIConnector(mockBundleContext);
		pisdr008.setInternalApiConnector(internalApiConnector);

		externalApiConnector = apiConnectorFactoryMock.getAPIConnector(mockBundleContext, false);
		pisdr008.setExternalApiConnector(externalApiConnector);

		mockService = mock(MockService.class);
		pisdr008.setMockService(mockService);

		pisdr014 = mock(PISDR014.class);
		pisdr008.setPisdR014(pisdr014);

		mockDTO = MockDTO.getInstance();

		responseBlackListASO = mockDTO.getBlackListASOMockResponse();
		responseBlackListHealthRimac = mockDTO.getBlackListHealthRimacMockResponse();
		responseBlackListRiskRimac = mockDTO.getBlackListRiskRimacMockResponse();
		responseBlackListRiskEasyYesRimac = mockDTO.getDataResponseBlackListRiskRimac();
		customerList = mockDTO.getCustomerDataResponse();
		when(pisdr014.executeSignatureConstruction(anyString(), anyString(), anyString(), anyString(), anyString()))
				.thenReturn(new SignatureAWS("", "", "", ""));
	}

	@Test
	public void executeGetBlackListIndicatorServiceTestNull() {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListIndicatorServiceTestNull...");
		when(mockService.isEnabled(anyString())).thenReturn(false);
		when(internalApiConnector.getForObject(anyString(), any(), anyMap())).thenReturn(null);
		BlackListIndicatorBO validation = pisdr008.executeGetBlackListIndicatorService(null);
		assertNull(validation);

		validation = pisdr008.executeGetBlackListIndicatorService("00000000");
		assertNull(validation);

		when(internalApiConnector.getForObject(anyString(), any(), anyMap())).thenReturn(new BlackListASO());
		validation = pisdr008.executeGetBlackListIndicatorService("00000000");
		assertNull(validation);
	}

	@Test
	public void executeGetBlackListIndicatorServiceTestWithMockData() throws IOException {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListIndicatorServiceTestWithMockData...");

		when(mockService.isEnabled(anyString())).thenReturn(true);
		when(mockService.getBlackListBBVAMock()).thenReturn(responseBlackListASO);
		BlackListIndicatorBO validation = pisdr008.executeGetBlackListIndicatorService("11111111");
		LOGGER.info("validation='" + validation + "'");
		assertNotNull(validation);
	}

	@Test
	public void executeGetBlackListRiskServiceTestNull() {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListRiskServiceTestNull...");
		when(mockService.isEnabled(anyString())).thenReturn(false);
		when(externalApiConnector.postForObject(anyString(), anyObject(), any())).thenReturn(null);
		SelectionQuotationPayloadBO validation = pisdr008.executeGetBlackListRiskService(null, "");
		assertNull(validation);

		validation = pisdr008.executeGetBlackListRiskService(new IdentityDataDTO(), "");
		assertNull(validation);

		validation = pisdr008.executeGetBlackListRiskService(new IdentityDataDTO("3" , "L", null), "");
		assertNull(validation);

		validation = pisdr008.executeGetBlackListRiskService(new IdentityDataDTO("3" , "L", "00000000"), "");
		assertNull(validation);

		BlackListRiskRimacBO request = new BlackListRiskRimacBO();
		when(externalApiConnector.postForObject(anyString(), anyObject(), any())).thenReturn(request);
		validation = pisdr008.executeGetBlackListRiskService(new IdentityDataDTO("3" , "L", "00000000"), "");
		assertNull(validation);

		request.setPayload(new ArrayList<>());
		when(externalApiConnector.postForObject(anyString(), anyObject(), any())).thenReturn(request);
		validation = pisdr008.executeGetBlackListRiskService(new IdentityDataDTO("3" , "L", "00000000"), "");
		assertNull(validation);
	}

	@Test
	public void executeGetBlackListRiskServiceTestWithMockData() {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListRiskServiceTestWithMockData...");

		when(mockService.isEnabled(anyString())).thenReturn(true);
		when(mockService.getBlackListRiskRimacMock()).thenReturn(responseBlackListRiskRimac);
		IdentityDataDTO input = new IdentityDataDTO();
		input.setNroDocumento("11111111");
		input.setTipoDocumento("L");
		input.setTipoLista("3");
		SelectionQuotationPayloadBO validation = pisdr008.executeGetBlackListRiskService(input, "");
		LOGGER.info("validation='" + validation + "'");
		assertNotNull(validation);
	}

	@Test
	public void executeGetBlackListHealthServiceTestNull() {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListHealthServiceTestNull...");
		when(mockService.isEnabled(anyString())).thenReturn(false);
		when(externalApiConnector.postForObject(anyString(), anyObject(), any())).thenReturn(null);
		SelectionQuotationPayloadBO validation = pisdr008.executeGetBlackListHealthService(null, "");
		assertNull(validation);

		validation = pisdr008.executeGetBlackListHealthService(new IdentityDataDTO(), "");
		assertNull(validation);

		validation = pisdr008.executeGetBlackListHealthService(new IdentityDataDTO("3" , "L", null), "");
		assertNull(validation);

		validation = pisdr008.executeGetBlackListHealthService(new IdentityDataDTO("3" , "L", "00000000"), "");
		assertNull(validation);

		when(externalApiConnector.postForObject(anyString(), anyObject(), any())).thenReturn(new BlackListHealthRimacBO());
		validation = pisdr008.executeGetBlackListHealthService(new IdentityDataDTO("3" , "L", "00000000"), "");
		assertNull(validation);
	}

	@Test
	public void executeGetBlackListHealthServiceTestWithMockData() {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListHealthServiceTestWithMockData...");

		when(mockService.isEnabled(anyString())).thenReturn(true);
		when(mockService.getBlackListHealthRimacMock()).thenReturn(responseBlackListHealthRimac);
		IdentityDataDTO input = new IdentityDataDTO();
		input.setNroDocumento("11111111");
		input.setTipoDocumento("L");
		SelectionQuotationPayloadBO validation = pisdr008.executeGetBlackListHealthService(input, "");
		LOGGER.info("validation='" + validation + "'");
		assertNotNull(validation);
	}

	@Test
	public void executeGetBlackListIndicatorServiceOK() throws IOException {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListIndicatorServiceOK...");

		BlackListASO response = mockDTO.getBlackListASOMockResponse();

		when(internalApiConnector.getForObject(anyString(), any(), anyMap()))
				.thenReturn(response);

		BlackListIndicatorBO validation = pisdr008.executeGetBlackListIndicatorService("00000000");
		assertNotNull(validation);
		assertNotNull(validation.getIndicatorId());
		assertNotNull(validation.getIsActive());
		assertNotNull(validation.getName());

		validation = pisdr008.executeGetBlackListIndicatorService(null);
		assertNull(validation);
	}

	@Test
	public void executeGetBlackListIndicatorServiceWithRestClientException() {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListIndicatorServiceWithRestClientException...");

		when(internalApiConnector.getForObject(anyString(), any(), anyMap()))
				.thenThrow(new RestClientException("CONNECTION ERROR"));

		BlackListIndicatorBO validation = pisdr008.executeGetBlackListIndicatorService("00000000");
		assertNull(validation);
	}

	@Test
	public void executeGetBlackListRiskServiceOK() throws IOException {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListRiskServiceOK...");

		BlackListRiskRimacBO response = mockDTO.getBlackListRiskRimacMockResponse();

		when(externalApiConnector.postForObject(anyString(), anyObject(), any()))
				.thenReturn(response);

		IdentityDataDTO identity = new IdentityDataDTO("3", "L", "99999999");
		SelectionQuotationPayloadBO validation = pisdr008.executeGetBlackListRiskService(identity, "");
		assertNotNull(validation);
		assertNotNull(validation.getMensaje());
		assertNotNull(validation.getStatus());

		validation = pisdr008.executeGetBlackListRiskService(null, "");
		assertNull(validation);
		identity = new IdentityDataDTO();
		validation = pisdr008.executeGetBlackListRiskService(identity, "");
		assertNull(validation);
		identity.setNroDocumento("00000000");
		validation = pisdr008.executeGetBlackListRiskService(identity, "");
		assertNull(validation);

		when(externalApiConnector.postForObject(anyString(), anyObject(), any())).thenReturn(null);
		validation = pisdr008.executeGetBlackListRiskService(identity, "");
		assertNull(validation);

		response = new BlackListRiskRimacBO();
		when(externalApiConnector.postForObject(anyString(), anyObject(), any())).thenReturn(response);
		validation = pisdr008.executeGetBlackListRiskService(identity, "");
		assertNull(validation);

		response.setPayload(null);
		when(externalApiConnector.postForObject(anyString(), anyObject(), any())).thenReturn(null);
		validation = pisdr008.executeGetBlackListRiskService(identity, "");
		assertNull(validation);

	}

	@Test
	public void executeGetBlackListRiskServiceWithRestClientException() {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListRiskServiceWithRestClientException...");

		when(externalApiConnector.postForObject(anyString(), anyObject(), any()))
				.thenThrow(new RestClientException("CONNECTION ERROR"));

		SelectionQuotationPayloadBO validation = pisdr008.executeGetBlackListRiskService(new IdentityDataDTO("3", "L", "00000000"), "");
		assertNull(validation);
	}

	@Test
	public void executeGetBlackListHealthServiceOK() throws IOException {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListHealthServiceOK...");

		BlackListHealthRimacBO response = mockDTO.getBlackListHealthRimacMockResponse();

		when(externalApiConnector.postForObject(anyString(), anyObject(), any()))
				.thenReturn(response);
		IdentityDataDTO identity = new IdentityDataDTO("3", "L", "22222222");
		SelectionQuotationPayloadBO validation = pisdr008.executeGetBlackListHealthService(identity, "");
		assertNotNull(validation);
		assertNotNull(validation.getMensaje());
		assertNotNull(validation.getStatus());

		validation = pisdr008.executeGetBlackListHealthService(null, "");
		assertNull(validation);

		validation = pisdr008.executeGetBlackListHealthService(null, "");
		assertNull(validation);
		identity = new IdentityDataDTO();
		validation = pisdr008.executeGetBlackListHealthService(identity, "");
		assertNull(validation);
		identity.setNroDocumento("00000000");
		validation = pisdr008.executeGetBlackListHealthService(identity, "");
		assertNull(validation);

		when(externalApiConnector.postForObject(anyString(), anyObject(), any())).thenReturn(null);
		validation = pisdr008.executeGetBlackListHealthService(identity, "");
		assertNull(validation);

		response = new BlackListHealthRimacBO();
		when(externalApiConnector.postForObject(anyString(), anyObject(), any())).thenReturn(response);
		validation = pisdr008.executeGetBlackListHealthService(identity, "");
		assertNull(validation);

		response.setPayload(null);
		when(externalApiConnector.postForObject(anyString(), anyObject(), any())).thenReturn(null);
		validation = pisdr008.executeGetBlackListHealthService(identity, "");
		assertNull(validation);
	}

	@Test
	public void executeGetBlackListHealthServiceWithRestClientException() {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListHealthServiceWithRestClientException...");

		when(externalApiConnector.postForObject(anyString(), anyObject(), any()))
				.thenThrow(new RestClientException("CONNECTION ERROR"));

		SelectionQuotationPayloadBO validation = pisdr008.executeGetBlackListHealthService(new IdentityDataDTO("3", "L", "22222222"), "");
		assertNull(validation);
	}
	@Test
	public void executeGetCustomerInformationServiceOK() {
		LOGGER.info("PISDR008Test - Executing executeGetCustomerInformationServiceOK...");

		when(internalApiConnector.getForObject(anyString(), any(), anyMap()))
				.thenReturn(customerList);

		CustomerListASO validation = pisdr008.executeGetCustomerInformation("90008603");
		assertNotNull(validation);
		assertNotNull(validation.getData().get(0).getBirthData().getBirthDate());
	}

	@Test
	public void executeGetCustomerInformationServiceWithRestClientException() {
		LOGGER.info("PISDR008Test - Executing executeGetCustomerInformationServiceWithRestClientException...");

		when(internalApiConnector.getForObject(anyString(), any(), anyMap())).
				thenThrow(new RestClientException(MESSAGE_EXCEPTION));

		CustomerListASO validation = pisdr008.executeGetCustomerInformation("customerId");

		assertNull(validation);
	}

}
