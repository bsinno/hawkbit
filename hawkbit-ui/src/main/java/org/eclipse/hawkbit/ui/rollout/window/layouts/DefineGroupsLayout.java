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
import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.builder.RolloutGroupCreate;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupErrorAction;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupErrorCondition;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupSuccessAction;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupSuccessCondition;
import org.eclipse.hawkbit.repository.model.RolloutGroupConditionBuilder;
import org.eclipse.hawkbit.repository.model.RolloutGroupConditions;
import org.eclipse.hawkbit.repository.model.RolloutGroupsValidation;
import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextAreaBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterQueryDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAdvancedRolloutGroupRow;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorderWithIcon;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFuture;

import com.vaadin.data.Binder;
import com.vaadin.data.converter.StringToFloatConverter;
import com.vaadin.data.validator.FloatRangeValidator;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Define groups for a Rollout
 */
public class DefineGroupsLayout extends GridLayout {

    private static final long serialVersionUID = 1L;

    private static final String MESSAGE_ROLLOUT_MAX_GROUP_SIZE_EXCEEDED = "message.rollout.max.group.size.exceeded.advanced";

    private final VaadinMessageSource i18n;

    private transient EntityFactory entityFactory;

    private transient RolloutManagement rolloutManagement;

    private transient RolloutGroupManagement rolloutGroupManagement;

    private final transient QuotaManagement quotaManagement;

    private transient TargetFilterQueryManagement targetFilterQueryManagement;

    private String defaultTriggerThreshold;

    private String defaultErrorThreshold;

    private String targetFilter;

    private transient List<GroupRow> groupRows;

    private int groupsCount;

    private transient List<RolloutGroupCreate> savedRolloutGroups;

    private transient ValidationListener validationListener;

    private ValidationStatus validationStatus = ValidationStatus.VALID;

    private transient RolloutGroupsValidation groupsValidation;

    private final AtomicInteger runningValidationsCounter;

    private final TargetFilterQueryDataProvider targetFilterQueryDataProvider;

    public DefineGroupsLayout(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final RolloutManagement rolloutManagement, final TargetFilterQueryManagement targetFilterQueryManagement,
            final RolloutGroupManagement rolloutGroupManagement, final QuotaManagement quotaManagement,
            final TargetFilterQueryDataProvider targetFilterQueryDataProvider) {
        this.i18n = i18n;
        this.entityFactory = entityFactory;
        this.rolloutManagement = rolloutManagement;
        this.rolloutGroupManagement = rolloutGroupManagement;
        this.quotaManagement = quotaManagement;
        this.targetFilterQueryManagement = targetFilterQueryManagement;
        this.targetFilterQueryDataProvider = targetFilterQueryDataProvider;

        runningValidationsCounter = new AtomicInteger(0);
        groupRows = new ArrayList<>(10);

        buildLayout();
    }

    private void buildLayout() {

        setSpacing(Boolean.TRUE);
        setSizeUndefined();
        setRows(3);
        setColumns(6);
        setStyleName("marginTop");

        addComponent(getLabel("caption.rollout.group.definition.desc"), 0, 0, 5, 0);

        final int headerRow = 1;
        addComponent(getLabel("header.name"), 0, headerRow);
        addComponent(getLabel("header.target.filter.query"), 1, headerRow);
        addComponent(getLabel("header.target.percentage"), 2, headerRow);
        addComponent(getLabel("header.rolloutgroup.threshold"), 3, headerRow);
        addComponent(getLabel("header.rolloutgroup.threshold.error"), 4, headerRow);

        addComponent(createAddButton(), 0, 2, 5, 2);

    }

    /**
     * @param targetFilter
     *            the target filter which is required for verification
     */
    public void setTargetFilter(final String targetFilter) {
        this.targetFilter = targetFilter;
        updateValidation();
    }

    private Label getLabel(final String key) {
        return new LabelBuilder().name(i18n.getMessage(key)).buildLabel();
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
        final GroupRow groupRow = addGroupRow();
        groupRow.populateWithDefaults();
        updateValidation();
    }

    private GroupRow addGroupRow() {
        final int rowIndex = addRow();
        final GroupRow groupRow = new GroupRow();
        groupRow.addToGridRow(this, rowIndex);
        groupRows.add(groupRow);
        return groupRow;
    }

    private int addRow() {
        final int insertIndex = getRows() - 1;
        insertRow(insertIndex);
        return insertIndex;
    }

    public List<RolloutGroupCreate> getSavedRolloutGroups() {
        return savedRolloutGroups;
    }

    /**
     * @return the validation instance if was already validated
     */
    public RolloutGroupsValidation getGroupsValidation() {
        return groupsValidation;
    }

