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
package com.codenvy.analytics.services.view;

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
class DateRow extends AbstractRow {

    private static final String SECTION_NAME           = "section-name";
    private static final String DAY_FORMAT_PARAM       = "dayFormat";
    private static final String WEEK_FORMAT_PARAM      = "weekFormat";
    private static final String MONTH_FORMAT_PARAM     = "monthFormat";
    private static final String LIFE_TIME_FORMAT_PARAM = "lifeTimeFormat";

    private static final String DAY_FORMAT_DEFAULT       = "dd MMM";
    private static final String WEEK_FORMAT_DEFAULT      = "dd MMM";
    private static final String MONTH_FORMAT_DEFAULT     = "MMM yyyy";
    private static final String LIFE_TIME_FORMAT_DEFAULT = "dd MMM";

    private final Map<Parameters.TimeUnit, String> format = new HashMap<>(4);

    public DateRow(Map<String, String> parameters) {
        super(parameters);
        format.put(Parameters.TimeUnit.DAY,
                   parameters.containsKey(DAY_FORMAT_PARAM) ? parameters.get(DAY_FORMAT_PARAM)
                                                            : getDayFormat());
        format.put(Parameters.TimeUnit.WEEK,
                   parameters.containsKey(WEEK_FORMAT_PARAM) ? parameters.get(WEEK_FORMAT_PARAM)
                                                             : getWeekFormat());
        format.put(Parameters.TimeUnit.MONTH,
                   parameters.containsKey(MONTH_FORMAT_PARAM) ? parameters.get(MONTH_FORMAT_PARAM)
                                                              : MONTH_FORMAT_DEFAULT);
        format.put(Parameters.TimeUnit.LIFETIME,
                   parameters.containsKey(LIFE_TIME_FORMAT_PARAM) ? parameters.get(LIFE_TIME_FORMAT_PARAM)
                                                                  : LIFE_TIME_FORMAT_DEFAULT);
    }

    public String getDayFormat() {
        return DAY_FORMAT_DEFAULT;
    }

    public String getWeekFormat() {
        return WEEK_FORMAT_DEFAULT;
    }

    @Override
    public List<List<ValueData>> getData(Context initialContext, int iterationsCount) throws IOException {
        List<ValueData> result = new ArrayList<>(iterationsCount);

        try {
            DateFormat dateFormat = new SimpleDateFormat(format.get(initialContext.getTimeUnit()));

            result.add(new StringValueData(parameters.get(SECTION_NAME)));
            for (int i = 1; i < iterationsCount; i++) {
                Calendar toDate = initialContext.getAsDate(Parameters.TO_DATE);
                result.add(new StringValueData(dateFormat.format(toDate.getTime())));

                initialContext = Utils.prevDateInterval(new Context.Builder(initialContext));
            }
        } catch (ParseException e) {
            throw new IOException(e);
        }

        return Arrays.asList(result);
    }
}