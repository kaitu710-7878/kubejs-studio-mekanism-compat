# Changelog / 変更履歴

All notable changes to **KubeJS Studio Mekanism Compat** will be documented in this file.

---

## [1.0.0-alpha.5] - 2026-11-01

### English
#### Changed & Improved
* **Native LDLib2 UI Integration & Flawless Visual Alignment:**
  * Eliminated vanilla Forge `Screen` overlay and coordinate-based button placement that overlapped KubeJS Studio's interface.
  * Injected native LDLib2 `Button` directly into `CreateUi`'s mode bar beside `Items`, `Blocks`, and `Fluids` (`gas-mode-btn`).
  * Ensured the Gases button only appears when navigating inside the **Create** tab, resolving persistent visibility on unrelated tabs.
  * Created [`GasEditorPanel.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/client/GasEditorPanel.java) entirely with LDLib2 widgets (`TextField`, `Label`, `Button`, `ScrollerView`, live color preview swatch with `ColorRectTexture`) matching KubeJS Studio's exact dark styling and Taffy layout rules.

---

### 日本語
#### 変更・改善
* **KubeJS StudioネイティブLDLib2 UIへの完全統合とデザイン調和:**
  * バニラForgeのScreenオーバーレイや座標固定によるボタン配置を完全廃止し、Studio本体のUIとの位置重複を解決。
  * `CreateUi` のモードボタングループ（Items / Blocks / Fluids）の並びへ、LDLib2ネイティブの `Button` として「**Gases**」を直接注入。Flexbox自動レイアウトにより綺麗に等幅配置。
  * Createタブを開いている時だけボタングループ内に現れる構造とし、他タブ（Recipes, Tags, Loot等）での不要な常時表示を完全解消。
  * 純粋なLDLib2ウィジェット（`TextField`, `Label`, `Button`, `ScrollerView`、`ColorRectTexture` によるリアルタイム色見本）で構成された [`GasEditorPanel.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/client/GasEditorPanel.java) を実装。KubeJS Studioと100%同一のテーマ・質感・操作感を実現。

---

## [1.0.0-alpha.4] - 2026-11-01

### English
#### Fixed
* **Fixed Missing Mode Toggle in Create Tab:**
  * Implemented [`CreateTabGasInjector.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/client/CreateTabGasInjector.java) hooking into Minecraft's screen initialization lifecycle (`ScreenEvent.Init.Post`).
  * Injects the visible `[ Gases ]` mode button onto KubeJS Studio header toolbar.
  * Implemented [`GasCreateScreen.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/client/GasCreateScreen.java) providing a native interactive GUI for Gas ID, Display Name, Hex Color (with live preview swatch), Texture, Radioactivity, and Tags.
  * Integrated with KubeJS Studio's `Workspace.INSTANCE` via [`StudioWorkspaceHelper.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/client/StudioWorkspaceHelper.java) for seamless Queue and Apply operations.

---

### 日本語
#### 修正
* **Createタブでモードトグルが表示されない不具合の根本解決:**
  * Minecraftの画面初期化イベント（`ScreenEvent.Init.Post`）を購読する [`CreateTabGasInjector.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/client/CreateTabGasInjector.java) を実装。
  * KubeJS Studio画面のツールバー上に物理的な「`§b[ Gases ]`」トグルボタンを直接追加注入。
  * ボタン押下時に開く専用GUI [`GasCreateScreen.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/client/GasCreateScreen.java) を新設。ガスID、表示名、カラーHex（リアルタイム色見本プレビュー枠付き）、テクスチャ、放射線量、タグの入力に対応。
  * [`StudioWorkspaceHelper.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/client/StudioWorkspaceHelper.java) を通じて Studio の `Workspace.INSTANCE` と連携し、「Queue」ボタンから保留ドックへの登録および反映を完全統合。

---

## [1.0.0-alpha.3] - 2026-11-01

### English
#### Added
* **Visible Gas Creation in Create Tab (`CreateUiHook` & `GasCreateUi`):**
  * Integrated the **Gases** mode into KubeJS Studio's Create Tab.
  * Dedicated visual editor panel with fields for ID, Display Name, Hex RGB Color, Texture, Radioactivity, and Tags.
  * Direct Queue & Apply workflows integrated with KubeJS Studio pending operation dock.

---

### 日本語
#### 追加
* **Createタブでの新規ガス作成モード明示的統合 (`CreateUiHook` & `GasCreateUi`):**
  * KubeJS StudioのCreateタブ上部に「**Gases**」モードを注入。
  * ID、表示名、色コード、テクスチャ、放射線量、タグの入力パネルを提供。

---

## [1.0.0-alpha.2] - 2026-11-01

### English
#### Changed
* **Universal Auto-Detection Architecture:**
  * Replaced static per-machine definitions with dynamic recipe introspection (`GasRecipeIntrospector`).
  * Automatic compatibility for arbitrary Mekanism addon machines and KubeJS-created custom machines.

---

### 日本語
#### 変更
* **汎用レシピ自動検出アーキテクチャの導入:**
  * 特定の機械を1つ1つ固定定義する方式から、動的レシピ解析エンジン（`GasRecipeIntrospector`）による汎用対応方式へ全面刷新。

---

## [1.0.0-alpha.1] - 2026-11-01

### English
#### Added
* Initial prototype with basic Mekanism Gas builder, tag management, and standard machine forms.

---

### 日本語
#### 追加
* 初期プロトタイプ（ガスビルダー、ガスタグ管理、標準機械フォーム）。
