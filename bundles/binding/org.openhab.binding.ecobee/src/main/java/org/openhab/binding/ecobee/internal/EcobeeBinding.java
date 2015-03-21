/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ecobee.internal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.openhab.binding.ecobee.EcobeeBindingProvider;
import org.openhab.binding.ecobee.internal.messages.AbstractFunction;
import org.openhab.binding.ecobee.internal.messages.ApiResponse;
import org.openhab.binding.ecobee.internal.messages.AuthorizeRequest;
import org.openhab.binding.ecobee.internal.messages.AuthorizeResponse;
import org.openhab.binding.ecobee.internal.messages.Request;
import org.openhab.binding.ecobee.internal.messages.Selection;
import org.openhab.binding.ecobee.internal.messages.Selection.SelectionType;
import org.openhab.binding.ecobee.internal.messages.Status;
import org.openhab.binding.ecobee.internal.messages.Temperature;
import org.openhab.binding.ecobee.internal.messages.Thermostat;
import org.openhab.binding.ecobee.internal.messages.Thermostat.HvacMode;
import org.openhab.binding.ecobee.internal.messages.Thermostat.VentilatorMode;
import org.openhab.binding.ecobee.internal.messages.ThermostatRequest;
import org.openhab.binding.ecobee.internal.messages.ThermostatResponse;
import org.openhab.binding.ecobee.internal.messages.ThermostatSummaryRequest;
import org.openhab.binding.ecobee.internal.messages.ThermostatSummaryResponse;
import org.openhab.binding.ecobee.internal.messages.ThermostatSummaryResponse.Revision;
import org.openhab.binding.ecobee.internal.messages.RefreshTokenRequest;
import org.openhab.binding.ecobee.internal.messages.TokenRequest;
import org.openhab.binding.ecobee.internal.messages.TokenResponse;
import org.openhab.binding.ecobee.internal.messages.UpdateThermostatRequest;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.binding.BindingProvider;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Binding that retrieves information about thermostats we're interested in every few minutes, and sends updates and
 * commands to Ecobee as they are made. Reviewed lots of other binding implementations, particularly Netatmo and XBMC.
 * 
 * @author John Cocula
 * @since 1.7.0
 */
public class EcobeeBinding extends AbstractActiveBinding<EcobeeBindingProvider> implements ManagedService {

	private static final String DEFAULT_USER_ID = "DEFAULT_USER";

	private static final Logger logger = LoggerFactory.getLogger(EcobeeBinding.class);

	protected static final String CONFIG_REFRESH = "refresh";
	protected static final String CONFIG_APP_KEY = "appkey";
	protected static final String CONFIG_SCOPE = "scope";
	protected static final String CONFIG_TEMP_SCALE = "tempscale";

	static {
		// Register bean type converters
		ConvertUtils.register(new Converter() {

			@SuppressWarnings("rawtypes")
			@Override
			public Object convert(Class type, Object value) {
				if (value instanceof DecimalType) {
					return Temperature.fromLocalTemperature(((DecimalType) value).toBigDecimal());
				} else {
					return null;
				}
			}
		}, Temperature.class);
		ConvertUtils.register(new Converter() {

			@SuppressWarnings("rawtypes")
			@Override
			public Object convert(Class type, Object value) {
				if (value instanceof StringType) {
					return HvacMode.forValue(value.toString());
				} else {
					return null;
				}
			}
		}, HvacMode.class);
		ConvertUtils.register(new Converter() {

			@SuppressWarnings("rawtypes")
			@Override
			public Object convert(Class type, Object value) {
				if (value instanceof DecimalType) {
					return ((DecimalType) value).intValue();
				} else {
					return null;
				}
			}
		}, Integer.class);
		ConvertUtils.register(new Converter() {

			@SuppressWarnings("rawtypes")
			@Override
			public Object convert(Class type, Object value) {
				if (value instanceof StringType) {
					return VentilatorMode.forValue(value.toString());
				} else {
					return null;
				}
			}
		}, VentilatorMode.class);
		ConvertUtils.register(new Converter() {

			@SuppressWarnings("rawtypes")
			@Override
			public Object convert(Class type, Object value) {
				if (value instanceof OnOffType) {
					return ((OnOffType) value) == OnOffType.ON;
				} else {
					return null;
				}
			}
		}, Boolean.class);
		ConvertUtils.register(new Converter() {

			@SuppressWarnings("rawtypes")
			@Override
			public Object convert(Class type, Object value) {
				return value.toString();
			}
		}, String.class);
	}

