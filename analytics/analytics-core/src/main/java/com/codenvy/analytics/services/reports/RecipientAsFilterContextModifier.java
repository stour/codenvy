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
package com.codenvy.analytics.services.reports;

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.services.configuration.ParameterConfiguration;

import java.util.List;

/**
 * @author Anatoliy Bazko
 */
public class RecipientAsFilterContextModifier extends AbstractContextModifier {

    public RecipientAsFilterContextModifier(List<ParameterConfiguration> parameters) {
        super(parameters);
    }

    @Override
    public Context update(Context context) {
        return context.cloneAndPut(MetricFilter.USER_ID, context.getAsString(Parameters.RECIPIENT));
    }
}
