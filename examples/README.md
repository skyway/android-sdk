# SkyWay Examples

SkyWay Android SDK の Jetpack Compose サンプルアプリです。

## サンプル一覧

### SkyWayComposeExamples

各種 Room タイプと機能をまとめたメインのサンプルアプリです。以下のサンプルが含まれています。

- **Room Sample** — DefaultRoom を使った映像・音声・データの PubSub
- **P2P Room Sample** — P2PRoom を使った映像・音声・データの PubSub
- **SFU Room Sample** — SFURoom を使った映像・音声の PubSub
- **Auto Subscribe Sample** — Room 入室後に既存 Publication を自動サブスクライブする Web 会議サンプル
- **Video Processors Sample** — 背景ぼかし（BlurProcessor）およびバーチャル背景（VirtualBackgroundProcessor）を使ったビデオプロセッササンプル

## 利用方法

[`SkyWayCredentials.kt`](https://github.com/skyway/android-sdk/tree/main/examples/JetpackCompose/SkyWayComposeExamples/app/src/main/java/com/ntt/skyway/compose/examples/SkyWayCredentials.kt) の `APP_ID` と `SECRET_KEY` を自分のアプリID・シークレットキーに書き換えてから実行してください。

appId・secretKey は SkyWay コンソールで確認できます。
https://console.skyway.ntt.com/

---

> **Note:** AndroidView のサンプルは今後更新されません。新規開発には JetpackCompose のサンプルをご参照ください。
