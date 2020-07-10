/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Describing the fields of the Group model which can be used in the REST API e.g.
 * for sorting etc.
 */
public enum DirectoryGroupFields implements FieldNameProvider {
    /**
     * The id field.
     */
    ID("id"),

    /**
     * The name field.
     */
    NAME("name"),

    /**
     * The directoryParent field.
     */
    DIRECTORYPARENT("directoryParent", "name", "id"),

    /**
     * The groups ancestors
     */
    ANCESTORTREE("ancestorTree.descendant");

    private final String fieldName;

    private final List<String> subEntityAttributes;

    DirectoryGroupFields(final String fieldName) {
        this(fieldName, Collections.emptyList());
    }

    DirectoryGroupFields(final String fieldName, final String... subEntityAttributes) {
        this(fieldName, Arrays.asList(subEntityAttributes));
    }

    DirectoryGroupFields(final String fieldName, final List<String> subEntityAttributes) {
        this.fieldName = fieldName;
        this.subEntityAttributes = subEntityAttributes;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public List<String> getSubEntityAttributes() {
        return subEntityAttributes;
    }
}
