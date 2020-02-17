/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.bulkupload;

import java.util.Optional;
import java.util.concurrent.Executor;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.builder.WindowBuilder;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Window;

public class BulkUploadWindowBuilder {
    private final VaadinMessageSource i18n;
    private final UIEventBus eventBus;
    private final SpPermissionChecker checker;
    private final UINotification uinotification;
    private final UiProperties uiproperties;
    private final Executor uiExecutor;
    private final TargetManagement targetManagement;
    private final DeploymentManagement deploymentManagement;
    private final TargetTagManagement tagManagement;
    private final DistributionSetManagement distributionSetManagement;
    private final EntityFactory entityFactory;

    private final TargetBulkUploadUiState targetBulkUploadUiState;

    private TargetBulkUpdateWindowLayout targetBulkUpdateWindowLayout;

    public BulkUploadWindowBuilder(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker checker, final UINotification uinotification, final UiProperties uiproperties,
            final Executor uiExecutor, final TargetManagement targetManagement,
            final DeploymentManagement deploymentManagement, final TargetTagManagement tagManagement,
            final DistributionSetManagement distributionSetManagement, final EntityFactory entityFactory,
            final TargetBulkUploadUiState targetBulkUploadUiState) {
        this.i18n = i18n;
        this.eventBus = eventBus;
        this.checker = checker;
        this.uinotification = uinotification;
        this.uiproperties = uiproperties;
        this.uiExecutor = uiExecutor;
        this.targetManagement = targetManagement;
        this.deploymentManagement = deploymentManagement;
        this.tagManagement = tagManagement;
        this.distributionSetManagement = distributionSetManagement;
        this.entityFactory = entityFactory;
        this.targetBulkUploadUiState = targetBulkUploadUiState;
    }

    public Window getWindowForTargetBulkUpload() {
        if (!targetBulkUploadUiState.isInProgress() || targetBulkUpdateWindowLayout == null) {
            targetBulkUpdateWindowLayout = new TargetBulkUpdateWindowLayout(i18n, eventBus, checker, uinotification,
                    targetManagement, deploymentManagement, tagManagement, distributionSetManagement, entityFactory,
                    uiproperties, uiExecutor, targetBulkUploadUiState);
        }

        final Window bulkUploadWindow = new WindowBuilder(SPUIDefinitions.CREATE_UPDATE_WINDOW).caption("")
                .content(targetBulkUpdateWindowLayout).buildWindow();
        bulkUploadWindow.addStyleName("bulk-upload-window");

        targetBulkUpdateWindowLayout.setCloseCallback(() -> closePopup(bulkUploadWindow));

        return bulkUploadWindow;
    }

    private void closePopup(final Window bulkUploadWindow) {
        if (!targetBulkUploadUiState.isInProgress() && targetBulkUpdateWindowLayout != null) {
            targetBulkUpdateWindowLayout.clearUiState();
            targetBulkUpdateWindowLayout = null;
        }
        bulkUploadWindow.close();
    }

    public void restoreState() {
        targetBulkUpdateWindowLayout = new TargetBulkUpdateWindowLayout(i18n, eventBus, checker, uinotification,
                targetManagement, deploymentManagement, tagManagement, distributionSetManagement, entityFactory,
                uiproperties, uiExecutor, targetBulkUploadUiState);
        targetBulkUpdateWindowLayout.restoreComponentsValue();
    }

    public Optional<TargetBulkUpdateWindowLayout> getLayout() {
        return Optional.ofNullable(targetBulkUpdateWindowLayout);
    }
}
