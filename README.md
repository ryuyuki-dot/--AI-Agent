# Personal AI Agent（パーソナルAIエージェント）

いただいたGemini生成コード（Client/Agent Core/Data Layerの3層アーキテクチャ設計に基づくバックエンド一式）
を土台に、実際に動くAndroidアプリとして組み立てたプロジェクトです。

- `backend/` … FastAPI（提供コードを整理し、不足していた schedules / actions ルーターを追加）
- `android/` … Kotlin + Jetpack Compose のAndroidアプリ本体

## 進め方
1. `backend/README.md` に従ってバックエンドをデプロイ（Cloud Run等）
2. `android/app/build.gradle.kts` の `API_BASE_URL` にデプロイ先URLを設定
3. `android/README.md` に従ってAPKをビルド
4. APKをGoogle Driveにアップロードし、スマホからダウンロード→インストール
   （詳細な配布手順は `android/README.md` 末尾に記載）

## APKファイルが欲しい場合（Android SDKなしでビルドする方法）
この場ではAndroid SDKが使えないため実際のAPKファイルは生成できませんが、
`.github/workflows/build-apk.yml` を同梱したので、GitHubにアップロードするだけで
**自動的にAPKがビルドされます**（手元にAndroid Studioは不要）。

1. GitHubで新しいリポジトリを作成（Private可）
2. このZIPの中身一式（`android/` `backend/` `.github/` など）をそのままリポジトリにアップロード
   - GitHub上の「Add file > Upload files」でドラッグ＆ドロップでもOK
3. リポジトリの「Actions」タブを開く → 自動で "Build Android APK" が実行される
   （動かない場合は "Run workflow" ボタンで手動実行）
4. ビルドが緑色のチェックになったら、そのジョブを開き、下部の
   「Artifacts」から `personal-ai-agent-debug-apk` をダウンロード（ZIP）
5. 解凍すると `app-debug.apk` が入っているので、これをGoogle Driveにアップロードして配布

※ このワークフローはデバッグ用APK（署名なし・そのままインストール可能）を作ります。
   ストア公開等が必要な場合は署名付きreleaseビルドへの変更が別途必要です。

## 「Error parsing AndroidManifest.xml」が出た場合
このプロジェクト内の `AndroidManifest.xml` 自体はXML構文としては正常です（`xmllint`で検証済み）。
この種のエラーは、GitHubへのアップロードやWeb上での編集時に
- ファイル先頭にUTF-8 BOMが付与される
- 改行コードがCRLFに変換される
ことで発生することが多いです。`.github/workflows/build-apk.yml` にビルド前の自動正規化・検証ステップを
追加したので、次回のビルドではこれが自動修正されます。それでも失敗する場合は、
Actionsの実行結果からダウンロードできる `gradle-build-log` Artifactに詳細な原因（`--stacktrace --info`付き）
が出力されます。

## 元コードからの主な変更点
- `app.routers.schedules`（週末プランAPI）が `main.py` からimportされているのに未提供だったため新規作成
- 利用状況ログ受信・おすすめAction取得用の `app.routers.actions` を新規作成（SQLの `action_recommendations` / `user_app_usages` テーブルに対応）
- NLP/RAGの中身は元コードと同じくTODOスタブのままです（実運用にはLLM連携の実装が必要です）
