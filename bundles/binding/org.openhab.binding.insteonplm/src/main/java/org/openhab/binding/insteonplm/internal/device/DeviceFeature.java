/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm.internal.device;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.openhab.binding.insteonplm.internal.driver.Driver;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.Msg;
import org.openhab.binding.insteonplm.internal.utils.Utils;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DeviceFeature represents a certain feature (trait) of a given Insteon device, e.g. something
 * operating under a given InsteonAddress that can be manipulated (relay) or read (sensor).
 * 
 * The DeviceFeature does the processing of incoming messages, and handles commands for the
 * particular feature it represents.
 * 
 * It uses four mechanisms for that:
 * 
 * 1) MessageDispatcher: makes high level decisions about an incoming message and then runs the
 * 2) MessageHandler: further processes the message, updates state etc
 * 3) CommandHandler: translates commands from the openhab bus into an Insteon message.
 * 4) PollHandler: creates an Insteon message to query the DeviceFeature
 * 
 * Lastly, StateListeners can register with the DeviceFeature to get notifications when
 * the state of a feature has changed.
 * 
 * The character of a DeviceFeature is thus given by its message and command handlers. The long
 * term goal is to abstract the logic into an xml file, such that new DeviceFeatures (for new devices)
 * can be plugged together from existing Handlers without touching the source code. Right now the
 * DeviceFeatures are hard coded in a factory method.
 * We decided to wait with going to xml until the problem domain (the quirks of the Insteon bus)
 * are better understood, and more devices are available for testing.
 * 
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 * @since 1.5.0
 */
public class DeviceFeature implements StatePublisher {
	public static enum QueryStatus {
		NEVER_QUERIED,
		QUERY_PENDING,
		QUERY_ANSWERED
	}

	private static final Logger logger = LoggerFactory.getLogger(DeviceFeature.class);
	
	private InsteonDevice				m_device = null;
	private String						m_name	 = "INVALID_FEATURE_NAME";
	private boolean						m_isStatus = false;
	private QueryStatus	                m_queryStatus = QueryStatus.NEVER_QUERIED;
	
	private MessageHandler				m_defaultMsgHandler = new DefaultMsgHandler(this);
	private CommandHandler				m_defaultCommandHandler = new WarnCommandHandler(this);
	private PollHandler					m_pollHandler = null;
	private	MessageDispatcher			m_dispatcher = null;
	private HashMap<String, String>		m_parameters = new HashMap<String, String>();

	private HashMap<Integer, MessageHandler> m_msgHandlers =
				new HashMap<Integer, MessageHandler>();
	private HashMap<Class<? extends Command>, CommandHandler> m_commandHandlers =
				new HashMap<Class<? extends Command>, CommandHandler>();
	private ArrayList<StateListener> m_listeners = new ArrayList<StateListener>();
	
	/**
	 * Constructor
	 * @param device Insteon device to which this feature belongs
	 * @param name descriptive name for that feature
	 */
	public DeviceFeature(InsteonDevice device, String name) {
		m_name = name;
		setDevice(device);
	}

	/**
	 * Constructor
	 * @param name descriptive name of the feature
	 */
	public DeviceFeature(String name) {
		m_name = name;
	}
	
	public String	 	 getName()			{ return m_name; }
	public QueryStatus	 getQueryStatus()	{ return m_queryStatus; }
	public String		 getParameter(String p)	{ return m_parameters.get(p); }
	public InsteonDevice getDevice() 		{ return m_device; }
	public boolean 	hasListeners() 			{ return !m_listeners.isEmpty(); }
	public boolean	isStatusFeature()		{ return m_isStatus; }
	
	
	public void	setStatusFeature(boolean f)		{ m_isStatus = f; }
	public void	setPollHandler(PollHandler h)	{ m_pollHandler = h; }
	public void	setDevice(InsteonDevice d)		{ m_device = d; }
	public void setMessageDispatcher(MessageDispatcher md) { m_dispatcher = md; }
	public void setDefaultCommandHandler(CommandHandler ch) { m_defaultCommandHandler = ch; }
	public void setDefaultMsgHandler(MessageHandler mh) { m_defaultMsgHandler = mh; }
	public void setParameters(HashMap<String,String> p) { m_parameters = p;	}
	
