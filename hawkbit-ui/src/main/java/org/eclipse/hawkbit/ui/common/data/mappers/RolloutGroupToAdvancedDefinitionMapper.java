/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.mappers;

import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAdvancedRolloutGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;

/**
 * Maps {@link RolloutGroup} entities, fetched from backend, to the
 * {@link ProxyAdvancedRolloutGroup} entities.
 */
public class RolloutGroupToAdvancedDefinitionMapper {

    private final TargetFilterQueryManagement targetFilterQueryManagement;

    public RolloutGroupToAdvancedDefinitionMapper(final TargetFilterQueryManagement targetFilterQueryManagement) {
        this.targetFilterQueryManagement = targetFilterQueryManagement;
    }

    public ProxyAdvancedRolloutGroup map(final RolloutGroup rolloutGroup) {
        final ProxyAdvancedRolloutGroup advancedGroupRow = new ProxyAdvancedRolloutGroup();
        advancedGroupRow.setGroupName(rolloutGroup.getName());
        advancedGroupRow.setTargetsCount((long) rolloutGroup.getTotalTargets());

        final String groupTargetFilterQuery = rolloutGroup.getTargetFilterQuery();
        if (!StringUtils.isEmpty(groupTargetFilterQuery)) {
            advancedGroupRow.setTargetFilterQuery(groupTargetFilterQuery);
            final Page<TargetFilterQuery> filterQueries = targetFilterQueryManagement.findByQuery(PageRequest.of(0, 1),
                    groupTargetFilterQuery);
            if (filterQueries.getTotalElements() == 1) {
                advancedGroupRow.setTargetFilterId(filterQueries.getContent().get(0).getId());
            }
        }

        advancedGroupRow.setTargetPercentage(rolloutGroup.getTargetPercentage());
        advancedGroupRow.setTriggerThresholdPercentage(rolloutGroup.getSuccessConditionExp());
        advancedGroupRow.setErrorThresholdPercentage(rolloutGroup.getErrorConditionExp());

        return advancedGroupRow;
    }
}
