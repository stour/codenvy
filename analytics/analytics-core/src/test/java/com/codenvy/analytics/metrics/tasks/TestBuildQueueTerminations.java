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
package com.codenvy.analytics.metrics.tasks;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author Alexander Reshetnyak
 */
public class TestBuildQueueTerminations extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(Event.Builder.createUserCreatedEvent("uid1", "user1@gmail.com", "user1@gmail.com").withDate("2013-02-10").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserCreatedEvent("uid2", "user2@gmail.com", "user2@gmail.com").withDate("2013-02-10").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserCreatedEvent("uid3", "user3@gmail.com", "user3@gmail.com").withDate("2013-02-10").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserCreatedEvent("uid5", "user5@gmail.com", "user5@gmail.com").withDate("2013-02-10").withTime("10:00:00,000").build());

        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid1", "ws1", "user1@gmail.com").withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid2", "ws2", "user2@gmail.com").withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid3", "ws3", "user3@gmail.com").withDate("2013-02-10").withTime("10:00:00").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid5", "ws5", "user5@gmail.com").withDate("2013-02-10").withTime("10:00:00").build());

        events.add(
                Event.Builder.buildQueueTerminatedEvent("user1@gmail.com", "ws1", "project1", "type1", "id1").withDate("2013-02-10").withTime("10:00:00")
                             .build());
        events.add(
                Event.Builder.buildQueueTerminatedEvent("user2@gmail.com", "ws2", "project2", "type2", "id2").withDate("2013-02-10").withTime("10:01:00")
                             .build());
        events.add(
                Event.Builder.buildQueueTerminatedEvent("user3@gmail.com", "ws3", "project3", "type3", "id3").withDate("2013-02-10").withTime("10:02:00")
                             .build());
        events.add(
                Event.Builder.buildQueueTerminatedEvent("user5@gmail.com", "ws5", "project5", "type5", "id5").withDate("2013-02-11").withTime("10:04:00")
                             .build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.BUILD_QUEUE_TERMINATIONS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());
    }

    @Test
    public void testUserEvents() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");

        Metric metric = MetricFactory.getMetric(MetricType.BUILD_QUEUE_TERMINATIONS);
        LongValueData lvd = (LongValueData)metric.getValue(builder.build());

        assertEquals(lvd.getAsLong(), 3);
    }
}
