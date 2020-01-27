/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

public class ShowDetailsEventPayload {
    private final Class<?> entityType;
    private final Long entityId;
    private final String entityName;
    private final String parentEntityName;
    private final View view;

    public ShowDetailsEventPayload(final Class<?> entityType, final Long entityId, final String entityName,
            final View view) {
        this(entityType, entityId, entityName, "", view);
    }

    public ShowDetailsEventPayload(final Class<?> entityType, final Long entityId, final String entityName,
            final String parentEntityName, final View view) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityName = entityName;
        this.parentEntityName = parentEntityName;
        this.view = view;
    }

    public Class<?> getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getParentEntityName() {
        return parentEntityName;
    }

    public View getView() {
        return view;
    }
}
