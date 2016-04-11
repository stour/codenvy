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
package com.codenvy.api.permission.shared.dto;

import com.codenvy.api.permission.shared.Permissions;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Sergii Leschenko
 */
@DTO
public interface PermissionsDto extends Permissions {
    @Override
    String getUser();

    void setUser(String user);

    PermissionsDto withUser(String user);

    @Override
    String getDomain();

    void setDomain(String domain);

    PermissionsDto withDomain(String domain);

    @Override
    String getInstance();

    void setInstance(String instance);

    PermissionsDto withInstance(String instance);

    @Override
    List<String> getActions();

    void setActions(List<String> actions);

    PermissionsDto withActions(List<String> actions);
}
