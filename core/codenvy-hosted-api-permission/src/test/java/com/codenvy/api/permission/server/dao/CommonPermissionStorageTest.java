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
package com.codenvy.api.permission.server.dao;

import com.codenvy.api.permission.shared.Permissions;
import com.codenvy.api.permission.server.PermissionsDomain;
import com.codenvy.api.permission.server.PermissionsImpl;
import com.github.fakemongo.Fongo;
import com.google.common.collect.ImmutableSet;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.eclipse.che.api.core.ServerException;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static java.util.Collections.*;
import static java.util.Collections.singletonList;
import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link CommonPermissionStorage}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class CommonPermissionStorageTest {

    private MongoCollection<Permissions> collection;
    private CommonPermissionStorage      permissionStorage;

    @BeforeMethod
    public void setUpDb() throws Exception {
        final Fongo fongo = new Fongo("Permissions test server");
        final CodecRegistry defaultRegistry = MongoClient.getDefaultCodecRegistry();
        final MongoDatabase database = fongo.getDatabase("permissions")
                                            .withCodecRegistry(fromRegistries(defaultRegistry,
                                                                              fromCodecs(new PermissionsImplCodec(defaultRegistry))));
        collection = database.getCollection("permissions", Permissions.class);
        permissionStorage = new CommonPermissionStorage(database, "permissions", ImmutableSet.of(new TestDomain()));
    }

    @Test
    public void shouldStorePermissions() throws Exception {
        final PermissionsImpl permissions = createPermissions();

        permissionStorage.store(permissions);

        final Permissions result = collection.find(and(eq("user", permissions.getUser()),
                                                       eq("domain", permissions.getDomain()),
                                                       eq("instance", permissions.getInstance())))
                                             .first();
        assertEquals(result, permissions);
    }

    @Test
    public void shouldUpdatePermissionsWhenItHasAlreadyExisted() throws Exception {
        PermissionsImpl oldPermissions = createPermissions();
        permissionStorage.store(oldPermissions);

        PermissionsImpl newPermissions = new PermissionsImpl(oldPermissions.getUser(), oldPermissions.getDomain(), oldPermissions.getInstance(),
                                                     singletonList("read"));
        permissionStorage.store(newPermissions);

        final Permissions result = collection.find(and(eq("user", newPermissions.getUser()),
                                                       eq("domain", newPermissions.getDomain()),
                                                       eq("instance", newPermissions.getInstance())))
                                             .first();
        assertEquals(result, newPermissions);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Storage doesn't support domain with id 'fake'")
    public void shouldNotStorePermissionsWhenItHasUnsupportedDomain() throws Exception {
        final PermissionsImpl permissions = new PermissionsImpl("user",
                                                        "fake",
                                                        "test123",
                                                        Arrays.asList("read", "use", "create", "remove"));

        permissionStorage.store(permissions);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Domain with id 'test' doesn't support next action\\(s\\): \\w+, \\w+")
    public void shouldNotStorePermissionsWhenItContainsUnsupportedActions() throws Exception {
        final PermissionsImpl permissions = new PermissionsImpl("user",
                                                        "test",
                                                        "test123",
                                                        Arrays.asList("read", "use", "create", "remove"));

        permissionStorage.store(permissions);
    }

    @Test
    public void shouldReturnsSupportedDomainsIds() {
        assertEquals(permissionStorage.getDomains(), ImmutableSet.of(new TestDomain()));
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionWhenMongoExceptionWasThrewOnPermissionsStoring() throws Exception {
        final MongoDatabase db = mockDatabase(col -> doThrow(mock(MongoException.class)).when(col).replaceOne(any(), any(), any()));

        new CommonPermissionStorage(db, "permissions", ImmutableSet.of(new TestDomain())).store(createPermissions());
    }

    @Test
    public void shouldRemovePermissions() throws Exception {
        final Permissions permissions = createPermissions();
        collection.insertOne(permissions);

        permissionStorage.remove(permissions.getUser(), permissions.getDomain(), permissions.getInstance());

        assertEquals(collection.count(and(eq("user", permissions.getUser()),
                                          eq("domain", permissions.getDomain()),
                                          eq("instance", permissions.getInstance()))),
                     0);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionWhenMongoExceptionWasThrewOnPermissionsRemoving() throws Exception {
        final MongoDatabase db = mockDatabase(col -> doThrow(mock(MongoException.class)).when(col).deleteOne(any()));

        new CommonPermissionStorage(db, "permissions", ImmutableSet.of(new TestDomain())).remove("user", "test", "test123");
    }

    @Test
    public void shouldBeAbleToGetPermissionsByUser() throws Exception {
        final Permissions permissions = createPermissions();
        collection.insertOne(permissions);
        collection.insertOne(new PermissionsImpl("anotherUser", "test", "test123", singletonList("read")));

        permissionStorage.get(permissions.getUser());

        final Permissions result = collection.find(and(eq("user", permissions.getUser()),
                                                       eq("domain", permissions.getDomain()),
                                                       eq("instance", permissions.getInstance())))
                                             .first();
        assertEquals(permissions, result);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionWhenMongoExceptionWasThrewOnGettingPermissionsByUser() throws Exception {
        final MongoDatabase db = mockDatabase(col -> doThrow(mock(MongoException.class)).when(col).find((Bson)any()));

        new CommonPermissionStorage(db, "permissions", ImmutableSet.of(new TestDomain())).get("user");
    }

    @Test
    public void shouldBeAbleToGetPermissionsByUserAndDomain() throws Exception {
        final Permissions permissions = createPermissions();
        collection.insertOne(permissions);
        collection.insertOne(new PermissionsImpl("user", "anotherDomain", "test123", singletonList("read")));

        final List<PermissionsImpl> result = permissionStorage.get(permissions.getUser(), permissions.getDomain());

        assertEquals(result.size(), 1);
        assertEquals(result.get(0), permissions);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionWhenMongoExceptionWasThrewOnGettingPermissionsByUserAndDomain() throws Exception {
        final MongoDatabase db = mockDatabase(col -> doThrow(mock(MongoException.class)).when(col).find((Bson)any()));

        new CommonPermissionStorage(db, "permissions", ImmutableSet.of(new TestDomain())).get("user", "domain");
    }

    @Test
    public void shouldBeAbleToGetPermissionsByInstance() throws Exception {
        final Permissions permissions = createPermissions();
        collection.insertOne(permissions);
        collection.insertOne(new PermissionsImpl("user", "domain", "otherTest", singletonList("read")));

        final List<PermissionsImpl> result = permissionStorage.getByInstance(permissions.getDomain(), permissions.getInstance());

        assertEquals(result.size(), 1);
        assertEquals(result.get(0), permissions);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionWhenMongoExceptionWasThrewOnGettingPermissionsByInstance() throws Exception {
        final MongoDatabase db = mockDatabase(col -> doThrow(mock(MongoException.class)).when(col).find((Bson)any()));

        new CommonPermissionStorage(db, "permissions", ImmutableSet.of(new TestDomain())).getByInstance("domain", "test123");
    }

    @Test
    public void shouldBeAbleToGetPermissions() throws Exception {
        final Permissions permissions = createPermissions();
        collection.insertOne(permissions);

        final Permissions result = permissionStorage.get(permissions.getUser(), permissions.getDomain(), permissions.getInstance());

        assertEquals(result, permissions);
    }

    @Test
    public void shouldReturnsPermissionsWithEmptyActionsListWhenThereIsNotAnyPermissionsForGivenUserAndDomainAndInstance() throws Exception {
        final Permissions result = permissionStorage.get("user", "domain", "instance");

        assertEquals(result, new PermissionsImpl("user", "domain", "instance", emptyList()));
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionWhenMongoExceptionWasThrewOnGettingPermissions() throws Exception {
        final MongoDatabase db = mockDatabase(col -> doThrow(mock(MongoException.class)).when(col).find((Bson)any()));

        new CommonPermissionStorage(db, "permissions", ImmutableSet.of(new TestDomain())).get("user", "domain", "test123");
    }

    @Test
    public void shouldBeAbleToCheckPermissionExistence() throws Exception {
        final Permissions permissions = createPermissions();
        collection.insertOne(permissions);

        final boolean readPermissionExisted = permissionStorage.exists(permissions.getUser(), permissions.getDomain(), permissions.getInstance(), "read");
        final boolean fakePermissionExisted = permissionStorage.exists(permissions.getUser(), permissions.getDomain(), permissions.getInstance(), "fake");

        assertEquals(readPermissionExisted, permissions.getActions().contains("read"));
        assertEquals(fakePermissionExisted, permissions.getActions().contains("fake"));
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionWhenMongoExceptionWasThrewOnCheckinngPermissionExistence() throws Exception {
        final MongoDatabase db = mockDatabase(col -> doThrow(mock(MongoException.class)).when(col).find((Bson)any()));

        new CommonPermissionStorage(db, "permissions", ImmutableSet.of(new TestDomain())).exists("user", "domain", "test123", "read");
    }

    private PermissionsImpl createPermissions() {
        return new PermissionsImpl("user",
                               "test",
                               "test123",
                               Arrays.asList("read", "write", "use", "delete"));
    }


    public class TestDomain extends PermissionsDomain {
        public TestDomain() {
            super("test", ImmutableSet.of("read", "write", "use", "delete"));
        }
    }

    private MongoDatabase mockDatabase(Consumer<MongoCollection<Permissions>> consumer) {
        @SuppressWarnings("unchecked")
        final MongoCollection<Permissions> collection = mock(MongoCollection.class);
        consumer.accept(collection);

        final MongoDatabase database = mock(MongoDatabase.class);
        when(database.getCollection("permissions", Permissions.class)).thenReturn(collection);

        return database;
    }
}
