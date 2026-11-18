
import io
import uuid

import qrcode

from fastapi import (
    APIRouter,
    Depends,
    HTTPException,
    status
)

from fastapi.responses import StreamingResponse
from fastapi.encoders import jsonable_encoder

from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.core.database import get_db

from app.models.location import Workspace, Room
from app.models.asset import Asset, AssetEvent

from app.schemas.asset import (
    AssetCreate,
    AssetUpdate,
    AssetResponse,
    AssetStatusUpdate,
    AssetConditionUpdate,
    AssetLocationUpdate,
    AssetEventResponse
)


router = APIRouter(
    prefix="/api/v1/assets",
    tags=["Asset Management"]
)


def get_asset_or_404(db: Session, asset_id: uuid.UUID):
    asset = db.get(Asset, asset_id)

    if asset is None:
        raise HTTPException(
            status_code=404,
            detail="Asset not found"
        )

    return asset



def verify_room(
    db: Session,
    room_id: uuid.UUID | None,
    workspace_id: uuid.UUID
):
    if room_id is None:
        return

    from app.models.location import Floor, Building, Site

    room = db.scalar(
        select(Room)
        .join(Floor, Room.floor_id == Floor.id)
        .join(Building, Floor.building_id == Building.id)
        .join(Site, Building.site_id == Site.id)
        .where(
            Room.id == room_id,
            Site.workspace_id == workspace_id
        )
    )

    if room is None:
        raise HTTPException(
            status_code=404,
            detail="Room not found in this workspace"
        )



def record_event(
    db: Session,
    asset_id: uuid.UUID,
    event_type: str,
    description: str,
    old_value: dict | None = None,
    new_value: dict | None = None
):
    event = AssetEvent(
        asset_id=asset_id,
        event_type=event_type,
        description=description,
        old_value=jsonable_encoder(old_value) if old_value is not None else None,
        new_value=jsonable_encoder(new_value) if new_value is not None else None
    )

    db.add(event)


def commit_record(db: Session):
    try:
        db.commit()
    except IntegrityError as exc:
        db.rollback()

        raise HTTPException(
            status_code=409,
            detail="Duplicate asset code or invalid database relationship"
        ) from exc


# ---------------------------------
# CREATE ASSET
# ---------------------------------

@router.post(
    "",
    response_model=AssetResponse,
    status_code=status.HTTP_201_CREATED
)
def create_asset(
    payload: AssetCreate,
    db: Session = Depends(get_db)
):
    workspace = db.get(Workspace, payload.workspace_id)

    if workspace is None:
        raise HTTPException(404, "Workspace not found")

    verify_room(db, payload.room_id, payload.workspace_id)

    asset = Asset(**payload.model_dump())

    db.add(asset)
    db.flush()

    record_event(
        db,
        asset.id,
        "registered",
        "Asset registered in GroundMap",
        new_value={
            "asset_code": asset.asset_code,
            "name": asset.name,
            "status": asset.status,
            "condition": asset.condition
        }
    )

    commit_record(db)
    db.refresh(asset)

    return asset


# ---------------------------------
# LIST ASSETS
# ---------------------------------

@router.get("", response_model=list[AssetResponse])
def list_assets(
    workspace_id: uuid.UUID,
    room_id: uuid.UUID | None = None,
    asset_status: str | None = None,
    db: Session = Depends(get_db)
):
    query = select(Asset).where(
        Asset.workspace_id == workspace_id
    )

    if db.get(Workspace, workspace_id) is None:
        raise HTTPException(404, "Workspace not found")

    if room_id is not None:
        query = query.where(Asset.room_id == room_id)

    if asset_status is not None:
        query = query.where(Asset.status == asset_status)

    return db.scalars(
        query.order_by(Asset.created_at.desc())
    ).all()


# ---------------------------------
# GET SINGLE ASSET
# ---------------------------------

@router.get("/{asset_id}", response_model=AssetResponse)
def get_asset(
    asset_id: uuid.UUID,
    db: Session = Depends(get_db)
):
    return get_asset_or_404(db, asset_id)


# ---------------------------------
# UPDATE ASSET DETAILS
# ---------------------------------

