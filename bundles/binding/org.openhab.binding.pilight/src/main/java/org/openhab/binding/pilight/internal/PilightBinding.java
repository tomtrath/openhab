/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pilight.internal;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.openhab.binding.pilight.PilightBindingProvider;
import org.openhab.binding.pilight.internal.communication.Code;
import org.openhab.binding.pilight.internal.communication.Config;
import org.openhab.binding.pilight.internal.communication.Device;
import org.openhab.binding.pilight.internal.communication.DeviceType;
import org.openhab.binding.pilight.internal.communication.Location;
import org.openhab.binding.pilight.internal.communication.Status;
import org.openhab.binding.pilight.internal.communication.Update;
import org.openhab.binding.pilight.internal.communication.Values;
import org.openhab.core.binding.AbstractBinding;
import org.openhab.core.binding.BindingProvider;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
	
/**
 * Binding that communicates with one or multiple pilight instances.
 * 
 * {@link http://www.pilight.org/}
 * 
 * @author Jeroen Idserda
 * @since 1.0
 */
public class PilightBinding extends AbstractBinding<PilightBindingProvider> implements ManagedService {

	private static final Logger logger = 
		LoggerFactory.getLogger(PilightBinding.class);
	
	private static final int MAX_DIM_LEVEL = 15;	
	
	private Map<String, PilightConnection> connections = new HashMap<String, PilightConnection>();
	
	private ObjectMapper inputMapper;
	
	private ObjectMapper outputMapper;
	
	private ExecutorService delayedUpdateThreadPool = Executors.newSingleThreadExecutor();
	
	/**
	 * Processes a status update received from pilight and changes the state of the
	 * corresponding openHAB item (if item config is found)
	 * 
	 * @param connection pilight connection 
	 * @param status The new Status
	 */
	private void processStatus(PilightConnection connection, Status status) {
		String type = status.getType();
		
		if (!type.equals(DeviceType.SERVER)) {
			Entry<String, List<String>> objectInfo = status.getDevices().entrySet().iterator().next();
			String instance = connection.getInstance();
			String location = objectInfo.getKey();
			String device = objectInfo.getValue().get(0);
			
			List<PilightBindingConfig> configs = getConfigs(instance, location, device);
			
			if (!configs.isEmpty()) {
				if (type.equals(DeviceType.SWITCH) || type.equals(DeviceType.DIMMER)) {
					processSwitchEvent(configs, status);
				} else if (type.equals(DeviceType.VALUE)) {
					processValueEvent(configs, status);
				}
			}
		}
	}

	private void processValueEvent(List<PilightBindingConfig> configs, Status status) {
		for (PilightBindingConfig config : configs) {
			String property = config.getProperty();
			if (status.getValues().containsKey(property)) {
				String value = status.getValues().get(property);
				State state = getState(value, config);
				
				if (state != null) {
					eventPublisher.postUpdate(config.getItemName(), state);
				}
			}
		}
	}

	protected State getState(String value, PilightBindingConfig config) {
		State state = null;
		
		if (config.getItemType().equals(StringItem.class)) {
			state = new StringType(value);
		} else if (config.getItemType().equals(NumberItem.class)) {
			if (!StringUtils.isBlank(value)) {
				// Number values are always received as an integer with an optional parameter describing 
				// the number of decimals (scale, default = 0). 
				BigDecimal numberValue = new BigDecimal(value);
				numberValue = numberValue.divide(new BigDecimal(Math.pow(10,config.getScale())), config.getScale(), RoundingMode.HALF_UP);
				state = new DecimalType(numberValue);
			}
		}
		
		return state;
	}

	private void processSwitchEvent(List<PilightBindingConfig> configs, Status status) {
		String type = status.getType();
		State state = OnOffType.valueOf(status.getValues().get("state").toUpperCase());

		if (type.equals(DeviceType.SWITCH)) {
			// noop, just use on/off state defined above
		} else if (type.equals(DeviceType.DIMMER)) {
			BigDecimal dimLevel = BigDecimal.ZERO;
			
			if (status.getValues().get("dimlevel") != null) 
				dimLevel = getPercentageFromDimLevel(status.getValues().get("dimlevel"));
			else {
				// Dimmer items can can also be switched on or off in pilight. 
				// When this happens, the dimmer value is not reported. At least we know it's on or off.
				dimLevel = state.equals(OnOffType.ON) ? new BigDecimal("100") : BigDecimal.ZERO;
			}
			
			state = new PercentType(dimLevel);
		}
		
		for (PilightBindingConfig config : configs) {
			eventPublisher.postUpdate(config.getItemName(), state);
		}
	}
	
	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void internalReceiveCommand(String itemName, Command command) {
		PilightBindingProvider provider = findFirstMatchingBindingProvider(itemName);
		PilightBindingConfig config = provider.getBindingConfig(itemName);
		PilightConnection connection = connections.get(config.getInstance());
		
		Update update = createUpdateCommand(command, config);
		
		if (update != null)
			sendUpdate(update, connection);
	}
	
