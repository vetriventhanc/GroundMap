
from datetime import date, datetime
from typing import Literal
from uuid import UUID
from fastapi.encoders import jsonable_encoder

from pydantic import BaseModel, ConfigDict, Field


AssetStatus = Literal[
    "active",
    "maintenance",
    "inactive",
    "retired"
]

AssetCondition = Literal[
    "excellent",
    "good",
    "fair",
    "poor",
    "damaged"
]


class AssetCreate(BaseModel):
    workspace_id: UUID
    room_id: UUID | None = None

    asset_code: str = Field(min_length=2, max_length=80)
    name: str = Field(min_length=2, max_length=150)
    category: str = Field(min_length=2, max_length=100)

    manufacturer: str | None = None
    model_number: str | None = None
    serial_number: str | None = None

    purchase_date: date | None = None

    status: AssetStatus = "active"
    condition: AssetCondition = "good"

    notes: str | None = None


class AssetUpdate(BaseModel):
    name: str | None = Field(default=None, min_length=2, max_length=150)
    category: str | None = Field(default=None, min_length=2, max_length=100)

    manufacturer: str | None = None
    model_number: str | None = None
    serial_number: str | None = None

    purchase_date: date | None = None
    notes: str | None = None


class AssetStatusUpdate(BaseModel):
    status: AssetStatus


class AssetConditionUpdate(BaseModel):
    condition: AssetCondition


class AssetLocationUpdate(BaseModel):
    room_id: UUID | None = None


class AssetResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    workspace_id: UUID
    room_id: UUID | None

    asset_code: str
    name: str
    category: str

    manufacturer: str | None
    model_number: str | None
    serial_number: str | None

    purchase_date: date | None

    status: str
    condition: str
    notes: str | None

    created_at: datetime
    updated_at: datetime


class AssetEventResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    asset_id: UUID
    event_type: str
    description: str
    old_value: dict | None
    new_value: dict | None
    created_at: datetime
