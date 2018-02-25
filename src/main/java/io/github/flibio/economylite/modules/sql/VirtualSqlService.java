/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.modules.sql;

import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.impl.VirtualServiceCommon;
import io.github.flibio.utils.sql.RemoteSqlManager;

public class VirtualSqlService extends VirtualServiceCommon {

    public VirtualSqlService(String hostname, String port, String database, String username, String password) {
        super(RemoteSqlManager.createInstance(EconomyLite.getInstance(), hostname, port, database, username, password).get(), false);
    }

}
