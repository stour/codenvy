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
package com.codenvy.analytics.metrics.sessions;

import com.codenvy.analytics.metrics.MetricType;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageSessionsBelow1Min extends AbstractProductUsage {

    public ProductUsageSessionsBelow1Min() {
        super(MetricType.PRODUCT_USAGE_SESSIONS_BELOW_1_MIN, 0, 1 * 60 * 1000, true, true);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The number of all sessions in persistent workspaces with duration less or equal to 1 minute";
    }
}
