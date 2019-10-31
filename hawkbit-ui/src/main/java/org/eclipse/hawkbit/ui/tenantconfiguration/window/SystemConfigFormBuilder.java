package org.eclipse.hawkbit.ui.tenantconfiguration.window;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxySystemConfig;
import org.eclipse.hawkbit.ui.tenantconfiguration.window.layouts.SystemConfigFormLayout;

import com.vaadin.data.Binder;
import com.vaadin.navigator.View;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

public class SystemConfigFormBuilder extends CustomComponent implements View {

    private final Label distributionSetLabel = new Label();

    public SystemConfigFormBuilder() {

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        setCompositionRoot(horizontalLayout);

        ProxySystemConfig config = createSystemConfig();
        horizontalLayout.addComponent(createSystemConfigLayout(config));
    }

    private static ProxySystemConfig createSystemConfig() {
        ProxySystemConfig config = new ProxySystemConfig();
        config.setDistributionSetId(new HashSet<String>(Arrays.asList("first")));
        return config;
    }

    private Layout createSystemConfigLayout(ProxySystemConfig systemConfig){

        SystemConfigFormLayout layout = new SystemConfigFormLayout();
        final Binder<ProxySystemConfig> binder = new Binder<>(ProxySystemConfig.class);

        binder.readBean(systemConfig);
        VerticalLayout editorContainer = new VerticalLayout();
        editorContainer.addComponents(layout);
        return editorContainer;
    }
}
