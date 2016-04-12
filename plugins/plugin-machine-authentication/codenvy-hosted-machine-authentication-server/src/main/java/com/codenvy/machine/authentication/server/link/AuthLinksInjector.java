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
package com.codenvy.machine.authentication.server.link;


import com.codenvy.machine.authentication.shared.dto.MachineTokenDto;
import com.google.common.base.MoreObjects;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
import org.eclipse.che.api.machine.server.MachineService;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.SnapshotDto;
import org.eclipse.che.api.workspace.server.LinksInjector;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.util.LinksHelper.createLink;
import static org.eclipse.che.api.machine.shared.Constants.TERMINAL_REFERENCE;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_WEBSOCKET_REFERENCE;
import static org.eclipse.che.api.workspace.shared.Constants.GET_ALL_USER_WORKSPACES;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_GET_SNAPSHOT;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_GET_WORKSPACE_EVENTS_CHANNEL;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_IDE_URL;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_REMOVE_WORKSPACE;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_SELF;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_START_WORKSPACE;
import static org.eclipse.che.api.workspace.shared.Constants.LINK_REL_STOP_WORKSPACE;
import static org.eclipse.che.api.workspace.shared.Constants.LIN_REL_GET_WORKSPACE;
import static org.eclipse.che.dto.server.DtoFactory.cloneDto;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * @author Anton Korneta
 */
public class AuthLinksInjector extends LinksInjector {

    final String                 ideContext;
    final String                 tokenServiceBaseUrl;
    final HttpJsonRequestFactory httpJsonRequestFactory;

    @Inject
    public AuthLinksInjector(@Named("che.ide.context") String ideContext,
                             @Named("api.endpoint") String apiEndpoint,
                             HttpJsonRequestFactory httpJsonRequestFactory) {
        super(ideContext);
        this.ideContext = ideContext;
        this.tokenServiceBaseUrl = apiEndpoint + "/machine/token/";
        this.httpJsonRequestFactory = httpJsonRequestFactory;
    }

