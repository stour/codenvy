/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.metrics.sessions;

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Expandable;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.IOException;
import java.text.ParseException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractProductUsageUsers extends ReadBasedMetric implements Expandable {

    private final long    min;
    private final long    max;
    private final boolean includeMin;
    private final boolean includeMax;

    private String expandingField = USER;

    protected AbstractProductUsageUsers(String metricName,
                                        long min,
                                        long max,
                                        boolean includeMin,
                                        boolean includeMax) {
        super(metricName);
        this.min = min;
        this.max = max;
        this.includeMin = includeMin;
        this.includeMax = includeMax;
    }

    protected AbstractProductUsageUsers(MetricType metricType,
                                        long min,
                                        long max,
                                        boolean includeMin,
                                        boolean includeMax) {
        this(metricType.name(), min, max, includeMin, includeMax);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{VALUE};
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.PRODUCT_USAGE_SESSIONS_LIST);
    }

    @Override
    public DBObject[] getSpecificDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + USER);
        group.put("total", new BasicDBObject("$sum", "$" + TIME));

        DBObject range = new BasicDBObject();
        range.put(includeMin ? "$gte" : "$gt", min);
        range.put(includeMax ? "$lte" : "$lt", max);

        BasicDBObject count = new BasicDBObject();
        count.put("_id", null);
        count.put(VALUE, new BasicDBObject("$sum", 1));

        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$match", new BasicDBObject("total", range)),
                              new BasicDBObject("$group", count)};
    }

    @Override
    public DBObject getFilter(Context clauses) throws ParseException, IOException {
        if (!clauses.exists(MetricFilter.USER)) {
            Context.Builder builder = new Context.Builder(clauses);
            builder.put(MetricFilter.USER, Parameters.USER_TYPES.REGISTERED.name());

            clauses = builder.build();

        }

        return super.getFilter(clauses);
    }

    @Override
    public DBObject[] getSpecificExpandedDBOperations(Context clauses) {       
        DBObject group = new BasicDBObject();
        group.put(ID, "$" + expandingField);
        group.put("total", new BasicDBObject("$sum", "$" + TIME));

        DBObject range = new BasicDBObject();
        range.put(includeMin ? "$gte" : "$gt", min);
        range.put(includeMax ? "$lte" : "$lt", max);

        DBObject projection = new BasicDBObject(expandingField, "$_id");
        
        return new DBObject[]{new BasicDBObject("$group", group),
                              new BasicDBObject("$match", new BasicDBObject("total", range)),
                              new BasicDBObject("$project", projection),
                              };
    }
    
    @Override
    public String getExpandedValueField() {
        return expandingField;
    }
}
