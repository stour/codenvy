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
package com.codenvy.analytics.metrics.im;

import com.codenvy.analytics.metrics.AbstractLongValueResulted;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.InternalMetric;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.OmitFilters;

import java.io.IOException;

/** @author Anatoliy Bazko */
@InternalMetric
@OmitFilters({MetricFilter.WS_ID, MetricFilter.PERSISTENT_WS})
public class IMDownloadsInstallScript extends AbstractLongValueResulted {

    public IMDownloadsInstallScript() {
        super(MetricType.IM_DOWNLOADS_INSTALL_SCRIPT, USER);
    }

    /** {@inheritDoc} */
    @Override
    public Context applySpecificFilter(Context context) throws IOException {
        return context.cloneAndPut(MetricFilter.ARTIFACT, IMDownloadsList.ARTIFACT_INSTALL_CODENVY_SINGLE.replace("_", "-"));
    }

    /** {@inheritDoc} */
    @Override
    public String[] getTrackedFields() {
        return new String[]{IMDownloadsList.ARTIFACT_INSTALL_CODENVY_SINGLE};
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.IM_DOWNLOADS);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The number of downloaded Install Scripts";
    }
}
