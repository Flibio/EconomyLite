/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.modules.sql;

import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.economylite.modules.Module;
import io.github.flibio.utils.file.FileManager;
import org.slf4j.Logger;

import java.util.Optional;

public class SqlModule implements Module {

    private FileManager configManager = EconomyLite.getFileManager();

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
        configManager.setDefault("config.conf", "modules.mysql.enabled", Boolean.class, false);
        configManager.setDefault("config.conf", "modules.mysql.hostname", String.class, "hostname");
        configManager.setDefault("config.conf", "modules.mysql.port", String.class, "3306");
        configManager.setDefault("config.conf", "modules.mysql.database", String.class, "database");
        configManager.setDefault("config.conf", "modules.mysql.username", String.class, "username");
        configManager.setDefault("config.conf", "modules.mysql.password", String.class, "password");
    }

    @Override
    public String getName() {
        return "SQL";
    }

    @Override
    public boolean isEnabled() {
        Optional<Boolean> eOpt = configManager.getValue("config.conf", "modules.mysql.enabled", Boolean.class);
        return (eOpt.isPresent()) ? eOpt.get() : false;
    }

    private String getDetail(String name) {
        Optional<String> vOpt = configManager.getValue("config.conf", "modules.mysql." + name, String.class);
        return (vOpt.isPresent()) ? vOpt.get() : "";
    }

}
