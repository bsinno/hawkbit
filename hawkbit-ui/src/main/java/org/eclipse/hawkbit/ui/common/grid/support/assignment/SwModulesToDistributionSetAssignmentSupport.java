/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support.assignment;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.im.authentication.SpPermission;
import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.event.DsModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.CollectionUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Support for assigning software modules to distribution set.
 * 
 */
public class SwModulesToDistributionSetAssignmentSupport
        extends DeploymentAssignmentSupport<ProxySoftwareModule, ProxyDistributionSet> {

    private final TargetManagement targetManagement;
    private final DistributionSetManagement dsManagement;
    private final UIEventBus eventBus;
    private final SpPermissionChecker permChecker;

    public SwModulesToDistributionSetAssignmentSupport(final UINotification notification,
            final VaadinMessageSource i18n, final TargetManagement targetManagement,
            final DistributionSetManagement dsManagement, final UIEventBus eventBus,
            final SpPermissionChecker permChecker) {
        super(notification, i18n);

        this.targetManagement = targetManagement;
        this.dsManagement = dsManagement;
        this.eventBus = eventBus;
        this.permChecker = permChecker;
    }

    @Override
    protected List<ProxySoftwareModule> getFilteredSourceItems(final List<ProxySoftwareModule> sourceItemsToAssign,
            final ProxyDistributionSet targetItem) {
        if (!isTargetDsValid(targetItem)) {
            return Collections.emptyList();
        }

        return sourceItemsToAssign.stream().filter(sm -> validateAssignment(sm, targetItem))
                .collect(Collectors.toList());
    }

    private boolean isTargetDsValid(final ProxyDistributionSet ds) {
        if (targetManagement.countByFilters(null, null, null, ds.getId(), Boolean.FALSE, "") > 0) {
            /* Distribution is already assigned */
            notification.displayValidationError(i18n.getMessage("message.dist.inuse", ds.getNameVersion()));
            return false;
        }

        if (dsManagement.isInUse(ds.getId())) {
            notification.displayValidationError(
                    i18n.getMessage("message.error.notification.ds.target.assigned", ds.getName(), ds.getVersion()));
            return false;
        }

        return true;
    }

    private boolean validateAssignment(final ProxySoftwareModule sm, final ProxyDistributionSet ds) {
        // TODO: check if < 1 is corect
        if (sm.getType().getMaxAssignments() < 1) {
            return false;
        }

        // TODO: Check if it is better to load software modules from DB here,
        // instead of eager load from DS in Mapper
        if (!CollectionUtils.isEmpty(ds.getModules())
                && ds.getModules().stream().map(ProxyIdentifiableEntity::getId).anyMatch(id -> id.equals(sm.getId()))) {
            notification.displayValidationError(i18n.getMessage("message.software.dist.already.assigned",
                    sm.getNameAndVersion(), ds.getNameVersion()));
            return false;
        }

        if (!ds.getType().containsModuleType(sm.getType())) {
            notification.displayValidationError(i18n.getMessage("message.software.dist.type.notallowed",
                    sm.getNameAndVersion(), ds.getNameVersion(), sm.getType().getName()));
            return false;
        }

        return true;
    }

    @Override
    public List<String> getMissingPermissionsForDrop() {
        return permChecker.hasUpdateRepositoryPermission() ? Collections.emptyList()
                : Collections.singletonList(SpPermission.UPDATE_REPOSITORY);
    }

    @Override
    protected void performAssignment(final List<ProxySoftwareModule> sourceItemsToAssign,
            final ProxyDistributionSet targetItem) {
        final List<String> softwareModuleNames = sourceItemsToAssign.stream()
                .map(ProxySoftwareModule::getNameAndVersion).collect(Collectors.toList());
        openConfirmationWindowForAssignments(softwareModuleNames, targetItem.getNameVersion(), null, true,
                () -> assignSwModulesToDistribution(sourceItemsToAssign, targetItem));
    }

    private void assignSwModulesToDistribution(final List<ProxySoftwareModule> swModules,
            final ProxyDistributionSet ds) {
        final Set<Long> swModuleIdsToAssign = swModules.stream().map(ProxySoftwareModule::getId)
                .collect(Collectors.toSet());
        dsManagement.assignSoftwareModules(ds.getId(), swModuleIdsToAssign);

        notification.displaySuccess(i18n.getMessage("message.software.assignment", swModuleIdsToAssign.size()));
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new DsModifiedEventPayload(EntityModifiedEventType.ENTITY_UPDATED, ds.getId()));
    }

    @Override
    protected String sourceEntityType() {
        return i18n.getMessage("caption.software.module");
    }

    @Override
    protected String targetEntityType() {
        return i18n.getMessage("distribution.details.header");
    }

    @Override
    protected String confirmationWindowId() {
        return UIComponentIdProvider.SOFT_MODULE_TO_DIST_ASSIGNMENT_CONFIRM_ID;
    }
}
