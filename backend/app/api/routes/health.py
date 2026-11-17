
from datetime import datetime, timezone

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import text
from sqlalchemy.orm import Session

from app.core.database import get_db

router = APIRouter(prefix="/health", tags=["Health"])


@router.get("/")
def health():
    return {
        "application": "GroundMap",
        "status": "healthy",
        "timestamp": datetime.now(timezone.utc).isoformat()
    }


@router.get("/database")
def database_health(db: Session = Depends(get_db)):
    try:
        result = db.execute(text("SELECT 1")).scalar_one()

        return {
            "application": "GroundMap",
            "database": "connected",
            "query_result": result,
            "status": "healthy"
        }

    except Exception as exc:
        raise HTTPException(
            status_code=503,
            detail="Database connection failed"
        ) from exc
