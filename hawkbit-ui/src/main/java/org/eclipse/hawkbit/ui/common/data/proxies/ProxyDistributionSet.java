/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.ui.common.data.aware.VersionAware;

/**
 * Proxy for {@link DistributionSet}.
 */
public class ProxyDistributionSet extends ProxyNamedEntity implements VersionAware {
    private static final long serialVersionUID = 1L;

    private Boolean isComplete;

    private String version;

    private String nameVersion;

    private Long typeId;

    // TODO: consider removing or refactoring ProxyType
    private ProxyType proxyType;

    private boolean requiredMigrationStep;

    public ProxyDistributionSet() {
    }

    public ProxyDistributionSet(final Long id) {
        super(id);
    }

    public String getNameVersion() {
        return nameVersion;
    }

    public void setNameVersion(final String nameVersion) {
        this.nameVersion = nameVersion;
    }

    public Boolean getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(final Boolean isComplete) {
        this.isComplete = isComplete;
    }

    public boolean isRequiredMigrationStep() {
        return requiredMigrationStep;
    }

    public void setRequiredMigrationStep(final boolean requiredMigrationStep) {
        this.requiredMigrationStep = requiredMigrationStep;
    }

    public ProxyType getProxyType() {
        return proxyType;
    }

    public void setProxyType(final ProxyType proxyType) {
        this.proxyType = proxyType;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setVersion(final String version) {
        this.version = version;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(final Long typeId) {
        this.typeId = typeId;
    }

    public ProxyDistributionSet of(final ProxyIdNameVersion dsIdNameVersion) {
        final ProxyDistributionSet ds = new ProxyDistributionSet();

        ds.setId(dsIdNameVersion.getId());
        ds.setName(dsIdNameVersion.getName());
        ds.setVersion(dsIdNameVersion.getVersion());

        return ds;
    }

    public ProxyIdNameVersion getIdNameVersion() {
        return new ProxyIdNameVersion(getId(), getName(), getVersion());
    }
}