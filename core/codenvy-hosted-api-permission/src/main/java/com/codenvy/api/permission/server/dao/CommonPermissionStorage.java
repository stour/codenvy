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
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;

import org.bson.Document;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static java.util.function.Function.identity;

/**
 * Common implementation for {@link PermissionsStorage} based on MongoDB storage.
 *
 * <p>Stores permissions of domains that bound by {@link CommonDomains}
 *
 * <p>Example of domain binding
 * <pre>
 *     Multibinder<PermissionsDomain> multibinder = Multibinder.newSetBinder(binder(), PermissionsDomain.class, CommonDomain.class);
 *     multibinder.addBinding().toInstance(new PermissionsDomain("myDomain",
 *                                                               new HashSet&lt;&gt;(Arrays.asList("read", "write", "use", "setPermissions"))));
 * </pre>
 *
 * <p>Permissions collection document scheme:
 * <pre>
 *
 * {
 *     "user" : "user123",
 *     "domain" : "workspace",
 *     "instance" : "workspace123",
 *     "actions" : [
 *         "read",
 *         "write",
 *         ...
 *     ]
 * }
 *
 * </pre>
 *
 * @author Sergii Leschenko
 */
@Singleton
public class CommonPermissionStorage implements PermissionsStorage {
    private final MongoCollection<PermissionsImpl> collection;

    private final Map<String, PermissionsDomain> idToDomain;

    @Inject
    public CommonPermissionStorage(@Named("mongo.db.organization") MongoDatabase database,
                                   @Named("organization.storage.db.permission.collection") String collectionName,
                                   @CommonDomains Set<PermissionsDomain> permissionsDomains) throws IOException {
        collection = database.getCollection(collectionName, PermissionsImpl.class);
        collection.createIndex(new Document("user", 1).append("domain", 1).append("instance", 1), new IndexOptions().unique(true));

        this.idToDomain = permissionsDomains.stream()
                                            .collect(Collectors.toMap(PermissionsDomain::getId, identity()));
    }

    @Override
    public Set<PermissionsDomain> getDomains() {
        return new HashSet<>(idToDomain.values());
    }

    @Override
    public void store(PermissionsImpl permissions) throws ServerException {
        final PermissionsDomain permissionsDomain = idToDomain.get(permissions.getDomain());
        if (permissionsDomain == null) {
            throw new IllegalArgumentException("Storage doesn't support domain with id '" + permissions.getDomain() + "'");
        }

        final Set<String> allowedActions = permissionsDomain.getAllowedActions();
        final Set<String> unsupportedActions = permissions.getActions()
                                                         .stream()
                                                         .filter(action -> !allowedActions.contains(action))
                                                         .collect(Collectors.toSet());
        if (!unsupportedActions.isEmpty()) {
            throw new IllegalArgumentException("Domain with id '" + permissions.getDomain() + "' doesn't support next action(s): " +
                                               unsupportedActions.stream()
                                                                 .collect(Collectors.joining(", ")));
        }


        try {
            collection.replaceOne(and(eq("user", permissions.getUser()),
                                      eq("domain", permissions.getDomain()),
                                      eq("instance", permissions.getInstance())),
                                  permissions,
                                  new UpdateOptions().upsert(true));
        } catch (MongoException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    public void remove(String user, String domain, String instance) throws ServerException, ConflictException {
        try {
            collection.deleteOne(and(eq("user", user),
                                     eq("domain", domain),
                                     eq("instance", instance)));
        } catch (MongoException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    public List<PermissionsImpl> get(String user) throws ServerException {
        try {
            return collection.find(eq("user", user))
                             .into(new ArrayList<>());
        } catch (MongoException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    public List<PermissionsImpl> get(String user, String domain) throws ServerException {
        try {
            return collection.find(and(eq("user", user),
                                       eq("domain", domain)))
                             .into(new ArrayList<>());
        } catch (MongoException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    public PermissionsImpl get(String user, String domain, String instance) throws ServerException {
        PermissionsImpl found;
        try {
            found = collection.find(and(eq("user", user),
                                        eq("domain", domain),
                                        eq("instance", instance)))
                              .first();
        } catch (MongoException e) {
            throw new ServerException(e.getMessage(), e);
        }

        if (found == null) {
            //TODO Maybe we should throw exception here
            return new PermissionsImpl(user, domain, instance, Collections.emptyList());
        }

        return found;
    }

    @Override
    public List<PermissionsImpl> getByInstance(String domain, String instance) throws ServerException {
        try {
            return collection.find(and(eq("domain", domain),
                                       eq("instance", instance)))
                             .into(new ArrayList<>());
        } catch (MongoException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String user, String domain, String instance, String requiredAction) throws ServerException {
        try {
            final Permissions found = collection.find(and(eq("user", user),
                                                          eq("domain", domain),
                                                          eq("instance", instance),
                                                          in("actions", requiredAction)))
                                                .first();
            return found != null;
        } catch (MongoException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }
}
