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
package com.codenvy.cdec.artifacts;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Anatoliy Bazko
 */
public interface Artifact extends Comparable<Artifact> {

    /**
     * Installs artifact
     *
     * @param pathToBinaries
     */
    void install(Path pathToBinaries) throws IOException;

    /**
     * @return current deployed version of the component
     */
    String getCurrentVersion(String accessToken) throws IOException;

    /**
     * @return true if subscription has to be checked before artifact downloading
     */
    boolean isValidSubscriptionRequired();

    /**
     * @return the artifact name
     */
    String getName();

    /**
     * @return the priority of the artifact to install, update etc.
     */
    int getPriority();
}