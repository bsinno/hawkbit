/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.builder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

import org.eclipse.hawkbit.repository.model.NamedEntity;
import org.eclipse.hawkbit.repository.model.NamedVersionedEntity;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetStatelessDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ActionCreator;
import org.eclipse.hawkbit.ui.common.data.proxies.Describable;
import org.eclipse.hawkbit.ui.common.data.proxies.DsIdProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.Named;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.Versioned;
import org.eclipse.hawkbit.ui.management.miscs.ActionTypeOptionGroupAssignmentLayout;
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
    public static <T extends Named> BoundComponent<TextField> createNameInput(final Binder<T> binder,
            VaadinMessageSource i18n, String fieldId) {
        final TextField nameInput = new TextFieldBuilder(NamedEntity.NAME_MAX_SIZE).id(fieldId)
                .caption(i18n.getMessage(TEXTFIELD_NAME)).prompt(i18n.getMessage(TEXTFIELD_NAME)).buildTextComponent();
        nameInput.setSizeUndefined();

        Binding<T, String> binding = binder.forField(nameInput)
                .asRequired(UIMessageIdProvider.MESSAGE_ERROR_NAMEREQUIRED).bind(T::getName, T::setName);
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
    public static <T extends Versioned> TextField createVersionInput(final Binder<T> binder, VaadinMessageSource i18n,
            String fieldId) {
        final TextField versionInput = new TextFieldBuilder(NamedVersionedEntity.VERSION_MAX_SIZE).id(fieldId)
                .caption(i18n.getMessage(TEXTFIELD_VERSION)).prompt(i18n.getMessage(TEXTFIELD_VERSION))
                .buildTextComponent();
        versionInput.setSizeUndefined();

        binder.forField(versionInput).asRequired(UIMessageIdProvider.MESSAGE_ERROR_VERSIONREQUIRED).bind(T::getVersion,
                T::setVersion);
        return versionInput;
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
    public static <T extends Describable> TextArea createDescriptionInput(final Binder<T> binder,
            VaadinMessageSource i18n, String fieldId) {
        final TextArea descriptionInput = new TextAreaBuilder(NamedEntity.DESCRIPTION_MAX_SIZE).id(fieldId)
                .caption(i18n.getMessage(TEXTFIELD_DESCRIPTION)).prompt(i18n.getMessage(TEXTFIELD_DESCRIPTION))
                .style("text-area-style").buildTextComponent();
        descriptionInput.setSizeUndefined();

        binder.forField(descriptionInput).bind(T::getDescription, T::setDescription);
        return descriptionInput;
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
    public static <T extends ActionCreator> ActionTypeOptionGroupAssignmentLayout createActionTypeOptionGroupLayout(
            final Binder<T> binder, VaadinMessageSource i18n, String componentId) {
        final ActionTypeOptionGroupAssignmentLayout actionTypeOptionGroupLayout = new ActionTypeOptionGroupAssignmentLayout(
                i18n, componentId);

        binder.forField(actionTypeOptionGroupLayout.getActionTypeOptionGroup()).bind(T::getActionType,
                T::setActionType);

        final TimeZone tz = SPDateTimeUtil.getBrowserTimeZone();
        binder.forField(actionTypeOptionGroupLayout.getForcedTimeDateField()).withConverter(localDateTime -> {
            if (localDateTime == null) {
                return null;
            }

            return localDateTime.atZone(SPDateTimeUtil.getTimeZoneId(tz)).toInstant().toEpochMilli();
        }, forcedTime -> {
            if (forcedTime == null) {
                return null;
            }

            return LocalDateTime.ofInstant(Instant.ofEpochMilli(forcedTime), SPDateTimeUtil.getTimeZoneId(tz));
        }).bind(T::getForcedTime, T::setForcedTime);

        return actionTypeOptionGroupLayout;
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
    public static ComboBox<ProxyDistributionSet> createDistributionSetComboBox(
            final DistributionSetStatelessDataProvider dataProvider, VaadinMessageSource i18n, String componentId) {
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

    /**
     * create a bound input for distribution sets
     * @param <T>
     *  type of the binder
     * @param binder
     *  that is bound to the input
     * @param dataProvider
     *            provides distribution sets
     * @param i18n
     *            i18n
     * @param componentId
     *            id of the input layout
     * @return bound ComboBox of distribution sets
     */
    public static <T extends DsIdProvider> BoundComponent<ComboBox<ProxyDistributionSet>> createDistributionSetComboBox(
            final Binder<T> binder, final DistributionSetStatelessDataProvider dataProvider, VaadinMessageSource i18n,
            String componentId) {
        final ComboBox<ProxyDistributionSet> comboBox = createDistributionSetComboBox(dataProvider, i18n,
                componentId);
        Binding<T, Long> binding = binder.forField(comboBox).asRequired(UIMessageIdProvider.MESSAGE_ERROR_DISTRIBUTIONSET_REQUIRED)
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
                }).bind(DsIdProvider::getDistributionSetId, DsIdProvider::setDistributionSetId);
        
        return new BoundComponent<>(comboBox, binding);
    }

}