    private void removeAllRows() {
        for (int i = getRows() - 2; i > 1; i--) {
            removeRow(i);
        }

        groupRows.clear();
    }

    public void setDefaultTriggerThreshold(final String defaultTriggerThreshold) {
        this.defaultTriggerThreshold = defaultTriggerThreshold;
    }

    public void setDefaultErrorThreshold(final String defaultErrorThreshold) {
        this.defaultErrorThreshold = defaultErrorThreshold;
    }

    /**
     * Populate groups by rollout Id
     *
     * @param rolloutId
     *            the rollout Id
     */
    public void populateByRolloutId(final Long rolloutId) {
        if (rolloutId == null) {
            return;
        }

        removeAllRows();

        final List<RolloutGroup> groups = rolloutGroupManagement
                .findByRollout(PageRequest.of(0, quotaManagement.getMaxRolloutGroupsPerRollout()), rolloutId)
                .getContent();
        for (final RolloutGroup group : groups) {
            final GroupRow groupRow = addGroupRow();
            groupRow.populateByGroup(group);
        }

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
        return groupRows.stream().allMatch(GroupRow::isValid);
    }

    private void resetErrors() {
        groupRows.forEach(GroupRow::resetError);
    }

    private void setValidationStatus(final ValidationStatus status) {
        validationStatus = status;
        if (validationListener != null) {
            validationListener.validation(status);
        }
    }

