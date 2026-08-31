import uuid
from fastapi import APIRouter, status
from app.schemas.learning import (
    LearningPlanRequest,
    LearningPlanResponse,
    LearningModule,
    EvidenceSource
)

router = APIRouter(prefix="/api/v1/learning", tags=["RAG Learning Engine"])

@router.post(
    "/plans",
    response_model=LearningPlanResponse,
    status_code=status.HTTP_201_CREATED,
    summary="RAGベースのエビデンス付き学習シナリオ作成"
)
async def create_learning_plan(payload: LearningPlanRequest):
    # TODO: RAG Engine (Web検索 + 論文/ドキュメントベクトル検索 + LLMロードマップ生成) 連携
    modules = [
        LearningModule(
            step_number=1,
            module_id=str(uuid.uuid4()),
            title="Pandasを用いたデータ読み込みとクレンジング",
            content_markdown="# Step 1: Pandas入門\nデータ分析の第一歩としてデータフレームの取り扱いを学びます...",
            evidences=[
                EvidenceSource(
                    source_name="Pandas Official Documentation",
                    url="https://pandas.pydata.org/docs/user_guide/io.html",
                    snippet="Pandas supports reading CSV, Excel, SQL, and JSON formats."
                )
            ]
        ),
        LearningModule(
            step_number=2,
            module_id=str(uuid.uuid4()),
            title="Matplotlib / Seabornによる可視化",
            content_markdown="# Step 2: データの視覚化\nグラフ描画の基本をマスターします...",
            evidences=[
                EvidenceSource(
                    source_name="Seaborn Tutorial",
                    url="https://seaborn.pydata.org/tutorial.html",
                    snippet="Statistical data visualization in Python."
                )
            ]
        )
    ]
    return LearningPlanResponse(
        plan_id=str(uuid.uuid4()),
        topic=payload.topic,
        total_steps=len(modules),
        modules=modules
    )
