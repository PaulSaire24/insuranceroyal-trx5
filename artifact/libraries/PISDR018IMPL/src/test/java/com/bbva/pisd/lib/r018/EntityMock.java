package com.bbva.rbvd.lib.r301;


import com.bbva.rbvd.lib.r301.impl.util.ObjectMapperHelper;
import com.bbva.t3ym.niubizsimulatepurchasev0.business.v0.dao.model.t3ymt001_1.ResponseTransactionT3ymt001_1;
import com.bbva.t3ym.niubizsimulatepurchasev0.business.v0.dto.BDtoInCreateSimulatePurchasePost;
import com.bbva.t3ym.niubizsimulatepurchasev0.business.v0.dto.BDtoOutCreateSimulatePurchasePost;
import com.bbva.t3ym.niubizsimulatepurchasev0.facade.v0.dto.DtoInCreateSimulatePurchasePost;
import com.bbva.t3ym.niubizsimulatepurchasev0.util.helper.ObjectMapperHelper;

import java.io.IOException;

public final class EntityMock {

    private static final EntityMock INSTANCE = new EntityMock();
    private ObjectMapperHelper objectMapper = ObjectMapperHelper.getInstance();

    private EntityMock() {
    }

    public static EntityMock getInstance() {
        return INSTANCE;
    }

    public DtoInCreateSimulatePurchasePost getRDtoSimulatePurchase() throws IOException {
        return objectMapper.readValue(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("mock/simulatePurchase.json"), DtoInCreateSimulatePurchasePost.class);
    }
}
