from datetime import datetime
from typing import List, Optional
from pydantic import BaseModel, Field

class FreeSlot(BaseModel):
    start_time: datetime
    end_time: datetime

class WeekendPlanRequest(BaseModel):
    target_weekend_date: str = Field(..., example="2026-09-05")
    free_slots: List[FreeSlot]

class ScheduleSuggestion(BaseModel):
    title: str
    start_datetime: datetime
    end_datetime: datetime
    category: str
    reason: str
    deep_link_url: Optional[str] = None

class WeekendPlanResponse(BaseModel):
    plan_id: str
    target_date: str
    suggestions: List[ScheduleSuggestion]
