/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2017 Flibio
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
