/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import org.eclipse.hawkbit.ui.common.layout.AbstractFooterSupport;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;

/**
 * Count message label which display current filter details and details on
 * pinning.
 */
public class TargetFilterCountMessageLabel extends AbstractFooterSupport {

    private final VaadinMessageSource i18n;

    private final Label targetCountLabel;

    public TargetFilterCountMessageLabel(final VaadinMessageSource i18n) {
        this.i18n = i18n;
        this.targetCountLabel = new Label();
        init();
    }

    private void init() {
        targetCountLabel.setId(UIComponentIdProvider.COUNT_LABEL);
        targetCountLabel.setContentMode(ContentMode.HTML);
        targetCountLabel.addStyleName(SPUIStyleDefinitions.SP_LABEL_MESSAGE_STYLE);

        targetCountLabel.setCaption(
                new StringBuilder(i18n.getMessage("label.target.filtered.total")).append(" : ").append(0).toString());
    }

    @Override
    protected Label getFooterMessageLabel() {
        return targetCountLabel;
    }

    public void updateTotalFilteredTargetsCount(final long count) {
        final StringBuilder targetMessage = new StringBuilder(i18n.getMessage("label.target.filtered.total"));
        targetMessage.append(" : ");
        targetMessage.append(count);
        targetCountLabel.setCaption(targetMessage.toString());
    }
}
