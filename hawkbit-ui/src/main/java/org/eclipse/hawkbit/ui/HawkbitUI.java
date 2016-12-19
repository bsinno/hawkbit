/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.Cookie;

import org.eclipse.hawkbit.ui.components.HawkbitUIErrorHandler;
import org.eclipse.hawkbit.ui.components.NotificationUnreadButton;
import org.eclipse.hawkbit.ui.menu.DashboardEvent.PostViewChangeEvent;
import org.eclipse.hawkbit.ui.menu.DashboardMenu;
import org.eclipse.hawkbit.ui.menu.DashboardMenuItem;
import org.eclipse.hawkbit.ui.push.EventPushStrategy;
import org.eclipse.hawkbit.ui.utils.I18N;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SpringContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.vaadin.spring.events.EventBus;

import com.vaadin.annotations.Title;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.server.ClientConnector.DetachListener;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Vaadin management UI.
 *
 */
@Title("hawkBit Update Server")
public class HawkbitUI extends DefaultHawkbitUI implements DetachListener {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(HawkbitUI.class);

    private static final String EMPTY_VIEW = "";

    private transient EventPushStrategy pushStrategy;

    @Autowired
    protected transient EventBus.UIEventBus eventBus;

    @Autowired
    private SpringViewProvider viewProvider;

    @Autowired
    private transient ApplicationContext context;

    @Autowired
    private I18N i18n;

    @Autowired
    private DashboardMenu dashboardMenu;

    @Autowired
    private ErrorView errorview;

    @Autowired
    private NotificationUnreadButton notificationUnreadButton;

    /**
     * Constructor taking the push strategy.
     *
     * @param pushStrategy
     *            the strategy to push events from the backend to the UI
     */
    public HawkbitUI(final EventPushStrategy pushStrategy) {
        this.pushStrategy = pushStrategy;
    }

    @Override
    public void detach(final DetachEvent event) {
        LOG.info("ManagementUI is detached uiid - {}", getUIId());
        eventBus.unsubscribe(this);
        if (pushStrategy != null) {
            pushStrategy.clean();
        }
    }

    @Override
    protected void init(final VaadinRequest vaadinRequest) {
        LOG.info("ManagementUI init starts uiid - {}", getUI().getUIId());
        if (pushStrategy != null) {
            pushStrategy.init(getUI());
        }
        addDetachListener(this);
        SpringContextHelper.setContext(context);

        Responsive.makeResponsive(this);
        addStyleName(ValoTheme.UI_WITH_MENU);
        setResponsive(Boolean.TRUE);

        final HorizontalLayout rootLayout = new HorizontalLayout();
        rootLayout.setSizeFull();

        dashboardMenu.init();
        dashboardMenu.setResponsive(Boolean.TRUE);

        final VerticalLayout contentVerticalLayout = new VerticalLayout();
        contentVerticalLayout.addComponent(buildHeader());
        contentVerticalLayout.setSizeFull();

        rootLayout.addComponent(dashboardMenu);
        rootLayout.addComponent(contentVerticalLayout);

        final HorizontalLayout viewHeadercontent = new HorizontalLayout();
        contentVerticalLayout.addComponent(viewHeadercontent);
        viewHeadercontent.setWidth("100%");
        viewHeadercontent.setHeight("43px");
        viewHeadercontent.addStyleName("view-header-layout");

        final Label viewHeader = new Label();
        viewHeader.setWidth("100%");
        viewHeader.setStyleName("header-content");
        viewHeadercontent.addComponent(viewHeader);

        viewHeadercontent.addComponent(notificationUnreadButton);
        viewHeadercontent.setComponentAlignment(notificationUnreadButton, Alignment.MIDDLE_RIGHT);

        final HorizontalLayout content = new HorizontalLayout();
        contentVerticalLayout.addComponent(content);
        content.setStyleName("view-content");
        content.setSizeFull();

        rootLayout.setExpandRatio(contentVerticalLayout, 1.0F);
        contentVerticalLayout.setStyleName("main-content");
        contentVerticalLayout.setExpandRatio(content, 1.0F);
        setContent(rootLayout);
        final Resource resource = context
                .getResource("classpath:/VAADIN/themes/" + UI.getCurrent().getTheme() + "/layouts/footer.html");
        try {
            final CustomLayout customLayout = new CustomLayout(resource.getInputStream());
            customLayout.setSizeUndefined();
            contentVerticalLayout.addComponent(customLayout);
        } catch (final IOException ex) {
            LOG.error("Footer file is missing", ex);
        }
        final Navigator navigator = new Navigator(this, content);
        navigator.addViewChangeListener(new ViewChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean beforeViewChange(final ViewChangeEvent event) {
                return true;
            }

            @Override
            public void afterViewChange(final ViewChangeEvent event) {
                final DashboardMenuItem view = dashboardMenu.getByViewName(event.getViewName());
                dashboardMenu.postViewChange(new PostViewChangeEvent(view));
                if (view == null) {
                    viewHeader.setCaption(null);
                    return;
                }
                viewHeader.setCaption(view.getDashboardCaptionLong());
                notificationUnreadButton.setCurrentView(event.getNewView());
            }
        });

