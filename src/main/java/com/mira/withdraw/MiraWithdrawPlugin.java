package com.mira.withdraw;

import com.mira.withdraw.command.WithdrawCommand;
import com.mira.withdraw.listener.VoucherListener;
import com.mira.withdraw.service.EconomyService;
import com.mira.withdraw.service.VoucherService;
import com.mira.withdraw.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;

public final class MiraWithdrawPlugin extends JavaPlugin {
    private EconomyService economy;
    private VoucherService vouchers;
    private DecimalFormat moneyFormat;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        int decimals = Math.max(0, Math.min(6, getConfig().getInt("currency.decimals", 2)));
        moneyFormat = new DecimalFormat(decimals == 0 ? "0" : "0." + "0".repeat(decimals));

        economy = new EconomyService();
        if (!economy.hook()) {
            getLogger().warning("No Vault economy provider detected. Money withdrawals will be unavailable until one is present.");
        }
        vouchers = new VoucherService(this);

        var command = getCommand("withdraw");
        if (command != null) command.setExecutor(new WithdrawCommand(this, economy, vouchers));
        getServer().getPluginManager().registerEvents(new VoucherListener(this, economy, vouchers), this);
        getLogger().info("MiraWithdraw v" + getPluginMeta().getVersion() + " enabled.");
    }

    public String money(double value) {
        return getConfig().getString("currency.symbol", "$") + moneyFormat.format(value);
    }

    public String message(String key) {
        return getConfig().getString("messages." + key, "&cMissing message: " + key);
    }

    public void msg(CommandSender sender, String message) {
        sender.sendMessage(Text.c(getConfig().getString("messages.prefix", "&5[MiraWithdraw] &r") + message));
    }
}
