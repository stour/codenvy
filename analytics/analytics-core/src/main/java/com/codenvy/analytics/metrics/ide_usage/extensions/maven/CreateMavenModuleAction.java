/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.analytics.metrics.ide_usage.extensions.maven;

import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ide_usage.AbstractIdeUsage;

/**
 * @author Dmytro Nochevnov
 */
public class CreateMavenModuleAction extends AbstractIdeUsage {
    public static final String[] SOURCE = {
            "com.codenvy.ide.extension.maven.client.actions.CreateMavenModuleAction",
            "org.eclipse.che.ide.extension.maven.client.actions.CreateMavenModuleAction"
    };

    public CreateMavenModuleAction() {
        super(MetricType.CREATE_MAVEN_MODULE_ACTION, SOURCE);
    }
}
