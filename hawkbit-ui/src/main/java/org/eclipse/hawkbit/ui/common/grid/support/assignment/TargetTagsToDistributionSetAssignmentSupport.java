/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support.assignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Support for assigning target tags to distribution set.
 * 
 */
public class TargetTagsToDistributionSetAssignmentSupport extends AssignmentSupport<ProxyTag, ProxyDistributionSet> {
    private final TargetsToDistributionSetAssignmentSupport targetsToDistributionSetAssignmentSupport;
    private final TargetManagement targetManagement;

    public TargetTagsToDistributionSetAssignmentSupport(final UINotification notification,
            final VaadinMessageSource i18n, final TargetManagement targetManagement,
            final TargetsToDistributionSetAssignmentSupport targetsToDistributionSetAssignmentSupport) {
        super(notification, i18n);

        this.targetManagement = targetManagement;
        this.targetsToDistributionSetAssignmentSupport = targetsToDistributionSetAssignmentSupport;
    }

    // TODO: remove duplication with TargetTagsToTargetAssignmentSupport
    @Override
    protected List<ProxyTag> getFilteredSourceItems(final List<ProxyTag> sourceItemsToAssign,
            final ProxyDistributionSet targetItem) {
        if (isNoTagAssigned(sourceItemsToAssign)) {
            notification.displayValidationError(
                    i18n.getMessage("message.tag.cannot.be.assigned", i18n.getMessage("label.no.tag.assigned")));
            return Collections.emptyList();
        }

        return sourceItemsToAssign;
    }

    // TODO: check if it is a valid check
    private boolean isNoTagAssigned(final List<ProxyTag> targetTagsToAssign) {
        return targetTagsToAssign.stream()
                .anyMatch(targetTag -> targetTag.getName().equals(i18n.getMessage("label.no.tag.assigned")));
    }

    @Override
    public List<String> getMissingPermissionsForDrop() {
        return targetsToDistributionSetAssignmentSupport.getMissingPermissionsForDrop();
    }

    @Override
    protected void performAssignment(final List<ProxyTag> sourceItemsToAssign, final ProxyDistributionSet targetItem) {
        // TODO: fix (we are taking first tag because multi-tag assignment is
        // not supported)
        final String tagName = sourceItemsToAssign.get(0).getName();
        final Long tagId = sourceItemsToAssign.get(0).getId();

        final List<Target> targetsToAssign = getTargetsAssignedToTag(tagId);

        if (targetsToAssign.isEmpty()) {
            notification.displayValidationError(i18n.getMessage("message.no.targets.assiged.fortag", tagName));
            return;
        }

        // TODO: try not to map here if possible
        final List<ProxyTarget> proxyTargetsToAssign = mapTargetsToProxyTargets(targetsToAssign);

        targetsToDistributionSetAssignmentSupport.performAssignment(proxyTargetsToAssign, targetItem);
    }

    private List<Target> getTargetsAssignedToTag(final Long tagId) {
        Pageable query = PageRequest.of(0, SPUIDefinitions.PAGE_SIZE);
        final List<Target> targetsAssignedToTag = new ArrayList<>();
        Page<Target> targetsAssignedToTagPageable;

        do {
            targetsAssignedToTagPageable = targetManagement.findByTag(query, tagId);
            if (targetsAssignedToTagPageable.hasContent()) {
                targetsAssignedToTag.addAll(targetsAssignedToTagPageable.getContent());
            }
        } while ((query = targetsAssignedToTagPageable.nextPageable()) != Pageable.unpaged());

        return targetsAssignedToTag;
    }

    private List<ProxyTarget> mapTargetsToProxyTargets(final List<Target> targetsToAssign) {
        return targetsToAssign.stream().map(target -> {
            final ProxyTarget proxyTarget = new ProxyTarget();

            proxyTarget.setId(target.getId());
            proxyTarget.setName(target.getName());

            return proxyTarget;
        }).collect(Collectors.toList());
    }
}
