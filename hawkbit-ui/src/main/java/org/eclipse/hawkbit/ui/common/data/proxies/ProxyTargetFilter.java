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

/**
 * Proxy for {@link TargetFilterQuery}.
 */
public class ProxyTargetFilter extends ProxyNamedEntity {

    private static final long serialVersionUID = 6622060929679084419L;

    private String query;

    private ProxyDistributionSet autoAssignDistributionSet;

    private ActionType autoAssignActionType;

    public String getQuery() {
        return query;
    }

    public void setQuery(final String query) {
        this.query = query;
    }

    public ProxyDistributionSet getAutoAssignDistributionSet() {
        return autoAssignDistributionSet;
    }

    public void setAutoAssignDistributionSet(final ProxyDistributionSet autoAssignDistributionSet) {
        this.autoAssignDistributionSet = autoAssignDistributionSet;
    }

    public ActionType getAutoAssignActionType() {
        return autoAssignActionType;
    }

    public void setAutoAssignActionType(ActionType autoAssignActionType) {
        this.autoAssignActionType = autoAssignActionType;
    }
}
