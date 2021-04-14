package com.bbva.pisd;

import com.bbva.elara.domain.transaction.Context;
import com.bbva.elara.domain.transaction.Severity;
import com.bbva.elara.domain.transaction.request.TransactionRequest;
import com.bbva.elara.domain.transaction.request.body.CommonRequestBody;
import com.bbva.elara.domain.transaction.request.header.CommonRequestHeader;
import com.bbva.elara.test.osgi.DummyBundleContext;
import com.bbva.pisd.dto.insurance.blacklist.BlackListTypeDTO;
import com.bbva.pisd.dto.insurance.blacklist.BlockingCompanyDTO;
import com.bbva.pisd.dto.insurance.blacklist.EntityOutBlackListDTO;
import com.bbva.pisd.dto.insurance.commons.DocumentTypeDTO;
import com.bbva.pisd.dto.insurance.commons.IdentityDocumentDTO;
import com.bbva.pisd.dto.insurance.commons.InsuranceProductDTO;
import com.bbva.pisd.dto.insurance.mock.MockDTO;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import javax.annotation.Resource;

import com.bbva.pisd.lib.r018.PISDR018;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test for transaction PISDT00501PETransaction
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/META-INF/spring/elara-test.xml",
		"classpath:/META-INF/spring/PISDT00501PETest.xml" })
public class PISDT00501PETransactionTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(PISDT00501PETransactionTest.class);

	@Spy
	@Autowired
	private PISDT00501PETransaction transaction;

	@Resource(name = "dummyBundleContext")
	private DummyBundleContext bundleContext;

	@Resource(name = "pisdR018")
	private PISDR018 pisdr018;

	@Mock
	private CommonRequestHeader header;

	@Mock
	private TransactionRequest transactionRequest;

	@Mock
	private IdentityDocumentDTO identityDocument;

	@Mock
	private BlackListTypeDTO blackListType;

	@Mock
	private BlockingCompanyDTO blockingCompany;

	@Mock
	private InsuranceProductDTO insuranceProduct;

	@Mock
	private DocumentTypeDTO documentType;

	private MockDTO mockDTO;

	@Before
	public void initializeClass() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.transaction.start(bundleContext);
		this.transaction.setContext(new Context());
		CommonRequestBody commonRequestBody = new CommonRequestBody();
		commonRequestBody.setTransactionParameters(new ArrayList<>());
		this.transactionRequest.setBody(commonRequestBody);
		this.transactionRequest.setHeader(header);
		this.transaction.getContext().setTransactionRequest(transactionRequest);

		mockDTO = MockDTO.getInstance();

		doReturn(identityDocument).when(this.transaction).getIdentitydocument();
		doReturn(blackListType).when(this.transaction).getBlacklisttype();
		doReturn(blockingCompany).when(this.transaction).getBlockingcompany();
		doReturn(insuranceProduct).when(this.transaction).getProduct();
		when(this.transaction.getCustomerid()).thenReturn("00000000");
		when(this.transaction.getIdentitydocument().getDocumentNumber()).thenReturn("00000000");
		when(this.transaction.getIdentitydocument().getDocumentType()).thenReturn(documentType);
		when(this.transaction.getIdentitydocument().getDocumentType().getId()).thenReturn("L");
		when(this.transaction.getBlacklisttype().getId()).thenReturn("1");
		when(this.transaction.getBlockingcompany().getId()).thenReturn("RIMAC");
		when(this.transaction.getProduct().getId()).thenReturn("SALUD");
	}

	@Test
	public void execute() throws IOException {
		EntityOutBlackListDTO output = mockDTO.getInsuranceBlackListResponse();
		LOGGER.info("Execution of PISDT00501PETransactionTest: output: " + output);
		when(pisdr018.executeBlackListValidation(anyObject())).thenReturn(output);
		this.transaction.execute();

		assertTrue(this.transaction.getAdviceList().isEmpty());
	}

	@Test
	public void testNotNull(){
		Assert.assertNotNull(this.transaction);
		this.transaction.execute();
	}

	@Test
	public void testNull() {
		when(pisdr018.executeBlackListValidation(anyObject())).thenReturn(null);
		this.transaction.execute();
		assertEquals(Severity.ENR.getValue(), this.transaction.getSeverity().getValue());
	}
}
