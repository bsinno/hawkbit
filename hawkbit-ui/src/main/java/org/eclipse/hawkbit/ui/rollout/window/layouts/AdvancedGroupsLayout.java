/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.layouts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.QuotaManagement;
import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.builder.RolloutGroupCreate;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.repository.model.RolloutGroupsValidation;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterQueryDataProvider;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorderWithIcon;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFuture;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.UI;

/**
 * Define groups for a Rollout
 */
public class AdvancedGroupsLayout extends GridLayout {
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE_ROLLOUT_MAX_GROUP_SIZE_EXCEEDED = "message.rollout.max.group.size.exceeded.advanced";

    private final VaadinMessageSource i18n;
    private final transient EntityFactory entityFactory;
    private final transient TargetFilterQueryManagement targetFilterQueryManagement;
    private final transient RolloutManagement rolloutManagement;
    private final transient QuotaManagement quotaManagement;

    private final TargetFilterQueryDataProvider targetFilterQueryDataProvider;

    private String targetFilter;

    private final transient List<AdvancedGroupRow> groupRows;
    private transient List<RolloutGroupCreate> savedRolloutGroups;

    private transient ValidationListener validationListener;
    private transient RolloutGroupsValidation groupsValidation;
    private final AtomicInteger runningValidationsCounter;
    private ValidationStatus validationStatus = ValidationStatus.VALID;

    public AdvancedGroupsLayout(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final RolloutManagement rolloutManagement, final TargetFilterQueryManagement targetFilterQueryManagement,
            final QuotaManagement quotaManagement, final TargetFilterQueryDataProvider targetFilterQueryDataProvider) {
        this.i18n = i18n;
        this.entityFactory = entityFactory;
        this.rolloutManagement = rolloutManagement;
        this.quotaManagement = quotaManagement;
        this.targetFilterQueryManagement = targetFilterQueryManagement;
        this.targetFilterQueryDataProvider = targetFilterQueryDataProvider;

        this.groupRows = new ArrayList<>(10);
        this.runningValidationsCounter = new AtomicInteger(0);

        buildLayout();
    }

    private void buildLayout() {
        setMargin(false);
        setSpacing(true);
        setSizeUndefined();
        setRows(3);
        setColumns(6);
        setStyleName("marginTop");

        addComponent(SPUIComponentProvider.getLabelByMsgKey(i18n, "caption.rollout.group.definition.desc"), 0, 0, 5, 0);

        final int headerRow = 1;
        addComponent(SPUIComponentProvider.getLabelByMsgKey(i18n, "header.name"), 0, headerRow);
        addComponent(SPUIComponentProvider.getLabelByMsgKey(i18n, "header.target.filter.query"), 1, headerRow);
        addComponent(SPUIComponentProvider.getLabelByMsgKey(i18n, "header.target.percentage"), 2, headerRow);
        addComponent(SPUIComponentProvider.getLabelByMsgKey(i18n, "header.rolloutgroup.threshold"), 3, headerRow);
        addComponent(SPUIComponentProvider.getLabelByMsgKey(i18n, "header.rolloutgroup.threshold.error"), 4, headerRow);

        addComponent(createAddButton(), 0, 2, 5, 2);

    }

    private Button createAddButton() {
        final Button button = SPUIComponentProvider.getButton(UIComponentIdProvider.ROLLOUT_GROUP_ADD_ID,
                i18n.getMessage("button.rollout.add.group"), "", "", true, VaadinIcons.PLUS,
                SPUIButtonStyleNoBorderWithIcon.class);
        button.setSizeUndefined();
        button.addStyleName("default-color");
        button.setEnabled(true);
        button.setVisible(true);
        button.addClickListener(event -> addGroupRowAndValidate());
        return button;

    }

    public void addGroupRowAndValidate() {
        final AdvancedGroupRow groupRow = addGroupRow();
        groupRow.populateWithDefaults();
        groupRow.addStatusChangeListener(event -> updateValidation());

        updateValidation();
    }

    private AdvancedGroupRow addGroupRow() {
        final AdvancedGroupRow groupRow = new AdvancedGroupRow(i18n, entityFactory, targetFilterQueryManagement,
                targetFilterQueryDataProvider, groupRows.size() + 1);
        groupRows.add(groupRow);

        addRowToLayout(groupRow);

        return groupRow;
    }

    private void addRowToLayout(final AdvancedGroupRow groupRow) {
        final int index = getRows() - 1;
        insertRow(index);

        groupRow.addRowToLayout(this, index);

        addComponent(createRemoveButton(groupRow, index), 5, index);
    }

    private Button createRemoveButton(final AdvancedGroupRow groupRow, final int index) {
        final Button button = SPUIComponentProvider.getButton(
                UIComponentIdProvider.ROLLOUT_GROUP_REMOVE_ID + "." + index, "", "", "", true, VaadinIcons.MINUS,
                SPUIButtonStyleNoBorderWithIcon.class);
        button.setSizeUndefined();
        button.addStyleName("default-color");

        button.addClickListener(event -> removeGroupRow(groupRow, index));

        return button;
    }

    private void removeGroupRow(final AdvancedGroupRow groupRow, final int index) {
        removeRow(index);
        groupRows.remove(groupRow);

        updateValidation();
    }

