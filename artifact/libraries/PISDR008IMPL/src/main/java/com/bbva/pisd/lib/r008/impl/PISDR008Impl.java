package com.bbva.pisd.lib.r008.impl;

import com.bbva.pbtq.dto.validatedocument.response.host.pewu.PEWUResponse;
import com.bbva.pisd.dto.insurance.amazon.SignatureAWS;

import com.bbva.pisd.dto.insurance.aso.BlackListASO;
import com.bbva.pisd.dto.insurance.aso.CustomerListASO;

import com.bbva.pisd.dto.insurance.blacklist.BlackListRequestRimacDTO;

import com.bbva.pisd.dto.insurance.bo.*;

import com.bbva.pisd.dto.insurance.bo.customer.CustomerBO;
import com.bbva.pisd.dto.insurance.commons.IdentityDataDTO;

import com.bbva.pisd.dto.insurance.utils.PISDConstants;
import com.bbva.pisd.dto.insurance.utils.PISDErrors;
import com.bbva.pisd.dto.insurance.utils.PISDProperties;

import com.bbva.pisd.lib.r008.impl.beans.CustomerBoBean;
import com.bbva.pisd.lib.r008.impl.util.JsonHelper;
import com.bbva.pisd.lib.r008.impl.util.RimacExceptionHandler;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.web.client.RestClientException;

import javax.ws.rs.HttpMethod;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Collections;

