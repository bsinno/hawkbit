/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.builder;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.Action.Status;
import org.eclipse.hawkbit.repository.model.Rollout.RolloutStatus;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupStatus;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAction.IsActiveDecoration;
import org.eclipse.hawkbit.ui.rollout.ProxyFontIcon;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.FontIcon;

/**
 * Class to create {@link ProxyFontIcon}s
 *
 */
public final class IconBuilder {

    private IconBuilder() {
    }

    /**
     * Generate {@link ProxyFontIcon}s mapped to their {@link Status}
     * 
     * @param i18n
     *            message source for internationalization
     * @return map of states with their icons
     */
    public static Map<Status, ProxyFontIcon> generateActionStatusIcons(final VaadinMessageSource i18n) {

        final IconMapBuilderWithGeneratedDesc<Status> builder = new IconMapBuilderWithGeneratedDesc<>(Status.class,
                i18n, status -> UIMessageIdProvider.TOOLTIP_ACTION_STATUS_PREFIX + status.toString());

        builder.add(Status.FINISHED, VaadinIcons.CHECK_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_GREEN);
        builder.add(Status.SCHEDULED, VaadinIcons.HOURGLASS_EMPTY, SPUIStyleDefinitions.STATUS_ICON_PENDING);
        builder.add(Status.RUNNING, VaadinIcons.ADJUST, SPUIStyleDefinitions.STATUS_ICON_PENDING);
        builder.add(Status.RETRIEVED, VaadinIcons.CHECK_CIRCLE_O, SPUIStyleDefinitions.STATUS_ICON_PENDING);
        builder.add(Status.WARNING, VaadinIcons.EXCLAMATION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_ORANGE);
        builder.add(Status.DOWNLOAD, VaadinIcons.CLOUD_DOWNLOAD, SPUIStyleDefinitions.STATUS_ICON_PENDING);
        builder.add(Status.DOWNLOADED, VaadinIcons.CLOUD_DOWNLOAD, SPUIStyleDefinitions.STATUS_ICON_GREEN);
        builder.add(Status.CANCELING, VaadinIcons.CLOSE_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_PENDING);
        builder.add(Status.CANCELED, VaadinIcons.CLOSE_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_GREEN);
        builder.add(Status.ERROR, VaadinIcons.EXCLAMATION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_RED);
        return builder.build();
    }

    /**
     * Generate {@link ProxyFontIcon}s mapped to their {@link RolloutStatus}
     * 
     * @param i18n
     *            message source for internationalization
     * @return map of icons and their states
     */
    public static Map<RolloutStatus, ProxyFontIcon> generateRolloutStatusIcons(final VaadinMessageSource i18n) {

        final IconMapBuilderWithGeneratedDesc<RolloutStatus> builder = new IconMapBuilderWithGeneratedDesc<>(
                RolloutStatus.class, i18n,
                status -> UIMessageIdProvider.TOOLTIP_ROLLOUT_STATUS_PREFIX + status.toString());

        builder.add(RolloutStatus.FINISHED, VaadinIcons.CHECK_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_GREEN);
        builder.add(RolloutStatus.PAUSED, VaadinIcons.PAUSE, SPUIStyleDefinitions.STATUS_ICON_BLUE);
        builder.add(RolloutStatus.RUNNING, null, SPUIStyleDefinitions.STATUS_SPINNER_YELLOW);
        builder.add(RolloutStatus.WAITING_FOR_APPROVAL, VaadinIcons.HOURGLASS, SPUIStyleDefinitions.STATUS_ICON_ORANGE);
        builder.add(RolloutStatus.APPROVAL_DENIED, VaadinIcons.CLOSE_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_RED);
        builder.add(RolloutStatus.READY, VaadinIcons.BULLSEYE, SPUIStyleDefinitions.STATUS_ICON_LIGHT_BLUE);
        builder.add(RolloutStatus.STOPPED, VaadinIcons.STOP, SPUIStyleDefinitions.STATUS_ICON_RED);
        builder.add(RolloutStatus.CREATING, null, SPUIStyleDefinitions.STATUS_SPINNER_GREY);
        builder.add(RolloutStatus.STARTING, null, SPUIStyleDefinitions.STATUS_SPINNER_BLUE);
        builder.add(RolloutStatus.DELETING, null, SPUIStyleDefinitions.STATUS_SPINNER_RED);
        return builder.build();
    }

