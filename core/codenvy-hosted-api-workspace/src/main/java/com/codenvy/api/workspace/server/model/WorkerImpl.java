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
package com.codenvy.api.workspace.server.model;

import com.codenvy.api.workspace.server.WorkspaceAction;

import java.util.List;

/**
 * @author Sergii Leschenko
 */
public class WorkerImpl implements Worker {
    private String                user;
    private String                workspace;
    private List<WorkspaceAction> actions;

    public WorkerImpl(String user, String workspace, List<WorkspaceAction> actions) {
        this.user = user;
        this.workspace = workspace;
        this.actions = actions;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getWorkspace() {
        return workspace;
    }

    @Override
    public List<WorkspaceAction> getActions() {
        return actions;
    }
}
