/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.thing.link

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.link.events.ItemChannelLinkAddedEvent
import org.eclipse.smarthome.core.thing.link.events.ItemChannelLinkRemovedEvent
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test

import com.google.common.collect.Sets

/**
 * Event Tests for {@link ItemChannelLinkRegistry}.
 *
 * @author Dennis Nobel - Initial contribution
 */
class LinkEventOSGiTest extends OSGiTest {


    ItemChannelLinkRegistry itemChannelLinkRegistry
    Event lastReceivedEvent = null

    @Before
    void setup() {
        registerVolatileStorageService()
        itemChannelLinkRegistry = getService(ItemChannelLinkRegistry)
        def eventSubscriber = [
            getSubscribedEventTypes: {
                Sets.newHashSet(ItemChannelLinkAddedEvent.TYPE,
                        ItemChannelLinkRemovedEvent.TYPE)
            },
            getEventFilter: { null },
            receive: { event -> lastReceivedEvent = event }
        ] as EventSubscriber
        registerService(eventSubscriber)
    }

    @Test
    void 'assert item channel link events are sent'() {
        def link = new ItemChannelLink("item", new ChannelUID("a:b:c:d"))

        itemChannelLinkRegistry.add(link)
        waitFor { lastReceivedEvent != null }
        waitForAssert { assertThat lastReceivedEvent.type, is(ItemChannelLinkAddedEvent.TYPE) }
        assertThat lastReceivedEvent.topic, is("smarthome/links/item-a:b:c:d/added")

        itemChannelLinkRegistry.remove(link.getUID())
        waitForAssert { assertThat lastReceivedEvent.type, is(ItemChannelLinkRemovedEvent.TYPE) }
        assertThat lastReceivedEvent.topic, is("smarthome/links/item-a:b:c:d/removed")
    }
}
