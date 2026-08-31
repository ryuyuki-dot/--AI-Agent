import uuid
from fastapi import APIRouter, status
from app.schemas.schedules import WeekendPlanRequest, WeekendPlanResponse, ScheduleSuggestion

router = APIRouter(prefix="/api/v1/schedules", tags=["Weekend Planner"])

@router.post(
    "/weekend-plan",
    response_model=WeekendPlanResponse,
    status_code=status.HTTP_200_OK,
    summary="週末の空き時間をもとに時間単位の行動を提案"
)
async def create_weekend_plan(payload: WeekendPlanRequest):
    # TODO: Task Planner（行動履歴・好み・空き時間から最適化して提案生成）との連携
    suggestions = []
    if payload.free_slots:
        first = payload.free_slots[0]
        suggestions.append(ScheduleSuggestion(
            title="話題のカフェで読書＆作業",
            start_datetime=first.start_time,
            end_datetime=first.start_time,
            category="leisure",
            reason="カフェ検索アプリの検索ログと午前中の空き時間に基づき提案",
            deep_link_url="maps://search?q=渋谷+カフェ"
        ))
    return WeekendPlanResponse(
        plan_id=str(uuid.uuid4()),
        target_date=payload.target_weekend_date,
        suggestions=suggestions
    )
