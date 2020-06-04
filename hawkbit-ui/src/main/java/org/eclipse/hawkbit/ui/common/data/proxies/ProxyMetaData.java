/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

import java.security.SecureRandom;

import org.eclipse.hawkbit.repository.model.MetaData;

/**
 * Proxy for {@link MetaData}.
 */
public class ProxyMetaData extends ProxyIdentifiableEntity {
    private static final long serialVersionUID = 1L;

    private String key;
    private String value;
    private Long entityId;
    private boolean isTargetVisible;

    public ProxyMetaData() {
        super(new SecureRandom().nextLong());
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(final Long entityId) {
        this.entityId = entityId;
    }

    public boolean isTargetVisible() {
        return isTargetVisible;
    }

    public void setTargetVisible(final boolean isTargetVisible) {
        this.isTargetVisible = isTargetVisible;
    }
}
