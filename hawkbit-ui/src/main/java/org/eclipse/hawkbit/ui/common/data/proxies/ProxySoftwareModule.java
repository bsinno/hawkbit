/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.ui.common.data.aware.VersionAware;

/**
 * Proxy for {@link SoftwareModule} to display details in Software modules
 * table.
 */
public class ProxySoftwareModule extends ProxyNamedEntity implements VersionAware {
    private static final long serialVersionUID = 1L;

    private String version;

    private String nameAndVersion;

    private String vendor;

    private Long typeId;

    // TODO: consider removing or refactoring ProxyType
    private ProxyType proxyType;

    private boolean assigned;

    /**
     * Gets the software module vendor
     *
     * @return vendor
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * Sets the vendor
     *
     * @param vendor
     *          software module vendor
     */
    public void setVendor(final String vendor) {
        this.vendor = vendor;
    }

    /**
     * Gets the software module name and version
     *
     * @return nameAndVersion
     */
    public String getNameAndVersion() {
        return nameAndVersion;
    }

    /**
     * Sets the nameAndVersion
     *
     * @param nameAndVersion
     *          software module name and version
     */
    public void setNameAndVersion(final String nameAndVersion) {
        this.nameAndVersion = nameAndVersion;
    }

    /**
     * Flag that indicates if the software module is assigned.
     *
     * @return <code>true</code> if the software module is assigned, otherwise
     *         <code>false</code>
     */
    public boolean isAssigned() {
        return assigned;
    }

    /**
     * Sets the flag that indicates if the software module is assigned.
     *
     * @param assigned
     *            <code>true</code> if the software module is assigned, otherwise
     *            <code>false</code>
     */
    public void setAssigned(final boolean assigned) {
        this.assigned = assigned;
    }

    /**
     * Gets the proxyType
     *
     * @return proxyType
     */
    public ProxyType getProxyType() {
        return proxyType;
    }

    /**
     * Sets the proxyType
     *
     * @param proxyType
     *          ProxyType
     */
    public void setProxyType(final ProxyType proxyType) {
        this.proxyType = proxyType;
    }

    /**
     * Gets the software module version
     *
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version
     *
     * @param version
     *          software module version
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    /**
     * Gets the id of software module type
     *
     * @return typeId
     */
    public Long getTypeId() {
        return typeId;
    }

    /**
     * Sets the typeId
     *
     * @param typeId
     *          id of software module type
     */
    public void setTypeId(final Long typeId) {
        this.typeId = typeId;
    }
}