import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class PISDR008Impl extends PISDR008Abstract {

	private static final Logger LOGGER = LoggerFactory.getLogger(PISDR008Impl.class);
	private static final String AUTHORIZATION = "Authorization";
	private static final String CUSTOMER_ID = "customerId";

	@Override
	public BlackListIndicatorBO executeGetBlackListIndicatorService(final String customerId) {
		LOGGER.info("***** PISDR008Impl - executeGetBlackListIndicatorService START *****");
		LOGGER.info("***** PISDR008Impl - executeGetBlackListIndicatorService ***** customerId: {} ", customerId);

		BlackListIndicatorBO output = null;

		if (customerId != null) {
			Map<String, String> map = singletonMap("customerid", customerId);

			try {
				BlackListASO response = this.internalApiConnector.getForObject(
						PISDProperties.ID_API_BLACKLIST_ASO.getValue(), BlackListASO.class, map);
				output = response.getData().stream().filter(s -> s.getIndicatorId().equals("INE")).findAny().orElse(null);
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
	public SelectionQuotationPayloadBO executeGetBlackListRiskService(final IdentityDataDTO payload, final String traceId) {
		LOGGER.info("***** PISDR008Impl - executeGetBlackListRiskService START *****");

		String requestJson = getRequestBodyAsJsonString(new BlackListRequestRimacDTO(payload));

		LOGGER.info("***** PISDR008Impl - executeGetBlackListRiskService ***** Params: {} - {}", requestJson, traceId);

		SelectionQuotationPayloadBO output = null;

		String uri = "";
		String apiBlackListId = "";

		if(Boolean.TRUE.equals(validateLifeProduct(payload))) {
			uri = PISDProperties.URI_BLACKLIST_EASYYES.getValue();
			apiBlackListId = PISDProperties.ID_API_BLACKLISTEASYYES_RIMAC.getValue();
		} else {
			uri = PISDProperties.URI_BLACKLIST_RISK.getValue();
			apiBlackListId = PISDProperties.ID_API_BLACKLISTRISK_RIMAC.getValue();
		}

		SignatureAWS signatureAWS = this.pisdR014.executeSignatureConstruction(requestJson, HttpMethod.POST, uri, null, traceId);

		HttpEntity<String> entity = new HttpEntity<>(requestJson, createHttpHeadersAWS(signatureAWS));

		try {
			BlackListRiskRimacBO response = this.externalApiConnector.postForObject(apiBlackListId, entity, BlackListRiskRimacBO.class);
			output = response.getPayload().get(0);
			output.setMensaje("");
		} catch(RestClientException e) {
			LOGGER.info("***** PISDR008Impl - executeGetBlackListRiskService ***** Exception: {}", e.getMessage());
			RimacExceptionHandler exceptionHandler = new RimacExceptionHandler();
			output = exceptionHandler.handler(e);
			if (isNull(output)){
				this.addAdvice(PISDErrors.ERROR_TO_CONNECT_SERVICE_BLACKLISTRISK_RIMAC.getAdviceCode());
			}
		}

		LOGGER.info("***** PISDR008Impl - executeGetBlackListRiskService ***** Response: {}", output);
		LOGGER.info("***** PISDR008Impl - executeGetBlackListRiskService END *****");
		return output;
	}

	@Override
	public SelectionQuotationPayloadBO executeGetBlackListHealthService(final IdentityDataDTO payload, final String traceId) {
		LOGGER.info("***** PISDR008Impl - executeGetBlackListHealthService START *****");

		String requestJson = getRequestBodyAsJsonString(new BlackListRequestRimacDTO(payload));

		LOGGER.info("***** PISDR008Impl - executeGetBlackListHealthService ***** Params: {} - {}", requestJson, traceId);

		String uri = PISDProperties.URI_BLACKLIST_HEALTH.getValue();

		SignatureAWS signatureAWS = this.pisdR014.executeSignatureConstruction(requestJson, HttpMethod.POST, uri, null, traceId);

		HttpEntity<String> entity = new HttpEntity<>(requestJson, createHttpHeadersAWS(signatureAWS));

		SelectionQuotationPayloadBO output = null;

		try {
			BlackListHealthRimacBO response = this.externalApiConnector.postForObject(PISDProperties.ID_API_BLACKLISTHEALTH_RIMAC.getValue(),
					entity, BlackListHealthRimacBO.class);
			output = response.getPayload();
		} catch(RestClientException e) {
			LOGGER.info("***** PISDR008Impl - executeGetBlackListHealthService ***** Exception: {}", e.getMessage());
			this.addAdvice(PISDErrors.ERROR_TO_CONNECT_SERVICE_BLACKLISTHEALTH_RIMAC.getAdviceCode());
		}

		LOGGER.info("***** PISDR008Impl - executeGetBlackListHealthService ***** Response: {}", output);
		LOGGER.info("***** PISDR008Impl - executeGetBlackListHealthService END *****");
		return output;
	}

	@Override
	public CustomerListASO executeGetCustomerInformation(final String customerId) {
		LOGGER.info("***** PISDR008Impl - executeGetCustomerInformation START *****");

		if(nonNull(customerId)) {
			Map<String, Object> pathParams = singletonMap(CUSTOMER_ID, customerId);

			try {
				CustomerListASO responseList = this.internalApiConnector.getForObject(PISDProperties.ID_API_CUSTOMER_INFORMATION.getValue(),
						CustomerListASO.class, pathParams);
				LOGGER.info("***** PISDR008Impl - executeGetCustomerInformation END ***** ");
				return responseList;
			} catch(RestClientException e) {
				LOGGER.info("***** PISDR008Impl - executeGetCustomerInformation ***** Exception: {}", e.getMessage());
				return null;
			}
		} else {
			LOGGER.info("***** PISDR008Impl - executeGetCustomerInformation ***** Customer id wasn't sent");
			return null;
		}
	}

	@Override
	public CustomerBO executeGetCustomerHost(String customerId) {
		LOGGER.info("***** RBVDR301Impl - executeGetListCustomer Start *****");
		LOGGER.info("***** RBVDR301Impl - executeGetListCustomer customerId {} *****",customerId);
		PEWUResponse result = this.pbtqR002.executeSearchInHostByCustomerId(customerId);
		LOGGER.info("***** RBVDR301Impl - executeGetListCustomer  ***** Response Host: {}", result);

		if( Objects.isNull(result.getHostAdviceCode()) || result.getHostAdviceCode().isEmpty()){
			CustomerBoBean customerListAsoBean = new CustomerBoBean(this.applicationConfigurationService);
			return customerListAsoBean.mapperCustomer(result);
		}
		this.addAdviceWithDescription(result.getHostAdviceCode(), result.getHostMessage());
		LOGGER.info("***** RBVDR301Impl - executeGetListCustomer ***** with error: {}", result.getHostMessage());
		return null;
	}

	private HttpHeaders createHttpHeadersAWS(final SignatureAWS signature) {
		HttpHeaders headers = new HttpHeaders();
		MediaType mediaType = new MediaType("application", "json", StandardCharsets.UTF_8);
		headers.setContentType(mediaType);
		headers.set(AUTHORIZATION, signature.getAuthorization());
		headers.set("X-Amz-Date", signature.getxAmzDate());
		headers.set("x-api-key", signature.getxApiKey());
		headers.set("traceId", signature.getTraceId());
		return headers;
	}

	private String getRequestBodyAsJsonString(final Object object) {
		return JsonHelper.getInstance().toJsonString(object);
	}


	/**
	 * Valida si el producto es EASY_YES o VIDADINAMICO
	 * @param payload
	 * @return boolean
	 */
	private Boolean validateLifeProduct(final IdentityDataDTO payload){
		List<String> productsLife = new ArrayList();
		productsLife.add(PISDConstants.ProductEasyYesLife.EASY_YES_RIMAC);
		productsLife.add(PISDConstants.ProductVidaDinamicoLife.VIDA_DINAMICO);
		return nonNull(payload.getProducto()) && productsLife.contains(payload.getProducto());
	}

}
