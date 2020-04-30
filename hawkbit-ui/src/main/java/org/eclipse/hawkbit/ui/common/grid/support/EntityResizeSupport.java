package org.eclipse.hawkbit.ui.common.grid.support;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import com.vaadin.ui.Grid;

public class EntityResizeSupport implements ResizeSupport {
    private final Grid<?> grid;

    private final String[] maxColumnOrder;
    private final String[] minColumnOrder;

    private final Collection<String> maxHiddenColumns;
    private final Collection<String> maxShownColumns;

    private final Map<String, Integer> maxColumnExpandRatio;
    private final Map<String, Integer> minColumnExpandRatio;

    public EntityResizeSupport(final Grid<?> grid, final String[] maxColumnOrder, final String[] minColumnOrder,
            final Collection<String> maxHiddenColumns, final Collection<String> maxShownColumns,
            final Map<String, Integer> maxColumnExpandRatio, final Map<String, Integer> minColumnExpandRatio) {
        this.grid = grid;

        this.maxColumnOrder = maxColumnOrder;
        this.minColumnOrder = minColumnOrder;

        this.maxHiddenColumns = maxHiddenColumns;
        this.maxShownColumns = maxShownColumns;

        this.maxColumnExpandRatio = maxColumnExpandRatio;
        this.minColumnExpandRatio = minColumnExpandRatio;
    }

    @Override
    public void setMaximizedColumnOrder() {
        changeColumnsOrder(maxColumnOrder);
    }

    private void changeColumnsOrder(final String[] columnIds) {
        grid.clearSortOrder();
        grid.setColumnOrder(columnIds);
    }

    @Override
    public void setMaximizedHiddenColumns() {
        maxHiddenColumns.forEach(columnId -> changeColumnHiddenState(columnId, true));
        maxShownColumns.forEach(columnId -> changeColumnHiddenState(columnId, false));

        // TODO: adapt
        // Arrays.asList(maxColumnOrder).forEach(columnId ->
        // changeColumnHidableState(columnId, true));
    }

    private void changeColumnHiddenState(final String columnId, final boolean hidden) {
        grid.getColumn(columnId).setHidden(hidden);
    }

    private void changeColumnHidableState(final String columnId, final boolean hidable) {
        grid.getColumn(columnId).setHidable(hidable);
    }

    @Override
    public void setMaximizedColumnExpandRatio() {
        changeColumnsExpandRatio(maxColumnExpandRatio);
    }

    private void changeColumnsExpandRatio(final Map<String, Integer> columnIdExpandRatios) {
        if (CollectionUtils.isEmpty(columnIdExpandRatios)) {
            return;
        }

        clearColumnsExpandRatio();

        columnIdExpandRatios.forEach(this::setColumnExpandRatio);
    }

    private void clearColumnsExpandRatio() {
        grid.getColumns().forEach(column -> column.setExpandRatio(1));
    }

    private void setColumnExpandRatio(final String columnId, final int expandRatio) {
        grid.getColumn(columnId).setExpandRatio(expandRatio);
    }

    @Override
    public void setMinimizedColumnOrder() {
        changeColumnsOrder(minColumnOrder);
    }

    @Override
    public void setMinimizedHiddenColumns() {
        maxHiddenColumns.forEach(columnId -> changeColumnHiddenState(columnId, false));
        maxShownColumns.forEach(columnId -> changeColumnHiddenState(columnId, true));

        // TODO: adapt
        // Arrays.asList(maxColumnOrder).forEach(columnId ->
        // changeColumnHidableState(columnId, false));
    }

    @Override
    public void setMinimizedColumnExpandRatio() {
        changeColumnsExpandRatio(minColumnExpandRatio);
    }

    public static class Builder {
        private final Grid<?> grid;

        private String[] maxColumnOrder;
        private String[] minColumnOrder;

        private Collection<String> maxHiddenColumns;
        private Collection<String> maxShownColumns;

        private Map<String, Integer> maxColumnExpandRatio;
        private Map<String, Integer> minColumnExpandRatio;

        public Builder(final Grid<?> grid) {
            this.grid = grid;
        }

        public Builder maxColumnOrder(final String... maxColumnOrder) {
            this.maxColumnOrder = maxColumnOrder;
            return this;
        }

        public Builder minColumnOrder(final String... minColumnOrder) {
            this.minColumnOrder = minColumnOrder;
            return this;
        }

        public Builder maxHiddenColumns(final String... maxHiddenColumns) {
            this.maxHiddenColumns = Arrays.asList(maxHiddenColumns);
            return this;
        }

        public Builder maxShownColumns(final String... maxShownColumns) {
            this.maxShownColumns = Arrays.asList(maxShownColumns);
            return this;
        }

        public Builder maxColumnExpandRatio(final Map<String, Integer> maxColumnExpandRatio) {
            this.maxColumnExpandRatio = maxColumnExpandRatio;
            return this;
        }

        public Builder minColumnExpandRatio(final Map<String, Integer> minColumnExpandRatio) {
            this.minColumnExpandRatio = minColumnExpandRatio;
            return this;
        }

        public EntityResizeSupport build() {
            if (maxColumnOrder == null) {
                maxColumnOrder = new String[0];
            }

            if (minColumnOrder == null) {
                minColumnOrder = new String[0];
            }

            if (CollectionUtils.isEmpty(maxHiddenColumns)) {
                maxHiddenColumns = Collections.emptyList();
            }

            if (CollectionUtils.isEmpty(maxShownColumns)) {
                maxShownColumns = Collections.emptyList();
            }

            if (CollectionUtils.isEmpty(maxColumnExpandRatio)) {
                maxColumnExpandRatio = Collections.emptyMap();
            }

            if (CollectionUtils.isEmpty(minColumnExpandRatio)) {
                minColumnExpandRatio = Collections.emptyMap();
            }

            return new EntityResizeSupport(grid, maxColumnOrder, minColumnOrder, maxHiddenColumns, maxShownColumns,
                    maxColumnExpandRatio, minColumnExpandRatio);
        }
    }
}
