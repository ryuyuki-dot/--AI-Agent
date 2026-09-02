"""
Gemini APIとの通信をまとめたモジュール。
Google GenAI SDK（google-genai）を使用（旧 google-generativeai は非推奨のため使用しない）。
- 学習プランの生成（プロンプトでJSON形式の出力を指示し、パースする）
- 生成済み教材についてのQ&A（チュータリング）

Gemini側が一時的に混雑している（503 UNAVAILABLE）場合に備えて、
軽いリトライ（自動再試行）を組み込んでいる。
"""
import json
import os
import re
import time
from typing import Any, Optional

from google import genai
from google.genai import errors as genai_errors

GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY")
GEMINI_MODEL = os.environ.get("GEMINI_MODEL", "gemini-3.5-flash")

_client: Optional[genai.Client] = None

_MAX_RETRIES = 3
_RETRY_DELAY_SECONDS = 3


def _get_client() -> genai.Client:
    global _client
    if not GEMINI_API_KEY:
        raise RuntimeError(
            "GEMINI_API_KEY が設定されていません。環境変数にGoogle AI Studioで発行したAPIキーを設定してください。"
        )
    if _client is None:
        _client = genai.Client(api_key=GEMINI_API_KEY)
    return _client


def _generate_with_retry(client: genai.Client, prompt: str):
    """Geminiが一時的に混雑（503）している場合に数回リトライする。"""
    last_error: Optional[Exception] = None
    for attempt in range(1, _MAX_RETRIES + 1):
        try:
            return client.models.generate_content(model=GEMINI_MODEL, contents=prompt)
        except genai_errors.APIError as e:
            is_overloaded = getattr(e, "code", None) == 503
            last_error = e
            if is_overloaded and attempt < _MAX_RETRIES:
                time.sleep(_RETRY_DELAY_SECONDS * attempt)
                continue
            raise
    raise last_error  # pragma: no cover


def _extract_json(text: str) -> Any:
    """Geminiの出力から最初のJSONブロックを取り出してパースする。"""
    fenced = re.search(r"```(?:json)?\s*(\{.*?\}|\[.*?\])\s*```", text, re.DOTALL)
    candidate = fenced.group(1) if fenced else text
    return json.loads(candidate)


def generate_learning_plan(topic: str, desired_duration_days: int) -> dict:
    """
    トピックから、ステップごとのタイトル・本文・参考情報（エビデンス）を持つ
    学習プランをJSONで生成する。
    """
    client = _get_client()

    prompt = f"""あなたは優秀な家庭教師です。次のトピックについて、
初学者向けの学習カリキュラムを作成してください。

トピック: {topic}
希望する学習日数: {desired_duration_days}日

以下のJSON形式で**のみ**出力してください（前後に説明文をつけないこと）。
必ず有効なJSONにしてください。

{{
  "modules": [
    {{
      "title": "ステップのタイトル",
      "content_markdown": "Markdown形式の解説本文（400〜800文字程度、具体例を含める）",
      "evidences": [
        {{"source_name": "参考にした情報源の名称", "url": "https://...", "snippet": "その情報源の要点を1文で"}}
      ]
    }}
  ]
}}

・モジュール数は日数に応じて3〜7個程度にすること
・evidencesは実在する可能性が高い一般的な公式ドキュメントやチュートリアルを挙げ、無ければ空配列でよい
・URLは実在しない場合、公式サイトのトップページなど確度の高いものにすること
"""

    response = _generate_with_retry(client, prompt)
    data = _extract_json(response.text)
    return data


def ask_tutor(topic: str, module_title: str, module_content: str, question: str,
              history: Optional[list[dict]] = None) -> str:
    """生成済みの学習モジュールについて、Geminiに先生役として質問に回答してもらう。"""
    client = _get_client()

    history_text = ""
    if history:
        for turn in history:
            role = "生徒" if turn.get("role") == "user" else "先生"
            history_text += f"{role}: {turn.get('content', '')}\n"

    prompt = f"""あなたは「{topic}」を教えている家庭教師です。
今、生徒は以下の教材を学習しています。

--- 教材: {module_title} ---
{module_content}
--------------------------------

これまでのやり取り:
{history_text if history_text else "（まだ質問はありません）"}

生徒からの新しい質問:
{question}

教材の内容を踏まえ、初学者にも分かりやすく、日本語で簡潔に（300文字程度を目安に）回答してください。
"""

    response = _generate_with_retry(client, prompt)
    return response.text.strip()
