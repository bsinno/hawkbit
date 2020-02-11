/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import java.util.List;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;

/**
 * Target tag layout in bulk upload popup.
 *
 */
public class BulkUploadTagsComponent extends CustomField<List<ProxyTag>> {
    private static final long serialVersionUID = 1L;

    private final transient TargetBulkTokenTags targetBulkTokenTags;

    public BulkUploadTagsComponent(final TargetBulkTokenTags targetBulkTokenTags) {
        this.targetBulkTokenTags = targetBulkTokenTags;
    }

    @Override
    public List<ProxyTag> getValue() {
        return targetBulkTokenTags.getSelectedTagsForAssignment();
    }

    @Override
    protected Component initContent() {
        // init with dummy master entity in order to init tag panel
        targetBulkTokenTags.updateMasterEntityFilter(new ProxyTarget());

        return targetBulkTokenTags.getTagPanel();
    }

    @Override
    protected void doSetValue(final List<ProxyTag> value) {
        // TODO: check why converter is not working in
        // BulkUploadWindowLayoutComponentBuilder
        if (value == null) {
            return;
        }

        value.forEach(tag -> targetBulkTokenTags.getTagPanel().setAssignedTag(tag));
    }

}
