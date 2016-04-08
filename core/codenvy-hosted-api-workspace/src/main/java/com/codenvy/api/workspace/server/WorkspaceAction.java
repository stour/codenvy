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

/**
 * @author Sergii Leschenko
 */
public enum WorkspaceAction {
    READ("read"),
    RUN("run"),
    USE("use"),
    CONFIGURE("configure"),
    SET_PERMISSIONS("setPermissions"),
    READ_PERMISSIONS("readPermissions"),
    DELETE("delete");

    private final String value;

    WorkspaceAction(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static WorkspaceAction getEnum(String value) {
        for (WorkspaceAction workspaceAction : values()) {
            if (workspaceAction.value.equals(value)) {
                return workspaceAction;
            }
        }
        throw new IllegalArgumentException("No enum constant " + WorkspaceAction.class.getCanonicalName() + " with given value " + value);
    }

    public static void main(String[] args) {
        WorkspaceAction.valueOf("READ");
    }
}