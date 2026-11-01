# KubeJS Studio Mekanism Compat

[English](#english) | [日本語](#japanese)

---

<a name="english"></a>
## English

### Overview
**KubeJS Studio Mekanism Compat** is a companion mod for **KubeJS Studio** on Minecraft 1.20.1 (Forge). It extends KubeJS Studio's visual interface and code generation capabilities with full, **universal** **Mekanism Gas** compatibility:
1. **Gas Registration Builder (Create Tab):** Dynamically injects the **Gases** mode into KubeJS Studio's Create Tab header toggle bar. Visually configure and create custom Mekanism gases (Display Name, Hex Color, Texture, Radioactivity, and Gas Tags) with automatic `StartupEvents.registry('mekanism:gas', ...)` script generation and Pending Dock integration.
2. **Gas Tag Management (Tags Tab):** Browse and modify tags under the `mekanism:gas` registry, with real-time diff preview and KubeJS tag script / datapack JSON export.
3. **Universal Gas Crafting & Machine Recipes (Recipes Tab):**
   * **Universal Auto-Detection Engine:** Automatically inspects and creates GUI forms for ANY gas-capable machine recipe without requiring manual per-machine hardcoding.
   * **Addon & KubeJS Custom Machine Support:** Full out-of-the-box compatibility with Mekanism addons and custom machines declared in KubeJS or Custom Machinery.
   * **Dual Script/JSON Generation:** Formats standard Mekanism method calls (`event.recipes.mekanism...`) and universal custom recipe calls (`event.custom({...})`).

### Requirements
* Minecraft: `1.20.1`
* Minecraft Forge: `47.1.0` or higher
* KubeJS: `2001.6.5` or higher
* Mekanism: `10.4.0` or higher
* (Recommended) KubeJS Studio: `1.0.0` or higher

### Installation
1. Ensure Minecraft Forge 1.20.1 and KubeJS / Mekanism are installed in your mods folder.
2. Place `kubejs_studio_mekanism_compat-1.0.0-alpha.4.jar` into your `mods/` directory.
3. Launch Minecraft, open KubeJS Studio (default hotkey: `K`), go to the **Create** tab, and click the **Gases** toggle button!

### License
This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)**.

---

<a name="japanese"></a>
## 日本語

### 概要
**KubeJS Studio Mekanism Compat** は、Minecraft 1.20.1 (Forge) 環境の **KubeJS Studio** に **Mekanism ガス（Gas）** の完全な互換性を追加する連携MODです。
1. **ガスの新規作成ビルダー（Createタブ対応）:** KubeJS StudioのCreateタブ上部トグルバー（Items / Blocks / Fluids）に「**Gases**」モードを直接動的注入。GUI上でMekanismガス（表示名、Hex色コード、テクスチャ、放射線量、ガスタグ）を定義し、Studioの保留ドック（Pending Dock）へシームレスに登録・反映。
2. **ガスタグの管理（Tagsタブ対応）:** `mekanism:gas` レジストリ配下のタグをブラウズ・編集可能。追加・削除の差分プレビューおよび KubeJSタスクスクリプト／データパックJSONへの出力をサポート。
3. **汎用ガス機械レシピ対応（Recipesタブ対応）:**
   * **動的自動解析エンジン:** レシピ構造を動的に検査し、ガス入力・ガス出力・アイテム・液体・パラメータを自動検出してGUIフォームを自動レイアウト。
   * **アドオンMOD・KubeJSカスタム機械に完全自動対応:** Mekanism本体の機械だけでなく、各種アドオンMOD（Mekanism Generators, More Mekanism Processing等）やKubeJS/Custom Machineryで自作したカスタム機械のガスレシピにも自動対応。
   * **スクリプト・JSON両対応:** 標準Mekanism形式（`event.recipes.mekanism...`）および汎用スクリプト形式（`event.custom({...})`）の両方を出力。

### 動作要件
* Minecraft: `1.20.1`
* Minecraft Forge: `47.1.0` 以上
* KubeJS: `2001.6.5` 以上
* Mekanism: `10.4.0` 以上
* （推奨）KubeJS Studio: `1.0.0` 以上

### 導入手順
1. modsフォルダにMinecraft Forge 1.20.1、KubeJS、Mekanismが導入されていることを確認します。
2. ビルドされた `kubejs_studio_mekanism_compat-1.0.0-alpha.4.jar` を `mods/` フォルダへ配置します。
3. ゲームを起動し、KubeJS Studio（デフォルトキー: `K`）を開いて、Createタブの「Gases」を選択します。

### ライセンス
本プロジェクトは **GNU General Public License v3.0 (GPL-3.0)** のもとで公開されています。
