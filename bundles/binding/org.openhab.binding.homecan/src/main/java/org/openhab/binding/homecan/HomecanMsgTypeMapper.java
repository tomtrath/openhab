package org.openhab.binding.homecan;

import org.openhab.core.types.Type;

public interface HomecanMsgTypeMapper {

	/**
	 * maps an openHAB command/state to a homecan msg 
	 *  
	 * @param type a command or state
	 * @param homecan  
	 * @return data in homecan msg format
	 */
	public byte[] toHomecanMsgData(Type type, HomecanMsg.MsgType msgtype);

	/**
	 * converts a HomecanMsg to an openHAB command or state
	 * 
	 * @param msg the source homecan msg 
	 * @param typeClass the class of the openhab command or state
	 * @return a command or state of openHAB
	 */
	public Type toType(HomecanMsg msg, Class<? extends Type> typeClass);

}
