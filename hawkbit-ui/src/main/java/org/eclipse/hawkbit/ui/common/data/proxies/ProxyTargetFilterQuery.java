/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.ui.common.data.aware.DsIdAware;

/**
 * Proxy for {@link TargetFilterQuery}.
 */
public class ProxyTargetFilterQuery extends ProxyNamedEntity implements DsIdAware {
    private static final long serialVersionUID = 1L;

    private String query;

    private boolean isAutoAssignmentEnabled;

    private ProxyIdNameVersion autoAssignDsIdNameVersion;

    private ActionType autoAssignActionType;

    public ProxyTargetFilterQuery() {
    }

    public ProxyTargetFilterQuery(final Long id) {
        super(id);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(final String query) {
        this.query = query;
    }

    public ActionType getAutoAssignActionType() {
        return autoAssignActionType;
    }

    public void setAutoAssignActionType(final ActionType autoAssignActionType) {
        this.autoAssignActionType = autoAssignActionType;
    }

    public boolean isAutoAssignmentEnabled() {
        return isAutoAssignmentEnabled;
    }

    public void setAutoAssignmentEnabled(final boolean isAutoAssignmentEnabled) {
        this.isAutoAssignmentEnabled = isAutoAssignmentEnabled;
    }

    public ProxyIdNameVersion getAutoAssignDsIdNameVersion() {
        return autoAssignDsIdNameVersion;
    }

    public void setAutoAssignDsIdNameVersion(final ProxyIdNameVersion autoAssignDsIdNameVersion) {
        this.autoAssignDsIdNameVersion = autoAssignDsIdNameVersion;
    }

    @Override
    public void setDistributionSetId(final Long id) {
        if (autoAssignDsIdNameVersion != null) {
            autoAssignDsIdNameVersion.setId(id);
        } else {
            autoAssignDsIdNameVersion = new ProxyIdNameVersion(id, null, null);
        }
    }

    @Override
    public Long getDistributionSetId() {
        return autoAssignDsIdNameVersion != null ? autoAssignDsIdNameVersion.getId() : null;
    }
}
