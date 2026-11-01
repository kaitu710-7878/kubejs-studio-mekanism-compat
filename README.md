# KubeJS Studio Mekanism Compat

[English](#english) | [日本語](#japanese)

---

<a name="english"></a>
## English

### Overview
**KubeJS Studio Mekanism Compat** is a companion mod for **KubeJS Studio** on Minecraft 1.20.1 (Forge). It extends KubeJS Studio's visual interface and code generation capabilities with full **Mekanism Gas** compatibility:
1. **Gas Registration Builder (Create Tab):** Visually configure and create custom Mekanism gases (Display Name, Hex Color, Texture, Radioactivity, and Gas Tags) with automatic `StartupEvents.registry('mekanism:gas', ...)` script generation.
2. **Gas Tag Management (Tags Tab):** Browse and modify tags under the `mekanism:gas` registry, with real-time diff preview and KubeJS tag script / datapack JSON export.
3. **Mekanism Gas Crafting & Machine Recipes (Recipes Tab):** Full GUI forms for all gas-handling Mekanism machines (Chemical Crystallizer, Chemical Dissolution Chamber, Chemical Oxidizer, Purification Chamber, Chemical Injection Chamber, Osmium Compressor, Rotary Condensentrator, and Pressurized Reaction Chamber).

### Features
* **Seamless Studio UI Integration:** Dynamically injects dedicated gas recipe layouts and slot helpers directly into KubeJS Studio's GUI.
* **Dual Export:** Supports both standard plain KubeJS JavaScript scripts and native Minecraft/Mekanism Datapack JSON formats.
* **Fail-Safe & Standalone Ready:** Works alongside KubeJS Studio or functions independently as a recipe generation toolkit.
* **Bilingual Localization:** Fully localized in English (`en_us.json`) and Japanese (`ja_jp.json`).

### Requirements
* Minecraft: `1.20.1`
* Minecraft Forge: `47.1.0` or higher
* KubeJS: `2001.6.5` or higher
* Mekanism: `10.4.0` or higher
* (Recommended) KubeJS Studio: `1.0.0` or higher

### Installation
1. Ensure Minecraft Forge 1.20.1 and KubeJS / Mekanism are installed in your mods folder.
2. Place `kubejs-studio-mekanism-compat-1.0.0-alpha.1.jar` into your `mods/` directory.
3. Launch Minecraft, open KubeJS Studio (default hotkey: `K`), and enjoy full Mekanism Gas authoring!

### License
This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)**.

---

<a name="japanese"></a>
## 日本語

### 概要
**KubeJS Studio Mekanism Compat** は、Minecraft 1.20.1 (Forge) 環境の **KubeJS Studio** に **Mekanism ガス（Gas）** の完全な互換性を追加する連携MODです。
1. **ガスの新規作成ビルダー（Createタブ対応）:** KubeJS StudioのCreateタブからGUI上でMekanismガス（表示名、Hex色コード、テクスチャ、放射線量、ガスタグ）を定義し、`StartupEvents.registry('mekanism:gas', ...)` スクリプトを自動生成。
2. **ガスタグの管理（Tagsタブ対応）:** `mekanism:gas` レジストリ配下のタグをブラウズ・編集可能。追加・削除の差分プレビューおよび KubeJSタスクスクリプト／データパックJSONへの出力をサポート。
3. **ガスを含むクラフト・機械レシピ対応（Recipesタブ対応）:** ガスを扱うMekanismの主要マシン（化学結晶化機、化学溶解機、化学酸化機、精製室、化学注入機、オスミウム圧縮機、回転式凝縮器、加圧反応室 PRC）の専用GUIフォームとスクリプト/JSON相互変換を提供。

### 主な特徴
* **KubeJS Studio GUIとのシームレス連携:** KubeJS Studio起動時に動的にガス専用スロットやレシピフォームを注入。
* **KubeJSスクリプト & データパック両対応:** 直感的なKubeJSスクリプト生成（`event.recipes.mekanism...`）およびMekanism標準データパックJSONの両形式で出力可能。
* **日英バイリンガル完全対応:** 英語（`en_us.json`）および日本語（`ja_jp.json`）を標準同梱。

### 動作要件
* Minecraft: `1.20.1`
* Minecraft Forge: `47.1.0` 以上
* KubeJS: `2001.6.5` 以上
* Mekanism: `10.4.0` 以上
* （推奨）KubeJS Studio: `1.0.0` 以上

### 導入手順
1. modsフォルダにMinecraft Forge 1.20.1、KubeJS、Mekanismが導入されていることを確認します。
2. ビルドされた `kubejs-studio-mekanism-compat-1.0.0-alpha.1.jar` を `mods/` フォルダへ配置します。
3. ゲームを起動し、KubeJS Studio（デフォルトキー: `K`）を開くことで、ガス作成・ガスタグ・ガスレシピの各機能が利用可能になります。

### ライセンス
本プロジェクトは **GNU General Public License v3.0 (GPL-3.0)** のもとで公開されています。
