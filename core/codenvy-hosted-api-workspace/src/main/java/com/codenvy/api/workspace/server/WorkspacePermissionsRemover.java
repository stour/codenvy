/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.api.workspace.server;

import com.codenvy.api.workspace.server.dao.WorkerDao;
import com.codenvy.api.workspace.server.model.WorkerImpl;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.event.WorkspaceRemovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Removes permissions related to workspace when it was removed
 *
 * @author Sergii Leschenko
 */
@Singleton
public class WorkspacePermissionsRemover implements EventSubscriber<WorkspaceRemovedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspacePermissionsRemover.class);

    private final EventService eventService;
    private final WorkerDao    workerDao;

    @Inject
    public WorkspacePermissionsRemover(EventService eventService,
                                       WorkerDao workerDao) {
        this.eventService = eventService;
        this.workerDao = workerDao;
    }

    @Override
    public void onEvent(WorkspaceRemovedEvent event) {
        final List<WorkerImpl> workers;
        try {
            workers = workerDao.getWorkers(event.getWorkspaceId());
        } catch (ServerException e) {
            LOG.error("Can't workers of workspace '" + event.getWorkspaceId() + "'", e);
            return;
        }

        for (WorkerImpl worker : workers) {
            try {
                workerDao.removeWorker(worker.getWorkspace(), worker.getUser());
            } catch (ServerException e) {
                LOG.error("Can't remove user's workers to workspace", e);
            }
        }
    }

    @PostConstruct
    void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    void unsubscribe() {
        eventService.unsubscribe(this);
    }
}
