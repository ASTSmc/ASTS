# [ASTS] Allen 跨平台伺服器插件

適用於 Paper 伺服器的全功能插件，同時服務 **Java 版**與**基岩版**玩家（基岩版透過 Geyser / Floodgate），以單一選單系統提供兩端一致的操作體驗。

- 官方網站：https://asts.tw/
- 支援：Paper 26.3（Java 25）
- 授權：[CC BY-SA 4.0](LICENSE)

## 功能

- **跨平台選單**：Java 玩家看到箱子 GUI（麵包屑導覽），基岩版玩家看到 Floodgate 表單，內容由 `menu.yml` 驅動。
- **世界傳送**：`/wtp` 傳送至指定世界。
- **隨機傳送**：`/rtp`，支援 `cave`（洞窟）與 `far`（遠方），含冷卻與安全落地偵測。
- **個人設定**：怪物攻擊、側邊資訊、背包自動收集，可透過 `/asts` 或選單切換。
- **背包自動收集**：破壞方塊/作物直接進背包（滿包時掉落）。
- **怪物攻擊控制**：預設怪物不攻擊玩家，玩家可自行開啟。
- **玩家頭顱掉落**：玩家被生物殺死時，機率掉落自身頭顱（機率與掠奪倍率可設定）。
- **不死圖騰地圖**：以地圖＋不死圖騰＋紙合成，具有不死圖騰效果且不可複製，支援工作台與合成器。
- **染色轉換**：用任意染料重新染色各種染色方塊。
- **礦物方塊熔煉**：原礦方塊 → 礦物方塊（熔爐／高爐／煙燻爐）。
- **連續登入**：依伺服器時區每日 00:00 起算，當日累計上線達 n 分鐘即列入，支援中斷寬限天數與對外 API。
- **玩家飛行**：`/pfly` 開啟創造模式般的飛行，透過 Vault Economy 每秒計費、掉落傷害按每顆心計費，附 actionbar 與餘額提醒。
- **襲擊調整**、**Residence 光源限制**、**PlaceholderAPI 擴充**、**多語言訊息**（正體中文／英文）。

## 需求

- 伺服器：Paper（26.3）
- Java 25
- 選用插件（依需求安裝）：
  - [Geyser-Spigot](https://geysermc.org/) 與 [Floodgate](https://geysermc.org/) — 基岩版玩家支援
  - [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) — 佔位符
  - [Residence](https://www.spigotmc.org/resources/residence.11480/) — 領地內光源使用限制
  - [Vault](https://github.com/MilkBowl/Vault)（搭配任一 Economy 插件）— 玩家飛行計費（選用，未安裝時免費飛行）

## 安裝

1. 建置或下載最新版 jar（GitHub Actions 會上傳建置產物）。
2. 將 jar 放入伺服器 `plugins/` 資料夾。
3. 重啟伺服器，插件會自動產生 `config.yml`、`menu.yml`、`user_config.yml`。

## 指令

| 指令 | 說明 | 權限 |
| --- | --- | --- |
| `/wtp <世界>` | 傳送至指定世界 | `asts.command.wtp` |
| `/rtp [cave\|far]` | 隨機傳送 | `asts.command.rtp` |
| `/menu` | 開啟跨平台選單 | `asts.command.menu` |
| `/asts` | 顯示插件資訊 | `asts.command.asts` |
| `/asts <mob\|sb\|inv> <true\|false\|toggle>` | 切換個人設定 | `asts.command.asts` |
| `/asts admin reload [save]` | 重載設定（`save` 先存檔） | `asts.admin` |
| `/pfly` | 開啟/關閉玩家飛行（首次需同意費用） | `asts.pfly` |
| `/pfly accept` | 同意飛行費用並啟用 | `asts.pfly` |
| `/asts pflyinfo` | 開關飛行資訊 actionbar 顯示 | `asts.command.asts` |
| `/asts admin pfly cost <金額>` | 設定每秒飛行費用並清除同意紀錄 | `asts.admin` |

指令別名：`/wtp` → `/worldtp`、`/wteleport`、`/worldteleport`；`/rtp` → `/randomtp`、`/rteleport`、`/randomteleport`；`/menu` → `/astsmenu`。

另：手持**指南針**點擊方塊也可開啟選單。

## 權限

- `asts.admin`（預設 OP）：全部功能，含 `/asts admin`。
- `asts.pfly`（預設開啟）：使用 `/pfly`。
- `asts.command`（預設開啟）：父權限，包含下列子權限（皆預設開啟）。
  - `asts.command.asts`
  - `asts.command.menu`
  - `asts.command.rtp`
  - `asts.command.wtp`

## 設定檔

### config.yml（插件自動產生）

| Key | 預設值 | 說明 |
| --- | --- | --- |
| `wtp.disable.worlds` | `world_nether`, `world_the_end` | 禁止世界傳送的世界 |
| `rtp.disable.worlds` | `ASTS` | 禁止隨機傳送的世界 |
| `rtp.disable.far` | `false` | 停用遠方隨機傳送 |
| `rtp.radius.default` | `10000` | 一般隨機傳送半徑 |
| `rtp.radius.far` | `100000` | 遠方隨機傳送半徑 |
| `player_head.rate` | `20` | 頭顱掉落機率（%） |
| `player_head.looting.1` / `2` / `3` | `30` / `40` / `50` | 掠奪 I / II / III 時的掉落機率（%） |
| `login_streak.minutes` | `10` | 連續登入每日達標所需上線分鐘數（n） |
| `login_streak.grace_days` | `1` | 連續登入中斷的寬限天數（d） |
| `login_streak.timezone` | 系統時區 | 連續登入計日的時區（如 `Asia/Taipei`） |
| `pfly.cost` | `200` | 玩家飛行每秒費用（Vault Economy 單位） |

### menu.yml

選單結構，支援巢狀選單與「線上玩家」動態選單，詳細格式見檔內範例。部分選單選項會執行其他插件提供的指令（如 `/shop`、`/vote`、`/home`、`/tpa`），需安裝對應插件才有作用。

### user_config.yml

玩家個人設定（如 `mob_attack.<玩家名>`、`scoreboard.<玩家名>`、`auto_inv_pick.<玩家名>`），可由指令或選單操作。

## PlaceholderAPI 佔位符

- `%asts_name%` — 伺服器名稱
- `%asts_user_mob_attack%` / `%asts_user_scoreboard%` / `%asts_user_auto_inv_pick%` — 玩家個人設定狀態
- `%asts_bedrock%` — Geyser 最新支援的基岩版版本

## 連續登入 API

其他插件可透過 `tw.asts.mc.asts.loginstreak.LoginStreakAPI` 靜態方法取得：

- `getStreak(Player | UUID)` — 目前連續登入日數（今日已列入時，回傳列入前的數值）
- `getTodayMinutes(Player | UUID)` — 今日累計上線分鐘數
- `hasQualifiedToday(Player | UUID)` — 今日是否已列入連續登入

玩家資料存放於 `login_streak.yml`（以 UUID 為 key）。

## 開發

```bash
./gradlew build      # 建置（產物在 build/libs/）
./gradlew runServer  # 啟動測試伺服器（Minecraft 26.3）
```

建置需求：Java 25。`libs/Residence.jar` 為本機依賴，需存在於 `libs/` 資料夾。

## 貢獻

1. 請勿額外使用本服未使用的插件作為依賴（可先詢問）。
2. 盡量以目前的程式碼格式進行增減。

## 授權

[CC BY-SA 4.0](LICENSE)
