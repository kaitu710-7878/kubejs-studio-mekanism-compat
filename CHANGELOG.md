# Changelog / 変更履歴

All notable changes to **KubeJS Studio Mekanism Compat** will be documented in this file.

---

## [1.0.0-alpha.8] - 2026-11-02

### English
#### Fixed & Improved
* **Create Tab Native Lifecycle Integration via `CreateUiMixin`:**
  * Resolved critical bug where selecting another category (`Items`, `Blocks`, `Fluids`) failed to restore the original Studio editor or left the `Chemicals` button permanently highlighted (`seg-active`).
  * Replaced external DOM querying with native Mixin hooks (`CreateUiMixin`) directly intercepting `setMode(Kind)` and `syncToggle()`. Selecting any standard mode now immediately hides `GasEditorPanel`, clears `seg-active`, and cleanly restores all native Studio editor elements.
  * Completely eliminated duplicate/overlapping text bug ("New fluid" / "New chemical" text stacking) by hiding all sibling Studio editor widgets when `Chemicals` is active.
* **Chemical Slot In-Game Rendering in Recipes Tab (`SlotGridSlotMixin`):**
  * Hooked `SlotGrid$Slot.drawFluid` to natively render authentic Mekanism chemical textures (via block atlas sprite) and RGB tints directly in 16x16 recipe slots instead of showing blank empty tildes (`"~"`) or missing item icons.
  * Added chemical translation component tooltips to hover popups.
* **Complex Multi-IO Recipe Parsing & Support (`UniversalMekanismRecipeForm`):**
  * Added dedicated support for multi-input / multi-output machines such as `astral_mekanism:reconstruction` (Item Input + Gas Input + Fluid Input -> Item Output + Gas Output).
  * Added fallback alias key matching (`itemOutput`, `gasInput`, `slurryInput`, `chemicalOutput`, etc.) so unmapped addon machine inputs and outputs render their true items and chemical values without leaving slots blank.
* **Full Support for All Mekanism Chemical Types:**
  * Expanded creation builder and serialization to cover **Infusion (`mekanism:infuse_type`)**, **Pigment (`mekanism:pigment`)**, **Slurry (`mekanism:slurry`)**, and **Radioactive Gases (`radioactivity > 0`)**.
  * Outputs grouped `StartupEvents.registry('mekanism:<type>', ...)` startup scripts with proper parameter filtering.

---

