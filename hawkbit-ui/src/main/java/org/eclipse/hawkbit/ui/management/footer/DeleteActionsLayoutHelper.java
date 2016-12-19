/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.footer;

import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;

import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;

/**
 * Delete actions layout helper class.
 *
 */
public final class DeleteActionsLayoutHelper {

    private DeleteActionsLayoutHelper() {

    }

    /**
     * Checks if component is a target tag.
     *
     * @param source
     *            Component dropped
     * @return true if it component is target tag
     */
    public static boolean isTargetTag(final Component source) {
        if (source instanceof DragAndDropWrapper) {
            final String wrapperData = ((DragAndDropWrapper) source).getData().toString();
            final String id = wrapperData.replace(SPUIDefinitions.TARGET_TAG_BUTTON, "");
            if (wrapperData.contains(SPUIDefinitions.TARGET_TAG_BUTTON) && !id.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if component is distribution tag.
     *
     * @param source
     *            component dropped
     * @return true if it component is distribution tag
     */
    public static boolean isDistributionTag(final Component source) {
        if (source instanceof DragAndDropWrapper) {
            final String wrapperData = ((DragAndDropWrapper) source).getData().toString();
            final String id = wrapperData.replace(SPUIDefinitions.DISTRIBUTION_TAG_BUTTON, "");
            return wrapperData.contains(SPUIDefinitions.DISTRIBUTION_TAG_BUTTON) && !id.trim().isEmpty();
        }
        return false;
    }

    /**
     * Extract the ds tag id by the drag and drop component
     * 
     * @param source
     *            the source
     * @return the ds tag id
     */
    public static Long getDistributionTagId(final DragAndDropWrapper source) {
        final String wrapperData = source.getData().toString();
        final String id = wrapperData.replace(SPUIDefinitions.DISTRIBUTION_TAG_BUTTON, "");
        return Long.valueOf(id.trim());
    }

    /**
     * Extract the target tag id by the drag and drop component
     * 
     * @param source
     *            the source
     * @return the target tag id
     */
    public static Long getTargetTagId(final DragAndDropWrapper source) {
        final String wrapperData = source.getData().toString();
        final String id = wrapperData.replace(SPUIDefinitions.TARGET_TAG_BUTTON, "");
        return Long.valueOf(id.trim());
    }

    /**
     * Checks if component is target table.
     *
     * @param source
     *            component dropped
     * @return true if it component is target table
     */
    public static boolean isTargetTable(final Component source) {
        return UIComponentIdProvider.TARGET_TABLE_ID.equalsIgnoreCase(source.getId());
    }

    /**
     * Checks id component is distribution table.
     *
     * @param source
     *            component dropped
     * @return true if it component is distribution table
     */
    public static boolean isDistributionTable(final Component source) {
        return UIComponentIdProvider.DIST_TABLE_ID.equalsIgnoreCase(source.getId());
    }

    /**
     * Check if dropped component can be deleted.
     *
     * @param source
     *            component dropped
     * @return true if component can be deleted
     */
    public static Boolean isComponentDeletable(final Component source) {
        return isTargetTable(source) || isDistributionTable(source) || isTargetTag(source) || isDistributionTag(source);
    }

}
