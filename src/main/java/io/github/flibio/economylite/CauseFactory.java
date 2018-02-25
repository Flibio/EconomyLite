/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite;

import org.spongepowered.api.event.cause.EventContext;

import org.spongepowered.api.event.cause.Cause;

public class CauseFactory {

    public static Cause create(String reason) {
        return Cause.of(EventContext.empty(), reason, EconomyLite.getInstance().getPluginContainer());
    }

    public static Cause stringCause(String reason) {
        return Cause.of(EventContext.empty(),("economylite:" + reason.toLowerCase().replaceAll(" ", "_")));
    }
}
