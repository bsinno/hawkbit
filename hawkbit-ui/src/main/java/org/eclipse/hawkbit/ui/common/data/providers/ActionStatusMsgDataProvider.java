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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.model.ActionStatus;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyActionStatus;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMessage;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
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
        final int pagesize = query.getLimit() > 0 ? query.getLimit() : SPUIDefinitions.PAGE_SIZE;
        final PageRequest pageRequest = PageRequest.of(query.getOffset() / pagesize, pagesize, defaultSortOrder);

        return loadBackendEntities(pageRequest, query.getFilter()).map(this::createProxyMessages)
                .orElse(Collections.emptyList()).stream();
    }

    private Optional<Page<String>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<Long> currentlySelectedActionStatusId) {
        return currentlySelectedActionStatusId
                .map(id -> deploymentManagement.findMessagesByActionStatusId(pageRequest, id));
    }

    /**
     * Creates a list of {@link ProxyActionStatus} for presentation layer from
     * page of {@link ActionStatus}.
     *
     * @param actionBeans
     *            page of {@link ActionStatus}
     * @return list of {@link ProxyActionStatus}
     */
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
        final int pagesize = query.getLimit() > 0 ? query.getLimit() : SPUIDefinitions.PAGE_SIZE;
        final PageRequest pageRequest = PageRequest.of(query.getOffset() / pagesize, pagesize, defaultSortOrder);

        final long size = sizeInBackEnd(pageRequest, query.getFilter());

        if (size > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return (int) size;
    }

    private long sizeInBackEnd(final PageRequest pageRequest, final Optional<Long> currentlySelectedActionStatusId) {
        return currentlySelectedActionStatusId
                .map(id -> deploymentManagement.findMessagesByActionStatusId(pageRequest, id).getTotalElements())
                .orElse(0L);
    }

    @Override
    public Object getId(final ProxyMessage item) {
        Objects.requireNonNull(item, "Cannot provide an id for a null item.");
        return item.getId();
    }
}
