# Changelog / 変更履歴

All notable changes to **KubeJS Studio Mekanism Compat** will be documented in this file.

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
  * Comprehensive GUI forms and serialization for 8 gas-handling machines:
    * Chemical Crystallizer (`mekanism:crystallizing`)
    * Chemical Dissolution Chamber (`mekanism:dissolution`)
    * Chemical Oxidizer (`mekanism:oxidizing`)
    * Purification Chamber (`mekanism:purifying`)
    * Chemical Injection Chamber (`mekanism:injecting`)
    * Osmium Compressor (`mekanism:compressing`)
    * Rotary Condensentrator (`mekanism:rotary`)
    * Pressurized Reaction Chamber (`mekanism:reaction`)
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
  * ガスを扱う以下の8種類の機械レシピフォームおよびシリアライザーを実装:
    * 化学結晶化機 (`mekanism:crystallizing`)
    * 化学溶解機 (`mekanism:dissolution`)
    * 化学酸化機 (`mekanism:oxidizing`)
    * 精製室 (`mekanism:purifying`)
    * 化学注入機 (`mekanism:injecting`)
    * オスミウム圧縮機 (`mekanism:compressing`)
    * 回転式凝縮器 (`mekanism:rotary`)
    * 加圧反応室 (`mekanism:reaction`)
  * ガススタック記法（例: `'100x mekanism:oxygen'`）およびガスタグ（`#...`）のサポート。
* **設定ファイル:**
  * 機能ごとの有効/無効化やデフォルト値を管理する `kubejs-studio-mekanism-compat.toml` を追加。
* **ローカライズ:**
  * 英語（`en_us.json`）および日本語（`ja_jp.json`）の完全バイリンガル対応。
