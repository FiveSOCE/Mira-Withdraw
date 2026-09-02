package com.mira.withdraw.command;

import com.mira.withdraw.MiraWithdrawPlugin;
import com.mira.withdraw.service.EconomyService;
import com.mira.withdraw.service.VoucherService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class WithdrawCommand implements CommandExecutor {
    private final MiraWithdrawPlugin plugin;
    private final EconomyService economy;
    private final VoucherService vouchers;

    public WithdrawCommand(MiraWithdrawPlugin plugin, EconomyService economy, VoucherService vouchers) {
        this.plugin = plugin;
        this.economy = economy;
        this.vouchers = vouchers;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.msg(sender, plugin.message("player-only"));
            return true;
        }
        if (!player.hasPermission("mirawithdraw.use")) {
            plugin.msg(player, plugin.message("no-permission"));
            return true;
        }
        if (args.length != 2) {
            plugin.msg(player, plugin.message("usage"));
            return true;
        }

        if (args[0].equalsIgnoreCase("xp")) return withdrawXp(player, args[1]);
        if (args[0].equalsIgnoreCase("money")) return withdrawMoney(player, args[1]);

        plugin.msg(player, plugin.message("usage"));
        return true;
    }

    private boolean withdrawXp(Player player, String raw) {
        if (!player.hasPermission("mirawithdraw.xp")) {
            plugin.msg(player, plugin.message("no-permission"));
            return true;
        }

        int available = player.getLevel();
        int levels;
        try {
            levels = raw.equalsIgnoreCase("all") ? available : Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            plugin.msg(player, plugin.message("invalid-amount"));
            return true;
        }

        if (levels <= 0) {
            plugin.msg(player, plugin.message("invalid-amount"));
            return true;
        }
        if (levels > available) {
            plugin.msg(player, plugin.message("not-enough-levels"));
            return true;
        }

        ItemStack voucher = vouchers.createXp(levels);
        player.setLevel(available - levels);
        var leftovers = player.getInventory().addItem(voucher);
        if (!leftovers.isEmpty()) {
            player.setLevel(player.getLevel() + levels);
            plugin.msg(player, plugin.message("inventory-full"));
            return true;
        }

        plugin.playPouchSound(player);
        plugin.msg(player, plugin.message("withdraw-xp").replace("%amount%", Integer.toString(levels)));
        return true;
    }

    private boolean withdrawMoney(Player player, String raw) {
        if (!player.hasPermission("mirawithdraw.money")) {
            plugin.msg(player, plugin.message("no-permission"));
            return true;
        }
        if (!economy.available() && !economy.hook()) {
            plugin.msg(player, plugin.message("economy-unavailable"));
            return true;
        }

        double balance = economy.balance(player);
        double amount;
        try {
            amount = raw.equalsIgnoreCase("all") ? balance : Double.parseDouble(raw);
        } catch (NumberFormatException ex) {
            plugin.msg(player, plugin.message("invalid-amount"));
            return true;
        }

        if (!Double.isFinite(amount) || amount <= 0D) {
            plugin.msg(player, plugin.message("invalid-amount"));
            return true;
        }
        if (amount > balance + 0.0000001D) {
            plugin.msg(player, plugin.message("not-enough-money"));
            return true;
        }

        ItemStack voucher = vouchers.createMoney(amount);
        if (!economy.withdraw(player, amount)) {
            plugin.msg(player, plugin.message("economy-unavailable"));
            return true;
        }

        var leftovers = player.getInventory().addItem(voucher);
        if (!leftovers.isEmpty()) {
            economy.deposit(player, amount);
            plugin.msg(player, plugin.message("inventory-full"));
            return true;
        }

        plugin.playPouchSound(player);
        plugin.msg(player, plugin.message("withdraw-money").replace("%amount%", plugin.money(amount)));
        return true;
    }
}
