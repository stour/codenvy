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
package com.codenvy.analytics.metrics.users;

import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.InternalMetric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.PrecomputedDataMetric;
import com.codenvy.analytics.metrics.ReadBasedSummariziable;
import com.mongodb.DBObject;

import static com.codenvy.analytics.Utils.getFilterAsSet;
import static com.codenvy.analytics.Utils.isAnonymousUserExist;

/**
 * @author Alexander Reshetnyak
 */
@InternalMetric
public class UsersStatisticsListPrecomputed extends AbstractListValueResulted implements PrecomputedDataMetric, ReadBasedSummariziable {

    public UsersStatisticsListPrecomputed() {
        super(MetricType.USERS_STATISTICS_LIST_PRECOMPUTED);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Users' statistics data";
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.USERS_STATISTICS_PRECOMPUTED);
    }

    /** {@inheritDoc} */
    @Override
    public String[] getTrackedFields() {
        return new String[]{USER,
                            PROJECTS,
                            SESSIONS,
                            TIME,
                            BUILDS,
                            BUILD_TIME,
                            RUNS,
                            RUN_TIME,
                            DEBUGS,
                            DEBUG_TIME,
                            DEPLOYS,
                            FACTORIES,
                            INVITES,
                            LOGINS,
                            ALIASES,
                            USER_FIRST_NAME,
                            USER_LAST_NAME,
                            USER_COMPANY,
                            USER_JOB
        };
    }

    /** {@inheritDoc} */
    @Override
    public DBObject[] getSpecificSummarizedDBOperations(Context clauses) {
        ReadBasedSummariziable summariziable = (ReadBasedSummariziable)MetricFactory.getMetric(getBasedMetric());
        return summariziable.getSpecificSummarizedDBOperations(clauses);
    }

    /** {@inheritDoc} */
    @Override
    public Context getContextForBasedMetric() {
        Context.Builder builder = new Context.Builder();
        builder.put(MetricFilter.USER, Parameters.USER_TYPES.REGISTERED.toString());
        return builder.build();
    }

    /** {@inheritDoc} */
    @Override
    public MetricType getBasedMetric() {
        return MetricType.USERS_STATISTICS_LIST;
    }

    /** {@inheritDoc} */
    @Override
    public boolean canReadPrecomputedData(Context context) {
        String value = context.getAsString(MetricFilter.USER_ID);
        return value == null || !isAnonymousUserExist(getFilterAsSet(value));
    }
}
