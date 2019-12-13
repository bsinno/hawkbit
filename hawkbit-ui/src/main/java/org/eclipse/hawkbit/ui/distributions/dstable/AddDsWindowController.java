/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.dstable;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowController;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowLayout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.event.DsModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

public class AddDsWindowController extends AbstractEntityWindowController<ProxyDistributionSet, ProxyDistributionSet> {
    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;
    private final SystemManagement systemManagement;

    private final DistributionSetManagement dsManagement;

    private final DsWindowLayout layout;

    public AddDsWindowController(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final UIEventBus eventBus, final UINotification uiNotification, final SystemManagement systemManagement,
            final DistributionSetManagement dsManagement, final DsWindowLayout layout) {
        this.i18n = i18n;
        this.entityFactory = entityFactory;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;
        this.systemManagement = systemManagement;

        this.dsManagement = dsManagement;

        this.layout = layout;
    }

    @Override
    public AbstractEntityWindowLayout<ProxyDistributionSet> getLayout() {
        return layout;
    }

    @Override
    protected ProxyDistributionSet buildEntityFromProxy(final ProxyDistributionSet proxyEntity) {
        // We ignore the method parameter, because we are interested in the
        // empty object, that we can populate with defaults
        final ProxyDistributionSet newDs = new ProxyDistributionSet();

        final ProxyType newDsType = new ProxyType();
        newDsType.setId(systemManagement.getTenantMetadata().getDefaultDsType().getId());
        newDs.setProxyType(newDsType);

        return newDs;
    }

    @Override
    protected void persistEntity(final ProxyDistributionSet entity) {
        final DistributionSet newDs = dsManagement.create(entityFactory.distributionSet().create()
                .type(entity.getProxyType().getKey()).name(entity.getName()).version(entity.getVersion())
                .description(entity.getDescription()).requiredMigrationStep(entity.isRequiredMigrationStep()));

        uiNotification
                .displaySuccess(i18n.getMessage("message.save.success", newDs.getName() + ":" + newDs.getVersion()));
        // TODO: verify if sender is correct
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new DsModifiedEventPayload(EntityModifiedEventType.ENTITY_ADDED, newDs.getId()));
    }

    @Override
    protected boolean isEntityValid(final ProxyDistributionSet entity) {
        if (!StringUtils.hasText(entity.getName()) || !StringUtils.hasText(entity.getVersion())) {
            uiNotification.displayValidationError(i18n.getMessage("message.error.missing.nameorversion"));
            return false;
        }

        final String trimmedName = StringUtils.trimWhitespace(entity.getName());
        final String trimmedVersion = StringUtils.trimWhitespace(entity.getVersion());
        if (dsManagement.getByNameAndVersion(trimmedName, trimmedVersion).isPresent()) {
            uiNotification
                    .displayValidationError(i18n.getMessage("message.duplicate.dist", trimmedName, trimmedVersion));
            return false;
        }

        return true;
    }
}
