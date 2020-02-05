/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.smtype.filter;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.smtype.SmTypeWindowBuilder;
import org.eclipse.hawkbit.ui.artifacts.smtype.filter.SMTypeFilterHeader;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Software Module Type filter buttons header.
 */
public class DistSMTypeFilterHeader extends SMTypeFilterHeader {
    private static final long serialVersionUID = 1L;

    private final DistSMTypeFilterLayoutUiState distSMTypeFilterLayoutUiState;

    public DistSMTypeFilterHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final DistSMTypeFilterLayoutUiState distSMTypeFilterLayoutUiState,
            final SmTypeWindowBuilder smTypeWindowBuilder) {
        super(i18n, permChecker, eventBus, null, smTypeWindowBuilder);

        this.distSMTypeFilterLayoutUiState = distSMTypeFilterLayoutUiState;
    }

    @Override
    protected void updateHiddenUiState() {
        distSMTypeFilterLayoutUiState.setHidden(true);
    }

    @Override
    protected View getView() {
        return View.DISTRIBUTIONS;
    }
}
