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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.builder.DistributionSetTypeUpdate;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.EntityReadOnlyException;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.artifacts.smtype.TypeWindowController;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.data.mappers.TypeToProxyTypeMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.event.DsTypeModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

public class UpdateDsTypeWindowController implements TypeWindowController {
    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final DistributionSetTypeManagement dsTypeManagement;
    private final DistributionSetManagement dsManagement;
    private final TypeToProxyTypeMapper<SoftwareModuleType> smTypeToProxyTypeMapper;

    private final DsTypeWindowLayout layout;

    private ProxyType type;
    private String typeNameBeforeEdit;
    private String typeKeyBeforeEdit;

    public UpdateDsTypeWindowController(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final UIEventBus eventBus, final UINotification uiNotification,
            final DistributionSetTypeManagement dsTypeManagement, final DistributionSetManagement dsManagement,
            final DsTypeWindowLayout layout) {
        this.i18n = i18n;
        this.entityFactory = entityFactory;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;

        this.dsTypeManagement = dsTypeManagement;
        this.dsManagement = dsManagement;

        this.smTypeToProxyTypeMapper = new TypeToProxyTypeMapper<>();

        this.layout = layout;
    }

    @Override
    public DsTypeWindowLayout getLayout() {
        return layout;
    }

    @Override
    public void populateWithData(final ProxyType proxyType) {
        type = new ProxyType();

        type.setId(proxyType.getId());
        type.setName(proxyType.getName());
        type.setDescription(proxyType.getDescription());
        type.setColour(StringUtils.hasText(proxyType.getColour()) ? proxyType.getColour() : "#2c9720");
        type.setKey(proxyType.getKey());

        if (dsManagement.countByTypeId(proxyType.getId()) <= 0) {
            type.setSelectedSmTypes(getSmTypesByDsTypeId(proxyType.getId()));
        } else {
            uiNotification.displayValidationError(
                    proxyType.getName() + "  " + i18n.getMessage("message.error.dist.set.type.update"));
            layout.disableDsTypeSmSelectLayout();
        }

        typeNameBeforeEdit = proxyType.getName();
        typeKeyBeforeEdit = proxyType.getKey();

        layout.getBinder().setBean(type);
        layout.disableTagName();
        layout.disableTypeKey();
    }

    private Set<ProxyType> getSmTypesByDsTypeId(final Long id) {
        return dsTypeManagement.get(id).map(dsType -> {
            final Stream<ProxyType> mandatorySmTypeStream = dsType.getMandatoryModuleTypes().stream()
                    .map(mandatorySmType -> {
                        final ProxyType mappedType = smTypeToProxyTypeMapper.map(mandatorySmType);
                        mappedType.setMandatory(true);

                        return mappedType;
                    });

            final Stream<ProxyType> optionalSmTypeStream = dsType.getOptionalModuleTypes().stream()
                    .map(optionalSmType -> {
                        final ProxyType mappedType = smTypeToProxyTypeMapper.map(optionalSmType);
                        mappedType.setMandatory(false);

                        return mappedType;
                    });

            return Stream.concat(mandatorySmTypeStream, optionalSmTypeStream).collect(Collectors.toSet());
        }).orElse(null);
    }

    @Override
    public SaveDialogCloseListener getSaveDialogCloseListener() {
        return new SaveDialogCloseListener() {
            @Override
            public void saveOrUpdate() {
                editType();
            }

            @Override
            public boolean canWindowSaveOrUpdate() {
                return duplicateCheckForEdit();
            }
        };
    }

    private void editType() {
        if (type == null) {
            return;
        }

        final DistributionSetTypeUpdate dsTypeUpdate = entityFactory.distributionSetType().update(type.getId())
                .description(type.getDescription()).colour(type.getColour());

        if (dsManagement.countByTypeId(type.getId()) <= 0 && !CollectionUtils.isEmpty(type.getSelectedSmTypes())) {
            final List<Long> mandatorySmTypeIds = type.getSelectedSmTypes().stream().filter(ProxyType::isMandatory)
                    .map(ProxyType::getId).collect(Collectors.toList());

            final List<Long> optionalSmTypeIds = type.getSelectedSmTypes().stream()
                    .filter(selectedSmType -> !selectedSmType.isMandatory()).map(ProxyType::getId)
                    .collect(Collectors.toList());

            dsTypeUpdate.mandatory(mandatorySmTypeIds).optional(optionalSmTypeIds);
        }

        final DistributionSetType updatedDsType;
        try {
            updatedDsType = dsTypeManagement.update(dsTypeUpdate);
        } catch (final EntityNotFoundException | EntityReadOnlyException e) {
            // TODO: use i18n
            uiNotification.displayWarning(
                    "Type with name " + type.getName() + " was deleted or you are not allowed to update it");
            return;
        }

        uiNotification.displaySuccess(i18n.getMessage("message.update.success", updatedDsType.getName()));
        // TODO: verify if sender is correct
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new DsTypeModifiedEventPayload(EntityModifiedEventType.ENTITY_UPDATED, updatedDsType.getId()));
    }

    private boolean duplicateCheckForEdit() {
        // TODO: check if another message should be shown when selected sm types
        // are empty
        if (!StringUtils.hasText(type.getName()) || !StringUtils.hasText(type.getKey())
                || CollectionUtils.isEmpty(type.getSelectedSmTypes())) {
            uiNotification.displayValidationError(i18n.getMessage("message.error.missing.typenameorkey"));
            return false;
        }
        if (!typeNameBeforeEdit.equals(getTrimmedTypeName())
                && dsTypeManagement.getByName(getTrimmedTypeName()).isPresent()) {
            // TODO: is the notification right here?
            uiNotification.displayValidationError(i18n.getMessage("message.tag.duplicate.check", getTrimmedTypeName()));
            return false;
        }
        if (!typeKeyBeforeEdit.equals(getTrimmedTypeKey())
                && dsTypeManagement.getByKey(getTrimmedTypeKey()).isPresent()) {
            // TODO: is the notification right here?
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
