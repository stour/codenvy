package com.codenvy.api.permission.server;

import com.codenvy.api.permission.server.dao.PermissionsStorage;
import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.core.ConflictException;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link PermissionManager}
 *
 * TODO Cover all method
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class PermissionManagerTest {
    @Mock
    private PermissionsStorage permissionsStorage;

    private PermissionManager permissionManager;

    @BeforeMethod
    public void setUp() throws Exception {
        when(permissionsStorage.getDomains()).thenReturn(ImmutableSet.of(new TestDomain()));

        permissionManager = new PermissionManager(ImmutableSet.of(permissionsStorage));
    }

    @Test
    public void shouldBeAbleToStorePermissions() throws Exception {
        final Permissions permissions = new Permissions("user", "test", "instance", Collections.singletonList("setPermissions"));

        permissionManager.storePermission(permissions);

        verify(permissionsStorage).store(eq(permissions));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Can't edit permissions because there is not any another user with permission 'setPermissions'")
    public void shouldNotStorePermissionsWhenItRemoveLastSetPermissions() throws Exception {
        when(permissionsStorage.exists("user", "test", "instance", "setPermissions")).thenReturn(true);
        when(permissionsStorage.getByInstance("test", "instance"))
                .thenReturn(Collections.singletonList(new Permissions("user", "test", "instance", Collections.singletonList("delete"))));

        permissionManager.storePermission(new Permissions("user", "test", "instance", Collections.singletonList("delete")));
    }

    @Test
    public void shouldNotCheckExistingSetPermissionsIfUserDoesNotHaveItAtAll() throws Exception {
        when(permissionsStorage.exists("user", "test", "instance", "setPermissions")).thenReturn(false);
        when(permissionsStorage.getByInstance("test", "instance"))
                .thenReturn(Collections.singletonList(new Permissions("user", "test", "instance", Collections.singletonList("delete"))));

        permissionManager.storePermission(new Permissions("user", "test", "instance", Collections.singletonList("delete")));

        verify(permissionsStorage, never()).getByInstance(anyString(), anyString());
    }

    @Test
    public void shouldNotCheckExistingSetPermissionsIfNewPermissionsContainIt() throws Exception {
        permissionManager.storePermission(new Permissions("user", "test", "instance", Collections.singletonList("setPermissions")));

        verify(permissionsStorage).store(anyObject());
        verify(permissionsStorage, never()).exists(anyString(), anyString(), anyString(), anyString());
        verify(permissionsStorage, never()).getByInstance(anyString(), anyString());
    }

    public class TestDomain extends PermissionsDomain {
        public TestDomain() {
            super("test", ImmutableSet.of("read", "write", "use", "delete"));
        }
    }
}