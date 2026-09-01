# Personal AI Agent - Backend (FastAPI)

いただいたGemini生成コードを整理し、不足していた `schedules` ルーター（週末プラン提案API）と
`actions` ルーター（アプリ利用状況ログ受信・おすすめAction取得API）を追加したものです。
NLP/RAGの中身は元コード同様 TODO のスタブのままです（実運用にはLLM/ベクトルDB連携の実装が必要です）。

## エンドポイント一覧
- POST /api/v1/events/parse-notification … SNS通知テキストの予定検出
- POST /api/v1/events/{event_id}/approve … 検出イベントの承認→スケジュール登録
- POST /api/v1/events/{event_id}/reject … 検出イベントの却下
- POST /api/v1/schedules/weekend-plan … 空き時間から週末の行動提案
- POST /api/v1/learning/plans … **Gemini APIで**学習シナリオ＋エビデンス付きガイドブックを生成
- POST /api/v1/learning/tutor/chat … 生成済み教材についてGeminiの先生に質問（チャット）
- POST /api/v1/actions/usage-logs … アプリ利用状況ログ送信
- GET  /api/v1/actions/recommendations … おすすめAction取得

## Gemini APIキーの設定（学習機能に必須）
1. https://aistudio.google.com/apikey でAPIキーを発行
2. ローカル実行時は環境変数として設定
   ```
   export GEMINI_API_KEY="発行したキー"
   ```
3. Cloud Runにデプロイする場合は `--set-env-vars` で渡す
   ```
   gcloud run deploy personal-ai-agent-api --source . --region asia-northeast1 \
     --allow-unauthenticated --set-env-vars GEMINI_API_KEY=発行したキー
   ```
   （キーをコマンド履歴に残したくない場合はSecret Managerの利用を推奨します）
4. 使用するモデルを変更したい場合は環境変数 `GEMINI_MODEL`（デフォルト: `gemini-2.5-flash`）で指定できます

APIキー未設定のまま `/api/v1/learning/*` を呼ぶと、503エラーで分かりやすくその旨が返ります。

## ローカル起動
```
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8080
```

## デプロイ（例1: Render.com — GUIだけで完結、初めての方におすすめ）
リポジトリ**直下**（`backend/`の外）に `render.yaml` を同梱しているので、コマンド操作なしでデプロイできます。

1. https://render.com にGitHubアカウントでサインアップ
2. ダッシュボードで「New +」→「Blueprint」を選択
3. このプロジェクトのGitHubリポジトリを選択（`render.yaml` が自動検出されます）
4. `GEMINI_API_KEY` の入力欄が出るので、**新しく発行し直した**Gemini APIキーを貼り付ける
   （画面上でしか入力しない＝コードやGitHubには一切残らないので安全です）
5. 「Apply」でデプロイ開始。数分待つと `https://personal-ai-agent-api-xxxx.onrender.com` のようなURLが発行される
6. そのURLを控えておく（次のAndroid側の設定で使います）

※ 無料プランは一定時間アクセスがないとスリープし、次のリクエスト時に起動まで数十秒かかることがあります。

## デプロイ（例2: Google Cloud Run）
```
docker build -t personal-ai-agent-api .
gcloud run deploy personal-ai-agent-api --source . --region asia-northeast1 --allow-unauthenticated
```
デプロイ後に発行されるURL（例: https://xxxx-an.a.run.app）を、Androidアプリ側の
`ApiClient.kt` の `BASE_URL` に設定してください。

`schema.sql` はPostgreSQL用DDLです。Cloud SQL等で先にテーブルを作成してください。
