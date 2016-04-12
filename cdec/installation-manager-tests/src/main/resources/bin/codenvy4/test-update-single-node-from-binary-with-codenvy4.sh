#!/bin/bash
#
# CODENVY CONFIDENTIAL
# ________________
#
# [2012] - [2015] Codenvy, S.A.
# All Rights Reserved.
# NOTICE: All information contained herein is, and remains
# the property of Codenvy S.A. and its suppliers,
# if any. The intellectual and technical concepts contained
# herein are proprietary to Codenvy S.A.
# and its suppliers and may be covered by U.S. and Foreign Patents,
# patents in process, and are protected by trade secret or copyright law.
# Dissemination of this information or reproduction of this material
# is strictly forbidden unless prior written permission is obtained
# from Codenvy S.A..
#

. ./lib.sh

printAndLog "TEST CASE: Update single-node Codenvy from binary with Codenvy 4"
vagrantUp ${SINGLE_NODE_VAGRANT_FILE}

installCodenvy ${PREV_CODENVY4_VERSION}
validateInstalledCodenvyVersion ${PREV_CODENVY4_VERSION}
authWithoutRealmAndServerDns "admin" "password"

executeIMCommand "im-download" "codenvy" "${LATEST_CODENVY4_VERSION}"

# put correct config into binaries
BINARIES="/home/vagrant/codenvy-im-data/updates/codenvy/${LATEST_CODENVY4_VERSION}/codenvy-${LATEST_CODENVY4_VERSION}.zip"
executeSshCommand "rm -rf /tmp/codenvy"
executeSshCommand "unzip ${BINARIES} -d /tmp/codenvy"

executeSshCommand "cat /tmp/codenvy/manifests/nodes/codenvy/codenvy.pp | grep '\$version\\s=' | sed 's/\\s*\$version\\s*=\\s*\"\\(.*\\)\"/\\1/'"
LATEST_PUPPET_VERSION=${OUTPUT}

executeSshCommand "cp /etc/puppet/manifests/nodes/codenvy/codenvy.pp /tmp/codenvy/manifests/nodes/codenvy/codenvy.pp"
executeSshCommand "sed -i s/${PREV_CODENVY4_VERSION}/${LATEST_PUPPET_VERSION}/g /tmp/codenvy/manifests/nodes/codenvy/codenvy.pp"
executeSshCommand "sudo yum install zip -y -q"
executeSshCommand "cd /tmp/codenvy && zip -r /tmp/codenvy.zip ."

# install from local folder
executeIMCommand "im-install" "--binaries=/tmp/codenvy.zip" "codenvy" "${LATEST_CODENVY4_VERSION}"
validateInstalledCodenvyVersion ${LATEST_CODENVY4_VERSION}
authWithoutRealmAndServerDns "admin" "password"

printAndLog "RESULT: PASSED"
vagrantDestroy
