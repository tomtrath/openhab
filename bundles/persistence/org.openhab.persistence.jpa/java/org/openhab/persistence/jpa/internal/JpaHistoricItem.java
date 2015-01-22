/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.persistence.jpa.internal;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.openhab.core.items.Item;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;
import org.openhab.persistence.jpa.internal.model.JpaPersistentItem;

/**
 * The historic item as returned when querying the service.
 * @author mbergmann
 * @since 1.6.0
 *
 */
public class JpaHistoricItem implements HistoricItem {

	final private String name;
	final private State state;
	final private Date timestamp;
		
	public JpaHistoricItem(String name, State state, Date timestamp) {
		this.name = name;
		this.state = state;
		this.timestamp = timestamp;
	}
	
	public String getName() {
		return name;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public State getState() {
		return state;
	}
	
	@Override
	public String toString() {
		return DateFormat.getDateTimeInstance().format(timestamp) + ": " + name + " -> "+ state.toString();
	}

	/**
	 * This method maps a jpa result item to this historic item.
	 * @param jpaQueryResult the result which jpa items
	 * @param item used for query information, like the state (State)
	 * @return list of historic items
	 */
	public static List<HistoricItem> fromResultList(List<JpaPersistentItem> jpaQueryResult, Item item) {
		List<HistoricItem> ret = new ArrayList<HistoricItem>();
		for(JpaPersistentItem i : jpaQueryResult) {
			
			State state;
			if (item instanceof NumberItem) {
				state = new DecimalType(Double.valueOf(i.getValue()));
			} else if (item instanceof DimmerItem) {
				state = new PercentType(Integer.valueOf(i.getValue()));
			} else if (item instanceof SwitchItem) {
				state = OnOffType.valueOf(i.getValue());
			} else if (item instanceof ContactItem) {
				state = OpenClosedType.valueOf(i.getValue());
			} else if (item instanceof RollershutterItem) {
				state = new PercentType(Integer.valueOf(i.getValue()));
			} else if (item instanceof ColorItem) {
				state = new HSBType(i.getValue());
			} else if (item instanceof DateTimeItem) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(Long.valueOf(i.getValue())));
				state = new DateTimeType(cal);
			} else {
				state = new StringType(i.getValue());
			}
			
			JpaHistoricItem hi = new JpaHistoricItem(item.getName(), state, i.getTimestamp());
			ret.add(hi);
		}
		
		return ret;
	}
}
