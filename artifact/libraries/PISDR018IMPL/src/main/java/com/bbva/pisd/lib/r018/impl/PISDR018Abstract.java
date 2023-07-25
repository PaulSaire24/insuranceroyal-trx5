package com.bbva.pisd.lib.r018.impl;

import com.bbva.elara.configuration.manager.application.ApplicationConfigurationService;
import com.bbva.elara.library.AbstractLibrary;
import com.bbva.pisd.lib.r008.PISDR008;
import com.bbva.pisd.lib.r018.PISDR018;
import com.bbva.pisd.lib.r018.impl.util.MapperHelper;

/**
 * This class automatically defines the libraries and utilities that it will use.
 */
public abstract class PISDR018Abstract extends AbstractLibrary implements PISDR018 {

	protected ApplicationConfigurationService applicationConfigurationService;

	protected PISDR008 pisdR008;

	protected MapperHelper mapperHelper;


	/**
	* @param applicationConfigurationService the this.applicationConfigurationService to set
	*/
	public void setApplicationConfigurationService(ApplicationConfigurationService applicationConfigurationService) {
		this.applicationConfigurationService = applicationConfigurationService;
	}

	/**
	* @param pisdR008 the this.pisdR008 to set
	*/
	public void setPisdR008(PISDR008 pisdR008) {
		this.pisdR008 = pisdR008;
	}

	public void setMapperHelper(MapperHelper mapperHelper) { this.mapperHelper = mapperHelper; }

}