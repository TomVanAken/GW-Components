package com.gw.common;

/**
 * @author Group-Wave NV
 * Factory to get application specific objects.
 * 
 */
public class ApplicationSetup {
	/**
	 * @return ConfigurationManager used for this application
	 * returns the configMgr bean from the Faces-config
	 */
	public static ConfigurationManager getConfigurationManager() {
		return new ConfigurationManager();
	}
	
}
