package com.bbva.pisd.lib.r008;

import com.bbva.apx.exception.business.BusinessException;

import com.bbva.elara.configuration.manager.application.ApplicationConfigurationService;
import com.bbva.elara.domain.transaction.Context;
import com.bbva.elara.domain.transaction.ThreadContext;

import com.bbva.elara.utility.api.connector.APIConnector;

import com.bbva.pbtq.dto.validatedocument.response.host.pewu.PEMSALW5;
import com.bbva.pbtq.dto.validatedocument.response.host.pewu.PEMSALWU;
import com.bbva.pbtq.dto.validatedocument.response.host.pewu.PEWUResponse;
import com.bbva.pbtq.lib.r002.PBTQR002;
import com.bbva.pisd.dto.insurance.amazon.SignatureAWS;

import com.bbva.pisd.dto.insurance.aso.BlackListASO;
import com.bbva.pisd.dto.insurance.aso.CustomerListASO;

import com.bbva.pisd.dto.insurance.bo.BlackListHealthRimacBO;
import com.bbva.pisd.dto.insurance.bo.BlackListIndicatorBO;
import com.bbva.pisd.dto.insurance.bo.BlackListRiskRimacBO;
import com.bbva.pisd.dto.insurance.bo.SelectionQuotationPayloadBO;

import com.bbva.pisd.dto.insurance.bo.customer.CustomerBO;
import com.bbva.pisd.dto.insurance.commons.IdentityDataDTO;

import com.bbva.pisd.dto.insurance.mock.MockDTO;

import com.bbva.pisd.dto.insurance.utils.PISDConstants;
import com.bbva.pisd.dto.insurance.utils.PISDErrors;
import com.bbva.pisd.lib.r008.factory.ApiConnectorFactoryMock;

import com.bbva.pisd.lib.r008.impl.PISDR008Impl;

import com.bbva.pisd.lib.r014.PISDR014;

