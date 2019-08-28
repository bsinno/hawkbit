/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import java.util.function.Consumer;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.ui.common.CommonDialogWindowV7;
import org.eclipse.hawkbit.ui.common.builder.WindowBuilderV7;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetStatelessDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorderWithIcon;
import org.eclipse.hawkbit.ui.filtermanagement.event.CustomFilterUIEvent;
import org.eclipse.hawkbit.ui.management.miscs.ActionTypeOptionGroupAutoAssignmentLayout;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Creates a dialog window to select the distribution set for a target filter
 * query.
 */
public class DistributionSetSelectWindow implements CommonDialogWindowV7.SaveDialogCloseListener {

    private final VaadinMessageSource i18n;
    private final EventBus.UIEventBus eventBus;
    private final TargetManagement targetManagement;
    private final TargetFilterQueryManagement targetFilterQueryManagement;
    private final DistributionSetStatelessDataProvider autoDsComboDataProvider;
    private final DistributionSetToProxyDistributionMapper mapper;

    private CheckBox checkBox;
    private ActionTypeOptionGroupAutoAssignmentLayout actionTypeOptionGroupLayout;
    private ComboBox<ProxyDistributionSet> autoDsCombo;
    private Long tfqId;

    DistributionSetSelectWindow(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final TargetManagement targetManagement, final TargetFilterQueryManagement targetFilterQueryManagement,
            final DistributionSetManagement distributionSetManagement) {
        this.i18n = i18n;
        this.eventBus = eventBus;
        this.targetManagement = targetManagement;
        this.targetFilterQueryManagement = targetFilterQueryManagement;

        this.mapper = new DistributionSetToProxyDistributionMapper();
        this.autoDsComboDataProvider = new DistributionSetStatelessDataProvider(distributionSetManagement, mapper);
    }

    private VerticalLayout initView() {
        final Label label = new Label(i18n.getMessage(UIMessageIdProvider.LABEL_AUTO_ASSIGNMENT_DESC));

        checkBox = new CheckBox(i18n.getMessage(UIMessageIdProvider.LABEL_AUTO_ASSIGNMENT_ENABLE));
        checkBox.setId(UIComponentIdProvider.DIST_SET_SELECT_ENABLE_ID);

        checkBox.addValueChangeListener(event -> switchAutoAssignmentInputsVisibility(event.getValue()));

        actionTypeOptionGroupLayout = new ActionTypeOptionGroupAutoAssignmentLayout(i18n);
        autoDsCombo = initAutoDsCombo();

        final VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addComponent(label);
        verticalLayout.addComponent(checkBox);
        verticalLayout.addComponent(actionTypeOptionGroupLayout);
        verticalLayout.addComponent(autoDsCombo);

        return verticalLayout;
    }

    private ComboBox<ProxyDistributionSet> initAutoDsCombo() {
        final ComboBox<ProxyDistributionSet> combo = new ComboBox<>(
                i18n.getMessage(UIMessageIdProvider.HEADER_DISTRIBUTION_SET));

        combo.setId(UIComponentIdProvider.DIST_SET_SELECT_COMBO_ID);
        combo.setSizeFull();
        combo.setReadOnly(true);
        combo.setItemCaptionGenerator(ProxyDistributionSet::getNameVersion);
        combo.setDataProvider(autoDsComboDataProvider);

        return combo;
    }

