/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettag;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.List;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TagManagement;
import org.eclipse.hawkbit.repository.model.TargetTag;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.colorpicker.ColorPickerConstants;
import org.eclipse.hawkbit.ui.colorpicker.ColorPickerHelper;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.components.RefreshableContainer;
import org.eclipse.hawkbit.ui.layouts.AbstractCreateUpdateTagLayout;
import org.eclipse.hawkbit.ui.management.event.TargetTagTableEvent;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.I18N;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Class for Create / Update Tag Layout of target
 */
public class CreateUpdateTargetTagLayoutWindow extends AbstractCreateUpdateTagLayout<TargetTag>
        implements RefreshableContainer {

    private static final long serialVersionUID = 2446682350481560235L;

    /**
     * Constructor for CreateUpdateTargetTagLayoutWindow
     * 
     * @param i18n
     *            I18N
     * @param tagManagement
     *            TagManagement
     * @param entityFactory
     *            EntityFactory
     * @param eventBus
     *            UIEventBus
     * @param permChecker
     *            SpPermissionChecker
     * @param uiNotification
     *            UINotification
     */
    public CreateUpdateTargetTagLayoutWindow(final I18N i18n, final TagManagement tagManagement,
            final EntityFactory entityFactory, final UIEventBus eventBus, final SpPermissionChecker permChecker,
            final UINotification uiNotification) {
        super(i18n, tagManagement, entityFactory, eventBus, permChecker, uiNotification);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        optiongroup.addValueChangeListener(this::optionValueChanged);
    }

    /**
     * Populate target name combo.
     */
    @Override
    public void populateTagNameCombo() {
        tagNameComboBox.removeAllItems();
        final List<TargetTag> trgTagNameList = tagManagement.findAllTargetTags();
        trgTagNameList.forEach(value -> tagNameComboBox.addItem(value.getName()));
    }

    /**
     * Select tag & set tag name & tag desc values corresponding to selected
     * tag.
     * 
     * @param targetTagSelected
     *            as the selected tag from combo
     */
    @Override
    public void setTagDetails(final String targetTagSelected) {
        tagName.setValue(targetTagSelected);
        final TargetTag selectedTargetTag = tagManagement.findTargetTag(targetTagSelected);
        if (selectedTargetTag != null) {
            tagDesc.setValue(selectedTargetTag.getDescription());
            if (null == selectedTargetTag.getColour()) {
                setTagColor(getColorPickerLayout().getDefaultColor(), ColorPickerConstants.DEFAULT_COLOR);
            } else {
                setTagColor(ColorPickerHelper.rgbToColorConverter(selectedTargetTag.getColour()),
                        selectedTargetTag.getColour());
            }
        }
    }

    @Override
    protected void updateEntity(final TargetTag entity) {
        updateExistingTag(entity);
    }

    @Override
    protected void createEntity() {
        createNewTag();
    }

    @Override
    protected TargetTag findEntityByName() {
        return tagManagement.findTargetTag(tagName.getValue());
    }

    /**
     * Create new tag.
     */
    @Override
    protected void createNewTag() {
        super.createNewTag();
        final String tagNameTrimmed = HawkbitCommonUtil.trimAndNullIfEmpty(tagNameValue);
        final String tagDescriptionTrimmed = HawkbitCommonUtil.trimAndNullIfEmpty(tagDescValue);
        if (isNotEmpty(tagNameTrimmed)) {
            String colour = ColorPickerConstants.START_COLOR.getCSS();
            if (isNotEmpty(getColorPicked())) {
                colour = getColorPicked();
            }

            final TargetTag newTargetTag = tagManagement.createTargetTag(entityFactory.tag().create()
                    .name(tagNameTrimmed).description(tagDescriptionTrimmed).colour(colour));
            eventBus.publish(this, new TargetTagTableEvent(BaseEntityEventType.ADD_ENTITY, newTargetTag));
            displaySuccess(newTargetTag.getName());
        } else {
            displayValidationError(i18n.get(MESSAGE_ERROR_MISSING_TAGNAME));
        }
    }

    @Override
    protected void createRequiredComponents() {
        super.createRequiredComponents();
        createOptionGroup(permChecker.hasCreateTargetPermission(), permChecker.hasUpdateTargetPermission());
    }

    @Override
    protected void reset() {

        super.reset();
        setOptionGroupDefaultValue(permChecker.hasCreateTargetPermission(), permChecker.hasUpdateTargetPermission());
    }

    @Override
    protected String getWindowCaption() {
        return i18n.get("caption.add.tag");
    }

    @Override
    public void refreshContainer() {
        populateTagNameCombo();
    }

}
