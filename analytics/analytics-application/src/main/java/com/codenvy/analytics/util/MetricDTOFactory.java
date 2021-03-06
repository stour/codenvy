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

import com.codenvy.analytics.datamodel.ValueDataFactory;
import com.codenvy.analytics.metrics.Metric;

import org.eclipse.che.api.analytics.AnalyticsService;
import org.eclipse.che.api.analytics.Constants;
import org.eclipse.che.api.analytics.shared.dto.MetricInfoDTO;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.server.DtoFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to create {@link org.eclipse.che.api.analytics.shared.dto.MetricInfoDTO} instances and to update its values
 *
 * @author Dmitry Kuleshov
 * @author Anatoliy Bazko
 */
public class MetricDTOFactory {

    private MetricDTOFactory() {
    }

    public static MetricInfoDTO createMetricDTO(Metric metric, String metricName, UriInfo uriInfo) {
        MetricInfoDTO metricInfoDTO = DtoFactory.getInstance().createDto(MetricInfoDTO.class);
        metricInfoDTO.setName(metricName);
        metricInfoDTO.setDescription(metric.getDescription());
        try {
            metricInfoDTO.setType(ValueDataFactory.createDefaultValue(metric.getValueDataClass()).getType());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        metricInfoDTO.setLinks(getLinks(metricName, uriInfo));
        return metricInfoDTO;
    }

    public static List<Link> getLinks(String metricName, UriInfo uriInfo) {
        final UriBuilder servicePathBuilder = uriInfo.getBaseUriBuilder();
        List<Link> links = new ArrayList<>();

        final Link statusLink = DtoFactory.getInstance().createDto(Link.class);
        statusLink.setRel(Constants.LINK_REL_GET_METRIC_VALUE);
        statusLink.setHref(servicePathBuilder
                                   .clone()
                                   .path("analytics-private")
                                   .path(getMethod("getValue"))
                                   .build(metricName, "name")
                                   .toString());
        statusLink.setMethod("GET");
        statusLink.setProduces(MediaType.APPLICATION_JSON);
        links.add(statusLink);
        return links;
    }

    public static Method getMethod(String name) {
        for (Method analyticsMethod : AnalyticsService.class.getMethods()) {
            if (analyticsMethod.getName().equals(name)) {
                return analyticsMethod;
            }
        }

        throw new RuntimeException("No '" + name + "' method found in " + AnalyticsService.class + "class");
    }
}
