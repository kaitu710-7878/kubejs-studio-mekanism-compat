# Changelog / 変更履歴

All notable changes to **KubeJS Studio Mekanism Compat** will be documented in this file.

---

## [1.0.0-alpha.3] - 2026-11-01

### English
#### Added
* **Visible Gas Creation in Create Tab (`CreateUiHook` & `GasCreateUi`):**
  * Explicitly integrated the **Gases** mode into KubeJS Studio's Create Tab header toggle.
  * Dedicated visual editor panel with fields for ID (namespace + name), Display Name, Hex RGB Color, Custom Texture, Radioactivity (Sv/h), and Tag attachments.
  * Direct Queue & Apply workflows integrated with KubeJS Studio pending operation dock.
  * Client utility command `/kubejs_gas` (`/kubejs_gas create` and `/kubejs_gas apply`).

---

### 日本語
#### 追加
* **Createタブでの新規ガス作成モード明示的統合 (`CreateUiHook` & `GasCreateUi`):**
  * KubeJS StudioのCreateタブ上部トグル（Item / Block / Fluid）に「**Gases**」モードを確実に注入・追加。
  * ID（名前空間 : パス）、表示名、16進数RGBカラーコード、テクスチャ、放射線量（Sv/h）、タグ設定を持つビジュアル入力パネルを表示。
  * 「Queue」および「Apply」による保留キュー管理・`kubejs/startup_scripts/gases.js` への一括書き込みに対応。
  * クライアント用コマンド `/kubejs_gas`（作成画面通知および即時反映）を追加。

---

## [1.0.0-alpha.2] - 2026-11-01

### English
#### Changed
* **Universal Auto-Detection Architecture:**
  * Replaced static per-machine definitions with dynamic recipe introspection (`GasRecipeIntrospector`).
  * Automatic compatibility for arbitrary Mekanism addon machines and KubeJS-created custom machines that consume or produce gases.
  * Dynamic layout mapping for item, fluid, and gas slots, as well as arbitrary machine parameters.
  * Universal script serialization fallback using clean `event.custom({...})` scripts.

---

### 日本語
#### 変更
* **汎用レシピ自動検出アーキテクチャの導入:**
  * 特定の機械を1つ1つ固定定義する方式から、動的レシピ解析エンジン（`GasRecipeIntrospector`）による汎用対応方式へ全面刷新。
  * MekanismのアドオンMODが追加する機械や、KubeJS / Custom Machineryで作成されたカスタム機械のガスレシピ（ガス入力・ガス出力）を完全自動検出・自動レイアウト。
  * 未知の機械レシピに対しても、KubeJSの `event.custom({...})` スクリプトおよび標準データパックJSONへの双方向変換を汎用的にサポート。

---

## [1.0.0-alpha.1] - 2026-11-01

### English
#### Added
* **Mekanism Gas Visual Builder (Create Tab):**
  * Support for configuring gas display name, hex RGB color, custom texture path, radioactivity (Sv/h), and associated tags.
  * Automatic generation and round-trip parsing of `StartupEvents.registry('mekanism:gas', event => { ... })` scripts.
* **Mekanism Gas Tag Management (Tags Tab):**
  * Support for browsing and editing tags under the `mekanism:gas` registry.
  * Real-time additions/removals diffing.
  * Generation of `ServerEvents.tags('mekanism:gas', ...)` scripts and datapack tag JSONs.
* **Mekanism Gas Recipe Forms (Recipes Tab):**
  * Comprehensive GUI forms and serialization for gas-handling machines.
  * Full gas stack parsing (e.g. `'100x mekanism:oxygen'`) and tag ingredient support (`#...`).
* **Configuration:**
  * Common config file `kubejs-studio-mekanism-compat.toml` with feature toggles and default values.
* **Localization:**
  * Full bilingual support for English (`en_us.json`) and Japanese (`ja_jp.json`).
