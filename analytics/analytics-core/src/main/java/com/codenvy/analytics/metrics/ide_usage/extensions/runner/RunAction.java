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
package com.codenvy.analytics.metrics.ide_usage.extensions.runner;

import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ide_usage.AbstractIdeUsage;

/**
 * @author Dmytro Nochevnov
 */
public class RunAction extends AbstractIdeUsage {
    public static final String[] SOURCE = {
            "com.codenvy.ide.extension.runner.client.actions.RunAction",
            "org.eclipse.che.ide.ext.runner.client.actions.RunAction",
            "org.eclipse.che.ide.ext.runner.client.runneractions.impl.RunAction",
            "com.codenvy.ide.extension.runner.client.actions.CustomRunAction",
    };

    public RunAction() {
        super(MetricType.RUN_ACTION, SOURCE);
    }
}
