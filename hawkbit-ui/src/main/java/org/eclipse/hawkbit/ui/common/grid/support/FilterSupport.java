package org.eclipse.hawkbit.ui.common.grid.support;

import java.util.EnumMap;
import java.util.function.BiConsumer;

import org.eclipse.hawkbit.ui.common.event.FilterType;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.DataProvider;

public class FilterSupport<T, F> {
    private final ConfigurableFilterDataProvider<T, Void, F> filterDataProvider;
    private final EnumMap<FilterType, BiConsumer<F, ?>> filterTypeToSetterMapping;
    private final Runnable afterRefreshFilterCallback;

    private F entityFilter;

    public FilterSupport(final DataProvider<T, F> dataProvider) {
        this(dataProvider, null);
    }

    public FilterSupport(final DataProvider<T, F> dataProvider, final Runnable afterRefreshFilterCallback) {
        this.filterDataProvider = dataProvider.withConfigurableFilter();
        this.filterTypeToSetterMapping = new EnumMap<>(FilterType.class);
        this.afterRefreshFilterCallback = afterRefreshFilterCallback;
    }

    public <R> void addMapping(final FilterType filterType, final BiConsumer<F, R> setter) {
        filterTypeToSetterMapping.put(filterType, setter);
    }

    public <R> void updateFilter(final FilterType filterType, final R filterValue) {
        updateFilter((BiConsumer<F, R>) filterTypeToSetterMapping.get(filterType), filterValue);
    }

    public <R> void updateFilter(final BiConsumer<F, R> setter, final R filterValue) {
        if (setter != null) {
            setter.accept(entityFilter, filterValue);
            refreshFilter();
        }
    }

    public void refreshFilter() {
        filterDataProvider.setFilter(entityFilter);

        if (afterRefreshFilterCallback != null) {
            afterRefreshFilterCallback.run();
        }
    }

    public ConfigurableFilterDataProvider<T, Void, F> getFilterDataProvider() {
        return filterDataProvider;
    }

    public F getFilter() {
        return entityFilter;
    }

    public void setFilter(final F entityFilter) {
        this.entityFilter = entityFilter;
    }

    public boolean isFilterTypeSupported(final FilterType filterType) {
        return filterTypeToSetterMapping.keySet().contains(filterType);
    }
}
