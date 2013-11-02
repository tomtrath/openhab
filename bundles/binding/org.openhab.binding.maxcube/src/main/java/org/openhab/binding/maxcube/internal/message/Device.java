/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.maxcube.internal.message;

import java.util.Date;
import java.util.Calendar;
import java.util.List;

import org.openhab.binding.maxcube.internal.Utils;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;

/**
 * Base class for devices provided by the MAX!Cube protocol.
 * 
 * @author Andreas Heil (info@aheil.de)
 * @since 1.4.0
 */
public abstract class Device {

	protected String serialNumber;
	private boolean initialized;
	private boolean answer;
	private boolean error;
	private boolean valid;
	private boolean DstSettingsActive;
	private boolean gatewayKnown;
	private boolean panelLocked;
	private boolean linkStatusError;
	private boolean batteryLow;

	public Device(Configuration c) {
		this.serialNumber = c.getSerialNumber();
	}

	public abstract DeviceType getType();

	public abstract String getRFAddress();

	public abstract String getName();

	public abstract Calendar getLastUpdate();

	private static Device create(String rfAddress,
			List<Configuration> configurations) {
		Device returnValue = null;
		for (Configuration c : configurations) {
			if (c.getRFAddress().toUpperCase().equals(rfAddress.toUpperCase())) {
				switch (c.getDeviceType()) {
				case HeatingThermostat:
					return new HeatingThermostat(c);
				case ShutterContact:
					return new ShutterContact(c);
				case WallMountedThermostat:
					return new WallMountedThermostat(c);
				default:
					// Device Tyoe not supported in Decvice.create()
				}
			}
		}
		return returnValue;
	}

	public static Device create(byte[] raw, List<Configuration> configurations) {

		if (raw.length == 0) {
			return null;
		}

		String rfAddress = Utils.toHex(raw[0] & 0xFF, raw[1] & 0xFF,
				raw[2] & 0xFF);

		// Based on the RF address and the corresponding configuration,
		// create the device based on the type specified in it's configuration

		Device device = Device.create(rfAddress, configurations);

		// byte 4 is skipped

		// multiple device information are encoded in those particular bytes
		boolean[] bits1 = Utils.getBits(Utils.fromByte(raw[4]));
		boolean[] bits2 = Utils.getBits(Utils.fromByte(raw[5]));

		device.setInitialized(bits1[1]);
		device.setAnswer(bits1[2]);
		device.setError(bits1[3]);
		device.setValid(bits1[4]);

		device.setDstSettingActive(bits2[3]);
		device.setGatewayKnown(bits2[4]);
		device.setPanelLocked(bits2[5]);
		device.setLinkStatusError(bits2[6]);
		device.setBatteryLow(bits2[7]);
	
		// TODO move the device specific readings into the sub classes
		switch (device.getType()) {
		case WallMountedThermostat:
		case HeatingThermostat:
			HeatingThermostat heatingThermostat = (HeatingThermostat) device;
			// "xxxx xx00 = automatic, xxxx xx01 = manual, xxxx xx10 = vacation, xxxx xx11 = boost":
			if (bits2[0] == false && bits2[0] == false) {
				heatingThermostat.setMode(ThermostatModeType.AUTOMATIC);
			} else if (bits2[0] == false && bits2[1] == false) {
				heatingThermostat.setMode(ThermostatModeType.MANUAL);
			} else if (bits2[1] == false && bits2[0] == false) {
				heatingThermostat.setMode(ThermostatModeType.VACATION);
			} else if (bits2[1] == false && bits2[1] == false) {
				heatingThermostat.setMode(ThermostatModeType.BOOST);
			} else {
				// TODO: handel malformed message
			}
			
			heatingThermostat.setValvePosition(raw[6] & 0xFF);
			heatingThermostat.setTemperatureSetpoint(raw[7] & 0xFF);
			
//			9       2     858B        Date until (05-09-2011) (see Encoding/Decoding date/time)
//			B       1     2E          Time until (23:00) (see Encoding/Decoding date/time)
			String hexDate = Utils.toHex(raw[8] & 0xFF, raw[9] & 0xFF);
			int dateValue = Utils.fromHex(hexDate);
			int timeValue = raw[10] & 0xFF;
			Date date = Utils.resolveDateTime(dateValue, timeValue);
			heatingThermostat.setDateSetpoint(date);
			
			break;
		case ShutterContact:
			ShutterContact shutterContact = (ShutterContact) device;
			// xxxx xx10 = shutter open, xxxx xx00 = shutter closed
			if (bits2[1] == true && bits2[0] == false) {
				shutterContact.setShutterState(OpenClosedType.OPEN);
			} else if (bits2[1] == false && bits2[0] == false) {
				shutterContact.setShutterState(OpenClosedType.CLOSED);
			} else {
				// TODO: handel malformed message
			}

			break;
		default:

		}
		return device;
	}

	private void setBatteryLow(boolean batteryLow) {
		this.batteryLow = batteryLow;
	}
	
	public StringType getBatteryLow() {
		return new StringType(this.batteryLow ? "low" : "ok");
	}

	private void setLinkStatusError(boolean linkStatusError) {
		this.linkStatusError = linkStatusError;
	}

	private void setPanelLocked(boolean panelLocked) {
		this.panelLocked = panelLocked;
	}

	private void setGatewayKnown(boolean gatewayKnown) {
		this.gatewayKnown = gatewayKnown;
	}

	private void setDstSettingActive(boolean dstSettingsActive) {
		this.DstSettingsActive = dstSettingsActive;
	}

	private void setValid(boolean valid) {
		this.valid = valid;
	}

	private void setError(boolean error) {
		this.error = error;

	}

	public String getSerialNumber() {
		return serialNumber;
	}

	private void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	private void setAnswer(boolean answer) {
		this.answer = answer;
	}
}