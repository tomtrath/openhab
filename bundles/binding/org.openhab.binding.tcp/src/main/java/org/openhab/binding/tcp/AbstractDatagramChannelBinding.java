/**
 * openHAB, the open Home Automation Bus.
 * Copyright (C) 2010-2013, openHAB.org <admin@openhab.org>
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with Eclipse (or a modified version of that library),
 * containing parts covered by the terms of the Eclipse Public License
 * (EPL), the licensors of this Program grant you additional permission
 * to convey the resulting work.
 */
package org.openhab.binding.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.openhab.core.binding.AbstractBinding;
import org.openhab.core.binding.BindingProvider;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;
import static org.quartz.DateBuilder.*;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This is the base for all "Datagram" connection-less network based connectivity and communication.It
 * requires a ChannelBindingProvider based binding provider. Data is pushed around using ByteBuffers with an indicator for blocking/non-blocking (synchronous/asynchronous) communication
 * 
 * @author Karel Goderis
 * @since 1.1.0
 * 
 */
public abstract class AbstractDatagramChannelBinding<P extends ChannelBindingProvider> extends  AbstractBinding<P> implements ManagedService {

	private static final Logger logger = LoggerFactory
			.getLogger(AbstractDatagramChannelBinding.class);

	// Configurable parameters
	protected Selector selector;
	// maximum size of buffer whilst reading from a channel
	private static int maximumBufferSize = 1024;
	// cron-style string to define time between reconnects
	private static String reconnectCron = "0 0 0 * * ?";
	// time to wait to attempt a reconnection of an interval, in case of channel failure
	private static int reconnectInterval = 5;
	// default port to listen on for incoming connections
	private static int listenerPort = 25002;
	// share channels between within an item definition
	private static boolean itemShareChannels = true;
	// share channels between items definitions
	private static boolean bindingShareChannels = true;
	// share channels between "outbound" and "inbound" item definitions 
	private static boolean directionsShareChannels = false;
	// allow *:* host:port definitions
	private static boolean useAddressMask = true;

	protected DatagramChannel listenerChannel = null;
	protected SelectionKey listenerKey = null;

	// Queue to store BufferElements that need to be written to the network
	protected  List<WriteBufferElement> writeQueue =  Collections.synchronizedList(new ArrayList<WriteBufferElement>());

	// Simple datastructure to track the state of Channels
	protected ChannelTracker<Channel> channels = new ChannelTracker<Channel>();

	/**
	 * 	Datastructure to represent that state of a communications channel
	 * 
	 * @author Karel Goderis
	 * @since  1.4.0
	 **/
	protected class Channel {

		// the Item that is bound to this channel
		public String item;
		// the Command that is bound to this channel
		public Command command;
		// the resolved remote address this channel is connected to
		public InetSocketAddress remote;
		// the direction, in our out, of this channel
		public Direction direction;
		// flag to indicate if the channel is in a blocking write/read operation
		public boolean isBlocking;
		// placeholder to store the received data as the result of a blocking write/read operation
		public ByteBuffer buffer;
		// flag to indicate if the channel is reconnecting / recovering from a previous communication error
		public boolean isReconnecting;
		// reference to the underlying Java NIO DatagramChannel that represents this UDP/IP connection
		public DatagramChannel channel;
		// remote host name to use. Could be "*" when using masked addresses
		public String host;
		// remote port number to use. Could be "*" when using masked addresses
		public String port;
		// the address of the last remote host:ip that this Channel received data from
		public InetSocketAddress lastRemote;

		public Channel(String item, Command command, InetSocketAddress remote,
				Direction direction, boolean isBlocking, ByteBuffer buffer,
				boolean isReconnecting, DatagramChannel channel) {
			super();
			this.item = item;
			this.command = command;
			this.remote = remote;
			this.direction = direction;
			this.isBlocking = isBlocking;
			this.buffer = buffer;
			this.isReconnecting = isReconnecting;
			this.channel = channel;
			this.host = remote.getHostString();
			this.port = Integer.toString(remote.getPort());
		}

		public Channel(String item, Command command, String host, String port,
				Direction direction, boolean isBlocking, ByteBuffer buffer,
				boolean isReconnecting, DatagramChannel channel) {
			super();
			this.item = item;
			this.command = command;
			this.direction = direction;
			this.isBlocking = isBlocking;
			this.buffer = buffer;
			this.isReconnecting = isReconnecting;
			this.channel = channel;
			this.host = host;
			this.port = port;
		}

		@Override
		public String toString() {
			try {
				String response = null;
				response = "Channel [item=" + item
						+ ", remote=" + remote
						+ ", buffer=";
				if(buffer!=null) {
					response = response + new String(buffer.array()) ;
				}
				response=response		+ ", direction=" + direction 
						+ ", isBlocking=" + isBlocking 
						+ ", command=" + command
						+ ", isReconnecting=" + isReconnecting  ;

				if(channel!=null) {
					response = response + ", channel=";
					try {
						if(channel.getLocalAddress() != null) {
							response = response + channel.getLocalAddress();
						}
					}
					catch (Exception e) {
						response = response + "N/A";

					}
					try {
						if (channel.getRemoteAddress() != null) {
							response = response +"::"+channel.getRemoteAddress();
						}
					}
					catch (Exception e) {
						response = response +"::N/A";

					}
				}

				if(lastRemote!=null) {
					response = response + ", lastremote=";
					response = response + lastRemote.getHostString();
					response = response +"::"+lastRemote.getPort();
				}

				if(useAddressMask) {
					response = response + ", host=" + host
							+ ", port=" + port;
				}

				response = response + "]";
				return response;
			} catch (Exception e) {
				logger.error("An exception occurred while converting Channel to String {}",e.getMessage());
			}
			return "";
		}
	}

	/**
	 * The ChannelTracker acts as a little dB that stores all the information on the state of the
	 * underlying NIO SocketChannels in use. It comes with a bunch of get... methods that allow a caller to 
	 * query Channels. 
	 * 
	 * get() - get the channel that matches the provided criteria for the given {Item,Command}
	 * getFirst() - get the first channel that matches the provided criteria
	 * getFirstServed() - return the first Channel that matches the criteria AND that is currently bound to a Java NIO channel
	 * getAll() - return a collection of all the Channels that match the given criteria
	 * contains() - return true if a channel that matches the provided criteria exists in the ChannelTracker
	 * replace() - replaces the underlying Java NIO channel on the Channels that match the provided criteria 
	 * 
	 * @author Karel Goderis
	 * @since  1.4.0
	 * 
	 **/
	protected class ChannelTracker<C extends Channel> extends ArrayList<C> {

		private static final long serialVersionUID = 1543958347565096785L;

