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
/*
/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.api;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.Injector;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.persistent.JdbcDataPersisterFactory;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;
import com.codenvy.analytics.services.pig.PigRunner;
import com.codenvy.analytics.services.view.CSVFileHolder;
import com.codenvy.analytics.services.view.CSVReportPersister;
import com.codenvy.analytics.services.view.DisplayConfiguration;
import com.codenvy.analytics.services.view.ViewBuilder;
import com.codenvy.analytics.services.view.ViewData;

import org.apache.commons.io.IOUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/** @author Dmytro Nochevnov */
public class TestViewApi extends BaseTest {

    private ViewBuilder viewBuilder;
    private PigRunner   pigRunner;

    private static final String TEST_RESOURCE_DIR = BASE_DIR + "/test-classes/"
                                                    + TestViewApi.class.getSimpleName();

    private static final String VIEW_CONFIGURATION_FILE = TEST_RESOURCE_DIR + "/view.xml";
    private static final String LOG_FILE                = TestViewApi.class.getSimpleName() + "/messages";
    private static final String EXPECTED_JSON_FILE      = TestViewApi.class.getSimpleName() + "/expected.json";
    private static final String EXPECTED_CSV_FILE       = TestViewApi.class.getSimpleName() + "/expected.csv";

    @BeforeClass
    public void prepare() throws Exception {
        pigRunner = getPigRunner();
        viewBuilder = getViewBuilder(VIEW_CONFIGURATION_FILE);
        runScript();
    }

    @Test
    private void testTransformInJsonFormat() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.putDefaultValue(Parameters.FROM_DATE);
        builder.putDefaultValue(Parameters.TO_DATE);
        builder.putDefaultValue(Parameters.TIME_UNIT);

        viewBuilder.computeDisplayData(builder.build());

        ArgumentCaptor<String> viewId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ViewData> viewData = ArgumentCaptor.forClass(ViewData.class);
        ArgumentCaptor<Context> context = ArgumentCaptor.forClass(Context.class);
        verify(viewBuilder, atLeastOnce()).retainViewData(viewId.capture(), viewData.capture(), context.capture());

        String response = new View(viewBuilder, new CSVFileHolder(configurator), utils)
                .transformToJson(viewData.getValue());
        String expectedResponse = getResourse(EXPECTED_JSON_FILE, "18 Mar", getToday());

        expectedResponse = expectedResponse.substring(0, expectedResponse.length() - 1); // remove ended "\n"

        assertEquals(response, expectedResponse);
    }


    @Test
    private void testTransformInCsvFormat() throws Exception {
        Context.Builder builder = new Context.Builder();
        builder.putDefaultValue(Parameters.FROM_DATE);
        builder.putDefaultValue(Parameters.TO_DATE);
        builder.putDefaultValue(Parameters.TIME_UNIT);

        viewBuilder.computeDisplayData(builder.build());

        ArgumentCaptor<String> viewId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ViewData> viewData = ArgumentCaptor.forClass(ViewData.class);
        ArgumentCaptor<Context> context = ArgumentCaptor.forClass(Context.class);
        verify(viewBuilder, atLeastOnce()).retainViewData(viewId.capture(), viewData.capture(), context.capture());

        ByteArrayOutputStream response = new ByteArrayOutputStream();
        new View(viewBuilder, new CSVFileHolder(configurator), utils).transformToCsv(viewData.getValue(), response);

        String expectedResponse = getResourse(EXPECTED_CSV_FILE, "18 Mar", getToday());

        assertEquals(new String(response.toByteArray()), expectedResponse);
    }


    private ViewBuilder getViewBuilder(final String viewConfigurationFile) throws IOException, URISyntaxException {
        XmlConfigurationManager configurationManager = mock(XmlConfigurationManager.class);

        when(configurationManager.loadConfiguration(any(Class.class), anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                XmlConfigurationManager manager = new XmlConfigurationManager();
                return manager.loadConfiguration(DisplayConfiguration.class, viewConfigurationFile);
            }
        });

        Configurator viewConfigurator = spy(Injector.getInstance(Configurator.class));

        doReturn(new String[]{viewConfigurationFile}).when(viewConfigurator).getArray(anyString());

        return spy(new ViewBuilder(Injector.getInstance(JdbcDataPersisterFactory.class),
                                   Injector.getInstance(CSVReportPersister.class),
                                   configurationManager,
                                   viewConfigurator));
    }

    private PigRunner getPigRunner() {
        return Injector.getInstance(PigRunner.class);
    }

    private void runScript() throws Exception {
        Context context = Utils.initializeContext(Parameters.TimeUnit.DAY);

        context =
                context.cloneAndPut(Parameters.LOG,
                                    getResourceAsBytes(LOG_FILE, "2013-11-24", getYesterday())
                                            .getAbsolutePath());
        pigRunner.forceExecute(context);
    }

    /**
     * Get yesterday date in format "yyyy-MM-dd"
     */
    private String getYesterday() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        return df.format(calendar.getTime());
    }

    /**
     * Get today in format "02 Mar"
     */
    private String getToday() {
        DateFormat df = new SimpleDateFormat("dd MMM");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        return df.format(calendar.getTime());
    }

    private File getResourceAsBytes(String filePath, String originalDate, String newDate) throws Exception {
        File initialFile = new File(getClass().getClassLoader().getResource(filePath).getFile());

        try (InputStream in = new BufferedInputStream(new FileInputStream(initialFile))) {
            String fixedFilePath = initialFile.getPath() + "_fixed";
            File fixedLog = new File(fixedFilePath);
            fixedLog.createNewFile();

            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(fixedLog))) {
                String resourceAsString = new String(IOUtils.toByteArray(in), "UTF-8");
                resourceAsString = resourceAsString.replace(originalDate, newDate);

                byte[] buf = resourceAsString.getBytes("UTF-8");
                IOUtils.copy(new ByteArrayInputStream(buf), out);

                return fixedLog;
            } finally {
                in.close();
            }
        }
    }

    private String getResourse(String filePath, String originalDate, String newDate) throws Exception {
        File file = getResourceAsBytes(filePath, originalDate, newDate);

        String fileContent = "";

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                fileContent += line + "\n";
            }
        }

        return fileContent;
    }

}
