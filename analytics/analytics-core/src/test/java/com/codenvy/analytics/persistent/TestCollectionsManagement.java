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
package com.codenvy.analytics.persistent;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

/**
 * Testing ensuring and dropping indexes in collections. It is needed to take in account existing index by '_id' field.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestCollectionsManagement extends BaseTest {

    private static final String FILE            = BASE_DIR + "/resource";
    private static final String COLLECTION_NAME = "test_collection_";
    private static final String CONFIGURATION   = "<collections>" +
                                                  "   <collection name=\"test_collection_\">" +
                                                  "      <indexes>" +
                                                  "         <index name=\"index1\">" +
                                                  "            <field>date</field>" +
                                                  "            <field>field1</field>" +
                                                  "         </index>" +
                                                  "         <index name=\"index2\">" +
                                                  "            <field>field2</field>" +
                                                  "         </index>" +
                                                  "      </indexes>" +
                                                  "   </collection>" +
                                                  "</collections>";

    private DB db;

    @BeforeClass
    public void prepare() {
        db = mongoDataStorage.getDb();
    }

    @Test
    public void shouldCreateIndexesIfCollectionExists() throws Exception {
        String collectionSuffix = "2";

        createCollection(collectionSuffix);
        createIndexes(collectionSuffix);

        assertIndexesNumber(collectionSuffix, 3);
    }

    @Test
    public void shouldNotDropIndexesIfCollectionNotExists() throws Exception {
        String collectionSuffix = "3";

        dropIndexes(collectionSuffix);
        assertIndexesNumber(collectionSuffix, 0);
    }

    @Test
    public void shouldDropIndexesIfCollectionExists() throws Exception {
        String collectionSuffix = "4";

        createCollection(collectionSuffix);
        createIndexes(collectionSuffix);

        assertIndexesNumber(collectionSuffix, 3);

        dropIndexes(collectionSuffix);

        assertIndexesNumber(collectionSuffix, 1);
    }

    @Test
    public void shouldNotCreateIndexesIfIndexesAlreadyExist() throws Exception {
        String collectionSuffix = "5";

        createCollection(collectionSuffix);
        createIndexes(collectionSuffix);

        assertIndexesNumber(collectionSuffix, 3);

        createIndexes(collectionSuffix);

        assertIndexesNumber(collectionSuffix, 3);
    }

    @Test
    public void shouldThrowExceptionsIfIndexesNotExist() throws Exception {
        String collectionSuffix = "6";

        createCollection(collectionSuffix);
        createIndexes(collectionSuffix);

        assertIndexesNumber(collectionSuffix, 3);

        dropIndexes(collectionSuffix);

        assertIndexesNumber(collectionSuffix, 1);

        dropIndexes(collectionSuffix);

        assertIndexesNumber(collectionSuffix, 1);
    }

    @Test
    public void shouldRemoveDataForSpecificPeriod() throws Exception {
        String collectionSuffix = "7";

        assertRowsCount(collectionSuffix, 0);

        fillData(collectionSuffix, "20131101", 1);
        fillData(collectionSuffix, "20131102", 2);
        fillData(collectionSuffix, "20131103", 3);
        fillData(collectionSuffix, "20131104", 4);
        fillData(collectionSuffix, "20131105", 5);

        assertRowsCount(collectionSuffix, 15);

        removeData(collectionSuffix, "20131102", "20131102");
        assertRowsCount(collectionSuffix, 13);

        removeData(collectionSuffix, "20131103", "20131103");
        assertRowsCount(collectionSuffix, 10);

        removeData(collectionSuffix, "20131101", "20131101");
        assertRowsCount(collectionSuffix, 9);

        removeData(collectionSuffix, "20131104", "20131105");
        assertRowsCount(collectionSuffix, 0);
    }

    @Test
    public void testBackup() throws Exception {
        String collectionSuffix = "8";

        assertRowsCount(collectionSuffix, 0);

        fillData(collectionSuffix, "20131101", 10);
        assertRowsCount(collectionSuffix, 10);

        backup(collectionSuffix);
        assertRowsCount(collectionSuffix + "_backup", 10);

        backup(collectionSuffix);
        assertRowsCount(collectionSuffix + "_backup", 10);
    }

    private void backup(String collectionSuffix) throws Exception {
        CollectionsManagement collectionsManagement = initCollectionManagement(collectionSuffix);
        collectionsManagement.backup(COLLECTION_NAME + collectionSuffix);
    }

    private void removeData(String collectionSuffix, String fromDate, String toDate) throws Exception {
        CollectionsManagement collectionsManagement = initCollectionManagement(collectionSuffix);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, fromDate);
        builder.put(Parameters.TO_DATE, toDate);

        collectionsManagement.removeData(builder.build());
    }

    private void assertRowsCount(String collectionSuffix, int rowCount) {
        DBCollection dbCollection = db.getCollection(COLLECTION_NAME + collectionSuffix);
        assertEquals(rowCount, dbCollection.find().size());
    }

    private void fillData(String collectionSuffix, String date, int rowCount) throws Exception {
        Calendar calendar = Utils.parseDate(date);

        DBCollection dbCollection = db.getCollection(COLLECTION_NAME + collectionSuffix);
        for (int i = 0; i < rowCount; i++) {
            DBObject dbObject = new BasicDBObject();
            dbObject.put("_id", UUID.randomUUID().toString());
            dbObject.put(ReadBasedMetric.DATE, calendar.getTimeInMillis());

            dbCollection.save(dbObject);
        }
    }

    private void createCollection(String collectionSuffix) {
        DBCollection dbCollection = db.getCollection(COLLECTION_NAME + collectionSuffix);
        dbCollection.save(new BasicDBObject());
    }

    private void createIndexes(String collectionSuffix) throws Exception {
        CollectionsManagement collectionsManagement = initCollectionManagement(collectionSuffix);
        collectionsManagement.crateIndexes();
    }

    private void dropIndexes(String collectionSuffix) throws Exception {
        CollectionsManagement collectionsManagement = initCollectionManagement(collectionSuffix);
        collectionsManagement.dropIndexes();
    }

    private void assertIndexesNumber(String collectionSuffix, int indexNumber) {
        DBCollection collection = db.getCollection(COLLECTION_NAME + collectionSuffix);
        assertEquals(collection.getIndexInfo().size(), indexNumber);
    }

    private CollectionsManagement initCollectionManagement(final String collectionSuffix) throws Exception {
        XmlConfigurationManager confManager = mock(XmlConfigurationManager.class);
        when(confManager.loadConfiguration(any(Class.class), anyString())).
                                                                                  thenAnswer(new Answer() {
                                                                                      @Override
                                                                                      public Object answer(
                                                                                              InvocationOnMock
                                                                                                      invocation)
                                                                                              throws Throwable {
                                                                                          String configuration =
                                                                                                  CONFIGURATION.replace(
                                                                                                          COLLECTION_NAME,
                                                                                                          COLLECTION_NAME +
                                                                                                          collectionSuffix);
                                                                                          String file = FILE + "_" +
                                                                                                        collectionSuffix;

                                                                                          try (BufferedWriter out = new BufferedWriter(
                                                                                                  new FileWriter(
                                                                                                          file))) {
                                                                                              out.write(configuration);
                                                                                          }

                                                                                          XmlConfigurationManager
                                                                                                  manager =
                                                                                                  new XmlConfigurationManager();
                                                                                          return manager
                                                                                                  .loadConfiguration(
                                                                                                          CollectionsConfiguration.class,
                                                                                                          file);
                                                                                      }
                                                                                  });

        return new CollectionsManagement(mongoDataStorage, confManager);
    }
}
