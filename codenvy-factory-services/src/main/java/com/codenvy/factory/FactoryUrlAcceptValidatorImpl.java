/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.factory;

import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.factory.FactoryUrlAcceptValidator;
import com.codenvy.api.factory.dto.Factory;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.user.server.dao.UserProfileDao;

import javax.inject.Inject;

/**
 * Factory URL accept stage validator.
 */
public class FactoryUrlAcceptValidatorImpl extends FactoryUrlBaseValidator implements FactoryUrlAcceptValidator {

    @Inject
    public FactoryUrlAcceptValidatorImpl(AccountDao accountDao, UserDao userDao, UserProfileDao profileDao) {
        super(accountDao,userDao,profileDao);
    }

    @Override
    public void validateOnAccept(Factory factory, boolean encoded) throws ApiException {
        if (!encoded) {
            validateVcs(factory);
            validateProjectName(factory);
        }
        validateTrackedFactoryAndParams(factory);
    }
}