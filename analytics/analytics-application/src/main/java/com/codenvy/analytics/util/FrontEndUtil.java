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
package com.codenvy.analytics.util;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.accounts.AbstractAccountMetric;
import com.codenvy.analytics.metrics.users.UsersProfilesList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.regex.Pattern;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;

/**
 * @author Dmytro Nochevnov
 */
public class FrontEndUtil {

    private static final Logger LOG = LoggerFactory.getLogger(FrontEndUtil.class);

    @NotNull
    private static Pattern adminPattern;

    static {
        try {
            Configurator configurator = new Configurator();
            String adminLoginRegexp = configurator.getString("analytics.admin_login_regexp");
            adminPattern = adminLoginRegexp == null ? Pattern.compile("") : Pattern.compile(adminLoginRegexp);
        } catch (IOException e) {
            adminPattern = Pattern.compile("");
            LOG.error(e.getMessage(), e);
        }
    }

    public static String getCurrentUserId(HttpServletRequest request) throws IOException {
        try {
            Context.Builder builder = new Context.Builder();
            builder.put(MetricFilter.ALIASES, request.getUserPrincipal().getName());

            Metric metric = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);
            ListValueData valueData = getAsList(metric, builder.build());
            if (valueData.size() == 0) {
                if (request.isUserInRole("user")) {
                    return AbstractAccountMetric.getCurrentUser().getId();
                } else {
                    return request.getUserPrincipal().getName();
                }
            } else {
                Map<String, ValueData> profile = treatAsMap(treatAsList(valueData).get(0));
                return profile.get(AbstractMetric.ID).getAsString();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return "";
        }
    }

    /**
     * Read name of logged user from userPrincipal and read his/him first and last names by using metric
     * "users_profiles_list" filtered by logged user name.
     *
     * @return "{user_first_name} {user_last_name}" String,
     * <br>- or "{logged user name}" if metric "users_profiles_list" is returning empty user_first_name and
     * user_last_name attributes,
     * <br>- or "{logged user name}" if there was impossible to read data from metric (exception happened in
     * time of reading data by metric).
     */
    public static String getFirstAndLastName(Principal userPrincipal) {
        String email = null;
        if (userPrincipal != null) {
            email = userPrincipal.getName();
        }

        Context.Builder builder = new Context.Builder();
        if (email != null) {
            builder.put(MetricFilter.ALIASES, email);
        }
        Context context = builder.build();

        ListValueData value;
        try {
            value = (ListValueData)MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST).getValue(context);
        } catch (IOException e) {
            // return user email if there was impossible to read data from metric
            return email;
        }

        ValueData firstName;
        ValueData lastName;

        if (value.size() > 0) {
            Map<String, ValueData> userProfile = ((MapValueData)value.getAll().get(0)).getAll();

            firstName = userProfile.get(UsersProfilesList.USER_FIRST_NAME);
            lastName = userProfile.get(UsersProfilesList.USER_LAST_NAME);

            String firstAndLastName = "";
            if (firstName != null && !firstName.getAsString().isEmpty()) {
                firstAndLastName += firstName.getAsString();
            }

            if (lastName != null && !lastName.getAsString().isEmpty()) {
                firstAndLastName += (firstAndLastName.isEmpty() ? "" : " ") + lastName.getAsString();
            }

            return firstAndLastName.isEmpty() ? email : firstAndLastName;
        } else {
            return email;
        }
    }
}