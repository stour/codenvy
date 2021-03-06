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

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.CalculatedMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Expandable;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricType;

import java.io.IOException;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeTotal extends CalculatedMetric implements Expandable {

    public ProductUsageTimeTotal() {
        super(MetricType.PRODUCT_USAGE_TIME_TOTAL,
              new MetricType[]{MetricType.PRODUCT_USAGE_TIME_BELOW_1_MIN,
                               MetricType.PRODUCT_USAGE_TIME_BETWEEN_1_AND_10_MIN,
                               MetricType.PRODUCT_USAGE_TIME_BETWEEN_10_AND_60_MIN,
                               MetricType.PRODUCT_USAGE_TIME_ABOVE_60_MIN});
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Context context) throws IOException {
        LongValueData value1 = ValueDataUtil.getAsLong(basedMetric[0], context);
        LongValueData value2 = ValueDataUtil.getAsLong(basedMetric[1], context);
        LongValueData value3 = ValueDataUtil.getAsLong(basedMetric[2], context);
        LongValueData value4 = ValueDataUtil.getAsLong(basedMetric[3], context);

        return new LongValueData(value1.getAsLong()
                                 + value2.getAsLong()
                                 + value3.getAsLong()
                                 + value4.getAsLong());
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The total time of all sessions in persistent workspaces";
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getExpandedValue(Context context) throws IOException {
        ValueData result = ListValueData.DEFAULT;

        for (Metric metric : basedMetric) {
            ValueData expandedValue = ((Expandable)metric).getExpandedValue(context);
            result = result.add(expandedValue);
        }

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public String getExpandedField() {
        return ((Expandable)basedMetric[0]).getExpandedField();
    }
}
