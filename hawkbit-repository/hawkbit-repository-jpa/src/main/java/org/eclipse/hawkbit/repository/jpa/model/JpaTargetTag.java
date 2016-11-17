/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa.model;

import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.model.TargetTag;

/**
 * A {@link TargetTag} is used to describe Target attributes and use them also
 * for filtering the target list.
 *
 */
@Entity
@Table(name = "sp_target_tag", indexes = {
        @Index(name = "sp_idx_target_tag_prim", columnList = "tenant,id") }, uniqueConstraints = @UniqueConstraint(columnNames = {
                "name", "tenant" }, name = "uk_targ_tag"))
public class JpaTargetTag extends JpaTag implements TargetTag {
    private static final long serialVersionUID = 1L;

    @ManyToMany(mappedBy = "tags", targetEntity = JpaTarget.class, fetch = FetchType.LAZY)
    private List<Target> assignedToTargets;

    /**
     * Constructor.
     *
     * @param name
     *            of {@link TargetTag}
     * @param description
     *            of {@link TargetTag}
     * @param colour
     *            of {@link TargetTag}
     */
    public JpaTargetTag(final String name, final String description, final String colour) {
        super(name, description, colour);
    }

    /**
     * Public constructor.
     *
     * @param name
     *            of the {@link TargetTag}
     **/
    public JpaTargetTag(final String name) {
        super(name, null, null);
    }

    public JpaTargetTag() {
        // Default constructor for JPA.
    }

    @Override
    public List<Target> getAssignedToTargets() {
        if (assignedToTargets == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(assignedToTargets);
    }

}
