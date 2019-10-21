/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.disttype;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.ui.artifacts.smtype.TypeWindowController;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType.SmTypeAssign;
import org.eclipse.hawkbit.ui.common.event.DsTypeModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

public class AddDsTypeWindowController implements TypeWindowController {
    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final DistributionSetTypeManagement dsTypeManagement;

    private final DsTypeWindowLayout layout;

    private ProxyType type;

    public AddDsTypeWindowController(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final UIEventBus eventBus, final UINotification uiNotification,
            final DistributionSetTypeManagement dsTypeManagement, final DsTypeWindowLayout layout) {
        this.i18n = i18n;
        this.entityFactory = entityFactory;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;

        this.dsTypeManagement = dsTypeManagement;

        this.layout = layout;
    }

    @Override
    public DsTypeWindowLayout getLayout() {
        return layout;
    }

    @Override
    public void populateWithData(final ProxyType proxyType) {
        // We ignore the method parameter, because we are interested in the
        // empty object, that we can populate with defaults
        type = new ProxyType();
        // TODO: either extract the constant, or define it as a default in model
        type.setColour("#2c9720");
        type.setSmTypeAssign(SmTypeAssign.SINGLE);

        layout.getBinder().setBean(type);
    }

    @Override
    public SaveDialogCloseListener getSaveDialogCloseListener() {
        return new SaveDialogCloseListener() {
            @Override
            public void saveOrUpdate() {
                saveSmType();
            }

            @Override
            public boolean canWindowSaveOrUpdate() {
                return duplicateCheck();
            }
        };
    }

    private void saveSmType() {

        final List<Long> mandatorySmTypeIds = type.getSelectedSmTypes().stream().filter(ProxyType::isMandatory)
                .map(ProxyType::getId).collect(Collectors.toList());

        final List<Long> optionalSmTypeIds = type.getSelectedSmTypes().stream()
                .filter(selectedSmType -> !selectedSmType.isMandatory()).map(ProxyType::getId)
                .collect(Collectors.toList());

        final DistributionSetType newDsType = dsTypeManagement.create(entityFactory.distributionSetType().create()
                .key(type.getKey()).name(type.getName()).description(type.getDescription()).colour(type.getColour())
                .mandatory(mandatorySmTypeIds).optional(optionalSmTypeIds));

        uiNotification.displaySuccess(i18n.getMessage("message.save.success", newDsType.getName()));
        // TODO: verify if sender is correct
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new DsTypeModifiedEventPayload(EntityModifiedEventType.ENTITY_ADDED, newDsType.getId()));
    }

    private boolean duplicateCheck() {
        // TODO: check if another message should be shown when selected sm types
        // are empty
        if (!StringUtils.hasText(type.getName()) || !StringUtils.hasText(type.getKey())
                || CollectionUtils.isEmpty(type.getSelectedSmTypes())) {
            uiNotification.displayValidationError(i18n.getMessage("message.error.missing.typenameorkey"));
            return false;
        }
        // TODO: check if this is correct
        if (dsTypeManagement.getByName(getTrimmedTypeName()).isPresent()) {
            uiNotification.displayValidationError(i18n.getMessage("message.tag.duplicate.check", getTrimmedTypeName()));
            return false;
        }
        if (dsTypeManagement.getByKey(getTrimmedTypeKey()).isPresent()) {
            uiNotification.displayValidationError(
                    i18n.getMessage("message.type.key.swmodule.duplicate.check", getTrimmedTypeKey()));
            return false;
        }
        return true;
    }

    private String getTrimmedTypeName() {
        return StringUtils.trimWhitespace(type.getName());
    }

    private String getTrimmedTypeKey() {
        return StringUtils.trimWhitespace(type.getKey());
    }
}