    public WorkspaceDto injectLinks(WorkspaceDto workspace, ServiceContext serviceContext) {
        final UriBuilder uriBuilder = serviceContext.getServiceUriBuilder();
        final List<Link> links = new ArrayList<>();
        // add common workspace links
        links.add(createLink("GET",
                             uriBuilder.clone()
                                       .path(WorkspaceService.class, "getByKey")
                                       .build(workspace.getId())
                                       .toString(),
                             LINK_REL_SELF));
        links.add(createLink("POST",
                             uriBuilder.clone()
                                       .path(WorkspaceService.class, "startById")
                                       .build(workspace.getId())
                                       .toString(),
                             APPLICATION_JSON,
                             LINK_REL_START_WORKSPACE));
        links.add(createLink("DELETE",
                             uriBuilder.clone()
                                       .path(WorkspaceService.class, "delete")
                                       .build(workspace.getId())
                                       .toString(),
                             APPLICATION_JSON,
                             LINK_REL_REMOVE_WORKSPACE));
        links.add(createLink("GET",
                             uriBuilder.clone()
                                       .path(WorkspaceService.class, "getWorkspaces")
                                       .build()
                                       .toString(),
                             APPLICATION_JSON,
                             GET_ALL_USER_WORKSPACES));
        links.add(createLink("GET",
                             uriBuilder.clone()
                                       .path(WorkspaceService.class, "getSnapshot")
                                       .build(workspace.getId())
                                       .toString(),
                             APPLICATION_JSON,
                             LINK_REL_GET_SNAPSHOT));

        //TODO here we add url to IDE with workspace name not good solution do it here but critical for this task  https://jira.codenvycorp.com/browse/IDEX-3619
        final URI ideUri = uriBuilder.clone()
                                     .replacePath(ideContext)
                                     .path(workspace.getConfig().getName())
                                     .build();
        links.add(createLink("GET", ideUri.toString(), TEXT_HTML, LINK_REL_IDE_URL));

        // add workspace channel link
        final Link workspaceChannelLink = createLink("GET",
                                                     serviceContext.getBaseUriBuilder()
                                                                   .path("ws")
                                                                   .path(workspace.getId())
                                                                   .scheme("https".equals(ideUri.getScheme()) ? "wss" : "ws")
                                                                   .build()
                                                                   .toString(),
                                                     null);
        final LinkParameter channelParameter = newDto(LinkParameter.class).withName("channel")
                                                                          .withRequired(true);

        links.add(cloneDto(workspaceChannelLink).withRel(LINK_REL_GET_WORKSPACE_EVENTS_CHANNEL)
                                                .withParameters(singletonList(
                                                        cloneDto(channelParameter).withDefaultValue("workspace:" + workspace.getId()))));

        // add machine channels links to machines configs
        workspace.getConfig()
                 .getEnvironments()
                 .stream()
                 .forEach(environmentDto -> injectMachineChannelsLinks(environmentDto,
                                                                       workspace.getId(),
                                                                       workspaceChannelLink,
                                                                       channelParameter));
        // add links for running workspace
        if (workspace.getStatus() == RUNNING) {
            workspace.getRuntime()
                     .getLinks()
                     .add(createLink("DELETE",
                                     uriBuilder.clone()
                                               .path(WorkspaceService.class, "stop")
                                               .build(workspace.getId())
                                               .toString(),
                                     LINK_REL_STOP_WORKSPACE));

            if (workspace.getRuntime() != null && workspace.getRuntime().getDevMachine() != null) {
                String token = null;
                try {
                    token = httpJsonRequestFactory.fromUrl(tokenServiceBaseUrl + workspace.getId())
                                                  .setMethod(HttpMethod.GET)
                                                  .request()
                                                  .asDto(MachineTokenDto.class).getMachineToken();
                } catch (ApiException | IOException ignore) {
                }
                final String userInfo = "codenvy:" + MoreObjects.firstNonNull(token, "");
                workspace.getRuntime()
                         .getDevMachine()
                         .getRuntime()
                         .getServers()
                         .values()
                         .stream()
                         .filter(server -> WSAGENT_REFERENCE.equals(server.getRef()))
                         .findAny()
                         .ifPresent(wsAgent -> {
                             workspace.getRuntime()
                                      .getLinks()
                                      .add(createLink("GET",
                                                      wsAgent.getUrl(),
                                                      WSAGENT_REFERENCE));
                             workspace.getRuntime()
                                      .getLinks()
                                      .add(createLink("GET",
                                                      UriBuilder.fromUri(wsAgent.getUrl())
                                                                .scheme("https".equals(ideUri.getScheme()) ? "wss" : "ws")
                                                                .build()
                                                                .toString(),
                                                      WSAGENT_WEBSOCKET_REFERENCE));
                         });

                workspace.getRuntime()
                         .getDevMachine()
                         .getRuntime()
                         .getServers()
                         .values()
                         .stream()
                         .filter(server -> WSAGENT_REFERENCE.equals(server.getRef()))
                         .findAny()
                         .ifPresent(wsAgent -> {
                             workspace.getRuntime()
                                      .getDevMachine()
                                      .getLinks()
                                      .add(createLink("GET",
                                                      UriBuilder.fromUri(wsAgent.getUrl())
                                                                .userInfo(userInfo)
                                                                .build()
                                                                .toString(),
                                                      WSAGENT_REFERENCE));
                             workspace.getRuntime()
                                      .getDevMachine()
                                      .getLinks()
                                      .add(createLink("GET",
                                                      UriBuilder.fromUri(wsAgent.getUrl())
                                                                .userInfo(userInfo)
                                                                .scheme("https".equals(ideUri.getScheme()) ? "wss" : "ws")
                                                                .build()
                                                                .toString(),
                                                      WSAGENT_WEBSOCKET_REFERENCE));
                         });

                workspace.getRuntime()
                         .getDevMachine()
                         .getRuntime()
                         .getServers()
                         .values()
                         .stream()
                         .filter(server -> TERMINAL_REFERENCE.equals(server.getRef()))
                         .findAny()
                         .ifPresent(terminal -> workspace.getRuntime()
                                                         .getDevMachine()
                                                         .getLinks()
                                                         .add(createLink("GET",
                                                                         UriBuilder.fromUri(terminal.getUrl())
                                                                                   .userInfo(userInfo)
                                                                                   .scheme("https".equals(ideUri.getScheme()) ? "wss"
                                                                                                                              : "ws")
                                                                                   .build()
                                                                                   .toString(),
                                                                         TERMINAL_REFERENCE)));

                for (MachineDto machineDto : workspace.getRuntime().getMachines()) {
                    machineDto.getRuntime()
                              .getServers()
                              .values()
                              .stream()
                              .filter(server -> WSAGENT_REFERENCE.equals(server.getRef()))
                              .findAny()
                              .ifPresent(wsAgent -> {
                                  machineDto.getLinks()
                                            .add(createLink("GET",
                                                            wsAgent.getUrl(),
                                                            WSAGENT_REFERENCE));
                                  machineDto.getLinks()
                                            .add(createLink("GET",
                                                            UriBuilder.fromUri(wsAgent.getUrl())
                                                                      .userInfo(userInfo)
                                                                      .scheme("https".equals(ideUri.getScheme()) ? "wss" : "ws")
                                                                      .build()
                                                                      .toString(),
                                                            WSAGENT_WEBSOCKET_REFERENCE));
                              });

                    machineDto.getRuntime()
                              .getServers()
                              .values()
                              .stream()
                              .filter(server -> TERMINAL_REFERENCE.equals(server.getRef()))
                              .findAny()
                              .ifPresent(terminal -> machineDto.getLinks()
                                                               .add(createLink("GET",
                                                                               UriBuilder.fromUri(terminal.getUrl())
                                                                                         .userInfo(userInfo)
                                                                                         .scheme("https".equals(ideUri.getScheme()) ? "wss"
                                                                                                                                    : "ws")
                                                                                         .build()
                                                                                         .toString(),
                                                                               TERMINAL_REFERENCE)));
                }
            }
        }
        return workspace.withLinks(links);
    }

