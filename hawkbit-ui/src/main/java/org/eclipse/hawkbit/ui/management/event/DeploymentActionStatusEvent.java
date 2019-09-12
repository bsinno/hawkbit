/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.event;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyActionStatus;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.common.table.BaseUIEntityEvent;

/**
 * Class which contains event when selecting action from Action Grid;
 */
public class DeploymentActionStatusEvent extends BaseUIEntityEvent<ProxyActionStatus> {

    /**
     * Constrcutor.
     * 
     * @param eventType
     *            the event type.
     */
    public DeploymentActionStatusEvent(final BaseEntityEventType eventType) {
        super(eventType, null);
    }

    /**
     * Constrcutor .
     * 
     * @param eventType
     *            the event type.
     * @param actionStatus
     *            the action status
     */
    public DeploymentActionStatusEvent(final BaseEntityEventType eventType, final ProxyActionStatus actionStatus) {
        super(eventType, actionStatus);
    }
}
