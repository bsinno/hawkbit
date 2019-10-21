/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

public final class EventTopics {
    public static final String ENTITY_MODIFIED = "entityModified";
    public static final String SELECTION_CHANGED = "selectionChanged";

    public static final String LAYOUT_VISIBILITY_CHANGED = "layoutVisibilityChanged";
    public static final String LAYOUT_RESIZED = "layoutResized";

    public static final String FILTER_BUTTONS_ACTIONS_CHANGED = "filterButtonsActionsChanged";

    public static final String SEARCH_FILTER_CHANGED = "searchFilterChanged";
    public static final String TYPE_FILTER_CHANGED = "typeFilterChanged";
    public static final String TAG_FILTER_CHANGED = "tagFilterChanged";
    public static final String STATUS_FILTER_CHANGED = "statusFilterChanged";
    public static final String OVERDUE_FILTER_CHANGED = "overdueFilterChanged";

    public static final String REMOTE_EVENT_RECEIVED = "remoteEventReceived";

    private EventTopics() {
    }
}