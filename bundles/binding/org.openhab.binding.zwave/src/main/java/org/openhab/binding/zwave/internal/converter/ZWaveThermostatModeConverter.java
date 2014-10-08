package org.openhab.binding.zwave.internal.converter;

import java.math.BigDecimal;
import java.util.Map;

import org.openhab.binding.zwave.internal.converter.command.DecimalCommandConverter;
import org.openhab.binding.zwave.internal.converter.command.ZWaveCommandConverter;
import org.openhab.binding.zwave.internal.converter.state.BigDecimalDecimalTypeConverter;
import org.openhab.binding.zwave.internal.converter.state.ZWaveStateConverter;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveThermostatModeCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveThermostatModeConverter class. Converter for communication with the 
 * {@link ZWaveThermostatModeCommandClass}. Implements polling of the mode
 * state and receiving of mode state events.
 *  @author Dan Cunningham
 *	@since 1.6.0
 */
public class ZWaveThermostatModeConverter extends
		ZWaveCommandClassConverter<ZWaveThermostatModeCommandClass> {

	private static final Logger logger = LoggerFactory.getLogger(ZWaveThermostatModeConverter.class);
	private static final int REFRESH_INTERVAL = 0; // refresh interval in seconds for the thermostat setpoint;

	/**
	 * Constructor. Creates a new instance of the {@link ZWaveThermostatModeConverter} class.
	 * @param controller the {@link ZWaveController} to use for sending messages.
	 * @param eventPublisher the {@link EventPublisher} to use to publish events.
	 */
	public ZWaveThermostatModeConverter(ZWaveController controller,
			EventPublisher eventPublisher) {
		super(controller, eventPublisher);
		this.addCommandConverter(new DecimalCommandConverter());
		this.addStateConverter(new BigDecimalDecimalTypeConverter());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void executeRefresh(ZWaveNode node,
			ZWaveThermostatModeCommandClass commandClass, int endpointId,
			Map<String, String> arguments) {
		logger.debug("Generating poll message for {} for node {} endpoint {}", commandClass.getCommandClass().getLabel(), node.getNodeId(), endpointId);
		SerialMessage serialMessage = node.encapsulate(commandClass.getValueMessage(), commandClass, endpointId);
		
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
	void handleEvent(ZWaveCommandClassValueEvent event, Item item,
			Map<String, String> arguments) {
		ZWaveStateConverter<?,?> converter = this.getStateConverter(item, event.getValue());
		
		if (converter == null) {
			logger.warn("No converter found for item = {}, node = {} endpoint = {}, ignoring event.", item.getName(), event.getNodeId(), event.getEndpoint());
			return;
		}
		
		State state = converter.convertFromValueToState(event.getValue());
		this.getEventPublisher().postUpdate(item.getName(), state);
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void receiveCommand(Item item, Command command, ZWaveNode node,
			ZWaveThermostatModeCommandClass commandClass, int endpointId,
			Map<String, String> arguments) {
		ZWaveCommandConverter<?,?> converter = this.getCommandConverter(command.getClass());
		
		if (converter == null) {
			logger.warn("No converter found for item = {}, node = {} endpoint = {}, ignoring command.", item.getName(), node.getNodeId(), endpointId);
			return;
		}
		
		logger.debug("receiveCommand with converter {} ", converter.getClass());
		
		SerialMessage serialMessage = node.encapsulate(commandClass.setValueMessage(((BigDecimal)converter.convertFromCommandToValue(item, command)).intValue()), commandClass, endpointId);
		logger.debug("receiveCommand sending message {} ", serialMessage); 
		if (serialMessage == null) {
			logger.warn("Generating message failed for command class = {}, node = {}, endpoint = {}", commandClass.getCommandClass().getLabel(), node.getNodeId(), endpointId);
			return;
		}
		
		this.getController().sendData(serialMessage);
		
		if (command instanceof State)
			this.getEventPublisher().postUpdate(item.getName(), (State)command);
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	int getRefreshInterval() {
		return REFRESH_INTERVAL;
	}

}
