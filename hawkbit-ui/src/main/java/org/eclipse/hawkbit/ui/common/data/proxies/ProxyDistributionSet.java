/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

import java.util.List;
import java.util.Set;

import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.DistributionSetType;

/**
 * Proxy for {@link DistributionSet}.
 */
public class ProxyDistributionSet extends ProxyNamedEntity {

    private static final long serialVersionUID = 1L;

    private Long distId;

    private Boolean isComplete;

    private String nameVersion;

    // TODO: can ProxyType be used here?
    private DistributionSetType type;

    private boolean requiredMigrationStep;

    // TODO: check if really needed
    private Set<ProxySoftwareModule> modules;

    // TODO: check if really needed
    private List<ProxyTargetFilterQuery> autoAssignFilters;

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

    public Long getDistId() {
        return distId;
    }

    public void setDistId(final Long distId) {
        this.distId = distId;
    }

    public DistributionSetType getType() {
        return type;
    }

    public void setType(final DistributionSetType type) {
        this.type = type;
    }

    public boolean isRequiredMigrationStep() {
        return requiredMigrationStep;
    }

    public void setRequiredMigrationStep(final boolean requiredMigrationStep) {
        this.requiredMigrationStep = requiredMigrationStep;
    }

    public Set<ProxySoftwareModule> getModules() {
        return modules;
    }

    public void setModules(final Set<ProxySoftwareModule> modules) {
        this.modules = modules;
    }

    public List<ProxyTargetFilterQuery> getAutoAssignFilters() {
        return autoAssignFilters;
    }

    public void setAutoAssignFilters(final List<ProxyTargetFilterQuery> autoAssignFilters) {
        this.autoAssignFilters = autoAssignFilters;
    }
}