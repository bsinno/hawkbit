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

/**
 * Proxy for {@link SoftwareModule} to display details in Software modules
 * table.
 */
public class ProxySoftwareModule extends ProxyNamedEntity {
    private static final long serialVersionUID = 1L;

    private String version;

    private String nameAndVersion;

    private String vendor;

    private Long typeId;

    // TODO: consider removing or refactoring ProxyType
    private ProxyType proxyType;

    private boolean assigned;

    public String getVendor() {
        return vendor;
    }

    public void setVendor(final String vendor) {
        this.vendor = vendor;
    }

    public String getNameAndVersion() {
        return nameAndVersion;
    }

    public void setNameAndVersion(final String nameAndVersion) {
        this.nameAndVersion = nameAndVersion;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public void setAssigned(final boolean assigned) {
        this.assigned = assigned;
    }

    public ProxyType getProxyType() {
        return proxyType;
    }

    public void setProxyType(final ProxyType proxyType) {
        this.proxyType = proxyType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(final Long typeId) {
        this.typeId = typeId;
    }
}
