/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstag;

import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

public class UpdateDsTagWindowLayout extends AddDsTagWindowLayout {
    private static final long serialVersionUID = 1L;

    public UpdateDsTagWindowLayout(final VaadinMessageSource i18n) {
        super(i18n);

        tagName.setEnabled(false);
    }
}
