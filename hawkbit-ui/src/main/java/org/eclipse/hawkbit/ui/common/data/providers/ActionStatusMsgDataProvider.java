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
import java.util.stream.Stream;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.model.ActionStatus;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.StringUtils;

/**
 * Data provider for Action message, which dynamically loads a batch of Action
 * messages from backend and maps them to corresponding {@link ProxyMessage}
 * entities. The filter is used for master-details relationship with
 * {@link ActionStatus}, using its id.
 */
public class ActionStatusMsgDataProvider extends GenericDataProvider<ProxyMessage, String, Long> {
    private static final long serialVersionUID = 1L;

    private final transient DeploymentManagement deploymentManagement;
    private final String noMessageText;

    public ActionStatusMsgDataProvider(final DeploymentManagement deploymentManagement, final String noMessageText) {
        this(deploymentManagement, noMessageText, new Sort(Direction.DESC, "id"));
    }

    public ActionStatusMsgDataProvider(final DeploymentManagement deploymentManagement, final String noMessageText,
            final Sort defaultSortOrder) {
        super(defaultSortOrder);

        this.deploymentManagement = deploymentManagement;
        this.noMessageText = noMessageText;
    }

    @Override
    protected Stream<ProxyMessage> getProxyEntities(final Slice<String> backendEntities) {
        return createProxyMessages(backendEntities).stream();
    }

    private List<ProxyMessage> createProxyMessages(final Slice<String> messages) {
        final List<ProxyMessage> proxyMsgs = new ArrayList<>(messages.getNumberOfElements());

        Long idx = messages.getNumber() * ((long) messages.getSize());
        for (final String msg : messages.getContent()) {
            final ProxyMessage proxyMsg = new ProxyMessage();

            proxyMsg.setMessage(msg);
            proxyMsg.setId(++idx);

            proxyMsgs.add(proxyMsg);
        }

        if (proxyMsgs.size() == 1L && StringUtils.isEmpty(proxyMsgs.get(0).getMessage())) {
            proxyMsgs.get(0).setMessage(noMessageText);
        }

        return proxyMsgs;
    }

    @Override
    protected Page<String> loadBackendEntities(final PageRequest pageRequest, final Long actionStatusId) {
        if (actionStatusId == null) {
            return Page.empty(pageRequest);
        }

        return deploymentManagement.findMessagesByActionStatusId(pageRequest, actionStatusId);
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Long actionStatusId) {
        if (actionStatusId == null) {
            return 0L;
        }

        return loadBackendEntities(pageRequest, actionStatusId).getTotalElements();
    }
}
