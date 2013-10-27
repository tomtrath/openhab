/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.bus;

import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.homematic.internal.device.ParameterKey;
import org.openhab.binding.homematic.test.HomematicBindingProviderMock;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;

public class SabotageSwitchTest extends BasicBindingTest {

    @Before
    public void setupProvider() {
        provider.setItem(new SwitchItem(HomematicBindingProviderMock.DEFAULT_ITEM_NAME));
    }

    @Test
    public void allBindingsChangedBatterieIsFine() {
        String sensor = "0";
        checkInitialValue(ParameterKey.ERROR, new Integer(sensor), OnOffType.OFF);
    }

    @Test
    public void allBindingsChangedBatterieIsLow() {
        String sensor = "7";
        checkInitialValue(ParameterKey.ERROR, new Integer(sensor), OnOffType.ON);
      }

    @Test
    public void receiveUpdateBatterieIsFine() {
        String sensor = "0";
        checkValueReceived(ParameterKey.ERROR, new Integer(sensor), OnOffType.OFF);
    }
    
    @Test
    public void receiveUpdateBatterieIsLow() {
        String sensor = "7";
        checkValueReceived(ParameterKey.ERROR, new Integer(sensor), OnOffType.ON);
    }
    
}
