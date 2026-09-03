# MiraWithdraw

MiraWithdraw lets players convert XP levels or Vault-backed money into secure physical vouchers for the Mira Paper server suite. The vouchers can be traded or stored and later redeemed back into XP or economy balance.

## Download

[**Download MiraWithdraw v0.1.1**](https://github.com/FiveSOCE/Mira-Withdraw/releases/download/v0.1.1/MiraWithdraw-0.1.1.jar)

## Requirements / Dependencies

- Paper 1.21.11
- Java 21
- Vault optional for XP-only use
- A Vault-compatible economy provider required for money withdrawals/redemptions

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