	private void sendUpdate(Update update, PilightConnection connection) 
	{
		if (connection.getDelay() != null) {
			delayedUpdateCall(update, connection);
		} else {
			doUpdateCall(update, connection);
		}
	}

	private void delayedUpdateCall(Update update, PilightConnection connection)
	{
		DelayedUpdate delayed = new DelayedUpdate(update, connection);
		delayedUpdateThreadPool.execute(delayed);
	}
	
	private void doUpdateCall(Update update, PilightConnection connection) {
		try {
			connection.setLastUpdate(new Date());
			getOutputMapper().writeValue(connection.getSocket().getOutputStream(), update);
		} catch (IOException e) {
			logger.error("Error while sending update to pilight server", e);
		}	
	}

	private Update createUpdateCommand(Command command,	PilightBindingConfig config) {
		Update update = new Update();
		Code code = new Code();

		code.setDevice(config.getDevice());
		code.setLocation(config.getLocation());
		
		if (command instanceof OnOffType) {
			setOnOffValue((OnOffType)command, code);
		} else if (command instanceof PercentType){
			setDimmerValue((PercentType) command, code);
		} else {
			logger.error("Only OnOffType and PercentType are supported by the pilight binding");
			return null;
		}
			
		update.setCode(code);
		update.setMessage(Update.SEND);
		return update;
	}

	private void setOnOffValue(OnOffType onOff, Code code) {
		code.setState(onOff.equals(OnOffType.ON) ? Code.STATE_ON : Code.STATE_OFF);
	}

