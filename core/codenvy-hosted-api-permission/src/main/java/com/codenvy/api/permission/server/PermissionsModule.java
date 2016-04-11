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
package com.codenvy.api.permission.server;

import com.codenvy.api.permission.server.dao.CommonDomain;
import com.codenvy.api.permission.server.dao.CommonPermissionStorage;
import com.codenvy.api.permission.server.dao.PermissionsCodec;
import com.codenvy.api.permission.server.dao.PermissionsStorage;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

/**
 * @author Sergii Leschenko
 */
public class PermissionsModule extends AbstractModule {
    @Override
    protected void configure() {
        //Creates empty multibinder to avoid error during container starting
        Multibinder.newSetBinder(binder(), PermissionsDomain.class, CommonDomain.class);

        Multibinder<PermissionsStorage> storages = Multibinder.newSetBinder(binder(),
                                                                            PermissionsStorage.class);
        storages.addBinding().to(CommonPermissionStorage.class);

        final Multibinder<CodecProvider> binder = Multibinder.newSetBinder(binder(), CodecProvider.class);
        binder.addBinding().toInstance(new CodecProvider() {
            @Override
            public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
                if (clazz == Permissions.class) {
                    @SuppressWarnings("unchecked")
                    final Codec<T> codec = (Codec<T>)new PermissionsCodec(registry);
                    return codec;
                }
                return null;
            }
        });
    }
}