import com.bbva.pisd.mock.MockBundleContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.junit.Assert.*;
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

	private final PISDR008Impl pisdr008 = new PISDR008Impl();

	private PBTQR002 pbtqr002;

	private MockDTO mockDTO;

	private APIConnector internalApiConnector;

	private APIConnector externalApiConnector;

	private CustomerListASO customerList;
	private static final String MESSAGE_EXCEPTION = "CONNECTION ERROR";
	@Resource(name = "applicationConfigurationService")
	private ApplicationConfigurationService applicationConfigurationService;
	@Before
	public void setUp() throws IOException {
		ThreadContext.set(new Context());

		MockBundleContext mockBundleContext = mock(MockBundleContext.class);

		ApiConnectorFactoryMock apiConnectorFactoryMock = new ApiConnectorFactoryMock();
		internalApiConnector = apiConnectorFactoryMock.getAPIConnector(mockBundleContext);
		pisdr008.setInternalApiConnector(internalApiConnector);

		externalApiConnector = apiConnectorFactoryMock.getAPIConnector(mockBundleContext, false);
		pisdr008.setExternalApiConnector(externalApiConnector);

		PISDR014 pisdr014 = mock(PISDR014.class);
		pisdr008.setPisdR014(pisdr014);


		pbtqr002 = mock(PBTQR002.class);
		pisdr008.setPbtqR002(pbtqr002);

		mockDTO = MockDTO.getInstance();

		customerList = mockDTO.getCustomerDataResponse();

		when(pisdr014.executeSignatureConstruction(anyString(), anyString(), anyString(), anyString(), anyString()))
				.thenReturn(new SignatureAWS("", "", "", ""));
	}

	@Test
	public void executeGetBlackListIndicatorServiceOK() throws IOException {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListIndicatorServiceOK...");

		BlackListASO response = mockDTO.getBlackListASOMockResponse();

		when(internalApiConnector.getForObject(anyString(), any(), anyMap()))
				.thenReturn(response);

		BlackListIndicatorBO validation = pisdr008.executeGetBlackListIndicatorService("customerId");
		assertNotNull(validation);
		assertNotNull(validation.getIndicatorId());
		assertNotNull(validation.getIsActive());
		assertNotNull(validation.getName());
	}

	@Test
	public void executeGetBlackListIndicatorServiceTestNull() {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListIndicatorServiceTestNull...");
		BlackListIndicatorBO validation = pisdr008.executeGetBlackListIndicatorService(null);
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

		IdentityDataDTO identity = new IdentityDataDTO("listType", "documentType", "documentNumber");
		identity.setProducto(PISDConstants.ProductEasyYesLife.EASY_YES_RIMAC);

		SelectionQuotationPayloadBO validation = pisdr008.executeGetBlackListRiskService(identity, "traceId");

		assertNotNull(validation);
		assertNotNull(validation.getMensaje());
		assertNotNull(validation.getStatus());
	}

	@Test(expected = BusinessException.class)
	public void executeGetBlackListRiskServiceWithHttpServerErrorException() {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListRiskServiceWithHttpServerErrorException...");
		when(externalApiConnector.postForObject(anyString(), anyObject(), any()))
				.thenThrow(new RestClientException(MESSAGE_EXCEPTION));
		pisdr008.executeGetBlackListRiskService(new IdentityDataDTO("3", "L", "00000000"), "");
	}

	@Test
	public void executeGetBlackListRiskServiceWithHttpClientErrorException_ErrorVIDA001() {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListRiskServiceWithHttpClientErrorException_ErrorVIDA001...");
		String responseBody = "{\"error\":{\"code\":\"VIDA001\",\"message\":\"Error al Validar Datos.\",\"details\":[\" El campo [fechaNacimiento]  debe ser mayor o igual a 18 años.\"],\"httpStatus\":400}}";
		when(externalApiConnector.postForObject(anyString(), anyObject(), any()))
				.thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "",responseBody.getBytes(), StandardCharsets.UTF_8));

		SelectionQuotationPayloadBO validation = pisdr008.executeGetBlackListRiskService(new IdentityDataDTO("3", "L", "00000000"), "");
		assertNotNull(validation);
		assertEquals("1",validation.getStatus());
	}

	@Test
	public void executeGetBlackListRiskServiceWithHttpClientErrorException_ErrorURCOT005() {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListRiskServiceWithHttpClientErrorException_ErrorURCOT005...");
		String responseBody = "{\"error\":{\"code\":\"URCOT005\",\"message\":\"Error al Validar Datos.\",\"details\":[],\"httpStatus\":400}}";
		when(externalApiConnector.postForObject(anyString(), anyObject(), any()))
				.thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "",responseBody.getBytes(), StandardCharsets.UTF_8));

		SelectionQuotationPayloadBO validation = pisdr008.executeGetBlackListRiskService(new IdentityDataDTO("3", "L", "00000000"), "");
		assertNull(validation);
	}

	@Test
	public void executeGetBlackListRiskServiceWithHttpClientErrorException_AnotherError() {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListRiskServiceWithHttpClientErrorException_AnotherError...");
		String responseBody = "{\"error\":{\"code\":\"ERROR\",\"message\":\"Error al Validar Datos.\",\"details\":[],\"httpStatus\":400}}";
		when(externalApiConnector.postForObject(anyString(), anyObject(), any()))
				.thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "",responseBody.getBytes(), StandardCharsets.UTF_8));

		SelectionQuotationPayloadBO validation = pisdr008.executeGetBlackListRiskService(new IdentityDataDTO("3", "L", "00000000"), "");
		assertNull(validation);
	}

	@Test
	public void executeGetBlackListHealthServiceOK() throws IOException {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListHealthServiceOK...");

		BlackListHealthRimacBO response = mockDTO.getBlackListHealthRimacMockResponse();

		when(externalApiConnector.postForObject(anyString(), anyObject(), any()))
				.thenReturn(response);

		IdentityDataDTO identity = new IdentityDataDTO("listType", "documentType", "documentNumber");

		SelectionQuotationPayloadBO validation = pisdr008.executeGetBlackListHealthService(identity, "traceId");

		assertNotNull(validation);
		assertNotNull(validation.getMensaje());
		assertNotNull(validation.getStatus());
	}

	@Test
	public void executeGetBlackListHealthServiceWithRestClientException() {
		LOGGER.info("PISDR008Test - Executing executeGetBlackListHealthServiceWithRestClientException...");

		when(externalApiConnector.postForObject(anyString(), anyObject(), any()))
				.thenThrow(new RestClientException("CONNECTION ERROR"));

		SelectionQuotationPayloadBO validation = pisdr008.executeGetBlackListHealthService(new IdentityDataDTO("3", "L", "22222222"), "");

		assertNull(validation);
		assertEquals(PISDErrors.ERROR_TO_CONNECT_SERVICE_BLACKLISTHEALTH_RIMAC.getAdviceCode(),
				this.pisdr008.getAdviceList().get(0).getCode());
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
