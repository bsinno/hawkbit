/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.OffsetBasedPageRequest;
import org.eclipse.hawkbit.repository.model.ActionStatus;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.StringUtils;

import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.Query;

/**
 * Data provider for Action message, which dynamically loads a batch of Action
 * messages from backend and maps them to corresponding {@link ProxyMessage}
 * entities. The filter is used for master-details relationship with
 * {@link ActionStatus}, using its id.
 */
public class ActionStatusMsgDataProvider extends AbstractBackEndDataProvider<ProxyMessage, Long> {
    private static final long serialVersionUID = 1L;

    private final Sort defaultSortOrder = new Sort(Direction.DESC, "id");

    private final transient DeploymentManagement deploymentManagement;
    private final String noMessageText;

    public ActionStatusMsgDataProvider(final DeploymentManagement deploymentManagement, final String noMessageText) {
        this.deploymentManagement = deploymentManagement;
        this.noMessageText = noMessageText;
    }

    @Override
    protected Stream<ProxyMessage> fetchFromBackEnd(final Query<ProxyMessage, Long> query) {
        return query.getFilter()
                .map(filter -> createProxyMessages(
                        loadBackendEntities(convertToPageRequest(query, defaultSortOrder), filter)).stream())
                .orElse(Stream.empty());
    }

    private Page<String> loadBackendEntities(final PageRequest pageRequest,
            final Long currentlySelectedActionStatusId) {
        if (currentlySelectedActionStatusId == null) {
            return Page.empty(pageRequest);
        }

        return deploymentManagement.findMessagesByActionStatusId(pageRequest, currentlySelectedActionStatusId);
    }

    // TODO: remove duplication with ProxyDataProvider
    private PageRequest convertToPageRequest(final Query<ProxyMessage, Long> query, final Sort sort) {
        return new OffsetBasedPageRequest(query.getOffset(), query.getLimit(), sort);
    }

    private List<ProxyMessage> createProxyMessages(final Page<String> messages) {
        final List<ProxyMessage> proxyMsgs = new ArrayList<>(messages.getNumberOfElements());

        Long idx = messages.getNumber() * ((long) messages.getSize());
        for (final String msg : messages.getContent()) {
            final ProxyMessage proxyMsg = new ProxyMessage();

            proxyMsg.setMessage(msg);
            proxyMsg.setId(++idx);

            proxyMsgs.add(proxyMsg);
        }

        if (messages.getTotalElements() == 1L && StringUtils.isEmpty(proxyMsgs.get(0).getMessage())) {
            proxyMsgs.get(0).setMessage(noMessageText);
        }

        return proxyMsgs;
    }

    @Override
    protected int sizeInBackEnd(final Query<ProxyMessage, Long> query) {
        final long size = sizeInBackEnd(convertToPageRequest(query, defaultSortOrder), query.getFilter().orElse(null));

        if (size > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return (int) size;
    }

    private long sizeInBackEnd(final PageRequest pageRequest, final Long currentlySelectedActionStatusId) {
        if (currentlySelectedActionStatusId == null) {
            return 0L;
        }

        return deploymentManagement.findMessagesByActionStatusId(pageRequest, currentlySelectedActionStatusId)
                .getTotalElements();
    }

    @Override
    public Object getId(final ProxyMessage item) {
        Objects.requireNonNull(item, "Cannot provide an id for a null item.");
        return item.getId();
    }
}
