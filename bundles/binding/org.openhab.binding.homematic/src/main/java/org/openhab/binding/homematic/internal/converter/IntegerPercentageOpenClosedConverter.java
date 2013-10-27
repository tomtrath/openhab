/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.converter;

import org.openhab.core.library.types.OpenClosedType;


/**
 * Converts an Integer to an OnOffValue. The given Integer is considered to be
 * in a range of 0..100 (like percent). Only a value of 100 is converted to OPEN.
 * 
 * @author Thomas Letsch (contact@thomas-letsch.de)
 * 
 */
public class IntegerPercentageOpenClosedConverter extends StateConverter<Integer, OpenClosedType> {

    private static final int MAX_VALUE = 100;
    private static final int MIN_VALUE = 0;

    @Override
    protected OpenClosedType convertToImpl(Integer source) {
        return (source == MAX_VALUE) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
    }

    @Override
    protected Integer convertFromImpl(OpenClosedType source) {
        return source.equals(OpenClosedType.OPEN) ? MAX_VALUE : MIN_VALUE;
    }

}
