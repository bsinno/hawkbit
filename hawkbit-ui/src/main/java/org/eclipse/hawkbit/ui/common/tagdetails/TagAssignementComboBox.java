/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.tagdetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.ui.common.tagdetails.TagPanelLayout.TagAssignmentListener;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.google.common.collect.Sets;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Combobox that lists all available Tags that can be assigned to a
 * {@link Target} or {@link DistributionSet}.
 */
public class TagAssignementComboBox extends HorizontalLayout {

    private static final long serialVersionUID = 1L;

    private final List<TagData> allAssignableTags;
    private final transient Set<TagAssignmentListener> listeners = Sets.newConcurrentHashSet();

    private final ComboBox<TagData> assignableTagsComboBox;

    private final boolean readOnlyMode;

    /**
     * Constructor.
     * 
     * @param i18n
     *            the i18n
     * @param readOnlyMode
     *            if true the combobox will be disabled so no assignment can be
     *            done.
     */
    TagAssignementComboBox(final VaadinMessageSource i18n, final boolean readOnlyMode) {

        this.readOnlyMode = readOnlyMode;

        setWidth("100%");

        allAssignableTags = new ArrayList<>();

        assignableTagsComboBox = getAssignableTagsComboBox(i18n.getMessage(UIMessageIdProvider.TOOLTIP_SELECT_TAG));

        assignableTagsComboBox.setItems(allAssignableTags);
        assignableTagsComboBox.clear();
        assignableTagsComboBox.addValueChangeListener(event -> assignTag(event.getValue()));

        addComponent(assignableTagsComboBox);
    }

    private ComboBox<TagData> getAssignableTagsComboBox(final String description) {
        final ComboBox<TagData> tagsComboBox = new ComboBox<>();

        tagsComboBox.setId(UIComponentIdProvider.TAG_SELECTION_ID);
        tagsComboBox.setDescription(description);
        tagsComboBox.addStyleName(SPUIStyleDefinitions.DETAILS_LAYOUT_STYLE);
        tagsComboBox.addStyleName(ValoTheme.COMBOBOX_TINY);
        tagsComboBox.setEnabled(!readOnlyMode);
        tagsComboBox.setWidth("100%");
        tagsComboBox.setEmptySelectionAllowed(true);

        return tagsComboBox;
    }

    /**
     * Initializes the Combobox with all assignable tags.
     * 
     * @param assignableTags
     *            assignable tags
     */
    void initializeAssignableTags(final List<TagData> assignableTags) {
        allAssignableTags.addAll(assignableTags);
    }

    private void assignTag(final TagData tagData) {
        if (tagData == null || readOnlyMode) {
            return;
        }

        allAssignableTags.remove(tagData);
        notifyListenersTagAssigned(tagData);
        assignableTagsComboBox.clear();
    }

    /**
     * Removes all Tags from Combobox.
     */
    void removeAllTags() {
        allAssignableTags.clear();
        assignableTagsComboBox.clear();
    }

    /**
     * Adds an assignable Tag to the combobox.
     * 
     * @param tagData
     *            the data of the Tag
     */
    void addAssignableTag(final TagData tagData) {
        if (tagData == null) {
            return;
        }

        allAssignableTags.add(tagData);
    }

    /**
     * Removes an assignable tag from the combobox.
     * 
     * @param tagData
     *            the {@link TagData} of the Tag that should be removed.
     */
    void removeAssignableTag(final TagData tagData) {
        allAssignableTags.remove(tagData);
    }

    /**
     * Registers an {@link TagAssignmentListener} on the combobox.
     * 
     * @param listener
     *            the listener to register
     */
    void addTagAssignmentListener(final TagAssignmentListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a {@link TagAssignmentListener} from the combobox,
     * 
     * @param listener
     *            the listener that should be removed.
     */
    void removeTagAssignmentListener(final TagAssignmentListener listener) {
        listeners.remove(listener);
    }

    private void notifyListenersTagAssigned(final TagData tagData) {
        listeners.forEach(listener -> listener.assignTagCallback(tagData));
    }
}
