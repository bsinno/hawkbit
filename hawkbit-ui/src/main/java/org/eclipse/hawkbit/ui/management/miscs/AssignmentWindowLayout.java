/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.miscs;

import java.util.function.Consumer;

import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAssignmentWindow;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;

/**
 * Layout for target to distribution set assignment
 */
public class AssignmentWindowLayout extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    private final Binder<ProxyAssignmentWindow> proxyAssignmentBinder;
    private final AssignmentWindowLayoutComponentBuilder componentBuilder;

    private final ActionTypeOptionGroupAssignmentLayout actionTypeLayout;
    private final CheckBox maintenanceWindowToggle;
    private final MaintenanceWindowLayout maintenanceWindowLayout;
    private final Link maintenanceHelpLink;

    public AssignmentWindowLayout(final VaadinMessageSource i18n, final UiProperties uiProperties) {
        this.proxyAssignmentBinder = new Binder<>();
        this.componentBuilder = new AssignmentWindowLayoutComponentBuilder(i18n);

        this.actionTypeLayout = componentBuilder.createActionTypeOptionGroupLayout(proxyAssignmentBinder);
        this.maintenanceWindowToggle = componentBuilder.createEnableMaintenanceWindowToggle(proxyAssignmentBinder);
        this.maintenanceWindowLayout = new MaintenanceWindowLayout(i18n,
                componentBuilder.createMaintenanceSchedule(proxyAssignmentBinder),
                componentBuilder.createMaintenanceDuration(proxyAssignmentBinder),
                componentBuilder.createMaintenanceTimeZoneCombo(proxyAssignmentBinder),
                componentBuilder.createMaintenanceScheduleTranslator());
        this.maintenanceHelpLink = componentBuilder.createMaintenanceHelpLink(uiProperties);

        initLayout();
        buildLayout();
        addValueChangeListeners();
    }

    private void initLayout() {
        setSizeFull();
        setMargin(false);
        setSpacing(false);
    }

    private void buildLayout() {
        addComponent(actionTypeLayout);

        final HorizontalLayout maintenanceWindowToggleLayout = new HorizontalLayout();
        maintenanceWindowToggleLayout.addComponent(maintenanceWindowToggle);
        maintenanceWindowToggleLayout.addComponent(maintenanceHelpLink);
        addComponent(maintenanceWindowToggleLayout);

        maintenanceWindowLayout.setVisible(false);
        maintenanceWindowLayout.setEnabled(false);
        addComponent(maintenanceWindowLayout);
    }

    private void addValueChangeListeners() {
        maintenanceWindowToggle.addValueChangeListener(event -> {
            final boolean isMaintenanceWindowEnabled = proxyAssignmentBinder.getBean().isMaintenanceWindowEnabled();

            maintenanceWindowLayout.setVisible(isMaintenanceWindowEnabled);
            maintenanceWindowLayout.setEnabled(isMaintenanceWindowEnabled);
            // TODO: check if needed - alternatively adapt
            // addValidationListener()
            // saveButtonToggle.accept(!isMaintenanceWindowEnabled);
            clearMaintenanceFields();
        });

        actionTypeLayout.getActionTypeOptionGroup().addValueChangeListener(event -> {
            if (event.getValue() == ActionType.DOWNLOAD_ONLY) {
                maintenanceWindowToggle.setValue(false);
                maintenanceWindowToggle.setEnabled(false);
                maintenanceHelpLink.setEnabled(false);
            } else {
                maintenanceWindowToggle.setEnabled(true);
                maintenanceHelpLink.setEnabled(true);
            }
        });
    }

    private void clearMaintenanceFields() {
        final ProxyAssignmentWindow currentAssignmentBean = proxyAssignmentBinder.getBean();
        if (currentAssignmentBean == null) {
            return;
        }

        // TODO: check if we should set "" instead of null
        currentAssignmentBean.setMaintenanceSchedule(null);
        currentAssignmentBean.setMaintenanceDuration(null);
        currentAssignmentBean.setMaintenanceTimeZone(SPDateTimeUtil.getClientTimeZoneOffsetId());

        proxyAssignmentBinder.setBean(currentAssignmentBean);
    }

    public Binder<ProxyAssignmentWindow> getProxyAssignmentBinder() {
        return proxyAssignmentBinder;
    }

    public void addValidationListener(final Consumer<Boolean> validationCallback) {
        proxyAssignmentBinder.addStatusChangeListener(event -> validationCallback.accept(event.getBinder().isValid()));
    }
}
