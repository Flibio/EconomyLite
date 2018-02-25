/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.modules;

import org.slf4j.Logger;

public interface Module {

    /**
     * Initializes the module. The EconomyService is not available.
     * 
     * @param logger An instance of the plugin's logger.
     * @param plugin An instance of the main plugin class.
     * @return If the initialization was successful or not.
     */
    boolean initialize(Logger logger, Object plugin);

    /**
     * Post-initialization of the module. The EconomyService is now available.
     * 
     * @param logger An instance of the plugin's logger.
     * @param plugin An instance of the main plugin class.
     */
    default void postInitialization(Logger logger, Object plugin) {
    }

    /**
     * Sets all default config variables for the module.
     */
    void initializeConfig();

    /**
     * Gets the name of the module.
     * 
     * @return The name of the module.
     */
    String getName();

    /**
     * Checks if a module is enabled.
     * 
     * @return If the module is enabled or not.
     */
    boolean isEnabled();

}
