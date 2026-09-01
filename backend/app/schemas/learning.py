from typing import List, Optional
from pydantic import BaseModel, Field, HttpUrl

class LearningPlanRequest(BaseModel):
    topic: str = Field(..., example="Pythonによるデータ分析基礎")
    desired_duration_days: int = Field(default=5, ge=1, le=30)

class EvidenceSource(BaseModel):
    source_name: str
    url: HttpUrl
    snippet: str

class LearningModule(BaseModel):
    step_number: int
    module_id: str
    title: str
    content_markdown: str
    evidences: List[EvidenceSource] = []

class LearningPlanResponse(BaseModel):
    plan_id: str
    topic: str
    total_steps: int
    modules: List[LearningModule]


# ===== 教材についてのQ&A（チュータリング） =====

class ChatMessage(BaseModel):
    role: str  # "user" または "assistant"
    content: str

class TutorChatRequest(BaseModel):
    topic: str = Field(..., example="Pythonによるデータ分析基礎")
    module_title: str = Field(..., example="Pandasを用いたデータ読み込みとクレンジング")
    module_content: str = Field(..., description="対象モジュールのcontent_markdown")
    question: str = Field(..., example="read_csvとread_excelの違いは何ですか？")
    history: Optional[List[ChatMessage]] = Field(default=None, description="これまでのやり取り（任意）")

class TutorChatResponse(BaseModel):
    answer: str

