package com.bbva.pisd.lib.r018;

import com.bbva.apx.exception.business.BusinessException;
import com.bbva.elara.configuration.manager.application.ApplicationConfigurationService;
import com.bbva.elara.domain.transaction.Context;
import com.bbva.elara.domain.transaction.ThreadContext;

import com.bbva.pisd.dto.insurance.aso.CustomerListASO;
import com.bbva.pisd.dto.insurance.blacklist.BlackListTypeDTO;
import com.bbva.pisd.dto.insurance.blacklist.BlockingCompanyDTO;
import com.bbva.pisd.dto.insurance.blacklist.EntityOutBlackListDTO;
import com.bbva.pisd.dto.insurance.blacklist.InsuranceBlackListDTO;
import com.bbva.pisd.dto.insurance.bo.*;
import com.bbva.pisd.dto.insurance.commons.ContactDTO;
import com.bbva.pisd.dto.insurance.commons.DocumentTypeDTO;
import com.bbva.pisd.dto.insurance.commons.IdentityDocumentDTO;
import com.bbva.pisd.dto.insurance.commons.InsuranceProductDTO;
import com.bbva.pisd.dto.insurance.mock.MockDTO;
import com.bbva.pisd.dto.insurance.utils.PISDConstants;
import com.bbva.pisd.lib.r008.PISDR008;
import com.bbva.pisd.lib.r018.impl.PISDR018Impl;
import org.checkerframework.checker.units.qual.C;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	private static final String CUSTOMER_VALIDATION_MESSAGE = "CUSTOMER_VALIDATION_MESSAGE";
	private static final String MOBILE_NUMBER = "MOBILE_NUMBER";
	private static final String EMAIL = "EMAIL";
	private static final String DISTRICT = "DISTRICT";
	private static final String PROVINCE = "PROVINCE";
	private static final String DEPARTMENT = "DEPARTMENT";
	private static final Logger LOGGER = LoggerFactory.getLogger(PISDR018Test.class);

	private final PISDR018Impl pisdR018 = new PISDR018Impl();

	private ApplicationConfigurationService applicationConfigurationService;
	private MockDTO mockDTO;
	private PISDR008 pisdr008;
	private CustomerListASO customerList;
	private SelectionQuotationPayloadBO blPositiveRimac;
	private BlackListIndicatorBO bliPositive;
	private SelectionQuotationPayloadBO blNegativeRimac;
	private BlackListIndicatorBO bliNegative;
	private InsuranceBlackListDTO request;

	@Before
	public void setUp() throws IOException{
		ThreadContext.set(new Context());

		mockDTO = MockDTO.getInstance();

		applicationConfigurationService = mock(ApplicationConfigurationService.class);

		when(applicationConfigurationService.getProperty(anyString())).thenReturn("somevalue");
		when(applicationConfigurationService.getProperty(anyString())).thenReturn("CUSTOMER_VALIDATION_MESSAGE");

		pisdR018.setApplicationConfigurationService(applicationConfigurationService);

		pisdr008 = mock(PISDR008.class);
		pisdR018.setPisdR008(pisdr008);
		customerList = mockDTO.getCustomerDataResponse();
		when(pisdr008.executeGetCustomerInformation(anyString())).thenReturn(customerList);

		blPositiveRimac = mockDTO.getBlackListValidationPositiveRimacMockResponse();
		bliPositive = mockDTO.getBlackListValidationPositiveASOMockResponse();
		blNegativeRimac = mockDTO.getBlackListValidationNegativeRimacMockResponse();
		bliNegative = mockDTO.getBlackListValidationNegativeASOMockResponse();
		when(pisdr008.executeGetBlackListIndicatorService(anyString())).thenReturn(bliPositive);
		when(pisdr008.executeGetBlackListHealthService(anyObject(), anyString())).thenReturn(blPositiveRimac);
		request = new InsuranceBlackListDTO();
		request.setBlockingCompany(new BlockingCompanyDTO("RIMAC"));
		request.setProduct(new InsuranceProductDTO("SALUD", null, null));
		request.setIdentityDocument(new IdentityDocumentDTO(new DocumentTypeDTO("L"), "00000000"));
	}

	@Test
	public void executeBlackListValidationTestNull() {
		LOGGER.info("PISDR018Test - Executing executeBlackListValidationTestNull...");
		when(pisdr008.executeGetBlackListIndicatorService(anyString())).thenReturn(null);
		when(pisdr008.executeGetBlackListHealthService(anyObject(), anyString())).thenReturn(null);
		request = null;
		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNull(validation);
		when(pisdr008.executeGetCustomerInformation(anyString())).thenReturn(customerList);
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
	public void executeBlackListValidationTestOK() {
		LOGGER.info("PISDR018Test - Executing executeBlackListValidationTestOK...");
		when(pisdr008.executeGetBlackListIndicatorService(anyString())).thenReturn(bliPositive);

		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);

		when(pisdr008.executeGetBlackListHealthService(anyObject(), anyString())).thenReturn(blPositiveRimac);
		validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);

		request.setProduct(new InsuranceProductDTO("VEHICULAR", null, null));
		validation = pisdR018.executeBlackListValidation(request);
		assertNull(validation);

		request.setCustomerId("000");
		validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);

		when(pisdr008.executeGetBlackListRiskService(anyObject(), anyString())).thenReturn(blPositiveRimac);
		validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);

		when(pisdr008.executeGetBlackListIndicatorService(anyString())).thenReturn(bliNegative);
		request.setTraceId(null);
		validation = pisdR018.executeBlackListValidation(request);
		assertEquals("", validation.getData().get(0).getId());

		request.setTraceId("123");
		validation = pisdR018.executeBlackListValidation(request);
		assertEquals("123", validation.getData().get(0).getId());

		request.setTraceId("012345678910111213141516171819202122232425");
		validation = pisdR018.executeBlackListValidation(request);
		assertEquals("012345678910111213141516171819202122", validation.getData().get(0).getId());

		when(pisdr008.executeGetBlackListRiskService(anyObject(), anyString())).thenReturn(blNegativeRimac);
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

		request.setProduct(new InsuranceProductDTO("EASYYES", null, null));
		validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);

	}

	@Test
	public void executeBlackListValidationWithNotIdentityDocumentsTest() {
		customerList.getData().get(0).setIdentityDocuments(null);

		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);
		assertEquals(validation.getData().get(0).getDescription(), CUSTOMER_VALIDATION_MESSAGE);
	}
	@Test
	public void executeBlackListValidationWithDocumentTypeNullTest() {
		customerList.getData().get(0).getIdentityDocuments().get(0).setDocumentType(null);

		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);
		assertEquals(validation.getData().get(0).getDescription(), CUSTOMER_VALIDATION_MESSAGE);
	}

	@Test
	public void executeBlackListValidationWithDocumentTypeEmptyTest() {
		customerList.getData().get(0).getIdentityDocuments().get(0).getDocumentType().setId("");

		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);
		assertEquals(validation.getData().get(0).getDescription(), CUSTOMER_VALIDATION_MESSAGE);
	}

	@Test
	public void executeBlackListValidationWithDocumentNumberEmptyTest() {
		customerList.getData().get(0).getIdentityDocuments().get(0).setDocumentNumber("");

		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);
		assertEquals(validation.getData().get(0).getDescription(), CUSTOMER_VALIDATION_MESSAGE);
	}

	@Test
	public void executeBlackListValidationWithGenderNullTest() {
		customerList.getData().get(0).setGender(null);

		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);
		assertEquals(validation.getData().get(0).getDescription(), CUSTOMER_VALIDATION_MESSAGE);
	}

	@Test
	public void executeBlackListValidationWithGenderEmptyTest() {
		customerList.getData().get(0).getGender().setId("");

		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);
		assertEquals(validation.getData().get(0).getDescription(), CUSTOMER_VALIDATION_MESSAGE);
	}

	@Test
	public void executeBlackListValidationWithContactDetailsNullTest() {

		customerList.getData().get(0).setContactDetails(null);
		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);
		assertEquals(validation.getData().get(0).getDescription(), CUSTOMER_VALIDATION_MESSAGE);
	}

	@Test
	public void executeBlackListValidationWithNotRequiredEmailTest() {

		ContactDetailsBO contactDetailsBOEmail = new ContactDetailsBO();
		ContactTypeBO contactTypeBO = new ContactTypeBO();
		contactTypeBO.setId(EMAIL);
		contactDetailsBOEmail.setContactType(contactTypeBO);

		ContactDetailsBO contactDetailsBOPhone = new ContactDetailsBO();

		List<ContactDetailsBO> contactDetailsBOList = new ArrayList<>();
		contactDetailsBOList.add(contactDetailsBOEmail);
		contactDetailsBOList.add(contactDetailsBOPhone);

		customerList.getData().get(0).setContactDetails(contactDetailsBOList);

		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);
		assertEquals(validation.getData().get(0).getDescription(), CUSTOMER_VALIDATION_MESSAGE);
	}

	@Test
	public void executeBlackListValidationWithNotRequiredPhoneTest() {

		ContactDetailsBO contactDetailsBOPhone = new ContactDetailsBO();
		ContactTypeBO contactTypeBO = new ContactTypeBO();
		contactTypeBO.setId(MOBILE_NUMBER);
		contactDetailsBOPhone.setContactType(contactTypeBO);

		ContactDetailsBO contactDetailsBOEmail = new ContactDetailsBO();

		List<ContactDetailsBO> contactDetailsBOList = new ArrayList<>();
		contactDetailsBOList.add(contactDetailsBOPhone);
		contactDetailsBOList.add(contactDetailsBOEmail);

		customerList.getData().get(0).setContactDetails(contactDetailsBOList);

		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);
		assertEquals(validation.getData().get(0).getDescription(), CUSTOMER_VALIDATION_MESSAGE);
	}

	@Test
	public void executeBlackListValidationWithNotCompleteContactDetailsTest() {

		ContactDetailsBO contactDetailsBOPhone = new ContactDetailsBO();
		List<ContactDetailsBO> contactDetailsBOList = new ArrayList<>();
		contactDetailsBOList.add(contactDetailsBOPhone);
		customerList.getData().get(0).setContactDetails(contactDetailsBOList);

		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);
		assertEquals(validation.getData().get(0).getDescription(), CUSTOMER_VALIDATION_MESSAGE);
	}


	@Test
	public void executeBlackListValidationWithAddressesNullTest() {

		customerList.getData().get(0).setAddresses(null);
		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);
		assertEquals(validation.getData().get(0).getDescription(), CUSTOMER_VALIDATION_MESSAGE);
	}

	@Test
	public void executeBlackListValidationWithNotCompleteGeographicGroupsTest(){

		AddressesBO addressesBO = new AddressesBO();
		LocationBO locationBO = new LocationBO();
		GeographicGroupsBO district = new GeographicGroupsBO();
		List<GeographicGroupsBO> geographicGroupsBOList = new ArrayList<>();
		geographicGroupsBOList.add(district);
		locationBO.setGeographicGroups(geographicGroupsBOList);
		addressesBO.setLocation(locationBO);
		List<AddressesBO> addressesBOList = new ArrayList<>();
		addressesBOList.add(addressesBO);

		customerList.getData().get(0).setAddresses(addressesBOList);
		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);
		assertEquals(validation.getData().get(0).getDescription(), CUSTOMER_VALIDATION_MESSAGE);
	}

	@Test
	public void executeBlackListValidationWithAdditionalInformationEmptyTest(){

		customerList.getData().get(0).getAddresses().get(0).getLocation().setAdditionalInformation("");
		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);
		assertEquals(validation.getData().get(0).getDescription(), CUSTOMER_VALIDATION_MESSAGE);
	}

	@Test
	public void executeBlackListValidationWithNotGeographicGroupsTest(){

		customerList.getData().get(0).getAddresses().get(0).getLocation().setGeographicGroups(null);
		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);
		assertEquals(validation.getData().get(0).getDescription(), CUSTOMER_VALIDATION_MESSAGE);
	}

	@Test
	public void executeBlackListValidationWithNotDistrictTest(){
		AddressesBO addressesBO = new AddressesBO();
		LocationBO locationBO = new LocationBO();
		GeographicGroupsBO district = new GeographicGroupsBO();
		GeographicGroupsBO province = new GeographicGroupsBO();
		GeographicGroupsBO department = new GeographicGroupsBO();
		GeographicGroupTypeBO geographicGroupTypeBODistrict = new GeographicGroupTypeBO();
		geographicGroupTypeBODistrict.setId(DISTRICT);
		district.setGeographicGroupType(geographicGroupTypeBODistrict);
		district.setName("");

		GeographicGroupTypeBO geographicGroupTypeBOProvince = new GeographicGroupTypeBO();
		geographicGroupTypeBOProvince.setId(PROVINCE);
		province.setGeographicGroupType(geographicGroupTypeBOProvince);
		province.setName("LIMA");

		GeographicGroupTypeBO geographicGroupTypeBODistrictDepartment = new GeographicGroupTypeBO();
		geographicGroupTypeBODistrictDepartment.setId(DEPARTMENT);
		department.setGeographicGroupType(geographicGroupTypeBODistrictDepartment);
		department.setName("LIMA");

		List<GeographicGroupsBO> geographicGroupsBOList = new ArrayList<>();
		geographicGroupsBOList.add(district);
		geographicGroupsBOList.add(province);
		geographicGroupsBOList.add(department);

		locationBO.setGeographicGroups(geographicGroupsBOList);
		addressesBO.setLocation(locationBO);
		List<AddressesBO> addressesBOList = new ArrayList<>();
		addressesBOList.add(addressesBO);

		customerList.getData().get(0).getAddresses().get(0).getLocation().setGeographicGroups(null);
		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);
		assertEquals(validation.getData().get(0).getDescription(), CUSTOMER_VALIDATION_MESSAGE);
	}

	@Test
	public void executeBlackListValidationWithNotProvinceTest(){
		AddressesBO addressesBO = new AddressesBO();
		LocationBO locationBO = new LocationBO();
		GeographicGroupsBO district = new GeographicGroupsBO();
		GeographicGroupsBO province = new GeographicGroupsBO();
		GeographicGroupsBO department = new GeographicGroupsBO();
		GeographicGroupTypeBO geographicGroupTypeBOProvince = new GeographicGroupTypeBO();
		geographicGroupTypeBOProvince.setId(PROVINCE);
		province.setGeographicGroupType(geographicGroupTypeBOProvince);
		province.setName("");

		GeographicGroupTypeBO geographicGroupTypeBODistrict = new GeographicGroupTypeBO();
		geographicGroupTypeBODistrict.setId(DISTRICT);
		district.setGeographicGroupType(geographicGroupTypeBODistrict);
		district.setName("LIMA");

		GeographicGroupTypeBO geographicGroupTypeBODistrictDepartment = new GeographicGroupTypeBO();
		geographicGroupTypeBODistrictDepartment.setId(DEPARTMENT);
		department.setGeographicGroupType(geographicGroupTypeBODistrictDepartment);
		department.setName("LIMA");

		List<GeographicGroupsBO> geographicGroupsBOList = new ArrayList<>();
		geographicGroupsBOList.add(district);
		geographicGroupsBOList.add(province);
		geographicGroupsBOList.add(department);

		locationBO.setGeographicGroups(geographicGroupsBOList);
		addressesBO.setLocation(locationBO);
		List<AddressesBO> addressesBOList = new ArrayList<>();
		addressesBOList.add(addressesBO);

		customerList.getData().get(0).getAddresses().get(0).getLocation().setGeographicGroups(null);
		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);
		assertEquals(validation.getData().get(0).getDescription(), CUSTOMER_VALIDATION_MESSAGE);
	}

	@Test
	public void executeBlackListValidationWithNotDepartmentTest(){
		AddressesBO addressesBO = new AddressesBO();
		LocationBO locationBO = new LocationBO();
		GeographicGroupsBO district = new GeographicGroupsBO();
		GeographicGroupsBO province = new GeographicGroupsBO();
		GeographicGroupsBO department = new GeographicGroupsBO();
		GeographicGroupTypeBO setGeographicGroupTypeDepartment = new GeographicGroupTypeBO();
		setGeographicGroupTypeDepartment.setId(DEPARTMENT);
		department.setGeographicGroupType(setGeographicGroupTypeDepartment);
		department.setName("");

		GeographicGroupTypeBO geographicGroupTypeBODistrict = new GeographicGroupTypeBO();
		geographicGroupTypeBODistrict.setId(DISTRICT);
		district.setGeographicGroupType(geographicGroupTypeBODistrict);
		district.setName("LIMA");

		GeographicGroupTypeBO geographicGroupTypeBODistrictProvince = new GeographicGroupTypeBO();
		geographicGroupTypeBODistrictProvince.setId(PROVINCE);
		province.setGeographicGroupType(geographicGroupTypeBODistrictProvince);
		province.setName("LIMA");

		List<GeographicGroupsBO> geographicGroupsBOList = new ArrayList<>();
		geographicGroupsBOList.add(district);
		geographicGroupsBOList.add(province);
		geographicGroupsBOList.add(department);

		locationBO.setGeographicGroups(geographicGroupsBOList);
		addressesBO.setLocation(locationBO);
		List<AddressesBO> addressesBOList = new ArrayList<>();
		addressesBOList.add(addressesBO);

		customerList.getData().get(0).getAddresses().get(0).getLocation().setGeographicGroups(null);
		EntityOutBlackListDTO validation = pisdR018.executeBlackListValidation(request);
		assertNotNull(validation);
		assertEquals(validation.getData().get(0).getDescription(), CUSTOMER_VALIDATION_MESSAGE);
	}

	@Test (expected = BusinessException.class)
	public void executeBlackListValidationWithCustomerListResponseNull(){
		when(pisdr008.executeGetCustomerInformation(anyString())).thenReturn(null);
		pisdR018.executeBlackListValidation(request);
	}

	@Test (expected = BusinessException.class)
	public void executeBlackListValidationWithBirtdateCustomerListResponseNull(){
		request.setProduct(new InsuranceProductDTO("EASYYES", null, null));
		customerList.getData().get(0).setBirthData(null);
		pisdR018.executeBlackListValidation(request);
	}
}
