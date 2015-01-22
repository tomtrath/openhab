/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.samsungac.internal;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.binding.openhab.samsungac.communicator.AirConditioner;
import org.binding.openhab.samsungac.communicator.SsdpDiscovery;
import org.openhab.binding.samsungac.SamsungAcBindingProvider;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.binding.BindingProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Binding listening OpenHAB bus and send commands to Samsung Air Conditioner
 * devices when command is received.
 * 
 * @author Stein Tore Tøsse
 * @since 1.6.0
 */
public class SamsungAcBinding extends
		AbstractActiveBinding<SamsungAcBindingProvider> implements
		ManagedService {

	private static final Logger logger = LoggerFactory
			.getLogger(SamsungAcBinding.class);

	/**
	 * the refresh interval which is used to check for lost connections
	 * (optional, defaults to 60000ms)
	 */
	private long refreshInterval = 60000;

	private Map<String, AirConditioner> nameHostMapper = null;

	protected Map<String, Map<String, String>> deviceConfigCache = new HashMap<String, Map<String, String>>();

	public SamsungAcBinding() {
	}

	public void activate() {
		logger.info("active");
	}

	public void deactivate() {
		logger.info("deactive");
		// close any open connections
		for (AirConditioner connector : nameHostMapper.values()) {
			connector.disconnect();
		}
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void internalReceiveCommand(String itemName, Command command) {

		if (itemName != null && command != null) {
			logger.debug("InternalReceiveCommand +'" + itemName + "':'"
					+ command + "'");
			String hostName = getAirConditionerInstance(itemName);
			AirConditioner host = nameHostMapper.get(hostName);
			if (host == null) {
				return;
			}
			CommandEnum property = getProperty(itemName);

			String cmd = getCmdStringFromEnumValue(command, property);

			if (cmd != null) {
				sendCommand(host, property, cmd);
			} else
				logger.debug("Not sending for itemName: '" + itemName
						+ "' because property not implemented: '" + property
						+ "'");
		}
	}

	private String getCmdStringFromEnumValue(Command command,
			CommandEnum property) {
		String cmd = null;
		switch (property) {
		case AC_FUN_POWER:
			cmd = "ON".equals(command.toString()) ? "On" : "Off";
			break;
		case AC_FUN_WINDLEVEL:
			cmd = WindLevelEnum.getFromValue(command).toString();
			break;
		case AC_FUN_OPMODE:
			cmd = OperationModeEnum.getFromValue(command).toString();
			break;
		case AC_FUN_COMODE:
			cmd = ConvenientModeEnum.getFromValue(command).toString();
			break;
		case AC_FUN_DIRECTION:
			cmd = DirectionEnum.getFromValue(command).toString();
			break;
		case AC_FUN_TEMPSET:
			cmd = command.toString();
			break;
		default:
			cmd = command.toString();
			break;
		}
		return cmd;
	}

	private void sendCommand(AirConditioner aircon, CommandEnum property,
			String value) {
		try {
			logger.debug("Sending command: " + value + " to property:"
					+ property + " with ip:" + aircon.getIpAddress());
			aircon.sendCommand(property, value);
		} catch (Exception e) {
			logger.warn("Could not send value: '" + value + "' to property:'"
					+ property + "'");
		}
	}

	private String getAirConditionerInstance(String itemName) {
		for (BindingProvider provider : providers) {
			if (provider instanceof SamsungAcBindingProvider) {
				SamsungAcBindingProvider acProvider = (SamsungAcBindingProvider) provider;
				if (acProvider.getItemNames().contains(itemName)) {
					return acProvider.getAirConditionerInstance(itemName);
				}
			}
		}
		return null;
	}

	private CommandEnum getProperty(String itemName) {
		for (BindingProvider provider : providers) {
			if (provider instanceof SamsungAcBindingProvider) {
				SamsungAcBindingProvider acProvider = (SamsungAcBindingProvider) provider;
				if (acProvider.getItemNames().contains(itemName)) {
					return acProvider.getProperty(itemName);
				}
			}
		}
		return null;
	}

	private String getItemName(CommandEnum property) {
		for (BindingProvider provider : providers) {
			if (provider instanceof SamsungAcBindingProvider) {
				SamsungAcBindingProvider acProvider = (SamsungAcBindingProvider) provider;
				return acProvider.getItemName(property);
			}
		}
		return null;
	}

	/**
	 * @{inheritDoc
	 */
	public void updated(Dictionary<String, ?> config)
			throws ConfigurationException {
		Enumeration<String> keys = config.keys();

		Map<String, AirConditioner> hosts = new HashMap<String, AirConditioner>();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			if ("service.pid".equals(key)) {
				continue;
			}
			
			String[] parts = key.split("\\.");
			String hostname = parts[0];

			AirConditioner host = hosts.get(hostname);
			if (host == null) {
				host = new AirConditioner();
			}

			String value = ((String) config.get(key)).trim();

			if ("host".equals(parts[1])) {
				host.setIpAddress(value);
			}
			if ("mac".equals(parts[1])) {
				host.setMacAddress(value);
			}
			if ("token".equals(parts[1])) {
				host.setToken(value);
			}
			hosts.put(hostname, host);
		}
		nameHostMapper = hosts;
		
		if (nameHostMapper == null || nameHostMapper.size() == 0) {
			setProperlyConfigured(false);
			Map<String, String> discovered = SsdpDiscovery.discover();
			if (discovered != null && discovered.size() > 0) {
				logger.warn("We found an air conditioner. Please put the following in your configuration file: " +
						"\r\n samsungac:Livingroom.host=" + discovered.get("IP") +
						"\r\n samsungac:Livingroom.mac=" + discovered.get("MAC_ADDR"));
			} else {
				logger.warn("No Samsung Air Conditioner has been configured, and we could not find one either");
			}
		} else {
			setProperlyConfigured(true);
		}
	}

	@Override
	protected void execute() {
		if (!bindingsExist()) {
			logger.debug("There is no existing Samsung AC binding configuration => refresh cycle aborted!");
			return;
		}

		if (nameHostMapper == null) {
			logger.debug("Name host mapper not yet set. Aborted refresh");
			return;
		}

		for (Map.Entry<String, AirConditioner> entry : nameHostMapper
				.entrySet()) {
			AirConditioner host = entry.getValue();
			if (host.isConnected()) {
				getAndUpdateStatusForAirConditioner(host);
			} else {
				reconnectToAirConditioner(entry.getKey(), host);
			}
		}
	}

	private void reconnectToAirConditioner(String key, AirConditioner host) {
		logger.info(
				"Broken connection found for '{}', attempting to reconnect...",
				key);
		try {
			host.login();
		} catch (Exception e) {
			logger.debug(e.toString() + " : " + e.getCause().toString());
			logger.info(
					"Reconnect failed for '{}', will retry in {}s",
					key, refreshInterval / 1000);
		}
	}

	private void getAndUpdateStatusForAirConditioner(AirConditioner host) {
		Map<CommandEnum, String> status = new HashMap<CommandEnum, String>();
		try {
			status = host.getStatus();
		} catch (Exception e) {
			logger.debug("Could not get status.. returning..");
			return;
		}
		
		for (CommandEnum cmd : status.keySet()) {
			String item = getItemName(cmd);
			String value = status.get(cmd);
			if (item != null && value != null) {
				updateItemWithValue(cmd, item, value);
			}
		}
	}

	private void updateItemWithValue(CommandEnum cmd, String item, String value) {
		switch (cmd) {
		case AC_FUN_TEMPNOW:
		case AC_FUN_TEMPSET:
			postUpdate(item, DecimalType.valueOf(value));
			break;
		case AC_FUN_POWER:
			postUpdate(
					item,
					value.toUpperCase().equals("ON") ? OnOffType.ON
							: OnOffType.OFF);
			break;
		case AC_FUN_COMODE:
			postUpdate(item,
					DecimalType.valueOf(Integer
							.toString(ConvenientModeEnum
									.valueOf(value).value)));
			break;
		case AC_FUN_OPMODE:
			postUpdate(item,
					DecimalType.valueOf(Integer
							.toString(OperationModeEnum
									.valueOf(value).value)));
			break;
		case AC_FUN_WINDLEVEL:
			postUpdate(item,
					DecimalType.valueOf(Integer
							.toString(WindLevelEnum
									.valueOf(value).value)));
			break;
		case AC_FUN_DIRECTION:
			postUpdate(item,
					DecimalType.valueOf(Integer
							.toString(DirectionEnum
									.valueOf(value).value)));
			break;
		default:
			logger.debug("Not implementation for updating: '"
					+ cmd + "'");
			break;
		}
	}

	private void postUpdate(String item, State state) {
		if (item != null && state != null) {
			logger.debug(item + " gets updated to: " + state);
			eventPublisher.postUpdate(item, state);
		}
	}

	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}

	@Override
	protected String getName() {
		return "Samsung Air Conditioner service";
	}
}
