package com.bbva.pisd.lib.r018;

import com.bbva.elara.domain.transaction.Context;
import com.bbva.elara.domain.transaction.ThreadContext;

import com.bbva.pisd.dto.insurance.aso.CustomerListASO;
import com.bbva.pisd.dto.insurance.blacklist.BlockingCompanyDTO;
import com.bbva.pisd.dto.insurance.blacklist.EntityOutBlackListDTO;
import com.bbva.pisd.dto.insurance.blacklist.InsuranceBlackListDTO;

import com.bbva.pisd.dto.insurance.bo.BlackListIndicatorBO;
import com.bbva.pisd.dto.insurance.commons.DocumentTypeDTO;
import com.bbva.pisd.dto.insurance.commons.IdentityDataDTO;
import com.bbva.pisd.dto.insurance.commons.IdentityDocumentDTO;
import com.bbva.pisd.dto.insurance.commons.InsuranceProductDTO;

import com.bbva.pisd.dto.insurance.mock.MockDTO;

import com.bbva.pisd.dto.insurance.utils.PISDConstants;
import com.bbva.pisd.lib.r008.PISDR008;

import com.bbva.pisd.lib.r018.impl.PISDR018Impl;

import com.bbva.pisd.lib.r018.impl.util.MapperHelper;
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
	private static final String RIMAC = "RIMAC";
	private static final String SALUD = "SALUD";
	private static final Logger LOGGER = LoggerFactory.getLogger(PISDR018Test.class);

	private final PISDR018Impl pisdR018 = new PISDR018Impl();

	private PISDR008 pisdR008;

	private MapperHelper mapperHelper;

	private MockDTO mockDTO;

	private InsuranceBlackListDTO request;

	@Before
	public void setUp() {
		ThreadContext.set(new Context());

		pisdR008 = mock(PISDR008.class);
		pisdR018.setPisdR008(pisdR008);

		mapperHelper = mock(MapperHelper.class);
		pisdR018.setMapperHelper(mapperHelper);

		mockDTO = MockDTO.getInstance();

		request = new InsuranceBlackListDTO();
		request.setBlockingCompany(new BlockingCompanyDTO(RIMAC));
		request.setProduct(new InsuranceProductDTO(SALUD, null, null));
		request.setIdentityDocument(new IdentityDocumentDTO(new DocumentTypeDTO("L"), "00000000"));
	}

	@Test
	public void executeBlackListValidationHealthProduct() {
		when(this.mapperHelper.createResponseBlackListBBVAService(anyObject(), anyObject())).thenReturn(new InsuranceBlackListDTO());

		this.request.setProduct(null);
		this.request.setBlockingCompany(null);

		EntityOutBlackListDTO validation = this.pisdR018.executeBlackListValidation(this.request);

		assertNotNull(validation);
		assertNotNull(validation.getData());
		assertFalse(validation.getData().isEmpty());
	}

	@Test
	public void executeBlackListValidationIneligibleCustomer() {
		this.request.getProduct().setId("VIDA");

		when(this.pisdR008.executeGetBlackListIndicatorService(anyString())).thenReturn(new BlackListIndicatorBO());

		InsuranceBlackListDTO responseIneligibleCustomer  = new InsuranceBlackListDTO();
		responseIneligibleCustomer.setId("indicatorId");
		responseIneligibleCustomer.setDescription("");
		responseIneligibleCustomer.setIsBlocked(PISDConstants.LETTER_SI);

		when(this.mapperHelper.createResponseToIneligibleCustomer(anyObject())).thenReturn(responseIneligibleCustomer);

		EntityOutBlackListDTO validation = this.pisdR018.executeBlackListValidation(this.request);

		assertNotNull(validation);
		assertNotNull(validation.getData());
		assertFalse(validation.getData().isEmpty());

	}

	@Test
	public void executeBlackListValidationEasyyesProduct() throws IOException {
		this.request.getProduct().setId("EASYYES");

		when(this.pisdR008.executeGetBlackListIndicatorService(anyString())).thenReturn(new BlackListIndicatorBO());

		InsuranceBlackListDTO responseIneligibleCustomer  = new InsuranceBlackListDTO();
		responseIneligibleCustomer.setId("indicatorId");
		responseIneligibleCustomer.setDescription("");
		responseIneligibleCustomer.setIsBlocked(PISDConstants.LETTER_NO);

		when(this.mapperHelper.createResponseToIneligibleCustomer(anyObject())).
				thenReturn(responseIneligibleCustomer);

		IdentityDataDTO identityData = new IdentityDataDTO();
		identityData.setNroDocumento("documentNumber");
		identityData.setTipoDocumento("DNI");
		identityData.setTipoLista("tipoLista");

		when(this.mapperHelper.createBlackListRimacRequest(anyObject(), anyString())).thenReturn(identityData);

		CustomerListASO customerList = this.mockDTO.getCustomerDataResponse();

		when(this.pisdR008.executeGetCustomerInformation(anyString())).thenReturn(customerList);

		when(this.mapperHelper.createResponseBlackListBBVAService(anyObject(), anyObject(), anyObject())).
				thenReturn(responseIneligibleCustomer);

		EntityOutBlackListDTO validation = this.pisdR018.executeBlackListValidation(this.request);

		assertNotNull(validation);
		assertNotNull(validation.getData());
		assertFalse(validation.getData().isEmpty());
	}

	@Test
	public void executeBlackListValidationOtherProduct() {
		this.request.getProduct().setId("VEHICULAR");

		InsuranceBlackListDTO responseIneligibleCustomer  = new InsuranceBlackListDTO();
		responseIneligibleCustomer.setId("indicatorId");
		responseIneligibleCustomer.setDescription("");
		responseIneligibleCustomer.setIsBlocked(PISDConstants.LETTER_NO);

		when(this.mapperHelper.createResponseBlackListBBVAService(anyObject(), anyObject(), anyObject())).
				thenReturn(responseIneligibleCustomer);

		EntityOutBlackListDTO validation = this.pisdR018.executeBlackListValidation(this.request);

		assertNotNull(validation);
		assertNotNull(validation.getData());
		assertFalse(validation.getData().isEmpty());
	}

	@Test
	public void executeBlackListValidationVidaDinamicoProduct() throws IOException {
		this.request.getProduct().setId("VIDADINAMICO");

		when(this.pisdR008.executeGetBlackListIndicatorService(anyString())).thenReturn(new BlackListIndicatorBO());

		InsuranceBlackListDTO responseIneligibleCustomer  = new InsuranceBlackListDTO();
		responseIneligibleCustomer.setId("indicatorId");
		responseIneligibleCustomer.setDescription("");
		responseIneligibleCustomer.setIsBlocked(PISDConstants.LETTER_NO);

		when(this.mapperHelper.createResponseToIneligibleCustomer(anyObject())).
				thenReturn(responseIneligibleCustomer);

		IdentityDataDTO identityData = new IdentityDataDTO();
		identityData.setNroDocumento("documentNumber");
		identityData.setTipoDocumento("DNI");
		identityData.setTipoLista("tipoLista");

		when(this.mapperHelper.createBlackListRimacRequest(anyObject(), anyString())).thenReturn(identityData);

		CustomerListASO customerList = this.mockDTO.getCustomerDataResponse();

		when(this.pisdR008.executeGetCustomerInformation(anyString())).thenReturn(customerList);

		when(this.mapperHelper.createResponseBlackListBBVAService(anyObject(), anyObject(), anyObject())).
				thenReturn(responseIneligibleCustomer);

		EntityOutBlackListDTO validation = this.pisdR018.executeBlackListValidation(this.request);

		assertNotNull(validation);
		assertNotNull(validation.getData());
		assertFalse(validation.getData().isEmpty());

		when(this.pisdR008.executeGetCustomerInformation(anyString())).thenReturn(null);

		validation = this.pisdR018.executeBlackListValidation(this.request);

		assertNotNull(validation);
		assertNotNull(validation.getData());
		assertFalse(validation.getData().isEmpty());
	}

}
