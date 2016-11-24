/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2016 Flibio
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
package io.github.flibio.economylite.modules.loan;

import io.github.flibio.economylite.EconomyLite;
import io.github.flibio.utils.message.MessageStorage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.economy.EconomyTransactionEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class LoanListener {

    private MessageStorage messages = EconomyLite.getMessageStorage();
    private LoanManager loans;
    private LoanModule module;
    private double maxLoanBal;
    private double intRate;

    public LoanListener(LoanModule module) {
        this.module = module;
        this.loans = module.getLoanManager();
        this.maxLoanBal = module.getMaxLoan();
        this.intRate = module.getInterestRate();
    }

    @Listener
    public void onBalanceChange(EconomyTransactionEvent event) {
        // Check if the transaction failed due to insufficient funds
        if (event.getTransactionResult().getResult().equals(ResultType.ACCOUNT_NO_FUNDS)) {
            // Check if the UUID is in the event
            Optional<UUID> uOpt = event.getCause().first(UUID.class);
            Optional<String> sOpt = event.getCause().first(String.class);
            if (sOpt.isPresent()) {
                if (sOpt.get().equalsIgnoreCase("economylite:loan"))
                    return;
            }
            if (uOpt.isPresent()) {
                UUID uuid = uOpt.get();
                // Try to get the player
                Optional<Player> pOpt = Sponge.getServer().getPlayer(uuid);
                if (pOpt.isPresent()) {
                    Player player = pOpt.get();
                    // Get loan balance of the player
                    Optional<Double> dOpt = loans.getLoanBalance(uuid);
                    if (dOpt.isPresent()) {
                        double loanBalance = dOpt.get();
                        // Calculate amount of money the player is short of
                        Currency cur = event.getTransactionResult().getCurrency();
                        BigDecimal bal = event.getTransactionResult().getAccount().getBalance(cur);
                        BigDecimal mis = event.getTransactionResult().getAmount().subtract(bal);
                        double misDouble = mis.doubleValue();
                        // Notify player of interest rate
                        player.sendMessage(messages.getMessage("module.loan.interest", "rate", Text.of(intRate)));
                        // Check how much loan they can take out
                        double maxLoan = (maxLoanBal - loanBalance) / intRate;
                        if (maxLoan <= 0)
                            return;
                        if (maxLoan < misDouble) {
                            // Offer the player a smaller loan
                            player.sendMessage(messages.getMessage("module.loan.partial"));
                            player.sendMessage(messages.getMessage("module.loan.ask", "amount",
                                    Text.of(String.format(Locale.ENGLISH, "%,.2f", maxLoan)), "label", getPrefix(maxLoan, cur)));
                            module.tableLoans.remove(uuid);
                            module.tableLoans.put(uuid, maxLoan);
                        } else {
                            // Ask the player if they want a full loan
                            player.sendMessage(messages.getMessage("module.loan.ask", "amount", Text.of(String.format(Locale.ENGLISH, "%,.2f", mis)),
                                    "label", getPrefix(mis.doubleValue(), cur)));
                            module.tableLoans.remove(uuid);
                            module.tableLoans.put(uuid, mis.doubleValue());
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void onPlayerChat(MessageChannelEvent.Chat event, @First Player player) {
        String message = event.getRawMessage().toPlain().toLowerCase().trim();
        UUID uuid = player.getUniqueId();
        if (module.tableLoans.containsKey(uuid)) {
            if (message.equals("no")) {
                module.tableLoans.remove(uuid);
                player.sendMessage(messages.getMessage("module.loan.no"));
            } else if (message.equals("yes")) {
                double amnt = module.tableLoans.get(uuid);
                if (loans.addLoanBalance(uuid, amnt)) {
                    module.tableLoans.remove(uuid);
                    player.sendMessage(messages.getMessage("module.loan.yes"));
                } else {
                    player.sendMessage(messages.getMessage("module.loan.fail"));
                }
            } else {
                player.sendMessage(messages.getMessage("module.loan.answer"));
            }
            event.setCancelled(true);
        }
    }

    private Text getPrefix(double amnt, Currency cur) {
        Text label = cur.getPluralDisplayName();
        if (amnt == 1.0) {
            label = cur.getDisplayName();
        }
        return label;
    }
}
