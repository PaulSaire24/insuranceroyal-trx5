package com.bbva.pisd.lib.r018;

import com.bbva.elara.configuration.manager.application.ApplicationConfigurationService;
import com.bbva.elara.domain.transaction.Context;
import com.bbva.elara.domain.transaction.ThreadContext;

import com.bbva.pisd.dto.insurance.blacklist.BlackListTypeDTO;
import com.bbva.pisd.dto.insurance.blacklist.BlockingCompanyDTO;
import com.bbva.pisd.dto.insurance.blacklist.EntityOutBlackListDTO;
import com.bbva.pisd.dto.insurance.blacklist.InsuranceBlackListDTO;
import com.bbva.pisd.dto.insurance.bo.SelectionQuotationPayloadBO;
import com.bbva.pisd.dto.insurance.bo.blacklist.BlackListIndicatorBO;
import com.bbva.pisd.dto.insurance.commons.DocumentTypeDTO;
import com.bbva.pisd.dto.insurance.commons.IdentityDocumentDTO;
import com.bbva.pisd.dto.insurance.commons.InsuranceProductDTO;
import com.bbva.pisd.dto.insurance.mock.MockDTO;
import com.bbva.pisd.dto.insurance.utils.PISDConstants;
import com.bbva.pisd.lib.r011.PISDR011;
import com.bbva.pisd.lib.r018.impl.PISDR018Impl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/META-INF/spring/PISDR018-app.xml",
		"classpath:/META-INF/spring/PISDR018-app-test.xml",
		"classpath:/META-INF/spring/PISDR018-arc.xml",
		"classpath:/META-INF/spring/PISDR018-arc-test.xml" })
public class PISDR018Test {

	private static final Logger LOGGER = LoggerFactory.getLogger(PISDR018Test.class);

	private final PISDR018Impl pisdR018 = new PISDR018Impl();

	private ApplicationConfigurationService applicationConfigurationService;
	private MockDTO mockDTO;
	private PISDR011 pisdr011;

	@Before
	public void setUp() {
		ThreadContext.set(new Context());

		mockDTO = MockDTO.getInstance();

		applicationConfigurationService = mock(ApplicationConfigurationService.class);

		when(applicationConfigurationService.getProperty(anyString())).thenReturn("somevalue");

		pisdR018.setApplicationConfigurationService(applicationConfigurationService);

		pisdr011 = mock(PISDR011.class);
		pisdR018.setPisdR011(pisdr011);
	}

	@Test
	public void executeBlackListValidationTestNull() {
		LOGGER.info("PISDR018Test - Executing executeBlackListValidationTestNull...");
		InsuranceBlackListDTO request = null;
		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNull(validation);

		request = new InsuranceBlackListDTO();
		validation = pisdR018.executeBlackListValidation(request);
		assertNull(validation);
		request.setBlockingCompany(new BlockingCompanyDTO());
		validation = pisdR018.executeBlackListValidation(request);
		assertNull(validation);
		request.setBlockingCompany(new BlockingCompanyDTO(PISDConstants.BLACKLIST_COMPANY_RIMAC));
		validation = pisdR018.executeBlackListValidation(request);
		assertNull(validation);
		request.setProduct(new InsuranceProductDTO());
		validation = pisdR018.executeBlackListValidation(request);
		assertNull(validation);
		request.setProduct(new InsuranceProductDTO(PISDConstants.HEALTH_RIMAC, null, null));
		validation = pisdR018.executeBlackListValidation(request);
		assertNull(validation);
		request.setIdentityDocument(new IdentityDocumentDTO());
		validation = pisdR018.executeBlackListValidation(request);
		assertNull(validation);
		request.setIdentityDocument(new IdentityDocumentDTO(new DocumentTypeDTO(null), null));
		validation = pisdR018.executeBlackListValidation(request);
		assertNull(validation);
		request.setIdentityDocument(new IdentityDocumentDTO(new DocumentTypeDTO("L"), null));
		validation = pisdR018.executeBlackListValidation(request);
		assertNull(validation);
		request.setIdentityDocument(new IdentityDocumentDTO(new DocumentTypeDTO("L"), "00000000"));
		validation = pisdR018.executeBlackListValidation(request);
		assertNull(validation);
	}

	@Test
	public void executeBlackListValidationTestOK() throws IOException {
		LOGGER.info("PISDR018Test - Executing executeBlackListValidationTestOK...");
		SelectionQuotationPayloadBO blPositiveRimac = mockDTO.getBlackListValidationPositiveRimacMockResponse();
		SelectionQuotationPayloadBO blNegativeRimac = mockDTO.getBlackListValidationNegativeRimacMockResponse();
		BlackListIndicatorBO bliPositive = mockDTO.getBlackListValidationPositiveASOMockResponse();
		BlackListIndicatorBO bliNegative = mockDTO.getBlackListValidationNegativeASOMockResponse();

		InsuranceBlackListDTO request = new InsuranceBlackListDTO();
		request.setBlockingCompany(new BlockingCompanyDTO("RIMAC"));
		request.setProduct(new InsuranceProductDTO("SALUD", null, null));
		request.setIdentityDocument(new IdentityDocumentDTO(new DocumentTypeDTO("L"), "00000000"));

		when(pisdr011.executeGetBlackListIndicatorService(anyString())).thenReturn(bliPositive);

		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNull(validation);

		when(pisdr011.executeGetBlackListHealthService(anyObject(), anyString())).thenReturn(blPositiveRimac);
		validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);

		request.setProduct(new InsuranceProductDTO("VEHICULAR", null, null));
		validation = pisdR018.executeBlackListValidation(request);
		assertNull(validation);

		request.setCustomerId("000");
		validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);

		when(pisdr011.executeGetBlackListRiskService(anyObject(), anyString())).thenReturn(blPositiveRimac);
		validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);

		when(pisdr011.executeGetBlackListIndicatorService(anyString())).thenReturn(bliNegative);
		request.setTraceId(null);
		validation = pisdR018.executeBlackListValidation(request);
		assertEquals("", validation.getData().get(0).getId());

		request.setTraceId("123");
		validation = pisdR018.executeBlackListValidation(request);
		assertEquals("123", validation.getData().get(0).getId());

		request.setTraceId("012345678910111213141516171819202122232425");
		validation = pisdR018.executeBlackListValidation(request);
		assertEquals("012345678910111213141516171819202122", validation.getData().get(0).getId());

		when(pisdr011.executeGetBlackListHealthService(anyObject(), anyString())).thenReturn(blNegativeRimac);
		validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);

		validation = pisdR018.executeBlackListValidation(request);
		assertEquals("3", validation.getData().get(0).getBlackListType().getId());

		request.setBlackListType(new BlackListTypeDTO());
		validation = pisdR018.executeBlackListValidation(request);
		assertEquals("3", validation.getData().get(0).getBlackListType().getId());

		request.setBlackListType(new BlackListTypeDTO("99"));
		validation = pisdR018.executeBlackListValidation(request);
		assertEquals("99", validation.getData().get(0).getBlackListType().getId());
	}
	
}
