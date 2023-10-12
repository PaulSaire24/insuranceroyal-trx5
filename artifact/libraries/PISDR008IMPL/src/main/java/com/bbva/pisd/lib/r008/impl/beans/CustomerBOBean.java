package com.bbva.pisd.lib.r008.impl.beans;

import com.bbva.elara.configuration.manager.application.ApplicationConfigurationService;
import com.bbva.pbtq.dto.validatedocument.response.host.pewu.PEWUResponse;
import com.bbva.pisd.dto.insurance.bo.*;
import com.bbva.pisd.dto.insurance.bo.customer.CustomerBO;
import com.bbva.pisd.dto.insurance.utils.PISDConstants;
import com.bbva.pisd.lib.r008.impl.util.Constans;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomerBOBean {
    private ApplicationConfigurationService applicationConfigurationService;

    public CustomerBOBean(ApplicationConfigurationService applicationConfigurationService) {
        this.applicationConfigurationService = applicationConfigurationService;
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerBOBean.class);

    public CustomerBO mapperCustomer(PEWUResponse result){
        LOGGER.info("***** PISDR008Impl - mapperCustomer Start *****");
        /* section customer data */
        CustomerBO customer = new CustomerBO();
        customer.setCustomerId(result.getPemsalwu().getNroclie());
        customer.setFirstName(result.getPemsalwu().getNombres());
        customer.setLastName(result.getPemsalwu().getApellip());
        customer.setSecondLastName(result.getPemsalwu().getApellim());
        customer.setBirthData(new BirthDataBO());
        customer.getBirthData().setBirthDate(result.getPemsalwu().getFechan());
        customer.getBirthData().setCountry(new CountryBO());
        customer.getBirthData().getCountry().setId(result.getPemsalwu().getPaisn());
        customer.setGender(new GenderBO());
        customer.getGender().setId(result.getPemsalwu().getSexo().equals("M") ? Constans.Gender.MALE : Constans.Gender.FEMALE);

        /* section identity document*/
        IdentityDocumentsBO identityDocumentsBO = new IdentityDocumentsBO();
        identityDocumentsBO.setDocumentNumber(result.getPemsalwu().getNdoi());
        identityDocumentsBO.setDocumentType(new DocumentTypeBO());
        /* map document type host ? yes*/
        /* map document type host ? yes*/
        switch (result.getPemsalwu().getTdoi()) {
            case "L":
                identityDocumentsBO.getDocumentType().setId(Constans.CustomerContact.DNI);
                break;
            case "R":
                identityDocumentsBO.getDocumentType().setId(Constans.CustomerContact.RUC);
                break;
            default:
                identityDocumentsBO.getDocumentType().setId(result.getPemsalwu().getTdoi());
                break;
        }

        identityDocumentsBO.setExpirationDate(result.getPemsalwu().getFechav());
        customer.setIdentityDocuments(Collections.singletonList(identityDocumentsBO));

        /* section contact Details */
        List<ContactDetailsBO> contactDetailsBOList = new ArrayList<>();

        /* section contact PHONE_NUMBER */
        LOGGER.info("***** PISDR008Impl - executeGetCustomerHost  ***** Map getTipocon: {}", result.getPemsalwu().getTipocon());
        if (StringUtils.isNotEmpty(result.getPemsalwu().getContact())) {
            ContactDetailsBO contactDetailPhone = new ContactDetailsBO();
            contactDetailPhone.setContactDetailId(result.getPemsalwu().getIdencon());
            contactDetailPhone.setContact(result.getPemsalwu().getContact());
            contactDetailPhone.setContactType(new ContactTypeBO());
            contactDetailPhone.getContactType().setId(Constans.CustomerContact.PHONE_NUMBER);
            contactDetailPhone.getContactType().setName(result.getPemsalw5().getDescmco());
            contactDetailsBOList.add(contactDetailPhone);
        }
        /* section contact2 type, validate MOBILE_NUMBER */
        LOGGER.info("***** PISDR008Impl - executeGetCustomerHost  ***** Map getTipoco2: {}", result.getPemsalwu().getTipoco2());
        if (StringUtils.isNotEmpty(result.getPemsalwu().getContac2())) {
            ContactDetailsBO contactDetailMobileNumber = new ContactDetailsBO();
            contactDetailMobileNumber.setContactDetailId(result.getPemsalwu().getIdenco2());
            contactDetailMobileNumber.setContact(result.getPemsalwu().getContac2());
            contactDetailMobileNumber.setContactType(new ContactTypeBO());
            contactDetailMobileNumber.getContactType().setId(Constans.CustomerContact.MOBILE_NUMBER);
            contactDetailMobileNumber.getContactType().setName(result.getPemsalw5().getDescmc1());
            contactDetailsBOList.add(contactDetailMobileNumber);
        }

        /* section contact3 type, validate EMAIL */
        LOGGER.info("***** PISDR008Impl - executeGetCustomerHost  ***** Map getTipoco3: {}", result.getPemsalwu().getTipoco3());
        if (StringUtils.isNotEmpty(result.getPemsalwu().getContac3())) {
            ContactDetailsBO contactDetailEmail = new ContactDetailsBO();
            contactDetailEmail.setContactDetailId(result.getPemsalwu().getIdenco3());
            contactDetailEmail.setContact(result.getPemsalwu().getContac3());
            contactDetailEmail.setContactType(new ContactTypeBO());
            contactDetailEmail.getContactType().setId(Constans.CustomerContact.EMAIL);
            contactDetailEmail.getContactType().setName(result.getPemsalw5().getDescmc2());
            contactDetailsBOList.add(contactDetailEmail);
        }

        customer.setContactDetails(contactDetailsBOList);
        /* section contact Details */

        /* section addresses */
        List<AddressesBO> addresses = new ArrayList<>();
        AddressesBO address = new AddressesBO();
        address.setAddressType(new AddressTypeBO());
        address.getAddressType().setId(result.getPemsalwu().getTipodir()); // map address type
        address.getAddressType().setName(result.getPemsalw4().getDepetdo());
        address.setResidenceStartDate(result.getPemsalwu().getFedocac());
        address.setAddressId(result.getPemsalwu().getCoddire());

        LocationBO location = new LocationBO();
        location.setCountry(new CountryBO());
        location.getCountry().setId(result.getPemsalwu().getPaisdom());
        location.setAdditionalInformation(result.getPemsalwu().getDetalle());
        GeographicGroupsBO avenida = new GeographicGroupsBO();
        avenida.setGeographicGroupType(new GeographicGroupTypeBO());
        avenida.getGeographicGroupType().setId(result.getPemsalwu().getIdendi1());
        avenida.getGeographicGroupType().setName(result.getPemsalw4().getDescvia());
        avenida.setName(result.getPemsalwu().getNombdi1());

        GeographicGroupsBO uncategory = new GeographicGroupsBO();
        uncategory.setGeographicGroupType(new GeographicGroupTypeBO());
        uncategory.getGeographicGroupType().setId(result.getPemsalwu().getIdendi2());
        uncategory.getGeographicGroupType().setName(result.getPemsalw4().getDescurb());
        uncategory.setName(result.getPemsalwu().getNombdi2());

        GeographicGroupsBO departament = new GeographicGroupsBO();
        departament.setGeographicGroupType(new GeographicGroupTypeBO());
        departament.setName(result.getPemsalw4().getDesdept());
        departament.setCode(result.getPemsalwu().getCodigod());

        GeographicGroupsBO province = new GeographicGroupsBO();
        province.setGeographicGroupType(new GeographicGroupTypeBO());
        province.setName(result.getPemsalw4().getDesprov());
        province.setCode(result.getPemsalwu().getCodigop());

        GeographicGroupsBO  district = new GeographicGroupsBO();
        district.setGeographicGroupType(new GeographicGroupTypeBO());
        district.setName(result.getPemsalw4().getDesdist());
        district.setCode(result.getPemsalwu().getCodigdi());

        GeographicGroupsBO  extNumber = new GeographicGroupsBO();
        extNumber.setGeographicGroupType(new GeographicGroupTypeBO());
        extNumber.setName(result.getPemsalwu().getNroext1());

        GeographicGroupsBO  intNumber = new GeographicGroupsBO();
        intNumber.setGeographicGroupType(new GeographicGroupTypeBO());
        intNumber.setName(result.getPemsalwu().getNroint1());

        GeographicGroupsBO ubigeo = new GeographicGroupsBO();
        String ubi = result.getPemsalwu().getCodigod() + result.getPemsalwu().getCodigop() + result.getPemsalwu().getCodigdi() ;
        ubigeo.setGeographicGroupType(new GeographicGroupTypeBO());
        ubigeo.setCode(ubi);

        List<GeographicGroupsBO> geographicGroups = new ArrayList<>();
        geographicGroups.add(avenida);
        geographicGroups.add(uncategory);
        geographicGroups.add(departament);
        geographicGroups.add(province);
        geographicGroups.add(district);
        geographicGroups.add(extNumber);
        geographicGroups.add(intNumber);
        geographicGroups.add(ubigeo);
        /* map geographicGroup ? */
        location.setGeographicGroups(geographicGroups);

        address.setLocation(location);
        addresses.add(address);
        customer.setAddresses(addresses);
        /* section addresses */


        LOGGER.info("***** CustomerListAsoBean - executeGetListCustomer End ***** customerBO: {}", customer);
        return customer;
    }

}
