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
public class ProductUsageTimeBetween10And60Min extends AbstractProductUsageTime {

    public ProductUsageTimeBetween10And60Min() {
        super(MetricType.PRODUCT_USAGE_TIME_BETWEEN_10_AND_60_MIN, 10 * 60 * 1000, 60 * 60 * 1000, true, true);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The total time of all sessions in persistent workspaces with duration between 10 and 60 minutes " +
               "inclusively";
    }
}