    /**
     * Shows a distribution set select window for the given target filter query
     *
     * @param tfqId
     *            target filter query id
     */
    public void showForTargetFilter(final Long tfqId) {
        this.tfqId = tfqId;
        final TargetFilterQuery tfq = targetFilterQueryManagement.get(tfqId)
                .orElseThrow(() -> new EntityNotFoundException(TargetFilterQuery.class, tfqId));

        final VerticalLayout verticalLayout = initView();

        final DistributionSet distributionSet = tfq.getAutoAssignDistributionSet();
        final ActionType actionType = tfq.getAutoAssignActionType();

        setInitialControlValues(distributionSet, actionType);

        // build window after values are set to view elements
        final CommonDialogWindowV7 window = new WindowBuilderV7(SPUIDefinitions.CREATE_UPDATE_WINDOW)
                .caption(i18n.getMessage(UIMessageIdProvider.CAPTION_SELECT_AUTO_ASSIGN_DS)).content(verticalLayout)
                .layout(verticalLayout).i18n(i18n).saveDialogCloseListener(this).buildCommonDialogWindow();
        window.setId(UIComponentIdProvider.DIST_SET_SELECT_WINDOW_ID);

        window.setWidth(40.0F, Sizeable.Unit.PERCENTAGE);
        UI.getCurrent().addWindow(window);
        window.setVisible(true);
    }

    private void setInitialControlValues(final DistributionSet distributionSet, final ActionType actionType) {
        checkBox.setValue(distributionSet != null);
        switchAutoAssignmentInputsVisibility(distributionSet != null);

        final ActionType actionTypeToSet = actionType == null ? ActionType.FORCED : actionType;
        actionTypeOptionGroupLayout.getActionTypeOptionGroup().setSelectedItem(actionTypeToSet);

        if (distributionSet != null) {
            // TODO: check if we need to apply filter before
            autoDsCombo.setSelectedItem(mapper.map(distributionSet));
        }
    }

    private void switchAutoAssignmentInputsVisibility(final boolean autoAssignmentEnabled) {
        actionTypeOptionGroupLayout.setVisible(autoAssignmentEnabled);

        autoDsCombo.setVisible(autoAssignmentEnabled);
        autoDsCombo.setEnabled(autoAssignmentEnabled);
    }

    /**
     * Is triggered when the save button is clicked
     *
     * @return whether the click should be allowed
     */
    @Override
    public boolean canWindowSaveOrUpdate() {
        return isFormularValid();
    }

    private boolean isFormularValid() {
        return isAutoAssignmentDisabled() || isAutoAssignmentEnabledAndDistributionSetSelected();
    }

    private boolean isAutoAssignmentEnabledAndDistributionSetSelected() {
        return checkBox.getValue() && autoDsCombo.getValue() != null;
    }

    private boolean isAutoAssignmentDisabled() {
        return !checkBox.getValue();
    }

    /**
     * Is called when the new value should be saved after the save button has
     * been clicked
     */
    @Override
    public void saveOrUpdate() {
        if (checkBox.getValue() && autoDsCombo.getValue() != null) {
            final ActionType autoAssignActionType = actionTypeOptionGroupLayout.getActionTypeOptionGroup().getValue();
            updateTargetFilterQueryDS(tfqId, autoDsCombo.getValue().getId(), autoAssignActionType);
        } else if (!checkBox.getValue()) {
            updateTargetFilterQueryDS(tfqId, null, null);
        }
    }

    private void updateTargetFilterQueryDS(final Long targetFilterQueryId, final Long dsId,
            final ActionType actionType) {
        final TargetFilterQuery tfq = targetFilterQueryManagement.get(targetFilterQueryId)
                .orElseThrow(() -> new EntityNotFoundException(TargetFilterQuery.class, targetFilterQueryId));

        if (dsId != null) {
            confirmWithConsequencesDialog(tfq, dsId, actionType);
        } else {
            targetFilterQueryManagement.updateAutoAssignDS(targetFilterQueryId, null);
            eventBus.publish(this, CustomFilterUIEvent.UPDATED_TARGET_FILTER_QUERY);
        }
    }

    private void confirmWithConsequencesDialog(final TargetFilterQuery tfq, final Long dsId,
            final ActionType actionType) {
        final ConfirmConsequencesDialog dialog = new ConfirmConsequencesDialog(tfq, dsId, accepted -> {
            if (accepted) {
                targetFilterQueryManagement.updateAutoAssignDSWithActionType(tfq.getId(), dsId, actionType);
                eventBus.publish(this, CustomFilterUIEvent.UPDATED_TARGET_FILTER_QUERY);
            }
        });

        dialog.setWidth(40.0F, Sizeable.Unit.PERCENTAGE);

        UI.getCurrent().addWindow(dialog);
        dialog.setVisible(true);
    }

