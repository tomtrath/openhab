package org.openhab.binding.homecan;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openhab.core.autoupdate.AutoUpdateBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.types.Type;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomecanGenericBindingProvider extends AbstractGenericBindingProvider implements HomecanBindingProvider, AutoUpdateBindingProvider {

	static final Logger logger = LoggerFactory.getLogger(HomecanGenericBindingProvider.class);
	
	/** {@link Pattern} which matches a binding configuration part 
	 * 
	 * direction[hostname:address:channel:msgtype]
	 */
	private static final Pattern BASE_CONFIG_PATTERN = Pattern.compile("([<|>|\\*]\\[.*?\\])*");
	private static final Pattern CONFIG_PATTERN = Pattern.compile("(<|>|\\*)\\[(.*?):(.*?):(.*?):(.*?)\\]");
		
	@Override
	public String getBindingType() {
		return "homecan";
	}

	@Override
	public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
		// / all types of items are valid ...
	}

	@Override
	public Boolean autoUpdate(String itemName) {
		return false;
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public void processBindingConfiguration(String context, Item item, String bindingConfig) throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);
		if (bindingConfig != null) {
			parseAndAddBindingConfig(item, bindingConfig);
		} else {
			logger.warn(getBindingType() + " bindingConfig is NULL (item=" + item + ") -> processing bindingConfig aborted!");
		}
	}

	private void parseAndAddBindingConfig(Item item, String bindingConfig) throws BindingConfigParseException {
		HomecanBindingConfig newConfig = new HomecanBindingConfig();
		Matcher matcher = BASE_CONFIG_PATTERN.matcher(bindingConfig);

		if (!matcher.matches()) {
			throw new BindingConfigParseException("bindingConfig '" + bindingConfig + "' doesn't contain a valid binding configuration");
		}
		matcher.reset();
		int i = 0;
		while (matcher.find()) {
			String bindingConfigPart = matcher.group(1);
			if (StringUtils.isNotBlank(bindingConfigPart)) {
				Class<? extends Type> typeClass = item.getAcceptedCommandTypes().size() > 0 ? item.getAcceptedCommandTypes().get(i) : item
						.getAcceptedDataTypes().size() > 1 ? item.getAcceptedDataTypes().get(i) : item.getAcceptedDataTypes().get(0);
				i++;
				parseBindingConfig(newConfig, item, bindingConfigPart, typeClass);
				addBindingConfig(item, newConfig);
			}
		}
	}

	private void parseBindingConfig(HomecanBindingConfig config, Item item, String bindingConfig,Class<? extends Type> typeClass) throws BindingConfigParseException {
		HomecanBindingConfigItem configItem = new HomecanBindingConfigItem();
		if(bindingConfig != null){
			Matcher matcher = CONFIG_PATTERN.matcher(bindingConfig);
			if (!matcher.matches()) {
				throw new BindingConfigParseException(getBindingType()+
						" binding configuration mismatch, expected [config="+matcher+"]");
			} else {
				//direction[hostname:address:channel:msgtype]				
				String direction = matcher.group(1);
				if (direction.equals(">")){
					configItem.direction = Direction.OUT;
				} else if (direction.equals("<")){
					configItem.direction = Direction.IN;
				} else if (direction.equals("*")){
					configItem.direction = Direction.BIDIRECTIONAL;
				} 							
				try {
					configItem.homecanID = new HomecanID(matcher.group(2),matcher.group(3),matcher.group(4),matcher.group(5));
					configItem.itemName = item.getName();	
					configItem.typeClass = typeClass;
					config.add(configItem);
				} catch (UnknownHostException e) {
					throw new BindingConfigParseException(getBindingType()+
							" binding configuration error, UnknownHostException occured [config="+matcher+"]");
				}
				
			}
		}
	}		
	
	
	public Collection<HomecanBindingConfigItem> getListeningConfigItems(final HomecanID homecanID) {
		synchronized(bindingConfigs) {
			List<HomecanBindingConfigItem> items = new ArrayList<HomecanBindingConfigItem>();		
			for (String itemName : bindingConfigs.keySet()) {
				HomecanBindingConfig aConfig = (HomecanBindingConfig) bindingConfigs.get(itemName);			
				for(HomecanBindingConfigItem anElement : aConfig) {					
					if (anElement.homecanID.equals(homecanID)) {
						if(!items.contains(anElement)) {
							items.add(anElement);
						}
					}			
				}
			}
			return items;
		}
	}
	
	public Collection<HomecanBindingConfigItem> getListeningConfigItems(final String itemName, final Class<? extends Type> typeClass) {
		synchronized(bindingConfigs) {
			List<HomecanBindingConfigItem> items = new ArrayList<HomecanBindingConfigItem>();		
			for (String aItemName : bindingConfigs.keySet()) {
				HomecanBindingConfig aConfig = (HomecanBindingConfig) bindingConfigs.get(aItemName);			
				for(HomecanBindingConfigItem anElement : aConfig) {					
					if (anElement.typeClass.equals(typeClass) && anElement.itemName.equals(itemName)) {
						if(!items.contains(anElement)) {
							items.add(anElement);
						}
					}			
				}
			}
			return items;
		}
	}	
	
	public Collection<HomecanBindingConfigItem> getAllConfigItems() {
		synchronized(bindingConfigs) {
			List<HomecanBindingConfigItem> items = new ArrayList<HomecanBindingConfigItem>();		
			for (String itemName : bindingConfigs.keySet()) {
				HomecanBindingConfig aConfig = (HomecanBindingConfig) bindingConfigs.get(itemName);			
				for(HomecanBindingConfigItem anElement : aConfig) {					
					if(!items.contains(anElement)) {
						items.add(anElement);				
					}			
				}
			}
			return items;
		}
	}

	/**
	 * This is an internal container to gather all config items for one opeHAB
	 * item.
	 * 
	 * @author Thomas Trathnigg
	 * 
	 */
	@SuppressWarnings("serial")
	static class HomecanBindingConfig extends LinkedList<HomecanBindingConfigItem> implements BindingConfig {
	}

}
