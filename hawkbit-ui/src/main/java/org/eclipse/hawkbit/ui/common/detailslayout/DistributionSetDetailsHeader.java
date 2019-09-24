/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.Optional;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.distributions.dstable.DsMetadataPopupLayout;
import org.eclipse.hawkbit.ui.management.dstable.DistributionAddUpdateWindowLayout;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class DistributionSetDetailsHeader extends DetailsHeader<ProxyDistributionSet> {
    private static final long serialVersionUID = 1L;

    private final transient EntityFactory entityFactory;
    private final transient DistributionSetManagement distributionSetManagement;

    private final DistributionAddUpdateWindowLayout distributionAddUpdateWindowLayout;

    public DistributionSetDetailsHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final UINotification uiNotification, final EntityFactory entityFactory,
            final DistributionSetManagement distributionSetManagement,
            final DistributionAddUpdateWindowLayout distributionAddUpdateWindowLayout) {
        super(i18n, permChecker, eventBus, uiNotification);

        this.entityFactory = entityFactory;
        this.distributionSetManagement = distributionSetManagement;
        this.distributionAddUpdateWindowLayout = distributionAddUpdateWindowLayout;

        restoreHeaderState();
        buildHeader();
    }

    @Override
    protected String getEntityName(final ProxyDistributionSet entity) {
        return entity.getNameVersion();
    }

    @Override
    protected boolean hasEditPermission() {
        return permChecker.hasUpdateRepositoryPermission();
    }

    @Override
    protected String getEntityType() {
        return i18n.getMessage("distribution.details.header");
    }

    @Override
    protected String getDetailsHeaderCaptionId() {
        return UIComponentIdProvider.DISTRIBUTION_DETAILS_HEADER_LABEL_ID;
    }

    @Override
    protected String getEditIconId() {
        return UIComponentIdProvider.DS_EDIT_BUTTON;
    }

    @Override
    protected void onEdit() {
        final Window newDistWindow = distributionAddUpdateWindowLayout
                .getWindowForUpdateDistributionSet(selectedEntity.getId());
        UI.getCurrent().addWindow(newDistWindow);
        newDistWindow.setVisible(Boolean.TRUE);
    }

    @Override
    protected String getMetaDataIconId() {
        return UIComponentIdProvider.DS_METADATA_BUTTON;
    }

    @Override
    protected void showMetaData() {
        final Optional<DistributionSet> ds = distributionSetManagement.get(selectedEntity.getId());
        if (!ds.isPresent()) {
            uiNotification.displayWarning(i18n.getMessage("distributionset.not.exists"));
            return;
        }

        final DsMetadataPopupLayout dsMetadataPopupLayout = new DsMetadataPopupLayout(i18n, uiNotification, eventBus,
                distributionSetManagement, entityFactory, permChecker);
        UI.getCurrent().addWindow(dsMetadataPopupLayout.getWindow(ds.get(), null));
    }

}
