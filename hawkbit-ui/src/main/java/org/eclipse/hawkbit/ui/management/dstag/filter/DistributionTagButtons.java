/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstag.filter;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTagManagement;
import org.eclipse.hawkbit.repository.model.DistributionSetTag;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.mappers.TagToProxyTagMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetTagDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.event.DsTagModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtonClickBehaviour;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtons;
import org.eclipse.hawkbit.ui.common.grid.support.DragAndDropSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.DistributionSetsToTagAssignmentSupport;
import org.eclipse.hawkbit.ui.management.dstag.DsTagWindowBuilder;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Class for defining the tag buttons of the distribution sets on the Deployment
 * View.
 */
public class DistributionTagButtons extends AbstractFilterButtons<ProxyTag, Void> {

    private static final long serialVersionUID = 1L;

    private final ManagementUIState managementUIState;
    private final UINotification uiNotification;

    private final transient DistributionSetTagManagement distributionSetTagManagement;
    private final transient DistributionTagButtonClick distributionTagButtonClickBehaviour;
    private final transient DsTagWindowBuilder dsTagWindowBuilder;

    private final ConfigurableFilterDataProvider<ProxyTag, Void, Void> dsTagDataProvider;
    private final DragAndDropSupport<ProxyTag> dragAndDropSupport;

    public DistributionTagButtons(final UIEventBus eventBus, final ManagementUIState managementUIState,
            final VaadinMessageSource i18n, final UINotification uiNotification, final SpPermissionChecker permChecker,
            final DistributionSetTagManagement distributionSetTagManagement,
            final DistributionSetManagement distributionSetManagement, final DsTagWindowBuilder dsTagWindowBuilder) {
        super(eventBus, i18n, uiNotification, permChecker);

        this.managementUIState = managementUIState;
        this.uiNotification = uiNotification;
        this.distributionSetTagManagement = distributionSetTagManagement;
        this.dsTagWindowBuilder = dsTagWindowBuilder;

        this.distributionTagButtonClickBehaviour = new DistributionTagButtonClick(eventBus, managementUIState);
        this.dsTagDataProvider = new DistributionSetTagDataProvider(distributionSetTagManagement,
                new TagToProxyTagMapper<DistributionSetTag>()).withConfigurableFilter();

        final DistributionSetsToTagAssignmentSupport distributionSetsToTagAssignment = new DistributionSetsToTagAssignmentSupport(
                uiNotification, i18n, distributionSetManagement, managementUIState, eventBus, permChecker);

        this.dragAndDropSupport = new DragAndDropSupport<>(this, i18n, uiNotification,
                Collections.singletonMap(UIComponentIdProvider.DIST_TABLE_ID, distributionSetsToTagAssignment));
        this.dragAndDropSupport.addDragAndDrop();

        init();
    }

    // TODO: should we unsubscribe from events here?

    @Override
    public String getGridId() {
        return UIComponentIdProvider.DISTRIBUTION_TAG_TABLE_ID;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyTag, Void, Void> getFilterDataProvider() {
        return dsTagDataProvider;
    }

    @Override
    protected String getFilterButtonsType() {
        return i18n.getMessage(UIMessageIdProvider.CAPTION_DISTRIBUTION_TAG);
    }

    @Override
    protected AbstractFilterButtonClickBehaviour<ProxyTag> getFilterButtonClickBehaviour() {
        return distributionTagButtonClickBehaviour;
    }

    @Override
    protected void deleteFilterButtons(final Collection<ProxyTag> filterButtonsToDelete) {
        // TODO: we do not allow multiple deletion yet
        final ProxyTag dsTagToDelete = filterButtonsToDelete.iterator().next();
        final String dsTagToDeleteName = dsTagToDelete.getName();

        if (managementUIState.getDistributionTableFilters().getClickedDistSetTags().contains(dsTagToDeleteName)) {
            uiNotification.displayValidationError(i18n.getMessage("message.tag.delete", dsTagToDeleteName));
        } else {
            distributionSetTagManagement.delete(dsTagToDeleteName);

            eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                    new DsTagModifiedEventPayload(EntityModifiedEventType.ENTITY_REMOVED, dsTagToDelete.getId()));
        }
    }

    @Override
    protected void editButtonClickListener(final ProxyTag clickedFilter) {
        final Window updateWindow = dsTagWindowBuilder.getWindowForUpdateDsTag(clickedFilter);

        updateWindow.setCaption(i18n.getMessage("caption.update", i18n.getMessage("caption.tag")));
        UI.getCurrent().addWindow(updateWindow);
        updateWindow.setVisible(Boolean.TRUE);
    }

    // TODO
    // @Override
    // protected boolean isClickedByDefault(final Long filterButtonId) {
    // return null !=
    // managementUIState.getDistributionTableFilters().getClickedDistSetTags()
    // &&
    // managementUIState.getDistributionTableFilters().getClickedDistSetTags().contains(tagName);
    // }

    @Override
    protected String getFilterButtonIdPrefix() {
        return SPUIDefinitions.DISTRIBUTION_TAG_ID_PREFIXS;
    }
}