@router.patch("/{asset_id}", response_model=AssetResponse)
def update_asset(
    asset_id: uuid.UUID,
    payload: AssetUpdate,
    db: Session = Depends(get_db)
):
    asset = get_asset_or_404(db, asset_id)

    changes = payload.model_dump(exclude_unset=True)

    old_value = {
        key: getattr(asset, key)
        for key in changes
    }

    for key, value in changes.items():
        setattr(asset, key, value)

    if changes:
        record_event(
            db,
            asset.id,
            "updated",
            "Asset information updated",
            old_value=old_value,
            new_value=changes
        )

    commit_record(db)
    db.refresh(asset)

    return asset


# ---------------------------------
# UPDATE ASSET STATUS
# ---------------------------------

@router.patch(
    "/{asset_id}/status",
    response_model=AssetResponse
)
def update_asset_status(
    asset_id: uuid.UUID,
    payload: AssetStatusUpdate,
    db: Session = Depends(get_db)
):
    asset = get_asset_or_404(db, asset_id)

    previous = asset.status
    asset.status = payload.status

    if previous != payload.status:
        record_event(
            db,
            asset.id,
            "status_changed",
            "Asset operational status changed",
            old_value={"status": previous},
            new_value={"status": payload.status}
        )

    commit_record(db)
    db.refresh(asset)

    return asset


# ---------------------------------
# UPDATE ASSET CONDITION
# ---------------------------------

@router.patch(
    "/{asset_id}/condition",
    response_model=AssetResponse
)
def update_asset_condition(
    asset_id: uuid.UUID,
    payload: AssetConditionUpdate,
    db: Session = Depends(get_db)
):
    asset = get_asset_or_404(db, asset_id)

    previous = asset.condition
    asset.condition = payload.condition

    if previous != payload.condition:
        record_event(
            db,
            asset.id,
            "condition_changed",
            "Asset physical condition changed",
            old_value={"condition": previous},
            new_value={"condition": payload.condition}
        )

    commit_record(db)
    db.refresh(asset)

    return asset


# ---------------------------------
# ASSIGN OR MOVE ASSET
# ---------------------------------

@router.patch(
    "/{asset_id}/location",
    response_model=AssetResponse
)
def update_asset_location(
    asset_id: uuid.UUID,
    payload: AssetLocationUpdate,
    db: Session = Depends(get_db)
):
    asset = get_asset_or_404(db, asset_id)

    verify_room(
        db,
        payload.room_id,
        asset.workspace_id
    )

    previous = asset.room_id
    asset.room_id = payload.room_id

    if previous != payload.room_id:
        record_event(
            db,
            asset.id,
            "location_changed",
            "Asset location changed",
            old_value={
                "room_id": str(previous) if previous else None
            },
            new_value={
                "room_id": str(payload.room_id) if payload.room_id else None
            }
        )

    commit_record(db)
    db.refresh(asset)

    return asset


# ---------------------------------
# ASSET HISTORY
# ---------------------------------

@router.get(
    "/{asset_id}/history",
    response_model=list[AssetEventResponse]
)
def get_asset_history(
    asset_id: uuid.UUID,
    db: Session = Depends(get_db)
):
    get_asset_or_404(db, asset_id)

    return db.scalars(
        select(AssetEvent)
        .where(AssetEvent.asset_id == asset_id)
        .order_by(AssetEvent.created_at.desc())
    ).all()


# ---------------------------------
# GENERATE QR CODE
# ---------------------------------

@router.get("/{asset_id}/qr")
def generate_asset_qr(
    asset_id: uuid.UUID,
    db: Session = Depends(get_db)
):
    asset = get_asset_or_404(db, asset_id)

    qr_payload = f"groundmap://asset/{asset.id}"

    qr = qrcode.QRCode(
        version=None,
        box_size=10,
        border=4
    )

    qr.add_data(qr_payload)
    qr.make(fit=True)

    image = qr.make_image(
        fill_color="black",
        back_color="white"
    )

    buffer = io.BytesIO()
    image.save(buffer, format="PNG")
    buffer.seek(0)

    return StreamingResponse(
        buffer,
        media_type="image/png",
        headers={
            "Content-Disposition":
                f'inline; filename="{asset.asset_code}.png"'
        }
    )
