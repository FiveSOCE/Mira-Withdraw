package com.mira.withdraw.listener;

import com.mira.withdraw.MiraWithdrawPlugin;
import com.mira.withdraw.service.EconomyService;
import com.mira.withdraw.service.VoucherService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class VoucherListener implements Listener {
    private final MiraWithdrawPlugin plugin;
    private final EconomyService economy;
    private final VoucherService vouchers;

    public VoucherListener(MiraWithdrawPlugin plugin, EconomyService economy, VoucherService vouchers) {
        this.plugin = plugin;
        this.economy = economy;
        this.vouchers = vouchers;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        EquipmentSlot hand = event.getHand();
        if (hand == null) return;

        ItemStack item = hand == EquipmentSlot.HAND
                ? event.getPlayer().getInventory().getItemInMainHand()
                : event.getPlayer().getInventory().getItemInOffHand();
        if (!vouchers.isTagged(item)) return;

        event.setCancelled(true);
        VoucherService.Voucher voucher = vouchers.readAuthentic(item);
        if (voucher == null) {
            plugin.msg(event.getPlayer(), plugin.message("invalid-voucher"));
            return;
        }

        switch (voucher.type()) {
            case XP -> {
                if (voucher.xpLevels() > Integer.MAX_VALUE - (long) event.getPlayer().getLevel()) {
                    plugin.msg(event.getPlayer(), plugin.message("invalid-voucher"));
                    return;
                }
                event.getPlayer().setLevel(event.getPlayer().getLevel() + (int) voucher.xpLevels());
                consume(event.getPlayer(), hand, item);
                plugin.msg(event.getPlayer(), plugin.message("redeem-xp").replace("%amount%", Long.toString(voucher.xpLevels())));
            }
            case MONEY -> {
                if (!economy.available() && !economy.hook()) {
                    plugin.msg(event.getPlayer(), plugin.message("economy-unavailable"));
                    return;
                }
                if (!economy.deposit(event.getPlayer(), voucher.money())) {
                    plugin.msg(event.getPlayer(), plugin.message("economy-unavailable"));
                    return;
                }
                consume(event.getPlayer(), hand, item);
                plugin.msg(event.getPlayer(), plugin.message("redeem-money").replace("%amount%", plugin.money(voucher.money())));
            }
        }
    }

    private void consume(org.bukkit.entity.Player player, EquipmentSlot hand, ItemStack item) {
        ItemStack updated = item.clone();
        updated.setAmount(item.getAmount() - 1);
        if (hand == EquipmentSlot.HAND) player.getInventory().setItemInMainHand(updated.getAmount() <= 0 ? null : updated);
        else player.getInventory().setItemInOffHand(updated.getAmount() <= 0 ? null : updated);
    }
}
