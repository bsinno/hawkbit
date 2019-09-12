package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.Collections;
import java.util.List;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyKeyValueDetails;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;

import com.vaadin.shared.ui.ContentMode;
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
        keyValueDetailLayout.setSpacing(false);
        keyValueDetailLayout.setMargin(false);

        keyValueDetailLayout.addComponent(
                buildKeyValueDetailLabel(keyValueDetail.getId(), keyValueDetail.getKey(), keyValueDetail.getValue()));

        keyValueDetailsLayout.addComponent(keyValueDetailLayout);
    }

    private Label buildKeyValueDetailLabel(final String id, final String key, final String value) {
        final Label keyValueDetailLabel = new Label("<b>" + sanitized(key) + "</b> " + sanitized(value),
                ContentMode.HTML);

        keyValueDetailLabel.setId(id);
        keyValueDetailLabel.setSizeFull();
        keyValueDetailLabel.addStyleName(SPUIDefinitions.TEXT_STYLE);
        keyValueDetailLabel.addStyleName("label-style");

        return keyValueDetailLabel;
    }

    // TODO: move to utilities, add HTML/Javascript sanitization
    private String sanitized(final String input) {
        return input != null ? input : "";
    }
}