	public void	 setQueryStatus(QueryStatus status)	{
		logger.trace("{} set query status to: {}", m_name, status);
		m_queryStatus = status;
	}
	

	public void addListener(StateListener l) {
		synchronized(m_listeners) {
			if (!m_listeners.contains(l)) {
				m_listeners.add(l);
			}
		}
	}
	public void removeListener(StateListener listener) {
		synchronized(m_listeners) {
			m_listeners.remove(listener);
		}
	}
	public void addMessageHandler(int cm1, MessageHandler handler) {
		synchronized(m_msgHandlers) {
			m_msgHandlers.put(cm1, handler);
		}
	}
	public void addCommandHandler(Class<? extends Command> c, CommandHandler handler) {
		synchronized(m_commandHandlers) {
			m_commandHandlers.put(c, handler);
		}
	}
	
	public boolean handleMessage(Msg msg, String port) {
		if (m_dispatcher == null) {
			logger.error("{} no dispatcher for msg {}", m_name, msg);
			return false;
		}
		return (m_dispatcher.dispatch(msg, port));
	}
	
	public void handleCommand(Command cmd) {
		Class<? extends Command> key = cmd.getClass();
		CommandHandler h = m_commandHandlers.containsKey(key) ? m_commandHandlers.get(key) : m_defaultCommandHandler;
		logger.trace("{} uses {} to handle command {} for {}", getName(), h.getClass().getSimpleName(),
				key.getSimpleName(), getDevice().getAddress());
		h.handleCommand(cmd, getDevice());
	}
	
	public Msg makePollMsg() {
		if (m_pollHandler == null) return null;
		logger.trace("{} making poll msg for {} using handler {}", getName(), getDevice().getAddress(),
				m_pollHandler.getClass().getSimpleName());
		Msg m = m_pollHandler.makeMsg(m_device);
		return m;
	}
	
	
	public void publish(State newState) {
		logger.debug("{} publishing: {}", getName(), newState);
		synchronized(m_listeners) {
			for (StateListener listener : m_listeners) {
				listener.stateChanged(newState);
			}
		}
	}
	@Override
	public String toString() {
		return m_name + "(" + m_listeners.size()  +":" +m_commandHandlers.size() + ":" + m_msgHandlers.size() + ")";
	}
	
	//
	// --- handlers for various incoming Insteon messages --------
	//
	/**
	 * Base class for all message handlers. A message handler processes incoming
	 * Insteon messages and reacts by publishing corresponding messages on the openhab
	 * bus, updates device state etc.
	 */
	public static abstract class MessageHandler {
		StatePublisher m_pub = null;
		/**
		 * Constructor
		 * @param p state publishing object for dissemination of state changes
		 */
		MessageHandler(StatePublisher p) {
				m_pub = p;
		}
		/**
		 * Method that processes incoming message. The cmd1 parameter
		 * has been extracted earlier already (to make a decision which message handler to call),
		 * and is passed in as an argument so cmd1 does not have to be extracted from the message again.
		 * @param cmd1 the insteon cmd1 field
		 * @param msg the received insteon message
		 * @param feature the DeviceFeature to which this message handler is attached
		 * @param fromPort the device (/dev/ttyUSB0) from which the message has been received
		 */
		public abstract void handleMessage(byte cmd1, Msg msg, DeviceFeature feature,
									String fromPort);
	}
	
	public static class DefaultMsgHandler extends MessageHandler {
		DefaultMsgHandler(StatePublisher p) { super(p); }
		@Override
		public void handleMessage(byte cmd1, Msg msg, DeviceFeature f,
				String fromPort) {
			logger.debug("drop unimpl message {}: {}", Utils.getHexByte(cmd1), msg);
		}
	}

	public static class NoOpMsgHandler extends MessageHandler {
		NoOpMsgHandler(StatePublisher p) { super(p); }
		@Override
		public void handleMessage(byte cmd1, Msg msg, DeviceFeature f,
				String fromPort) {
			logger.debug("ignore msg {}: {}", Utils.getHexByte(cmd1), msg);
		}
	}

