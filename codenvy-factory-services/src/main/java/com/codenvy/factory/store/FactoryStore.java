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
package com.codenvy.factory.store;

import com.codenvy.factory.commons.AdvancedFactoryUrl;
import com.codenvy.factory.commons.FactoryUrlException;
import com.codenvy.factory.commons.Image;

import java.util.Map;

/** Interface for CRUD operations with factory data. */
public interface FactoryStore {
    /**
     * Save factory at storage.
     * @param factoryUrl - factory information
     * @param image - factory image
     * @return - copy of saved data
     * @throws FactoryUrlException
     */
    public SavedFactoryData saveFactory(AdvancedFactoryUrl factoryUrl, Image image) throws FactoryUrlException;

    /**
     * Retrieve factory data by its id
     * @param id - factory id
     * @return - data if factory exist and found, null otherwise
     * @throws FactoryUrlException
     */
    public SavedFactoryData getFactory(String id) throws FactoryUrlException;

    /**
     * Retrieve image data by its id
     * @param id - image id
     * @return - image if it exist in storage, null otherwise
     * @throws FactoryUrlException
     */
    public Image getImage(String id) throws FactoryUrlException;
}
