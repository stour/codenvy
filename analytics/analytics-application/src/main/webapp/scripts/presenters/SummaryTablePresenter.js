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
if (typeof analytics === "undefined") {
    analytics = {};
}

analytics.presenter = analytics.presenter || {};

analytics.presenter.SummaryTablePresenter = function SummaryTablePresenter() {};

analytics.presenter.SummaryTablePresenter.prototype = new Presenter();

analytics.presenter.SummaryTablePresenter.prototype.load = function() {
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;
    
    var viewParams = view.getParams();
    
    var modelParams = presenter.getModelParams(viewParams);    
    model.setParams(modelParams);
    
    presenter.displayEmptyWidget("Summary");
    
    // get Number of entries
    model.pushDoneFunction(function (data) {
        model.popDoneFunction();

        var numberOfEntries = data;
        
        presenter.obtainSummaryData(model, view, presenter, numberOfEntries);
    });

    var modelMetricName = analytics.configuration.getProperty(presenter.widgetName, "modelMetricName");
    model.getMetricValue(modelMetricName);
};

analytics.presenter.SummaryTablePresenter.prototype.obtainSummaryData = function (model, view, presenter, numberOfEntries) {
    var viewParams = view.getParams();
    var modelParams = presenter.getModelParams(viewParams);
    
    model.pushDoneFunction(function(table) {
        // add number of entries into the table
        if (table.rows.length != 0) {
            table.columns.unshift("entries");
            table.rows[0].unshift("<div class='bold'>" + numberOfEntries + "</div>");
        } else {
            table.columns = ["entries"];
            table.rows[0] =  ["<div class='bold'>" + numberOfEntries + "</div>"];
        }
        
        if (typeof modelParams[presenter.EXPANDED_METRIC_NAME_PARAMETER] == "undefined") {  // don't expand summary metric secondary
            // add links to drill down page
            table = presenter.linkTableValuesWithDrillDownPage(presenter.widgetName, table, modelParams);
        }
    
        var tableId = presenter.widgetName + "_table";
        
        view.print("<div class='body'>");
        view.printTable(table, false, tableId, "text-aligned-center");
        view.print("</div>");
        
        view.loadTableHandlers(false, {}, tableId);
        
        // finish loading widget
        presenter.needLoader = false;
    });
    
    model.setParams(modelParams);

    var modelViewName = analytics.configuration.getProperty(presenter.widgetName, "modelViewName");
    model.getSummarizedMetricValue(modelViewName);
}