package org.eclipse.hawkbit.ui.tenantconfiguration.window;

import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSetType;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySystemConfigWindow;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;

import com.vaadin.data.Binder;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

public class SystemConfigWindowLayoutComponentBuilder {
    private final SystemConfigWindowDependencies dependencies;

    public SystemConfigWindowLayoutComponentBuilder(SystemConfigWindowDependencies dependencies) {
        this.dependencies = dependencies;
    }
    public Label getLabel(final String key) {
        return new LabelBuilder().name(dependencies.getI18n().getMessage(key)).buildLabel();
    }

    public ComboBox<ProxyDistributionSetType> createDistributionSetCombo(Binder<ProxySystemConfigWindow> binder ) {
        final ComboBox<ProxyDistributionSetType> distributionSetType = new ComboBox<>();
        distributionSetType.setDescription(dependencies.getI18n().getMessage(UIMessageIdProvider.CAPTION_DISTRIBUTION_TAG));
        distributionSetType.setId(UIComponentIdProvider.SYSTEM_CONFIGURATION_DEFAULTDIS_COMBOBOX);
        distributionSetType.addStyleName(ValoTheme.COMBOBOX_SMALL);
        distributionSetType.addStyleName(SPUIDefinitions.COMBO_BOX_SPECIFIC_STYLE);
        distributionSetType.addStyleName(ValoTheme.COMBOBOX_TINY);
        distributionSetType.setWidth(300f, Sizeable.Unit.PIXELS);
        distributionSetType.setEmptySelectionAllowed(false);
        distributionSetType.setItemCaptionGenerator(ProxyDistributionSetType::getName);
        distributionSetType.setDataProvider(dependencies.getDistributionSetTypeDataProvider());
        binder = new Binder<>(ProxySystemConfigWindow.class);

        binder.forField(distributionSetType).asRequired("select").withConverter(dst -> {
            if (dst == null) {
                return null;
            }

            return dst.getId();
        }, dstId -> {
            if (dstId == null) {
                return null;
            }

            final ProxyDistributionSetType dst = new ProxyDistributionSetType();
            dst.setId(dstId);

            return dst;
        }).bind(ProxySystemConfigWindow::getDistributionSetTypeId, ProxySystemConfigWindow::setDistributionSetTypeId);
        return distributionSetType;
    }
}
