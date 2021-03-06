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
package com.codenvy.analytics.metrics.projects;


import com.codenvy.analytics.metrics.CumulativeMetric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TotalProjects extends CumulativeMetric {

    public TotalProjects() {
        super(MetricType.TOTAL_PROJECTS,
              MetricFactory.getMetric(MetricType.CREATED_PROJECTS),
              MetricFactory.getMetric(MetricType.DESTROYED_PROJECTS));
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The total number of projects";
    }

    /** {@inheritDoc} */
    @Override
    public String getExpandedField() {
        return PROJECT_ID;
    }
}
