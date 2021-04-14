package com.bbva.pisd.lib.r018;

import com.bbva.pisd.dto.insurance.blacklist.EntityOutBlackListDTO;
import com.bbva.pisd.dto.insurance.blacklist.InsuranceBlackListDTO;

public interface PISDR018 {

	EntityOutBlackListDTO executeBlackListValidation(InsuranceBlackListDTO input);

}