    private void injectMachineChannelsLinks(EnvironmentDto environmentDto,
                                            String workspaceId,
                                            Link machineChannelLink,
                                            LinkParameter channelParameter) {

        for (MachineConfigDto machineConfigDto : environmentDto.getMachineConfigs()) {
            MachineService.injectMachineChannelsLinks(machineConfigDto,
                                                      workspaceId,
                                                      environmentDto.getName(),
                                                      machineChannelLink,
                                                      channelParameter);
        }
    }

    public SnapshotDto injectLinks(SnapshotDto snapshotDto, ServiceContext serviceContext) {
        final UriBuilder uriBuilder = serviceContext.getServiceUriBuilder();
        final Link machineLink = createLink("GET",
                                            serviceContext.getBaseUriBuilder()
                                                          .path("/machine/{id}")
                                                          .build(snapshotDto.getId())
                                                          .toString(),
                                            APPLICATION_JSON,
                                            "get machine");
        final Link workspaceLink = createLink("GET",
                                              uriBuilder.clone()
                                                        .path(WorkspaceService.class, "getByKey")
                                                        .build(snapshotDto.getWorkspaceId())
                                                        .toString(),
                                              APPLICATION_JSON,
                                              LIN_REL_GET_WORKSPACE);
        final Link workspaceSnapshotLink = createLink("GET",
                                                      uriBuilder.clone()
                                                                .path(WorkspaceService.class, "getSnapshot")
                                                                .build(snapshotDto.getWorkspaceId())
                                                                .toString(),
                                                      APPLICATION_JSON,
                                                      LINK_REL_SELF);
        return snapshotDto.withLinks(asList(machineLink, workspaceLink, workspaceSnapshotLink));
    }
}