    private void validateRemainingTargets() {
        resetErrors();
        if (targetFilter == null) {
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

        final int lastIdx = groupRows.size() - 1;
        final GroupRow lastRow = groupRows.get(lastIdx);
        if (groupsValidation != null && groupsValidation.isValid() && validationStatus != ValidationStatus.INVALID) {
            setValidationStatus(ValidationStatus.VALID);
        } else {
            lastRow.setError(i18n.getMessage("message.rollout.remaining.targets.error"));
            setValidationStatus(ValidationStatus.INVALID);
        }

        // validate the single groups
        final int maxTargets = quotaManagement.getMaxTargetsPerRolloutGroup();
        final boolean hasRemainingTargetsError = validationStatus == ValidationStatus.INVALID;
        for (int i = 0; i < groupRows.size(); ++i) {
            final GroupRow row = groupRows.get(i);
            // do not mask the 'remaining targets' error
            if (hasRemainingTargetsError && row.equals(lastRow)) {
                continue;
            }
            final Long count = groupsValidation.getTargetsPerGroup().get(i);
            if (count != null && count > maxTargets) {
                row.setError(i18n.getMessage(MESSAGE_ROLLOUT_MAX_GROUP_SIZE_EXCEEDED, maxTargets));
                setValidationStatus(ValidationStatus.INVALID);
            }
        }

    }

    private List<RolloutGroupCreate> getGroupsFromRows() {
        return groupRows.stream().map(GroupRow::getGroupEntity).collect(Collectors.toList());
    }

    public void setValidationListener(final ValidationListener validationListener) {
        this.validationListener = validationListener;
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

    private class GroupRow {

        private TextField groupName;

        private ComboBox<ProxyTargetFilterQuery> targetFilterQueryCombo;

        private TextArea targetFilterQuery;

        private TextField targetPercentage;

        private TextField triggerThreshold;

        private TextField errorThreshold;

        private HorizontalLayout optionsLayout;

        private boolean populated;

        private ProxyAdvancedRolloutGroupRow proxyAdvancedRolloutGroupRow;

        private final Binder<ProxyAdvancedRolloutGroupRow> proxyGroupRowBinder;

        public GroupRow() {
            proxyGroupRowBinder = new Binder<>();
            init();
        }

        private void init() {
            groupsCount += 1;
            createGroupName();
            createTargetFilterQueryCombo();

            createTargetFilterQuery();
            createTargetPercentage();
            createTriggerThreshold();
            createErrorThreshold();

            optionsLayout = new HorizontalLayout();
            optionsLayout.addComponent(createRemoveButton());
        }

        private void createGroupName() {
            groupName = new TextFieldBuilder(RolloutGroup.NAME_MAX_SIZE).prompt(i18n.getMessage("textfield.name"))
                    .id(UIComponentIdProvider.ROLLOUT_GROUP_LIST_GRID_ID).buildTextComponent();
            groupName.setSizeUndefined();
            groupName.setStyleName("rollout-group-name");

            proxyGroupRowBinder.forField(groupName).asRequired("Group name can not be empty")
                    .bind(ProxyAdvancedRolloutGroupRow::getGroupName, ProxyAdvancedRolloutGroupRow::setGroupName);
        }

        private void createTargetFilterQueryCombo() {
            targetFilterQueryCombo = new ComboBox<>();

            targetFilterQueryCombo.setId(UIComponentIdProvider.ROLLOUT_TARGET_FILTER_COMBO_ID);
            targetFilterQueryCombo.setPlaceholder(i18n.getMessage("prompt.target.filter"));
            targetFilterQueryCombo.addStyleName(ValoTheme.COMBOBOX_SMALL);

            targetFilterQueryCombo.setItemCaptionGenerator(ProxyTargetFilterQuery::getName);
            targetFilterQueryCombo.setDataProvider(targetFilterQueryDataProvider);

            proxyGroupRowBinder.forField(targetFilterQueryCombo).withConverter(filter -> {
                if (filter == null) {
                    return null;
                }

                return filter.getId();
            }, filterId -> {
                if (filterId == null) {
                    return null;
                }

                final ProxyTargetFilterQuery filter = new ProxyTargetFilterQuery();
                filter.setId(filterId);

                return filter;
            }).bind(ProxyAdvancedRolloutGroupRow::getTargetFilterId, ProxyAdvancedRolloutGroupRow::setTargetFilterId);
        }

        private void createTargetFilterQuery() {
            targetFilterQuery = new TextAreaBuilder(TargetFilterQuery.QUERY_MAX_SIZE).style("text-area-style")
                    .id(UIComponentIdProvider.ROLLOUT_TARGET_FILTER_QUERY_FIELD).buildTextComponent();
            targetFilterQuery.setEnabled(false);
            targetFilterQuery.setSizeUndefined();

            proxyGroupRowBinder.forField(targetFilterQuery).bind(ProxyAdvancedRolloutGroupRow::getTargetFilterQuery,
                    ProxyAdvancedRolloutGroupRow::setTargetFilterQuery);
        }

        private void createTargetPercentage() {
            targetPercentage = new TextFieldBuilder(32).prompt(i18n.getMessage("textfield.target.percentage"))
                    .id(UIComponentIdProvider.ROLLOUT_GROUP_TARGET_PERC_ID).buildTextComponent();
            targetPercentage.setWidth(80, Unit.PIXELS);

            proxyGroupRowBinder.forField(targetPercentage).asRequired()
                    .withConverter(new StringToFloatConverter("only float values are allowed"))
                    .withValidator((value, context) -> {
                        final FloatRangeValidator validator = new FloatRangeValidator(
                                i18n.getMessage("message.rollout.field.value.range", 0, 100), 0F, 100F);
                        validator.setMinValueIncluded(false);
                        return validator.apply(value, context);
                    }).bind(ProxyAdvancedRolloutGroupRow::getTargetPercentage,
                            ProxyAdvancedRolloutGroupRow::setTargetPercentage);
        }

        private void createTriggerThreshold() {
            triggerThreshold = new TextFieldBuilder(32).prompt(i18n.getMessage("prompt.tigger.threshold"))
                    .id(UIComponentIdProvider.ROLLOUT_TRIGGER_THRESOLD_ID).buildTextComponent();
            triggerThreshold.setWidth(80, Unit.PIXELS);

            proxyGroupRowBinder.forField(triggerThreshold).asRequired().bind(
                    ProxyAdvancedRolloutGroupRow::getTriggerThresholdPercentage,
                    ProxyAdvancedRolloutGroupRow::setTriggerThresholdPercentage);
        }

        private void createErrorThreshold() {
            errorThreshold = new TextFieldBuilder(32).prompt(i18n.getMessage("prompt.error.threshold"))
                    .id(UIComponentIdProvider.ROLLOUT_ERROR_THRESOLD_ID).buildTextComponent();
            errorThreshold.setWidth(80, Unit.PIXELS);

            proxyGroupRowBinder.forField(errorThreshold).asRequired().bind(
                    ProxyAdvancedRolloutGroupRow::getErrorThresholdPercentage,
                    ProxyAdvancedRolloutGroupRow::setErrorThresholdPercentage);
        }

        private Button createRemoveButton() {
            final Button button = SPUIComponentProvider.getButton(UIComponentIdProvider.ROLLOUT_GROUP_REMOVE_ID, "", "",
                    "", true, VaadinIcons.MINUS, SPUIButtonStyleNoBorderWithIcon.class);
            button.setSizeUndefined();
            button.addStyleName("default-color");
            button.setEnabled(true);
            button.setVisible(true);
            button.addClickListener(event -> onRemove());
            return button;
        }

        private void onRemove() {
            final int index = findRowIndexFor(groupName, 0);
            if (index != -1) {
                removeRow(index);
            }
            removeGroupRow(this);
        }

        private void removeGroupRow(final GroupRow groupRow) {
            groupRows.remove(groupRow);
            updateValidation();
        }

        private int findRowIndexFor(final Component component, final int col) {
            final int rows = getRows();
            for (int i = 0; i < rows; i++) {
                final Component rowComponent = getComponent(col, i);
                if (component.equals(rowComponent)) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * Adds this group row to a grid layout
         * 
         * @param layout
         *            the grid layout
         * @param rowIndex
         *            the row of the grid layout
         */
        public void addToGridRow(final GridLayout layout, final int rowIndex) {
            layout.addComponent(groupName, 0, rowIndex);
            if (populated) {
                layout.addComponent(targetFilterQuery, 1, rowIndex);
            } else {
                layout.addComponent(targetFilterQueryCombo, 1, rowIndex);
            }
            layout.addComponent(targetPercentage, 2, rowIndex);
            layout.addComponent(triggerThreshold, 3, rowIndex);
            layout.addComponent(errorThreshold, 4, rowIndex);
            layout.addComponent(optionsLayout, 5, rowIndex);
        }

        /**
         * Builds a group definition from this group row
         * 
         * @return the RolloutGroupCreate definition
         */
        public RolloutGroupCreate getGroupEntity() {
            final RolloutGroupConditions conditions = new RolloutGroupConditionBuilder()
                    .successAction(RolloutGroupSuccessAction.NEXTGROUP, null)
                    .successCondition(RolloutGroupSuccessCondition.THRESHOLD,
                            proxyAdvancedRolloutGroupRow.getTriggerThresholdPercentage())
                    .errorCondition(RolloutGroupErrorCondition.THRESHOLD,
                            proxyAdvancedRolloutGroupRow.getErrorThresholdPercentage())
                    .errorAction(RolloutGroupErrorAction.PAUSE, null).build();

            return entityFactory.rolloutGroup().create().name(proxyAdvancedRolloutGroupRow.getGroupName())
                    .description(proxyAdvancedRolloutGroupRow.getGroupName())
                    .targetFilterQuery(proxyAdvancedRolloutGroupRow.getTargetFilterQuery())
                    .targetPercentage(proxyAdvancedRolloutGroupRow.getTargetPercentage()).conditions(conditions);
        }

        /**
         * Populates the row with the default data.
         * 
         */
        public void populateWithDefaults() {
            proxyAdvancedRolloutGroupRow = new ProxyAdvancedRolloutGroupRow();
            proxyAdvancedRolloutGroupRow
                    .setGroupName(i18n.getMessage("textfield.rollout.group.default.name", groupsCount));
            proxyAdvancedRolloutGroupRow.setTargetPercentage(100f);
            proxyAdvancedRolloutGroupRow.setTriggerThresholdPercentage(defaultTriggerThreshold);
            proxyAdvancedRolloutGroupRow.setErrorThresholdPercentage(defaultErrorThreshold);
            proxyGroupRowBinder.setBean(proxyAdvancedRolloutGroupRow);

            proxyGroupRowBinder.addStatusChangeListener(event -> updateValidation());
        }

        /**
         * Populates the row with the data from the provided groups.
         * 
         * @param group
         *            the data source
         */
        public void populateByGroup(final RolloutGroup group) {
            proxyAdvancedRolloutGroupRow = new ProxyAdvancedRolloutGroupRow();
            proxyAdvancedRolloutGroupRow.setGroupName(group.getName());
            if (!StringUtils.isEmpty(group.getTargetFilterQuery())) {
                proxyAdvancedRolloutGroupRow.setTargetFilterQuery(group.getTargetFilterQuery());
                final Page<TargetFilterQuery> filterQueries = targetFilterQueryManagement
                        .findByQuery(PageRequest.of(0, 1), group.getTargetFilterQuery());
                if (filterQueries.getTotalElements() > 0) {
                    proxyAdvancedRolloutGroupRow.setTargetFilterId(filterQueries.getContent().get(0).getId());
                }
            }
            proxyAdvancedRolloutGroupRow.setTargetPercentage(group.getTargetPercentage());
            proxyAdvancedRolloutGroupRow.setTriggerThresholdPercentage(group.getSuccessConditionExp());
            proxyAdvancedRolloutGroupRow.setErrorThresholdPercentage(group.getErrorConditionExp());
            proxyGroupRowBinder.setBean(proxyAdvancedRolloutGroupRow);

            populated = true;

            proxyGroupRowBinder.addStatusChangeListener(event -> updateValidation());
        }

        /**
         * @return whether the data entered in this row is valid
         */
        public boolean isValid() {
            return proxyGroupRowBinder.isValid();
        }

        private void setError(final String error) {
            targetPercentage.setComponentError(new UserError(error));
        }

        private void resetError() {
            targetPercentage.setComponentError(null);
        }
    }

}
