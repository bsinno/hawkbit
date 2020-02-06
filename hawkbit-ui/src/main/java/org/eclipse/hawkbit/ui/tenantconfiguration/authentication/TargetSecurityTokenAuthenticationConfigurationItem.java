/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.tenantconfiguration.authentication;

import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.ui.HorizontalLayout;

/**
 * This class represents the UI item for the target security token section in
 * the authentication configuration view.
 */
public class TargetSecurityTokenAuthenticationConfigurationItem extends HorizontalLayout {

    private static final long serialVersionUID = 1L;

    public TargetSecurityTokenAuthenticationConfigurationItem(final VaadinMessageSource i18n) {
        this.setSpacing(false);
        this.setMargin(false);
        addComponent(new LabelBuilder().name(i18n.getMessage("label.configuration.auth.targettoken")).buildLabel());
    }
}
