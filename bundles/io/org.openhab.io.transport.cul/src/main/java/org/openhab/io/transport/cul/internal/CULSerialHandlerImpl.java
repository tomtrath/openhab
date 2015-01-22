/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.cul.internal;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.TooManyListenersException;

import org.openhab.io.transport.cul.CULCommunicationException;
import org.openhab.io.transport.cul.CULDeviceException;
import org.openhab.io.transport.cul.CULMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation for culfw based devices which communicate via serial port
 * (cullite for example). This is based on rxtx and assumes constant parameters
 * for the serial port.
 * 
 * @author Till Klocke
 * @since 1.4.0
 */
public class CULSerialHandlerImpl extends AbstractCULHandler implements SerialPortEventListener {

	final static Logger log = LoggerFactory.getLogger(CULSerialHandlerImpl.class);

	private SerialPort serialPort;
	private Integer baudRate = 9600;
	private Integer parityMode = SerialPort.PARITY_EVEN;
	private InputStream is;
	private OutputStream os;
	/**
	 * Default Constructor
	 * @param deviceName
	 * 			String representing the device.
	 * @param mode
	 * 			The RF mode for which the device will be configured.
	 */
	public CULSerialHandlerImpl(String deviceName, CULMode mode) {
		super(deviceName, mode);
	}
	
	
	/**
	 * Constructor including property map for specific configuration.
	 * @param deviceName
	 * 			String representing the device.
	 * @param mode
	 * 			The RF mode for which the device will be configured.
	 * @param properties
	 * 			Property Map containing specific configuration for serial device connection.
	 * 			<ul>
	 * 				<li>"baudrate" (Integer) Setup baudrate</li>
	 * 				<li>"parity" (Integer) Setup parity bit handling. (http://show.docjava.com/book/cgij/code/data/j4pDoc/constant-values.html#serialPort.rxtx.SerialPortInterface.PARITY_NONE)
	 * 			</ul>
	 */
	public CULSerialHandlerImpl(String deviceName, CULMode mode, Map<String, ?> properties){
		super(deviceName, mode);
		
		if(properties.get("baudrate") != null){
			baudRate = (Integer) properties.get("baudrate");
			log.debug("Set baudrate to " + baudRate);
		}
		
		if(properties.get("parity") != null){
			parityMode = (Integer) properties.get("parity");
			log.debug("Set parity to " + parityMode);
		}
		
	}
	
	@Override
	public void serialEvent(SerialPortEvent event) {
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				processNextLine();
			} catch (CULCommunicationException e) {
				log.error("Serial CUL connection read failed for " + deviceName);
			}
		}
	}

	@Override
	protected void openHardware() throws CULDeviceException {
		log.debug("Opening serial CUL connection for " + deviceName);
		try {
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(deviceName);
			if (portIdentifier.isCurrentlyOwned()) {
				throw new CULDeviceException("The port " + deviceName + " is currenty used by "
						+ portIdentifier.getCurrentOwner());
			}
			CommPort port = portIdentifier.open(this.getClass().getName(), 2000);
			if (!(port instanceof SerialPort)) {
				throw new CULDeviceException("The device " + deviceName + " is not a serial port");
			}
			serialPort = (SerialPort) port;
			serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, parityMode);
			is = serialPort.getInputStream();
			os = serialPort.getOutputStream();
			br = new BufferedReader(new InputStreamReader(is));
			bw = new BufferedWriter(new OutputStreamWriter(os));

			serialPort.notifyOnDataAvailable(true);
			log.debug("Adding serial port event listener");
			serialPort.addEventListener(this);
		} catch (NoSuchPortException e) {
			throw new CULDeviceException(e);
		} catch (PortInUseException e) {
			throw new CULDeviceException(e);
		} catch (UnsupportedCommOperationException e) {
			throw new CULDeviceException(e);
		} catch (IOException e) {
			throw new CULDeviceException(e);
		} catch (TooManyListenersException e) {
			throw new CULDeviceException(e);
		}

	}

	@Override
	protected void closeHardware() {
		log.debug("Closing serial device " + deviceName);
		if (serialPort != null) {
			serialPort.removeEventListener();
		}
		try {
			if (br != null) {
				br.close();
			}
			if (bw != null) {
				bw.close();
			}
		} catch (IOException e) {
			log.error("Can't close the input and output streams propberly", e);
		} finally {
			if (serialPort != null) {
				serialPort.close();
			}
		}

	}
}
