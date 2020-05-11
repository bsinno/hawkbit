package org.eclipse.hawkbit.ui.common.grid;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.GridComponentBuilder;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.common.state.GridLayoutUiState;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;

public abstract class AbstractDsGrid<F> extends AbstractGrid<ProxyDistributionSet, F> {
    private static final long serialVersionUID = 1L;

    protected static final String DS_NAME_ID = "dsName";
    protected static final String DS_VERSION_ID = "dsVersion";
    protected static final String DS_CREATED_BY_ID = "dsCreatedBy";
    protected static final String DS_CREATED_DATE_ID = "dsCreatedDate";
    protected static final String DS_MODIFIED_BY_ID = "dsModifiedBy";
    protected static final String DS_MODIFIED_DATE_ID = "dsModifiedDate";
    protected static final String DS_DESC_ID = "dsDescription";
    protected static final String DS_DELETE_BUTTON_ID = "dsDeleteButton";

    protected final GridLayoutUiState distributionSetGridLayoutUiState;
    protected final transient DistributionSetManagement dsManagement;
    protected final transient DistributionSetToProxyDistributionMapper dsToProxyDistributionMapper;

    protected final transient DeleteSupport<ProxyDistributionSet> distributionDeleteSupport;

    protected AbstractDsGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final DistributionSetManagement dsManagement, final GridLayoutUiState distributionSetGridLayoutUiState,
            final EventView view) {
        super(i18n, eventBus, permissionChecker);

        this.distributionSetGridLayoutUiState = distributionSetGridLayoutUiState;
        this.dsManagement = dsManagement;
        this.dsToProxyDistributionMapper = new DistributionSetToProxyDistributionMapper();

        setSelectionSupport(new SelectionSupport<ProxyDistributionSet>(this, eventBus, EventLayout.DS_LIST, view,
                this::mapIdToProxyEntity, this::getSelectedEntityIdFromUiState, this::setSelectedEntityIdToUiState));
        if (distributionSetGridLayoutUiState.isMaximized()) {
            getSelectionSupport().disableSelection();
        } else {
            getSelectionSupport().enableMultiSelection();
        }

        this.distributionDeleteSupport = new DeleteSupport<>(this, i18n, notification,
                i18n.getMessage("distribution.details.header"), ProxyDistributionSet::getNameVersion,
                this::deleteDistributionSets, UIComponentIdProvider.DS_DELETE_CONFIRMATION_DIALOG);
    }

    @Override
    public void init() {
        super.init();

        addStyleName("grid-row-border");
    }

    public Optional<ProxyDistributionSet> mapIdToProxyEntity(final long entityId) {
        return dsManagement.get(entityId).map(dsToProxyDistributionMapper::map);
    }

    private Optional<Long> getSelectedEntityIdFromUiState() {
        return Optional.ofNullable(distributionSetGridLayoutUiState.getSelectedEntityId());
    }

    private void setSelectedEntityIdToUiState(final Optional<Long> entityId) {
        distributionSetGridLayoutUiState.setSelectedEntityId(entityId.orElse(null));
    }

    private boolean deleteDistributionSets(final Collection<ProxyDistributionSet> setsToBeDeleted) {
        final Collection<Long> dsToBeDeletedIds = setsToBeDeleted.stream().map(ProxyIdentifiableEntity::getId)
                .collect(Collectors.toList());
        dsManagement.delete(dsToBeDeletedIds);

        eventBus.publish(EventTopics.ENTITY_MODIFIED, this, new EntityModifiedEventPayload(
                EntityModifiedEventType.ENTITY_REMOVED, ProxyDistributionSet.class, dsToBeDeletedIds));

        return true;
    }

    /**
     * Creates the grid content for maximized-state.
     */
    public void createMaximizedContent() {
        removeAllColumns();
        addMaxColumns();
        getColumns().forEach(column -> column.setHidable(true));

        getSelectionSupport().disableSelection();
        getDragAndDropSupportSupport().removeDropTarget();
    }

    /**
     * Creates the grid content for normal (minimized) state.
     */
    public void createMinimizedContent() {
        removeAllColumns();
        addColumns();

        getSelectionSupport().enableMultiSelection();
        getDragAndDropSupportSupport().addDropTarget();
    }

    protected Column<ProxyDistributionSet, String> addNameColumn() {
        return addColumn(ProxyDistributionSet::getName).setId(DS_NAME_ID).setCaption(i18n.getMessage("header.name"));
    }

    protected Column<ProxyDistributionSet, String> addVersionColumn() {
        return addColumn(ProxyDistributionSet::getVersion).setId(DS_VERSION_ID)
                .setCaption(i18n.getMessage("header.version"));
    }

    protected Column<ProxyDistributionSet, Button> addDeleteColumn() {
        return addComponentColumn(ds -> GridComponentBuilder.buildActionButton(i18n,
                clickEvent -> distributionDeleteSupport.openConfirmationWindowDeleteAction(ds), VaadinIcons.TRASH,
                UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.DIST_DELET_ICON + "." + ds.getId(),
                permissionChecker.hasDeleteRepositoryPermission())).setId(DS_DELETE_BUTTON_ID)
                        .setCaption(i18n.getMessage("header.action.delete"));
    }

    protected void addMaxColumns() {
        addNameColumn().setMinimumWidth(100d).setExpandRatio(1);

        addCreatedByColumn();
        addCreatedDateColumn();
        addModifiedByColumn();
        addModifiedDateColumn();

        addDescriptionColumn().setMinimumWidth(100d).setExpandRatio(1);

        addVersionColumn().setMinimumWidth(100d);

        addDeleteColumn().setMinimumWidth(80d);
    }

    protected Column<ProxyDistributionSet, String> addCreatedByColumn() {
        return addColumn(ProxyDistributionSet::getCreatedBy).setId(DS_CREATED_BY_ID)
                .setCaption(i18n.getMessage("header.createdBy"));
    }

    protected Column<ProxyDistributionSet, String> addCreatedDateColumn() {
        return addColumn(ProxyDistributionSet::getCreatedDate).setId(DS_CREATED_DATE_ID)
                .setCaption(i18n.getMessage("header.createdDate"));
    }

    protected Column<ProxyDistributionSet, String> addModifiedByColumn() {
        return addColumn(ProxyDistributionSet::getLastModifiedBy).setId(DS_MODIFIED_BY_ID)
                .setCaption(i18n.getMessage("header.modifiedBy"));
    }

    protected Column<ProxyDistributionSet, String> addModifiedDateColumn() {
        return addColumn(ProxyDistributionSet::getModifiedDate).setId(DS_MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("header.modifiedDate"));
    }

    protected Column<ProxyDistributionSet, String> addDescriptionColumn() {
        return addColumn(ProxyDistributionSet::getDescription).setId(DS_DESC_ID)
                .setCaption(i18n.getMessage("header.description"));
    }
}
