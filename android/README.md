# Personal AI Agent - Android アプリ

## 構成
- Kotlin + Jetpack Compose（Material3）
- Retrofit でバックエンド（`../backend`）と通信
- WorkManager でアプリ利用状況を定期収集
- NotificationListenerService でSNS通知（LINE等）を監視

## 実装した4機能と対応箇所
| 機能 | 実装ファイル |
|---|---|
| 週末の時間単位の行動提案 | `ui/WeekendPlanScreen.kt` → `POST /api/v1/schedules/weekend-plan` |
| SNSメッセージの予定検出→承認ポップアップ→スケジュール反映 | `service/SnsNotificationListenerService.kt`, `service/EventApprovalReceiver.kt` |
| アプリ利用状況からのおすすめAction提案 | `worker/UsageStatsWorker.kt`, `ui/RecommendationScreen.kt` |
| 学習シナリオ＋エビデンス付きガイドブック作成 | `ui/LearningScreen.kt` → `POST /api/v1/learning/plans` |

## ビルド手順（Android Studio）
1. Android Studio で `android/` フォルダを開く（Gradle Wrapperは自動生成されます）
2. `app/build.gradle.kts` の `API_BASE_URL` を、デプロイしたバックエンドのURLに変更
3. Build > Generate Signed Bundle / APK... で **APK** を選択し、release APKを作成
   （社内配布用の自己署名で問題ありません）

## コマンドラインでビルドする場合
```
cd android
gradle wrapper          # gradlew を生成（初回のみ、Gradleが別途必要）
./gradlew assembleRelease
# 生成物: app/build/outputs/apk/release/app-release.apk
```

## 初回起動後に必要な設定（アプリ内の「設定」タブから遷移可能）
- 通知へのアクセス（設定 > アプリ > 特別なアプリアクセス > 通知アクセス）→ 本アプリをON
- 使用状況へのアクセス（設定 > アプリ > 特別なアプリアクセス > 使用状況アクセス）→ 本アプリをON
- 通知の許可（Android 13以降はダイアログで許可）

## Google Driveでの配布方法
1. 上記でビルドした `app-release.apk` をGoogle Driveにアップロード
2. 共有リンクを発行（「リンクを知っている全員」等）
3. Androidスマホ側で、Driveアプリからそのリンク／ファイルを開き「ダウンロード」
4. ダウンロード完了後、通知またはファイルアプリからAPKをタップ
   - 初回は「このソースからのアプリのインストールを許可」の確認が出るのでON
5. インストール完了と同時に、アプリ一覧（ホーム画面のドロワー）にアイコンが追加されます
   - ホーム画面に直接置きたい場合は、アイコンを長押しして「ホーム画面に追加」

※ ZIPで固めて配布する必要はありません。APKファイル単体をそのままDrive経由で渡せます。
