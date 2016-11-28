/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.event;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.hawkbit.ui.common.AbstractAcceptCriteria;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.google.common.collect.Maps;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Component;

/**
 * Distributions View for Accept criteria.
 * 
 */
@SpringComponent
@UIScope
public class DistributionsViewAcceptCriteria extends AbstractAcceptCriteria {

    private static final long serialVersionUID = -7686564967583118935L;

    private static final Map<String, Object> DROP_HINTS_CONFIGS = createDropHintConfigurations();

    private static final Map<String, List<String>> DROP_CONFIGS = createDropConfigurations();

    @Autowired
    DistributionsViewAcceptCriteria(final UINotification uiNotification, final UIEventBus eventBus) {
        super(uiNotification, eventBus);
    }

    @Override
    protected String getComponentId(final Component component) {
        String id = component.getId();
        if (isDistributionTypeButtonId(component.getId())) {
            id = SPUIDefinitions.DISTRIBUTION_TYPE_ID_PREFIXS;
        } else if (isSoftwareTypeButtonId(component.getId())) {
            id = SPUIDefinitions.SOFTWARE_MODULE_TAG_ID_PREFIXS;
        }
        return id;
    }

    @Override
    protected Map<String, Object> getDropHintConfigurations() {
        return DROP_HINTS_CONFIGS;
    }

    @Override
    protected Map<String, List<String>> getDropConfigurations() {
        return DROP_CONFIGS;
    }

    private boolean isDistributionTypeButtonId(final String id) {
        return id != null && (id.startsWith(SPUIDefinitions.DISTRIBUTION_TYPE_ID_PREFIXS)
                || id.startsWith(SPUIDefinitions.DISTRIBUTION_SET_TYPE_ID_PREFIXS));
    }

    private boolean isSoftwareTypeButtonId(final String id) {
        return id != null && id.startsWith(SPUIDefinitions.SOFTWARE_MODULE_TAG_ID_PREFIXS);
    }

    private static Map<String, List<String>> createDropConfigurations() {
        final Map<String, List<String>> config = Maps.newHashMapWithExpectedSize(2);

        // Delete drop area droppable components
        config.put(UIComponentIdProvider.DELETE_BUTTON_WRAPPER_ID,
                Arrays.asList(SPUIDefinitions.DISTRIBUTION_TYPE_ID_PREFIXS, UIComponentIdProvider.DIST_TABLE_ID,
                        UIComponentIdProvider.UPLOAD_SOFTWARE_MODULE_TABLE,
                        SPUIDefinitions.SOFTWARE_MODULE_TAG_ID_PREFIXS));

        // Distribution table drop components
        config.put(UIComponentIdProvider.DIST_TABLE_ID,
                Arrays.asList(UIComponentIdProvider.UPLOAD_SOFTWARE_MODULE_TABLE));

        return config;
    }

    private static Map<String, Object> createDropHintConfigurations() {
        final Map<String, Object> config = Maps.newHashMapWithExpectedSize(4);
        config.put(SPUIDefinitions.DISTRIBUTION_TYPE_ID_PREFIXS, DragEvent.DISTRIBUTION_TYPE_DRAG);
        config.put(UIComponentIdProvider.DIST_TABLE_ID, DragEvent.DISTRIBUTION_DRAG);
        config.put(UIComponentIdProvider.UPLOAD_SOFTWARE_MODULE_TABLE, DragEvent.SOFTWAREMODULE_DRAG);
        config.put(SPUIDefinitions.SOFTWARE_MODULE_TAG_ID_PREFIXS, DragEvent.SOFTWAREMODULE_TYPE_DRAG);
        return config;
    }

}
