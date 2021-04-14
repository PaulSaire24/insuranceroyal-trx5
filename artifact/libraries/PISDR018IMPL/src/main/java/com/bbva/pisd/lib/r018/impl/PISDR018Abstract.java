package com.bbva.pisd.lib.r018.impl;

import com.bbva.elara.configuration.manager.application.ApplicationConfigurationService;
import com.bbva.elara.library.AbstractLibrary;
import com.bbva.pisd.lib.r011.PISDR011;
import com.bbva.pisd.lib.r018.PISDR018;

/**
 * This class automatically defines the libraries and utilities that it will use.
 */
public abstract class PISDR018Abstract extends AbstractLibrary implements PISDR018 {

	protected ApplicationConfigurationService applicationConfigurationService;

	protected PISDR011 pisdR011;

	/**
	* @param applicationConfigurationService the this.applicationConfigurationService to set
	*/
	public void setApplicationConfigurationService(ApplicationConfigurationService applicationConfigurationService) { this.applicationConfigurationService = applicationConfigurationService; }

	/**
	* @param pisdR011 the this.pisdR011 to set
	*/
	public void setPisdR011(PISDR011 pisdR011) { this.pisdR011 = pisdR011; }

}