    /**
     * Generate {@link ProxyFontIcon}s mapped to their
     * {@link RolloutGroupStatus}
     * 
     * @param i18n
     *            message source for internationalization
     * @return map of icons and their states
     */
    public static Map<RolloutGroupStatus, ProxyFontIcon> generateRolloutGroupStatusIcons(
            final VaadinMessageSource i18n) {

        final IconMapBuilderWithGeneratedDesc<RolloutGroupStatus> builder = new IconMapBuilderWithGeneratedDesc<>(
                RolloutGroupStatus.class, i18n,
                status -> UIMessageIdProvider.TOOLTIP_ROLLOUT_GROUP_STATUS_PREFIX + status.toString());

        builder.add(RolloutGroupStatus.FINISHED, VaadinIcons.CHECK_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_GREEN);
        builder.add(RolloutGroupStatus.RUNNING, VaadinIcons.ADJUST, SPUIStyleDefinitions.STATUS_ICON_YELLOW);
        builder.add(RolloutGroupStatus.READY, VaadinIcons.BULLSEYE, SPUIStyleDefinitions.STATUS_ICON_LIGHT_BLUE);
        builder.add(RolloutGroupStatus.ERROR, VaadinIcons.EXCLAMATION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_RED);
        builder.add(RolloutGroupStatus.SCHEDULED, VaadinIcons.HOURGLASS_START,
                SPUIStyleDefinitions.STATUS_ICON_PENDING);
        return builder.build();
    }

    /**
     * Generate {@link ProxyFontIcon}s showing the active-status of actions
     * mapped to their {@link IsActiveDecoration}
     * 
     * @param i18n
     *            message source for internationalization
     * @return map of icons and their states
     */
    public static Map<IsActiveDecoration, ProxyFontIcon> generateActiveStatusIcons(final VaadinMessageSource i18n) {

        final IconMapBuilderWithGeneratedDesc<IsActiveDecoration> builder = new IconMapBuilderWithGeneratedDesc<>(
                IsActiveDecoration.class, i18n,
                status -> UIMessageIdProvider.TOOLTIP_ACTIVE_ACTION_STATUS_PREFIX + status.getMsgName());

        builder.add(IsActiveDecoration.ACTIVE, null, SPUIStyleDefinitions.STATUS_ICON_ACTIVE);
        builder.add(IsActiveDecoration.SCHEDULED, VaadinIcons.HOURGLASS_EMPTY,
                SPUIStyleDefinitions.STATUS_ICON_PENDING);
        builder.add(IsActiveDecoration.IN_ACTIVE, VaadinIcons.CHECK_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL);
        builder.add(IsActiveDecoration.IN_ACTIVE_ERROR, VaadinIcons.CHECK_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_RED);
        return builder.build();
    }

    /**
     * Generate {@link ProxyFontIcon}s mapped to their {@link ActionType}
     * 
     * @param i18n
     *            message source for internationalization
     * @return map of icons and their states
     */
    public static Map<ActionType, ProxyFontIcon> generateActionTypeIcons(final VaadinMessageSource i18n) {

        final IconMapBuilder<ActionType> builder = new IconMapBuilder<>(ActionType.class, i18n);

        builder.add(ActionType.FORCED, VaadinIcons.BOLT, SPUIStyleDefinitions.STATUS_ICON_FORCED,
                UIMessageIdProvider.CAPTION_ACTION_FORCED);
        builder.add(ActionType.TIMEFORCED, VaadinIcons.BOLT, SPUIStyleDefinitions.STATUS_ICON_FORCED,
                UIMessageIdProvider.CAPTION_ACTION_FORCED);
        builder.add(ActionType.SOFT, VaadinIcons.STEP_FORWARD, SPUIStyleDefinitions.STATUS_ICON_SOFT,
                UIMessageIdProvider.CAPTION_ACTION_SOFT);
        builder.add(ActionType.DOWNLOAD_ONLY, VaadinIcons.DOWNLOAD, SPUIStyleDefinitions.STATUS_ICON_DOWNLOAD_ONLY,
                UIMessageIdProvider.CAPTION_ACTION_DOWNLOAD_ONLY);
        return builder.build();
    }

    private static class IconMapBuilder<T extends Enum<T>> {
        private final Map<T, ProxyFontIcon> statusIconMap;
        private final VaadinMessageSource i18n;

        protected IconMapBuilder(final Class<T> statusType, final VaadinMessageSource i18n) {
            statusIconMap = new EnumMap<>(statusType);
            this.i18n = i18n;
        }

        protected void add(final T status, final FontIcon icon, final String style, final String description) {
            statusIconMap.put(status, new ProxyFontIcon(icon, style, i18n.getMessage(description)));
        }

        protected Map<T, ProxyFontIcon> build() {
            return Collections.unmodifiableMap(statusIconMap);
        }
    }

    private static final class IconMapBuilderWithGeneratedDesc<T extends Enum<T>> extends IconMapBuilder<T> {
        private final Function<T, String> tooltipMessageGenerator;

        private IconMapBuilderWithGeneratedDesc(final Class<T> statusType, final VaadinMessageSource i18n,
                final Function<T, String> tooltipMessageGenerator) {
            super(statusType, i18n);
            this.tooltipMessageGenerator = tooltipMessageGenerator;
        }

        private void add(final T status, final FontIcon icon, final String style) {
            final String description = tooltipMessageGenerator.apply(status);
            add(status, icon, style, description);
        }

    }

}
