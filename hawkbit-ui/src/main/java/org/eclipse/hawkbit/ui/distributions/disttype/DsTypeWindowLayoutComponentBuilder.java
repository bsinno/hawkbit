/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.disttype;

import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.CollectionUtils;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationResult;

public class DsTypeWindowLayoutComponentBuilder {

    private final VaadinMessageSource i18n;
    private final SoftwareModuleTypeManagement softwareModuleTypeManagement;

    public DsTypeWindowLayoutComponentBuilder(final VaadinMessageSource i18n,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement) {
        this.i18n = i18n;
        this.softwareModuleTypeManagement = softwareModuleTypeManagement;
    }

    public DsTypeSmSelectLayout createDsTypeSmSelectLayout(final Binder<ProxyType> binder) {
        final DsTypeSmSelectLayout dsTypeSmSelectLayout = new DsTypeSmSelectLayout(i18n, softwareModuleTypeManagement);
        dsTypeSmSelectLayout.setRequiredIndicatorVisible(true);

        binder.forField(dsTypeSmSelectLayout)
                .withValidator((selectedSmTypes, context) -> CollectionUtils.isEmpty(selectedSmTypes)
                        ? ValidationResult.error(i18n.getMessage("message.error.noSmTypeSelected"))
                        : ValidationResult.ok())
                .bind(ProxyType::getSelectedSmTypes, ProxyType::setSelectedSmTypes);

        return dsTypeSmSelectLayout;
    }

}
