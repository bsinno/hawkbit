/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.builder;

import org.eclipse.hawkbit.repository.model.NamedEntity;
import org.eclipse.hawkbit.repository.model.NamedVersionedEntity;
import org.eclipse.hawkbit.ui.common.data.aware.ActionTypeAware;
import org.eclipse.hawkbit.ui.common.data.aware.DescriptionAware;
import org.eclipse.hawkbit.ui.common.data.aware.DsIdAware;
import org.eclipse.hawkbit.ui.common.data.aware.NameAware;
import org.eclipse.hawkbit.ui.common.data.aware.StartOptionAware;
import org.eclipse.hawkbit.ui.common.data.aware.VersionAware;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetStatelessDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.management.miscs.ActionTypeOptionGroupAssignmentLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AutoStartOptionGroupLayout;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Builder class for from components
 */
public final class FormComponentBuilder {
    public static final String TEXTFIELD_NAME = "textfield.name";
    public static final String TEXTFIELD_VERSION = "textfield.version";
    public static final String TEXTFIELD_DESCRIPTION = "textfield.description";

    public static final String PROMPT_DISTRIBUTION_SET = "prompt.distribution.set";

    private FormComponentBuilder() {
    }

    /**
     * Create an input field for a name
     * 
     * @param <T>
     *            type of the binder
     * @param binder
     *            that is bound to the field
     * @param i18n
     *            message source
     * @param fieldId
     *            id of the field
     * @return the TextField with its Binding
     */
    public static <T extends NameAware> BoundComponent<TextField> createNameInput(final Binder<T> binder,
            final VaadinMessageSource i18n, final String fieldId) {
        final TextField nameInput = new TextFieldBuilder(NamedEntity.NAME_MAX_SIZE).id(fieldId)
                .caption(i18n.getMessage(TEXTFIELD_NAME)).prompt(i18n.getMessage(TEXTFIELD_NAME)).buildTextComponent();
        nameInput.setSizeUndefined();

        final Binding<T, String> binding = binder.forField(nameInput)
                .asRequired(i18n.getMessage(UIMessageIdProvider.MESSAGE_ERROR_NAMEREQUIRED))
                .bind(NameAware::getName, NameAware::setName);

        return new BoundComponent<>(nameInput, binding);
    }

    /**
     * Create a required input field for a version
     * 
     * @param <T>
     *            type of the binder
     * @param binder
     *            that is bound to the field
     * @param i18n
     *            message source
     * @param fieldId
     *            id of the field
     * @return the TextField
     */
    public static <T extends VersionAware> BoundComponent<TextField> createVersionInput(final Binder<T> binder,
            final VaadinMessageSource i18n, final String fieldId) {
        final TextField versionInput = new TextFieldBuilder(NamedVersionedEntity.VERSION_MAX_SIZE).id(fieldId)
                .caption(i18n.getMessage(TEXTFIELD_VERSION)).prompt(i18n.getMessage(TEXTFIELD_VERSION))
                .buildTextComponent();
        versionInput.setSizeUndefined();

        final Binding<T, String> binding = binder.forField(versionInput)
                .asRequired(i18n.getMessage(UIMessageIdProvider.MESSAGE_ERROR_VERSIONREQUIRED))
                .bind(VersionAware::getVersion, VersionAware::setVersion);

        return new BoundComponent<>(versionInput, binding);
    }

    /**
     * Create an optional input field for a description
     * 
     * @param <T>
     *            type of the binder
     * @param binder
     *            that is bound to the field
     * @param i18n
     *            message source
     * @param fieldId
     *            id of the field
     * @return the TextArea
     */
    public static <T extends DescriptionAware> BoundComponent<TextArea> createDescriptionInput(final Binder<T> binder,
            final VaadinMessageSource i18n, final String fieldId) {
        final TextArea descriptionInput = new TextAreaBuilder(NamedEntity.DESCRIPTION_MAX_SIZE).id(fieldId)
                .caption(i18n.getMessage(TEXTFIELD_DESCRIPTION)).prompt(i18n.getMessage(TEXTFIELD_DESCRIPTION))
                .style("text-area-style").buildTextComponent();
        descriptionInput.setSizeUndefined();

        final Binding<T, String> binding = binder.forField(descriptionInput).bind(DescriptionAware::getDescription,
                DescriptionAware::setDescription);

        return new BoundComponent<>(descriptionInput, binding);
    }

