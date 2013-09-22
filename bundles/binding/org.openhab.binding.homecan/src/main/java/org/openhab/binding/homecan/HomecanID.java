package org.openhab.binding.homecan;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.openhab.binding.homecan.HomecanMsg.MsgType;

public class HomecanID {		
	InetAddress host;
	byte address;
	byte channel;		
	MsgType msgtype;
	
	public HomecanID(String host, String address, String channel,String msgtype) throws UnknownHostException {
		this.host = InetAddress.getByName(host);
		this.address = Integer.decode(address).byteValue();
		this.channel = Integer.decode(channel).byteValue();
		this.msgtype = MsgType.valueOf(msgtype);	
	}
	
	public HomecanID(InetAddress host, byte address, byte channel, MsgType msgtype) {
		this.host = host;
		this.address = address;
		this.channel = channel;		
		this.msgtype = msgtype;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		HomecanID id = (HomecanID)obj;
		return host.equals(id.host) && address==id.address && channel==id.channel && msgtype==id.msgtype;
	}
	
	
}
