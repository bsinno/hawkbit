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

    /**
     * Constructor for ProxyMetaData
     */
    public ProxyMetaData() {
        super(new SecureRandom().nextLong());
    }

    /**
     * Gets the key
     *
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key
     *
     * @param key
     *          Key of entity
     */
    public void setKey(final String key) {
        this.key = key;
    }

    /**
     * Gets the value
     *
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value
     *
     * @param value
     *          Value correspondent to entity key
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * Gets the id of entity
     *
     * @return entityId
     */
    public Long getEntityId() {
        return entityId;
    }

    /**
     * Sets the entityId
     *
     * @param entityId
     *          id of entity
     */
    public void setEntityId(final Long entityId) {
        this.entityId = entityId;
    }

    /**
     * Flag that indicates if the target is visible.
     *
     * @return <code>true</code> if the target is visible, otherwise
     *         <code>false</code>
     */
    public boolean isTargetVisible() {
        return isTargetVisible;
    }

    /**
     * Sets the flag that indicates if the target is visible.
     *
     * @param isTargetVisible
     *            <code>true</code> if the target is visible, otherwise
     *            <code>false</code>
     */
    public void setTargetVisible(final boolean isTargetVisible) {
        this.isTargetVisible = isTargetVisible;
    }
}
