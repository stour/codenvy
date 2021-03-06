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

import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TitleRow extends AbstractRow {

    private static final String TITLES = "titles";
    private final String[] titles;

    public TitleRow(Map<String, String> parameters) {
        super(parameters);
        titles = parameters.get(TITLES).split(",");
    }

    @Override
    public List<List<ValueData>> getData(Context initialContext, int iterationsCount) throws IOException {
        List<ValueData> result = new ArrayList<>(titles.length);

        for (int i = 0; i < titles.length; i++) {
            result.add(new StringValueData(titles[i]));
        }

        return Arrays.asList(result);
    }
}
