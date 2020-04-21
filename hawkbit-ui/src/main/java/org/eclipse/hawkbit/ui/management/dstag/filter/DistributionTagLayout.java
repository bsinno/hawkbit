/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstag.filter;

import java.util.Collections;
import java.util.List;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTagManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterLayout;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener.EntityModifiedAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.support.EntityModifiedGridRefreshAwareSupport;
import org.eclipse.hawkbit.ui.management.dstag.DsTagWindowBuilder;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.VerticalLayout;

/**
 * Layout for Distribution Tags
 *
 */
public class DistributionTagLayout extends AbstractFilterLayout {
    private static final long serialVersionUID = 1L;

    private final DistributionTagFilterHeader distributionTagFilterHeader;
    private final DistributionTagButtons distributionTagButtons;

    private final transient DistributionTagLayoutEventListener eventListener;

    private final transient EntityModifiedListener<ProxyTag> entityModifiedListener;

    /**
     * Constructor
     * 
     * @param eventBus
     *            UIEventBus
     * @param i18n
     *            VaadinMessageSource
     * @param permChecker
     *            SpPermissionChecker
     * @param distributionSetTagManagement
     *            DistributionSetTagManagement
     * @param entityFactory
     *            EntityFactory
     * @param uiNotification
     *            UINotification
     */
    public DistributionTagLayout(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final SpPermissionChecker permChecker, final DistributionSetTagManagement distributionSetTagManagement,
            final EntityFactory entityFactory, final UINotification uiNotification,
            final DistributionSetManagement distributionSetManagement,
            final DistributionTagLayoutUiState distributionTagLayoutUiState) {
        final DsTagWindowBuilder dsTagWindowBuilder = new DsTagWindowBuilder(i18n, entityFactory, eventBus,
                uiNotification, distributionSetTagManagement);

        this.distributionTagFilterHeader = new DistributionTagFilterHeader(i18n, permChecker, eventBus,
                dsTagWindowBuilder, distributionTagLayoutUiState);
        this.distributionTagButtons = new DistributionTagButtons(eventBus, i18n, uiNotification, permChecker,
                distributionSetTagManagement, distributionSetManagement, dsTagWindowBuilder,
                distributionTagLayoutUiState);

        this.eventListener = new DistributionTagLayoutEventListener(this, eventBus);

        this.entityModifiedListener = new EntityModifiedListener.Builder<>(eventBus, ProxyTag.class)
                .entityModifiedAwareSupports(getEntityModifiedAwareSupports())
                .parentEntityType(ProxyDistributionSet.class).build();

        buildLayout();
    }

    private List<EntityModifiedAwareSupport> getEntityModifiedAwareSupports() {
        return Collections
                .singletonList(EntityModifiedGridRefreshAwareSupport.of(distributionTagButtons::refreshContainer));
    }

    @Override
    protected DistributionTagFilterHeader getFilterHeader() {
        return distributionTagFilterHeader;
    }

    @Override
    protected ComponentContainer getFilterContent() {
        final VerticalLayout filterButtonsLayout = wrapFilterContent(distributionTagButtons);

        final Button noTagButton = distributionTagButtons.getNoTagButton();
        filterButtonsLayout.addComponent(noTagButton, 0);
        filterButtonsLayout.setComponentAlignment(noTagButton, Alignment.TOP_LEFT);

        return filterButtonsLayout;
    }

    public void showFilterButtonsEditIcon() {
        distributionTagButtons.showEditColumn();
    }

    public void showFilterButtonsDeleteIcon() {
        distributionTagButtons.showDeleteColumn();
    }

    public void hideFilterButtonsActionIcons() {
        distributionTagButtons.hideActionColumns();
    }

    public void restoreState() {
        distributionTagFilterHeader.restoreState();
        distributionTagButtons.restoreState();
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();

        entityModifiedListener.unsubscribe();
    }

    public Layout getLayout() {
        return Layout.DS_TAG_FILTER;
    }

    public View getView() {
        return View.DEPLOYMENT;
    }
}
