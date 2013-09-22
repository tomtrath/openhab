package org.openhab.binding.homecan;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.IllegalClassException;
import org.openhab.binding.homecan.HomecanMsg.MsgType;
import org.openhab.core.binding.AbstractBinding;
import org.openhab.core.binding.BindingProvider;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HomecanBinding extends AbstractBinding<HomecanBindingProvider> implements ManagedService {

	static final Logger logger = LoggerFactory.getLogger(HomecanBinding.class);
	
	private final static int HOMECAN_PORT = 15000;
	
	protected Collection<HomecanMsgTypeMapper> typeMappers = new HashSet<HomecanMsgTypeMapper>();

	private EventPublisher eventPublisher;
	
	protected Thread receiveThread = null;
	protected DatagramSocket clientSocket;
	
	protected boolean shutdown = false;
	
	/**
	 * keeps track of all configItems for which we should send a read request to the HomeCAN bus
	 */
	private Set<HomecanBindingConfigItem> configItemsToInitialize = Collections.synchronizedSet(new HashSet<HomecanBindingConfigItem>());

	/** the configItem initializer, which runs in a separate thread */
	private ConfigItemInitializer initializer = new ConfigItemInitializer();
	
	
	public void addHomecanMsgTypeMapper(HomecanMsgTypeMapper typeMapper) {
		this.typeMappers.add(typeMapper);
	}

	public void removeHomecanMsgTypeMapper(HomecanMsgTypeMapper typeMapper) {
		this.typeMappers.remove(typeMapper);
	}
	
	public void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	public void unsetEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = null;
	}
	

	public Type getType(HomecanMsg msg, Class<? extends Type> typeClass) {
		for (HomecanMsgTypeMapper typeMapper : typeMappers) {
			Type type = typeMapper.toType(msg,typeClass);
			if (type != null)
				return type;
		}
		return null;
	}
	
	private byte[] toHomecanMsgData(Type type,MsgType msgtype) {
		for (HomecanMsgTypeMapper typeMapper : typeMappers) {
			byte[] data = typeMapper.toHomecanMsgData(type,msgtype);
			if (data != null)
				return data;
		}
		return null;
	}

	@Override
	protected void internalReceiveCommand(String itemName, Command command) {		
		sendToHomecan(itemName, command);
	}

	@Override
	protected void internalReceiveUpdate(String itemName, State newState) {
		//sendToHomecan(itemName, newState);
	}
	
	private void sendToHomecan(String itemName, Type type) {
		//get all configItems for itemName
		for (HomecanBindingConfigItem configItem : getConfigItems(itemName, type.getClass())) {
			if (configItem.direction == Direction.OUT || configItem.direction == Direction.BIDIRECTIONAL) {
				HomecanMsg msg = new HomecanMsg(configItem.homecanID.address, configItem.homecanID.msgtype, configItem.homecanID.channel, toHomecanMsgData(type, configItem.homecanID.msgtype));
				//send msg
				try {
					synchronized (clientSocket) {											
						clientSocket.send(msg.getPacket(configItem.homecanID.host, HOMECAN_PORT));
						logger.debug("Send HomeCAN Msg = '{}')", msg.toString());
						Thread.sleep(50);
					}

				} catch (IOException e) {
					logger.warn("Send packet failed "+e);
				} catch (InterruptedException e) {
					logger.warn("Send packet Thread was interrupted "+e);				
				}
			}
		}
	}
	
	public void activate() {
		if (this.receiveThread == null) {
			try {
				this.receiveThread = new ReceiveThread("HomecanBinding" + " Receive Thread");
				this.receiveThread.start();
			} catch (SocketException e) {
				logger.error("receive Thread could not be started.");
			}			
		}
		try {
			clientSocket = new DatagramSocket();
			initializer = new ConfigItemInitializer();
			initializer.start();			
		} catch (SocketException e1) {
			logger.error("Client Socket could not be created.");
		}

	}

	public void deactivate() {
		for (HomecanBindingProvider provider : providers) {
			provider.removeBindingChangeListener(this);
		}
		providers.clear();
		this.shutdown = true;
		initializer.setInterrupted(true);
	}
		
	private class ReceiveThread extends Thread {
		private DatagramSocket serverSocket;
		private byte[] receiveData;	

		public ReceiveThread(String name) throws SocketException {
			super(name);
			this.setDaemon(true);
			shutdown = false;			
			serverSocket = new DatagramSocket(HOMECAN_PORT);
			receiveData = new byte[1024];
		}

		@Override
		public void run() {
			logger.debug(getName() + " has been started");
			while (!shutdown) {
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try {
					serverSocket.receive(receivePacket);					
					HomecanMsg msg = new HomecanMsg(receivePacket);
					InetAddress IPAddress = receivePacket.getAddress();
					logger.debug("Received from '{}' HomeCAN Msg = '{}')", IPAddress, msg.toString());
					for (HomecanBindingConfigItem configItem : getConfigItems(new HomecanID(IPAddress, msg.address, msg.channel, msg.msgtype))) {
						if (configItem.direction == Direction.IN || configItem.direction == Direction.BIDIRECTIONAL) {
							postToOpenhab(configItem, msg);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException iae) {
					logger.warn(getName() + ": Exception occured,");
					iae.printStackTrace();
				}
			}
			logger.info(getName() + " has been shut down");
		}
	}

	private void postToOpenhab(HomecanBindingConfigItem configItem, HomecanMsg msg) {
		Type type = getType(msg, configItem.typeClass);
		if (type != null) {
			if (type instanceof Command && msg.isCommand()) {
				eventPublisher.postCommand(configItem.itemName, (Command) type);
			} else if (type instanceof State && msg.isUpdate()) {
				eventPublisher.postUpdate(configItem.itemName, (State) type);
			} else {
				throw new IllegalClassException("Cannot process homecan msg -> type " + type.toString() + msg.toString());
			}

			logger.trace("Processed event (item='{}', type='{}')", new String[] { configItem.itemName, type.toString() });
			return;
		}
	}
		
	/**
	 * Returns all listening config items. This method iterates over all registered Homecan binding providers and aggregates
	 * the result.
	 * 
	 * @param groupAddress
	 *            the group address that the items are listening to
	 * @return an array of all listening items
	 */
	private Iterable<HomecanBindingConfigItem> getConfigItems(HomecanID id) {
		List<HomecanBindingConfigItem> itemConfigs = new ArrayList<HomecanBindingConfigItem>();
		for (HomecanBindingProvider provider : providers) {
			for (HomecanBindingConfigItem itemConfig : provider.getListeningConfigItems(id)) {
				itemConfigs.add(itemConfig);
			}
		}
		return itemConfigs;
	}
	
	/**
	 * Returns the HomecanIDs for a given item and type class. This method iterates over all registered homecan binding
	 * providers to find the result.
	 * 
	 * @param itemName
	 *            the item name for the HomecanIDs
	 * @param typeClass
	 *            the type class associated to the HomecanIDs
	 * @return the HomecanIDs which corresponds to the given item and type class
	 */
	private Iterable<HomecanBindingConfigItem> getConfigItems(final String itemName, final Class<? extends Type> typeClass) {
		List<HomecanBindingConfigItem> itemConfigs = new ArrayList<HomecanBindingConfigItem>();
		for (HomecanBindingProvider provider : providers) {
			for (HomecanBindingConfigItem itemConfig : provider.getListeningConfigItems(itemName,typeClass)) {
				itemConfigs.add(itemConfig);
			}
		}
		return itemConfigs;
	}

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		// TODO Auto-generated method stub		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void bindingChanged(BindingProvider provider, String itemName) {
		if (provider instanceof HomecanBindingProvider) {
			HomecanBindingProvider homecanProvider = (HomecanBindingProvider) provider;
			for (HomecanBindingConfigItem itemConfig : homecanProvider.getAllConfigItems()) {
				if(itemConfig.itemName.equals(itemName)) {
					configItemsToInitialize.add(itemConfig);
				}
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void allBindingsChanged(BindingProvider provider) {
		if (provider instanceof HomecanBindingProvider) {
			HomecanBindingProvider homecanProvider = (HomecanBindingProvider) provider;
			for (HomecanBindingConfigItem itemConfig : homecanProvider.getAllConfigItems()) {
				configItemsToInitialize.add(itemConfig);
			}
		}
	}
	
	/**
	 * The ItemInitializer runs as a separate thread. Whenever new HomeCAN bindings are added, it takes care that read
	 * requests are sent to all new items, which support this request. By this, the initial status can be
	 * determined and one does not have to stay in an "undefined" state until the first telegram is sent on the HomeCAN bus
	 * for this item. As there might be hundreds of item added at the same time and we do not want to flood
	 * the HomeCAN bus with read requests, we wait a configurable period of milliseconds between two requests. As a result,
	 * this might be quite long running and thus is executed in its own thread.
	 * 
	 * @author Thomas Trathnigg
	 * 
	 */
	private class ConfigItemInitializer extends Thread {
		
		private boolean interrupted = false;
		
		public ConfigItemInitializer() {
			super("HomeCAN item initializer");
		}

		public void setInterrupted(boolean interrupted) {
			this.interrupted = interrupted;
		}

		@Override
		public void run() {
			// as long as no interrupt is requested, continue running
			while (!interrupted && !shutdown) {
				if (configItemsToInitialize.size() > 0) {
					// we first clone the map, so that it stays unmodified
					HashSet<HomecanBindingConfigItem> clonedSet =
						new HashSet<HomecanBindingConfigItem>(configItemsToInitialize);
					initializeItems(clonedSet);
				}
				// just wait before looping again
				try {
					sleep(1000L);
				} catch (InterruptedException e) {
					interrupted = true;
				}
			}
		}

		private void initializeItems(HashSet<HomecanBindingConfigItem> clonedSet) {
			for (HomecanBindingConfigItem configItem : clonedSet) {
				HomecanMsg msg = new HomecanMsg(configItem.homecanID.address,HomecanMsg.MsgType.REQUEST_STATE,configItem.homecanID.channel,null);				
				// send msg
				try {
					synchronized (clientSocket) {						
						clientSocket.send(msg.getPacket(configItem.homecanID.host, HOMECAN_PORT));
						logger.debug("Send HomeCAN Msg = '{}')", msg.toString());
						Thread.sleep(50);
					}

				} catch (IOException e) {
					logger.warn("Item initializer Thread send packet failed " + e);
				} catch (InterruptedException e) {
					logger.warn("Item initializer Thread sending packet was interrupted " + e);
				}
				configItemsToInitialize.remove(configItem);
			}
		}
	}
}
