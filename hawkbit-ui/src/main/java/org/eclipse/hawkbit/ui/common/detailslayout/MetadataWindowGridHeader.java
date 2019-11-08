/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.Arrays;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.AddHeaderSupport;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Component;

/**
 * Target table header layout.
 */
public class MetadataWindowGridHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final transient AddHeaderSupport addHeaderSupport;

    public MetadataWindowGridHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final Runnable addNewItemCallback) {
        super(i18n, permChecker, eventBus);

        // TODO: consider moving permission check to header support or parent
        // header
        if (permChecker.hasCreateRepositoryPermission()) {
            this.addHeaderSupport = new AddHeaderSupport(i18n, UIComponentIdProvider.METADTA_ADD_ICON_ID,
                    addNewItemCallback, () -> false);
        } else {
            this.addHeaderSupport = null;
        }

        addHeaderSupports(Arrays.asList(addHeaderSupport));

        buildHeader();
    }

    @Override
    protected void init() {
        super.init();

        setWidth("100%");
        setHeight("30px");
        addStyleName("metadata-table-margin");
    }

    @Override
    protected Component getHeaderCaption() {
        return new LabelBuilder().name(i18n.getMessage("caption.metadata")).buildCaptionLabel();
    }
}
