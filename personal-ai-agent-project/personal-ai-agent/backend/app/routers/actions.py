import uuid
from fastapi import APIRouter, status
from app.schemas.actions import (
    UsageLogRequest, UsageLogResponse,
    ActionRecommendation, ActionRecommendationListResponse
)

router = APIRouter(prefix="/api/v1/actions", tags=["Action Recommendations"])

@router.post(
    "/usage-logs",
    response_model=UsageLogResponse,
    status_code=status.HTTP_201_CREATED,
    summary="アプリ利用状況ログの受信"
)
async def ingest_usage_logs(payload: UsageLogRequest):
    # TODO: user_app_usages テーブルへの保存
    return UsageLogResponse(received=len(payload.entries), message="利用状況を受信しました。")


@router.get(
    "/recommendations",
    response_model=ActionRecommendationListResponse,
    status_code=status.HTTP_200_OK,
    summary="利用状況に基づくおすすめActionの取得"
)
async def get_recommendations():
    # TODO: 利用ログ分析ロジックとの連携（現状はダミー）
    recs = [
        ActionRecommendation(
            recommendation_id=str(uuid.uuid4()),
            action_type="RESTAURANT_RESERVATION",
            title="よく使うグルメアプリでランチ予約",
            description="週末のランチタイムに予約が取りやすい人気店があります。",
            reason="直近1週間のグルメアプリ利用頻度が高いため",
            deep_link_url="tabelog://search?area=shibuya"
        ),
        ActionRecommendation(
            recommendation_id=str(uuid.uuid4()),
            action_type="ARTICLE_SUMMARY",
            title="未読の保存記事をまとめて要約",
            description="読書アプリに溜まっている未読記事を要約して確認できます。",
            reason="読書アプリの起動頻度に対し滞在時間が短いため",
            deep_link_url=None
        ),
    ]
    return ActionRecommendationListResponse(recommendations=recs)
