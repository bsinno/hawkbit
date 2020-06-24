/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.layouts;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;

import com.vaadin.data.ValidationException;
import com.vaadin.ui.GridLayout;

/**
 * Layout builder for Update Rollout window.
 */
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2160" })
public class UpdateRolloutWindowLayout extends AbstractRolloutWindowLayout {
    protected final RolloutFormLayout rolloutFormLayout;
    private final VisualGroupDefinitionLayout visualGroupDefinitionLayout;

    public UpdateRolloutWindowLayout(final RolloutWindowDependencies dependencies) {
        super(dependencies);

        this.rolloutFormLayout = rolloutComponentBuilder.createRolloutFormLayout();
        this.visualGroupDefinitionLayout = rolloutComponentBuilder.createVisualGroupDefinitionLayout();

        addValidatableLayout(rolloutFormLayout);
    }

    @Override
    protected void addComponents(final GridLayout rootLayout) {
        rootLayout.setRows(6);

        final int lastColumnIdx = rootLayout.getColumns() - 1;

        rolloutFormLayout.addFormToEditLayout(rootLayout);
        visualGroupDefinitionLayout.addChartWithLegendToLayout(rootLayout, lastColumnIdx, 3);
    }

    @Override
    public void setEntity(final ProxyRolloutWindow proxyEntity) {
        rolloutFormLayout.setBean(proxyEntity.getRolloutForm());
        visualGroupDefinitionLayout.setGroupDefinitionMode(proxyEntity.getGroupDefinitionMode());
        visualGroupDefinitionLayout.setTotalTargets(proxyEntity.getTotalTargets());
        visualGroupDefinitionLayout
                .setAdvancedRolloutGroupDefinitions(proxyEntity.getAdvancedRolloutGroupDefinitions());
    }

    @Override
    public ProxyRolloutWindow getValidatableEntity() throws ValidationException {
        final ProxyRolloutWindow proxyEntity = new ProxyRolloutWindow();
        proxyEntity.setRolloutForm(rolloutFormLayout.getBean());

        return proxyEntity;
    }

    public void resetValidation() {
        rolloutFormLayout.resetValidationStatus();
    }

    public void adaptForPendingStatus() {
        rolloutFormLayout.disableFieldsOnEditForInActive();
    }

    public void adaptForStartedStatus() {
        rolloutFormLayout.disableFieldsOnEditForActive();
    }
}
