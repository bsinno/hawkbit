/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.event;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.SpPermissionChecker;
import org.eclipse.hawkbit.repository.model.DistributionSetTagAssignmentResult;
import org.eclipse.hawkbit.ui.common.DistributionSetIdName;
import org.eclipse.hawkbit.ui.common.table.AbstractTable;
import org.eclipse.hawkbit.ui.management.state.DistributionTableFilters;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.I18N;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUILabelDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableTransferable;

/**
 *
 *
 */
public class DistributionTagDropEvent implements DropHandler {

    private static final long serialVersionUID = 7338133229709850212L;

    private final I18N i18n;

    private final UINotification notification;

    private final SpPermissionChecker permChecker;

    private final DistributionTableFilters distFilterParameters;

    private final DistributionSetManagement distributionSetManagement;

    private final EventBus.UIEventBus eventBus;

    private final ManagementViewAcceptCriteria managementViewAcceptCriteria;

    public DistributionTagDropEvent(final I18N i18n, final UINotification notification,
            final SpPermissionChecker permChecker, final DistributionTableFilters distFilterParameters,
            final DistributionSetManagement distributionSetManagement, final UIEventBus eventBus,
            final ManagementViewAcceptCriteria managementViewAcceptCriteria) {
        this.i18n = i18n;
        this.notification = notification;
        this.permChecker = permChecker;
        this.distFilterParameters = distFilterParameters;
        this.distributionSetManagement = distributionSetManagement;
        this.eventBus = eventBus;
        this.managementViewAcceptCriteria = managementViewAcceptCriteria;
    }

    @Override
    public void drop(final DragAndDropEvent event) {
        if (validate(event) && isNoTagAssigned(event)) {
            final TableTransferable tbl = (TableTransferable) event.getTransferable();
            final Table source = tbl.getSourceComponent();

            if (source.getId().equals(UIComponentIdProvider.DIST_TABLE_ID)) {
                processDistributionDrop(event);
            }

        }
    }

    private Boolean isNoTagAssigned(final DragAndDropEvent event) {
        final String tagName = ((DragAndDropWrapper) (event.getTargetDetails().getTarget())).getData().toString();
        if (tagName.equals(SPUIDefinitions.DISTRIBUTION_TAG_BUTTON)) {
            notification.displayValidationError(
                    i18n.get("message.tag.cannot.be.assigned", new Object[] { i18n.get("label.no.tag.assigned") }));
            return false;
        }
        return true;
    }

    private Boolean validate(final DragAndDropEvent event) {
        final Component compsource = event.getTransferable().getSourceComponent();
        if (!(compsource instanceof Table)) {

            notification.displayValidationError(i18n.get(SPUILabelDefinitions.ACTION_NOT_ALLOWED));
            return false;
        } else {
            final Table source = ((TableTransferable) event.getTransferable()).getSourceComponent();

            if (!validateIfSourceIsDs(source) && !checkForDSUpdatePermission()) {
                return false;
            }
        }
        return true;
    }

    private boolean checkForDSUpdatePermission() {
        if (!permChecker.hasUpdateDistributionPermission()) {

            notification.displayValidationError(i18n.get("message.permission.insufficient"));
            return false;
        }

        return true;
    }

    private boolean validateIfSourceIsDs(final Table source) {
        if (!source.getId().equals(UIComponentIdProvider.DIST_TABLE_ID)) {
            notification.displayValidationError(i18n.get(SPUILabelDefinitions.ACTION_NOT_ALLOWED));
            return false;
        }
        return true;
    }

    private void processDistributionDrop(final DragAndDropEvent event) {

        final com.vaadin.event.dd.TargetDetails targetDetails = event.getTargetDetails();

        final TableTransferable transferable = (TableTransferable) event.getTransferable();
        @SuppressWarnings("unchecked")
        final AbstractTable<?, DistributionSetIdName> source = (AbstractTable<?, DistributionSetIdName>) transferable
                .getSourceComponent();

        final Set<DistributionSetIdName> distSelected = source.getDeletedEntityByTransferable(transferable);
        final Set<Long> distributionList = distSelected.stream().map(entity -> entity.getId())
                .collect(Collectors.toSet());

        final String distTagName = HawkbitCommonUtil.removePrefix(targetDetails.getTarget().getId(),
                SPUIDefinitions.DISTRIBUTION_TAG_ID_PREFIXS);

        final List<String> tagsClickedList = distFilterParameters.getDistSetTags();
        final DistributionSetTagAssignmentResult result = distributionSetManagement
                .toggleTagAssignment(distributionList, distTagName);

        notification.displaySuccess(HawkbitCommonUtil.createAssignmentMessage(distTagName, result, i18n));
        if (result.getUnassigned() >= 1 && !tagsClickedList.isEmpty()) {
            eventBus.publish(this, TargetFilterEvent.FILTER_BY_TAG);
        }
    }

    /**
     * Criteria.
     * 
     * @return AcceptCriterion as accept
     */
    @Override
    public AcceptCriterion getAcceptCriterion() {
        return managementViewAcceptCriteria;

    }
}