    /**
     * A dialog that displays how many targets will be assigned immediately with
     * the
     */
    private class ConfirmConsequencesDialog extends Window implements Button.ClickListener {
        private static final long serialVersionUID = 1L;

        private final TargetFilterQuery targetFilterQuery;
        private final Long distributionSetId;
        private final transient Consumer<Boolean> callback;

        public ConfirmConsequencesDialog(final TargetFilterQuery targetFilterQuery, final Long dsId,
                final Consumer<Boolean> callback) {
            super(i18n.getMessage(UIMessageIdProvider.CAPTION_CONFIRM_AUTO_ASSIGN_CONSEQUENCES));

            this.callback = callback;
            this.targetFilterQuery = targetFilterQuery;
            this.distributionSetId = dsId;

            init();
        }

        private void init() {
            setId(UIComponentIdProvider.DIST_SET_SELECT_CONS_WINDOW_ID);
            setModal(true);
            setResizable(false);

            final VerticalLayout layout = new VerticalLayout();
            layout.setSpacing(true);
            layout.setMargin(true);
            setContent(layout);

            final Long targetsCount = targetManagement.countByRsqlAndNonDS(distributionSetId,
                    targetFilterQuery.getQuery());
            Label mainTextLabel;
            if (targetsCount == 0) {
                mainTextLabel = new Label(
                        i18n.getMessage(UIMessageIdProvider.MESSAGE_CONFIRM_AUTO_ASSIGN_CONSEQUENCES_NONE));
            } else {
                mainTextLabel = new Label(i18n
                        .getMessage(UIMessageIdProvider.MESSAGE_CONFIRM_AUTO_ASSIGN_CONSEQUENCES_TEXT, targetsCount));
            }

            layout.addComponent(mainTextLabel);

            final HorizontalLayout buttonsLayout = new HorizontalLayout();
            buttonsLayout.setSizeFull();
            buttonsLayout.setSpacing(true);
            buttonsLayout.addStyleName("actionButtonsMargin");
            layout.addComponent(buttonsLayout);

            final Button okButton = SPUIComponentProvider.getButton(UIComponentIdProvider.SAVE_BUTTON,
                    i18n.getMessage(UIMessageIdProvider.BUTTON_OK), "", "", true, FontAwesome.SAVE,
                    SPUIButtonStyleNoBorderWithIcon.class);
            okButton.setSizeUndefined();
            okButton.addStyleName("default-color");
            okButton.addClickListener(this);
            buttonsLayout.addComponent(okButton);
            buttonsLayout.setComponentAlignment(okButton, Alignment.MIDDLE_RIGHT);
            buttonsLayout.setExpandRatio(okButton, 1.0F);

            final Button cancelButton = SPUIComponentProvider.getButton(UIComponentIdProvider.CANCEL_BUTTON,
                    i18n.getMessage(UIMessageIdProvider.BUTTON_CANCEL), "", "", true, FontAwesome.TIMES,
                    SPUIButtonStyleNoBorderWithIcon.class);
            cancelButton.setSizeUndefined();
            cancelButton.addStyleName("default-color");
            cancelButton.addClickListener(this);
            buttonsLayout.addComponent(cancelButton);
            buttonsLayout.setComponentAlignment(cancelButton, Alignment.MIDDLE_LEFT);
            buttonsLayout.setExpandRatio(cancelButton, 1.0F);
        }

        @Override
        public void buttonClick(final Button.ClickEvent event) {
            if (event.getButton().getId().equals(UIComponentIdProvider.SAVE_BUTTON)) {
                callback.accept(true);
            } else {
                callback.accept(false);
            }

            close();
        }
    }
}
