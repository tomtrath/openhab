/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.commandclass;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Z-Wave Command Class. Z-Wave device functions are controlled by command classes. A command class
 * can be have one or multiple commands allowing the use of a certain function of the device.
 * 
 * @author Brian Crosby
 * @since 1.3.0
 */
public abstract class ZWaveCommandClass {

	private static final Logger logger = LoggerFactory.getLogger(ZWaveCommandClass.class);

	private final ZWaveNode node;
	private final ZWaveController controller;
	private final ZWaveEndpoint endpoint;
	
	private int version = 0;
	private int instances = 0;
	
	/**
	 * Protected constructor. Initiates a new instance of a Command Class.
	 * @param node the node this instance commands.
	 * @param controller the controller to send messages to.
	 * @param endpoint the endpoint this Command class belongs to.
	 */
	protected ZWaveCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint)	{
		this.node = node;
		this.controller = controller;
		this.endpoint = endpoint;
		logger.trace("Command class {} created", getCommandClass().getLabel());
	}
	
	/**
	 * Returns the node this command class belongs to.
	 * @return node
	 */
	protected ZWaveNode getNode() {
		return node;
	}
	
	/**
	 * Returns the controller to send messages to.
	 * @return controller
	 */
	protected ZWaveController getController() {
		return controller;
	}
	
	/**
	 * Returns the endpoint this command class belongs to.
	 * @return the endpoint this command class belongs to.
	 */
	public ZWaveEndpoint getEndpoint() {
		return endpoint;
	}
	
	/**
	 * Returns the version of the command class.
	 * @return node
	 */
	public int getVersion() {
		return version;
	}
	
	/**
	 * Sets the version number for this command class.
	 * @param version. The version number to set.
	 */
	public void setVersion(int version) {
		this.version = version;
	}
	
	/**
	 * The maximum version implemented by this command class.
	 */
	public int getMaxVersion () {
		return 1;
	}
	
	/**
	 * Returns the number of instances of this command class
	 * in case the node supports the MULTI_INSTANCE command class (Version 1).
	 * @return the number of instances 
	 */
	public int getInstances() {
		return instances;
	}
	
	/**
	 * Returns the number of instances of this command class
	 * in case the node supports the MULTI_INSTANCE command class (Version 1).
	 * @param instances. The number of instances.
	 */
	public void setInstances(int instances) {
		this.instances = instances;
	}
	
	/**
	 * Returns the command class.
	 * @return command class
	 */
	public abstract CommandClass getCommandClass();
	
	/**
	 * Handles an incoming application command request.
	 * @param serialMessage the incoming message to process.
	 * @param offset the offset position from which to start message processing.
	 * @param endpoint the endpoint or instance number this message is meant for.
	 */
	public abstract void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint);

	/**
	 * Gets an instance of the right command class.
	 * Returns null if the command class is not found.
	 * @param i the code to instantiate
	 * @param node the node this instance commands.
	 * @param controller the controller to send messages to.
	 * @return the ZWaveCommandClass instance that was instantiated, null otherwise 
	 */
	public static ZWaveCommandClass getInstance(int i, ZWaveNode node, ZWaveController controller) {
		return ZWaveCommandClass.getInstance(i, node, controller, null);
	}
	
	/**
	 * Gets an instance of the right command class.
	 * Returns null if the command class is not found.
	 * @param i the code to instantiate
	 * @param node the node this instance commands.
	 * @param controller the controller to send messages to.
	 * @param endpoint the endpoint this Command class belongs to
	 * @return the ZWaveCommandClass instance that was instantiated, null otherwise 
	 */
	public static ZWaveCommandClass getInstance(int i, ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
		logger.debug(String.format("Creating new instance of command class 0x%02X", i));
		try {
			CommandClass commandClass = CommandClass.getCommandClass(i);
			if (commandClass == null) {
				logger.warn(String.format("Unsupported command class 0x%02x", i));
				return null;
			}
			Class<? extends ZWaveCommandClass> commandClassClass = commandClass.getCommandClassClass();
			
			if (commandClassClass == null) {
				logger.warn(String.format("Unsupported command class %s (0x%02x)", commandClass.getLabel(), i, endpoint));
				return null;
			}
				
			Constructor<? extends ZWaveCommandClass> constructor = commandClassClass.getConstructor(ZWaveNode.class, ZWaveController.class, ZWaveEndpoint.class);
			return constructor.newInstance(new Object[] {node, controller, endpoint});
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (IllegalArgumentException e) {
		} catch (InvocationTargetException e) {
		} catch (NoSuchMethodException e) {
		} catch (SecurityException e) {
		}
		logger.error(String.format("Error instantiating command class 0x%02x", i));
		return null;
	}
	
	
	/**
	 * Command class enumeration. Lists all command classes available.
	 * Unsupported command classes by the binding return null for the command class Class.
	 * Taken from: http://wiki.micasaverde.com/index.php/ZWave_Command_Classes
	 * @author Jan-Willem Spuij
	 * @since 1.3.0
	 */
	public enum CommandClass {
		NO_OPERATION(0x00,"NO_OPERATION", ZWaveNoOperationCommandClass.class),
		BASIC(0x20,"BASIC",ZWaveBasicCommandClass.class),
		CONTROLLER_REPLICATION(0x21,"CONTROLLER_REPLICATION",null),
		APPLICATION_STATUS(0x22,"APPLICATION_STATUS",null),
		ZIP_SERVICES(0x23,"ZIP_SERVICES",null),
		ZIP_SERVER(0x24,"ZIP_SERVER",null),
		SWITCH_BINARY(0x25,"SWITCH_BINARY",ZWaveBinarySwitchCommandClass.class),
		SWITCH_MULTILEVEL(0x26,"SWITCH_MULTILEVEL",ZWaveMultiLevelSwitchCommandClass.class),
		SWITCH_ALL(0x27,"SWITCH_ALL",null),
		SWITCH_TOGGLE_BINARY(0x28,"SWITCH_TOGGLE_BINARY",null),
		SWITCH_TOGGLE_MULTILEVEL(0x29,"SWITCH_TOGGLE_MULTILEVEL",null),
		CHIMNEY_FAN(0x2A,"CHIMNEY_FAN",null),
		SCENE_ACTIVATION(0x2B,"SCENE_ACTIVATION",null),
		SCENE_ACTUATOR_CONF(0x2C,"SCENE_ACTUATOR_CONF",null),
		SCENE_CONTROLLER_CONF(0x2D,"SCENE_CONTROLLER_CONF",null),
		ZIP_CLIENT(0x2E,"ZIP_CLIENT",null),
		ZIP_ADV_SERVICES(0x2F,"ZIP_ADV_SERVICES",null),
		SENSOR_BINARY(0x30,"SENSOR_BINARY",ZWaveBinarySensorCommandClass.class),
		SENSOR_MULTILEVEL(0x31,"SENSOR_MULTILEVEL",ZWaveMultiLevelSensorCommandClass.class),
		METER(0x32,"METER",null),
		ZIP_ADV_SERVER(0x33,"ZIP_ADV_SERVER",null),
		ZIP_ADV_CLIENT(0x34,"ZIP_ADV_CLIENT",null),
		METER_PULSE(0x35,"METER_PULSE",null),
		METER_TBL_CONFIG(0x3C,"METER_TBL_CONFIG",null),
		METER_TBL_MONITOR(0x3D,"METER_TBL_MONITOR",null),
		METER_TBL_PUSH(0x3E,"METER_TBL_PUSH",null),
		THERMOSTAT_HEATING(0x38,"THERMOSTAT_HEATING",null),
		THERMOSTAT_MODE(0x40,"THERMOSTAT_MODE",null),
		THERMOSTAT_OPERATING_STATE(0x42,"THERMOSTAT_OPERATING_STATE",null),
		THERMOSTAT_SETPOINT(0x43,"THERMOSTAT_SETPOINT",null),
		THERMOSTAT_FAN_MODE(0x44,"THERMOSTAT_FAN_MODE",null),
		THERMOSTAT_FAN_STATE(0x45,"THERMOSTAT_FAN_STATE",null),
		CLIMATE_CONTROL_SCHEDULE(0x46,"CLIMATE_CONTROL_SCHEDULE",null),
		THERMOSTAT_SETBACK(0x47,"THERMOSTAT_SETBACK",null),
		DOOR_LOCK_LOGGING(0x4C,"DOOR_LOCK_LOGGING",null),
		SCHEDULE_ENTRY_LOCK(0x4E,"SCHEDULE_ENTRY_LOCK",null),
		BASIC_WINDOW_COVERING(0x50,"BASIC_WINDOW_COVERING",null),
		MTP_WINDOW_COVERING(0x51,"MTP_WINDOW_COVERING",null),
		MULTI_INSTANCE(0x60,"MULTI_INSTANCE",ZWaveMultiInstanceCommandClass.class),
		DOOR_LOCK(0x62,"DOOR_LOCK",null),
		USER_CODE(0x63,"USER_CODE",null),
		CONFIGURATION(0x70,"CONFIGURATION",null),
		ALARM(0x71,"ALARM",null),
		MANUFACTURER_SPECIFIC(0x72,"MANUFACTURER_SPECIFIC",ZWaveManufacturerSpecificCommandClass.class),
		POWERLEVEL(0x73,"POWERLEVEL",null),
		PROTECTION(0x75,"PROTECTION",null),
		LOCK(0x76,"LOCK",null),
		NODE_NAMING(0x77,"NODE_NAMING",null),
		FIRMWARE_UPDATE_MD(0x7A,"FIRMWARE_UPDATE_MD",null),
		GROUPING_NAME(0x7B,"GROUPING_NAME",null),
		REMOTE_ASSOCIATION_ACTIVATE(0x7C,"REMOTE_ASSOCIATION_ACTIVATE",null),
		REMOTE_ASSOCIATION(0x7D,"REMOTE_ASSOCIATION",null),
		BATTERY(0x80,"BATTERY",ZWaveBatteryCommandClass.class),
		CLOCK(0x81,"CLOCK",null),
		HAIL(0x82,"HAIL",null),
		WAKE_UP(0x84,"WAKE_UP", ZWaveWakeUpCommandClass.class),
		ASSOCIATION(0x85,"ASSOCIATION",null),
		VERSION(0x86,"VERSION",ZWaveVersionCommandClass.class),
		INDICATOR(0x87,"INDICATOR",null),
		PROPRIETARY(0x88,"PROPRIETARY",null),
		LANGUAGE(0x89,"LANGUAGE",null),
		TIME(0x8A,"TIME",null),
		TIME_PARAMETERS(0x8B,"TIME_PARAMETERS",null),
		GEOGRAPHIC_LOCATION(0x8C,"GEOGRAPHIC_LOCATION",null),
		COMPOSITE(0x8D,"COMPOSITE",null),
		MULTI_INSTANCE_ASSOCIATION(0x8E,"MULTI_INSTANCE_ASSOCIATION",null),
		MULTI_CMD(0x8F,"MULTI_CMD",null),
		ENERGY_PRODUCTION(0x90,"ENERGY_PRODUCTION",null),
		MANUFACTURER_PROPRIETARY(0x91,"MANUFACTURER_PROPRIETARY",null),
		SCREEN_MD(0x92,"SCREEN_MD",null),
		SCREEN_ATTRIBUTES(0x93,"SCREEN_ATTRIBUTES",null),
		SIMPLE_AV_CONTROL(0x94,"SIMPLE_AV_CONTROL",null),
		AV_CONTENT_DIRECTORY_MD(0x95,"AV_CONTENT_DIRECTORY_MD",null),
		AV_RENDERER_STATUS(0x96,"AV_RENDERER_STATUS",null),
		AV_CONTENT_SEARCH_MD(0x97,"AV_CONTENT_SEARCH_MD",null),
		SECURITY(0x98,"SECURITY",null),
		AV_TAGGING_MD(0x99,"AV_TAGGING_MD",null),
		IP_CONFIGURATION(0x9A,"IP_CONFIGURATION",null),
		ASSOCIATION_COMMAND_CONFIGURATION(0x9B,"ASSOCIATION_COMMAND_CONFIGURATION",null),
		SENSOR_ALARM(0x9C,"SENSOR_ALARM",ZWaveAlarmSensorCommandClass.class),
		SILENCE_ALARM(0x9D,"SILENCE_ALARM",null),
		SENSOR_CONFIGURATION(0x9E,"SENSOR_CONFIGURATION",null),
		MARK(0xEF,"MARK",null),
		NON_INTEROPERABLE(0xF0,"NON_INTEROPERABLE",null);

		/**
		 * A mapping between the integer code and its corresponding
		 * Command class to facilitate lookup by code.
		 */
		private static Map<Integer, CommandClass> codeToCommandClassMapping;

		private int key;
		private String label;
		private Class<? extends ZWaveCommandClass> commandClassClass;


		private CommandClass(int key, String label, Class<? extends ZWaveCommandClass> commandClassClass) {
			this.key = key;
			this.label = label;
			this.commandClassClass = commandClassClass;
		}

		private static void initMapping() {
			codeToCommandClassMapping = new HashMap<Integer, CommandClass>();
			for (CommandClass s : values()) {
				codeToCommandClassMapping.put(s.key, s);
			}
		}

		/**
		 * Lookup function based on the command class code.
		 * Returns null if there is no command class with code i
		 * @param i the code to lookup
		 * @return enumeration value of the command class.
		 */
		public static CommandClass getCommandClass(int i) {
			if (codeToCommandClassMapping == null) {
				initMapping();
			}
			
			return codeToCommandClassMapping.get(i);
		}

		/**
		 * @return the key
		 */
		public int getKey() {
			return key;
		}

		/**
		 * @return the label
		 */
		public String getLabel() {
			return label;
		}
		
		/**
		 * @return the command class Class
		 */
		public Class<? extends ZWaveCommandClass> getCommandClassClass() {
			return commandClassClass;
		}
	}
	
}