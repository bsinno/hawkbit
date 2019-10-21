/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.disttype;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.ui.artifacts.smtype.TypeWindowController;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.builder.WindowBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

//TODO: remove duplication with other window builders
public class DsTypeWindowBuilder {
    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final DistributionSetTypeManagement dsTypeManagement;
    private final DistributionSetManagement dsManagement;
    private final SoftwareModuleTypeManagement smTypeManagement;

    public DsTypeWindowBuilder(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final UIEventBus eventBus, final UINotification uiNotification,
            final DistributionSetTypeManagement dsTypeManagement, final DistributionSetManagement dsManagement,
            final SoftwareModuleTypeManagement smTypeManagement) {
        this.i18n = i18n;
        this.entityFactory = entityFactory;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;

        this.dsTypeManagement = dsTypeManagement;
        this.dsManagement = dsManagement;
        this.smTypeManagement = smTypeManagement;
    }

    public Window getWindowForAddDsType() {
        return getWindowForTag(null, new AddDsTypeWindowController(i18n, entityFactory, eventBus, uiNotification,
                dsTypeManagement, new DsTypeWindowLayout(i18n, smTypeManagement)));

    }

    public Window getWindowForUpdateDsType(final ProxyType proxyType) {
        return getWindowForTag(proxyType, new UpdateDsTypeWindowController(i18n, entityFactory, eventBus,
                uiNotification, dsTypeManagement, dsManagement, new DsTypeWindowLayout(i18n, smTypeManagement)));
    }

    private Window getWindowForTag(final ProxyType proxyType, final TypeWindowController controller) {
        controller.populateWithData(proxyType);

        final CommonDialogWindow window = createWindow(controller.getLayout(), controller.getSaveDialogCloseListener());

        controller.getLayout().addValidationListener(window::setSaveButtonEnabled);

        return window;

    }

    private CommonDialogWindow createWindow(final Component content,
            final SaveDialogCloseListener saveDialogCloseListener) {
        return new WindowBuilder(SPUIDefinitions.CREATE_UPDATE_WINDOW).id(UIComponentIdProvider.TAG_POPUP_ID)
                .content(content).i18n(i18n).saveDialogCloseListener(saveDialogCloseListener).buildCommonDialogWindow();
    }
}
