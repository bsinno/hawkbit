/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.smtable;

import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.ui.artifacts.smtable.SmMetaDataWindowBuilder;
import org.eclipse.hawkbit.ui.common.detailslayout.AbstractSoftwareModuleDetails;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Implementation of software module details block using generic abstract
 * details style .
 */
public class SwModuleDetails extends AbstractSoftwareModuleDetails {
    private static final long serialVersionUID = 1L;

    SwModuleDetails(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SoftwareModuleManagement softwareManagement, final SmMetaDataWindowBuilder smMetaDataWindowBuilder) {
        super(i18n, eventBus, softwareManagement, smMetaDataWindowBuilder);

        buildDetails();
    }
}
