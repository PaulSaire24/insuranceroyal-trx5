package com.bbva.pisd.lib.r018.impl.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

public class ObjectMapperHelper {
    private static final ObjectMapperHelper INSTANCE = new ObjectMapperHelper();
    private ObjectMapper mapper = new ObjectMapper();

    private ObjectMapperHelper() {
    }

    public static ObjectMapperHelper getInstance() {
        return INSTANCE;
    }

    public <T> T readValue(InputStream src, Class<T> valueType) throws IOException {
        return this.mapper.readValue(src, valueType);
    }
}
