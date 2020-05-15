package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.Collections;
import java.util.List;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyKeyValueDetails;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class KeyValueDetailsComponent extends CustomField<List<ProxyKeyValueDetails>> {
    private static final long serialVersionUID = 1L;

    final VerticalLayout keyValueDetailsLayout;

    public KeyValueDetailsComponent() {
        keyValueDetailsLayout = new VerticalLayout();
        keyValueDetailsLayout.setSpacing(true);
        keyValueDetailsLayout.setMargin(false);
        keyValueDetailsLayout.setWidthFull();
        keyValueDetailsLayout.addStyleName("enable-horizontal-scroll");

        setReadOnly(true);
    }

    @Override
    public List<ProxyKeyValueDetails> getValue() {
        // not needed to return meaningful key-value pairs, because it is
        // intended to be read-only
        return Collections.emptyList();
    }

    @Override
    protected Component initContent() {
        return keyValueDetailsLayout;
    }

    @Override
    protected void doSetValue(final List<ProxyKeyValueDetails> keyValueDetails) {
        keyValueDetailsLayout.removeAllComponents();

        if (keyValueDetails != null) {
            keyValueDetails.forEach(this::addKeyValueDetail);
        }
    }

    private void addKeyValueDetail(final ProxyKeyValueDetails keyValueDetail) {
        final HorizontalLayout keyValueDetailLayout = new HorizontalLayout();
        keyValueDetailLayout.setSpacing(true);
        keyValueDetailLayout.setMargin(false);
        keyValueDetailLayout.setWidthUndefined();

        final Label keyDetail = buildKeyDetail(keyValueDetail.getKey());
        final Label valueDetail = buildValueDetail(keyValueDetail.getId(), keyValueDetail.getValue());

        keyValueDetailLayout.addComponent(keyDetail);
        keyValueDetailLayout.setExpandRatio(keyDetail, 0.0F);

        keyValueDetailLayout.addComponent(valueDetail);
        keyValueDetailLayout.setExpandRatio(valueDetail, 1.0F);

        keyValueDetailsLayout.addComponent(keyValueDetailLayout);
    }

    private Label buildKeyDetail(final String key) {
        final Label keyLabel = new Label(key + ":");

        keyLabel.addStyleName(SPUIDefinitions.TEXT_STYLE);
        keyLabel.addStyleName("text-bold");

        return keyLabel;
    }

    private Label buildValueDetail(final String id, final String value) {
        final Label valueLabel = new Label(value);

        valueLabel.setId(id);
        valueLabel.addStyleName(SPUIDefinitions.TEXT_STYLE);

        return valueLabel;
    }

    public void disableSpacing() {
        keyValueDetailsLayout.setSpacing(false);
    }
}
