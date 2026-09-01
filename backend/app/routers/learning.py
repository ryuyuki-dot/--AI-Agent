import uuid
import logging

from fastapi import APIRouter, HTTPException, status

from app.schemas.learning import (
    LearningPlanRequest,
    LearningPlanResponse,
    LearningModule,
    EvidenceSource,
    TutorChatRequest,
    TutorChatResponse,
)
from app.services import gemini_client

router = APIRouter(prefix="/api/v1/learning", tags=["RAG Learning Engine"])
logger = logging.getLogger(__name__)


@router.post(
    "/plans",
    response_model=LearningPlanResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Geminiによるエビデンス付き学習シナリオ作成"
)
async def create_learning_plan(payload: LearningPlanRequest):
    try:
        raw = gemini_client.generate_learning_plan(
            topic=payload.topic,
            desired_duration_days=payload.desired_duration_days
        )
        modules = []
        for i, m in enumerate(raw.get("modules", []), start=1):
            evidences = [
                EvidenceSource(
                    source_name=e.get("source_name", "参考情報"),
                    url=e.get("url"),
                    snippet=e.get("snippet", "")
                )
                for e in m.get("evidences", [])
                if e.get("url")
            ]
            modules.append(
                LearningModule(
                    step_number=i,
                    module_id=str(uuid.uuid4()),
                    title=m.get("title", f"Step {i}"),
                    content_markdown=m.get("content_markdown", ""),
                    evidences=evidences
                )
            )

        if not modules:
            raise ValueError("Geminiの応答からモジュールを生成できませんでした。")

        return LearningPlanResponse(
            plan_id=str(uuid.uuid4()),
            topic=payload.topic,
            total_steps=len(modules),
            modules=modules
        )

    except RuntimeError as e:
        # GEMINI_API_KEY未設定など、設定不備
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail=str(e))
    except Exception as e:
        logger.exception("学習プラン生成に失敗しました")
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"学習プランの生成に失敗しました: {e}"
        )


@router.post(
    "/tutor/chat",
    response_model=TutorChatResponse,
    status_code=status.HTTP_200_OK,
    summary="生成済み教材についてGeminiの先生に質問する"
)
async def tutor_chat(payload: TutorChatRequest):
    try:
        history = [h.model_dump() for h in payload.history] if payload.history else None
        answer = gemini_client.ask_tutor(
            topic=payload.topic,
            module_title=payload.module_title,
            module_content=payload.module_content,
            question=payload.question,
            history=history
        )
        return TutorChatResponse(answer=answer)

    except RuntimeError as e:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail=str(e))
    except Exception as e:
        logger.exception("チュータリング応答生成に失敗しました")
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"回答の生成に失敗しました: {e}"
        )
