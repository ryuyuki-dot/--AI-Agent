import uuid
from datetime import datetime
from fastapi import APIRouter, status
from app.schemas.events import (
    ParseNotificationRequest,
    ParseNotificationResponse,
    DetectedEventDetail,
    ApproveEventResponse
)

router = APIRouter(prefix="/api/v1/events", tags=["Events & Notifications"])

@router.post(
    "/parse-notification",
    response_model=ParseNotificationResponse,
    status_code=status.HTTP_200_OK,
    summary="SNS通知テキストのインテント解析"
)
async def parse_notification(payload: ParseNotificationRequest):
    # TODO: SLM/NLPを用いた意図解釈・日時特定ロジックの実装（現状は簡易ルール判定）
    if any(k in payload.raw_text for k in ["食事", "飲み会", "予定", "会う", "ランチ"]):
        detected = DetectedEventDetail(
            event_id=str(uuid.uuid4()),
            title=f"{payload.sender}と食事",
            start_datetime=datetime.fromisoformat("2026-09-05T19:00:00+09:00"),
            end_datetime=datetime.fromisoformat("2026-09-05T21:00:00+09:00"),
            location="渋谷",
            has_conflict=False,
            conflicting_event_title=None
        )
        return ParseNotificationResponse(is_schedule_intent=True, detected_event=detected)

    return ParseNotificationResponse(is_schedule_intent=False, detected_event=None)


@router.post(
    "/{event_id}/approve",
    response_model=ApproveEventResponse,
    status_code=status.HTTP_200_OK,
    summary="検出イベントの承認およびスケジュール登録"
)
async def approve_event(event_id: str):
    # TODO: DBステータス更新 (PENDING -> APPROVED) および schedules テーブルへの登録
    schedule_id = str(uuid.uuid4())
    return ApproveEventResponse(
        event_id=event_id,
        status="APPROVED",
        created_schedule_id=schedule_id,
        message="スケジュール登録が完了しました。"
    )


@router.post(
    "/{event_id}/reject",
    status_code=status.HTTP_200_OK,
    summary="検出イベントの却下"
)
async def reject_event(event_id: str):
    # TODO: DBステータス更新 (PENDING -> REJECTED)
    return {"event_id": event_id, "status": "REJECTED"}
