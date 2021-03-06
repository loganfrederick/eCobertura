/*
 * This file is part of eCobertura.
 * 
 * Copyright (c) 2009, 2010 Joachim Hofer
 * All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ecobertura.ui.launching.config

import java.util.logging._

import org.eclipse.core.runtime._
import org.eclipse.debug.core.ILaunchManager
import org.eclipse.debug.ui._

object ConfigurationTabGroupRetriever {
	val logger = Logger.getLogger("ecobertura.ui.launching.config") //$NON-NLS-1$
	
	def fromConfig(config: IConfigurationElement): ILaunchConfigurationTabGroup = {
		new ConfigurationTabGroupRetriever(config).getConfigurationTabGroup
	}
}

class ConfigurationTabGroupRetriever(config: IConfigurationElement) {
	import ConfigurationTabGroupRetriever.logger
	
	var launchTypeId : String = null 
	
	def getConfigurationTabGroup = {
		launchTypeId = config.getAttribute("type") //$NON-NLS-1$
		val launchConfigTabGroups = Platform.getExtensionRegistry.getExtensionPoint(
				IDebugUIConstants.PLUGIN_ID, 
				IDebugUIConstants.EXTENSION_POINT_LAUNCH_CONFIGURATION_TAB_GROUPS)
		getTabGroupFromExtensionPoint(launchConfigTabGroups)
		
	}
	
	private def getTabGroupFromExtensionPoint(launchConfigTabGroups: IExtensionPoint) = {
		val launchConfigTabGroup = launchConfigTabGroups.getConfigurationElements.find(delegateConfig =>
				hasCorrectType(delegateConfig) && hasRunMode(delegateConfig))
		launchConfigTabGroup match {
			case Some(launchConfigTabGroupFound) => 
				getTabGroupFromConfigurationElement(launchConfigTabGroupFound)
			case None => {
				logger.warning(String.format(
					"No standard launch configuration tab group for launch type '%s' found.", //$NON-NLS-1$ 
					launchTypeId)) 
				null
			}
		}
	}

	private def hasCorrectType(delegateConfig: IConfigurationElement) = 
		launchTypeId.equals(delegateConfig.getAttribute("type")) //$NON-NLS-1$
		
	private def hasRunMode(delegateConfig: IConfigurationElement) = {
		delegateConfig.getChildren("launchMode").exists( //$NON-NLS-1$
			launchModeConfig => ILaunchManager.RUN_MODE == launchModeConfig.getAttribute("mode")) //$NON-NLS-1$
	}
	
	private def getTabGroupFromConfigurationElement(delegateConfig: IConfigurationElement) = {
		try {
			delegateConfig.createExecutableExtension("class").asInstanceOf[ILaunchConfigurationTabGroup] //$NON-NLS-1$
		} catch {
			case e: CoreException => {
				logger.log(Level.WARNING, "unable to create launch configuration tab group", e) //$NON-NLS-1$
				null
			}
		}
	}
}
