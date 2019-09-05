/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.header.support;

import java.util.function.BooleanSupplier;

import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;

public class BulkUploadHeaderSupport {
    private final VaadinMessageSource i18n;

    private final Button bulkUploadIcon;
    private final Runnable bulkUploadCallback;
    private final BooleanSupplier bulkUploadInProgressStateSupplier;
    private final BooleanSupplier maximizedStateSupplier;

    public BulkUploadHeaderSupport(final VaadinMessageSource i18n, final Runnable bulkUploadCallback,
            final BooleanSupplier bulkUploadInProgressStateSupplier, final BooleanSupplier maximizedStateSupplier) {
        this.i18n = i18n;

        this.bulkUploadCallback = bulkUploadCallback;
        this.bulkUploadInProgressStateSupplier = bulkUploadInProgressStateSupplier;
        this.maximizedStateSupplier = maximizedStateSupplier;

        this.bulkUploadIcon = createBulkUploadIcon();
    }

    private Button createBulkUploadIcon() {
        final Button bulkUploadButton = SPUIComponentProvider.getButton(
                UIComponentIdProvider.TARGET_TBL_BULK_UPLOAD_ICON_ID, "",
                i18n.getMessage(UIMessageIdProvider.TOOLTIP_BULK_UPLOAD), null, false, VaadinIcons.UPLOAD,
                SPUIButtonStyleNoBorder.class);

        bulkUploadButton.addClickListener(event -> bulkUploadCallback.run());

        return bulkUploadButton;
    }

    public Button getBulkUploadIcon() {
        return bulkUploadIcon;
    }

    public void disableBulkUpload() {
        bulkUploadIcon.setEnabled(false);
    }

    public void enableBulkUpload() {
        bulkUploadIcon.setEnabled(true);
    }

    public void hideBulkUpload() {
        bulkUploadIcon.setVisible(false);
    }

    public void showBulkUpload() {
        bulkUploadIcon.setVisible(true);
    }

    public void restoreBulkUploadState() {
        if (maximizedStateSupplier.getAsBoolean()) {
            hideBulkUpload();
        }

        if (bulkUploadInProgressStateSupplier.getAsBoolean()) {
            disableBulkUpload();
        }
    }
}
