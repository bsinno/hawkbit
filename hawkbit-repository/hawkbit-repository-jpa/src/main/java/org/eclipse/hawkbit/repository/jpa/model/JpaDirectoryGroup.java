/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa.model;

import java.util.Collection;
import java.util.List;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.eclipse.hawkbit.repository.model.DirectoryGroup;
import org.eclipse.hawkbit.repository.model.DirectoryTree;
import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.platform.database.H2Platform;
import org.eclipse.persistence.platform.database.PostgreSQLPlatform;
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
public class JpaDirectoryGroup extends AbstractJpaNamedEntity implements DirectoryGroup, EventAwareEntity {
    private static final long serialVersionUID = 1L;

    @JoinColumn(name = "directory_parent", nullable = true, updatable = true, foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_directory_parent"))
    @ManyToOne(targetEntity = JpaDirectoryGroup.class)
    private DirectoryGroup directoryParent;

    @OneToMany(targetEntity = JpaDirectoryGroup.class, mappedBy = "directoryParent")
    private Collection<DirectoryGroup> directoryChildren;

    @OneToMany(targetEntity = JpaDirectoryTree.class, mappedBy = "ancestor")
    private Collection<DirectoryTree> ancestorTree;

    @OneToMany(targetEntity = JpaDirectoryTree.class, mappedBy = "descendant")
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

    /**
     * Helper to pick the correct call for the platform
     * <p>
     * It is necessary as the current EclipseLink implementation (2.7.x) does not correctly handle Postgres and H2 calls
     * without a return value
     *
     * @param descriptorEvent     of the event
     * @param storedProcedureCall that shall be executed
     */
    public static void executeCall(final DescriptorEvent descriptorEvent, final StoredProcedureCall storedProcedureCall) {
        if (descriptorEvent.getSession().getPlatform() instanceof PostgreSQLPlatform) {
            descriptorEvent.getSession().executeSelectingCall(storedProcedureCall);
        } else if (descriptorEvent.getSession().getPlatform() instanceof H2Platform) {
            final List<Object> parameters = storedProcedureCall.getParameters();
            descriptorEvent.getSession().executeNonSelectingSQL("CALL " + storedProcedureCall.getProcedureName() + "(" + parameters.get(0) + ", " + parameters.get(1) + ")");
        } else {
            descriptorEvent.getSession().executeNonSelectingCall(storedProcedureCall);
        }
    }

    @Override
    public void fireCreateEvent(final DescriptorEvent descriptorEvent) {
        // Create entries in closure table by calling procedure
        Object object = descriptorEvent.getObject();
        if (object instanceof DirectoryGroup) {
            DirectoryGroup group = (DirectoryGroup) object;
            long groupId = group.getId();
            // if no parent was specified during create only self assign (depth 0)
            long parentId = group.getDirectoryParent() != null ? group.getDirectoryParent().getId() : groupId;
            StoredProcedureCall storedProcedureCall = new StoredProcedureCall();
            storedProcedureCall.setProcedureName("p_group_node_add");
            storedProcedureCall.addNamedArgumentValue("param_group", groupId);
            storedProcedureCall.addNamedArgumentValue("param_parent", parentId);

            descriptorEvent.getSession().beginTransaction();
            executeCall(descriptorEvent, storedProcedureCall);
            descriptorEvent.getSession().commitTransaction();
        }
    }

    @Override
    public void fireUpdateEvent(final DescriptorEvent descriptorEvent) {
        // Update entries in closure table by calling procedure
        Object newObject = descriptorEvent.getObject();
        if (newObject instanceof DirectoryGroup) {
            DirectoryGroup newGroup = (DirectoryGroup) newObject;
            DirectoryGroup newParent = newGroup.getDirectoryParent();
            if (newParent != null) {
                StoredProcedureCall storedProcedureCall = new StoredProcedureCall();
                storedProcedureCall.setProcedureName("p_group_node_move");
                storedProcedureCall.addNamedArgumentValue("param_group", newGroup.getId());
                // an empty parent is means remove group assignment, set it to zero will clean up its closure tree
                // self assignment should not be possible, but still try to handle it the same
                if (newGroup.getDirectoryParent() == null || newGroup.getId().equals(newGroup.getDirectoryParent().getId())) {
                    storedProcedureCall.addNamedArgumentValue("param_parent", 0);
                } else {
                    storedProcedureCall.addNamedArgumentValue("param_parent", newGroup.getDirectoryParent().getId());
                }

                descriptorEvent.getSession().beginTransaction();
                executeCall(descriptorEvent, storedProcedureCall);
                descriptorEvent.getSession().commitTransaction();
            }
        }
    }

    @Override
    public void fireDeleteEvent(final DescriptorEvent descriptorEvent) {
        // delete event is not necessary as delete is handled by database constraints
    }

}