	public static class LightOnDimmerHandler extends MessageHandler {
		LightOnDimmerHandler(StatePublisher p) { super(p); }
		@Override
		public void handleMessage(byte cmd1, Msg msg, DeviceFeature f,
				String fromPort) {
			// There are two ways we can get cmd1 == 0x11 messages:
			// 1) When we query (poll). In this case, we get a DIRECT_ACK back,
			//    and the cmd2 code has the new light level
			// 2) When the switch/dimmer is switched completely on manually,
			//    i.e. by physically tapping the button. 
			try {
				InsteonAddress a = f.getDevice().getAddress();
				if (msg.isAckOfDirect()) {
					 // got this in response to query, check cmd2 for light level
					int cmd2 = (int) (msg.getByte("command2") & 0xff);
					if (cmd2 > 0) {
						if (cmd2 == 0xff) {
							// only if it's fully on should we send
							// an ON status message
							logger.info("LightOnDimmerHandler: device {} was turned fully on", a);
							m_pub.publish(OnOffType.ON);
						} else {
							int level = Math.max(1, (cmd2*100)/255);
							logger.info("LightOnDimmerHandler: device {} was set to level {}", a, level);
							m_pub.publish(new PercentType(level));
						}
					} else {
						logger.info("LightOnDimmerHandler: device {} was turned fully off", a);
						m_pub.publish(OnOffType.OFF);
					}
				} else {
					// if we get this via broadcast, ignore the light level and just switch on
					m_pub.publish(OnOffType.ON);
				}
			}  catch (FieldException e) {
				logger.error("error parsing {}: ", msg, e);
			}
		}
	}
	public static class LightOnSwitchHandler extends MessageHandler {
		LightOnSwitchHandler(StatePublisher p) { super(p); }
		@Override
		public void handleMessage(byte cmd1, Msg msg, DeviceFeature f,
				String fromPort) {
			m_pub.publish(OnOffType.ON);
		}
	}
	
	public static class LightOffHandler extends MessageHandler {
		LightOffHandler(StatePublisher p) { super(p); }
		@Override
		public void handleMessage(byte cmd1, Msg msg, DeviceFeature f,
				String fromPort) {
			m_pub.publish(OnOffType.OFF);
		}
	}
	
	public static class LightStateSwitchHandler extends  MessageHandler {
		LightStateSwitchHandler(StatePublisher p) { super(p); }
		@Override
		public void handleMessage(byte cmd1, Msg msg, DeviceFeature f,
				String fromPort) {
			try {
				InsteonAddress a = f.getDevice().getAddress();
				int cmd2 = (int) (msg.getByte("command2") & 0xff);
				if (cmd2 == 0) {
					logger.info("LightStateSwitchHandler: set device {} to OFF", a);
					m_pub.publish(OnOffType.OFF);
				} else if (cmd2 == 0xff) {
					logger.info("LightStateSwitchHandler: set device {} to ON", a);
					m_pub.publish(OnOffType.ON);
				} else {
					logger.warn("LightStateSwitchHandler: {} ignoring unexpected" +
							" cmd2 in msg: {}", a, msg);
				}
			} catch (FieldException e) {
				logger.error("error parsing {}: ", msg, e);
			}
		}
	}
	public static class LightStateDimmerHandler extends  MessageHandler {
		LightStateDimmerHandler(StatePublisher p) { super(p); }
		@Override
		public void handleMessage(byte cmd1, Msg msg, DeviceFeature f,
				String fromPort) {
			InsteonDevice dev = f.getDevice();
			try {
				int cmd2 = (int) (msg.getByte("command2") & 0xff);

				int level = cmd2*100/255;
				if (level == 0 && cmd2 > 0) level = 1;
				if (cmd2 == 0) {
					logger.info("LightStateDimmerHandler: set device {} to OFF",
							dev.getAddress());
					m_pub.publish(OnOffType.OFF);
				} else if (cmd2 == 0xff) {
					logger.info("LightStateDimmerHandler: set device {} to ON",
							dev.getAddress());
					m_pub.publish(OnOffType.ON);
				} else {
					logger.info("LightStateDimmerHandler: set device {} to level {}",
							dev.getAddress(), level);
				}
				m_pub.publish(new PercentType(level));
			} catch (FieldException e) {
				logger.error("error parsing {}: ", msg, e);
			}
		}
	}
	
