/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.openhab.core.library.types.DecimalType;

/**
 * Converts a Double value into a {@link DecimalType}. The resulting
 * {@link DecimalType} is rounded to 3 digits.
 * 
 * @author Thomas Letsch (contact@thomas-letsch.de)
 * @since 1.4.0
 */
public class DoubleDecimalConverter extends StateConverter<Double, DecimalType> {

    @Override
    protected DecimalType convertToImpl(Double source) {
        BigDecimal bValue = BigDecimal.valueOf(source);
        bValue.setScale(3, RoundingMode.HALF_UP);
        return new DecimalType(bValue);
    }

    @Override
    protected Double convertFromImpl(DecimalType source) {
        Double value = source.doubleValue();
        return value;
    }

}
