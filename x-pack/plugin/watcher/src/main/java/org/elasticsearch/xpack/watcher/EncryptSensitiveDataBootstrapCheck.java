/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.watcher;

import org.elasticsearch.bootstrap.BootstrapCheck;
import org.elasticsearch.bootstrap.BootstrapContext;
import org.elasticsearch.env.Environment;
import org.elasticsearch.xpack.core.XPackField;
import org.elasticsearch.xpack.core.XPackPlugin;
import org.elasticsearch.xpack.core.watcher.WatcherField;

import java.nio.file.Files;
import java.nio.file.Path;

final class EncryptSensitiveDataBootstrapCheck implements BootstrapCheck {

    private final Environment environment;

    EncryptSensitiveDataBootstrapCheck(Environment environment) {
        this.environment = environment;
    }

    @Override
    public BootstrapCheckResult check(BootstrapContext context) {
        if (Watcher.ENCRYPT_SENSITIVE_DATA_SETTING.get(context.settings)
                && WatcherField.ENCRYPTION_KEY_SETTING.exists(context.settings) == false) {
            final Path systemKeyPath = XPackPlugin.resolveConfigFile(environment, "system_key").toAbsolutePath();
            final String message;
            if (Files.exists(systemKeyPath)) {
                message = "Encryption of sensitive data requires the key to be placed in the secure setting store. Run " +
                        "'bin/elasticsearch-keystore add-file " + WatcherField.ENCRYPTION_KEY_SETTING.getKey() + " " +
                        systemKeyPath +
                        "' to import the file.\nAfter importing, the system_key file should be removed from the " +
                        "filesystem.\nRepeat this on every node in the cluster.";
            } else {
                message = "Encryption of sensitive data requires a key to be placed in the secure setting store. First run the " +
                        "bin/elasticsearch-syskeygen tool to generate a key file.\nThen run 'bin/elasticsearch-keystore add-file " +
                        WatcherField.ENCRYPTION_KEY_SETTING.getKey() + " " +
                        systemKeyPath + "' to import the key into" +
                        " the secure setting store. Finally, remove the system_key file from the filesystem.\n" +
                        "Repeat this on every node in the cluster";
            }
            return BootstrapCheckResult.failure(message);
        } else {
            return BootstrapCheckResult.success();
        }
    }

    @Override
    public boolean alwaysEnforce() {
        return true;
    }
}