	public static class StopManualChangeHandler extends MessageHandler {
		StopManualChangeHandler(StatePublisher p) { super(p); }
		@Override
		public void handleMessage(byte cmd1, Msg msg, DeviceFeature f,
				String fromPort) {
			Msg m = f.makePollMsg();
			if (m != null)	f.getDevice().enqueueMessage(m, f);
		}
	}
	
	public static class InfoRequestReplyHandler extends MessageHandler {
		InfoRequestReplyHandler(StatePublisher p) { super(p); }
		@Override
		public void handleMessage(byte cmd1, Msg msg, DeviceFeature f,
				String fromPort) {
			InsteonDevice dev = f.getDevice();
			if (!msg.isExtended()) {
				logger.warn("device {} expected extended msg as info reply, got {}", dev.getAddress(), msg);
				return;
			}
			try {
				int cmd2 = (int) (msg.getByte("command2") & 0xff);
				switch (cmd2) {
				case 0x00: // this is a product data response message
					int prodKey = msg.getInt24("userData2", "userData3", "userData4");
					int devCat  = msg.getByte("userData5");
					int subCat  = msg.getByte("userData6");
					logger.info("{} got product data: cat: {} subcat: {} key: {} ", dev.getAddress(), devCat, subCat,
							Utils.getHexString(prodKey));
					break;
				case 0x02: // this is a device text string response message
					logger.info("{} got text str {} ", dev.getAddress(), msg);
					break;
				default:
					logger.warn("unknown cmd2 = {} in info reply message {}", cmd2, msg);
					break;
				}
			} catch (FieldException e) {
				logger.error("error parsing {}: ", msg, e);
			}
		}
	}

	public static class LastTimeHandler extends MessageHandler {
		LastTimeHandler(StatePublisher p) { super(p); }
		@Override
		public void handleMessage(byte cmd1a, Msg msg, DeviceFeature f,
					String fromPort) {
			GregorianCalendar calendar = new GregorianCalendar();
	        calendar.setTimeInMillis(System.currentTimeMillis());
	        DateTimeType t = new DateTimeType(calendar);
			m_pub.publish(t);
		}
	}

	
	public static class StateContactHandler extends MessageHandler {
		StateContactHandler(StatePublisher p) { super(p); }
		@Override
		public void handleMessage(byte cmd1a, Msg msg, DeviceFeature f,
					String fromPort) {
			byte cmd  = 0x00;
			byte cmd2 = 0x00;
			try {
				cmd = msg.getByte("Cmd");
				cmd2 = msg.getByte("command2");
			} catch (FieldException e) {
				logger.debug("no cmd found, dropping msg {}", msg);
				return;
			}
			if (msg.isAckOfDirect() && (f.getQueryStatus() == QueryStatus.QUERY_PENDING)
					&& cmd == 0x50) {
				OpenClosedType oc = (cmd2 == 0) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
				logger.info("StateContactHandler: set contact {} to: {}", f.getDevice().getAddress(), oc);
				m_pub.publish(oc);
			}
		}
	}
	
	public static class ClosedContactHandler extends MessageHandler {
		ClosedContactHandler(StatePublisher p) { super(p); }
		@Override
		public void handleMessage(byte cmd1, Msg msg, DeviceFeature f,
				String fromPort) {
			m_pub.publish(OpenClosedType.CLOSED);
		}
	}

	public static class OpenedContactHandler extends MessageHandler {
		OpenedContactHandler(StatePublisher p) { super(p); }
		@Override
		public void handleMessage(byte cmd1, Msg msg, DeviceFeature f,
				String fromPort) {
			m_pub.publish(OpenClosedType.OPEN);
		}
	}

	//
	// --- poll handlers create the appropriate polling messages
	//

	/**
	 * A PollHandler creates an Insteon message to query a particular
	 * DeviceFeature of an Insteon device.  
	 */
	public static abstract class PollHandler {
		DeviceFeature m_feature = null;
		/**
		 * Constructor
		 * @param feature The device feature to be polled
		 */
		PollHandler(DeviceFeature feature) {
			m_feature = feature;
		}
		/**
		 * Creates Insteon message that can be used to poll a feature
		 * via the Insteon network.
		 * @param device reference to the insteon device to be polled
		 * @return Insteon query message or null if creation failed
		 */
		public abstract Msg makeMsg(InsteonDevice device);
	}
	
