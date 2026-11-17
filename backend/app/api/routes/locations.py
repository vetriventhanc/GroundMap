
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session, selectinload

from app.core.database import get_db
from app.models.location import (
    Workspace,
    Site,
    Building,
    Floor,
    Room
)

from app.schemas.location import (
    WorkspaceCreate,
    WorkspaceResponse,
    SiteCreate,
    SiteResponse,
    BuildingCreate,
    BuildingResponse,
    FloorCreate,
    FloorResponse,
    RoomCreate,
    RoomResponse,
    WorkspaceTree
)


router = APIRouter(
    prefix="/api/v1",
    tags=["Spatial Locations"]
)


def save_record(db: Session, record):
    try:
        db.add(record)
        db.commit()
        db.refresh(record)
        return record

    except IntegrityError as exc:
        db.rollback()

        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="A record with these details already exists."
        ) from exc


# =========================================
# WORKSPACES
# =========================================

@router.post(
    "/workspaces",
    response_model=WorkspaceResponse,
    status_code=status.HTTP_201_CREATED
)
def create_workspace(
    payload: WorkspaceCreate,
    db: Session = Depends(get_db)
):
    workspace = Workspace(**payload.model_dump())
    return save_record(db, workspace)


@router.get(
    "/workspaces",
    response_model=list[WorkspaceResponse]
)
def list_workspaces(db: Session = Depends(get_db)):
    return db.scalars(
        select(Workspace).order_by(Workspace.created_at.desc())
    ).all()


# =========================================
# SITES
# =========================================

@router.post(
    "/sites",
    response_model=SiteResponse,
    status_code=status.HTTP_201_CREATED
)
def create_site(
    payload: SiteCreate,
    db: Session = Depends(get_db)
):
    workspace = db.get(Workspace, payload.workspace_id)

    if workspace is None:
        raise HTTPException(404, "Workspace not found")

    site = Site(**payload.model_dump())
    return save_record(db, site)


@router.get(
    "/sites/{workspace_id}",
    response_model=list[SiteResponse]
)
def list_sites(
    workspace_id: UUID,
    db: Session = Depends(get_db)
):
    if db.get(Workspace, workspace_id) is None:
        raise HTTPException(404, "Workspace not found")

    return db.scalars(
        select(Site)
        .where(Site.workspace_id == workspace_id)
        .order_by(Site.created_at.desc())
    ).all()


# =========================================
# BUILDINGS
# =========================================

@router.post(
    "/buildings",
    response_model=BuildingResponse,
    status_code=status.HTTP_201_CREATED
)
def create_building(
    payload: BuildingCreate,
    db: Session = Depends(get_db)
):
    if db.get(Site, payload.site_id) is None:
        raise HTTPException(404, "Site not found")

    building = Building(**payload.model_dump())
    return save_record(db, building)


# =========================================
# FLOORS
# =========================================

@router.post(
    "/floors",
    response_model=FloorResponse,
    status_code=status.HTTP_201_CREATED
)
def create_floor(
    payload: FloorCreate,
    db: Session = Depends(get_db)
):
    if db.get(Building, payload.building_id) is None:
        raise HTTPException(404, "Building not found")

    floor = Floor(**payload.model_dump())
    return save_record(db, floor)


# =========================================
# ROOMS
# =========================================

@router.post(
    "/rooms",
    response_model=RoomResponse,
    status_code=status.HTTP_201_CREATED
)
def create_room(
    payload: RoomCreate,
    db: Session = Depends(get_db)
):
    if db.get(Floor, payload.floor_id) is None:
        raise HTTPException(404, "Floor not found")

    room = Room(**payload.model_dump())
    return save_record(db, room)


# =========================================
# COMPLETE LOCATION TREE
# =========================================

@router.get(
    "/locations/tree",
    response_model=list[WorkspaceTree]
)
def get_location_tree(
    workspace_id: UUID | None = None,
    db: Session = Depends(get_db)
):
    query = (
        select(Workspace)
        .options(
            selectinload(Workspace.sites)
            .selectinload(Site.buildings)
            .selectinload(Building.floors)
            .selectinload(Floor.rooms)
        )
        .order_by(Workspace.name)
    )

    if workspace_id is not None:
        query = query.where(Workspace.id == workspace_id)

    return db.scalars(query).all()
