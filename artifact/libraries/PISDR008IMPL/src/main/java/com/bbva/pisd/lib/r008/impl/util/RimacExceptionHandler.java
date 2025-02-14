package com.bbva.pisd.lib.r008.impl.util;

import com.bbva.pisd.dto.insurance.bo.ErrorResponseBO;
import com.bbva.pisd.dto.insurance.bo.ErrorRimacBO;
import com.bbva.pisd.dto.insurance.bo.SelectionQuotationPayloadBO;
import com.bbva.pisd.dto.insurance.utils.PISDErrors;
import com.bbva.pisd.dto.insurance.utils.PISDValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

public class RimacExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RimacExceptionHandler.class);
    private static final String ERROR_CODE_001 = "VIDA001";
    private static final String STATUS_BLOCKED = "1";

    public SelectionQuotationPayloadBO handler(RestClientException exception) {
        if(exception instanceof HttpClientErrorException) {
            LOGGER.info("RimacExceptionHandler - HttpClientErrorException");
            return this.clientExceptionHandler((HttpClientErrorException) exception);
        } else {
            LOGGER.info("RimacExceptionHandler - HttpServerErrorException");
            throw PISDValidation.build(PISDErrors.RIMAC_SERVER_ERROR);
        }
    }

    private SelectionQuotationPayloadBO clientExceptionHandler(HttpClientErrorException exception) {
        LOGGER.debug("HttpClientErrorException - Response body: {}", exception.getResponseBodyAsString());
        ErrorRimacBO errorObject = this.getErrorObject(exception.getResponseBodyAsString());
        return this.throwingBusinessException(errorObject.getError());
    }

    private ErrorRimacBO getErrorObject(String responseBody) {
        return JsonHelper.getInstance().deserialization(responseBody, ErrorRimacBO.class);
    }

    private SelectionQuotationPayloadBO throwingBusinessException(ErrorResponseBO error) {
        LOGGER.debug("Exception error code -> {}", error.getCode());
        SelectionQuotationPayloadBO output = new SelectionQuotationPayloadBO();
        if(error.getCode().equals(ERROR_CODE_001) && !error.getDetails().isEmpty()) {
            output.setStatus(STATUS_BLOCKED);
            output.setMensaje(PISDErrors.ERROR_AGE_VALIDATION_BLACKLIST.getMessage());
            return output;
        }else{
            return null;
        }

    }
}