### 日本語
#### 修正・改善
* **`CreateUiMixin` によるCreateタブのネイティブ統合と画面切り替え・点灯バグの根本解消:**
  * Createタブで「Chemicals」を選択した後、元の「Items」「Blocks」「Fluids」をクリックしても画面が戻らない、および「Chemicals」ボタンが緑色に点灯しっぱなしになる不具合を完全解決。
  * 外部イベントによるDOM探索を廃止し、Studio本体の `CreateUi` に直接フックする [`CreateUiMixin.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/mixin/CreateUiMixin.java) を実装。`setMode` や `syncToggle` と100%同期させ、他モード選択時に即座にガスエディタを非表示化＆点灯解除し、Studio標準エディタを美しく復元。
  * 「New fluid」と「New chemical」の文字が重なって表示される不具合を、右ペインのStudio子要素の排他制御によって完全に根絶。
* **Recipesタブでのケミカル（気体・懸濁液・注入物質・染料）のゲーム内テクスチャ描画 (`SlotGridSlotMixin`):**
  * `SlotGrid$Slot.drawFluid` に介入し、レシピスロット内にMekanism公式のテクスチャスプライトおよびTint（色情報）を直接16x16でレンダリングする描画エンジンを実装。空欄の「~」記号やバケツアイコンへの化けを解消し、ゲーム内と全く同じ美しいケミカルスロット表示を実現。
  * マウスホバー時のツールチップに、化学物質の正式名称（表示名・ローカライズ名）を表示する機能を追加。
* **複合入出力レシピ（`astral_mekanism:reconstruction` 等）の完全対応 & キー自動フォールバック:**
  * アイテム入力＋ガス入力＋流体入力からアイテム出力＋ガス出力を生み出す `astral_mekanism:reconstruction` などの高度な機械レシピのフォーム定義を新設。
  * JSONキーの自動探索フォールバック（`itemOutput`, `gasInput`, `slurryInput`, `fluidInput`, `chemicalOutput` 等）を導入し、アドオンMOD独自のキー命名規則でもスロットが空欄にならずアイテムやガスが100%正確に読み込まれるよう改善。
* **全化学物質（放射線ガス、懸濁液/スラリー、染料/顔料、注入物質）の完全対応:**
  * ガスだけでなく、**注入物質 (`mekanism:infuse_type`)**、**染料 (`mekanism:pigment`)**、**懸濁液 (`mekanism:slurry`)**、および**放射線ガス（`radioactivity > 0`）** の新規作成・保留キュー・KubeJSスクリプト生成（`StartupEvents.registry`）に対応。

---

## [1.0.0-alpha.7] - 2026-11-02

### English
#### Fixed
* **Mekanism & Astral Mekanism Machine Recipes Fully Supported in Recipes Tab:**
  * **Resolved Missing & Blank Slot UI:**
    * KubeJS Studio natively only supported 4 basic recipes (`crushing`, `enriching`, `smelting`, `combining`), returning `null` for all other Mekanism machines and addons. This caused Studio to skip `buildSlotArea()`, leaving the recipe area empty or completely blank.
    * Implemented [`RecipesUiMixin.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/mixin/RecipesUiMixin.java), [`MekanismRecipeFormRegistry.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/recipe/MekanismRecipeFormRegistry.java), and [`UniversalMekanismRecipeForm.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/recipe/UniversalMekanismRecipeForm.java) supporting 17+ machines (Purifying, Injecting, Compressing, Metallurgic Infusing, Dissolution, Crystallizing, Chemical Infusing, Reaction Chamber, Rotary Condensentrator, Chemical Oxidizer, Chemical Washer, Electrolytic Separator, Thermal Evaporation, Nucleosynthesizing, Astral Gas Conversion, and Chemixing) plus dynamic auto-detection for custom addon machines.
  * **Resolved Red Exclamation Mark ("!") Slot Errors:**
    * Fixed false-positive unresolved slot errors caused by Studio only checking vanilla Item and Fluid registries in `GameState.idInRegistry`.
    * Implemented [`GameStateMixin.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/mixin/GameStateMixin.java) and [`SlotGridSlotMixin.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/mixin/SlotGridSlotMixin.java) via [`MekanismRegistryLookup.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/recipe/MekanismRegistryLookup.java) to validate Mekanism Chemical registries (Gas, Slurry, InfuseType, Pigment) and chemical tags.
  * **Integrated Type Dropdown Menu:**
    * Hooked into `RecipesUi.openTypeMenu` using Mixin `@ModifyArg` so all Mekanism and Astral machine recipe types appear directly in the recipe creation dropdown.

---

### 日本語
#### 修正
* **RecipesタブにおけるMekanism公式機械・Astral Mekanismレシピの完全対応と不具合解消:**
  * **スロットUI非表示・空欄バグの根本解消:**
    * KubeJS Studio本体は4種類（`crushing`, `enriching`, `smelting`, `combining`）しか認識できず、Purifying, Injecting, Metallurgic Infusing, Reaction, Dissolution, Crystallizing, Chemixing等のあらゆる機械で `matchForm` が `null` を返していました。その結果、Studio本体の `buildSlotArea()` の呼び出しがスキップされ、加工前・加工後のスロットUIが表示されない、または中身が空欄になる致命的不具合が発生していました。
    * [`RecipesUiMixin.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/mixin/RecipesUiMixin.java)、[`MekanismRecipeFormRegistry.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/recipe/MekanismRecipeFormRegistry.java)、[`UniversalMekanismRecipeForm.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/recipe/UniversalMekanismRecipeForm.java) を新設し、Mekanism公式全機械、Astral Mekanism（`chemixing`, `ame_gas_conversion`等）、および未知のアドオン機械への動的自動推論を完備。
  * **入出力スロットの赤い「!」エラーマーク（Unresolved）の完全解消:**
    * KubeJS StudioはスロットIDを `GameState.idInRegistry` で検証しますが、ItemとFluidレジストリしか検索しないため、MekanismのGasやChemical ID（`mekanism:oxygen`, `astral_mekanism:astral_ether` 等）が存在しないIDと誤認され、赤い「!」マークが表示されていました。
    * [`GameStateMixin.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/mixin/GameStateMixin.java) と [`SlotGridSlotMixin.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/mixin/SlotGridSlotMixin.java) を実装し、[`MekanismRegistryLookup.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/recipe/MekanismRegistryLookup.java) を通じてGas/Slurry/InfuseType/Pigmentおよびガスタグを有効判定するように拡張。エラーアイコンの誤表示を100%解消しました。
  * **新規レシピ作成時のTypeピッカーメニューへの統合:**
    * `RecipesUi.openTypeMenu` に Mixin `@ModifyArg` を注入し、Studioのタイプ選択ドロップダウンにすべてのMekanism機械フォームが自動的に登録・表示されるよう連携を強化しました。

---

## [1.0.0-alpha.6] - 2026-11-01

### English
#### Fixed
* **Fixed Left Pane Visual Corruption & Editor Misalignment:**
  * Resolved critical bug where `GasEditorPanel` was accidentally injected into the left browser pane (`browserPanel`), overlapping the search bar and item list.
  * Corrected target insertion point to `SplitView.second` (the right editor container), preserving the left browser pane intact without any visual distortion.
* **Proper Editor Pane Toggle & Mutual Exclusion:**
  * When clicking the mode button (`Chemicals`), KubeJS Studio's original right editor pane is cleanly hidden via `setDisplay(false)` and `GasEditorPanel` is displayed with `setDisplay(true)`.
  * Attached capturing event listeners to `modeBar` and sibling buttons (`Items`, `Blocks`, `Fluids`) so selecting any other category immediately deactivates `Chemicals`, hides `GasEditorPanel`, and restores standard Studio editor view without button state desynchronization.
* **Pixel-Perfect Right Editor Layout:**
  * Redesigned [`GasEditorPanel.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/client/GasEditorPanel.java) to mirror KubeJS Studio's right editor layout:
    * Header: `New chemical` & `Give it an id, then queue it` with `Output: KubeJS only`.
    * Id row: `Id` `[kubejs]` `:` `[path]`.
    * Type row: `Type` with selector dropdown (`gas`, `infuse_type`, `pigment`, `slurry`).
    * Properties scroller with `list-inset` class (`Display name`, `Colour` with live swatch, `Texture`, `Radioactivity`, `Tags`).
    * Bottom action row with `Revert` and `Queue` (`primary-button`).

