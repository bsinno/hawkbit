package org.eclipse.hawkbit.ui.tenantconfiguration.window;

import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySystemConfigWindow;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;

import com.vaadin.data.Binder;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

public class SystemConfigWindowLayoutComponentBuilder {
    private final SystemConfigWindowDependencies dependencies;

    public SystemConfigWindowLayoutComponentBuilder(final SystemConfigWindowDependencies dependencies) {
        this.dependencies = dependencies;
    }

    public Label getLabel(final String key) {
        return new LabelBuilder().name(dependencies.getI18n().getMessage(key)).buildLabel();
    }

    public ComboBox<ProxyType> createDistributionSetCombo(final Binder<ProxySystemConfigWindow> binder) {
        final ComboBox<ProxyType> distributionSetType = new ComboBox<>();
        distributionSetType.setDescription(
                dependencies.getI18n().getMessage(UIMessageIdProvider.CAPTION_DISTRIBUTION_TAG));
        distributionSetType.setId(UIComponentIdProvider.SYSTEM_CONFIGURATION_DEFAULTDIS_COMBOBOX);
        distributionSetType.addStyleName(ValoTheme.COMBOBOX_TINY);
        distributionSetType.setWidth(330f, Sizeable.Unit.PIXELS);
        distributionSetType.setEmptySelectionAllowed(false);
        distributionSetType.setItemCaptionGenerator(ProxyType::getKeyAndName);
        distributionSetType.setDataProvider(dependencies.getDistributionSetTypeDataProvider());
        binder.forField(distributionSetType).withConverter(dst -> {
            if (dst == null) {
                return null;
            }

            return dst.getId();
        }, dstId -> {
            if (dstId == null) {
                return null;
            }

            final ProxyType dst = new ProxyType();
            dst.setId(dstId);

            return dst;
        }).bind(ProxySystemConfigWindow::getDistributionSetTypeId, ProxySystemConfigWindow::setDistributionSetTypeId);

        return distributionSetType;
    }

    public SystemConfigWindowDependencies getDependencies() {
        return dependencies;
    }
}
