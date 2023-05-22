package com.bbva.pisd.lib.r008.impl;

import com.bbva.pisd.dto.insurance.amazon.SignatureAWS;
import com.bbva.pisd.dto.insurance.aso.BlackListASO;
import com.bbva.pisd.dto.insurance.aso.CustomerListASO;
import com.bbva.pisd.dto.insurance.blacklist.BlackListRequestRimacDTO;
import com.bbva.pisd.dto.insurance.bo.BlackListHealthRimacBO;
import com.bbva.pisd.dto.insurance.bo.BlackListIndicatorBO;
import com.bbva.pisd.dto.insurance.bo.BlackListRiskRimacBO;
import com.bbva.pisd.dto.insurance.bo.SelectionQuotationPayloadBO;
import com.bbva.pisd.dto.insurance.commons.IdentityDataDTO;
import com.bbva.pisd.dto.insurance.utils.PISDConstants;
import com.bbva.pisd.dto.insurance.utils.PISDErrors;
import com.bbva.pisd.dto.insurance.utils.PISDProperties;
import com.bbva.pisd.lib.r008.impl.util.JsonHelper;
import com.bbva.pisd.lib.r008.impl.util.RimacExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;

import javax.ws.rs.HttpMethod;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class PISDR008Impl extends PISDR008Abstract {

	private static final Logger LOGGER = LoggerFactory.getLogger(PISDR008Impl.class);
	private static final String AUTHORIZATION = "Authorization";
	private static final String JSON_LOG = "JSON BODY TO SEND: {}";
	private static final String CUSTOMER_ID = "customerId";

	@Override
	public BlackListIndicatorBO executeGetBlackListIndicatorService(String customerId) {
		LOGGER.info("***** PISDR008Impl - executeGetBlackListIndicatorService START *****");
		LOGGER.info("***** PISDR008Impl - executeGetBlackListIndicatorService ***** customerId: {} ", customerId);

		if(this.mockService.isEnabled(PISDConstants.MOCKERBBVA)) {
			LOGGER.info("***** PISDR008Impl - asoMockService getBlackListBBVAMock Invokation *****");
			BlackListASO mockresp = this.mockService.getBlackListBBVAMock();
			LOGGER.info("***** PISDR008Impl - asoMockService getBlackListBBVAMock Invokation ***** mockresp: {} ", mockresp);
			return mockresp.getData().stream().filter(s -> s.getIndicatorId().equals("INE")).findAny().orElse(null);
		}

		BlackListIndicatorBO output = null;

		if (customerId != null) {

			Map<String, String> map = new HashMap<>();
			map.put("customerid", customerId);

			try {
				BlackListASO response = this.internalApiConnector.getForObject(
						PISDProperties.ID_API_BLACKLIST_ASO.getValue(), BlackListASO.class, map);
				if (response != null && response.getData() != null) {
					output = response.getData().stream().filter(s -> s.getIndicatorId().equals("INE")).findAny().orElse(null);
				}
			} catch(RestClientException e) {
				LOGGER.info("***** PISDR008Impl - executeGetBlackListIndicatorService ***** Exception: {}", e.getMessage());
				this .addAdvice(PISDErrors.ERROR_TO_CONNECT_SERVICE_BLACKLIST_ASO.getAdviceCode());
			}
		}

		LOGGER.info("***** PISDR008Impl - executeGetBlackListIndicatorService ***** Response: {}", output);
		LOGGER.info("***** PISDR008Impl - executeGetBlackListIndicatorService END *****");

		return output;
	}

	@Override
	public SelectionQuotationPayloadBO executeGetBlackListRiskService(IdentityDataDTO payload, String traceId) {
		LOGGER.info("***** PISDR008Impl - executeGetBlackListRiskService START *****");
		LOGGER.info("***** PISDR008Impl - executeGetBlackListRiskService ***** Params: {}", payload);

		if(this.mockService.isEnabled(PISDConstants.MOCKERRIMAC)) {
			LOGGER.info("***** PISDR008Impl - asoMockService getBlackListRiskRimacMock Invokation *****");
			SelectionQuotationPayloadBO mockresp = this.mockService.getBlackListRiskRimacMock().getPayload().get(0);
			LOGGER.info("***** PISDR008Impl - asoMockService getBlackListRiskRimacMock Invokation ***** mockresp: {} ", mockresp);
			return mockresp;
		}

		SelectionQuotationPayloadBO output = null;

		if (Boolean.TRUE.equals(validateDocumentoIdentidad(payload))) {
			String uri = Boolean.TRUE.equals(validateEasyYesProduct(payload)) ? PISDProperties.URI_BLACKLIST_EASYYES.getValue() : PISDProperties.URI_BLACKLIST_RISK.getValue();

			String requestJson = JsonHelper.getInstance().toJsonString(new BlackListRequestRimacDTO(payload));

			SignatureAWS signatureAWS = this.pisdR014.executeSignatureConstruction(requestJson, HttpMethod.POST, uri, null, traceId);

			HttpEntity<String> entity = new HttpEntity<>(requestJson, createHttpHeadersAWS(signatureAWS));
			LOGGER.info(JSON_LOG, entity.getBody());

			try {
				String apiBlackListId = Boolean.TRUE.equals(validateEasyYesProduct(payload)) ? PISDProperties.ID_API_BLACKLISTEASYYES_RIMAC.getValue() : PISDProperties.ID_API_BLACKLISTRISK_RIMAC.getValue();
				BlackListRiskRimacBO response = this.externalApiConnector.postForObject(apiBlackListId, entity, BlackListRiskRimacBO.class);
				if (response != null && response.getPayload() != null && !response.getPayload().isEmpty()) {
					output = response.getPayload().get(0);
				}
			} catch(RestClientException e) {
				LOGGER.info("***** PISDR008Impl - executeGetBlackListRiskService ***** Exception: {}", e.getMessage());
				RimacExceptionHandler exceptionHandler = new RimacExceptionHandler();
				output = exceptionHandler.handler(e);
				if (isNull(output)){
					this .addAdvice(PISDErrors.ERROR_TO_CONNECT_SERVICE_BLACKLISTRISK_RIMAC.getAdviceCode());
				}
			}
		}
		if (isNull(output)){
			this .addAdvice(PISDErrors.ERROR_TO_CONNECT_SERVICE_BLACKLISTRISK_RIMAC.getAdviceCode());
		}

		LOGGER.info("***** PISDR008Impl - executeGetBlackListRiskService ***** Response: {}", output);
		LOGGER.info("***** PISDR008Impl - executeGetBlackListRiskService END *****");
		return output;
	}

	private Boolean validateEasyYesProduct(IdentityDataDTO payload){
		return nonNull(payload.getProducto()) && payload.getProducto().equals(PISDConstants.ProductEasyYesLife.EASY_YES_RIMAC);
	}

	private Boolean validateDocumentoIdentidad(IdentityDataDTO payload){
		return payload != null && payload.getNroDocumento() != null && payload.getTipoDocumento() != null;
	}

	@Override
	public SelectionQuotationPayloadBO executeGetBlackListHealthService(IdentityDataDTO payload, String traceId) {
		LOGGER.info("***** PISDR008Impl - executeGetBlackListHealthService START *****");
		LOGGER.info("***** PISDR008Impl - executeGetBlackListHealthService ***** Params: {} - {}", payload, traceId);

		if(this.mockService.isEnabled(PISDConstants.MOCKERRIMAC)) {
			LOGGER.info("***** PISDR008Impl - asoMockService getBlackListHealthRimacMock Invokation *****");
			SelectionQuotationPayloadBO mockresp = this.mockService.getBlackListHealthRimacMock().getPayload();
			LOGGER.info("***** PISDR008Impl - asoMockService getBlackListRiskRimacMock Invokation ***** mockresp: {} ", mockresp);
			return mockresp;
		}

		SelectionQuotationPayloadBO output = null;
		if (payload != null && payload.getNroDocumento() != null && payload.getTipoDocumento() != null) {
			String uri = PISDProperties.URI_BLACKLIST_HEALTH.getValue();
			String requestJson = JsonHelper.getInstance().toJsonString(new BlackListRequestRimacDTO(payload));

			SignatureAWS signatureAWS = this.pisdR014.executeSignatureConstruction(requestJson, HttpMethod.POST, uri, null, traceId);

			HttpEntity<String> entity = new HttpEntity<>(requestJson, createHttpHeadersAWS(signatureAWS));
			LOGGER.info(JSON_LOG, entity.getBody());

			try {
				BlackListHealthRimacBO response = this.externalApiConnector.postForObject(PISDProperties.ID_API_BLACKLISTHEALTH_RIMAC.getValue(),
						entity, BlackListHealthRimacBO.class);
				if (response != null && response.getPayload() != null) {
					output = response.getPayload();
				}
			} catch(RestClientException e) {
				LOGGER.info("***** PISDR008Impl - executeGetBlackListHealthService ***** Exception: {}", e.getMessage());
				this .addAdvice(PISDErrors.ERROR_TO_CONNECT_SERVICE_BLACKLISTHEALTH_RIMAC.getAdviceCode());
			}
		}

		LOGGER.info("***** PISDR008Impl - executeGetBlackListHealthService ***** Response: {}", output);
		LOGGER.info("***** PISDR008Impl - executeGetBlackListHealthService END *****");
		return output;
	}

	private HttpHeaders createHttpHeadersAWS(SignatureAWS signature) {
		HttpHeaders headers = new HttpHeaders();
		MediaType mediaType = new MediaType("application", "json", StandardCharsets.UTF_8);
		headers.setContentType(mediaType);
		headers.set(AUTHORIZATION, signature.getAuthorization());
		headers.set("X-Amz-Date", signature.getxAmzDate());
		headers.set("x-api-key", signature.getxApiKey());
		headers.set("traceId", signature.getTraceId());
		return headers;
	}

	@Override
	public CustomerListASO executeGetCustomerInformation(String customerId) {
		LOGGER.info("***** PISDR008Impl - executeGetCustomerInformation START ***** customerId: {} ", customerId);


		Map<String, Object> pathParams = new HashMap<>();
		pathParams.put(CUSTOMER_ID, customerId);

		try {
			CustomerListASO responseList = this.internalApiConnector.getForObject(PISDProperties.ID_API_CUSTOMER_INFORMATION.getValue(),CustomerListASO.class,pathParams);
			LOGGER.info("***** PISDR008Impl - executeGetCustomerInformation END ***** ");
			return responseList;
		} catch(RestClientException e) {
			LOGGER.info("***** PISDR008Impl - executeGetCustomerInformation ***** Exception: {}", e.getMessage());
			this.addAdvice(PISDErrors.ERROR_CONNECTION_VALIDATE_CUSTOMER_SERVICE.getAdviceCode());
			return null;
		}
	}

}
