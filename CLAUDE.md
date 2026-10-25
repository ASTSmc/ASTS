# CLAUDE.md

此檔案提供 Claude Code 在此儲存庫工作時的指引。

## 專案簡介

ASTS 是為「[ASTS] Allen 跨平台伺服器」開發的 Paper 插件，同時支援 Java 版與基岩版（透過 Geyser / Floodgate）。官方網站：https://asts.tw/。

- 套件群組：`tw.asts.mc`（原始碼在 `src/main/java/tw/asts/mc/asts/`）
- 主要類別：`tw.asts.mc.asts.ASTS`
- 授權：CC BY-SA 4.0（`LICENSE`）
- 語言：正體中文（訊息另附英文語系）

## 常用指令

- 建置：`./gradlew build`（產物在 `build/libs/`）
- 啟動測試伺服器：`./gradlew runServer`（run-paper，Minecraft 26.2）
- 沒有單元測試。

## 建置環境注意事項

- 建置目標為 **Java 25**（`build.gradle` 的 `targetJavaVersion`，低於此版本會觸發 Gradle toolchain）。
- 本機依賴 `libs/Residence.jar`（compileOnly）未發佈到公開 Maven，此 jar 已納入版本控制（`.gitignore` 的 `!libs/*`），缺少它會無法編譯。
- CI（`.github/workflows/gradle.yml`）目前使用 **JDK 21**，與 `build.gradle` 的 Java 25 目標不一致；更新任一側時需留意。
- 使用 paperweight userdev（`io.papermc.paperweight.userdev`），Paper API `26.2.build.+`。

## 專案結構

```
src/main/java/tw/asts/mc/asts/
├── ASTS.java                # 主類別：註冊指令、事件、配方與外部整合
├── command/                 # 指令：Command（註冊）、Asts、Menu、MenuClick、Rtp、Wtp、Pfly、PflyCommand、PflyVault
│   └── menu/                # 跨平台選單系統
│       ├── Base.java        # 選單抽象
│       ├── Inventory.java   # Java 版箱子 GUI
│       ├── Bedrock.java     # 基岩版 Floodgate 表單
│       ├── MenuItem.java
│       ├── MenuParser.java
│       └── menuParser/      # Basic（靜態選單）、Player（線上玩家列表）
├── event/                   # Event、MobAttack、AutoInvPick、Totem
├── loginstreak/             # 連續登入：LoginStreak（管理器）、LoginStreakAPI（對外 API）
├── recipe/                  # Recipe、DyeSwitch、Furnace、Totem
└── util/
    ├── action/              # 指令/伺服器動作輔助類別
    ├── adventure/           # Adventure 翻譯儲存
    ├── placeholder/         # Geyser 版本查詢
    ├── Bundle.java          # 載入 zh_TW / en_US 訊息
    ├── ExternalClass.java   # 選用依賴的反射載入
    ├── LightBlock.java      # Residence 光源限制
    ├── Log.java             # 日誌包裝
    ├── Placeholder.java     # PlaceholderAPI 擴充
    ├── PluginPermission.java# 權限常數
    ├── text.java            # 訊息/格式輔助
    └── UserConfig.java      # 玩家個人設定（user_config.yml）

src/main/resources/
├── plugin.yml               # 插件描述、權限、softdepend
├── menu.yml                 # 選單結構
├── login_streak.yml         # 連續登入玩家資料
├── user_config.yml          # 玩家個人設定範本
└── bundle/                  # 多語言訊息（Text_zh_TW.properties、Text_en_US.properties）
```

## 架構慣例

- **選用依賴**（Geyser-Spigot、floodgate、PlaceholderAPI、Residence、Vault）一律以 `compileOnly` 宣告，並透過 `util/ExternalClass` 反射載入：插件存在才實例化對應類別，避免硬依賴。新增外部整合時沿用此模式。
- **指令**使用 Paper Brigadier API（`io.papermc.paper.command.brigadier.BasicCommand`），在 `command/Command.java` 以 `LifecycleEvents.COMMANDS` 註冊，別名以 `List.of(...)` 傳入。
- **訊息**以 Adventure `Component.translatable`（`text.t(...)`）發出，翻譯在 `bundle/Text_*.properties`。新增訊息需同時加到 zh_TW 與 en_US 兩個檔案；舊式 `§` 色碼由 `text.legacy()` 轉成 MiniMessage。
- **玩家個人設定**存於 `user_config.yml`（`UserConfig`），key 格式為 `<設定>.<玩家名>`（例如 `mob_attack.AS_Allen_Shih`）。
- **選單**由 `menu.yml` 驅動：`cmd` 執行指令、`menu` 開啟子選單、`item` 為材質、`name`/`desc` 為顯示文字；`menu` 值以 `.` 分隔並轉成巢狀 key（如 `menu: "teleport.world"` → `/menu teleport world`）。

## 常用修改位置

- 新增/修改指令 → `command/`（別名與描述在 `Command.java`）。
- 新增事件監聽 → `event/`（在 `event/Event.java` 註冊）。
- 新增配方 → `recipe/`（在 `recipe/Recipe.java` 註冊）。
- 新增/調整連續登入邏輯 → `loginstreak/`（玩家資料存 `login_streak.yml`，設定在 `config.yml` 的 `login_streak.*`）。
- 新增/調整玩家飛行邏輯 → `command/Pfly.java`（每秒費用設定在 `config.yml` 的 `pfly.cost`，玩家同意紀錄在 `user_config.yml` 的 `pfly_accept`）。
- 新增訊息 → `bundle/Text_zh_TW.properties` 與 `bundle/Text_en_US.properties`。
- 新增選單/選項 → `menu.yml`。
- 新增權限 → `util/PluginPermission.java` 與 `plugin.yml`。
