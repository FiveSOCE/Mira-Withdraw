package com.mira.withdraw.service;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class EconomyService {
    private Economy economy;

    public boolean hook() {
        var registration = Bukkit.getServicesManager().getRegistration(Economy.class);
        economy = registration == null ? null : registration.getProvider();
        return economy != null;
    }

    public boolean available() { return economy != null; }
    public double balance(Player player) { return economy == null ? 0D : economy.getBalance(player); }
    public boolean withdraw(Player player, double amount) { return economy != null && economy.withdrawPlayer(player, amount).transactionSuccess(); }
    public boolean deposit(Player player, double amount) { return economy != null && economy.depositPlayer(player, amount).transactionSuccess(); }
}
