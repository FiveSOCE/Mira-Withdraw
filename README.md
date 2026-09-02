# Mira-Withdraw

Secure physical withdrawals for XP levels and Vault-backed money.

## Download

**Current release: v0.1.0**

[Download MiraWithdraw-0.1.0.jar](https://github.com/FiveSOCE/Mira-Withdraw/releases/download/v0.1.0/MiraWithdraw-0.1.0.jar)

[View all releases](https://github.com/FiveSOCE/Mira-Withdraw/releases)

## Requirements

- Paper 1.21.11
- Java 21
- Vault + a Vault-compatible economy provider for money withdrawals

## Commands

```text
/withdraw xp <amount|all>
/withdraw money <amount|all>
```

XP amounts are whole Minecraft levels. `all` withdraws all current whole levels.

Money uses the player's live Vault balance. `all` withdraws the full available balance.

## XP voucher

XP withdrawals create a single custom Experience Bottle named **Withdrawn Experience** with lore showing the stored level count.

The bottle is redeemed by right-clicking it. MiraWithdraw cancels normal Experience Bottle use, so the custom voucher is redeemed instead of being thrown.

## Money voucher

Money withdrawals create a Gold Ingot named **Withdrawn Money** with lore showing the stored balance. The ingot uses a forced enchantment glint without a visible enchantment.

Right-clicking deposits the stored value into the redeemer's Vault account and consumes one voucher.

## Security

Withdrawal vouchers use Mira-style signed PDC data. The HMAC signature covers both voucher type and stored value. MiraWithdraw also validates the entire canonical ItemStack before redemption, so altered names, lore, metadata or stored values invalidate the voucher.

A private signing secret is generated automatically in `config.yml` on first startup.

## Transaction safety

- XP levels are restored if the voucher cannot be delivered.
- Money is refunded if the voucher cannot be delivered.
- Money vouchers are only consumed after Vault confirms a successful deposit.
- Exactly one voucher is consumed per redemption.

## Permissions

- `mirawithdraw.use`
- `mirawithdraw.xp`
- `mirawithdraw.money`