		public boolean contains(String item, Command command, Direction direction, InetSocketAddress remote) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(item.equals(aChannel.item) && command.equals(aChannel.command) && direction.equals(aChannel.direction)&& remote.equals(aChannel.remote)) {
						return true;
					}
				}

				return false;
			}
		}

		public Channel get(String item, Command command, Direction direction, InetSocketAddress remote) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(item.equals(aChannel.item) && command.equals(aChannel.command) && direction.equals(aChannel.direction)&& remote.equals(aChannel.remote)) {
						return aChannel;
					}
				}

				return null;
			}
		}

		public Channel get(String item, Command command, Direction direction, String host, String port) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(item.equals(aChannel.item) && command.equals(aChannel.command) && direction.equals(aChannel.direction)) {
						if(aChannel.host.equals(host) && aChannel.port.equals(port)) {
							return aChannel;
						}
					}
				}

				return null;

			}
		}


		public Channel get(DatagramChannel theDatagramChannel) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(theDatagramChannel.equals(aChannel.channel)) {
						return aChannel;
					}
				}
				return null;
			}
		}

		public Channel getFirst(Direction direction, InetSocketAddress remoteAddress) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(remoteAddress.equals(aChannel.remote) && aChannel.channel == null && direction.equals(aChannel.direction)) {
						return aChannel;
					}
				}

				Iterator<C> it2 = iterator();
				while(it2.hasNext()) {
					C aChannel = it2.next();
					if(remoteAddress.equals(aChannel.remote)) {
						return aChannel;
					}
				}

				return null;
			}
		}

		public Channel getFirst(String itemName, Direction direction, InetSocketAddress remoteAddress) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(itemName.equals(aChannel.item) && remoteAddress.equals(aChannel.remote) && direction.equals(aChannel.direction)) {
						return aChannel;
					}
				}

				return null;
			}
		}

		public Channel getFirstServed(String itemName, Direction direction, InetSocketAddress remoteAddress) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(itemName.equals(aChannel.item) && remoteAddress.equals(aChannel.remote) &&aChannel.channel != null && direction.equals(aChannel.direction)) {
						return aChannel;
					}
				}

				return null;
			}
		}

		public void replace(String itemName, Direction direction, DatagramChannel theDatagramChannel,
				DatagramChannel channel) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(itemName.equals(aChannel.item) &&theDatagramChannel.equals(aChannel.channel) && direction.equals(aChannel.direction)) {
						aChannel.channel = channel;
					}
				}	

			}
		}

		public void replace(String itemName, Direction direction, InetSocketAddress remoteAddress,
				DatagramChannel channel) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(itemName.equals(aChannel.item) &&remoteAddress.equals(aChannel.remote) && direction.equals(aChannel.direction) && !channel.equals(aChannel.channel)) {
						aChannel.channel = channel;
					}
				}
			}

		}

		public ArrayList<Channel> getAll(String itemName, Direction direction, DatagramChannel theDatagramChannel) {
			synchronized(this) {

				ArrayList<Channel> selectedChannels = new ArrayList<Channel>();

				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(itemName.equals(aChannel.item) &&theDatagramChannel.equals(aChannel.channel) && direction.equals(aChannel.direction)) {
						selectedChannels.add(aChannel);
					}
				}

				return selectedChannels;
			}
		}

		public ArrayList<Channel> getAll(Direction direction, InetSocketAddress remote ) {
			synchronized(this) {

				ArrayList<Channel> selectedChannels = new ArrayList<Channel>();

				if(AbstractDatagramChannelBinding.useAddressMask) {
					Iterator<C> it = iterator();
					while(it.hasNext()) {
						C aChannel = it.next();
						if(direction.equals(aChannel.direction)) {
							if(aChannel.host.equals("*") && aChannel.port.equals(Integer.toString(remote.getPort()))) {
								selectedChannels.add(aChannel);
							} else 	if(aChannel.port.equals("*") && aChannel.host.equals(remote.getHostString())) {
								selectedChannels.add(aChannel);
							} else if(aChannel.port.equals("*") && aChannel.host.equals("*")) {
								selectedChannels.add(aChannel);
							}
						}
					}
				} else {
					Iterator<C> it = iterator();
					while(it.hasNext()) {
						C aChannel = it.next();
						if(remote.equals(aChannel.remote) && direction.equals(aChannel.direction)) {
							selectedChannels.add(aChannel);
						}
					}
				}


				return selectedChannels;
			}
		}

		public void setAll(String itemName, Direction direction, DatagramChannel theDatagramChannel, boolean b) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(itemName.equals(aChannel.item) &&theDatagramChannel.equals(aChannel.channel) && direction.equals(aChannel.direction)) {
						aChannel.isBlocking = b;
					}
				}		
			}
		}

		public Channel getFirstServed(InetSocketAddress remoteAddress) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(remoteAddress.equals(aChannel.remote) &&aChannel.channel != null) {
						return aChannel;
					}
				}

				return null;
			}
		}

		public void replace(Direction direction,
				SocketChannel oldSocketChannel, DatagramChannel channel) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(oldSocketChannel.equals(aChannel.channel) && direction.equals(aChannel.direction)) {
						aChannel.channel = channel;
					}
				}		
			}
		}

		public void replace(Direction direction, InetSocketAddress remoteAddress,
				DatagramChannel channel) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(remoteAddress.equals(aChannel.remote) && direction.equals(aChannel.direction) && !channel.equals(aChannel.channel)) {
						aChannel.channel = channel;
					}
				}		
			}
		}

		public ArrayList<Channel> getAll(Direction direction,
				DatagramChannel theDatagramChannel) {
			synchronized(this) {
				ArrayList<Channel> selectedChannels = new ArrayList<Channel>();

				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(theDatagramChannel.equals(aChannel.channel) && direction.equals(aChannel.direction)) {
						selectedChannels.add(aChannel);
					}
				}

				return selectedChannels;	
			}
		}

		public void setAllBlocking(Direction direction, DatagramChannel theDatagramChannel,
				boolean b) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(theDatagramChannel.equals(aChannel.channel) && direction.equals(aChannel.direction)) {
						aChannel.isBlocking = true;
					}
				}	
			}
		}

		public Channel getFirstServed(Direction direction,
				InetSocketAddress remoteAddress) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(remoteAddress.equals(aChannel.remote) &&aChannel.channel != null && direction.equals(aChannel.direction)) {
						return aChannel;
					}
				}

				return null;
			}
		}

		public void replace(DatagramChannel oldDatagramChannel,
				DatagramChannel channel) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(oldDatagramChannel.equals(aChannel.channel)) {
						aChannel.channel = channel;
					}
				}	

			}			
		}

		public void replace(InetSocketAddress remoteAddress, DatagramChannel channel) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(remoteAddress.equals(aChannel.remote) && !channel.equals(aChannel.channel)) {
						aChannel.channel = channel;
					}
				}		
			}

		}

		public ArrayList<Channel> getAll(DatagramChannel theDatagramChannel) {
			synchronized(this) {
				ArrayList<Channel> selectedChannels = new ArrayList<Channel>();

				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(theDatagramChannel.equals(aChannel.channel)) {
						selectedChannels.add(aChannel);
					}
				}

				return selectedChannels;	
			}
		}

		public void setAllBlocking(DatagramChannel theDatagramChannel, boolean b) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(theDatagramChannel.equals(aChannel.channel) ) {
						aChannel.isBlocking = b;
					}
				}	
			}			
		}

		public void setAllReconnecting(DatagramChannel theDatagramChannel, boolean b) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(theDatagramChannel.equals(aChannel.channel) ) {
						aChannel.isReconnecting = b;
					}
				}	
			}			
		}

		public Channel getFirstNotServed(Direction direction,
				InetSocketAddress remoteAddress) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(remoteAddress.equals(aChannel.remote) && direction.equals(aChannel.direction) && (aChannel.channel == null || !aChannel.channel.isOpen()) ) {
						return aChannel;
					}
				}

				return null;
			}
		}

		public ArrayList<Channel> getAll(Direction direction,
				DatagramChannel theDatagramChannel,
				InetSocketAddress clientAddress) {
			synchronized(this) {

				ArrayList<Channel> selectedChannels = new ArrayList<Channel>();

				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(clientAddress.equals(aChannel.remote) &&theDatagramChannel.equals(aChannel.channel) && direction.equals(aChannel.direction)) {
						selectedChannels.add(aChannel);
					}
				}

				return selectedChannels;
			}
		}

		public boolean isBlocking(DatagramChannel theDatagramChannel) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(theDatagramChannel.equals(aChannel.channel) && aChannel.isBlocking) {
						return true;
					}
				}
				return false;
			}
		}

		public Channel getBlocking(DatagramChannel theDatagramChannel) {
			synchronized(this) {
				Iterator<C> it = iterator();
				while(it.hasNext()) {
					C aChannel = it.next();
					if(theDatagramChannel.equals(aChannel.channel) && aChannel.isBlocking) {
						return aChannel;
					}
				}
				return null;
			}
		}

	}

	/**
	 * Simple helper class to store data that needs to be sent over a given channel
	 * 
	 * 
	 * @author Karel Goderis
	 * @since  1.4.0
	 * 
	 **/
	protected class WriteBufferElement {
		public Channel channel;
		public ByteBuffer buffer;
		public boolean isBlocking;

		public WriteBufferElement(Channel channel, ByteBuffer buffer, boolean isBlocking) {
			super();
			this.channel = channel;
			this.buffer = buffer;
			this.isBlocking = isBlocking;
		}

		@Override
		public String toString() {
			String response = null;

			response =  "WriteBufferElement [Channel=";

			if(channel!=null) {
				response = response + channel.toString();
			}

			response = response 
					+ ", buffer=" + new String(buffer.array()) 
			+ ", isblocking=" + isBlocking + "]";
			return response;
		}
	}

	/**
	 * Instantiates a new abstract channel event subscriber binding.
	 */
	public AbstractDatagramChannelBinding() {
	}


	/**
	 * Activate.
	 */
	public void activate() {

		// Start the Quartz job
		Scheduler scheduler = null;
		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
		} catch (SchedulerException e1) {
			logger.error("An exception occurred while getting the Quartz scheduler: {}",e1.getMessage());
		}

		// Start the initial Selector job
		JobDataMap map = new JobDataMap();
		map.put("Binding", this);

		JobDetail job = newJob(SelectorJob.class)
				.withIdentity(Integer.toHexString(hashCode()) +"-Select-"+Long.toString(System.currentTimeMillis()), "AbstractDatagramChannelBinding")
				.usingJobData(map)
				.build();

		Trigger trigger = newTrigger()
				.withIdentity(Integer.toHexString(hashCode()) +"-Select-"+Long.toString(System.currentTimeMillis()), "AbstractDatagramChannelBinding")
				.startNow()             
				.build();

		try {
			scheduler.getListenerManager().addJobListener(new SelectorJobListener(), KeyMatcher.keyEquals(job.getKey()));
		} catch (SchedulerException e1) {
			logger.error("An exception occurred while getting a Quartz Listener Manager: {}",e1.getMessage());
		}

		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			logger.error("An exception occurred while scheduling a finish job with the Quartz Scheduler : {}",e.getMessage());
		}

		//register the selectors
		try {
			selector = Selector.open();
		} catch (IOException e) {
			logger.error("An exception occurred while registering the selector: {}",e.getMessage());
		}

		// open the listener port
		try {
			listenerChannel = DatagramChannel.open();
			listenerChannel.socket().bind(new InetSocketAddress(listenerPort));
			listenerChannel.configureBlocking(false);

			logger.info("Listening for incoming connections on {}",listenerChannel.getLocalAddress());

			synchronized(selector) {
				selector.wakeup();
				try {
					listenerKey = listenerChannel.register(selector, listenerChannel.validOps());
				} catch (ClosedChannelException e1) {
					logger.error("An exception occurred while registering a selector: {}",e1.getMessage());
				}
			}			
		} catch (Exception e3) {
			logger.error("An exception occurred while creating the Listener Channel on port number {} ({})",listenerPort,e3.getMessage());
		}

	}

	/**
	 * Deactivate.
	 */
	public void deactivate() {

		try {
			selector.close();
		} catch (IOException e) {
			logger.error("An exception occurred while closing the selector: {}",e.getMessage());
		}

		try {
			listenerChannel.close();
		} catch (IOException e) {
			logger.error("An exception occurred while closing the Listener Channel on port number {} ({})",listenerPort,e.getMessage());
		}
	}

	/**
	 * Find the first matching {@link ChannelBindingProvider}
	 * according to <code>itemName</code>
	 * 
	 * @param itemName
	 * 
	 * @return the matching binding provider or <code>null</code> if no binding
	 *         provider could be found
	 */
	protected P findFirstMatchingBindingProvider(String itemName) {
		P firstMatchingProvider = null;
		for (P provider : this.providers) {
			if(!useAddressMask) {
				List<InetSocketAddress> socketAddresses = provider
						.getInetSocketAddresses(itemName);
				if (socketAddresses != null && socketAddresses.size() > 0) {
					firstMatchingProvider = provider;
					break;
				}
			} else {
				List<Command> commands = provider.getAllCommands(itemName);
				if (commands != null && commands.size() > 0) {
					firstMatchingProvider = provider;
					break;
				}
			}
		}
		return firstMatchingProvider;
	}

	/**
	 * {@inheritDoc}
	 */
	public void allBindingsChanged(BindingProvider provider) {
		Collection<String> providerItems = provider.getItemNames();

		for (String anItem : providerItems) {
			bindingChanged(provider, anItem);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void bindingChanged(BindingProvider provider, String itemName) {

		for(Command aCommand : ((P) provider).getAllCommands(itemName)) {

			String remoteHost = ((P)provider).getHost(itemName, aCommand);
			String remotePort = ((P)provider).getPortAsString(itemName, aCommand);
			Direction direction = ((P)provider).getDirection(itemName, aCommand);

			InetSocketAddress remoteAddress = null;
			if(!(remoteHost.equals("*") || remotePort.equals("*"))) {
				remoteAddress = new InetSocketAddress(remoteHost,Integer.parseInt(remotePort));
			}

			Channel newChannel = null;
			Channel existingChannel = null;

			if(useAddressMask && (remoteHost.equals("*") || remotePort.equals("*"))) {
				newChannel = new Channel(itemName, aCommand, remoteHost, remotePort,((P)provider).getDirection(itemName, aCommand), false, null, false, null);
				existingChannel = channels.get(itemName, aCommand, direction, remoteHost, remotePort);
			} else {
				newChannel = new Channel(itemName, aCommand, remoteAddress , ((P)provider).getDirection(itemName, aCommand), false, null, false, null);
				existingChannel = channels.get(itemName, aCommand, direction, remoteAddress);
			}


			if ( direction == Direction.IN) {

				if(existingChannel == null) {

					boolean assigned = false;

					if(useAddressMask && (remoteHost.equals("*") || remotePort.equals("*"))) {
						logger.warn("When using address masks we will not verify if we are already listening to similar incoming connections");
					}
					else {
						if(channels.contains(itemName,aCommand,Direction.IN,remoteAddress)) {
							logger.warn("We already listen for incoming connections from {} for the given Item/Command",remoteAddress);
						} else {

							if(itemShareChannels) {
								Channel firstChannel = channels.getFirstServed(itemName, direction,remoteAddress);
								if(firstChannel != null) {
									newChannel.channel = firstChannel.channel;
									assigned = true;
								}
							} 

							if(bindingShareChannels) {
								Channel firstChannel = channels.getFirstServed(direction,remoteAddress);
								if(firstChannel != null) {
									newChannel.channel = firstChannel.channel;
									assigned = true;
								}					
							}

							if(directionsShareChannels) {
								Channel firstChannel = channels.getFirstServed(remoteAddress);
								if(firstChannel != null) {
									newChannel.channel = firstChannel.channel;
									assigned = true;
								}					
							}
						}
					}

					if(!assigned) {
						newChannel.channel = listenerChannel;
					}

					if(useAddressMask && (remoteHost.equals("*") || remotePort.equals("*"))) {
						logger.info("We will accept data coming from the remote end with mask {}:{}",remoteHost,remotePort);
					}  else {
						logger.info("We will accept data coming from the remote end {}",remoteAddress);
					}
					logger.debug("Adding {} to the list of channels",newChannel);
					channels.add(newChannel);
				}


			} else if ( direction == Direction.OUT  ) {

				if(useAddressMask && (remoteHost.equals("*") || remotePort.equals("*")) ) {
					logger.error("We do not accept outgoing connections for Items that do use address masks");
				} else {

					if(existingChannel == null) {
						existingChannel = newChannel;
						channels.add(newChannel);
						logger.debug("Adding {} to the list of channels",newChannel);
					}

					if(existingChannel.channel==null) {
						boolean assigned = false;
						if(itemShareChannels) {
							Channel firstChannel = channels.getFirstServed(itemName, direction,remoteAddress);
							if(firstChannel != null) {
								newChannel.channel = firstChannel.channel;
								assigned = true;
							}
						} 

						if(bindingShareChannels) {
							Channel firstChannel = channels.getFirstServed(direction,remoteAddress);
							if(firstChannel != null) {
								newChannel.channel = firstChannel.channel;
								assigned = true;
							}					
						}


						// I think it is better not to share incoming connections with outgoing connections (in the case of the 
						// UDP binding)

						//					if(directionsShareChannels) {
						//						Channel firstChannel = channels.getFirstServed(remoteAddress);
						//						if(firstChannel != null) {
						//							newChannel.channel = firstChannel.channel;
						//							assigned = true;
						//						}					
						//					}

						synchronized (this) {

							if(!assigned || newChannel.channel==null) {

								DatagramChannel newDatagramChannel = null;
								try {
									newDatagramChannel = DatagramChannel.open();
								} catch (IOException e2) {
									logger.error("An exception occurred while opening a channel: {}",e2.getMessage());
								}

								try {
									newDatagramChannel.configureBlocking(false);
									//setKeepAlive(true);
								} catch (IOException e) {
									logger.error("An exception occurred while configuring a channel: {}",e.getMessage());
								}

								synchronized(selector) {
									selector.wakeup();
									try {
										newDatagramChannel.register(selector, newDatagramChannel.validOps());
									} catch (ClosedChannelException e1) {
										logger.error("An exception occurred while registering a selector: {}",e1.getMessage());
									}
								}

								newChannel.channel = newDatagramChannel;

								try {
									newDatagramChannel.connect(remoteAddress);
								} catch (IOException e) {
									logger.error("An exception occurred while connecting a channel: {}",e.getMessage());
								}
							} 
							else {
								logger.info("There is already an active channel {} for the remote end {}",existingChannel.channel,existingChannel.remote);
							}
						}
					}
				}
			}
		}
	}




	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void updated(Dictionary config) throws ConfigurationException {

		if (config != null) {
			String bufferString = (String) config.get("buffersize");
			if (StringUtils.isNotBlank(bufferString)) {
				maximumBufferSize = Integer.parseInt((bufferString));
			} else {
				logger.info("The maximum buffer will be set to the default vaulue of {}",maximumBufferSize);
			}

			String reconnectString = (String) config.get("retryinterval");
			if (StringUtils.isNotBlank(reconnectString)) {
				reconnectInterval = Integer.parseInt((reconnectString));
			} else {
				logger.info("The interval to retry connection setups will be set to the default vaulue of {}",reconnectInterval);
			}

			String cronString = (String) config.get("reconnectcron");
			if (StringUtils.isNotBlank(cronString)) {
				reconnectCron = cronString;
			} else {
				logger.info("The cron job to reset connections will be set to the default vaulue of {}",reconnectCron);
			}

			String portString = (String) config.get("port");
			if (StringUtils.isNotBlank(portString)) {
				listenerPort = Integer.parseInt((portString));
			} else {
				logger.info("The port to listen for incoming connections will be set to the default vaulue of {}",listenerPort);
			}

			String shareString = (String) config.get("itemsharedconnections");
			if (StringUtils.isNotBlank(shareString)) {
				itemShareChannels = Boolean.parseBoolean(shareString);
			} else {
				logger.info("The setting to share channels within an Item will be set to the default vaulue of {}",itemShareChannels);
			}

			String shareString2 = (String) config.get("bindingsharedconnections");
			if (StringUtils.isNotBlank(shareString2)) {
				bindingShareChannels = Boolean.parseBoolean(shareString2);
			} else {
				logger.info("The setting to share channels between the items with the same direction will be set to the default vaulue of {}",bindingShareChannels);
			}

			String shareString3 = (String) config.get("directionssharedconnections");
			if (StringUtils.isNotBlank(shareString3)) {
				directionsShareChannels = Boolean.parseBoolean(shareString3);
			} else {
				logger.info("The setting to share channels between directions will be set to the default vaulue of {}",bindingShareChannels);
			}

			String shareString4 = (String) config.get("addressmask");
			if (StringUtils.isNotBlank(shareString4)) {
				useAddressMask = Boolean.parseBoolean(shareString4);
			} else {
				logger.info("The setting to use address masks for incoming connections will be set to the default vaulue of {}",useAddressMask);
			}

			if(useAddressMask && directionsShareChannels) {
				logger.warn("The setting to share channels between directions is not compatible with the setting to use address masks. We will override the setting to share between directions");
				directionsShareChannels = false;
			}		

			if(bindingShareChannels && !itemShareChannels) {
				logger.warn("The setting to share channels in the binding is not compatible with the setting to share channels within items. We will override the setting to share between items");
				itemShareChannels = true;
			}

			if(directionsShareChannels && (!bindingShareChannels || !itemShareChannels)) {
				logger.warn("The setting to share channels between directions is not compatible with the setting to share channels between items or within items. We will override the settings");
				itemShareChannels = true;
				bindingShareChannels = true;
			}

		}

	}

	/**
	 * {@inheritDoc}
	 */
	protected void internalReceiveCommand(String itemName, Command command) {
		P provider = findFirstMatchingBindingProvider(itemName);

		if (provider == null) {
			logger.warn(
					"cannot find matching binding provider [itemName={}, command={}]",
					itemName, command);
			return;
		}

		if(command != null){
			List<Command> commands = provider.getQualifiedCommands(itemName,command);

			for(Command someCommand : commands) {
				Channel theChannel = null;
				if(useAddressMask && (((P)provider).getHost(itemName, someCommand).equals("*") || ((P)provider).getPortAsString(itemName, someCommand).equals("*"))) {
					theChannel = channels.get(itemName, someCommand, ((P)provider).getDirection(itemName, someCommand), ((P)provider).getHost(itemName, someCommand), ((P)provider).getPortAsString(itemName, someCommand));
				} else {
					theChannel = channels.get(itemName, someCommand, ((P)provider).getDirection(itemName, someCommand), new InetSocketAddress(provider.getHost(itemName, someCommand),provider.getPort(itemName, someCommand)));	
				}
				DatagramChannel theDatagramChannel = null;
				if(theChannel != null) {
					theDatagramChannel = theChannel.channel;
				}
				if (theDatagramChannel != null) {

					boolean result = internalReceiveChanneledCommand(itemName, someCommand, theChannel,command.toString());

					if(!theDatagramChannel.isConnected() && theDatagramChannel !=  listenerChannel) {

						logger.warn("The channel for {} has a connection problem. Data will queued to the new channel when it is successfully set up.",theChannel.remote);

						Scheduler scheduler = null;
						try {
							scheduler = StdSchedulerFactory.getDefaultScheduler();
						} catch (SchedulerException e1) {
							logger.error("An exception occurred while getting the Quartz scheduler: {}",e1.getMessage());
						}

						JobDataMap map = new JobDataMap();
						map.put("Channel", theChannel);
						map.put("Binding", this);


						JobDetail job = newJob(ReconnectJob.class)
								.withIdentity(Integer.toHexString(hashCode()) +"-Reconnect-"+Long.toString(System.currentTimeMillis()), "AbstractDatagramChannelBinding")
								.usingJobData(map)
								.build();

						Trigger trigger = newTrigger()
								.withIdentity(Integer.toHexString(hashCode()) +"-Reconnect-"+Long.toString(System.currentTimeMillis()), "AbstractDatagramChannelBinding")
								.startNow()         
								.build();

						try {
							if(job!= null && trigger != null) {
								if(!theChannel.isReconnecting) {
									theChannel.isReconnecting=true;
									scheduler.scheduleJob(job, trigger);
								}
							}
						} catch (SchedulerException e) {
							logger.error("An exception occurred while scheduling a job with the Quartz Scheduler {}",e.getMessage());
						}
					}

					if(result) {
						List<Class<? extends State>> stateTypeList = provider.getAcceptedDataTypes(itemName,someCommand);
						State newState = createStateFromString(stateTypeList,command.toString());

						if(newState != null) {
							eventPublisher.postUpdate(itemName, newState);							        						
						}
					}
				} else {
					logger.error(
							"there is no channel that services [itemName={}, command={}]",
							itemName, command);
				}
			}
		}               
	}

	/**
	 * This function should be  implemented and used to "setup" the
	 * ASCII protocol after that the socket channel has been created. This because some ASCII
	 * protocols need to first emit a certain sequence of characters to
	 * configure or setup the communication channel with the remote end
	 **/
	abstract protected void configureChannel(DatagramChannel channel);

	/**
	 * The actual implementation for receiving a command from the openhab runtime should go here
	 *
	 * @param itemName the item name
	 * @param command the command
	 * @param channel the network channel
	 * @param commandAsString the command as String
	 * @return true, if successful
	 */
	abstract protected boolean internalReceiveChanneledCommand(String itemName,
			Command command, Channel channel, String commandAsString);

	/**
	 * Returns a {@link State} which is inherited from provide list of DataTypes. The call is delegated to the  {@link TypeParser}. If
	 * <code>stateTypeList</code> is <code>null</code> the {@link StringType} is used.
	 *  
	 * @param stateTypeList - a list of state types that we should parse against
	 * @param transformedResponse
	 * 
	 * @return a {@link State} which type is inherited by the {@link TypeParser}
	 * or a {@link StringType} if <code>stateTypeList</code> is <code>null</code> 
	 */
	protected State createStateFromString(List<Class<? extends State>> stateTypeList, String transformedResponse) {
		if (stateTypeList != null) {
			return TypeParser.parseState(stateTypeList, transformedResponse);
		}
		else {
			return StringType.valueOf(transformedResponse);
		}
	}

	/**
	 * Parses the buffer received from the Channel
	 *
	 * @param networkChannel the network channel
	 * @param byteBuffer the byte buffer
	 */
	protected void parseChanneledBuffer(Channel theChannel, ByteBuffer byteBuffer) {
		if(theChannel != null && byteBuffer != null && byteBuffer.limit() != 0) {			
			parseBuffer(theChannel.item, theChannel.command,theChannel.direction,byteBuffer);
		}
	}

	/**
	 * 
	 * Callback that will be called when data is received on a given channel.
	 * This method should deal with the actual details of the protocol being implemented
	 */
	abstract protected void parseBuffer(String itemName, Command aCommand, Direction theDirection,ByteBuffer byteBuffer);

	/**
	 * Queues (writes) a ByteBuffer to a channel
	 *
	 * @param theChannel the network channel
	 * @param byteBuffer the byte buffer
	 * @param isBlockingWriteRead set to true if we have to wait for a response from the remote end (e.g. ACK message or alike)
	 * @param timeOut time to wait for a response from the remote end
	 * @return a ByteBuffer with the response, if blocking operation, or the original ByteBuffer in all other cases
	 */
	protected ByteBuffer writeBuffer(ByteBuffer theBuffer, Channel theChannel, boolean isBlockingWriteRead, long timeOut) {

		if(isBlockingWriteRead) {

			if(theBuffer != null) {
				writeQueue.add(new WriteBufferElement(theChannel,theBuffer,true));

				long currentElapsedTimeMillis = System.currentTimeMillis();

				while(theChannel.buffer==null && (System.currentTimeMillis()-currentElapsedTimeMillis)<timeOut ) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						logger.warn("An Exception occurred while waiting waiting during a blocking buffer write");
					}
				}

				ByteBuffer responseBuffer = null;
				synchronized(this) {
					responseBuffer = theChannel.buffer;
					theChannel.buffer = null;
					theChannel.isBlocking = false;
				}
				return responseBuffer;
			} else {
				return theBuffer;
			}

		} else {

			if(theBuffer != null) {
				writeQueue.add(new WriteBufferElement(theChannel,theBuffer,false));
			}

			return theBuffer;
		}
	}


	/**
	 * Quartz Job to reconnect a channel
	 * 
	 * @author Karel Goderis
	 * @since  1.2.0
	 *
	 */
	public static class ReconnectJob implements Job {

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void execute(JobExecutionContext context)
				throws JobExecutionException {
			JobDataMap dataMap = context.getJobDetail().getJobDataMap();
			AbstractDatagramChannelBinding theBinding = (AbstractDatagramChannelBinding) dataMap.get("Binding");
			AbstractDatagramChannelBinding.Channel theChannel = (AbstractDatagramChannelBinding.Channel) dataMap.get("Channel");

			if(theChannel.isReconnecting) {

				if(theChannel.remote != null ) {
					if(theChannel.channel != theBinding.listenerChannel) {

						SelectionKey sKey = theChannel.channel.keyFor(theBinding.selector);
						if(sKey != null) {
							sKey.cancel();
						}

						try {
							theChannel.channel.close();
						} catch (IOException e) {
							logger.error("An exception occurred while closing a channel: {}",e.getMessage());
						}

						try {
							theChannel.channel = DatagramChannel.open();
						} catch (IOException e) {
							logger.error("An exception occurred while opening a channel: {}",e.getMessage());
						}

						theChannel.isBlocking = false;
						theChannel.buffer = null;

						try {
							theChannel.channel.configureBlocking(false);
							//setKeepAlive(true);
						} catch (Exception e) {
							logger.error("An exception occurred while configuring a channel: {}",e.getMessage());
						}


						synchronized(theBinding.selector) {
							theBinding.selector.wakeup();
							try {
								if(theChannel.channel != null) {
									theChannel.channel.register(theBinding.selector, theChannel.channel.validOps());
								}
							} catch (ClosedChannelException e1) {
								logger.error("An exception occurred while registering a selector: {}",e1.getMessage());
							}
						}

						try {
							if(theChannel.channel != null ) {

								if(AbstractDatagramChannelBinding.itemShareChannels) {
									theBinding.channels.replace(theChannel.item, theChannel.direction, theChannel.remote, theChannel.channel);		
								}

								if(AbstractDatagramChannelBinding.bindingShareChannels) {
									theBinding.channels.replace(theChannel.direction, theChannel.remote, theChannel.channel);		
								}

								if(AbstractDatagramChannelBinding.directionsShareChannels) {
									theBinding.channels.replace(theChannel.remote, theChannel.channel);		
								}

								theChannel.isBlocking = false;
								theBinding.channels.setAllReconnecting(theChannel.channel,false);

								theChannel.channel.connect(theChannel.remote);
								logger.info("Attempting to reconnect the channel for {}",theChannel.remote);
							}
						} catch (Exception e) {
							logger.error("An exception occurred while connecting a channel: {}",e.getMessage());
						}
					}
					else {
						logger.warn("The listener channel can not be closed!");
					}
				} else {
					logger.debug("I cannot proceed without remote address");
				}
			} else {
				logger.warn("Already reconnecting the channel for {}",theChannel.remote);
			}
		}
	}

	/**
	 * Quartz Job to process SelectKeys for all DatagramChannels
	 * 
	 * @author Karel Goderis
	 * @since  1.3.0
	 *
	 */
	public static class SelectorJob implements Job {

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void execute(JobExecutionContext context)
				throws JobExecutionException {

			JobDataMap dataMap = context.getJobDetail().getJobDataMap();
			AbstractDatagramChannelBinding theBinding = (AbstractDatagramChannelBinding) dataMap.get("Binding");

			boolean jobDone = false;

			while(!jobDone) {

				synchronized(theBinding.selector) {
					try {
						// Wait for an event
						theBinding.selector.selectNow();
					} catch (IOException e) {
						// Handle error with selector
					}
				}

				// Get list of selection keys with pending events
				Iterator<SelectionKey> it = theBinding.selector.selectedKeys().iterator();

				// Process each key at a time
				while (it.hasNext()) {
					SelectionKey selKey = (SelectionKey) it.next();
					it.remove();

					if (selKey.isValid()) {
						DatagramChannel theDatagramChannel = (DatagramChannel) selKey.channel();
						AbstractDatagramChannelBinding.Channel theChannel = theBinding.channels.get(theDatagramChannel);

						if (selKey.isReadable()) {
							InetSocketAddress clientAddress = null;
							ByteBuffer readBuffer = ByteBuffer.allocate(maximumBufferSize);
							int numberBytesRead = 0;
							boolean error = false;

							if(selKey == theBinding.listenerKey) {
								try {
									clientAddress = (InetSocketAddress) theDatagramChannel.receive(readBuffer);
									logger.debug("Received {} on the listener port from {}",new String(readBuffer.array()),clientAddress);
									numberBytesRead = readBuffer.position();
								} catch (Exception e) {
									error=true;
								}

							} else {

								try {
									//TODO: Additional code to split readBuffer in multiple parts, in case the data send by the remote end is not correctly fragemented. Could be handed of to implementation class if for example, the buffer needs to be split based on a special character like line feed or carriage return
									numberBytesRead = theDatagramChannel.read(readBuffer);
									logger.debug("Received {} bytes ({}) on the channel {}->{}", new Object[]{numberBytesRead,new String(readBuffer.array()),theDatagramChannel.getLocalAddress(),theDatagramChannel.getRemoteAddress()});
								} catch (NotYetConnectedException e) {
									try {
										logger.warn("The channel for {} has no connection pending ({})",theDatagramChannel.getRemoteAddress(),e.getMessage());
									} catch (IOException e1) {
										logger.error("An exception occurred while getting the remote address of channel {} ({})",theDatagramChannel,e1.getMessage());
									}
									error=true;
								} catch (IOException e) {
									// If some other I/O error occurs
									try {
										logger.warn("The channel for {} has encountered an unknown IO Exception: {}",theDatagramChannel.getRemoteAddress(),e.getMessage());
									} catch (IOException e1) {
										logger.error("An exception occurred while getting the remote address of channel {} ({})",theDatagramChannel,e1.getMessage());
									}
									error=true;
								}
							}

							if(numberBytesRead == -1) {
								try {
									if(selKey != theBinding.listenerKey) {
										theDatagramChannel.close();
									}
								} catch (IOException e) {
									try {
										logger.warn("The channel for {} is closed ({})",theDatagramChannel.getRemoteAddress(),e.getMessage());
									} catch (IOException e1) {
										logger.error("An exception occurred while getting the remote address of channel {} ({})",theDatagramChannel,e1.getMessage());
									}
								}
								error = true;
							}

							if(error) {
								if(selKey != theBinding.listenerKey) {

									Scheduler scheduler = null;
									try {
										scheduler = StdSchedulerFactory.getDefaultScheduler();
									} catch (SchedulerException e1) {
										logger.error("An exception occurred while getting the Quartz scheduler: {}",e1.getMessage());
									}

									JobDataMap map = new JobDataMap();
									map.put("Channel", theChannel);
									map.put("Binding", theBinding);

									JobDetail job = null;
									Trigger trigger = null;	

									job = newJob(ReconnectJob.class)
											.withIdentity(Integer.toHexString(hashCode()) +"-Reconnect-"+Long.toString(System.currentTimeMillis()), "AbstractDatagramChannelBinding")
											.usingJobData(map)
											.build();

									trigger = newTrigger()
											.withIdentity(Integer.toHexString(hashCode()) +"-Reconnect-"+Long.toString(System.currentTimeMillis()), "AbstractDatagramChannelBinding")
											.startAt(futureDate(reconnectInterval, IntervalUnit.SECOND))         
											.build();

									try {
										if(job!= null && trigger != null && selKey!=theBinding.listenerKey) {
											if(!theChannel.isReconnecting) {
												theBinding.channels.setAllReconnecting(theDatagramChannel,true);
												scheduler.scheduleJob(job, trigger);
											}
										}
									} catch (SchedulerException e) {
										logger.error("An exception occurred while scheduling a job with the Quartz Scheduler {}",e.getMessage());
									}	
								}

							} else {

								ArrayList<AbstractDatagramChannelBinding.Channel> channelsToServe = new ArrayList<AbstractDatagramChannelBinding.Channel>();

								if(selKey == theBinding.listenerKey) {
									channelsToServe = theBinding.channels.getAll(Direction.IN,clientAddress);
									if(channelsToServe.size()==0) {
										logger.warn("Received data {} from an undefined remote end {}. We will not process it",new String(readBuffer.array()),clientAddress );
									}
								} else {
									channelsToServe = theBinding.channels.getAll(theDatagramChannel);
								}


								if(channelsToServe.size() >0) {

									readBuffer.flip();

									if(theBinding.channels.isBlocking(theDatagramChannel)) {
										// if we are in a blocking operation, we get are now finished and we have to reset the flag. The read buffer will be returned to the instance
										// that initiated the write opreation - it has to parse the buffer itself

										//find the Channel with this DGC that is holding a Blocking flag
										theChannel = theBinding.channels.getBlocking(theDatagramChannel);
										theChannel.buffer = readBuffer;

									} else {
										for(AbstractDatagramChannelBinding.Channel aChannel : channelsToServe) {
											if(AbstractDatagramChannelBinding.useAddressMask) {
												aChannel.lastRemote = clientAddress;
											}
											// if not, then we parse the buffer as ususal
											logger.debug("Parsing the received buffer {} for channel {}",new String(readBuffer.array()),aChannel);
											theBinding.parseChanneledBuffer(aChannel,readBuffer);
										}
									}
								} else {
									try {
										if(selKey == theBinding.listenerKey) {
											logger.warn("No channel is active or defined for the data we received from {}. It will be discarded.",clientAddress);											
										} else {
											logger.warn("No channel is active or defined for the data we received from {}. It will be discarded.",theDatagramChannel.getRemoteAddress());
										}
									} catch (IOException e) {
										logger.error("An exception occurred while getting the remote address of channel {} ({})",theDatagramChannel,e.getMessage());
									}
								}
							}
						} else if (selKey.isWritable()) {

							AbstractDatagramChannelBinding.WriteBufferElement theElement = null;

							if(selKey == theBinding.listenerKey) {
								Iterator<AbstractDatagramChannelBinding.WriteBufferElement> iterator = theBinding.writeQueue.iterator();
								while (iterator.hasNext()) {
									AbstractDatagramChannelBinding.WriteBufferElement anElement = iterator.next();
									if(anElement.channel.channel.equals(theBinding.listenerChannel)) {
										theElement = anElement;
										break;
									}
								}
							}

							//check if any of the Channel using the DatagramChannel is blocking the DGC in a R/W operation
							boolean isBlocking = theBinding.channels.isBlocking(theDatagramChannel);

							if(isBlocking) {
								// if this channel is already flagged as being in a blocked write/read operation, we skip this selKey
							} else { 

								if(selKey != theBinding.listenerKey) {									
									Iterator<AbstractDatagramChannelBinding.WriteBufferElement> iterator = theBinding.writeQueue.iterator();
									while (iterator.hasNext()) {
										AbstractDatagramChannelBinding.WriteBufferElement anElement = iterator.next();
										if(anElement.channel.channel.equals(theDatagramChannel)) {
											theElement = anElement;
											break;
										}
									}
								}


								if(theElement != null && theElement.buffer != null) {

									logger.debug("Picked {} from the queue",theElement);

									if(theElement.isBlocking) {
										theElement.channel.isBlocking = true;
									}

									boolean error=false;

									theElement.buffer.rewind();

									if(selKey == theBinding.listenerKey) {
										try {
											if(AbstractDatagramChannelBinding.useAddressMask && theElement.channel.remote==null) {
												if(theElement.channel.lastRemote!=null) {
													logger.debug("Sending {} for the masked inbound channel {}:{} to the remote address {}", new Object[]{new String(theElement.buffer.array()),theElement.channel.host,theElement.channel.port,theElement.channel.lastRemote});
													theBinding.listenerChannel.send(theElement.buffer, theElement.channel.lastRemote);
												} else {
													logger.warn("I do not know where to send the data {}",new String(theElement.buffer.array()));
												}
											} else {
												logger.debug("Sending {} for the inbound channel {}:{} to the remote address {}", new Object[]{new String(theElement.buffer.array()),theElement.channel.host,theElement.channel.port,theElement.channel.remote});
												theBinding.listenerChannel.send(theElement.buffer, theElement.channel.remote);
											}
										} catch (IOException e) {
											if(theElement.channel.lastRemote!=null) {
												logger.error("An exception occurred while sending data to the remote end {} ({})",theElement.channel.lastRemote,e.getMessage());
											} else {
												logger.error("An exception occurred while sending data to the remote end {} ({})",theElement.channel.remote,e.getMessage());												
											}
										}
									} else {

										try {
											logger.debug("Sending {} for the outbound channel {}:{} to the remote address {}", new Object[]{new String(theElement.buffer.array()),theElement.channel.host,theElement.channel.port,theElement.channel.remote});
											theDatagramChannel.write(theElement.buffer);
										} catch (NotYetConnectedException e) {
											logger.warn("The channel for {} has no connection pending ({})",theElement.channel.remote,e.getMessage());
											error=true;
										} catch (ClosedChannelException e) {
											// If some other I/O error occurs
											logger.warn("The channel for {} is closed ({})",theElement.channel.remote,e.getMessage());
											error=true;
										} catch (IOException e) {
											// If some other I/O error occurs
											logger.warn("The channel for {} has encountered an unknown IO Exception: {}",theElement.channel.remote,e.getMessage());
											error=true;
										}
									}

									if(error) {

										if(selKey != theBinding.listenerKey) {

											Scheduler scheduler = null;
											try {
												scheduler = StdSchedulerFactory.getDefaultScheduler();
											} catch (SchedulerException e1) {
												logger.error("An exception occurred while getting the Quartz scheduler: {}",e1.getMessage());
											}

											JobDataMap map = new JobDataMap();
											map.put("Channel", theElement.channel);
											map.put("Binding", theBinding);

											JobDetail job = null;
											Trigger trigger = null;	

											job = newJob(ReconnectJob.class)
													.withIdentity(Integer.toHexString(hashCode()) +"-Reconnect-"+Long.toString(System.currentTimeMillis()), "AbstractDatagramChannelBinding")
													.usingJobData(map)
													.build();

											trigger = newTrigger()
													.withIdentity(Integer.toHexString(hashCode()) +"-Reconnect-"+Long.toString(System.currentTimeMillis()), "AbstractDatagramChannelBinding")
													.startAt(futureDate(reconnectInterval, IntervalUnit.SECOND))         
													.build();

											try {
												if(job!= null && trigger != null && selKey!=theBinding.listenerKey) {
													if(!theElement.channel.isReconnecting) {
														theBinding.channels.setAllReconnecting(theElement.channel.channel,true);
														scheduler.scheduleJob(job, trigger);
													}
												}
											} catch (SchedulerException e) {
												logger.error("An exception occurred while scheduling a job with the Quartz Scheduler {}",e.getMessage());
											}
										}
									} else {
										if(theElement != null ) {
											theBinding.writeQueue.remove(theElement);
										}

									}
								}
							}
						}
					}
				}
			}

			jobDone = true;

		}
	}


	/**
	 * Quartz Job Listener that will schedule a new SelectorJob when the previous one is "done"
	 * 
	 * @author Karel Goderis
	 * @since  1.3.0
	 *
	 */
	public static class SelectorJobListener implements JobListener {

		public SelectorJobListener() {
		}

		public void jobToBeExecuted(JobExecutionContext context) {
			// do something with the event
		}

		public void jobWasExecuted(JobExecutionContext context,
				JobExecutionException jobException) {

			Scheduler scheduler = null;
			try {
				scheduler = StdSchedulerFactory.getDefaultScheduler();
			} catch (SchedulerException e1) {
				logger.error("An exception occurred while getting the Quartz scheduler: {}",e1.getMessage());
			}

			JobDataMap dataMap = context.getJobDetail().getJobDataMap();
			@SuppressWarnings("rawtypes")
			AbstractDatagramChannelBinding theBinding = (AbstractDatagramChannelBinding) dataMap.get("Binding");

			JobDataMap map = new JobDataMap();
			map.put("Binding", theBinding);

			JobDetail job = newJob(SelectorJob.class)
					.withIdentity(Integer.toHexString(hashCode()) +"-Select-"+Long.toString(System.currentTimeMillis()), "AbstractDatagramChannelBinding")
					.usingJobData(map)
					.build();

			Trigger trigger = newTrigger()
					.withIdentity(Integer.toHexString(hashCode()) +"-Select-"+Long.toString(System.currentTimeMillis()), "AbstractDatagramChannelBinding")
					.startNow()         
					.build();

			try {
				scheduler.getListenerManager().addJobListener(new SelectorJobListener(), KeyMatcher.keyEquals(job.getKey()));
			} catch (SchedulerException e1) {
				logger.error("An exception occurred while getting a Quartz Listener Manager: {}",e1.getMessage());
			}

			try {
				scheduler.scheduleJob(job, trigger);
			} catch (SchedulerException e) {
				logger.error("Error scheduling a read job with the Quartz Scheduler");
			}		   
		}

		public void jobExecutionVetoed(JobExecutionContext context) {
			// do something with the event
		}

		@Override
		public String getName() {
			return "DatagramSelectorJobListener";
		}
	}

}





