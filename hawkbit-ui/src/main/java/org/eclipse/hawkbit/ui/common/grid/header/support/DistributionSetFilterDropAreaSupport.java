/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.header.support;

import java.util.List;
import java.util.Optional;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.entity.DistributionSetIdName;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.management.targettable.TargetGridLayoutUiState;
import org.eclipse.hawkbit.ui.management.targettag.filter.TargetTagFilterLayoutUiState;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUITargetDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.cronutils.utils.StringUtils;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.dnd.DropTargetExtension;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.ui.Label;

//TODO: refactor
public class DistributionSetFilterDropAreaSupport implements HeaderSupport {

    private final VaadinMessageSource i18n;
    private final UIEventBus eventBus;
    private final UINotification notification;

    private final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState;
    private final TargetGridLayoutUiState targetGridLayoutUiState;

    private final DistributionSetManagement distributionSetManagement;

    private final HorizontalLayout dropHintDropFilterLayout;
    private final HorizontalLayout dropAreaLayout;

    public DistributionSetFilterDropAreaSupport(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final UINotification notification, final DistributionSetManagement distributionSetManagement,
            final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState,
            final TargetGridLayoutUiState targetGridLayoutUiState) {
        this.i18n = i18n;
        this.eventBus = eventBus;
        this.notification = notification;
        this.distributionSetManagement = distributionSetManagement;
        this.targetTagFilterLayoutUiState = targetTagFilterLayoutUiState;
        this.targetGridLayoutUiState = targetGridLayoutUiState;

        this.dropAreaLayout = buildDistributionSetFilterDropArea();
        // TODO: do we really need hint layout?
        this.dropHintDropFilterLayout = buildDistributionSetFilterHintDropArea();
    }

    private HorizontalLayout buildDistributionSetFilterDropArea() {
        final HorizontalLayout dropArea = new HorizontalLayout();

        dropArea.setId(UIComponentIdProvider.TARGET_DROP_FILTER_ICON);
        dropArea.setStyleName("target-dist-filter-info");
        dropArea.setHeightUndefined();
        dropArea.setSizeUndefined();

        return dropArea;
    }

    private HorizontalLayout buildDistributionSetFilterHintDropArea() {
        final HorizontalLayout hintDropFilterLayout = new HorizontalLayout();

        hintDropFilterLayout.addStyleName("filter-drop-hint-layout");
        hintDropFilterLayout.setWidth(100, Unit.PERCENTAGE);

        // TODO: check if it works
        final DropTargetExtension<HorizontalLayout> dropTarget = new DropTargetExtension<>(dropAreaLayout);
        dropTarget.addDropListener(event -> {
            final String sourceId = event.getDataTransferData("source_id").orElse("");
            final Object sourceDragData = event.getDragData().orElse(null);

            if (!isDropValid(sourceId, sourceDragData)) {
                return;
            }

            if (sourceDragData instanceof List) {
                filterByDroppedDistSets((List<ProxyDistributionSet>) sourceDragData);
            } else {
                notification.displayValidationError(i18n.getMessage("message.action.did.not.work"));
            }
        });

        if (targetGridLayoutUiState.getFilterDsIdNameVersion() != null) {
            addDsFilterDropAreaTextField(targetGridLayoutUiState.getFilterDsIdNameVersion());
        }
        hintDropFilterLayout.addComponent(dropAreaLayout);
        hintDropFilterLayout.setComponentAlignment(dropAreaLayout, Alignment.TOP_CENTER);
        hintDropFilterLayout.setExpandRatio(dropAreaLayout, 1.0F);

        return hintDropFilterLayout;
    }

    private boolean isDropValid(final String sourceId, final Object sourceDragData) {
        // TODO: adapt message for isCustomFilterSelected case (e.g.
        // "Filter by DS is not allowed while Custom Filter is active")
        if (StringUtils.isEmpty(sourceId) || !sourceId.equals(UIComponentIdProvider.DIST_TABLE_ID)
                || targetTagFilterLayoutUiState.isCustomFilterTabSelected() || sourceDragData == null) {
            notification.displayValidationError(i18n.getMessage(UIMessageIdProvider.MESSAGE_ACTION_NOT_ALLOWED));
            return false;
        }

        return true;
    }

    private void filterByDroppedDistSets(final List<ProxyDistributionSet> droppedDistSets) {
        if (droppedDistSets.size() != 1) {
            notification.displayValidationError(i18n.getMessage("message.onlyone.distribution.dropallowed"));
            return;
        }

        final Long droppedDistSetId = droppedDistSets.get(0).getId();
        final Optional<DistributionSet> distributionSet = distributionSetManagement.get(droppedDistSetId);
        if (!distributionSet.isPresent()) {
            notification.displayWarning(i18n.getMessage("distributionset.not.exists"));
            return;
        }
        final DistributionSetIdName distributionSetIdName = new DistributionSetIdName(distributionSet.get());
        targetGridLayoutUiState.setFilterDsIdNameVersion(distributionSetIdName);

        addDsFilterDropAreaTextField(distributionSetIdName);
    }

    private void addDsFilterDropAreaTextField(final DistributionSetIdName distributionSetIdName) {
        final Button filterLabelClose = SPUIComponentProvider.getButton("drop.filter.close", "", "", "", true,
                FontAwesome.TIMES_CIRCLE, SPUIButtonStyleNoBorder.class);
        filterLabelClose.addClickListener(clickEvent -> restoreState());

        final Label filteredDistLabel = new Label();
        filteredDistLabel.setStyleName(ValoTheme.LABEL_COLORED + " " + ValoTheme.LABEL_SMALL);
        String name = HawkbitCommonUtil.getDistributionNameAndVersion(distributionSetIdName.getName(),
                distributionSetIdName.getVersion());
        if (name.length() > SPUITargetDefinitions.DISTRIBUTION_NAME_MAX_LENGTH_ALLOWED) {
            name = new StringBuilder(name.substring(0, SPUITargetDefinitions.DISTRIBUTION_NAME_LENGTH_ON_FILTER))
                    .append("...").toString();
        }
        filteredDistLabel.setValue(name);
        filteredDistLabel.setSizeUndefined();

        dropAreaLayout.removeAllComponents();
        dropAreaLayout.setSizeFull();
        dropAreaLayout.addComponent(filteredDistLabel);
        dropAreaLayout.addComponent(filterLabelClose);
        dropAreaLayout.setExpandRatio(filteredDistLabel, 1.0F);

        // TODO: implement
        // eventBus.publish(this, TargetFilterEvent.FILTER_BY_DISTRIBUTION);
    }

    @Override
    public Component getHeaderComponent() {
        return dropHintDropFilterLayout;
    }

    @Override
    public void restoreState() {
        /* Remove filter by distribution information. */
        dropAreaLayout.removeAllComponents();
        dropAreaLayout.setSizeUndefined();
        /* Remove distribution Id from target filter parameters */
        targetGridLayoutUiState.setFilterDsIdNameVersion(null);
    }
}
