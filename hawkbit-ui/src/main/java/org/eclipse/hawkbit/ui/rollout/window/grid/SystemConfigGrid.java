package org.eclipse.hawkbit.ui.rollout.window.grid;

import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus;

public class SystemConfigGrid extends AbstractGridComponentLayout {
    /**
     * Constructor.
     *
     * @param i18n
     * @param eventBus
     */
    public SystemConfigGrid(VaadinMessageSource i18n, EventBus.UIEventBus eventBus) {
        super(i18n, eventBus);
    }
}
