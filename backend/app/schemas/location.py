
from datetime import datetime
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field


# -------------------------
# Workspace schemas
# -------------------------

class WorkspaceCreate(BaseModel):
    name: str = Field(min_length=2, max_length=150)
    description: str | None = None


class WorkspaceResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    name: str
    description: str | None
    created_at: datetime
    updated_at: datetime


# -------------------------
# Site schemas
# -------------------------

class SiteCreate(BaseModel):
    workspace_id: UUID
    name: str = Field(min_length=2, max_length=150)
    address: str | None = None


class SiteResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    workspace_id: UUID
    name: str
    address: str | None
    created_at: datetime
    updated_at: datetime


# -------------------------
# Building schemas
# -------------------------

class BuildingCreate(BaseModel):
    site_id: UUID
    name: str = Field(min_length=2, max_length=150)
    description: str | None = None


class BuildingResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    site_id: UUID
    name: str
    description: str | None
    created_at: datetime
    updated_at: datetime


# -------------------------
# Floor schemas
# -------------------------

class FloorCreate(BaseModel):
    building_id: UUID
    name: str = Field(min_length=1, max_length=100)
    floor_number: int


class FloorResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    building_id: UUID
    name: str
    floor_number: int
    created_at: datetime
    updated_at: datetime


# -------------------------
# Room schemas
# -------------------------

class RoomCreate(BaseModel):
    floor_id: UUID
    name: str = Field(min_length=1, max_length=100)
    description: str | None = None


class RoomResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    floor_id: UUID
    name: str
    description: str | None
    created_at: datetime
    updated_at: datetime


# -------------------------
# Nested hierarchy schemas
# -------------------------

class RoomTree(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    name: str
    description: str | None


class FloorTree(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    name: str
    floor_number: int
    rooms: list[RoomTree]


class BuildingTree(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    name: str
    description: str | None
    floors: list[FloorTree]


class SiteTree(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    name: str
    address: str | None
    buildings: list[BuildingTree]


class WorkspaceTree(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    name: str
    description: str | None
    sites: list[SiteTree]
