/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa.model;

import java.util.Collection;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.eclipse.hawkbit.repository.model.DirectoryGroup;
import org.eclipse.hawkbit.repository.model.DirectoryTree;
import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.queries.StoredProcedureCall;

/**
 * A JpaGroup provides a grouping mechanism for targets as well as other groups
 */
@Entity
@Table(name = "sp_directory_group", uniqueConstraints = @UniqueConstraint(columnNames = {"name",
        "tenant"}, name = "uk_directory_group"))
// exception squid:S2160 - BaseEntity equals/hashcode is handling correctly for
// sub entities
@SuppressWarnings("squid:S2160")
public class JpaDirectoryGroup extends AbstractJpaNamedEntity implements DirectoryGroup, ProcedureAwareEntity {
    private static final long serialVersionUID = 1L;

    public static final String PROCEDURE_DIRECTORY_TREE_ADD = "p_directory_tree_add";

    public static final String PROCEDURE_DIRECTORY_TREE_MOVE = "p_directory_tree_move";

    public static final String PROCEDURE_PARAM_PARENT = "param_parent";

    public static final String PROCEDURE_PARAM_GROUP = "param_group";

    @JoinColumn(name = "directory_parent", nullable = true, updatable = true, foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_directory_parent"))
    @ManyToOne(fetch=FetchType.LAZY, targetEntity = JpaDirectoryGroup.class)
    private DirectoryGroup directoryParent;

    @OneToMany(fetch=FetchType.LAZY, targetEntity = JpaDirectoryGroup.class, mappedBy = "directoryParent")
    private Collection<DirectoryGroup> directoryChildren;

    // Seems like a sonar bug as DirectoryTree implements Serializable also occurred in AbstractTagToken
    @SuppressWarnings("squid:S1948")
    @OneToMany(fetch=FetchType.LAZY, targetEntity = JpaDirectoryTree.class, mappedBy = "ancestor")
    private Collection<DirectoryTree> ancestorTree;

    // Seems like a sonar bug as DirectoryTree implements Serializable also occurred in AbstractTagToken
    @SuppressWarnings("squid:S1948")
    @OneToMany(fetch=FetchType.LAZY, targetEntity = JpaDirectoryTree.class, mappedBy = "descendant")
    private Collection<DirectoryTree> descendantTree;

    protected JpaDirectoryGroup() {
        // Default constructor needed for JPA entities
    }

    /**
     * Public constructor.
     *
     * @param name        of the {@link DirectoryGroup}
     * @param description of the {@link DirectoryGroup}
     */
    public JpaDirectoryGroup(final String name, final String description, final DirectoryGroup directoryParent) {
        super(name, description);
        this.directoryParent = directoryParent;
    }

    public DirectoryGroup getDirectoryParent() {
        return directoryParent;
    }

    public Collection<DirectoryGroup> getDirectoryChildren() {
        return directoryChildren;
    }

    @Override
    public Collection<DirectoryTree> getAncestorTree() {
        return ancestorTree;
    }

    @Override
    public Collection<DirectoryTree> getDescendantTree() {
        return descendantTree;
    }

    public void setDirectoryParent(final DirectoryGroup directoryParent) {
        this.directoryParent = directoryParent;
    }

    @Override
    public String toString() {
        return "DirectoryGroup [getOptLockRevision()=" + getOptLockRevision() + ", getId()=" + getId() + "]";
    }

    @Override
    public void fireCreateProcedure(final DescriptorEvent descriptorEvent) {
        // Create entries in closure table by calling procedure
        Object object = descriptorEvent.getObject();
        if (object instanceof DirectoryGroup) {
            DirectoryGroup group = (DirectoryGroup) object;
            long groupId = group.getId();
            // if no parent was specified during create only self assign (depth 0)
            long parentId = group.getDirectoryParent() != null ? group.getDirectoryParent().getId() : groupId;
            StoredProcedureCall storedProcedureCall = new StoredProcedureCall();
            storedProcedureCall.setProcedureName(PROCEDURE_DIRECTORY_TREE_ADD);
            storedProcedureCall.addNamedArgumentValue(PROCEDURE_PARAM_GROUP, groupId);
            storedProcedureCall.addNamedArgumentValue(PROCEDURE_PARAM_PARENT, parentId);

            descriptorEvent.getSession().beginTransaction();
            executeCall(descriptorEvent, storedProcedureCall);
            descriptorEvent.getSession().commitTransaction();
        }
    }

    @Override
    public void fireUpdateProcedure(final DescriptorEvent descriptorEvent) {
        // Update entries in closure table by calling procedure
        Object newObject = descriptorEvent.getObject();
        if (newObject instanceof DirectoryGroup) {
            DirectoryGroup newGroup = (DirectoryGroup) newObject;
            DirectoryGroup newParent = newGroup.getDirectoryParent();
            if (newParent != null) {
                StoredProcedureCall storedProcedureCall = new StoredProcedureCall();
                storedProcedureCall.setProcedureName(PROCEDURE_DIRECTORY_TREE_MOVE);
                storedProcedureCall.addNamedArgumentValue(PROCEDURE_PARAM_GROUP, newGroup.getId());
                // an empty parent is means remove group assignment, set it to zero will clean up its closure tree
                // self assignment should not be possible, but still try to handle it the same
                if (newGroup.getDirectoryParent() == null || newGroup.getId().equals(newGroup.getDirectoryParent().getId())) {
                    storedProcedureCall.addNamedArgumentValue(PROCEDURE_PARAM_PARENT, 0);
                } else {
                    storedProcedureCall.addNamedArgumentValue(PROCEDURE_PARAM_PARENT, newGroup.getDirectoryParent().getId());
                }

                descriptorEvent.getSession().beginTransaction();
                executeCall(descriptorEvent, storedProcedureCall);
                descriptorEvent.getSession().commitTransaction();
            }
        }
    }
}