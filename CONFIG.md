# Configuration Guide / コンフィグ設定ガイド

[English](#english) | [日本語](#japanese)

---

<a name="english"></a>
## English

Configuration file path: `config/kubejs-studio-mekanism-compat.toml`

### [features]
| Parameter | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `enableGasBuilder` | `boolean` | `true` | Enables the visual Mekanism Gas creation panel in KubeJS Studio's Create tab. Set to `false` to disable. |
| `enableGasTagManagement` | `boolean` | `true` | Enables browsing and editing tags under the `mekanism:gas` registry in KubeJS Studio's Tags tab. |
| `enableGasRecipes` | `boolean` | `true` | Enables dedicated recipe forms for gas-handling Mekanism machines in KubeJS Studio's Recipes tab. |

### [defaults]
| Parameter | Type | Default | Range | Description |
| :--- | :--- | :--- | :--- | :--- |
| `defaultGasColor` | `integer` | `16777215` (`0xFFFFFF`) | `0` ~ `16777215` | Default Hex RGB integer color used when opening a new gas builder dialog. |
| `defaultGasAmountMb` | `integer` | `100` | `1` ~ `100000` | Default gas amount in mB filled in recipe input/output fields. |
| `defaultRecipeNamespace` | `string` | `"kubejs"` | Any valid mod ID | Default namespace prefix applied to exported KubeJS Mekanism recipe scripts. |

---

<a name="japanese"></a>
## 日本語

設定ファイル配置場所: `config/kubejs-studio-mekanism-compat.toml`

### [features] 機能トグル
| パラメータ名 | データ型 | デフォルト値 | 説明 |
| :--- | :--- | :--- | :--- |
| `enableGasBuilder` | `boolean` | `true` | KubeJS StudioのCreateタブでMekanismガスのビジュアル作成フォームを有効化します。無効にする場合は `false` に設定します。 |
| `enableGasTagManagement` | `boolean` | `true` | KubeJS StudioのTagsタブで `mekanism:gas` レジストリ配下のタグ閲覧・編集を有効化します。 |
| `enableGasRecipes` | `boolean` | `true` | KubeJS StudioのRecipesタブでガスを扱うMekanism機械レシピフォームを有効化します。 |

### [defaults] デフォルト値
| パラメータ名 | データ型 | デフォルト値 | 設定可能範囲 | 説明 |
| :--- | :--- | :--- | :--- | :--- |
| `defaultGasColor` | `integer` | `16777215` (`0xFFFFFF`) | `0` ～ `16777215` | 新規ガス作成フォームを開いた際のデフォルト色コード（Hex RGB）。 |
| `defaultGasAmountMb` | `integer` | `100` | `1` ～ `100000` | レシピのガス入力・出力欄に初期入力されるデフォルトガス量（mB）。 |
| `defaultRecipeNamespace` | `string` | `"kubejs"` | 任意の英小文字ID | 生成されるKubeJS Mekanismレシピスクリプトに付与されるデフォルト名前空間。 |
