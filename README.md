# Mira-Withdraw

Secure physical withdrawals for XP levels and Vault-backed money.

## Download

**Current release: v0.1.1**

[Download MiraWithdraw-0.1.1.jar](https://github.com/FiveSOCE/Mira-Withdraw/releases/download/v0.1.1/MiraWithdraw-0.1.1.jar)

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

## XP pouch

XP withdrawals create a single custom Experience Bottle.

Examples:

- `&6&l100L`
- `&6&l1,000L`
- `&6&l10,000L`

Lore: `This is an Xp Pouch`

The bottle is redeemed by right-clicking it. MiraWithdraw cancels normal Experience Bottle use, so the custom pouch is redeemed instead of being thrown.

## Money pouch

Money withdrawals create a glowing Gold Ingot.

Examples:

- `&6&l$100`
- `&6&l$1,000`
- `&6&l$10,000`
- `&6&l$1,000,000`

Lore: `This is a Money Pouch`

Right-clicking deposits the stored value into the redeemer's Vault account and consumes one pouch.

Both successful withdrawal and redemption play the Minecraft XP-orb pickup sound. Sound, volume and pitch are configurable in `config.yml`.

## Security

Withdrawal pouches use Mira-style signed PDC data. The HMAC signature covers both pouch type and stored value. MiraWithdraw also validates the entire canonical ItemStack before redemption, so altered names, lore, metadata or stored values invalidate the pouch.

A private signing secret is generated automatically in `config.yml` on first startup.

## Transaction safety

- XP levels are restored if the pouch cannot be delivered.
- Money is refunded if the pouch cannot be delivered.
- Money pouches are only consumed after Vault confirms a successful deposit.
- Exactly one pouch is consumed per redemption.

## Permissions

- `mirawithdraw.use`
- `mirawithdraw.xp`
- `mirawithdraw.money`