    private void updateValidation() {
        validationStatus = ValidationStatus.VALID;
        if (isValid()) {
            setValidationStatus(ValidationStatus.LOADING);
            savedRolloutGroups = getGroupsFromRows();
            validateRemainingTargets();
        } else {
            setValidationStatus(ValidationStatus.INVALID);
        }
    }

    /**
     * @return whether the groups definition form is valid
     */
    public boolean isValid() {
        if (groupRows.isEmpty() || validationStatus != ValidationStatus.VALID) {
            return false;
        }
        return groupRows.stream().allMatch(AdvancedGroupRow::isValid);
    }

    private void setValidationStatus(final ValidationStatus status) {
        validationStatus = status;
        if (validationListener != null) {
            validationListener.validation(status);
        }
    }

    private List<RolloutGroupCreate> getGroupsFromRows() {
        return groupRows.stream().map(AdvancedGroupRow::getGroupEntity).collect(Collectors.toList());
    }

    private void validateRemainingTargets() {
        resetErrors();

        if (StringUtils.isEmpty(targetFilter)) {
            return;
        }

        if (runningValidationsCounter.incrementAndGet() == 1) {
            final ListenableFuture<RolloutGroupsValidation> validateTargetsInGroups = rolloutManagement
                    .validateTargetsInGroups(savedRolloutGroups, targetFilter, System.currentTimeMillis());
            final UI ui = UI.getCurrent();
            validateTargetsInGroups.addCallback(validation -> ui.access(() -> setGroupsValidation(validation)),
                    throwable -> ui.access(() -> setGroupsValidation(null)));
            return;
        }

        runningValidationsCounter.incrementAndGet();
    }

    private void resetErrors() {
        groupRows.forEach(AdvancedGroupRow::resetError);
    }

    /**
     * YOU SHOULD NOT CALL THIS METHOD MANUALLY. It's only for the callback.
     * Only 1 runningValidation should be executed. If this runningValidation is
     * done, then this method is called. Maybe then a new runningValidation is
     * executed.
     * 
     */
    private void setGroupsValidation(final RolloutGroupsValidation validation) {
        final int runningValidation = runningValidationsCounter.getAndSet(0);
        if (runningValidation > 1) {
            validateRemainingTargets();
            return;
        }
        groupsValidation = validation;

        if (groupsValidation != null && groupsValidation.isValid() && validationStatus != ValidationStatus.INVALID) {
            setValidationStatus(ValidationStatus.VALID);
        } else {
            final AdvancedGroupRow lastRow = groupRows.get(groupRows.size() - 1);
            lastRow.setError(i18n.getMessage("message.rollout.remaining.targets.error"));
            setValidationStatus(ValidationStatus.INVALID);
        }

        validateSingleGroups();
    }

    private void validateSingleGroups() {
        if (groupsValidation == null || CollectionUtils.isEmpty(groupsValidation.getTargetsPerGroup())) {
            return;
        }

        final int maxTargets = quotaManagement.getMaxTargetsPerRolloutGroup();
        final boolean hasRemainingTargetsError = validationStatus == ValidationStatus.INVALID;
        final int lastIdx = groupRows.size() - 1;
        for (int i = 0; i < groupRows.size(); ++i) {
            // do not mask the 'remaining targets' error
            if (hasRemainingTargetsError && (i == lastIdx)) {
                continue;
            }

            final Long count = groupsValidation.getTargetsPerGroup().get(i);
            if (count != null && count > maxTargets) {
                final AdvancedGroupRow row = groupRows.get(i);
                row.setError(i18n.getMessage(MESSAGE_ROLLOUT_MAX_GROUP_SIZE_EXCEEDED, maxTargets));
                setValidationStatus(ValidationStatus.INVALID);
            }
        }
    }

    /**
     * @param targetFilter
     *            the target filter which is required for verification
     */
    public void setTargetFilter(final String targetFilter) {
        this.targetFilter = targetFilter;

        updateValidation();
    }

    public void setValidationListener(final ValidationListener validationListener) {
        this.validationListener = validationListener;
    }

    /**
     * Populate groups by rollout groups
     *
     * @param groups
     *            the rollout groups
     */
    public void populateByRolloutGroups(final List<RolloutGroup> groups) {
        if (CollectionUtils.isEmpty(groups)) {
            return;
        }

        removeAllRows();

        for (final RolloutGroup group : groups) {
            final AdvancedGroupRow groupRow = addGroupRow();
            groupRow.populateByGroup(group);
            groupRow.addStatusChangeListener(event -> updateValidation());
        }

        updateValidation();
    }

    private void removeAllRows() {
        for (int i = getRows() - 2; i > 1; i--) {
            removeRow(i);
        }

        groupRows.clear();
    }

    public List<RolloutGroupCreate> getSavedRolloutGroupDefinitions() {
        return savedRolloutGroups;
    }

    /**
     * @return the validation instance if was already validated
     */
    public RolloutGroupsValidation getGroupsValidation() {
        return groupsValidation;
    }

    /**
     * Status of the groups validation
     */
    public enum ValidationStatus {
        VALID, INVALID, LOADING
    }

    /**
     * Implement the interface and set the instance with setValidationListener
     * to receive updates for any changes within the group rows.
     */
    @FunctionalInterface
    public interface ValidationListener {
        /**
         * Is called after user input
         * 
         * @param isValid
         *            whether the input of the group rows is valid
         */
        void validation(ValidationStatus isValid);
    }
}
