/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.event;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAction;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.common.table.BaseUIEntityEvent;

/**
 * Class which contains event when selecting action from Action Grid;
 */
public class DeploymentActionEvent extends BaseUIEntityEvent<ProxyAction> {

    /**
     * Constrcutor.
     * 
     * @param eventType
     *            the event type.
     */
    public DeploymentActionEvent(final BaseEntityEventType eventType) {
        super(eventType, null);
    }

    /**
     * Constrcutor .
     * 
     * @param eventType
     *            the event type.
     * @param action
     *            the action
     */
    public DeploymentActionEvent(final BaseEntityEventType eventType, final ProxyAction action) {
        super(eventType, action);
    }
}
