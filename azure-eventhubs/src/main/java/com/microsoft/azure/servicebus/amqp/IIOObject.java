/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

public interface IIOObject {

    public static enum IOObjectState {
        OPENING,
        OPENED,
        CLOSED,
        CLOSING
    }

    // should be run on reactor thread
    public IOObjectState getState();
}
