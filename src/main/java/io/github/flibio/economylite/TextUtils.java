/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.function.Consumer;

public class TextUtils {

    public static Text yesOrNo(Consumer<CommandSource> yes, Consumer<CommandSource> no) {
        Text accept =
                Text.of(TextColors.DARK_GRAY, "[", TextColors.GREEN, "YES", TextColors.DARK_GRAY, "]").toBuilder()
                        .onHover(TextActions.showText(Text.of(TextColors.GREEN, "Yes!")))
                        .onClick(TextActions.executeCallback(yes))
                        .build();
        Text deny =
                Text.of(TextColors.DARK_GRAY, "[", TextColors.RED, "NO", TextColors.DARK_GRAY, "]").toBuilder()
                        .onHover(TextActions.showText(Text.of(TextColors.RED, "No!")))
                        .onClick(TextActions.executeCallback(no))
                        .build();
        return accept.toBuilder().append(Text.of(" "), deny).build();
    }
}
