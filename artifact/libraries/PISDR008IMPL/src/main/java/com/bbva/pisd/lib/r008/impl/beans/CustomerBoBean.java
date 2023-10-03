package com.bbva.pisd.lib.r008.impl.beans;

import com.bbva.elara.configuration.manager.application.ApplicationConfigurationService;
import com.bbva.pbtq.dto.validatedocument.response.host.pewu.PEWUResponse;
import com.bbva.pisd.dto.insurance.bo.*;
import com.bbva.pisd.dto.insurance.bo.customer.CustomerBO;
import com.bbva.pisd.lib.r008.impl.util.Constans;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomerBoBean {
    private ApplicationConfigurationService applicationConfigurationService;

    public CustomerBoBean(ApplicationConfigurationService applicationConfigurationService) {
        this.applicationConfigurationService = applicationConfigurationService;
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerBoBean.class);

    public CustomerBO mapperCustomer(PEWUResponse result){

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
        identityDocumentsBO.getDocumentType().setId(this.applicationConfigurationService.getProperty(result.getPemsalwu().getTdoi()));

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

        /* section contact2 type, validate EMAIL */
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

        LOGGER.info("***** CustomerListAsoBean - executeGetListCustomer End ***** customerBO: {}", customer);
        return customer;
    }

}
