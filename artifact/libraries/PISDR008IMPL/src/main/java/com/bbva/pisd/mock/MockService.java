package com.bbva.pisd.mock;

import com.bbva.elara.configuration.manager.application.ApplicationConfigurationService;
import com.bbva.pisd.dto.insurance.aso.BlackListASO;
import com.bbva.pisd.dto.insurance.bo.BlackListHealthRimacBO;
import com.bbva.pisd.dto.insurance.bo.BlackListRiskRimacBO;
import com.bbva.pisd.dto.insurance.utils.PISDConstants;
import com.bbva.pisd.dto.insurance.utils.PISDProperties;
import com.bbva.pisd.lib.r008.impl.util.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockService.class);

    protected ApplicationConfigurationService applicationConfigurationService;

    public boolean isEnabled() {

        String value = this.applicationConfigurationService.getProperty(PISDProperties.ASO_MOCK_ENABLED.getValue());

        boolean result = Boolean.parseBoolean(value);
        if(result) LOGGER.info("***** mockService: Mock enabled *****");
        return result;
    }

    public boolean isEnabledTierMock() {
        String value = this.applicationConfigurationService.getProperty(PISDProperties.ASO_MOCK_TIER_ENABLED.getValue());
        boolean result = Boolean.parseBoolean(value);
        if(result) LOGGER.info("***** mockService: TIER SERVICE MOCK ENABLED *****");
        return result;
    }

    public boolean isEnabled(String mockType) {
        String value;
        switch (mockType) {
            case PISDConstants.MOCKERSEARCH:
                value = this.applicationConfigurationService.getProperty(PISDProperties.ASO_MOCK_SEARCH_ENABLED.getValue());
                break;
            case PISDConstants.MOCKERBBVA:
                value = this.applicationConfigurationService.getProperty(PISDProperties.ASO_MOCK_BBVA_ENABLED.getValue());
                break;
            case PISDConstants.MOCKERRIMAC:
                value = this.applicationConfigurationService.getProperty(PISDProperties.ASO_MOCK_RIMAC_ENABLED.getValue());
                break;
            default:
                return false;
        }
        boolean result = Boolean.parseBoolean(value);
        if(result) LOGGER.info("***** mockService: Mock enabled *****");
        return result;
    }

    public BlackListASO getBlackListBBVAMock() {
        LOGGER.info("***** mockService getBlackListBBVAMock *****");
        return JsonHelper.getInstance().fromString(
                applicationConfigurationService.getProperty(PISDProperties.ASO_GET_BLACKLISTBBVA_MOCK.getValue()),
                BlackListASO.class);
    }

    public BlackListRiskRimacBO getBlackListRiskRimacMock() {
        LOGGER.info("***** mockService getBlackListRimacMock *****");
        return JsonHelper.getInstance().fromString(
                applicationConfigurationService.getProperty(PISDProperties.ASO_GET_BLACKLISTRISKRIMAC_MOCK.getValue()),
                BlackListRiskRimacBO.class);
    }

    public BlackListHealthRimacBO getBlackListHealthRimacMock() {
        LOGGER.info("***** mockService getBlackListRimacMock *****");
        return JsonHelper.getInstance().fromString(
                applicationConfigurationService.getProperty(PISDProperties.ASO_GET_BLACKLISTHEALTHRIMAC_MOCK.getValue()),
                BlackListHealthRimacBO.class);
    }

    public void setApplicationConfigurationService(ApplicationConfigurationService applicationConfigurationService) {
        this.applicationConfigurationService = applicationConfigurationService;
    }
}
