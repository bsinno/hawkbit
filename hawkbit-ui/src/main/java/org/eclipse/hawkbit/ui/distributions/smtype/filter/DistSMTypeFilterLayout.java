/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.smtype.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.repository.model.Type;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.smtype.SmTypeWindowBuilder;
import org.eclipse.hawkbit.ui.common.data.mappers.TypeToProxyTypeMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterLayout;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedGridRefreshAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener.EntityModifiedAwareSupport;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.server.Page;
import com.vaadin.ui.ComponentContainer;

/**
 * Software Module Type filter layout.
 */
public class DistSMTypeFilterLayout extends AbstractFilterLayout {
    private static final long serialVersionUID = 1L;

    private final transient SoftwareModuleTypeManagement softwareModuleTypeManagement;
    private final transient TypeToProxyTypeMapper<SoftwareModuleType> smTypeMapper;

    private final DistSMTypeFilterHeader distSMTypeFilterHeader;
    private final DistSMTypeFilterButtons distSMTypeFilterButtons;

    private final transient DistSMTypeFilterLayoutEventListener eventListener;

    private final transient EntityModifiedListener<ProxyType> entityModifiedListener;

    /**
     * Constructor
     * 
     * @param eventBus
     *            UIEventBus
     * @param i18n
     *            VaadinMessageSource
     * @param permChecker
     *            SpPermissionChecker
     * @param entityFactory
     *            EntityFactory
     * @param uiNotification
     *            UINotification
     * @param softwareModuleTypeManagement
     *            SoftwareModuleTypeManagement
     */
    public DistSMTypeFilterLayout(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final SpPermissionChecker permChecker, final EntityFactory entityFactory,
            final UINotification uiNotification, final SoftwareModuleTypeManagement softwareModuleTypeManagement,
            final DistSMTypeFilterLayoutUiState distSMTypeFilterLayoutUiState) {
        this.softwareModuleTypeManagement = softwareModuleTypeManagement;
        this.smTypeMapper = new TypeToProxyTypeMapper<>();

        final SmTypeWindowBuilder smTypeWindowBuilder = new SmTypeWindowBuilder(i18n, entityFactory, eventBus,
                uiNotification, softwareModuleTypeManagement);

        this.distSMTypeFilterHeader = new DistSMTypeFilterHeader(i18n, permChecker, eventBus,
                distSMTypeFilterLayoutUiState, smTypeWindowBuilder);
        this.distSMTypeFilterButtons = new DistSMTypeFilterButtons(eventBus, softwareModuleTypeManagement, i18n,
                entityFactory, permChecker, uiNotification, distSMTypeFilterLayoutUiState, smTypeWindowBuilder,
                smTypeMapper);

        this.eventListener = new DistSMTypeFilterLayoutEventListener(this, eventBus);

        this.entityModifiedListener = new EntityModifiedListener.Builder<>(eventBus, ProxyType.class)
                .entityModifiedAwareSupports(getEntityModifiedAwareSupports())
                .parentEntityType(ProxySoftwareModule.class).build();

        updateSmTypeStyles();
        buildLayout();
    }

    private List<EntityModifiedAwareSupport> getEntityModifiedAwareSupports() {
        return Collections
                .singletonList(EntityModifiedGridRefreshAwareSupport.of(distSMTypeFilterButtons::refreshContainer));
    }

    private void updateSmTypeStyles() {
        final String recreateStylesheetScript = String.format("const stylesheet = recreateStylesheet('%s').sheet;",
                UIComponentIdProvider.SM_TYPE_COLOR_STYLE);
        final String addStyleRulesScript = buildStyleRulesScript(getSmTypeIdWithColor(getAllSmTypes()));

        Page.getCurrent().getJavaScript().execute(recreateStylesheetScript + addStyleRulesScript);
    }

    private List<SoftwareModuleType> getAllSmTypes() {
        Pageable query = PageRequest.of(0, SPUIDefinitions.PAGE_SIZE);
        Slice<SoftwareModuleType> smTypeSlice;
        final List<SoftwareModuleType> smTypes = new ArrayList<>();

        do {
            smTypeSlice = softwareModuleTypeManagement.findAll(query);
            smTypes.addAll(smTypeSlice.getContent());
        } while ((query = smTypeSlice.nextPageable()) != Pageable.unpaged());

        return smTypes;
    }

    private Map<Long, String> getSmTypeIdWithColor(final List<SoftwareModuleType> smTypes) {
        return smTypes.stream().collect(Collectors.toMap(Type::getId,
                type -> Optional.ofNullable(type.getColour()).orElse(SPUIDefinitions.DEFAULT_COLOR)));
    }

    private String buildStyleRulesScript(final Map<Long, String> typeIdWithColor) {
        return typeIdWithColor.entrySet().stream().map(entry -> {
            final String typeClass = String.join("-", UIComponentIdProvider.SM_TYPE_COLOR_CLASS,
                    String.valueOf(entry.getKey()));
            final String typeColor = entry.getValue();

            // !important is needed because we are overriding valo theme here
            // (alternatively we could provide more specific selector)
            return String.format(
                    "addStyleRule(stylesheet, '.%1$s, .%1$s > td, .%1$s .v-grid-cell', "
                            + "'background-color:%2$s !important; background-image: none !important;')",
                    typeClass, typeColor);
        }).collect(Collectors.joining(";"));
    }

    @Override
    protected DistSMTypeFilterHeader getFilterHeader() {
        return distSMTypeFilterHeader;
    }

    @Override
    protected ComponentContainer getFilterContent() {
        return wrapFilterContent(distSMTypeFilterButtons);
    }

    public void restoreState() {
        distSMTypeFilterButtons.restoreState();
    }

    public void showFilterButtonsEditIcon() {
        distSMTypeFilterButtons.showEditColumn();
    }

    public void showFilterButtonsDeleteIcon() {
        distSMTypeFilterButtons.showDeleteColumn();
    }

    public void hideFilterButtonsActionIcons() {
        distSMTypeFilterButtons.hideActionColumns();
    }

    public void refreshFilterButtons() {
        distSMTypeFilterButtons.refreshContainer();
        updateSmTypeStyles();
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();

        entityModifiedListener.unsubscribe();
    }

    public Layout getLayout() {
        return Layout.SM_TYPE_FILTER;
    }
}
