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
package com.codenvy.analytics.metrics.workspaces;

import com.codenvy.analytics.metrics.AbstractSetValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class CreatedWorkspacesSet extends AbstractSetValueResulted {

    public CreatedWorkspacesSet() {
        super(MetricType.CREATED_WORKSPACES_SET, WS);
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.CREATED_WORKSPACES);
    }

    /** {@inheritDoc} */
    @Override
    public Context applySpecificFilter(Context clauses) {
        if (!clauses.exists(MetricFilter.WS)) {
            Context.Builder builder = new Context.Builder(clauses);
            builder.put(MetricFilter.WS, Parameters.WS_TYPES.PERSISTENT.name());
            return builder.build();
        }

        return clauses;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Created persistent workspaces";
    }
}
