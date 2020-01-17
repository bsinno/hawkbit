/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

public class ShowFormEventPayload<T> {
    private final FormType formType;
    private final Class<?> entityType;
    private final Class<?> parentEntityType;
    private final T entity;
    private final View view;

    public ShowFormEventPayload(final FormType formType, final Class<?> entityType, final View view) {
        this(formType, entityType, null, null, view);
    }

    public ShowFormEventPayload(final FormType formType, final T entity, final View view) {
        this(formType, entity.getClass(), null, entity, view);
    }

    public ShowFormEventPayload(final FormType formType, final Class<?> entityType, final Class<?> parentEntityType,
            final View view) {
        this(formType, entityType, parentEntityType, null, view);
    }

    public ShowFormEventPayload(final FormType formType, final Class<?> parentEntityType, final T entity,
            final View view) {
        this(formType, entity.getClass(), parentEntityType, entity, view);
    }

    private ShowFormEventPayload(final FormType formType, final Class<?> entityType, final Class<?> parentEntityType,
            final T entity, final View view) {
        this.formType = formType;
        this.entityType = entityType;
        this.parentEntityType = parentEntityType;
        this.entity = entity;
        this.view = view;
    }

    public FormType getFormType() {
        return formType;
    }

    public Class<?> getEntityType() {
        return entityType;
    }

    public Class<?> getParentEntityType() {
        return parentEntityType;
    }

    public T getEntity() {
        return entity;
    }

    public View getView() {
        return view;
    }

    public enum FormType {
        ADD, EDIT;
    }
}
