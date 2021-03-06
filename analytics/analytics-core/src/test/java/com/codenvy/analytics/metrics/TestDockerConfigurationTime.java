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

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Reshetnyak
 */
public class TestDockerConfigurationTime extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid1", "ws1", "user1@gmail.com")
                                .withDate("2013-01-01").withTime("19:00:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid2", "ws2", "user2@gmail.com")
                                .withDate("2013-01-01").withTime("19:00:00").build());

        // user1@gmail.com 6m session
        events.add(Event.Builder.createConfigureDockerStartedEvent("user1@gmail.com", "ws1", "").withDate(
                "2013-01-01").withTime("19:00:00").build());
        events.add(Event.Builder.createConfigureDockerFinishedEvent("user1@gmail.com", "ws1", "").withDate(
                "2013-01-01").withTime("19:06:00").build());

        // user2@gmail.com 2m session
        events.add(Event.Builder.createConfigureDockerStartedEvent("user2@gmail.com", "ws2", "id1").withDate(
                "2013-01-01").withTime("19:08:00").build());
        events.add(Event.Builder.createConfigureDockerFinishedEvent("user2@gmail.com", "ws2", "id1").withDate(
                "2013-01-01").withTime("19:10:00").build());

        // user1@gmail.com 1m session
        events.add(Event.Builder.createConfigureDockerStartedEvent("user1@gmail.com", "ws1", "id2").withDate(
                "2013-01-01").withTime("19:11:00").build());
        events.add(Event.Builder.createConfigureDockerFinishedEvent("user1@gmail.com", "ws1", "id2").withDate(
                "2013-01-01").withTime("19:12:00").build());

        // corrupted session events, 'run-started' event is absent
        events.add(Event.Builder.createConfigureDockerFinishedEvent("user4@gmail.com", "ws1", "").withDate(
                "2013-01-01").withTime("19:13:00").build());

        // corrupted session events, 'run-finished' event is absent
        events.add(Event.Builder.createConfigureDockerStartedEvent("user1@gmail.com", "ws1", "").withDate(
                "2013-01-01").withTime("19:07:00").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.TIME_SPENT_IN_ACTION, MetricType.DOCKER_CONFIGURATION_TIME).getParamsAsMap());
        pigServer.execute(ScriptType.TIME_SPENT_IN_ACTION, builder.build());
    }

    @Test
    public void testDateFilter() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");

        Metric metric = MetricFactory.getMetric(MetricType.DOCKER_CONFIGURATION_TIME);
        Assert.assertEquals(metric.getValue(builder.build()), new LongValueData(540000));
    }
}
