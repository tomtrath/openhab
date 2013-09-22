package org.openhab.binding.homecan;

import org.openhab.core.types.Type;

/**
 * This is an internal data structure to store information from the binding
 * config strings and use it to answer the requests to the Homecan binding
 * provider.
 * 
 * @author Thomas Trathnigg
 * 
 */
public class HomecanBindingConfigItem {
	public String itemName;		
	public Class<? extends Type> typeClass;
	public Direction direction;
	public HomecanID homecanID;		
}