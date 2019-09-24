/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.model.SoftwareModuleMetadata;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.event.SoftwareModuleEvent;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyKeyValueDetails;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.distributions.smtable.SwMetadataPopupLayout;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.PageRequest;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.UI;

/**
 * Abstract class which contains common code for Software Module Details
 *
 */
public abstract class AbstractSoftwareModuleDetails extends AbstractGridDetailsLayout<ProxySoftwareModule> {
    private static final long serialVersionUID = 1L;

    private final UINotification uiNotification;

    private final transient EntityFactory entityFactory;
    private final transient SoftwareModuleManagement softwareModuleManagement;

    private final MetadataDetailsLayout<ProxySoftwareModule> swmMetadataLayout;

    protected AbstractSoftwareModuleDetails(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final SoftwareModuleManagement softwareManagement,
            final EntityFactory entityFactory, final UINotification uiNotification) {
        super(i18n, permissionChecker, eventBus);

        this.uiNotification = uiNotification;
        this.entityFactory = entityFactory;
        this.softwareModuleManagement = softwareManagement;

        this.swmMetadataLayout = new MetadataDetailsLayout<>(i18n, UIComponentIdProvider.SW_METADATA_DETAIL_LINK,
                this::showMetadataDetails, this::getSmMetaData);
        binder.forField(swmMetadataLayout).bind(sm -> sm, null);

        addDetailsComponents(Arrays.asList(new SimpleEntry<>(i18n.getMessage("caption.tab.details"), entityDetails),
                new SimpleEntry<>(i18n.getMessage("caption.tab.description"), entityDescription),
                new SimpleEntry<>(i18n.getMessage("caption.logs.tab"), logDetails),
                new SimpleEntry<>(i18n.getMessage("caption.metadata"), swmMetadataLayout)));
    }

    @Override
    protected String getTabSheetId() {
        return UIComponentIdProvider.DIST_SW_MODULE_DETAILS_TABSHEET_ID;
    }

    @Override
    protected List<ProxyKeyValueDetails> getEntityDetails(final ProxySoftwareModule entity) {
        return Arrays.asList(
                new ProxyKeyValueDetails(UIComponentIdProvider.DETAILS_VENDOR_LABEL_ID,
                        i18n.getMessage("label.dist.details.vendor"), entity.getVendor()),
                new ProxyKeyValueDetails(UIComponentIdProvider.DETAILS_TYPE_LABEL_ID,
                        i18n.getMessage("label.dist.details.type"), entity.getType().getName()),
                new ProxyKeyValueDetails(UIComponentIdProvider.SWM_DTLS_MAX_ASSIGN,
                        i18n.getMessage("label.assigned.type"),
                        entity.getType().getMaxAssignments() == 1 ? i18n.getMessage("label.singleAssign.type")
                                : i18n.getMessage("label.multiAssign.type")));
    }

    @Override
    protected String getDetailsDescriptionId() {
        return UIComponentIdProvider.SM_DETAILS_DESCRIPTION_LABEL_ID;
    }

    @Override
    protected String getLogLabelIdPrefix() {
        // TODO: fix with constant
        return "sm.";
    }

    private void showMetadataDetails(final String metadataKey) {
        // TODO: adapt after popup refactoring
        softwareModuleManagement.get(binder.getBean().getId()).ifPresent(sm -> {
            final SwMetadataPopupLayout swMetadataPopupLayout = new SwMetadataPopupLayout(i18n, uiNotification,
                    eventBus, softwareModuleManagement, entityFactory, permChecker);
            UI.getCurrent().addWindow(swMetadataPopupLayout.getWindow(sm, metadataKey));
        });
    }

    private List<SoftwareModuleMetadata> getSmMetaData(final ProxySoftwareModule sm) {
        return softwareModuleManagement
                .findMetaDataBySoftwareModuleId(PageRequest.of(0, MetadataDetailsGrid.MAX_METADATA_QUERY), sm.getId())
                .getContent();
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final SoftwareModuleEvent softwareModuleEvent) {
        onBaseEntityEvent(softwareModuleEvent);
    }
}
