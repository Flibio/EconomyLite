/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.impl;

import java.io.File;

import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.utils.sql.LocalSqlManager;

public class VirtualDataService extends VirtualServiceCommon {

    public VirtualDataService() {
        super(LocalSqlManager.createInstance(EconomyLite.getInstance(), "data", correctPath(EconomyLite.getInstance().getConfigDir().toString()))
                .get(), true);
    }

    private static String correctPath(String path) {
        if (new File(path).isAbsolute()) {
            return path;
        } else {
            return "./" + path;
        }
    }

}
