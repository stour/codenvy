/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.cdec.im.cli.preferences;

import com.codenvy.cli.preferences.Preferences;
import com.codenvy.cli.security.RemoteCredentials;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** @author Dmytro Nochevnov */
public class PreferencesStorage {
    private final Preferences        globalPreferences;
    private final String             remote;

    public PreferencesStorage(Preferences globalPreferences, String remote) {
        this.globalPreferences = globalPreferences;
        this.remote = remote;
    }

    @Nullable
    public String getAccountId() {        
        SubscriptionPreferences accountDescription = readPreference(SubscriptionPreferences.class);
        if (accountDescription == null || accountDescription.getAccountId() == null) {
            throw new IllegalStateException("ID of Codenvy account which is used for subscription is needed.");
        }

        return accountDescription.getAccountId();
    }

    public void setAccountId(String accountId) {
        SubscriptionPreferences accountDescription = new SubscriptionPreferences();
        accountDescription.setAccountId(accountId);
        
        writePreference(accountDescription);
    }
    
    @Nonnull
    public String getAuthToken() throws IllegalStateException {
        RemoteCredentials credentials = readPreference(RemoteCredentials.class);
        return credentials.getToken(); // TODO outdated after 3h ?
    }

    private <T> T readPreference(Class<T> clazz) { 
        return globalPreferences.path("remotes").get(remote, clazz);
    }
    
    private <T> void writePreference(T preference) {
        globalPreferences.path("remotes").merge(remote, preference);
    }
}