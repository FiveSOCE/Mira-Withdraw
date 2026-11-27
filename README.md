# MiraWithdraw

## Version-specific downloads

| Paper server | Java | JAR name | Download |
| --- | --- | --- | --- |
| **1.18.2** | Java 17 | `MiraWithdraw_1.18.2.jar` | [Legacy release](https://github.com/FiveSOCE/Mira-Withdraw/releases/tag/dev-1.18.2) |
| **1.21.11–26.2** | Java 21 baseline; modern server may require Java 25 | `MiraWithdraw_1.21.11-26.2.jar` | [Modern release](https://github.com/FiveSOCE/Mira-Withdraw/releases/tag/compat-modern-1.21.11-26.2) |

Assets become available after each successful CI run. Intermediate Minecraft 1.19–1.21.10 and full in-game testing remain uncertified.

[Legacy source](https://github.com/FiveSOCE/Mira-Withdraw/tree/compat/1.18.2) · [Modern source](https://github.com/FiveSOCE/Mira-Withdraw) · [All releases](https://github.com/FiveSOCE/Mira-Withdraw/releases)

MiraWithdraw converts XP levels and economy balance into authenticated, signed physical vouchers. Right-clicking redeems the stored value; payout is verified before consumption.

## Dependencies

- **Required:** Paper and Java appropriate for the chosen server version.
- **Optional:** Vault and a Vault-compatible economy for money vouchers. XP vouchers work without an economy.
- **MiraCore:** Not required.
- Backup `plugins/MiraWithdraw/`, especially the signing secret, before migration. Existing pouches rely on the original secret.

## Commands and permissions

| Command | Permission | Description |
| --- | --- | --- |
| `/withdraw xp <amount\|all>` | `mirawithdraw.use` + `mirawithdraw.xp` | Convert XP levels into pouches |
| `/withdraw money <amount\|all>` | `mirawithdraw.use` + `mirawithdraw.money` | Convert balance into signed money pouches |

The nodes `mirawithdraw.use`, `mirawithdraw.xp` and `mirawithdraw.money` default to everyone.

## How MiraWithdraw Works

`/withdraw xp <amount|all>` removes whole Minecraft XP levels and creates one custom Experience Bottle pouch containing that value. `/withdraw money <amount|all>` removes money from the player's live Vault balance and creates one custom Gold Ingot money pouch. `all` withdraws the player's entire available balance for the chosen type.

Right-clicking an XP pouch redeems the stored levels instead of throwing the bottle. Right-clicking a money pouch deposits the stored value through Vault and consumes one pouch only after the deposit succeeds. Successful withdrawals and redemptions play a configurable XP-orb pickup sound.

Pouches use signed persistent data. The HMAC signature covers the pouch type and stored value, and MiraWithdraw validates the canonical ItemStack so changes to name, lore, metadata or value invalidate the pouch. A signing secret is generated in `config.yml`; changing it after pouches have been issued will invalidate old pouches.

Transaction safety restores XP or refunds money if a newly created pouch cannot be delivered, and money pouches are not consumed until Vault confirms a successful deposit.

## Commands

| Command | Permission | What it does |
| --- | --- | --- |
| `/withdraw xp <amount>` | `mirawithdraw.use` + `mirawithdraw.xp` | Converts the specified whole XP levels into one physical XP pouch. |
| `/withdraw xp all` | `mirawithdraw.use` + `mirawithdraw.xp` | Converts all current whole XP levels into one pouch. |
| `/withdraw money <amount>` | `mirawithdraw.use` + `mirawithdraw.money` | Converts the specified Vault balance into one physical money pouch. |
| `/withdraw money all` | `mirawithdraw.use` + `mirawithdraw.money` | Converts the player's full available Vault balance into one pouch. |

Voucher redemption is performed by right-clicking the physical pouch rather than through a command.

## Permissions

| Permission | Default | What it does |
| --- | --- | --- |
| `mirawithdraw.use` | Everyone | Allows use of the `/withdraw` command. |
| `mirawithdraw.xp` | Everyone | Allows XP-level withdrawals. |
| `mirawithdraw.money` | Everyone | Allows Vault money withdrawals. |
