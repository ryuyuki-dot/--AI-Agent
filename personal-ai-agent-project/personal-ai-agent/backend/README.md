# Personal AI Agent - Backend (FastAPI)

いただいたGemini生成コードを整理し、不足していた `schedules` ルーター（週末プラン提案API）と
`actions` ルーター（アプリ利用状況ログ受信・おすすめAction取得API）を追加したものです。
NLP/RAGの中身は元コード同様 TODO のスタブのままです（実運用にはLLM/ベクトルDB連携の実装が必要です）。

## エンドポイント一覧
- POST /api/v1/events/parse-notification … SNS通知テキストの予定検出
- POST /api/v1/events/{event_id}/approve … 検出イベントの承認→スケジュール登録
- POST /api/v1/events/{event_id}/reject … 検出イベントの却下
- POST /api/v1/schedules/weekend-plan … 空き時間から週末の行動提案
- POST /api/v1/learning/plans … 学習シナリオ＋エビデンス付きガイドブック生成
- POST /api/v1/actions/usage-logs … アプリ利用状況ログ送信
- GET  /api/v1/actions/recommendations … おすすめAction取得

## ローカル起動
```
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8080
```

## デプロイ（例: Google Cloud Run）
```
docker build -t personal-ai-agent-api .
gcloud run deploy personal-ai-agent-api --source . --region asia-northeast1 --allow-unauthenticated
```
デプロイ後に発行されるURL（例: https://xxxx-an.a.run.app）を、Androidアプリ側の
`ApiClient.kt` の `BASE_URL` に設定してください。

`schema.sql` はPostgreSQL用DDLです。Cloud SQL等で先にテーブルを作成してください。
