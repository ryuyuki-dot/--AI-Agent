from typing import List
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
