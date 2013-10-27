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
import org.openhab.binding.homematic.internal.config.HomematicParameterAddress;
import org.openhab.binding.homematic.internal.device.ParameterKey;
import org.openhab.binding.homematic.test.HomematicBindingProviderMock;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;

public class SmokeDetectorTest extends BasicBindingTest {

    private static final ParameterKey PARAMETER_KEY = ParameterKey.STATE;

    @Before
    public void setupProvider() {
        provider.setItem(new SwitchItem(HomematicBindingProviderMock.DEFAULT_ITEM_NAME));
        provider.setParameterAddress(HomematicParameterAddress.from(MOCK_PARAM_ADDRESS, PARAMETER_KEY.name()));
        ccu.getPhysicalDevice(MOCK_PARAM_ADDRESS).getDeviceDescription().setType("HM-Sec-SD");
    }

    @Test
    public void allBindingsChangedClosed() {
        checkInitialValue(PARAMETER_KEY, Boolean.FALSE, OnOffType.OFF);
    }

    @Test
    public void allBindingsChangedOpen() {
        checkInitialValue(PARAMETER_KEY, Boolean.TRUE, OnOffType.ON);
    }

    @Test
    public void receiveSensorUpdateClosed() {
        checkValueReceived(PARAMETER_KEY, Boolean.FALSE, OnOffType.OFF);
    }

    @Test
    public void receiveSensorUpdateOpen() {
        checkValueReceived(PARAMETER_KEY, Boolean.TRUE, OnOffType.ON);
    }

}
