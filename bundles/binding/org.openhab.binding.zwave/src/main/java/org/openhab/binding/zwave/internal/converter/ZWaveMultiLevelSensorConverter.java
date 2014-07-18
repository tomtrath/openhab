/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.converter;

import java.math.BigDecimal;
import java.util.Map;

import org.openhab.binding.zwave.internal.converter.command.ZWaveCommandConverter;
import org.openhab.binding.zwave.internal.converter.state.BigDecimalDecimalTypeConverter;
import org.openhab.binding.zwave.internal.converter.state.BigDecimalOnOffTypeConverter;
import org.openhab.binding.zwave.internal.converter.state.BigDecimalOpenClosedTypeConverter;
import org.openhab.binding.zwave.internal.converter.state.ZWaveStateConverter;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass.SensorType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass.ZWaveMultiLevelSensorValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * ZWaveMultiLevelSensorConverter class. Converter for communication with the 
 * {@link ZWaveMultiLevelSensorCommandClass}. Implements polling of the sensor
 * status and receiving of sensor events.
 * @author Jan-Willem Spuij
 * @since 1.4.0
 */
public class ZWaveMultiLevelSensorConverter extends ZWaveCommandClassConverter<ZWaveMultiLevelSensorCommandClass> {

	private static final Logger logger = LoggerFactory.getLogger(ZWaveMultiLevelSensorConverter.class);
	private static final int REFRESH_INTERVAL = 0; // refresh interval in seconds for the multi level switch;

	/**
	 * Constructor. Creates a new instance of the {@link ZWaveMultiLevelSensorConverter} class.
	 * @param controller the {@link ZWaveController} to use for sending messages.
	 * @param eventPublisher the {@link EventPublisher} to use to publish events.
	 */
	public ZWaveMultiLevelSensorConverter(ZWaveController controller, EventPublisher eventPublisher) {
		super(controller, eventPublisher);
		
		// State and commmand converters used by this converter. 
		this.addStateConverter(new BigDecimalDecimalTypeConverter());
		this.addStateConverter(new BigDecimalOnOffTypeConverter());
		this.addStateConverter(new BigDecimalOpenClosedTypeConverter());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void executeRefresh(ZWaveNode node, 
			ZWaveMultiLevelSensorCommandClass commandClass, int endpointId, Map<String,String> arguments) {
		String sensorType = arguments.get("sensor_type");
		SerialMessage serialMessage;

		logger.debug("Generating poll message for {} for node {} endpoint {}", commandClass.getCommandClass().getLabel(), node.getNodeId(), endpointId);
		
		if (sensorType != null) {
			serialMessage = node.encapsulate(commandClass.getMessage(SensorType.getSensorType(Integer.parseInt(sensorType))), commandClass, endpointId);
		} else {
			serialMessage = node.encapsulate(commandClass.getValueMessage(), commandClass, endpointId);
		}
		
		if (serialMessage == null) {
			logger.warn("Generating message failed for command class = {}, node = {}, endpoint = {}", commandClass.getCommandClass().getLabel(), node.getNodeId(), endpointId);
			return;
		}
		
		this.getController().sendData(serialMessage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEvent(ZWaveCommandClassValueEvent event, Item item, Map<String,String> arguments) {
		ZWaveStateConverter<?,?> converter = this.getStateConverter(item, event.getValue());
		String sensorType = arguments.get("sensor_type");
		String sensorScale = arguments.get("sensor_scale");
		ZWaveMultiLevelSensorValueEvent sensorEvent = (ZWaveMultiLevelSensorValueEvent)event;

		if (converter == null) {
			logger.warn("No converter found for item = {}, node = {} endpoint = {}, ignoring event.", item.getName(), event.getNodeId(), event.getEndpoint());
			return;
		}
		
		// Don't trigger event if this item is bound to another sensor type
		if (sensorType != null && SensorType.getSensorType(Integer.parseInt(sensorType)) != sensorEvent.getSensorType())
			return;

		Object val = event.getValue();
		// Perform a scale conversion if needed
		if (sensorScale != null && Integer.parseInt(sensorScale) != sensorEvent.getSensorScale()) {
			switch(SensorType.getSensorType(Integer.parseInt(sensorType))) {
			case TEMPERATURE:
				// For temperature, there are only two scales, so we simplify the conversion
				if(sensorEvent.getSensorScale() == 0) {
					// Scale is celsius, convert to fahrenheit
					double c = ((BigDecimal)val).doubleValue();
					val = new BigDecimal((c * 9.0 / 5.0) + 32.0 );
				}
				else if(sensorEvent.getSensorScale() == 1) {
					// Scale is fahrenheit, convert to celsius
					double f = ((BigDecimal)val).doubleValue();
					val = new BigDecimal((f - 32.0) * 5.0 / 9.0 );					
				}
				break;
			default:
				break;
			}
		}

		State state = converter.convertFromValueToState(val);
		this.getEventPublisher().postUpdate(item.getName(), state);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receiveCommand(Item item, Command command, ZWaveNode node,
			ZWaveMultiLevelSensorCommandClass commandClass, int endpointId, Map<String,String> arguments) {
		ZWaveCommandConverter<?,?> converter = this.getCommandConverter(command.getClass());
		
		if (converter == null) {
			logger.warn("No converter found for item = {}, node = {} endpoint = {}, ignoring command.", item.getName(), node.getNodeId(), endpointId);
			return;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	int getRefreshInterval() {
		return REFRESH_INTERVAL;
	}
}
