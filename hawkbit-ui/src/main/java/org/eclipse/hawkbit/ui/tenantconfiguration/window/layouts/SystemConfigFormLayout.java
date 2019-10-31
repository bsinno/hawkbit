package org.eclipse.hawkbit.ui.tenantconfiguration.window.layouts;

import com.vaadin.ui.CheckBoxGroup;
import com.vaadin.ui.FormLayout;

public class SystemConfigFormLayout extends FormLayout {

    final private CheckBoxGroup<String> distributionSetGroup = new CheckBoxGroup<>("distributionSetId");

    public SystemConfigFormLayout() {
        distributionSetGroup.setItems("item1", "item2");
        addComponents(distributionSetGroup);
    }

    public CheckBoxGroup<String> getDistributionSetGroup() {
        return distributionSetGroup;
    }
}
