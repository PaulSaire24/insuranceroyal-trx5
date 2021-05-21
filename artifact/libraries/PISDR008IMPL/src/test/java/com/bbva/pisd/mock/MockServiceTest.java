package com.bbva.pisd.mock;

import com.bbva.elara.configuration.manager.application.ApplicationConfigurationService;
import com.bbva.pisd.dto.insurance.aso.BlackListASO;
import com.bbva.pisd.dto.insurance.bo.BlackListHealthRimacBO;
import com.bbva.pisd.dto.insurance.bo.BlackListRiskRimacBO;
import com.bbva.pisd.dto.insurance.utils.PISDConstants;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockServiceTest {

    private MockService mockService = new MockService();
    private ApplicationConfigurationService applicationConfigurationService;

    @Before
    public void setUp() {
        applicationConfigurationService = mock(ApplicationConfigurationService.class);
        mockService.setApplicationConfigurationService(applicationConfigurationService);
    }

    @Test
    public void isEnabledTrue() {
        when(applicationConfigurationService.getProperty(anyString())).thenReturn("true");
        boolean validation = mockService.isEnabled();
        assertTrue(validation);

        validation = mockService.isEnabled(PISDConstants.MOCKERBBVA);
        assertTrue(validation);
        validation = mockService.isEnabled(PISDConstants.MOCKERRIMAC);
        assertTrue(validation);
        validation = mockService.isEnabled(PISDConstants.MOCKERSEARCH);
        assertTrue(validation);
    }

    @Test
    public void isEnabledFalse() {
        when(applicationConfigurationService.getProperty(anyString())).thenReturn("false");
        boolean validation = mockService.isEnabled();
        assertFalse(validation);

        validation = mockService.isEnabled("");
        assertFalse(validation);
        validation = mockService.isEnabled(PISDConstants.MOCKERBBVA);
        assertFalse(validation);
        validation = mockService.isEnabled(PISDConstants.MOCKERRIMAC);
        assertFalse(validation);
        validation = mockService.isEnabled(PISDConstants.MOCKERSEARCH);
        assertFalse(validation);
    }

    @Test
    public void getBlackListBBVAMockOK() {
        String responseMock = "{"
                + " \"data\": [ {"
                + " \"indicatorId\": \"PEP\","
                + " \"name\": \"PERSONA EXPUESTA POLITICAMENTE\","
                + " \"isActive\": false },"
                + " { \"indicatorId\": \"INE\","
                + " \"name\": \"PERSONA HA TENIDO COMPORTAMIENTO INADEC.\","
                + " \"isActive\": false },"
                + " { \"indicatorId\": \"EMPLOYEE\","
                + " \"name\": \"PERSONA EMPLEADO BANCO\","
                + " \"isActive\": false },"
                + " { \"indicatorId\": \"MEDIA_COMMUNICATION_PERSON\","
                + " \"name\": \"PERSONA MEDIATICA\","
                + " \"isActive\": false },"
                + " { \"indicatorId\": \"FATCA\","
                + " \"name\": \"PERSONA FATCA\","
                + " \"isActive\": false"
                + " } ] }";
        when(applicationConfigurationService.getProperty(anyString())).thenReturn(responseMock);
        BlackListASO validation = mockService.getBlackListBBVAMock();
        assertNotNull(validation);
    }

    @Test
    public void getBlackListHealthRimacMockOK() {
        String responseMock = "{"
                + " \"payload\": {"
                + " \"status\": \"1\","
                + " \"mensaje\": \"El Dni con número 22462260 si cuenta con registro por rechazo de salud.\""
                + " } }";
        when(applicationConfigurationService.getProperty(anyString())).thenReturn(responseMock);
        BlackListHealthRimacBO validation = mockService.getBlackListHealthRimacMock();
        assertNotNull(validation);
    }

    @Test
    public void getBlackListRiskRimacMockOK() {
        String responseMock = "{"
                + " \"payload\": [ {"
                + " \"status\": \"0\","
                + " \"mensaje\": \"El Dni con número 00000000 no se encuentra registrado.\""
                + " } ] }";
        when(applicationConfigurationService.getProperty(anyString())).thenReturn(responseMock);
        BlackListRiskRimacBO validation = mockService.getBlackListRiskRimacMock();
        assertNotNull(validation);
    }

    @Test
    public void isEnabledTierMockTrue() {
        when(applicationConfigurationService.getProperty(anyString())).thenReturn("true");
        boolean validation = mockService.isEnabledTierMock();
        assertTrue(validation);
    }

    @Test
    public void isEnabledTierMockFalse() {
        when(applicationConfigurationService.getProperty(anyString())).thenReturn("false");
        boolean validation = mockService.isEnabledTierMock();
        assertFalse(validation);
    }

}
