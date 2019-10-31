package org.eclipse.hawkbit.ui.tenantconfiguration.window.layouts;

import java.util.function.Consumer;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxySystemConfigWindow;
import org.eclipse.hawkbit.ui.tenantconfiguration.window.SystemConfigWindowDependencies;
import org.eclipse.hawkbit.ui.tenantconfiguration.window.SystemConfigWindowLayoutComponentBuilder;

import com.vaadin.data.Binder;
import com.vaadin.ui.GridLayout;

public class AbstractSystemConfigWindowLayout extends GridLayout {
    private static final long serialVersionUID = 1L;

    protected final Binder<ProxySystemConfigWindow> proxySystemConfigBinder;

    protected final SystemConfigWindowLayoutComponentBuilder componentBuilder;

    protected final SystemConfigWindowDependencies dependencies;

    protected AbstractSystemConfigWindowLayout(final SystemConfigWindowDependencies dependencies) {
        this.dependencies = dependencies;
        this.proxySystemConfigBinder = new Binder<>();
        this.componentBuilder = new SystemConfigWindowLayoutComponentBuilder(dependencies);

        initLayout();
    }

    private void initLayout() {
        setSpacing(true);
        setSizeUndefined();
        setColumns(4);
        setStyleName("marginTop");
        setColumnExpandRatio(3, 1);
        setWidth(850, Unit.PIXELS);
    }

    public Binder<ProxySystemConfigWindow> getProxySystemConfigBinder() {
        return proxySystemConfigBinder;
    }

    public void addValidationListener(final Consumer<Boolean> validationCallback) {
        proxySystemConfigBinder.addStatusChangeListener(
                event -> validationCallback.accept(event.getBinder().isValid()));
    }
}