---

### 日本語
#### 修正
* **左ペインの表示崩れ・重なりバグの根本解消:**
  * エディタの挿入対象判定の誤りにより、左側のブラウザペイン（`browserPanel`）内に `GasEditorPanel` が誤って追加され、検索欄やリストの上に文字やフォームが被っていた不具合を修正。
  * `modeBar` の親階層から `SplitView` を直接取得し、エディタを右ペイン（`SplitView.second`）へ正確に配置。左側のブラウザペインへの干渉を完全撤廃し、UIの重なり・破損を100%解消。
* **右ペインエディタの排他切り替えとトグル同期の修正:**
  * 「Chemicals」ボタン押下時に、Studio本体の元エディタを `setDisplay(false)` で完全に非表示化し、`GasEditorPanel` を `setDisplay(true)` で美しく排他表示。
  * `modeBar` 全体および各兄弟ボタン（`Items`, `Blocks`, `Fluids`）にキャプチャフェーズのリスナーを登録。他のモードが選ばれた瞬間に即座に `Chemicals` のアクティブ状態を解除し、ガスエディタを非表示にして通常エディタに復帰。ボタンが両方アクティブに見える状態を完全に防止。
* **Studio標準の右ペインエディタと100%一致するレイアウト改善:**
  * [`GasEditorPanel.java`](src/main/java/com/github/antigravity/kubejsstudio/mekanism/client/GasEditorPanel.java) の階層構造・クラス・余白をStudio本体と同一に刷新:
    * ヘッダー: `New chemical`、`Give it an id, then queue it`、右端 `Output: KubeJS only`。
    * ID行: `Id` `[kubejs]` `:` `[path]`。
    * Type行: `Type` セレクター（`gas`, `infuse_type`, `pigment`, `slurry`）。
    * スクロール部: `list-inset` クラス適用（表示名、Hex色コード＋見本スウォッチ、テクスチャ、放射線量、タグ）。
    * 下部アクション: `Revert` ボタンおよび `Queue` ボタン（`primary-button`）。

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
