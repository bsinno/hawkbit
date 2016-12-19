/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions;

import java.util.Arrays;
import java.util.List;

import org.eclipse.hawkbit.im.authentication.SpPermission;
import org.eclipse.hawkbit.ui.management.AbstractDashboardMenuItemNotification;
import org.springframework.core.annotation.Order;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;

/**
 * Menu item for distributions view.
 * 
 *
 * 
 */
@SpringComponent
@UIScope
@Order(400)
public class DistributionsViewMenuItem extends AbstractDashboardMenuItemNotification {

    private static final long serialVersionUID = -4048522766974227222L;

    @Override
    public String getViewName() {
        return DistributionsView.VIEW_NAME;
    }

    @Override
    public Resource getDashboardIcon() {
        return FontAwesome.BRIEFCASE;
    }

    @Override
    public String getDashboardCaption() {
        return "Distributions";
    }

    @Override
    public String getDashboardCaptionLong() {
        return "Distributions Management";
    }

    @Override
    public List<String> getPermissions() {
        return Arrays.asList(SpPermission.CREATE_REPOSITORY, SpPermission.READ_REPOSITORY,
                SpPermission.UPDATE_REPOSITORY);
    }

}