	public static class DefaultPollHandler extends PollHandler {
		DefaultPollHandler(DeviceFeature f) { super(f); }
		@Override
		public Msg makeMsg(InsteonDevice d) {
			Msg m = null;
			try {
				m = d.makeStandardMessage((byte)0x0f, (byte)0x19, (byte)0x00);
				m.setQuietTime(500L);
			} catch (FieldException e) {
				logger.warn("error setting field in msg: ", e);
			} catch (IOException e) {
				logger.error("poll failed with exception ", e);
			}
			return m;
		}
	}
	
	public static class ContactPollHandler extends PollHandler {
		ContactPollHandler(DeviceFeature f) { super(f); }
		@Override
		public Msg makeMsg(InsteonDevice d) {
			Msg m = null;
			try {
				// setting cmd2 = 0x01 will return the LED bit flags
				// rather than the relay status!
				m = d.makeStandardMessage((byte)0x0f, (byte)0x19, (byte)0x01);
				m.setQuietTime(500L);
			} catch (FieldException e) {
				logger.warn("error setting field in msg: ", e);
			} catch (IOException e) {
				logger.error("poll failed with exception ", e);
			}
			return m;
		}
	}

	public static class NoPollHandler extends PollHandler {
		NoPollHandler(DeviceFeature f) { super(f); }
		@Override
		public Msg makeMsg(InsteonDevice d) {
			return null;
		}
	}
	//
	// --- command handlers to translate OpenHAB commands into insteon messages
	//
	/**
	 * A command handler translates an openHAB command into a insteon message
	 *
	 */
	public static abstract class CommandHandler {
		DeviceFeature m_feature = null; // related DeviceFeature
		/**
		 * Constructor
		 * @param feature The DeviceFeature for which this command was intended.
		 * The openHAB commands are issued on an openhab item. The .items files bind
		 * an openHAB item to a DeviceFeature.
		 */
		CommandHandler(DeviceFeature feature) {
			m_feature = feature;
		}
		/**
		 * Implements what to do when an openHAB command is received
		 * @param cmd the openhab command issued
		 * @param device the Insteon device to which this command applies
		 */
		public abstract void handleCommand(Command cmd, InsteonDevice device);
	}
	

	public static class WarnCommandHandler extends CommandHandler {
		WarnCommandHandler(DeviceFeature f) { super(f); }
		@Override
		public void handleCommand(Command cmd, InsteonDevice dev) {
			logger.warn("command {} is not implemented yet!", cmd);
		}
	}
	
	public static class NoOpCommandHandler extends CommandHandler {
		NoOpCommandHandler(DeviceFeature f) { super(f); }
		@Override
		public void handleCommand(Command cmd, InsteonDevice dev) {
			// do nothing, not even log
		}
	}
	
