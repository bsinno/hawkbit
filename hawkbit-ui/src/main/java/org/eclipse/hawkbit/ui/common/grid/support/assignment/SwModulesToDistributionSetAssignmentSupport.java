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
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.management.event.SaveActionWindowEvent;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Support for assigning software modules to distribution set.
 * 
 */
public class SwModulesToDistributionSetAssignmentSupport
        extends DeploymentAssignmentSupport<ProxySoftwareModule, ProxyDistributionSet> {

    private final TargetManagement targetManagement;
    private final DistributionSetManagement distributionSetManagement;
    private final UIEventBus eventBus;
    private final SpPermissionChecker permChecker;

    public SwModulesToDistributionSetAssignmentSupport(final UINotification notification,
            final VaadinMessageSource i18n, final TargetManagement targetManagement,
            final DistributionSetManagement distributionSetManagement, final UIEventBus eventBus,
            final SpPermissionChecker permChecker) {
        super(notification, i18n);

        this.targetManagement = targetManagement;
        this.distributionSetManagement = distributionSetManagement;
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
        if (targetManagement.countByFilters(null, null, null, ds.getId(), Boolean.FALSE, new String[] {}) > 0) {
            /* Distribution is already assigned */
            notification.displayValidationError(i18n.getMessage("message.dist.inuse", ds.getNameVersion()));
            return false;
        }

        if (distributionSetManagement.isInUse(ds.getId())) {
            notification.displayValidationError(
                    i18n.getMessage("message.error.notification.ds.target.assigned", ds.getName(), ds.getVersion()));
            return false;
        }

        return true;
    }

    private boolean validateAssignment(final ProxySoftwareModule sm, final ProxyDistributionSet ds) {
        // TODO: do we need this check here (for isSoftwareModuleDragged
        // impelementation check master)?
        // if (!isSoftwareModuleDragged(ds.getId(), sm)) {
        // return false;
        // }

        if (sm.getType().getMaxAssignments() < 1) {
            return false;
        }

        if (ds.getModules().contains(sm)) {
            /* Already has software module */
            notification.displayValidationError(i18n.getMessage("message.software.dist.already.assigned",
                    sm.getNameAndVersion(), ds.getNameVersion()));
            return false;
        }

        if (!ds.getType().containsModuleType(sm.getType())) {
            /* Invalid type of the software module */
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
        // TODO: check if we need to manage Ui State here (getAssignedList(),
        // getConsolidatedDistSoftwareList())

        final List<String> softwareModuleNames = sourceItemsToAssign.stream()
                .map(ProxySoftwareModule::getNameAndVersion).collect(Collectors.toList());
        openConfirmationWindowForAssignments(softwareModuleNames, targetItem.getNameVersion(), null, true,
                () -> assignSwModulesToDistribution(sourceItemsToAssign, targetItem));
    }

    private void assignSwModulesToDistribution(final List<ProxySoftwareModule> swModules,
            final ProxyDistributionSet ds) {
        final Set<Long> swModuleIdsToAssign = swModules.stream().map(ProxySoftwareModule::getId)
                .collect(Collectors.toSet());
        distributionSetManagement.assignSoftwareModules(ds.getId(), swModuleIdsToAssign);

        notification.displaySuccess(i18n.getMessage("message.software.assignment", swModuleIdsToAssign.size()));
        eventBus.publish(this, SaveActionWindowEvent.SAVED_ASSIGNMENTS);
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
