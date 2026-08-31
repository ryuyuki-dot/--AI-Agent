from datetime import datetime
from typing import Optional
from pydantic import BaseModel, Field

class ParseNotificationRequest(BaseModel):
    source_app: str = Field(..., example="LINE")
    sender: str = Field(..., example="佐藤")
    raw_text: str = Field(..., example="来週の土曜日（9月5日）の19時から渋谷で食事どうかな？")
    received_at: datetime

class DetectedEventDetail(BaseModel):
    event_id: str
    title: str
    start_datetime: datetime
    end_datetime: datetime
    location: Optional[str] = None
    has_conflict: bool = False
    conflicting_event_title: Optional[str] = None

class ParseNotificationResponse(BaseModel):
    is_schedule_intent: bool
    detected_event: Optional[DetectedEventDetail] = None

class ApproveEventResponse(BaseModel):
    event_id: str
    status: str
    created_schedule_id: str
    message: str
