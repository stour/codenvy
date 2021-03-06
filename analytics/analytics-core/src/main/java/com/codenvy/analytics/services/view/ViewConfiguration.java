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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
@XmlRootElement(name = "view")
public class ViewConfiguration {

    private String                     name;
    private String                     timeUnit;
    private String                     passedDaysCount;
    private boolean                    onDemand;
    private int                        columns;
    private List<SectionConfiguration> sections;

    public List<SectionConfiguration> getSections() {
        return sections;
    }

    public int getColumns() {
        return columns;
    }

    public String getName() {
        return name;
    }

    @XmlAttribute(name = "on-demand")
    public void setOnDemand(boolean onDemand) {
        this.onDemand = onDemand;
    }

    public boolean isOnDemand() {
        return onDemand;
    }

    @XmlAttribute(name = "columns")
    public void setColumns(int columns) {
        this.columns = columns;
    }

    @XmlAttribute(name = "name")
    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "section")
    public void setSections(List<SectionConfiguration> sections) {
        this.sections = sections;
    }

    @XmlAttribute(name = "time-unit")
    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String getTimeUnit() {
        return timeUnit;
    }
    
    @XmlAttribute(name = "passed-days-count")
    public void setPassedDaysCount(String passedDaysCount) {
        this.passedDaysCount = passedDaysCount;
    }
    
    public String getPassedDaysCount() {
        return passedDaysCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ViewConfiguration)) return false;

        ViewConfiguration that = (ViewConfiguration)o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
