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
package com.codenvy.api.workspace.server.dao;

import com.codenvy.api.workspace.server.model.WorkerImpl;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import java.util.List;

/**
 * @author Sergii Leschenko
 */
public interface WorkerDao {
    void store(WorkerImpl worker) throws ServerException;

    WorkerImpl getWorker(String workspace, String user) throws NotFoundException, ServerException;

    void removeWorker(String workspace, String user) throws ServerException;

    List<WorkerImpl> getWorkers(String workspace) throws ServerException;

    List<WorkerImpl> getWorkersByUser(String user) throws ServerException;
}
