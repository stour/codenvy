/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2013] Codenvy, S.A. 
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
package com.codenvy.factory.store.impl;

import com.codenvy.factory.commons.AdvancedFactoryUrl;
import com.codenvy.factory.commons.FactoryUrlException;
import com.codenvy.factory.commons.Image;
import com.codenvy.factory.store.FactoryStore;
import com.codenvy.factory.store.SavedFactoryData;

import java.util.Map;

public class InMemoryFactoryStore implements FactoryStore {
    @Override
    public SavedFactoryData saveFactory(AdvancedFactoryUrl factoryUrl, Image image) throws FactoryUrlException {
        return null;
    }

    @Override
    public SavedFactoryData getFactory(String id) throws FactoryUrlException {
        return null;
    }

    @Override
    public Image getImage(String id) throws FactoryUrlException {
        return null;
    }
}
