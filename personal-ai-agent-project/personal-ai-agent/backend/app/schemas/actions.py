from typing import List, Optional
from pydantic import BaseModel

class AppUsageEntry(BaseModel):
    package_name: str
    app_category: Optional[str] = None
    duration_seconds: int
    last_opened_at: str

class UsageLogRequest(BaseModel):
    entries: List[AppUsageEntry]

class UsageLogResponse(BaseModel):
    received: int
    message: str

class ActionRecommendation(BaseModel):
    recommendation_id: str
    action_type: str
    title: str
    description: str
    reason: str
    deep_link_url: Optional[str] = None

class ActionRecommendationListResponse(BaseModel):
    recommendations: List[ActionRecommendation]
