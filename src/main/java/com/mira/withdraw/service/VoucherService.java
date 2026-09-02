package com.mira.withdraw.service;

import com.mira.withdraw.MiraWithdrawPlugin;
import com.mira.withdraw.util.Text;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

public final class VoucherService {
    public enum Type { XP, MONEY }

    public record Voucher(Type type, long xpLevels, double money) {}

    private final MiraWithdrawPlugin plugin;
    private final NamespacedKey typeKey;
    private final NamespacedKey xpKey;
    private final NamespacedKey moneyKey;
    private final NamespacedKey signatureKey;
    private byte[] signingSecret;

    public VoucherService(MiraWithdrawPlugin plugin) {
        this.plugin = plugin;
        this.typeKey = new NamespacedKey(plugin, "voucher_type");
        this.xpKey = new NamespacedKey(plugin, "xp_levels");
        this.moneyKey = new NamespacedKey(plugin, "money_value");
        this.signatureKey = new NamespacedKey(plugin, "signature");
        ensureSecret();
    }

    public ItemStack createXp(long levels) {
        if (levels <= 0) throw new IllegalArgumentException("XP levels must be positive");
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Text.c(plugin.getConfig().getString("items.xp.name", "&bWithdrawn Experience")));
        meta.lore(replaceLore(plugin.getConfig().getStringList("items.xp.lore"), String.valueOf(levels)));
        meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, "xp");
        meta.getPersistentDataContainer().set(xpKey, PersistentDataType.LONG, levels);
        meta.getPersistentDataContainer().set(signatureKey, PersistentDataType.STRING, signature("xp", Long.toString(levels)));
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createMoney(double amount) {
        if (!Double.isFinite(amount) || amount <= 0D) throw new IllegalArgumentException("Money must be positive");
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Text.c(plugin.getConfig().getString("items.money.name", "&6Withdrawn Money")));
        meta.lore(replaceLore(plugin.getConfig().getStringList("items.money.lore"), plugin.money(amount)));
        meta.setEnchantmentGlintOverride(true);
        meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, "money");
        meta.getPersistentDataContainer().set(moneyKey, PersistentDataType.DOUBLE, amount);
        meta.getPersistentDataContainer().set(signatureKey, PersistentDataType.STRING, signature("money", Double.toString(amount)));
        item.setItemMeta(meta);
        return item;
    }

    public boolean isTagged(ItemStack item) {
        return item != null && !item.getType().isAir() && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer().has(typeKey, PersistentDataType.STRING);
    }

    public Voucher readAuthentic(ItemStack item) {
        if (!isTagged(item)) return null;
        ItemMeta meta = item.getItemMeta();
        String type = meta.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        String supplied = meta.getPersistentDataContainer().get(signatureKey, PersistentDataType.STRING);
        if (type == null || supplied == null) return null;

        if (type.equalsIgnoreCase("xp")) {
            Long levels = meta.getPersistentDataContainer().get(xpKey, PersistentDataType.LONG);
            if (levels == null || levels <= 0) return null;
            if (!matches(supplied, signature("xp", Long.toString(levels)))) return null;
            ItemStack canonical = createXp(levels);
            canonical.setAmount(item.getAmount());
            if (!item.isSimilar(canonical)) return null;
            return new Voucher(Type.XP, levels, 0D);
        }

        if (type.equalsIgnoreCase("money")) {
            Double amount = meta.getPersistentDataContainer().get(moneyKey, PersistentDataType.DOUBLE);
            if (amount == null || !Double.isFinite(amount) || amount <= 0D) return null;
            if (!matches(supplied, signature("money", Double.toString(amount)))) return null;
            ItemStack canonical = createMoney(amount);
            canonical.setAmount(item.getAmount());
            if (!item.isSimilar(canonical)) return null;
            return new Voucher(Type.MONEY, 0L, amount);
        }
        return null;
    }

    private List<net.kyori.adventure.text.Component> replaceLore(List<String> lines, String amount) {
        return lines.stream().map(line -> Text.c(line.replace("%amount%", amount))).toList();
    }

    private void ensureSecret() {
        FileConfiguration config = plugin.getConfig();
        String configured = config.getString("security.signing-secret", "").trim();
        if (configured.isEmpty()) {
            byte[] generated = new byte[32];
            new SecureRandom().nextBytes(generated);
            configured = Base64.getEncoder().encodeToString(generated);
            config.set("security.signing-secret", configured);
            plugin.saveConfig();
        }
        try {
            signingSecret = Base64.getDecoder().decode(configured);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("security.signing-secret must be valid Base64", ex);
        }
    }

    private String signature(String type, String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingSecret, "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal((type.toLowerCase(Locale.ROOT) + ":" + value).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign withdrawal voucher", ex);
        }
    }

    private boolean matches(String supplied, String expected) {
        return MessageDigest.isEqual(supplied.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
    }
}
