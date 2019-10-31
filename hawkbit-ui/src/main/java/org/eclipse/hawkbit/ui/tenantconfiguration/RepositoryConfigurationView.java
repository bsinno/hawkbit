/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.tenantconfiguration;

import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.tenantconfiguration.generic.BooleanConfigurationItem;
import org.eclipse.hawkbit.ui.tenantconfiguration.repository.ActionAutocleanupConfigurationItem;
import org.eclipse.hawkbit.ui.tenantconfiguration.repository.ActionAutocloseConfigurationItem;
import org.eclipse.hawkbit.ui.tenantconfiguration.repository.MultiAssignmentsConfigurationItem;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.HasValue;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * View to configure the authentication mode.
 */
public class RepositoryConfigurationView extends BaseConfigurationView
        implements ConfigurationGroup, ConfigurationItem.ConfigurationItemChangeListener, HasValue.ValueChangeListener {

    private static final String DIST_CHECKBOX_STYLE = "dist-checkbox-style";

    private static final long serialVersionUID = 1L;

    private final VaadinMessageSource i18n;

    private final UiProperties uiProperties;

    private final ActionAutocloseConfigurationItem actionAutocloseConfigurationItem;

    private final ActionAutocleanupConfigurationItem actionAutocleanupConfigurationItem;

    private final MultiAssignmentsConfigurationItem multiAssignmentsConfigurationItem;

    private CheckBox actionAutocloseCheckBox;

    private CheckBox actionAutocleanupCheckBox;

    private CheckBox multiAssignmentsCheckBox;

    RepositoryConfigurationView(final VaadinMessageSource i18n,
            final TenantConfigurationManagement tenantConfigurationManagement, final UiProperties uiProperties) {
        this.i18n = i18n;
        this.uiProperties = uiProperties;
        this.actionAutocloseConfigurationItem = new ActionAutocloseConfigurationItem(tenantConfigurationManagement,
                i18n);
        this.actionAutocleanupConfigurationItem = new ActionAutocleanupConfigurationItem(tenantConfigurationManagement,
                i18n);
        this.multiAssignmentsConfigurationItem = new MultiAssignmentsConfigurationItem(tenantConfigurationManagement,
                i18n);

        init();
    }

    @Override
    public void valueChange(HasValue.ValueChangeEvent event) {
        if (!(event.getComponent() instanceof CheckBox)) {
            return;
        }

        notifyConfigurationChanged();

        final CheckBox checkBox = (CheckBox) event.getComponent();
        BooleanConfigurationItem configurationItem;

        if (actionAutocloseCheckBox.equals(checkBox)) {
            configurationItem = actionAutocloseConfigurationItem;
        } else if (actionAutocleanupCheckBox.equals(checkBox)) {
            configurationItem = actionAutocleanupConfigurationItem;
        } else if (multiAssignmentsCheckBox.equals(checkBox)) {
            configurationItem = multiAssignmentsConfigurationItem;
            actionAutocloseCheckBox.setEnabled(!checkBox.getValue());
            actionAutocloseConfigurationItem.setEnabled(!checkBox.getValue());
        } else {
            return;
        }

        if (checkBox.getValue()) {
            configurationItem.configEnable();
        } else {
            configurationItem.configDisable();
        }
    }

    private void init() {

        final Panel rootPanel = new Panel();
        rootPanel.setSizeFull();

        rootPanel.addStyleName("config-panel");

        final VerticalLayout vLayout = new VerticalLayout();
        vLayout.setMargin(true);
        vLayout.setSizeFull();

        final Label header = new Label(i18n.getMessage("configuration.repository.title"));
        header.addStyleName("config-panel-header");
        vLayout.addComponent(header);

        final GridLayout gridLayout = new GridLayout(3, 3);
        gridLayout.setSpacing(true);

        gridLayout.setColumnExpandRatio(1, 1.0F);
        gridLayout.setSizeFull();

        final boolean isMultiAssignmentsEnabled = multiAssignmentsConfigurationItem.isConfigEnabled();

        actionAutocloseCheckBox = new CheckBox();
        actionAutocloseCheckBox.setStyleName(DIST_CHECKBOX_STYLE);
        actionAutocloseCheckBox.setId(UIComponentIdProvider.REPOSITORY_ACTIONS_AUTOCLOSE_CHECKBOX);
        actionAutocloseCheckBox.setEnabled(!isMultiAssignmentsEnabled);
        actionAutocloseConfigurationItem.setEnabled(!isMultiAssignmentsEnabled);
        actionAutocloseCheckBox.setValue(actionAutocloseConfigurationItem.isConfigEnabled());
        actionAutocloseCheckBox.addValueChangeListener(this);
        actionAutocloseConfigurationItem.addChangeListener(this);
        gridLayout.addComponent(actionAutocloseCheckBox, 0, 0);
        gridLayout.addComponent(actionAutocloseConfigurationItem, 1, 0);

        multiAssignmentsCheckBox = new CheckBox();
        multiAssignmentsCheckBox.setStyleName(DIST_CHECKBOX_STYLE);
        multiAssignmentsCheckBox.setId(UIComponentIdProvider.REPOSITORY_MULTI_ASSIGNMENTS_CHECKBOX);
        multiAssignmentsCheckBox.setValue(multiAssignmentsConfigurationItem.isConfigEnabled());
        multiAssignmentsCheckBox.addValueChangeListener(this);
        multiAssignmentsCheckBox.setEnabled(!isMultiAssignmentsEnabled);
        multiAssignmentsConfigurationItem.setEnabled(!isMultiAssignmentsEnabled);
        multiAssignmentsConfigurationItem.addChangeListener(this);
        gridLayout.addComponent(multiAssignmentsCheckBox, 0, 1);
        gridLayout.addComponent(multiAssignmentsConfigurationItem, 1, 1);

        actionAutocleanupCheckBox = new CheckBox();
        actionAutocleanupCheckBox.setStyleName(DIST_CHECKBOX_STYLE);
        actionAutocleanupCheckBox.setId(UIComponentIdProvider.REPOSITORY_ACTIONS_AUTOCLEANUP_CHECKBOX);
        actionAutocleanupCheckBox.setValue(actionAutocleanupConfigurationItem.isConfigEnabled());
        actionAutocleanupCheckBox.addValueChangeListener(this);
        actionAutocleanupConfigurationItem.addChangeListener(this);
        gridLayout.addComponent(actionAutocleanupCheckBox, 0, 2);
        gridLayout.addComponent(actionAutocleanupConfigurationItem, 1, 2);

        final Link linkToProvisioningHelp = SPUIComponentProvider.getHelpLink(i18n,
                uiProperties.getLinks().getDocumentation().getProvisioningStateMachine());
        gridLayout.addComponent(linkToProvisioningHelp, 2, 2);
        gridLayout.setComponentAlignment(linkToProvisioningHelp, Alignment.BOTTOM_RIGHT);

        vLayout.addComponent(gridLayout);
        rootPanel.setContent(vLayout);
        setCompositionRoot(rootPanel);
    }

    @Override
    public void save() {
        actionAutocloseConfigurationItem.save();
        actionAutocleanupConfigurationItem.save();
        multiAssignmentsConfigurationItem.save();

        final boolean isMultiAssignmentsEnabled = multiAssignmentsConfigurationItem.isConfigEnabled();
        multiAssignmentsCheckBox.setEnabled(!isMultiAssignmentsEnabled);
        multiAssignmentsConfigurationItem.setEnabled(!isMultiAssignmentsEnabled);
    }

    @Override
    public boolean isUserInputValid() {
        return actionAutocloseConfigurationItem.isUserInputValid()
                && actionAutocleanupConfigurationItem.isUserInputValid()
                && multiAssignmentsConfigurationItem.isUserInputValid();
    }

    @Override
    public void undo() {
        multiAssignmentsConfigurationItem.undo();
        final boolean isMultiAssignmentsEnabled = multiAssignmentsConfigurationItem.isConfigEnabled();
        multiAssignmentsCheckBox.setValue(isMultiAssignmentsEnabled);
        actionAutocloseConfigurationItem.undo();
        actionAutocloseCheckBox.setValue(actionAutocloseConfigurationItem.isConfigEnabled());
        actionAutocloseCheckBox.setEnabled(!isMultiAssignmentsEnabled);
        actionAutocloseConfigurationItem.setEnabled(!isMultiAssignmentsEnabled);
        actionAutocleanupConfigurationItem.undo();
        actionAutocleanupCheckBox.setValue(actionAutocleanupConfigurationItem.isConfigEnabled());
    }

    @Override
    public void configurationHasChanged() {
        notifyConfigurationChanged();
    }

}
