# Changelog / 変更履歴

All notable changes to **KubeJS Studio Mekanism Compat** will be documented in this file.

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

---

### 日本語
#### 追加
* **Mekanismガス ビジュアルビルダー（Createタブ対応）:**
  * ガスの表示名、16進数RGBカラーコード、テクスチャパス、放射線量（Sv/h）、タグ設定をGUIで直感的に構成可能。
  * `StartupEvents.registry('mekanism:gas', event => { ... })` スクリプトの自動生成と双方向パース。
* **Mekanismガスタグ管理（Tagsタブ対応）:**
  * `mekanism:gas` レジストリ配下のタグ閲覧・編集に対応。
  * タグメンバーの追加・削除の差分表示。
  * `ServerEvents.tags('mekanism:gas', ...)` スクリプトおよびデータパックタグJSONの生成。
* **Mekanismガス取扱機械レシピフォーム（Recipesタブ対応）:**
  * ガスを扱う機械レシピフォームおよびシリアライザーを実装。
  * ガススタック記法（例: `'100x mekanism:oxygen'`）およびガスタグ（`#...`）のサポート。
* **設定ファイル:**
  * 機能ごとの有効/無効化やデフォルト値を管理する `kubejs-studio-mekanism-compat.toml` を追加。
* **ローカライズ:**
  * 英語（`en_us.json`）および日本語（`ja_jp.json`）の完全バイリンガル対応。
