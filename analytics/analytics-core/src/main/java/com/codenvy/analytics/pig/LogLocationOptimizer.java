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
package com.codenvy.analytics.pig;


import com.codenvy.analytics.Utils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/** Log path optimization utility provides means to parse paths where logs are stored */
public class LogLocationOptimizer {

    public static String generatePaths(String baseDir, String fromDateString, String toDateString) throws ParseException,
                                                                                                  IllegalStateException, IOException {
        Calendar fromDate = Utils.parseDate(fromDateString);
        Calendar toDate = Utils.parseDate(toDateString);

        return doGeneratePath(baseDir, fromDate, toDate);
    }

    /**
     * If particular path is absent it will not be included in resulted set. Since Pig framework throw an exception when path does not
     * exist.
     */
    private static String doGeneratePath(String baseDir, Calendar fromDate, Calendar toDate) {
        final DateFormat dayF = new SimpleDateFormat("yyyy" + File.separator + "MM" + File.separator + "dd");
        final DateFormat monthF = new SimpleDateFormat("yyyy" + File.separator + "MM");

        if (fromDate.after(toDate)) {
            throw new IllegalStateException(Utils.formatDate(fromDate) + " is after " + Utils.formatDate(toDate));
        }

        StringBuilder builder = new StringBuilder();
        do {
            if (fromDate.get(Calendar.DAY_OF_MONTH) == 1 &&
                (fromDate.get(Calendar.MONTH) != toDate.get(Calendar.MONTH) ||
                toDate.get(Calendar.DAY_OF_MONTH) == toDate.getActualMaximum(Calendar.DAY_OF_MONTH))) {
                
                File file = new File(baseDir, monthF.format(fromDate.getTime()));
                if (file.exists()) {
                    builder.append(file.getPath());
                    builder.append(',');
                }

                fromDate.add(Calendar.MONTH, 1);

            } else {
                File file = new File(baseDir, dayF.format(fromDate.getTime()));
                if (file.exists()) {
                    builder.append(file.getPath());
                    builder.append(',');
                }

                fromDate.add(Calendar.DAY_OF_MONTH, 1);
            }
        } while (!fromDate.after(toDate));

        if (builder.length() != 0) {
            builder.deleteCharAt(builder.length() - 1);
        }

        return builder.toString();
    }
}
