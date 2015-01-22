/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.comfoair.datatypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.openhab.binding.comfoair.handling.ComfoAirCommandType;
import org.openhab.binding.comfoair.handling.ComfoAirConnector;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;

/**
 * Class to handle temperature values
 * 
 * @author Holger Hees
 * @since 1.3.0
 */
public class DataTypeRPM implements ComfoAirDataType {

	/**
	 * {@inheritDoc}
	 */
	public State convertToState(int[] data, ComfoAirCommandType commandType) {		
		double value = data[commandType.getGetReplyDataPos()[0]]*256 + data[commandType.getGetReplyDataPos()[1]];		
		return new DecimalType( 1875000.0 / value );
	}

	/**
	 * {@inheritDoc}
	 */
	public int[] convertFromState(State value, ComfoAirCommandType commandType) {
		int[] template = commandType.getChangeDataTemplate();
		int rpm = (int) (1875000.0 / ((DecimalType)value).doubleValue());

		template[0] = rpm>>8;
		template[1] = rpm&0xff;
			
		return template;
	}
	
}
