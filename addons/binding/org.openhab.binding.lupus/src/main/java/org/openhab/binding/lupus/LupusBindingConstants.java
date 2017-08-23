/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lupus;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link LupusBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin Schlaak - Initial contribution
 */
public class LupusBindingConstants {

    private static final String BINDING_ID = "lupus";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_XT2 = new ThingTypeUID(BINDING_ID, "xt2");

    // List of all Channel ids
    public static final String CHANNEL_MODE_A1 = LupusConnection.MODE_A1;
    public static final String CHANNEL_MODE_A2 = LupusConnection.MODE_A2;
    public static final String CHANNEL_STATE_A1 = LupusConnection.STATE_A1;
    public static final String CHANNEL_STATE_A2 = LupusConnection.STATE_A2;
    public static final String CHANNEL_STATE_A1_MSG = LupusConnection.STATE_A1_MSG;
    public static final String CHANNEL_STATE_A2_MSG = LupusConnection.STATE_A2_MSG;

}
