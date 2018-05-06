/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.modules.sql;

import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.modules.Module;
import io.github.flibio.utils.config.ConfigManager;
import org.slf4j.Logger;

import java.util.Optional;

public class SqlModule implements Module {

    private ConfigManager configManager = EconomyLite.getConfigManager();

    @Override
    public boolean initialize(Logger logger, Object plugin) {
        String hostname = getDetail("hostname");
        String port = getDetail("port");
        String database = getDetail("database");
        String username = getDetail("username");
        String password = getDetail("password");
        PlayerSqlService pService = new PlayerSqlService(hostname, port, database, username, password);
        VirtualSqlService vService = new VirtualSqlService(hostname, port, database, username, password);
        if (pService.isWorking() && vService.isWorking()) {
            EconomyLite.setPlayerService(pService);
            EconomyLite.setVirtualService(vService);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void initializeConfig() {
        configManager.setDefault(Boolean.class, false, "modules", "mysql", "enabled");
        configManager.setDefault(String.class, "hostname", "modules", "mysql", "hostname");
        configManager.setDefault(String.class, "3306", "modules", "mysql", "port");
        configManager.setDefault(String.class, "database", "modules", "mysql", "database");
        configManager.setDefault(String.class, "username", "modules", "mysql", "username");
        configManager.setDefault(String.class, "password", "modules", "mysql", "password");
    }

    @Override
    public String getName() {
        return "SQL";
    }

    @Override
    public boolean isEnabled() {
        return configManager.getValue(Boolean.class, false, "modules", "mysql", "enabled");
    }

    private String getDetail(String name) {
        return configManager.getValue("", String.class, "modules", "mysql", name);
    }

}
