package com.bbva.pisd.lib.r018;


import com.bbva.pisd.dto.insurance.bo.customer.CustomerBO;
import com.bbva.pisd.lib.r018.impl.util.ObjectMapperHelper;
import java.io.IOException;

public final class EntityMock {

    private static final EntityMock INSTANCE = new EntityMock();
    private final ObjectMapperHelper objectMapper = ObjectMapperHelper.getInstance();

    private EntityMock() {
    }

    public static EntityMock getInstance() {
        return INSTANCE;
    }

    public CustomerBO getCustomerDataResponseBO() throws IOException {
        return objectMapper.readValue(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("mock/simulatePurchase.json"), CustomerBO.class);
    }
}
