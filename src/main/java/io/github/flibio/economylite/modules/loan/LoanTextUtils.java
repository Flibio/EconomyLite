/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite.modules.loan;

import org.spongepowered.api.text.action.TextActions;

import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.Text;

public class LoanTextUtils {

    public static Text yesOrNo(String yes, String no) {
        Text accept =
                Text.of(TextColors.DARK_GRAY, "[", TextColors.GREEN, "YES", TextColors.DARK_GRAY, "]").toBuilder()
                        .onHover(TextActions.showText(Text.of(TextColors.GREEN, "Yes!")))
                        .onClick(TextActions.runCommand(yes))
                        .build();
        Text deny =
                Text.of(TextColors.DARK_GRAY, "[", TextColors.RED, "NO", TextColors.DARK_GRAY, "]").toBuilder()
                        .onHover(TextActions.showText(Text.of(TextColors.RED, "No!")))
                        .onClick(TextActions.runCommand(no))
                        .build();
        return accept.toBuilder().append(Text.of(" "), deny).build();
    }
}
