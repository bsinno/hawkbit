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
    private final Class<?> parentEntityType;
    private final Long parentEntityId;
    private final String parentEntityName;
    private final View view;

    public ShowDetailsEventPayload(final Class<?> entityType, final Class<?> parentEntityType,
            final Long parentEntityId, final String parentEntityName, final View view) {
        this.entityType = entityType;
        this.parentEntityType = parentEntityType;
        this.parentEntityId = parentEntityId;
        this.parentEntityName = parentEntityName;
        this.view = view;
    }

    public Class<?> getEntityType() {
        return entityType;
    }

    public Class<?> getParentEntityType() {
        return parentEntityType;
    }

    public Long getParentEntityId() {
        return parentEntityId;
    }

    public String getParentEntityName() {
        return parentEntityName;
    }

    public View getView() {
        return view;
    }
}