	private void setDimmerValue(PercentType percent, Code code) {
		if (BigDecimal.ZERO.equals(percent.toBigDecimal())) {
			// pilight is not responding to commands that set both the dimlevel to 0 and state to off. 
			// So, we're only updating the state for now 
			code.setState(Code.STATE_OFF); 
		} else {
			BigDecimal dimlevel = new BigDecimal(percent.toBigDecimal().toString()).setScale(2)
					.divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP)
					.multiply(BigDecimal.valueOf(MAX_DIM_LEVEL)).setScale(0, RoundingMode.HALF_UP);
			
			Values values = new Values();
			values.setDimlevel(dimlevel.intValue());
			code.setValues(values);
			code.setState(dimlevel.compareTo(BigDecimal.ZERO) == 1 ? Code.STATE_ON : Code.STATE_OFF);
		}
	}
	
	private PilightBindingProvider findFirstMatchingBindingProvider(String itemName) {
		for (PilightBindingProvider provider : providers) {
			 if (provider.getItemNames().contains(itemName))
				 return provider;
		}
			 
		return null;
	}
	
	private List<PilightBindingConfig> getConfigs(String instance, String location, String device) {
		for (PilightBindingProvider provider : providers) {
			List<PilightBindingConfig> configs = provider.getBindingConfigs(instance, location, device);
			 if (!configs.isEmpty())
				 return configs;
		}
			 
		return new ArrayList<PilightBindingConfig>();
	}
		
	/**
	 * @{inheritDoc}
	 */
	@Override
	public void updated(Dictionary<String, ?> config) throws ConfigurationException {
		if (config != null) {			
			this.connections = parseConfig(config);
			
			if (this.connections.size() > 0) {
				for (Entry<String, PilightConnection> entry : this.connections.entrySet()) {
					PilightConnection connection = entry.getValue();
					
					if (connection.connect(getInputMapper(), getOutputMapper())) {
						logger.info("Established connection to pilight server at {}:{}", connection.getHostname(), connection.getPort());
						startListener(connection);
					} else { 
						String msg = String.format("Cannot connect to pilight server at %s:%d", connection.getHostname(), connection.getPort());
						logger.error(msg);
						throw new ConfigurationException("pilight." + connection.getInstance(), msg);
					}
				}
			}
		}
	}

	private Map<String, PilightConnection> parseConfig(Dictionary<String, ?> config) {
		Map<String, PilightConnection> connections = new HashMap<String, PilightConnection>();
		Enumeration<String> keys = config.keys();

		while (keys.hasMoreElements()) {
			String key = keys.nextElement();

			if ("service.pid".equals(key)) 
				continue;

			String[] parts = key.split("\\.");
			String instance = parts[0];

			PilightConnection connection = connections.get(instance);
			if (connection == null) {
				connection = new PilightConnection();
				connection.setInstance(instance);
			}

			String value = ((String) config.get(key)).trim();

			if ("host".equals(parts[1])) {
				connection.setHostname(value);
			}
			if ("port".equals(parts[1])) {
				connection.setPort(Integer.valueOf(value));
			}
			if ("delay".equals(parts[1])) {
				connection.setDelay(Long.valueOf(value));
			}

			connections.put(instance, connection);
		}
		return connections;
	}

	private void startListener(PilightConnection connection) {
		connection.setListener(new PilightListener(connection, new IPilightMessageReceivedCallback() {
			@Override
			public void messageReceived(PilightConnection connection,
					Status status) {
				processStatus(connection, status);
			}
		}));
		connection.getListener().start();
		setInitialState();
	}

	/**
	 * Gets the state for item in {@code bindingConfig} from {@code pilightConfig}  
	 * 
	 * @param pilightConfig The complete pilight configuration
	 * @param bindingConfig Specific pilight item in openHAB
	 * @return Current state of the item 
	 */
	private State getStateFromConfig(Config pilightConfig, PilightBindingConfig bindingConfig) {
		Location loc = pilightConfig.getConfig().get(bindingConfig.getLocation());
		if (loc != null) {
			Device dev = loc.getDevices().get(bindingConfig.getDevice());
			
			if (dev != null) {
				String devType = dev.getType().toString();
				if (devType.equals(DeviceType.SWITCH) || devType.equals(DeviceType.DIMMER)) {
					OnOffType state = OnOffType.valueOf(dev.getState().toUpperCase());
					
					if (dev.getDimlevel() != null && dev.getDimlevel() > 0) {
						if (state.equals(OnOffType.ON))
							return new PercentType(getPercentageFromDimLevel(dev.getDimlevel().toString()));
						else
							return PercentType.ZERO;
					}
					
					return state;
				} else if (devType.equals(DeviceType.VALUE)) {
					if (dev.getScale() != null) {
						bindingConfig.setScale(dev.getScale());
					}
					
					String property = bindingConfig.getProperty();
					if (dev.getProperties().containsKey(property)) {
						String value = dev.getProperties().get(property);
						return getState(value, bindingConfig);
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * @{inheritDoc}
	 */
	@Override
	public void bindingChanged(BindingProvider provider, String itemName) {
		logger.debug("Binding changed for item {}", itemName);
		checkItemState(provider, itemName);
	}
	
	/**
	 * @{inheritDoc}
	 */
	@Override
	public void allBindingsChanged(BindingProvider provider) {
		logger.debug("All bindings changed");
		for (String itemName : provider.getItemNames()) {
			checkItemState(provider, itemName);
		}
	}
	
	private void setInitialState() {
		for (PilightBindingProvider provider : providers) {
			allBindingsChanged(provider);
		}
	}

	/**
	 * Synchronize itemName with the current state in pilight. 
	 * 
	 * @param provider The PilightBindingProvider 
	 * @param itemName The itemName in openHAB
	 */
	private void checkItemState(BindingProvider provider, final String itemName) {
		PilightBindingProvider pilightProvider = (PilightBindingProvider) provider;
		final PilightBindingConfig config = pilightProvider.getBindingConfig(itemName);
		
		if (config != null) {
			PilightConnection connection = connections.get(config.getInstance());
			if (connection.isConnected()) {
				connection.getListener().refreshConfig(new IPilightConfigReceivedCallback() {
					@Override
					public void configReceived(PilightConnection connection) {
						State state = getStateFromConfig(connection.getConfig(), config);
						if (state != null) {
							eventPublisher.postUpdate(itemName, state);
						}
					}
				});
			}
		}
	}

	private BigDecimal getPercentageFromDimLevel(String string) {
		return new BigDecimal(string).setScale(2)
			.divide(new BigDecimal(MAX_DIM_LEVEL), RoundingMode.HALF_UP)
			.multiply(new BigDecimal(100));
	}

	private ObjectMapper getInputMapper() {
		if (inputMapper == null)
			inputMapper = new ObjectMapper().configure(
					org.codehaus.jackson.JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
		
		return inputMapper;
	}

	private ObjectMapper getOutputMapper() {
		if (outputMapper == null) 
			outputMapper = new ObjectMapper().configure(
					Feature.AUTO_CLOSE_TARGET, false); 
		
		return outputMapper;
	}
	
	/**
	 * Simple thread to allow calls to pilight to be throttled  
	 * 
	 * @author Jeroen Idserda
	 * @since 1.0
	 */
	private class DelayedUpdate implements Runnable {
		
		private Update update;
		
		private PilightConnection connection;
		
		public DelayedUpdate(Update update, PilightConnection connection) {
			this.update = update; 
			this.connection = connection;
		}
		
		@Override
		public void run() {
			long delayBetweenUpdates = connection.getDelay();

			if (connection.getLastUpdate() != null) {
				long diff = new Date().getTime() - connection.getLastUpdate().getTime();
				if (diff < delayBetweenUpdates) {
					long delay = delayBetweenUpdates-diff;
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						logger.error("Error while processing pilight throttling delay");
					}
				}
			}
			
			doUpdateCall(update, connection);
		}
	}
}
