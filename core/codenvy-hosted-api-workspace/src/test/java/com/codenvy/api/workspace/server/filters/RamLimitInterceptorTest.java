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
package com.codenvy.api.workspace.server.filters;

import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.user.UserImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RamLimitInterceptor}
 *
 * //TODO Cover all use cases
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class RamLimitInterceptorTest {
    private static final String WORKSPACE_ID     = "workspaceId";
    private static final String DEFAULT_ENV_NAME = "defaultEnv";

    @Mock
    private MethodInvocation invocation;

    @Mock
    private WorkspaceManager workspaceManager;

    @Mock
    private WorkspaceImpl workspace;

    RamLimitInterceptor ramLimitInterceptor;

    @BeforeMethod
    public void setUp() throws Exception {
        ramLimitInterceptor = new RamLimitInterceptor();
        ramLimitInterceptor.workspaceManager = workspaceManager;

        EnvironmentContext.getCurrent().setUser(new UserImpl(null, "userId", null, null, false));

        when(workspace.getId()).thenReturn(WORKSPACE_ID);
        final WorkspaceConfigImpl machineConfig = createWorkspaceConfig(DEFAULT_ENV_NAME, 1024);
        when(workspace.getConfig()).thenReturn(machineConfig);

        when(workspaceManager.getWorkspace(WORKSPACE_ID)).thenReturn(workspace);
    }

    @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "Can't find workspace's environment")
    public void shouldThrowExceptionWhenEnvWithGivenIdDoesNotExist() throws Throwable {
        // preparing workspace service's method
        final Method method = getServiceMethod(new Object[] {"startById", String.class, String.class, String.class});
        when(invocation.getMethod()).thenReturn(method);

        // preparing invocation arguments which are actually the same size as method parameters
        final Object[] invocationArgs = new Object[method.getParameterCount()];
        invocationArgs[0] = "workspaceId";
        invocationArgs[1] = "myCustomEnvironment";
        when(invocation.getArguments()).thenReturn(invocationArgs);

        // do the interception
        ramLimitInterceptor.invoke(invocation);
    }

    @Test
    public void shouldCheckRamLimitWhenIsUsedCustomEnvironment() throws Throwable {
        // preparing workspace service's method
        final Method method = getServiceMethod(new Object[] {"startById", String.class, String.class, String.class});
        when(invocation.getMethod()).thenReturn(method);

        // preparing invocation arguments which are actually the same size as method parameters
        final Object[] invocationArgs = new Object[method.getParameterCount()];
        invocationArgs[0] = "workspaceId";
        invocationArgs[1] = "myCustomEnvironment";
        when(invocation.getArguments()).thenReturn(invocationArgs);
        @SuppressWarnings("unchecked")
        final WorkspaceConfigImpl machineConfig = createWorkspaceConfig(Pair.of(DEFAULT_ENV_NAME, 1024),
                                                                        Pair.of("myCustomEnvironment", 1024));
        when(workspace.getConfig()).thenReturn(machineConfig);

        // do the interception
        ramLimitInterceptor.invoke(invocation);

        verify(invocation.proceed());
    }

    /**
     * Provides 1 argument which is array of objects, first element is method name, the other elements are type parameters.
     */
    @DataProvider(name = "workspaceServiceMethods")
    private Object[][] serviceMethodsProvider() {
        return new Object[][] {
                {new Object[] {"startById", String.class, String.class, String.class}},
                {new Object[] {"startFromConfig", WorkspaceConfigDto.class, Boolean.class, String.class}},
        };
    }

    private WorkspaceConfigImpl createWorkspaceConfig(String envName, int ram) {
        WorkspaceConfigImpl workspaceConfig = mock(WorkspaceConfigImpl.class);
        MachineConfigImpl machineConfig = mock(MachineConfigImpl.class);
        EnvironmentImpl environment = mock(EnvironmentImpl.class);

        when(environment.getName()).thenReturn(envName);
        when(environment.getMachineConfigs()).thenReturn(Collections.singletonList(machineConfig));
        when(machineConfig.getLimits()).thenReturn(new LimitsImpl(ram));
        when(workspaceConfig.getEnvironments()).thenReturn(Collections.singletonList(environment));
        return workspaceConfig;
    }

    private WorkspaceConfigImpl createWorkspaceConfig(Pair<String, Integer>... envNameToRam) {
        WorkspaceConfigImpl workspaceConfig = mock(WorkspaceConfigImpl.class);

        List<EnvironmentImpl> envs = new ArrayList<>();
        for (Pair<String, Integer> stringIntegerPair : envNameToRam) {
            EnvironmentImpl environment = mock(EnvironmentImpl.class);
            MachineConfigImpl machineConfig = mock(MachineConfigImpl.class);
            when(environment.getName()).thenReturn(stringIntegerPair.first);
            when(environment.getMachineConfigs()).thenReturn(Collections.singletonList(machineConfig));
            when(machineConfig.getLimits()).thenReturn(new LimitsImpl(stringIntegerPair.second));
            envs.add(environment);
        }

        when(workspaceConfig.getEnvironments()).thenReturn(envs);
        return workspaceConfig;
    }

    /**
     * Gets a {@link WorkspaceService} method based on data provided by {@link #serviceMethodsProvider()
     */
    private Method getServiceMethod(Object[] methodDescription) throws Exception {
        final Class<?>[] methodParams = new Class<?>[methodDescription.length - 1];
        for (int i = 1; i < methodDescription.length; i++) {
            methodParams[i - 1] = (Class<?>)methodDescription[i];
        }
        return WorkspaceService.class.getMethod(methodDescription[0].toString(), methodParams);
    }
}