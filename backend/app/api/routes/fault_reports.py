
import uuid

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.models.asset import Asset, AssetEvent
from app.models.fault_report import FaultReport
from app.schemas.fault_report import (
    FaultReportCreate,
    FaultReportResponse,
)


router = APIRouter(
    prefix="/api/v1/assets",
    tags=["Fault Reports"]
)


def get_asset_or_404(db: Session, asset_id: uuid.UUID):
    asset = db.get(Asset, asset_id)

    if asset is None:
        raise HTTPException(
            status_code=404,
            detail="Asset not found"
        )

    return asset


@router.post(
    "/{asset_id}/fault-reports",
    response_model=FaultReportResponse,
    status_code=status.HTTP_201_CREATED
)
def create_fault_report(
    asset_id: uuid.UUID,
    payload: FaultReportCreate,
    db: Session = Depends(get_db)
):
    asset = get_asset_or_404(db, asset_id)

    report = FaultReport(
        asset_id=asset.id,
        title=payload.title,
        description=payload.description,
        severity=payload.severity,
        status="open"
    )

    try:
        db.add(report)
        db.flush()

        event = AssetEvent(
            asset_id=asset.id,
            event_type="fault_reported",
            description=f"Fault reported: {report.title}",
            new_value={
                "fault_report_id": str(report.id),
                "title": report.title,
                "severity": report.severity,
                "status": report.status
            }
        )

        db.add(event)
        db.commit()
        db.refresh(report)

    except IntegrityError as exc:
        db.rollback()
        raise HTTPException(
            status_code=409,
            detail="Unable to save fault report"
        ) from exc

    return report


@router.get(
    "/{asset_id}/fault-reports",
    response_model=list[FaultReportResponse]
)
def list_fault_reports(
    asset_id: uuid.UUID,
    db: Session = Depends(get_db)
):
    get_asset_or_404(db, asset_id)

    query = (
        select(FaultReport)
        .where(FaultReport.asset_id == asset_id)
        .order_by(FaultReport.created_at.desc())
    )

    return db.scalars(query).all()
