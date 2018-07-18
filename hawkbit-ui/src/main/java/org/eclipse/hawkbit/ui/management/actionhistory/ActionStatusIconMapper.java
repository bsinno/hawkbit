/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.actionhistory;

import java.util.Map;

import org.eclipse.hawkbit.repository.model.Action;
import org.eclipse.hawkbit.repository.model.ActionStatus;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;

import com.google.common.collect.Maps;
import com.vaadin.icons.VaadinIcons;

/**
 * Mapping utility for {@link ActionStatus} to icon in action history table.
 *
 */
public final class ActionStatusIconMapper {
    static final Map<Action.Status, ActionStatusIconMapper> MAPPINGS = Maps.newEnumMap(Action.Status.class);

    static {
        MAPPINGS.put(Action.Status.FINISHED, new ActionStatusIconMapper("label.finished",
                SPUIStyleDefinitions.STATUS_ICON_GREEN, VaadinIcons.CHECK_CIRCLE));
        MAPPINGS.put(Action.Status.CANCELED, new ActionStatusIconMapper("label.cancelled",
                SPUIStyleDefinitions.STATUS_ICON_GREEN, VaadinIcons.CLOSE_CIRCLE));

        MAPPINGS.put(Action.Status.ERROR, new ActionStatusIconMapper("label.error",
                SPUIStyleDefinitions.STATUS_ICON_RED, VaadinIcons.EXCLAMATION_CIRCLE));

        MAPPINGS.put(Action.Status.WARNING, new ActionStatusIconMapper("label.warning",
                SPUIStyleDefinitions.STATUS_ICON_ORANGE, VaadinIcons.EXCLAMATION_CIRCLE));
        MAPPINGS.put(Action.Status.CANCEL_REJECTED, new ActionStatusIconMapper("label.warning",
                SPUIStyleDefinitions.STATUS_ICON_ORANGE, VaadinIcons.EXCLAMATION_CIRCLE));

        MAPPINGS.put(Action.Status.RUNNING, new ActionStatusIconMapper("label.running",
                SPUIStyleDefinitions.STATUS_ICON_PENDING, VaadinIcons.ADJUST));
        MAPPINGS.put(Action.Status.CANCELING, new ActionStatusIconMapper("label.cancelling",
                SPUIStyleDefinitions.STATUS_ICON_PENDING, VaadinIcons.CLOSE_CIRCLE));
        MAPPINGS.put(Action.Status.RETRIEVED, new ActionStatusIconMapper("label.retrieved",
                SPUIStyleDefinitions.STATUS_ICON_PENDING, VaadinIcons.BULLSEYE));
        MAPPINGS.put(Action.Status.DOWNLOAD, new ActionStatusIconMapper("label.download",
                SPUIStyleDefinitions.STATUS_ICON_PENDING, VaadinIcons.CLOUD_DOWNLOAD));
        MAPPINGS.put(Action.Status.DOWNLOADED, new ActionStatusIconMapper("label.downloaded",
                SPUIStyleDefinitions.STATUS_ICON_GREEN, VaadinIcons.CLOUD_DOWNLOAD));
        MAPPINGS.put(Action.Status.SCHEDULED, new ActionStatusIconMapper("label.scheduled",
                SPUIStyleDefinitions.STATUS_ICON_PENDING, VaadinIcons.HOURGLASS));
    }

    private final String descriptionI18N;
    private final String styleName;
    private final VaadinIcons icon;

    private ActionStatusIconMapper(final String descriptionI18N, final String styleName, final VaadinIcons icon) {
        this.descriptionI18N = descriptionI18N;
        this.styleName = styleName;
        this.icon = icon;
    }

    String getDescriptionI18N() {
        return descriptionI18N;
    }

    String getStyleName() {
        return styleName;
    }

    VaadinIcons getIcon() {
        return icon;
    }

}
