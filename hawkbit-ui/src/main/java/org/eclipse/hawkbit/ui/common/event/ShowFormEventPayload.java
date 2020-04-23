/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;

public class ShowFormEventPayload<T extends ProxyIdentifiableEntity> extends EventViewAware {
    private final FormType formType;
    private final Class<? extends ProxyIdentifiableEntity> entityType;
    private final Class<? extends ProxyIdentifiableEntity> parentEntityType;
    private final T entity;

    public ShowFormEventPayload(final FormType formType, final Class<? extends ProxyIdentifiableEntity> entityType,
            final EventView view) {
        this(formType, entityType, null, null, view);
    }

    public ShowFormEventPayload(final FormType formType, final T entity, final EventView view) {
        this(formType, entity.getClass(), null, entity, view);
    }

    public ShowFormEventPayload(final FormType formType, final Class<? extends ProxyIdentifiableEntity> entityType,
            final Class<? extends ProxyIdentifiableEntity> parentEntityType, final EventView view) {
        this(formType, entityType, parentEntityType, null, view);
    }

    public ShowFormEventPayload(final FormType formType,
            final Class<? extends ProxyIdentifiableEntity> parentEntityType, final T entity, final EventView view) {
        this(formType, entity.getClass(), parentEntityType, entity, view);
    }

    private ShowFormEventPayload(final FormType formType, final Class<? extends ProxyIdentifiableEntity> entityType,
            final Class<? extends ProxyIdentifiableEntity> parentEntityType, final T entity, final EventView view) {
        super(view);

        this.formType = formType;
        this.entityType = entityType;
        this.parentEntityType = parentEntityType;
        this.entity = entity;
    }

    public FormType getFormType() {
        return formType;
    }

    public Class<? extends ProxyIdentifiableEntity> getEntityType() {
        return entityType;
    }

    public Class<? extends ProxyIdentifiableEntity> getParentEntityType() {
        return parentEntityType;
    }

    public T getEntity() {
        return entity;
    }

    public enum FormType {
        ADD, EDIT;
    }
}
