/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.api.workspace.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.codenvy.api.workspace.server.WorkspaceDomain.WorkspaceActions;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.server.WorkspaceValidator;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.commons.env.EnvironmentContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.workspace.server.DtoConverter.asDto;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_GET_WORKSPACES;

/**
 * Implementation of {@link WorkspaceService} that overrides {@link WorkspaceService#getWorkspaces(Integer, Integer, String)} method.
 *
 * New implementation of method use '{@link WorkspaceActions#READ read}' permission for fetching workspaces that belong to current user.
 *
 * @author Sergii Leschenko
 */
@Api(value = "/workspace", description = "Workspace REST API")
@Path("/workspace")
public class PermissionsWorkspaceService extends WorkspaceService {

    private final WorkspacePermissionStorage workspacePermissionStorage;

    @Context
    private SecurityContext securityContext;

    @Inject
    public PermissionsWorkspaceService(WorkspaceManager workspaceManager,
                                       MachineManager machineManager,
                                       WorkspaceValidator validator,
                                       WorkspacePermissionStorage workspacePermissionStorage,
                                       @Named("che.ide.context") String ideContext) {
        super(workspaceManager, machineManager, validator, ideContext);
        this.workspacePermissionStorage = workspacePermissionStorage;
    }

    @GET
    @Produces(APPLICATION_JSON)
    @GenerateLink(rel = LINK_REL_GET_WORKSPACES)
    @ApiOperation(value = "Get the workspaces where the current user has read permission",
                  notes = "This operation can be performed only by authorized user",
                  response = WorkspaceDto.class,
                  responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "The workspaces successfully fetched"),
                   @ApiResponse(code = 500, message = "Internal server error occurred during workspaces fetching")})
    public List<WorkspaceDto> getWorkspaces(@ApiParam("The number of the items to skip")
                                            @DefaultValue("0")
                                            @QueryParam("skipCount")
                                            Integer skipCount,
                                            @ApiParam("The limit of the items in the response, default is 30")
                                            @DefaultValue("30")
                                            @QueryParam("maxItems")
                                            Integer maxItems) throws ServerException, BadRequestException {
        return workspacePermissionStorage.getWorkspaces(getCurrentUserId(), WorkspaceActions.READ)
                                         .stream()
                                         .map(workspace -> injectLinks(asDto(workspace)))
                                         .collect(toList());
    }

    private static String getCurrentUserId() {
        return EnvironmentContext.getCurrent().getUser().getId();
    }
}