        navigator.setErrorView(errorview);
        navigator.addProvider(new ManagementViewProvider());
        setNavigator(navigator);
        navigator.addView(EMPTY_VIEW, new Navigator.EmptyView());
        // set locale is required for I18N class also, to get the locale from
        // cookie
        final String locale = getLocaleId(SPUIDefinitions.getAvailableLocales());
        setLocale(new Locale(locale));

        if (UI.getCurrent().getErrorHandler() == null) {
            UI.getCurrent().setErrorHandler(new HawkbitUIErrorHandler());
        }

        LOG.info("Current locale of the application is : {}", i18n.getLocale());
    }

    private Component buildHeader() {
        final CssLayout cssLayout = new CssLayout();
        cssLayout.setStyleName("view-header");
        return cssLayout;
    }

    /**
     * Get Specific Locale.
     *
     * @param availableLocalesInApp
     *            as set
     * @return String as preferred locale
     */
    private static String getLocaleId(final Set<String> availableLocalesInApp) {
        final String[] localeChain = getLocaleChain();
        String spLocale = SPUIDefinitions.DEFAULT_LOCALE;
        if (null != localeChain) {
            // Find best matching locale
            for (final String localeId : localeChain) {
                if (availableLocalesInApp.contains(localeId)) {
                    spLocale = localeId;
                    break;
                }
            }
        }
        return spLocale;
    }

    /**
     * Get Locale for i18n.
     *
     * @return String as locales
     */
    private static String[] getLocaleChain() {
        String[] localeChain = null;
        // Fetch all cookies from the request
        final Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
        if (cookies == null) {
            return localeChain;
        }

        for (final Cookie c : cookies) {
            if (c.getName().equals(SPUIDefinitions.COOKIE_NAME) && !c.getValue().isEmpty()) {
                localeChain = c.getValue().split("#");
                break;
            }
        }
        return localeChain;
    }

    private class ManagementViewProvider implements ViewProvider {

        private static final long serialVersionUID = 1L;

        @Override
        public String getViewName(final String viewAndParameters) {
            return viewProvider.getViewName(getStartView(viewAndParameters));
        }

        @Override
        public View getView(final String viewName) {
            return viewProvider.getView(getStartView(viewName));
        }

        private String getStartView(final String viewName) {
            final DashboardMenuItem view = dashboardMenu.getByViewName(viewName);
            if ("".equals(viewName) && !dashboardMenu.isAccessibleViewsEmpty()) {
                return dashboardMenu.getInitialViewName();
            }
            if (view == null || dashboardMenu.isAccessDenied(viewName)) {
                return " ";
            }
            return viewName;
        }

    }

}
