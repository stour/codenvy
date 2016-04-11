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
package com.codenvy.ide.hosted.client.inject;

import com.codenvy.ide.hosted.client.notifier.BadConnectionNotifierView;
import com.codenvy.ide.hosted.client.notifier.BadConnectionNotifierViewImpl;
import com.codenvy.ide.hosted.client.informers.HostedEnvConnectionClosedInformer;
import com.codenvy.ide.hosted.client.login.PromptToLoginView;
import com.codenvy.ide.hosted.client.login.PromptToLoginViewImpl;
import com.codenvy.ide.hosted.client.workspace.HostedWorkspaceStoppedHandler;
import com.codenvy.ide.hosted.client.workspace.WorkspaceNotRunningView;
import com.codenvy.ide.hosted.client.workspace.WorkspaceNotRunningViewImpl;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.ConnectionClosedInformer;
import org.eclipse.che.ide.api.ProductInfoDataProvider;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;

/**
 * @author Vitaly Parfonov
 */
@ExtensionGinModule
public class HostedEnvironmentGinModule extends AbstractGinModule {
    @Override
    protected void configure() {
        bind(ProductInfoDataProvider.class).to(CodenvyProductInfoDataProvider.class).in(Singleton.class);
        bind(ConnectionClosedInformer.class).to(HostedEnvConnectionClosedInformer.class).in(javax.inject.Singleton.class);
        bind(PromptToLoginView.class).to(PromptToLoginViewImpl.class);
        bind(BadConnectionNotifierView.class).to(BadConnectionNotifierViewImpl.class);
        bind(WorkspaceNotRunningView.class).to(WorkspaceNotRunningViewImpl.class);
        bind(HostedWorkspaceStoppedHandler.class).asEagerSingleton();
    }
}