	public static class LightOnOffCommandHandler extends CommandHandler {
		LightOnOffCommandHandler(DeviceFeature f) { super(f); }
		@Override
		public void handleCommand(Command cmd, InsteonDevice dev) {
			try {
				if (cmd == OnOffType.ON) {
					Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x11, (byte) 0xff);
					dev.enqueueMessage(m, m_feature);
					logger.info("LightOnOffCommandHandler: sent msg to switch {} on", dev.getAddress());
				} else if (cmd == OnOffType.OFF) {
					Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x13, (byte) 0x00);
					dev.enqueueMessage(m, m_feature);
					logger.info("LightOnOffCommandHandler: sent msg to switch {} off", dev.getAddress());
				}
				// expect to get a direct ack after this!
			} catch (IOException e) {
				logger.error("command send i/o error: ", e);
			} catch (FieldException e) {
				logger.error("command send message creation error ", e);
			}
		}
	}
	
	public static class IOLincOnOffCommandHandler extends CommandHandler {
		IOLincOnOffCommandHandler(DeviceFeature f) { super(f); }
		@Override
		public void handleCommand(Command cmd, InsteonDevice dev) {
			try {
				if (cmd == OnOffType.ON) {
					Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x11, (byte) 0xff);
					dev.enqueueMessage(m, m_feature);
					logger.info("IOLincOnOffCommandHandler: sent msg to switch {} on", dev.getAddress());
				} else if (cmd == OnOffType.OFF) {
					Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x13, (byte) 0x00);
					dev.enqueueMessage(m, m_feature);
					logger.info("IOLincOnOffCommandHandler: sent msg to switch {} off", dev.getAddress());
				}
				// expect DIRECT_ACK for this one
				String ds = m_feature.getParameter("momentary");
				int delay = (ds == null) ? 2000 : Integer.parseInt(ds);
				delay = Math.max(1000, delay);
				delay = Math.min(10000, delay);
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
						@Override
						public void run() {
							Msg m = m_feature.makePollMsg();
							InsteonDevice dev = m_feature.getDevice();
							if (m != null) dev.enqueueMessage(m, m_feature);
						}
					}, delay);
			} catch (IOException e) {
				logger.error("command send i/o error: ", e);
			} catch (FieldException e) {
				logger.error("command send message creation error: ", e);
			}
		}
	}

	public static class IncreaseDecreaseHandler extends CommandHandler {
		IncreaseDecreaseHandler(DeviceFeature f) { super(f); }
		@Override
		public void handleCommand(Command cmd, InsteonDevice dev) {
			try {
				if (cmd == IncreaseDecreaseType.INCREASE) {
					Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x15, (byte) 0x00);
					dev.enqueueMessage(m, m_feature);
					logger.info("IncreaseDecreaseHandler: sent msg to brighten {}", dev.getAddress());
				} else if (cmd == IncreaseDecreaseType.DECREASE) {
					Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x16, (byte) 0x00);
					dev.enqueueMessage(m, m_feature);
					logger.info("IncreaseDecreaseHandler: sent msg to dimm {}", dev.getAddress());
				}
			} catch (IOException e) {
				logger.error("command send i/o error: ", e);
			} catch (FieldException e) {
				logger.error("command send message creation error ", e);
			}
		}
	}
	public static class PercentHandler extends CommandHandler {
		PercentHandler(DeviceFeature f) { super(f); }
		@Override
		public void handleCommand(Command cmd, InsteonDevice dev) {
			try {
				PercentType pc = (PercentType)cmd;
				logger.debug("changing level of {} to {}", dev.getAddress(), pc.intValue());
				int level = (pc.intValue()*255)/100;
				if (level > 0) { // make light on message with given level
					Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x11, (byte) level);
					dev.enqueueMessage(m, m_feature);
					logger.info("PercentHandler: sent msg to set {} to {}", dev.getAddress(), level);
				} else { // switch off
					Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x13, (byte) 0x00);
					dev.enqueueMessage(m, m_feature);
					logger.info("PercentHandler: sent msg to set {} to zero by switching off", dev.getAddress());
				}
			} catch (IOException e) {
				logger.error("command send i/o error: ", e);
			} catch (FieldException e) {
				logger.error("command send message creation error ", e);
			}
		}
	}
	public static class ModemCommandHandler extends CommandHandler {
		ModemCommandHandler(DeviceFeature f) { super(f); }
		@Override
		public void handleCommand(Command cmd, InsteonDevice modem) {
			if (!(cmd instanceof DecimalType)) return;
			DecimalType d = (DecimalType)cmd;
			int num = d.intValue();
			if (num != 1) return;
			Driver dr = m_feature.getDevice().getDriver();
			HashMap<InsteonAddress, InsteonDevice> devs = dr.getDeviceList(); 
			for (InsteonDevice dev : devs.values()) {
				if (!dev.isReferenced() && dev.hasLinkRecords()) {
					logger.info("ModemCommandHandler: erasing {} from modem link database", dev.getAddress());
					dev.eraseFromModem();
					break; // only one at a time
				}
			}
		}

	}
	/**
	 * Does preprocessing of messages to decide which handler should be called
	 *
	 */
	private static abstract class MessageDispatcher {
		DeviceFeature m_feature = null;
		/**
		 * Constructor
		 * @param f DeviceFeature to which this MessageDispatcher belongs
		 */
		MessageDispatcher(DeviceFeature f) {
			m_feature = f;
		}
		/**
		 * Dispatches message
		 * @param msg Message to dispatch
		 * @param port Insteon device ('/dev/usb') from which the message came
		 * @return true if dispatch was successful, false otherwise
		 */
		abstract boolean dispatch(Msg msg, String port);
	}

	private static class DefaultDispatcher extends MessageDispatcher {
		DefaultDispatcher(DeviceFeature f) { super(f); }
		@Override
		public boolean dispatch(Msg msg, String port) {
			byte cmd  = 0x00;
			byte cmd1 = 0x00;
			boolean isConsumed = false;
			int key = -1;
			try {
				cmd  = msg.getByte("Cmd");
				cmd1 = msg.getByte("command1");
			} catch (FieldException e) {
				logger.debug("no command found, dropping msg {}", msg);
				return isConsumed;
			}
			if (msg.isAckOfDirect()) {
				// in the case of direct ack, the cmd1 code is useless.
				// you have to know what message was sent before to
				// interpret the reply message
				if (m_feature.getQueryStatus() == QueryStatus.QUERY_PENDING
						&& cmd == 0x50) {
					// must be a reply to our message, tweak the cmd1 code!
					logger.trace("changing key to 0x19 for msg {}", msg);
					key = 0x19; // we have installed a handler under that command number
					isConsumed = true;
				} else {
					key = -1;
				}
			} else {
				key = (cmd1 & 0xFF);
			}
			if (key != -1 || m_feature.isStatusFeature()) {
				MessageHandler h = m_feature.m_msgHandlers.get(key);
				if (h == null) h = m_feature.m_defaultMsgHandler;
				logger.trace("{}:{}->{} {}", m_feature.getDevice().getAddress(), m_feature.getName(),
						h.getClass().getSimpleName(), msg);
				h.handleMessage(cmd1, msg, m_feature, port);
			}
			if (isConsumed) {
				m_feature.setQueryStatus(QueryStatus.QUERY_ANSWERED);
			}

			return isConsumed;
		}
	}
	
	
	private static class PassThroughDispatcher extends MessageDispatcher {
		PassThroughDispatcher(DeviceFeature f) { super(f); }
		@Override
		public boolean dispatch(Msg msg, String port) {
			MessageHandler h = m_feature.m_defaultMsgHandler;
			logger.trace("{}:{}->{} {}", m_feature.getDevice().getAddress(), m_feature.getName(),
					h.getClass().getSimpleName(), msg);
			h.handleMessage((byte)0x01, msg, m_feature, port);
			return false;
		}
	}
	
	/**
	 * Factory method for creating DeviceFeatures. Once the problem domain is better understood
	 * and there are more devices, this should be done with an xml file.
	 * @param s The name of the device feature to create.
	 * @return The newly created DeviceFeature, or null if requested DeviceFeature does not exist.
	 */
	
	public static DeviceFeature s_makeDeviceFeature(String s) {
		DeviceFeature f = null;
		if (s.equals("PLACEHOLDER")) {
			f = new DeviceFeature("PLACEHOLDER");
		} else	if (s.equals("2477Sswitch")) {
			f = new DeviceFeature("2477Sswitch");
			f.setMessageDispatcher(new DefaultDispatcher(f));
			f.addMessageHandler(new Integer(0x11), new LightOnSwitchHandler(f));
			f.addMessageHandler(new Integer(0x13), new LightOffHandler(f));
			f.addMessageHandler(new Integer(0x19), new LightStateSwitchHandler(f));
			f.addCommandHandler(OnOffType.class, new LightOnOffCommandHandler(f));
			f.setPollHandler(new DefaultPollHandler(f));
		} else	if (s.equals("2477Slasttime")) {
			f = new DeviceFeature("2477Slasttime");
			f.setStatusFeature(true);
			f.setMessageDispatcher(new PassThroughDispatcher(f));
			f.setDefaultMsgHandler(new LastTimeHandler(f));
			f.setDefaultCommandHandler(new NoOpCommandHandler(f));
		} else	if (s.equals("2477Ddimmer")) {
			f = new DeviceFeature("2477Ddimmer");
			f.setMessageDispatcher(new DefaultDispatcher(f));
			f.addMessageHandler(new Integer(0x06), new NoOpMsgHandler(f));
			f.addMessageHandler(new Integer(0x11), new LightOnDimmerHandler(f));
			f.addMessageHandler(new Integer(0x13), new LightOffHandler(f));
			f.addMessageHandler(new Integer(0x17), new NoOpMsgHandler(f));
			f.addMessageHandler(new Integer(0x18), new StopManualChangeHandler(f));
			f.addMessageHandler(new Integer(0x19), new LightStateDimmerHandler(f));
			f.addCommandHandler(PercentType.class, new PercentHandler(f));
			f.addCommandHandler(OnOffType.class, new LightOnOffCommandHandler(f));
			f.setPollHandler(new DefaultPollHandler(f));
		} else	if (s.equals("2477Dlasttime")) {
			f = new DeviceFeature("2477Dlasttime");
			f.setStatusFeature(true);
			f.setMessageDispatcher(new PassThroughDispatcher(f));
			f.setDefaultMsgHandler(new LastTimeHandler(f));
			f.setDefaultCommandHandler(new NoOpCommandHandler(f));
		} else	if (s.equals("2450IOLincContact")) {
			f = new DeviceFeature("2450IOLincContact");
			f.setMessageDispatcher(new DefaultDispatcher(f));
			f.addMessageHandler(new Integer(0x06), new NoOpMsgHandler(f));
			f.addMessageHandler(new Integer(0x11), new OpenedContactHandler(f));
			f.addMessageHandler(new Integer(0x13), new ClosedContactHandler(f));
			f.addMessageHandler(new Integer(0x19), new StateContactHandler(f));
			f.addCommandHandler(OnOffType.class, new NoOpCommandHandler(f));
			f.setPollHandler(new ContactPollHandler(f));
		} else	if (s.equals("2842contact")) {
			f = new DeviceFeature("2842contact");
			f.setMessageDispatcher(new DefaultDispatcher(f));
			f.addMessageHandler(new Integer(0x06), new NoOpMsgHandler(f));
			f.addMessageHandler(new Integer(0x11), new OpenedContactHandler(f));
			f.addMessageHandler(new Integer(0x13), new ClosedContactHandler(f));
			f.addCommandHandler(OnOffType.class, new NoOpCommandHandler(f));
			f.setPollHandler(new NoPollHandler(f));
		} else	if (s.equals("2450IOLincSwitch")) {
			f = new DeviceFeature("2450IOLincSwitch");
			f.setMessageDispatcher(new DefaultDispatcher(f));
			f.addMessageHandler(new Integer(0x06), new NoOpMsgHandler(f));
			f.addMessageHandler(new Integer(0x11), new NoOpMsgHandler(f));
			f.addMessageHandler(new Integer(0x13), new NoOpMsgHandler(f));
			f.addMessageHandler(new Integer(0x19), new LightStateSwitchHandler(f));
			f.addCommandHandler(OnOffType.class, new IOLincOnOffCommandHandler(f));
			f.setPollHandler(new DefaultPollHandler(f));
		} else	if (s.equals("2450IOLinclasttime")) {
			f = new DeviceFeature("2450IOLinclasttime");
			f.setStatusFeature(true);
			f.setMessageDispatcher(new PassThroughDispatcher(f));
			f.setDefaultMsgHandler(new LastTimeHandler(f));
			f.setDefaultCommandHandler(new NoOpCommandHandler(f));
		} else	if (s.equals("2842lasttime")) {
			f = new DeviceFeature("2842lasttime");
			f.setStatusFeature(true);
			f.setMessageDispatcher(new PassThroughDispatcher(f));
			f.setDefaultMsgHandler(new LastTimeHandler(f));
			f.setDefaultCommandHandler(new NoOpCommandHandler(f));
		} else	if (s.equals("2413Ucontrol")) {
			f = new DeviceFeature("2413Ucontrol");
			f.setStatusFeature(false);
			f.setMessageDispatcher(new PassThroughDispatcher(f));
			f.setDefaultMsgHandler(new NoOpMsgHandler(f));
			f.addCommandHandler(DecimalType.class, new ModemCommandHandler(f));
		} else {
			logger.error("unimplemented feature requested: {}", s);
		}
		return f;
	}
	
}