    /**
     * Create a bound {@link ActionTypeOptionGroupAssignmentLayout}
     * 
     * @param <T>
     *            type of the binder
     * @param binder
     *            that is bound to the layout
     * @param i18n
     *            message source
     * @param componentId
     *            id of the input layout
     * @return a bound layout
     */
    public static <T extends ActionTypeAware> BoundComponent<ActionTypeOptionGroupAssignmentLayout> createActionTypeOptionGroupLayout(
            final Binder<T> binder, final VaadinMessageSource i18n, final String componentId) {
        final ActionTypeOptionGroupAssignmentLayout actionTypeOptionGroupLayout = new ActionTypeOptionGroupAssignmentLayout(
                i18n, componentId);

        binder.forField(actionTypeOptionGroupLayout.getActionTypeOptionGroup()).bind(ActionTypeAware::getActionType,
                ActionTypeAware::setActionType);

        final Binding<T, Long> binding = binder.forField(actionTypeOptionGroupLayout.getForcedTimeDateField())
                .asRequired(i18n.getMessage("message.forcedTime.cannotBeEmpty")).withConverter(localDateTime -> {
                    if (localDateTime == null) {
                        return null;
                    }

                    return SPDateTimeUtil.localDateTimeToEpochMilli(localDateTime);
                }, forcedTime -> {
                    if (forcedTime == null) {
                        return null;
                    }

                    return SPDateTimeUtil.epochMilliToLocalDateTime(forcedTime);
                }).bind(ActionTypeAware::getForcedTime, ActionTypeAware::setForcedTime);

        return new BoundComponent<>(actionTypeOptionGroupLayout, binding);
    }

    /**
     * Create a bound {@link AutoStartOptionGroupLayout}
     * 
     * @param <T>
     *            type of the binder
     * @param binder
     *            that is bound to the layout
     * @param i18n
     *            message source
     * @param componentId
     *            id of the input layout
     * @return a bound layout
     */
    public static <T extends StartOptionAware> BoundComponent<AutoStartOptionGroupLayout> createAutoStartOptionGroupLayout(
            final Binder<T> binder, final VaadinMessageSource i18n, final String componentId) {
        final AutoStartOptionGroupLayout autoStartOptionGroup = new AutoStartOptionGroupLayout(i18n, componentId);

        binder.forField(autoStartOptionGroup.getAutoStartOptionGroup()).bind(StartOptionAware::getStartOption,
                StartOptionAware::setStartOption);

        final Binding<T, Long> binding = binder.forField(autoStartOptionGroup.getStartAtDateField())
                .asRequired(i18n.getMessage("message.scheduledTime.cannotBeEmpty")).withConverter(localDateTime -> {
                    if (localDateTime == null) {
                        return null;
                    }

                    return SPDateTimeUtil.localDateTimeToEpochMilli(localDateTime);
                }, startAtTime -> {
                    if (startAtTime == null) {
                        return null;
                    }

                    return SPDateTimeUtil.epochMilliToLocalDateTime(startAtTime);
                }).bind(StartOptionAware::getStartAt, StartOptionAware::setStartAt);

        return new BoundComponent<>(autoStartOptionGroup, binding);
    }

    /**
     * create a bound input for distribution sets
     * 
     * @param <T>
     *            type of the binder
     * @param binder
     *            that is bound to the input
     * @param dataProvider
     *            provides distribution sets
     * @param i18n
     *            i18n
     * @param componentId
     *            id of the input layout
     * @return bound ComboBox of distribution sets
     */
    public static <T extends DsIdAware> BoundComponent<ComboBox<ProxyDistributionSet>> createDistributionSetComboBox(
            final Binder<T> binder, final DistributionSetStatelessDataProvider dataProvider,
            final VaadinMessageSource i18n, final String componentId) {
        final ComboBox<ProxyDistributionSet> comboBox = createDistributionSetComboBox(dataProvider, i18n, componentId);

        final Binding<T, Long> binding = binder.forField(comboBox)
                .asRequired(i18n.getMessage(UIMessageIdProvider.MESSAGE_ERROR_DISTRIBUTIONSET_REQUIRED))
                .withConverter(ds -> {
                    if (ds == null) {
                        return null;
                    }

                    return ds.getId();
                }, dsId -> {
                    if (dsId == null) {
                        return null;
                    }

                    final ProxyDistributionSet ds = new ProxyDistributionSet();
                    ds.setId(dsId);

                    return ds;
                }).bind(DsIdAware::getDistributionSetId, DsIdAware::setDistributionSetId);

        return new BoundComponent<>(comboBox, binding);
    }

    /**
     * create an unbound input for distribution sets
     * 
     * @param dataProvider
     *            provides distribution sets
     * @param i18n
     *            i18n
     * @param componentId
     *            id of the input layout
     * @return ComboBox of distribution sets
     */
    private static ComboBox<ProxyDistributionSet> createDistributionSetComboBox(
            final DistributionSetStatelessDataProvider dataProvider, final VaadinMessageSource i18n,
            final String componentId) {
        final ComboBox<ProxyDistributionSet> comboBox = new ComboBox<>();
        comboBox.setCaption(i18n.getMessage(UIMessageIdProvider.HEADER_DISTRIBUTION_SET));
        comboBox.setId(componentId);
        comboBox.setPlaceholder(i18n.getMessage(PROMPT_DISTRIBUTION_SET));
        comboBox.addStyleName(ValoTheme.COMBOBOX_SMALL);
        comboBox.setEmptySelectionAllowed(false);

        comboBox.setItemCaptionGenerator(ProxyDistributionSet::getNameVersion);
        comboBox.setDataProvider(dataProvider);

        return comboBox;
    }
}