	/**
	 * the refresh interval which is used to poll values from the Ecobee server (optional, defaults to 60000ms)
	 */
	private long refreshInterval = 60000;

	/**
	 * A map of userids from the openhab.cfg file to OAuth credentials used to communicate with each app instance.
	 */
	private Map<String, OAuthCredentials> credentialsCache = new HashMap<String, OAuthCredentials>();

	/**
	 * used to store events that we have sent ourselves; we need to remember them for not reacting to them
	 */
	private List<String> ignoreEventList = Collections.synchronizedList(new ArrayList<String>());

	/**
	 * The most recently received list of revisions, or an empty Map if none have been retrieved yet.
	 */
	private Map<String, Revision> lastRevisionMap = new HashMap<String, Revision>();

	// Injected by the OSGi Container through the setItemRegistry and
	// unsetItemRegistry methods.
	private ItemRegistry itemRegistry;

	public EcobeeBinding() {
	}

	/**
	 * Invoked by the OSGi Framework.
	 * 
	 * This method is invoked by OSGi during the initialization of the EcobeeBinding, so we have subsequent access to
	 * the ItemRegistry (needed to get values from Items in openHAB)
	 */
	public void setItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = itemRegistry;
	}

	/**
	 * Invoked by the OSGi Framework.
	 * 
	 * This method is invoked by OSGi during the initialization of the EcobeeBinding, so we have subsequent access to
	 * the ItemRegistry (needed to get values from Items in openHAB)
	 */
	public void unsetItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void activate() {
		super.activate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deactivate() {
		// deallocate resources here that are no longer needed and
		// should be reset when activating this binding again
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getName() {
		return "Ecobee Refresh Service";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute() {
		logger.trace("Querying Ecobee API");

		try {
			for (String userid : credentialsCache.keySet()) {
				OAuthCredentials oauthCredentials = getOAuthCredentials(userid);

				Selection selection = createSelection(oauthCredentials);
				if (selection == null) {
					logger.debug("Nothing to retrieve for '{}'; skipping thermostat retrieval.",
							oauthCredentials.userid);
					continue;
				}

				if (oauthCredentials.noAccessToken()) {
					if (!oauthCredentials.refreshTokens()) {
						logger.warn("Periodic poll skipped for '{}'.", oauthCredentials.userid);
						continue;
					}
				}

				readEcobee(oauthCredentials, selection);
			}
		} catch (Exception e) {
			logger.error("Error reading from Ecobee:", e);
		}
	}

	/**
	 * Given the credentials to use and what to select from the Ecobee API, read any changed information from Ecobee and
	 * update the affected items.
	 * 
	 * @param oauthCredentials
	 *            the credentials to use
	 * @param selection
	 *            the selection of data to retrieve
	 */
	private void readEcobee(OAuthCredentials oauthCredentials, Selection selection) throws Exception {

		logger.debug("Requesting summaries for {}", selection);

		ThermostatSummaryRequest request = new ThermostatSummaryRequest(oauthCredentials.accessToken, selection);
		ThermostatSummaryResponse response = request.execute();
		if (response.isError()) {
			final Status status = response.getStatus();

			if (status.isAccessTokenExpired()) {
				logger.debug("Access token has expired: {}", status);
				if (oauthCredentials.refreshTokens())
					readEcobee(oauthCredentials, selection);
			} else {
				logger.error(status.getMessage());
			}

			return; // abort processing
		}

		logger.debug("Retrieved summaries for {} thermostat(s).", response.getRevisionList().size());

		// Identify which thermostats have changed since the last fetch

		Map<String, Revision> newRevisionMap = new HashMap<String, Revision>();
		for (Revision r : response.getRevisionList()) {
			newRevisionMap.put(r.getThermostatIdentifier(), r);
		}

		// Accumulate the thermostat IDs for thermostats that have updated
		// since the last fetch.
		Set<String> thermostatIdentifiers = new HashSet<String>();

		for (Revision newRevision : newRevisionMap.values()) {
			Revision lastRevision = this.lastRevisionMap.get(newRevision.getThermostatIdentifier());

			// If this thermostat's values have changed,
			// add it to the list for full retrieval

			/*
			 * NOTE: The following tests may be more eager than they should be, because we may have a settings binding
			 * for one thermostat and not another, and a runtime binding for another thermostat but not this one, but we
			 * will now retrieve both thermostats. A small sin. If the Ecobee binding is only working with a single
			 * thermostat, these tests will be perfectly accurate.
			 */

			boolean changed = false;

			changed = changed
					|| (newRevision.hasRuntimeChanged(lastRevision) && (selection.includeRuntime() || selection
							.includeExtendedRuntime()));
			changed = changed
					|| (newRevision.hasThermostatChanged(lastRevision) && (selection.includeSettings() || selection
							.includeProgram()));

			if (changed) {
				thermostatIdentifiers.add(newRevision.getThermostatIdentifier());
			}
		}

		// Remember the new revisions for the next execute() call.
		this.lastRevisionMap = newRevisionMap;

		if (0 == thermostatIdentifiers.size()) {
			logger.debug("No changes detected.");
			return;
		}

		logger.debug("Requesting full retrieval for {} thermostat(s).", thermostatIdentifiers.size());

		// Potentially decrease the number of thermostats for the full
		// retrieval.

		selection.setSelectionMatch(thermostatIdentifiers);

		// TODO loop through possibly multiple pages (@watou)
		ThermostatRequest treq = new ThermostatRequest(oauthCredentials.accessToken, selection, null);
		ThermostatResponse tres = treq.execute();

		if (tres.isError()) {
			logger.error("Error retrieving thermostats: {}", tres.getStatus());
			return;
		}

		// Create a ID-based map of the thermostats we retrieved.
		Map<String, Thermostat> thermostats = new HashMap<String, Thermostat>();

		for (Thermostat t : tres.getThermostatList()) {
			thermostats.put(t.getIdentifier(), t);
		}

		// Iterate through bindings and update all inbound values.
		for (final EcobeeBindingProvider provider : this.providers) {
			for (final String itemName : provider.getItemNames()) {
				if (provider.isInBound(itemName)) {
					final State newState = getState(provider, thermostats, itemName);
					State oldState = (itemRegistry == null) ? null : itemRegistry.getItem(itemName).getState();

					if ((oldState == null && newState != null)
							|| (UnDefType.UNDEF.equals(oldState) && !UnDefType.UNDEF.equals(newState))
							|| !oldState.equals(newState)) {
						logger.debug("readEcobee: Updating itemName '{}' with newState '{}', oldState '{}'", itemName,
								newState, oldState);

						/*
						 * we need to make sure that we won't send out this event to Ecobee again, when receiving it on
						 * the openHAB bus
						 */
						ignoreEventList.add(itemName + newState.toString());
						logger.trace("Added event (item='{}', newState='{}') to the ignore event list", itemName,
								newState);
						this.eventPublisher.postUpdate(itemName, newState);
					} else {
						logger.trace("readEcobee: Ignoring item='{}' with newState='{}', oldState='{}'", itemName,
								newState, oldState);
					}
				}
			}
		}
	}

	/**
	 * Give a binding provider, a map of thermostats, and an item name, return the corresponding state object.
	 * 
	 * @param provider
	 *            the Ecobee binding provider
	 * @param thermostats
	 *            a map of thermostat identifiers to {@link Thermostat} objects
	 * @param itemName
	 *            the item name from the items file.
	 * @return the State object for the named item
	 */
	private State getState(EcobeeBindingProvider provider, Map<String, Thermostat> thermostats, String itemName) {

		final String thermostatIdentifier = provider.getThermostatIdentifier(itemName);
		final String property = provider.getProperty(itemName);
		final Thermostat thermostat = thermostats.get(thermostatIdentifier);

		if (thermostat == null) {
			logger.error("Did not receive thermostat '{}' for item '{}'; skipping.", thermostatIdentifier, itemName);
		} else {
			try {
				return createState(thermostat.getProperty(property));
			} catch (Exception e) {
				logger.error("Unable to get state from thermostat", e);
			}
		}
		return UnDefType.NULL;
	}

	/**
	 * Creates an openHAB {@link State} in accordance to the class of the given {@code propertyValue}. Currently
	 * {@link Date}, {@link BigDecimal}, {@link Temperature} and {@link Boolean} are handled explicitly. All other
	 * {@code dataTypes} are mapped to {@link StringType}.
	 * <p>
	 * If {@code propertyValue} is {@code null}, {@link UnDefType#NULL} will be returned.
	 * 
	 * Copied/adapted from the Koubachi binding.
	 * 
	 * @param propertyValue
	 * 
	 * @return the new {@link State} in accordance with {@code dataType}. Will never be {@code null}.
	 */
	private State createState(Object propertyValue) {
		if (propertyValue == null) {
			return UnDefType.NULL;
		}

		Class<?> dataType = propertyValue.getClass();

		if (Date.class.isAssignableFrom(dataType)) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime((Date) propertyValue);
			return new DateTimeType(calendar);
		} else if (Integer.class.isAssignableFrom(dataType)) {
			return new DecimalType((Integer) propertyValue);
		} else if (BigDecimal.class.isAssignableFrom(dataType)) {
			return new DecimalType((BigDecimal) propertyValue);
		} else if (Boolean.class.isAssignableFrom(dataType)) {
			if ((Boolean) propertyValue) {
				return OnOffType.ON;
			} else {
				return OnOffType.OFF;
			}
		} else if (Temperature.class.isAssignableFrom(dataType)) {
			return new DecimalType(((Temperature) propertyValue).toLocalTemperature());
		} else {
			return new StringType(propertyValue.toString());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalReceiveCommand(String itemName, Command command) {
		logger.trace("internalReceiveCommand(item='{}', command='{}')", itemName, command);
		commandEcobee(itemName, command);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalReceiveUpdate(final String itemName, final State newState) {
		logger.trace("Received update (item='{}', state='{}')", itemName, newState.toString());
		if (!isEcho(itemName, newState)) {
			updateEcobee(itemName, newState);
		}
	}

	/**
	 * Perform the given {@code command} against all targets referenced in {@code itemName}.
	 * 
	 * @param command
	 *            the command to execute
	 * @param the
	 *            target(s) against which to execute this command
	 */
	private void commandEcobee(final String itemName, final Command command) {
		if (command instanceof State) {
			updateEcobee(itemName, (State) command);
		}
	}

	private boolean isEcho(String itemName, State state) {
		String ignoreEventListKey = itemName + state.toString();
		if (ignoreEventList.remove(ignoreEventListKey)) {
			logger.trace(
					"We received this event (item='{}', state='{}') from Ecobee, so we don't send it back again -> ignore!",
					itemName, state.toString());
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Send the {@code newState} for the given {@code itemName} to Ecobee.
	 * 
	 * @param itemName
	 * @param newState
	 */
	private void updateEcobee(final String itemName, final State newState) {

		// Find the first binding provider for this itemName.
		EcobeeBindingProvider provider = null;
		String selectionMatch = null;
		for (EcobeeBindingProvider p : this.providers) {
			selectionMatch = p.getThermostatIdentifier(itemName);
			if (selectionMatch != null) {
				provider = p;
				break;
			}
		}

		if (provider == null) {
			logger.warn("no matching binding provider found [itemName={}, newState={}]", itemName, newState);
			return;
		} else {
			final Selection selection = new Selection(selectionMatch);
			List<AbstractFunction> functions = null;
			logger.debug("Selection for update: {}", selection);

			String property = provider.getProperty(itemName);

			try {
				final Thermostat thermostat = new Thermostat(null);

				logger.debug("About to set property '{}' to '{}'", property, newState);

				thermostat.setProperty(property, newState);

				logger.debug("Thermostat for update: {}", thermostat);

				OAuthCredentials oauthCredentials = getOAuthCredentials(provider.getUserid(itemName));

				if (oauthCredentials == null) {
					logger.warn("Unable to locate credentials for item {}; aborting update.", itemName);
					return;
				}

				if (oauthCredentials.noAccessToken()) {
					if (!oauthCredentials.refreshTokens()) {
						logger.warn("Sending update skipped.");
						return;
					}
				}

				UpdateThermostatRequest request = new UpdateThermostatRequest(oauthCredentials.accessToken, selection,
						functions, thermostat);
				ApiResponse response = request.execute();
				if (response.isError()) {
					final Status status = response.getStatus();
					if (status.isAccessTokenExpired()) {
						if (oauthCredentials.refreshTokens()) {
							updateEcobee(itemName, newState);
						}
					} else {
						logger.error("Error updating thermostat(s): {}", response);
					}
				}
			} catch (Exception e) {
				logger.error("Unable to update thermostat(s)", e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void bindingChanged(BindingProvider provider, String itemName) {

		// Forget prior revisions because we may be concerned with
		// different thermostats or properties than before.
		if (provider instanceof EcobeeBindingProvider) {
			this.lastRevisionMap.clear();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void allBindingsChanged(BindingProvider provider) {

		// Forget prior revisions because we may be concerned with
		// different thermostats or properties than before.
		if (provider instanceof EcobeeBindingProvider) {
			this.lastRevisionMap.clear();
		}
	}

	/**
	 * Returns the cached {@link OAuthCredentials} for the given {@code userid}. If their is no such cached
	 * {@link OAuthCredentials} element, the cache is searched with the {@code DEFAULT_USER}. If there is still no
	 * cached element found {@code NULL} is returned.
	 * 
	 * @param userid
	 *            the userid to find the {@link OAuthCredentials}
	 * @return the cached {@link OAuthCredentials} or {@code NULL}
	 */
	private OAuthCredentials getOAuthCredentials(String userid) {
		if (credentialsCache.containsKey(userid)) {
			return credentialsCache.get(userid);
		} else {
			return credentialsCache.get(DEFAULT_USER_ID);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updated(Dictionary<String, ?> config) throws ConfigurationException {
		if (config != null) {

			// to override the default refresh interval one has to add a
			// parameter to openhab.cfg like ecobee:refresh=120000
			String refreshIntervalString = (String) config.get(CONFIG_REFRESH);
			if (isNotBlank(refreshIntervalString)) {
				refreshInterval = Long.parseLong(refreshIntervalString);
			}
			// to override the default usage of Fahrenheit one has to add a
			// parameter to openhab.cfg, as in ecobee:tempscale=C
			String tempScaleString = (String) config.get(CONFIG_TEMP_SCALE);
			if (isNotBlank(tempScaleString)) {
				try {
					Temperature.setLocalScale(Temperature.Scale.forValue(tempScaleString));
				} catch (IllegalArgumentException iae) {
					throw new ConfigurationException(CONFIG_TEMP_SCALE, "Unsupported temperature scale '"
							+ tempScaleString + "'.");
				}
			}

			Enumeration<String> configKeys = config.keys();
			while (configKeys.hasMoreElements()) {
				String configKey = (String) configKeys.nextElement();

				// the config-key enumeration contains additional keys that we
				// don't want to process here ...
				if (CONFIG_REFRESH.equals(configKey) || CONFIG_TEMP_SCALE.equals(configKey)
						|| "service.pid".equals(configKey)) {
					continue;
				}

				String userid;
				String configKeyTail;

				if (configKey.contains(".")) {
					String[] keyElements = configKey.split("\\.");
					userid = keyElements[0];
					configKeyTail = keyElements[1];

				} else {
					userid = DEFAULT_USER_ID;
					configKeyTail = configKey;
				}

				OAuthCredentials credentials = credentialsCache.get(userid);
				if (credentials == null) {
					credentials = new OAuthCredentials(userid);
					credentialsCache.put(userid, credentials);
				}

				String value = (String) config.get(configKey);

				if (CONFIG_APP_KEY.equals(configKeyTail)) {
					credentials.appKey = value;
				} else if (CONFIG_SCOPE.equals(configKeyTail)) {
					credentials.scope = value;
				} else {
					throw new ConfigurationException(configKey, "the given configKey '" + configKey + "' is unknown");
				}
			}

			// Verify the completeness of each OAuthCredentials entry
			// to make sure we can get started.

			boolean properlyConfigured = true;

			for (String userid : credentialsCache.keySet()) {
				OAuthCredentials oauthCredentials = getOAuthCredentials(userid);
				String userString = (DEFAULT_USER_ID.equals(userid)) ? "" : (userid + ".");
				if (oauthCredentials.appKey == null) {
					logger.error("Required ecobee:{}{} is missing.", userString, CONFIG_APP_KEY);
					properlyConfigured = false;
					break;
				}
				if (oauthCredentials.scope == null) {
					logger.error("Required ecobee:{}{} is missing.", userString, CONFIG_SCOPE);
					properlyConfigured = false;
					break;
				}
			}

			setProperlyConfigured(properlyConfigured);
		}
	}

	/**
	 * Creates the necessary {@link Selection} object to request all information required from the Ecobee API for all
	 * thermostats and sub-objects that have a binding, per set of credentials configured in openhab.cfg. One
	 * {@link ThermostatRequest} can then query all information in one go.
	 * 
	 * @param oauthCredentials
	 *            constrain the resulting Selection object to only select the thermostats which the configuration
	 *            indicates can be reached using these credentials.
	 * @returns the Selection object, or <code>null</code> if only an unsuitable Selection is possible.
	 */
	private Selection createSelection(OAuthCredentials oauthCredentials) {
		final Selection selection = new Selection(SelectionType.THERMOSTATS, null);
		final Set<String> thermostatIdentifiers = new HashSet<String>();

		for (final EcobeeBindingProvider provider : this.providers) {
			for (final String itemName : provider.getItemNames()) {

				final String thermostatIdentifier = provider.getThermostatIdentifier(itemName);
				final String property = provider.getProperty(itemName);

				/*
				 * We are only concerned with inbound items, so there would be no point to including the criteria for
				 * this item.
				 * 
				 * We are also only concerned with items that can be reached by the given credentials.
				 */

				if (!provider.isInBound(itemName)
						|| oauthCredentials != getOAuthCredentials(provider.getUserid(itemName))) {
					continue;
				}

				thermostatIdentifiers.add(thermostatIdentifier);

				if (property.startsWith("settings")) {
					selection.setIncludeSettings(true);
				} else if (property.startsWith("runtime")) {
					selection.setIncludeRuntime(true);
				} else if (property.startsWith("extendedRuntime")) {
					selection.setIncludeExtendedRuntime(true);
				} else if (property.startsWith("electricity")) {
					selection.setIncludeElectricity(true);
				} else if (property.startsWith("devices")) {
					selection.setIncludeDevice(true);
				} else if (property.startsWith("electricity")) {
					selection.setIncludeElectricity(true);
				} else if (property.startsWith("location")) {
					selection.setIncludeLocation(true);
				} else if (property.startsWith("technician")) {
					selection.setIncludeTechnician(true);
				} else if (property.startsWith("utility")) {
					selection.setIncludeUtility(true);
				} else if (property.startsWith("management")) {
					selection.setIncludeManagement(true);
				} else if (property.startsWith("weather")) {
					selection.setIncludeWeather(true);
				} else if (property.startsWith("events")) {
					selection.setIncludeEvents(true);
				} else if (property.startsWith("program")) {
					selection.setIncludeProgram(true);
				} else if (property.startsWith("houseDetails")) {
					selection.setIncludeHouseDetails(true);
				} else if (property.startsWith("oemCfg")) {
					selection.setIncludeOemCfg(true);
				} else if (property.startsWith("equipmentStatus")) {
					selection.setIncludeEquipmentStatus(true);
				} else if (property.startsWith("notificationSettings")) {
					selection.setIncludeNotificationSettings(true);
				} else if (property.startsWith("privacy")) {
					selection.setIncludePrivacy(true);
				} else if (property.startsWith("version")) {
					selection.setIncludeVersion(true);
				}
			}
		}

		if (thermostatIdentifiers.isEmpty()) {
			logger.info("No Ecobee in-bindings have been found for selection.");
			return null;
		}

		// include all the thermostats we found in the bindings
		selection.setSelectionMatch(thermostatIdentifiers);

		return selection;
	}

	/**
	 * This internal class holds the different credentials necessary for the OAuth2 flow to work. It also provides basic
	 * methods to refresh the tokens.
	 * 
	 * <p>
	 * OAuth States
	 * <table>
	 * <thead>
	 * <tr>
	 * <th>authToken</th>
	 * <th>refreshToken</th>
	 * <th>accessToken</th>
	 * <th>State</th>
	 * </tr>
	 * <thead> <tbody>
	 * <tr>
	 * <td>null</td>
	 * <td></td>
	 * <td></td>
	 * <td>authorize</td>
	 * </tr>
	 * <tr>
	 * <td>non-null</td>
	 * <td>null</td>
	 * <td></td>
	 * <td>request tokens</td>
	 * </tr>
	 * <tr>
	 * <td>non-null</td>
	 * <td>non-null</td>
	 * <td>null</td>
	 * <td>refresh tokens</td>
	 * </tr>
	 * <tr>
	 * <td>non-null</td>
	 * <td>non-null</td>
	 * <td>non-null</td>
	 * <td>if expired, refresh if any error, authorize</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * 
	 * @author John Cocula
	 * @since 1.7.0
	 */
	static class OAuthCredentials {

		private static final String AUTH_TOKEN = "authToken";
		private static final String REFRESH_TOKEN = "refreshToken";
		private static final String ACCESS_TOKEN = "accessToken";

		private String userid;

		/**
		 * The private app key needed in order to interact with the Ecobee API. This must be provided in the
		 * <code>openhab.cfg</code> file.
		 */
		private String appKey;

		/**
		 * The scope needed when authorizing this client to the Ecobee API.
		 * 
		 * @see AuthorizeRequest
		 */
		private String scope;

		/**
		 * The authorization token needed to request the refresh and access tokens. Obtained and persisted when
		 * {@code authorize()} is called.
		 * 
		 * @see AuthorizeRequest
		 * @see #authorize()
		 */
		private String authToken;

		/**
		 * The refresh token to access the Ecobee API. Initial token is received using the <code>authToken</code>,
		 * periodically refreshed using the previous refreshToken, and saved in persistent storage so it can be used
		 * across activations.
		 * 
		 * @see TokenRequest
		 * @see RefreshTokenRequest
		 */
		private String refreshToken;

		/**
		 * The access token to access the Ecobee API. Automatically renewed from the API using the refresh token and
		 * persisted for use across activations.
		 * 
		 * @see #refreshTokens()
		 */
		private String accessToken;

		public OAuthCredentials(String userid) {

			try {
				this.userid = userid;
				load();
			} catch (Exception e) {
				throw new EcobeeException("Cannot create OAuthCredentials.", e);
			}
		}

		private Preferences getPrefsNode() {
			return Preferences.userRoot().node("org.openhab.ecobee." + userid);
		}

		private void load() {
			Preferences prefs = getPrefsNode();
			this.authToken = prefs.get(AUTH_TOKEN, null);
			this.refreshToken = prefs.get(REFRESH_TOKEN, null);
			this.accessToken = prefs.get(ACCESS_TOKEN, null);
		}

		private void save() {
			Preferences prefs = getPrefsNode();
			if (this.authToken != null) {
				prefs.put(AUTH_TOKEN, this.authToken);
			} else {
				prefs.remove(AUTH_TOKEN);
			}

			if (this.refreshToken != null) {
				prefs.put(REFRESH_TOKEN, this.refreshToken);
			} else {
				prefs.remove(REFRESH_TOKEN);
			}

			if (this.accessToken != null) {
				prefs.put(ACCESS_TOKEN, this.accessToken);
			} else {
				prefs.remove(ACCESS_TOKEN);
			}
		}

		public boolean noAccessToken() {
			return this.accessToken == null;
		}

		public void authorize() {
			logger.trace("Authorizing this binding with the Ecobee API.");

			final AuthorizeRequest request = new AuthorizeRequest(this.appKey, this.scope);
			logger.trace("Request: {}", request);

			final AuthorizeResponse response = request.execute();
			logger.trace("Response: {}", response);

			this.authToken = response.getAuthToken();
			this.refreshToken = null;
			this.accessToken = null;
			save();

			logger.info("#########################################################################################");
			logger.info("# Ecobee-Integration: U S E R   I N T E R A C T I O N   R E Q U I R E D !!");
			logger.info("# 1. Login to www.ecobee.com using your '{}' account", this.userid);
			logger.info("# 2. Enter the PIN '{}' in My Apps within the next {} minutes.", response.getEcobeePin(),
					response.getExpiresIn());
			logger.info("# NOTE: Any API attempts will fail in the meantime.");
			logger.info("#########################################################################################");
		}

		/**
		 * This method attempts to advance the authorization process by retrieving the tokens needed to use the API. It
		 * returns <code>true</code> if there is reason to believe that an immediately subsequent API call would
		 * succeed.
		 * <p>
		 * This method requests access and refresh tokens to use the Ecobee API. If there is a <code>refreshToken</code>
		 * , it will be used to obtain the tokens, but if there is only an <code>authToken</code>, that will be used
		 * instead.
		 * 
		 * @return <code>true</code> if there is reason to believe that an immediately subsequent API call would
		 *         succeed.
		 */
		public boolean refreshTokens() {
			if (this.authToken == null) {
				authorize();
				return false;
			} else {
				logger.trace("Refreshing tokens.");

				Request request;

				if (this.refreshToken == null) {
					request = new TokenRequest(this.authToken, this.appKey);
				} else {
					request = new RefreshTokenRequest(this.refreshToken, this.appKey);
				}
				logger.trace("Request: {}", request);

				final TokenResponse response = (TokenResponse) request.execute();
				logger.trace("Response: {}", response);

				if (response.isError()) {
					logger.error("Error retrieving tokens: {}", response.getError());
					return false;
				} else {
					this.refreshToken = response.getRefreshToken();
					this.accessToken = response.getAccessToken();
					save();
					return true;
				}
			}
		}
	}
}
