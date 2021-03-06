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
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.Utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public enum Parameters {
    SORT,

    PAGE,
    PER_PAGE,

    USER_PRINCIPAL,
    USER_PRINCIPAL_ROLE,

    LOG,
    PARAM,
    EVENT,

    STORAGE_URL,
    STORAGE_USER,
    STORAGE_PASSWORD,
    STORAGE_TABLE,
    STORAGE_TABLE_USERS_STATISTICS,
    STORAGE_TABLE_USERS_PROFILES {
        @Override
        public String getDefaultValue() {
            return MetricType.USERS_PROFILES_LIST.toString().toLowerCase();
        }
    },
    STORAGE_TABLE_PRODUCT_USAGE_SESSIONS,
    STORAGE_TABLE_PRODUCT_USAGE_SESSIONS_FAILS,
    STORAGE_TABLE_ACCEPTED_FACTORIES,

    REPORT_DATE,
    RECIPIENT,
    VIEW,
    REPORT_ROWS,
    EXPANDED_METRIC_NAME,
    TIME_UNIT {
        @Override
        public void validate(String value, Context context) throws IllegalArgumentException {
            TimeUnit.valueOf(value.toUpperCase());
        }

        @Override
        public String getDefaultValue() {
            return TimeUnit.LIFETIME.name();
        }
    },

    PASSED_DAYS_COUNT {
        @Override
        public void validate(String value, Context context) throws IllegalArgumentException {
            PassedDaysCount.valueOf(value.toUpperCase());
        }

        @Override
        public String getDefaultValue() {
            return PassedDaysCount.BY_LIFETIME.name();
        }
    },



    TIME_INTERVAL,
    DATA_COMPUTATION_PROCESS,
    FROM_DATE {
        @Override
        public String getDefaultValue() {
            return "20130101";
        }

        @Override
        public void validate(String value, Context context) throws IllegalArgumentException {
            try {
                Calendar fromDate = Utils.parseDate(value);
                Calendar toDate = context.getAsDate(TO_DATE);
                Calendar minDate = Utils.parseDate(getDefaultValue());

                if (fromDate.before(minDate)) {
                    throw new IllegalArgumentException("The illegal FROM_DATE parameter value '"
                                                       + Utils.formatDate(fromDate)
                                                       + "' The lowest allowed date is '"
                                                       + Utils.formatDate(minDate)
                                                       + "'");
                } else if (!fromDate.equals(toDate)) {
                    throw new IllegalArgumentException("The illegal TO_DATE parameter value: '"
                                                       + Utils.formatDate(toDate)
                                                       + "'. Should be the same as FROM_DATE parameter value: '"
                                                       + Utils.formatDate(fromDate)
                                                       + "'");
                }
            } catch (ParseException e) {
                throw new IllegalArgumentException("FROM_DATE parameter has illegal format '" + value
                                                   + "' The only supported format is '" + PARAM_DATE_FORMAT + "'");
            }
        }
    },
    TO_DATE {
        @Override
        public String getDefaultValue() {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -1);

            DateFormat df = new SimpleDateFormat(Parameters.PARAM_DATE_FORMAT);
            return df.format(calendar.getTime());
        }

        @Override
        public void validate(String value, Context context) throws IllegalArgumentException {
            try {
                Calendar toDate = Utils.parseDate(value);
                Calendar maxDate = Utils.parseDate(getDefaultValue());
                Calendar fromDate = context.getAsDate(FROM_DATE);

                if (toDate.after(maxDate)) {
                    throw new IllegalArgumentException("The illegal TO_DATE parameter value: '"
                                                       + Utils.formatDate(toDate)
                                                       + "' The highest allowed date is '"
                                                       + Utils.formatDate(maxDate)
                                                       + "'");

                }

                if (fromDate.after(toDate)) {
                    throw new IllegalArgumentException("The illegal TO_DATE parameter value: '"
                                                       + Utils.formatDate(toDate)
                                                       + "'. Should be higher than FROM_DATE parameter value: '"
                                                       + Utils.formatDate(fromDate)
                                                       + "'");
                } else if (!fromDate.equals(toDate)) {
                    throw new IllegalArgumentException("The illegal TO_DATE parameter value: '"
                                                       + Utils.formatDate(toDate)
                                                       + "'. Should be the same as FROM_DATE parameter value: '"
                                                       + Utils.formatDate(fromDate)
                                                       + "'");
                }
            } catch (ParseException e) {
                throw new IllegalArgumentException("TO_DATE parameter has illegal format '" + value
                                                   + "'. The only supported format is 'yyyyMMdd'");
            }
        }
    },
    // TODO
    IS_CUSTOM_DATE_RANGE,  // existence of this parameter indicates that user set date range through URL parameters; could contain any String value.

    USER {
        @Override
        public void validate(String value, Context context) throws IllegalArgumentException {
            USER_TYPES.valueOf(value);
        }

        @Override
        public String getDefaultValue() {
            return USER_TYPES.ANY.toString();
        }
    },
    WS {
        @Override
        public void validate(String value, Context context) throws IllegalArgumentException {
            WS_TYPES.valueOf(value);
        }

        @Override
        public String getDefaultValue() {
            return WS_TYPES.ANY.toString();
        }

    },
    ORIGINAL_USER,
    ORIGINAL_WS,

    IS_CSV_DATA;

    /** @return the default value for given parameter. */
    public String getDefaultValue() {
        throw new UnsupportedOperationException("Method is not supported");
    }

    /** Validates the value of parameter. Throws {@link IllegalArgumentException} if something wrong. */
    public void validate(String value, Context context) throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(this.name() + " parameter is null or empty");
        }
    }

    /** The date format is used in scripts. */
    public static final String PARAM_DATE_FORMAT = "yyyyMMdd";

    public enum WS_TYPES {
        ANY, PERSISTENT, TEMPORARY
    }

    public enum USER_TYPES {
        ANY, REGISTERED, ANONYMOUS
    }

    public static enum TimeUnit {
        DAY,
        WEEK,
        MONTH,
        LIFETIME,
    }

    public static enum PassedDaysCount {
        /** for top metrics and engagement levels reports */
        BY_1_DAY("by_1_day", 1),
        BY_7_DAYS("by_7_days", 7),
        BY_30_DAYS("by_30_days", 30),
        BY_60_DAYS("by_60_days", 60),
        BY_90_DAYS("by_90_days", 90),
        BY_365_DAYS("by_365_days", 365),
        BY_LIFETIME("by_lifetime", -1);

        private String fieldName;
        private int dayCount;

        PassedDaysCount(String name, int dayCount) {
            this.fieldName = name;
            this.dayCount = dayCount;
        }

        public int getDayCount() {
            return dayCount;
        }

        public String getFieldName() {
            return fieldName;
        }
    }
}
